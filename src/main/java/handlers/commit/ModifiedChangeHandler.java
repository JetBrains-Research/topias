package handlers.commit;

import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import processing.PsiBuilder;
import state.MethodInfo;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class ModifiedChangeHandler implements Function<Change, AbstractMap.SimpleEntry<String, Set<MethodInfo>>> {
    @Override
    public AbstractMap.SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
        final ContentRevision before = change.getBeforeRevision();
        final ContentRevision after = change.getAfterRevision();

        final String path = before.getFile().getPath();

        final ComparisonManager comparisonManager = ComparisonManager.getInstance();
        final ProgressIndicator indicator = new EmptyProgressIndicator();

        try {
            final String contentBefore = before.getContent() != null ? before.getContent() : "";
            final String contentAfter = after.getContent() != null ? after.getContent() : "";

            final AbstractMap.SimpleEntry<String, List<LineFragment>> parsedChanges =
                    new AbstractMap.SimpleEntry<>(path,
                            comparisonManager.compareLines(contentBefore, contentAfter, ComparisonPolicy.DEFAULT, indicator));

            final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(after.getFile().getPath());

            final PsiBuilder mapper = new PsiBuilder(project);
            final AbstractMap.SimpleEntry<String, Set<MethodInfo>> infos = mapper.vfsToMethodsData(
                    after.getContent(), after.getFile().getPath(), branchName
            );

            final List<AbstractMap.SimpleEntry<Integer, Integer>> boundariesOfChanges =
                    parsedChanges.getValue()
                            .stream()
                            .map(y -> new AbstractMap.SimpleEntry<>(y.getStartLine2(), y.getEndLine2())).collect(toList());

            final Set<MethodInfo> selected = infos.getValue().stream()
                    .flatMap(y ->
                            boundariesOfChanges.stream().map(y::ifWithin).filter(Objects::nonNull).distinct()).collect(toCollection(HashSet::new));

            return new AbstractMap.SimpleEntry<>(before.getFile().getPath(), selected);
        } catch (VcsException e) {
            logger.debug("VCS exception has occured");
            e.printStackTrace();
            return null;
        }
    }
}