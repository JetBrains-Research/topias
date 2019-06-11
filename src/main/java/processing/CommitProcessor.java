package processing;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.Change.Type;
import db.dao.MethodsChangelogDAO;
import db.dao.MethodsDictionaryDAO;
import db.entities.MethodChangeLogEntity;
import db.entities.MethodDictionaryEntity;
import git4idea.GitCommit;
import handlers.commit.AddedChangeHandler;
import handlers.commit.DeletedChangeHandler;
import handlers.commit.ModifiedChangeHandler;
import handlers.commit.MovedChangeHandler;
import kotlin.Pair;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.ChangesState;
import state.MethodInfo;
import state.MethodsStorage;
import state.RefactoringData;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static processing.Utils.buildDBUrlForSystem;

public final class CommitProcessor {
    private final static Logger logger = LoggerFactory.getLogger(CommitProcessor.class);
    private final Map<Type, BiFunction<Project, Change, Optional<List<MethodInfo>>>> handlers;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Project project;
    private final Repository repository;
    private final MethodsChangelogDAO methodsChangelogDAO;
    private final MethodsDictionaryDAO methodsDictionaryDAO;
    private String branchName;
    private String hashValue;
    private boolean foundLastHash = false;
    private final ProgressIndicator indicator;
    private int commitCountToProcess;
    double count = 0.0;

    public CommitProcessor(Project project, String branchName, ProgressIndicator indicator, int commitCountToProcess) throws Exception {
        handlers = new HashMap<>();
        this.indicator = indicator;
        this.commitCountToProcess = commitCountToProcess;
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = branchName;

        this.hashValue = Objects.requireNonNull(ChangesState.getInstance(project).getState())
                .persistentState
                .get(branchName);

        this.methodsChangelogDAO = new MethodsChangelogDAO(buildDBUrlForSystem(project));
        this.methodsDictionaryDAO = new MethodsDictionaryDAO(buildDBUrlForSystem(project));

        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    void processCommit(GitCommit commit) {
        final String authorName = commit.getAuthor().getEmail();
        final long commitTime = commit.getCommitTime();

        processNewCommit(commit.getChanges(),
                commit.getId().asString(), authorName, commitTime);

        ChangesState.getInstance(project).getState().
                persistentState.put(branchName, commit.getId().asString());
        count++;
        indicator.setFraction(count / commitCountToProcess);
    }

    private void processNewCommit(Collection<Change> changes, String commitId, String authorName, long commitTime) {
        final List<Change> javaChanges = changes.stream()
                .filter(x -> x.toString().substring(x.toString().lastIndexOf('.') + 1).equalsIgnoreCase("java"))
                .collect(Collectors.toList());

        final List<MethodInfo> changedMethods = javaChanges.stream()
                .map(x -> handlers.get(x.getType()).apply(project, x))
                .filter(Optional::isPresent)
                .flatMap(x -> x.get().stream())
                .collect(Collectors.toList());

        long start = currentTimeMillis();
        final RefactoringProcessor processor = new RefactoringProcessor(changedMethods);
        final List<RefactoringData> data = new LinkedList<>();
        miner.churnAtCommit(repository, commitId, new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(processor::process).filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedList::new)));
            }
        });
        logger.info("Refactorings were processed for " + (currentTimeMillis() - start) / 1000.0 + " secs!");

        final MethodsStorage methodsStorage = MethodsStorage.getInstance();
        final List<MethodInfo> deleted = methodsStorage.getDeletedMethods();
        final List<MethodInfo> added = methodsStorage.getAddedMethods();
        final List<RefactoringData> moved = methodsStorage.getMovedMethods();

        moved.forEach(x -> {
                added.remove(x.getNewMethod());
                deleted.remove(x.getOldMethod());
        });

        for (RefactoringData refactoringData : data) {
            deleted.remove(refactoringData.getOldMethod());
            added.remove(refactoringData.getNewMethod());
            moved.remove(data);
        }

        data.addAll(moved);
        methodsDictionaryDAO.addToDictionary(added
                .stream()
                .map(x -> new MethodDictionaryEntity(x.getMethodFullName(), x.getStartOffset(), x.getFileName()))
                .collect(Collectors.toList()));

        deleted.forEach(x -> methodsDictionaryDAO.removeFromDictionary(x.getMethodFullName()));

        methodsDictionaryDAO.updateBySignature(data.stream().map(x -> new Pair<>(
                x.getOldMethod().getMethodFullName(),
                new MethodDictionaryEntity(x.getNewMethod().getMethodFullName(),
                        x.getNewMethod().getStartOffset(),
                        x.getNewMethod().getFileName())
        )).collect(Collectors.toList()));


        methodsDictionaryDAO.addToDictionary(
                methodsStorage.getRecalcMethods()
                        .stream()
                        .map(x -> new MethodDictionaryEntity(x.getMethodFullName(),
                                x.getStartOffset(),
                                x.getFileName()))
                        .collect(Collectors.toList())
        );
        //Signature position updating
        //methodsStorage.getRecalcMethods().forEach(x -> methodsDictionaryDAO.dumbUpsertOfNotChangedMethodEntries(new MethodDictionaryEntity(x.getMethodFullName(), x.getStartOffset(), x.getFileName())));

//        start = currentTimeMillis();
//        methodsDictionaryDAO.upsertOfNotChangedMethodEntries(
//                methodsStorage.getRecalcMethods()
//                        .stream()
//                        .map(x -> new MethodDictionaryEntity(x.getMethodFullName(),
//                                x.getStartOffset(),
//                                x.getFileName()))
//                        .collect(Collectors.toList())
//        );
//        logger.info("Update of methods offsets took only " + (currentTimeMillis() - start) / 1000.0 + " secs!");
        //Just clearing
        methodsStorage.clear();

        changedMethods.forEach(x -> {
            x.setAuthorInfo(authorName);
            x.setTimeChangeMade(commitTime);
            x.setBranchName(branchName);
        });

        start = currentTimeMillis();
        //if method existed and was modified but not listed in dictionary
        methodsDictionaryDAO.addToDictionary(changedMethods
                .stream()
                .map(x -> new MethodDictionaryEntity(x.getMethodFullName(), x.getStartOffset(), x.getFileName()))
                .collect(Collectors.toList()));

        final List<MethodChangeLogEntity> entities = methodsDictionaryDAO.buildChangelogs(changedMethods);
        logger.info("Changelog build took only " + (currentTimeMillis() - start) / 1000.0 + " secs!");
        start = currentTimeMillis();
        methodsChangelogDAO.insertMethodsChanges(entities);
        logger.info("Stats upserting took only " + (currentTimeMillis() - start) / 1000.0 + " secs!");
    }
}
