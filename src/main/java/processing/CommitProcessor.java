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
    private final Repository repository;
    private final MethodsChangelogDAO methodsChangelogDAO;
    private final MethodsDictionaryDAO methodsDictionaryDAO;
    private Project project;
    private String branchName;
    private final ProgressIndicator indicator;
    private int commitCountToProcess;
    double count = 0.0;

    public CommitProcessor(Project project, String branchName, ProgressIndicator indicator, int commitCountToProcess) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        this.indicator = indicator;
        this.commitCountToProcess = commitCountToProcess;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = branchName;

        this.methodsChangelogDAO = new MethodsChangelogDAO(buildDBUrlForSystem(project));
        this.methodsDictionaryDAO = new MethodsDictionaryDAO(buildDBUrlForSystem(project));

        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    void processGitCommit(GitCommit commit) {
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
        final RefactoringProcessor processor = new RefactoringProcessor(project.getBasePath());
        final List<RefactoringData> data = new LinkedList<>();
        miner.detectAtCommit(repository, "", commitId, new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                final List<RefactoringData> refs = refactorings.stream().map(processor::process)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                data.addAll(refs);
            }
        });
        logger.info("Refactorings were processed for " + (currentTimeMillis() - start) / 1000.0 + " secs!");

        final MethodsStorage methodsStorage = MethodsStorage.getInstance();
        final List<MethodInfo> deleted = methodsStorage.getDeletedMethods();
        final List<MethodInfo> added = methodsStorage.getAddedMethods();
        final List<RefactoringData> moved = methodsStorage.getMovedMethods();

//        moved.forEach(x -> {
//                added.remove(x.getNewMethod());
//                deleted.remove(x.getOldMethod());
//        });

        for (RefactoringData refactoringData : data) {
            deleted.remove(refactoringData.getOldMethod());
            added.remove(refactoringData.getNewMethod());
            if (!changedMethods.contains(refactoringData.getNewMethod()))
                changedMethods.add(refactoringData.getNewMethod());

            moved.remove(data);
        }

//        data.addAll(moved);
//        methodsDictionaryDAO.addToDictionary(added
//                .stream()
//                .map(x -> new MethodDictionaryEntity(x.getMethodFullName(), x.getStartOffset(), x.getFileName()))
//                .collect(Collectors.toList()));

//        deleted.forEach(x -> methodsDictionaryDAO.removeFromDictionary(x.getMethodFullName()));

        methodsDictionaryDAO.updateBySignature(data.stream().map(x -> new Pair<>(
                x.getOldMethod().getMethodFullName(),
                new MethodDictionaryEntity(x.getNewMethod().getMethodFullName(),
                        x.getNewMethod().getStartOffset(),
                        x.getNewMethod().getFileName())
        )).collect(Collectors.toList()));


//        methodsDictionaryDAO.addToDictionary(
//                methodsStorage.getRecalcMethods()
//                        .stream()
//                        .map(x -> new MethodDictionaryEntity(x.getMethodFullName(),
//                                x.getStartOffset(),
//                                x.getFileName()))
//                        .collect(Collectors.toList())
//        );
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

    private static boolean compareMethodSigs(MethodInfo first, MethodInfo second) {
        final String sigFst = first.getMethodFullName();
        final String sigSnd = second.getMethodFullName();
        return sigFst.substring(sigFst.lastIndexOf('.') + 1).equalsIgnoreCase(sigSnd.substring(sigSnd.lastIndexOf('.') + 1));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.project = null;
    }
}
