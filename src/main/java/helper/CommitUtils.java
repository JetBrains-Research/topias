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
import state.ChangesState;
import state.MethodInfo;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public final class CommitUtils {
    private final static Logger logger = LoggerFactory.getLogger(CommitUtils.class);
    private final Map<Type, Function<Change, SimpleEntry<VirtualFile, SortedSet<MethodInfo>>>> handlers;
    private final Project project;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Repository repository;
    private final helper.RefactoringHandler handler;
    private String branchName;

    public CommitUtils(Project project) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = repository.getBranch();
        this.handler = new helper.RefactoringHandler(branchName);
        handlers.put(Type.DELETED, new DeletedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    private class AddedChangeHandler implements Function<Change, SimpleEntry<VirtualFile, SortedSet<MethodInfo>>> {
        @Override
        public SimpleEntry<VirtualFile, SortedSet<MethodInfo>> apply(Change change) {
            final ContentRevision newRevision = change.getAfterRevision();
            final FileMapper mapper = new FileMapper(project);
            final VirtualFile file = newRevision.getFile().getVirtualFile()
            return new SimpleEntry<>(file, mapper.vfsToMethodsData(file, branchName).getValue());

                /*infos.values().stream().flatMap(Collection::stream).forEach(MethodInfo::incrementChangesCount);
                final ChangesState state = ChangesState.getInstance();
                state.update(infos, branchName);*/
        }
    }

    private class ModifiedChangeHandler implements Function<Change, SimpleEntry<VirtualFile, SortedSet<MethodInfo>>> {
        @Override
        public SimpleEntry<VirtualFile, SortedSet<MethodInfo>> apply(Change change) {
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

                final FileMapper mapper = new FileMapper(project);
                final SimpleEntry<String, SortedSet<MethodInfo>> infos = mapper.vfsToMethodsData(before.getFile().getVirtualFile(), branchName);

                final List<SimpleEntry<Integer, Integer>> boundariesOfChanges =
                        parsedChanges.getValue()
                                .stream()
                                .map(y -> new SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());

                final SortedSet<MethodInfo> selected = infos.getValue().stream()
                        .flatMap(y ->
                                boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toCollection(TreeSet::new));

                return new SimpleEntry<>(before.getFile().getVirtualFile(), selected);

                /*changedMethods.values().stream().flatMap(Collection::stream).forEach(MethodInfo::incrementChangesCount);
                final ChangesState state = ChangesState.getInstance();
                state.update(changedMethods, branchName);*/
            } catch (VcsException e) {
                logger.debug("VCS exception has occured");
                e.printStackTrace();
                return null;
            }
        }
    }

    private class MovedChangeHandler implements Function<Change, SimpleEntry<VirtualFile, SortedSet<MethodInfo>>> {
        @Override
        public SimpleEntry<VirtualFile, SortedSet<MethodInfo>> apply(Change change) {
            return new SimpleEntry<>(change.getAfterRevision().getFile().getVirtualFile(),
                    new ModifiedChangeHandler().apply(change).getValue());
        }
    }

    private class DeletedChangeHandler implements Function<Change, SimpleEntry<VirtualFile, SortedSet<MethodInfo>>> {
        @Override
        public SimpleEntry<VirtualFile, SortedSet<MethodInfo>> apply(Change change) {
            final ContentRevision revision = change.getBeforeRevision();
            return new SimpleEntry<>(revision.getFile().getVirtualFile(), null);
        }
    }

    public void processCommit(GitCommit commit) throws Exception {
        final Set<RefactoringData> data = new HashSet<>();

        miner.churnAtCommit(repository, commit.getId().asString(), new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(handler::process).collect(toSet()));
            }
        });


        processNewCommit(commit.getChanges(), data);
    }

    public void processCommit(@NotNull CheckinProjectPanel panel) {
        processNewCommit(panel.getSelectedChanges(), null);
    }

    private void processNewCommit(Collection<Change> changes, @Nullable Set<RefactoringData> data) {
        final Map<String, SortedSet<MethodInfo>> state = ChangesState.getInstance().getState().persistentState.get(branchName).getMethods();
        if (data != null) {
            final Set<MethodInfo> oldMethods = data.stream().map(RefactoringData::getOldMethod).collect(toSet());
            for (Change change : changes) {
                final SimpleEntry<VirtualFile, SortedSet<MethodInfo>> res = handlers.get(change.getType()).apply(change);
                if (res.getValue() == null) {
                    state.remove(res.getKey().getCanonicalPath());
                    return;
                }

            }
        }
    }
}
