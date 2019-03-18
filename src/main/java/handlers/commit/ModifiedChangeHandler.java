package handlers.commit;

import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import processing.PsiBuilder;
import state.MethodInfo;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class ModifiedChangeHandler implements BiFunction<Project, Change, Optional<Set<MethodInfo>>> {
    @Override
    public Optional<Set<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision before = change.getBeforeRevision();
        final ContentRevision after = change.getAfterRevision();

        if (before == null || after == null)
            return Optional.empty();

        final ComparisonManager comparisonManager = ComparisonManager.getInstance();
        final ProgressIndicator indicator = new EmptyProgressIndicator();

        try {

            final String contentBefore = before.getContent() != null ? before.getContent() : "";
            final String contentAfter = after.getContent() != null ? after.getContent() : "";

            final List<LineFragment> parsedChanges =
                            comparisonManager.compareLines(contentBefore, contentAfter, ComparisonPolicy.DEFAULT, indicator);

            final PsiBuilder psiBuilder = new PsiBuilder(project);
            final Set<MethodInfo> methodsInNewRev = psiBuilder.buildMethodInfoSetFromContent(
                    after.getContent()
            );

            final List<AbstractMap.SimpleEntry<Integer, Integer>> boundariesOfChanges =
                    parsedChanges.stream().map(y ->
                            new AbstractMap.SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());

            final Set<MethodInfo> selected = methodsInNewRev.stream()
                    .flatMap(y ->
                            boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toCollection(HashSet::new));

            return Optional.of(selected);
        } catch (VcsException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}