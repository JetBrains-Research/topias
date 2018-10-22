package helper;

import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import diff.FileMapper;
import git4idea.GitCommit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.ChangesState;
import state.MethodInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public final class CommitUtils {
    private final static Logger logger = LoggerFactory.getLogger(CommitUtils.class);

    private static Map<String, String> changesToMap(Stream<Change> changes, Function<Change, String> getContent) {
        return changes.map(x -> new AbstractMap.SimpleEntry<>(Objects.requireNonNull(x.getVirtualFile()).getCanonicalPath(), getContent.apply(x)))
                .collect(groupingBy(
                        AbstractMap.SimpleEntry::getKey, mapping(AbstractMap.SimpleEntry::getValue, joining("")))
                );
    }

    public static void processCommit(GitCommit commit, Project project) {
        processNewCommit(project, commit.getChanges());
    }

    public static void processCommit(@NotNull CheckinProjectPanel panel, Project project) {
        processNewCommit(project, panel.getSelectedChanges());
    }

    private static void processNewCommit(Project project, Collection<Change> changes) {
        final Function<Change, String> before = x -> {
            try {
                if (x.getFileStatus() == FileStatus.ADDED)
                    return "";

                return Objects.requireNonNull(x.getBeforeRevision()).getContent();

            } catch (VcsException e) {
                //Not ok, but for beginning it may be OK...
                logger.debug("Got vcs exception while getting before commit revisions. Stacktrace:");
                e.printStackTrace();
                return "";
            }
        };

        final Function<Change, String> after = x -> {
            try {
                if (x.getFileStatus() == FileStatus.DELETED)
                    return "";

                return Objects.requireNonNull(x.getAfterRevision()).getContent();
            } catch (VcsException e) {
                //Not ok, but for beginning it may be OK...
                logger.debug("Got vcs exception while getting after commit revisions. Stacktrace:");
                e.printStackTrace();
                return "";
            }
        };

        final Map<String, String> changesBefore = changesToMap(changes.stream(), before);
        final Map<String, String> changesAfter = changesToMap(changes.stream(), after);

        final ComparisonManager comparisonManager = ComparisonManager.getInstance();
        final ProgressIndicator indicator = new EmptyProgressIndicator();

        //Idk how to call flatMap in collect(...)
        final Map<String, List<List<LineFragment>>> fragments = changesBefore.keySet().stream().map(x ->
                new AbstractMap.SimpleEntry<>(x, comparisonManager.compareLines(changesBefore.get(x), changesAfter.get(x),
                        ComparisonPolicy.DEFAULT, indicator)))
                .collect(groupingBy(AbstractMap.SimpleEntry::getKey, mapping(AbstractMap.SimpleEntry::getValue, toList())));

        final FileMapper mapper = new FileMapper(project);
        final Map<String, List<MethodInfo>> infos = mapper.vfsToMethodsData(changes.stream().map(Change::getVirtualFile).collect(toList()));

        //Same issue with flatmap and collecting here
        //Pretty complex structure, I don't need this yet, but probably will need later.
        final List<AbstractMap.SimpleEntry<String, List<MethodInfo>>> changedMethods = infos.keySet().stream().map(x ->
                new AbstractMap.SimpleEntry<>(x, fragments.get(x).stream().flatMap(Collection::stream).collect(toList())))
                .map(x -> {
                    final List<MethodInfo> methods = infos.get(x.getKey());
                    final List<AbstractMap.SimpleEntry<Integer, Integer>> boundariesOfChanges =
                            x.getValue().stream().map(y -> new AbstractMap.SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());
                    final List<MethodInfo> selected = methods.stream()
                            .flatMap(y ->
                                    boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toList()
                            );
                    return new AbstractMap.SimpleEntry<>(x.getKey(), selected);
                }).collect(toList());

        final ChangesState state = ChangesState.getInstance();
        state.update(changedMethods.stream()
                .flatMap(x -> x.getValue().stream())
                .map(MethodInfo::getMethodFullName).collect(toList()));
    }
}
