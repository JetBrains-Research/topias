package helper;

import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.Change.Type;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import diff.FileMapper;
import git4idea.GitCommit;
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

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public final class CommitUtils {
    private final static Logger logger = LoggerFactory.getLogger(CommitUtils.class);
    private final Map<Type, Function<Change, SimpleEntry<String, Set<MethodInfo>>>> handlers;
    private final Project project;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Repository repository;
    private final helper.RefactoringHandler handler;
    private String branchName;
    private boolean foundLastHash = false;
    private final BranchInfo state;

    public CommitUtils(Project project) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = repository.getBranch();
        this.handler = new helper.RefactoringHandler(branchName, project);

        this.state = Objects.requireNonNull(ChangesState.getInstance(project).getState())
                .persistentState
                .get(branchName);

        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    private class AddedChangeHandler implements Function<Change, SimpleEntry<String, Set<MethodInfo>>> {
        @Override
        public SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
            final ContentRevision newRevision = change.getAfterRevision();
            final FileMapper mapper = new FileMapper(project);
            try {
                return new SimpleEntry<>(newRevision.getFile().getPath(),
                        mapper.vfsToMethodsData(newRevision.getContent(), newRevision.getFile().getPath(), branchName).getValue());
            } catch (VcsException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ModifiedChangeHandler implements Function<Change, SimpleEntry<String, Set<MethodInfo>>> {
        @Override
        public SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
            final ContentRevision before = change.getBeforeRevision();
            final ContentRevision after = change.getAfterRevision();

            final String path = before.getFile().getPath();

            final ComparisonManager comparisonManager = ComparisonManager.getInstance();
            final ProgressIndicator indicator = new EmptyProgressIndicator();

            try {
                final String contentBefore = before.getContent() != null ? before.getContent() : "";
                final String contentAfter = after.getContent() != null ? after.getContent() : "";

                final SimpleEntry<String, List<LineFragment>> parsedChanges =
                        new SimpleEntry<>(path,
                                comparisonManager.compareLines(contentBefore, contentAfter, ComparisonPolicy.DEFAULT, indicator));

                final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(after.getFile().getPath());

                final FileMapper mapper = new FileMapper(project);
                final SimpleEntry<String, Set<MethodInfo>> infos = mapper.vfsToMethodsData(
                        after.getContent(), after.getFile().getPath(), branchName
                );

                final List<SimpleEntry<Integer, Integer>> boundariesOfChanges =
                        parsedChanges.getValue()
                                .stream()
                                .map(y -> new SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());

                final Set<MethodInfo> selected = infos.getValue().stream()
                        .flatMap(y ->
                                boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toCollection(HashSet::new));

                return new SimpleEntry<>(before.getFile().getPath(), selected);
            } catch (VcsException e) {
                logger.debug("VCS exception has occured");
                e.printStackTrace();
                return null;
            }
        }
    }

    private class MovedChangeHandler implements Function<Change, SimpleEntry<String, Set<MethodInfo>>> {
        @Override
        public SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
            final FileMapper mapper = new FileMapper(project);
            final ContentRevision after = change.getAfterRevision();
            final ContentRevision before = change.getBeforeRevision();
            try {
                return mapper.vfsToMethodsData(after.getContent(), after.getFile().getPath(), branchName);
            } catch (VcsException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class DeletedChangeHandler implements Function<Change, SimpleEntry<String, Set<MethodInfo>>> {
        @Override
        public SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
            final ContentRevision revision = change.getBeforeRevision();
            return new SimpleEntry<>(revision.getFile().getPath(), null);
        }
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
