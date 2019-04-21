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
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.PsiBuilder;
import processing.Utils;
import state.MethodInfo;
import state.MethodsStorage;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class ModifiedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    private final static Logger logger = LoggerFactory.getLogger(ModifiedChangeHandler.class);

    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision before = change.getBeforeRevision();
        final ContentRevision after = change.getAfterRevision();

        final MethodsStorage methodsStorage = MethodsStorage.getInstance();

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

            final List<MethodInfo> methodsInNewRev = psiBuilder.buildMethodInfoSetFromContent(
                    after.getContent(), Utils.getFileName(change)
            );

            final Set<MethodInfo> methodsInOldRev = new HashSet<>(psiBuilder.buildMethodInfoSetFromContent(
                    before.getContent(), Utils.getFileName(change)
            ));

            methodsStorage.storeDeletedMethods(new LinkedList<MethodInfo>(CollectionUtils.subtract(methodsInOldRev, methodsInNewRev)));
            methodsStorage.storeAddedMethods(new LinkedList<MethodInfo>(CollectionUtils.subtract(methodsInNewRev, methodsInOldRev)));

            final List<AbstractMap.SimpleEntry<Integer, Integer>> boundariesOfChanges =
                    parsedChanges.stream().map(y ->
                            new AbstractMap.SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());

            final List<MethodInfo> selected = methodsInNewRev.stream()
                    .flatMap(y ->
                            boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toCollection(LinkedList::new));

            return Optional.of(selected);
        } catch (VcsException e) {
            logger.error("Vcs exception occured while trying to build PsiTree for modified class", e);
            return Optional.empty();
        }
    }
}