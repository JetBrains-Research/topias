package handlers;


import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import diff.FileMapper;
import helper.MethodInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.ChangesState;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

//todo: concurrency issues
public final class VcsChangesHandlerFactory extends CheckinHandlerFactory {
    private final static Logger logger = LoggerFactory.getLogger(VcsChangesHandlerFactory.class);

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        return new GitCommitHandler(panel);
    }

    private static class GitCommitHandler extends CheckinHandler {
        @NotNull
        private final CheckinProjectPanel panel;
        @NotNull
        private final Project project;


        private GitCommitHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;
            this.project = panel.getProject();
        }

        @Override
        public void checkinSuccessful() {

            final Function<Change, String> before = x -> {
                try {
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
                    return Objects.requireNonNull(x.getAfterRevision()).getContent();
                } catch (VcsException e) {
                    //Not ok, but for beginning it may be OK...
                    logger.debug("Got vcs exception while getting after commit revisions. Stacktrace:");
                    e.printStackTrace();
                    return "";
                }
            };

            final Map<String, String> changesBefore = changesToMap(panel.getSelectedChanges().stream(), before);
            final Map<String, String> changesAfter = changesToMap(panel.getSelectedChanges().stream(), after);

            final ComparisonManager comparisonManager = ComparisonManager.getInstance();
            final ProgressIndicator indicator = new EmptyProgressIndicator();

            //Idk how to call flatMap in collect(...)
            final Map<String, List<List<LineFragment>>> fragments = changesBefore.keySet().stream().map(x ->
                    new SimpleEntry<>(x, comparisonManager.compareLines(changesBefore.get(x), changesAfter.get(x),
                            ComparisonPolicy.DEFAULT, indicator)))
                    .collect(groupingBy(SimpleEntry::getKey, mapping(SimpleEntry::getValue, toList())));

            final FileMapper mapper = new FileMapper(project);
            final Map<String, List<MethodInfo>> infos = mapper.vfsToMethodsData(panel.getVirtualFiles());

            //Same issue with flatmap and collecting here
            //Pretty complex structure, I don't need this yet, but probably will need later.
            final List<SimpleEntry<String, List<MethodInfo>>> changedMethods = infos.keySet().stream().map(x ->
                    new SimpleEntry<>(x, fragments.get(x).stream().flatMap(Collection::stream).collect(toList())))
                    .map(x -> {
                        final List<MethodInfo> methods = infos.get(x.getKey());
                        final List<SimpleEntry<Integer, Integer>> boundariesOfChanges =
                                x.getValue().stream().map(y -> new SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());
                        final List<MethodInfo> selected = methods.stream()
                                .flatMap(y ->
                                        boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toList()
                                );
                        return new SimpleEntry<>(x.getKey(), selected);
                    }).collect(toList());

            final ChangesState state = ChangesState.getInstance();
            state.update(changedMethods.stream()
                    .flatMap(x -> x.getValue().stream())
                    .map(MethodInfo::getMethodFullName).collect(toList()));

            super.checkinSuccessful();
        }

        private Map<String, String> changesToMap(Stream<Change> changes, Function<Change, String> getContent) {
            return changes.map(x -> new SimpleEntry<>(Objects.requireNonNull(x.getVirtualFile()).getCanonicalPath(), getContent.apply(x)))
                    .collect(groupingBy(
                            SimpleEntry::getKey, mapping(SimpleEntry::getValue, joining("")))
                    );
        }
    }
}
