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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CommitProcessor {
    private final static Logger logger = LoggerFactory.getLogger(CommitProcessor.class);
    private final Map<Type, BiFunction<Project, Change, SimpleEntry<String, Set<MethodInfo>>>> handlers;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Project project;
    private final Repository repository;
    private String branchName;
    private boolean foundLastHash = false;
    private final BranchInfo state;

    public CommitProcessor(Project project, String branchName) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = branchName;

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

        final Set<MethodInfo> changedMethods = processNewCommit(commit.getChanges());
        final RefactoringProcessor processor = new RefactoringProcessor(branchName, project, changedMethods);
        final Set<RefactoringData> data = new HashSet<>();
        miner.churnAtCommit(repository, commit.getId().asString(), new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(processor::process).collect(Collectors.toCollection(HashSet::new)));
            }
        });


        state.updateHashValue(commit.getId());
    }

    public void processCommit(@NotNull CheckinProjectPanel panel) {
        processNewCommit(panel.getSelectedChanges());
    }

    private static String methodNameWithoutPackage(MethodInfo info) {
        final String[] splitted = info.getMethodFullName().split("\\.");
        return splitted[splitted.length - 1];
    }

    private Set<MethodInfo> processNewCommit(Collection<Change> changes) {

        for (Change change : changes) {
            final Change.Type type = change.getType();
            final Set<MethodInfo> res = handlers.get(type).apply(project, change);

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

        }
    }
}
