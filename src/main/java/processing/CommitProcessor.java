package processing;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.Change.Type;
import git4idea.GitCommit;
import handlers.commit.AddedChangeHandler;
import handlers.commit.DeletedChangeHandler;
import handlers.commit.ModifiedChangeHandler;
import handlers.commit.MovedChangeHandler;
import jdbc.dao.MethodsChangelogDAO;
import jdbc.dao.MethodsDictionaryDAO;
import jdbc.entities.MethodChangeLogEntity;
import jdbc.entities.MethodDictionaryEntity;
import kotlin.Pair;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
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

    public CommitProcessor(Project project, String branchName) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = branchName;

        this.hashValue = Objects.requireNonNull(ChangesState.getInstance(project).getState())
                .persistentState
                .get(branchName);

        this.methodsChangelogDAO = new MethodsChangelogDAO("jdbc:sqlite:" + project.getBasePath() + "/.idea/state.db");
        this.methodsDictionaryDAO = new MethodsDictionaryDAO("jdbc:sqlite:" + project.getBasePath() + "/.idea/state.db");

        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    private static String methodNameWithoutPackage(MethodInfo info) {
        final String[] splitted = info.getMethodFullName().split("\\.");
        return splitted[splitted.length - 1];
    }

    public void processCommit(GitCommit commit) {
        if (hashValue != null && !hashValue.isEmpty() && commit.getId().asString().equals(hashValue)) {
            System.out.println("Found last parsed commit");
            foundLastHash = true;
            return;
        }

        if (hashValue != null && !hashValue.isEmpty() && !foundLastHash)
            return;

        final String authorName = commit.getAuthor().getEmail();
        final long commitTime = commit.getCommitTime();

        processNewCommit(commit.getChanges(), commit.getId().asString(), authorName, commitTime);

        ChangesState.getInstance(project).getState().persistentState.put(branchName, commit.getId().asString());
    }

    public void processCommit(@NotNull CheckinProjectPanel panel, String authorData, long commitTime) {
        processNewCommit(panel.getSelectedChanges(), "", authorData, commitTime);
    }

    private void processNewCommit(Collection<Change> changes, String commitId, String authorName, long commitTime) {
        //@TODO: filter only java files
        final List<Change> javaChanges = changes.stream()
                .filter(x -> x.toString().substring(x.toString().lastIndexOf('.') + 1).equalsIgnoreCase("java"))
                .collect(Collectors.toList());

        final List<MethodInfo> changedMethods = javaChanges.stream()
                .map(x -> handlers.get(x.getType()).apply(project, x))
                .filter(Optional::isPresent)
                .flatMap(x -> x.get().stream())
                .collect(Collectors.toList());

        final RefactoringProcessor processor = new RefactoringProcessor(changedMethods);
        final List<RefactoringData> data = new LinkedList<>();
        miner.churnAtCommit(repository, commitId, new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(processor::process).collect(Collectors.toCollection(LinkedList::new)));
            }
        });

        final MethodsStorage methodsStorage = MethodsStorage.getInstance();
        final List<MethodInfo> deleted = methodsStorage.getDeletedMethods();
        final List<MethodInfo> added = methodsStorage.getAddedMethods();
        final List<Pair<MethodInfo, MethodInfo>> moved = methodsStorage.getMovedMethods();

        final List<MethodInfo> updateDictionary = new LinkedList<>();

        for (RefactoringData refactoringData : data) {
            if (deleted.contains(refactoringData.getOldMethod()) && added.contains(refactoringData.getNewMethod())) {
                deleted.remove(refactoringData.getOldMethod());
                added.remove(refactoringData.getNewMethod());
            }
        }

        added.forEach(x -> methodsDictionaryDAO.addToDictionary(new MethodDictionaryEntity(x.getMethodFullName(), x.getStartOffset(), x.getFileName())));
        deleted.forEach(x -> methodsDictionaryDAO.removeFromDictionary(x.getMethodFullName()));
        data.forEach(x -> methodsDictionaryDAO.updateBySignature(x.getOldMethod().getMethodFullName(), new MethodDictionaryEntity(x.getNewMethod().getMethodFullName(), x.getNewMethod().getStartOffset(), x.getNewMethod().getFileName())));

        methodsStorage.clear();

        changedMethods.forEach(x -> {
            x.setAuthorInfo(authorName);
            x.setTimeChangeMade(commitTime);
            x.setBranchName(branchName);
        });

        final List<MethodChangeLogEntity> entities = methodsDictionaryDAO.buildChangelogs(changedMethods);
        methodsChangelogDAO.insertMethodsChanges(entities);
    }
}
