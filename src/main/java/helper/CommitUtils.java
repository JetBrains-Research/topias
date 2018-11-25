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
import com.intellij.util.Consumer;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public final class CommitUtils {
    private final static Logger logger = LoggerFactory.getLogger(CommitUtils.class);
    private final Map<Type, Consumer<Change>> handlers;
    private final Project project;
    private final GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private final Repository repository;
    private final helper.RefactoringHandler handler = new helper.RefactoringHandler();
    private String branchName;

    public CommitUtils(Project project) throws Exception {
        handlers = new HashMap<>();
        this.project = project;
        GitService gitService = new GitServiceImpl();
        this.repository = gitService.openRepository(project.getBasePath());
        this.branchName = repository.getBranch();
        handlers.put(Type.DELETED, new AddedChangeHandler());
        handlers.put(Type.MODIFICATION, new ModifiedChangeHandler());
        handlers.put(Type.MOVED, new MovedChangeHandler());
        handlers.put(Type.NEW, new AddedChangeHandler());
    }

    private class AddedChangeHandler implements Consumer<Change> {
        @Override
        public void consume(Change change) {
            final ContentRevision newRevision = change.getAfterRevision();
            if (newRevision != null) {
                final String content = newRevision.getFile().getPath();
                final FileMapper mapper = new FileMapper(project);
                final Map<String, SortedSet<MethodInfo>> infos =
                        mapper.vfsToMethodsData(Collections.singleton(change.getVirtualFile()));

                infos.values().stream().flatMap(Collection::stream).forEach(MethodInfo::incrementChangesCount);
                final ChangesState state = ChangesState.getInstance();
                state.update(infos, branchName);
            }
        }
    }

    private class ModifiedChangeHandler implements Consumer<Change> {
        @Override
        public void consume(Change change) {
            final ContentRevision before = change.getBeforeRevision();
            final ContentRevision after = change.getAfterRevision();

            final String path = after.getFile().getPath();

            final ComparisonManager comparisonManager = ComparisonManager.getInstance();
            final ProgressIndicator indicator = new EmptyProgressIndicator();

            try {
                final String contentBefore = before.getContent() != null ? before.getContent() : "";
                final String contentAfter = after.getContent() != null ? after.getContent() : "";

                final SimpleEntry<String, List<LineFragment>> parsedChanges =
                        new SimpleEntry<>(path,
                                comparisonManager.compareLines(contentBefore, contentAfter, ComparisonPolicy.DEFAULT, indicator));

                final FileMapper mapper = new FileMapper(project);
                final Map<String, SortedSet<MethodInfo>> infos = mapper.vfsToMethodsData(Collections.singleton(change.getVirtualFile()));

                final Map<String, SortedSet<MethodInfo>> changedMethods = infos.keySet().stream().map(x ->
                        new SimpleEntry<>(x, parsedChanges.getValue()))
                        .map(x -> {
                            final SortedSet<MethodInfo> methods = infos.get(x.getKey());
                            final List<SimpleEntry<Integer, Integer>> boundariesOfChanges =
                                    x.getValue().stream().map(y -> new SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());
                            final Set<MethodInfo> selected = methods.stream()
                                    .flatMap(y ->
                                            boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toSet()
                                    );
                            return new SimpleEntry<>(x.getKey(), selected);
                        })
                        .collect(groupingBy(SimpleEntry::getKey, collectingAndThen(mapping(SimpleEntry::getValue,
                                toList()), x -> x.stream().flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new)))));

                changedMethods.values().stream().flatMap(Collection::stream).forEach(MethodInfo::incrementChangesCount);
                final ChangesState state = ChangesState.getInstance();
                state.update(changedMethods, branchName);
            } catch (VcsException e) {
                e.printStackTrace();
            }
        }
    }

    private class MovedChangeHandler implements Consumer<Change> {
        @Override
        public void consume(Change changes) {
            final ContentRevision revision = changes.getBeforeRevision();
            if (revision != null) {
                final ChangesState state = ChangesState.getInstance();
                final String path = changes.getAfterRevision().getFile().getPath();
                assert state.getState() != null;

                state.getState().persistentState.get(branchName).getMethods().put(
                        path,
                        state.getState().persistentState.get("master").getMethods().remove(revision.getFile().getPath())
                );
            }
        }
    }

    private class DeletedChangeHandler implements Consumer<Change> {
        @Override
        public void consume(Change changes) {
            final ContentRevision revision = changes.getBeforeRevision();
            if (revision != null) {
                final ChangesState state = ChangesState.getInstance();

                assert state.getState() != null;

                state.getState().persistentState.get("master").getMethods().remove(revision.getFile().getPath());
            }
        }
    }

    public void processCommit(GitCommit commit) throws Exception {
        final Set<RefactoringData> data = new HashSet<>();

        miner.churnAtCommit(repository, commit.getId().asString(), new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                data.addAll(refactorings.stream().map(handler::process).collect(Collectors.toSet()));
            }
        });


        processNewCommit(commit.getChanges(), data);
    }

    public void processCommit(@NotNull CheckinProjectPanel panel) {
        processNewCommit(panel.getSelectedChanges(), null);
    }

    private void processNewCommit(Collection<Change> changes, @Nullable Set<RefactoringData> data) {
        changes.parallelStream().forEach(x -> handlers.get(x.getType()).consume(x));
    }
}
