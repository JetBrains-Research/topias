package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import processing.PsiBuilder;
import state.MethodInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class AddedChangeHandler implements BiFunction<Project, Change, Optional<Set<MethodInfo>>> {
    @Override
    public Optional<Set<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision newRevision = change.getAfterRevision();
        final PsiBuilder mapper = new PsiBuilder(project);
        try {
            if (newRevision == null)
                return Optional.empty();

            final String content = newRevision.getContent();

            if (content == null || content.isEmpty())
                return Optional.empty();
            //@TODO add methods to Storage.addToDict
            return Optional.of(mapper.buildMethodInfoSetFromContent(content));
        } catch (VcsException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
