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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.BranchInfo;
import state.ChangesState;
import state.MethodInfo;
import state.RefactoringData;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CommitProcessor {
    private final static Logger logger = LoggerFactory.getLogger(CommitProcessor.class);
    private final Map<Type, Function<Change, SimpleEntry<String, Set<MethodInfo>>>> handlers;
    private final Project project;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Repository repository;
    private final RefactoringProcessor handler;
    private String branchName;
    private boolean foundLastHash = false;
    private final BranchInfo state;

    public CommitProcessor(Project project) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = repository.getBranch();
        this.handler = new RefactoringProcessor(branchName, project);

        this.state = Objects.requireNonNull(ChangesState.getInstance(project).getState())
                .persistentState
                .get(branchName);

        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    public void processCommit(GitCommit commit) {
        if (!state.getHashValue().isEmpty() && commit.getId().asString().equals(state.getHashValue())) {
            System.out.println("Found last parsed commit");
            foundLastHash = true;
            return;
        }

        if (!state.getHashValue().isEmpty() && !foundLastHash)
            return;

        final Set<RefactoringData> data = new HashSet<>();
        miner.churnAtCommit(repository, commit.getId().asString(), new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(handler::process).collect(Collectors.toCollection(HashSet::new)));
            }
        });

        processNewCommit(commit.getChanges(), data);
        state.updateHashValue(commit.getId());
    }

    public void processCommit(@NotNull CheckinProjectPanel panel) {
        processNewCommit(panel.getSelectedChanges(), null);
    }

    private static String methodNameWithoutPackage(MethodInfo info) {
        final String[] splitted = info.getMethodFullName().split("\\.");
        return splitted[splitted.length - 1];
    }

    private void processNewCommit(Collection<Change> changes, @Nullable Set<RefactoringData> data) {
        for (Change change : changes) {
            final Change.Type type = change.getType();
            final SimpleEntry<String, Set<MethodInfo>> res = handlers.get(change.getType()).apply(change);
            final Set<MethodInfo> changedMethods = res.getValue();
            if (res.getValue() == null) {
                state.getMethods().remove(res.getKey());
                continue;
            }

            if (change.getType().equals(Type.MOVED)) {
                final Set<MethodInfo> beforeMoveMethods = state.getMethods().remove(change.getBeforeRevision().getFile().getPath());

                changedMethods.forEach(x -> {
                    beforeMoveMethods.forEach(y -> {
                        if (methodNameWithoutPackage(y).equals(methodNameWithoutPackage(x)))
                            x.setChangesCount(y.getChangesCount());
                    });
                });
            }

            if (data != null) {
                for (RefactoringData rData : data) {
                    if (changedMethods.contains(rData.getOldMethod())) {
                        changedMethods.remove(rData.getOldMethod());
                        changedMethods.add(rData.getNewMethod());
                    }
                }
            }

            changedMethods.forEach(MethodInfo::incrementChangesCount);

            state.getMethods().merge(res.getKey(), changedMethods, (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
    }
}
