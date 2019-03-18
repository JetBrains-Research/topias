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

public class MovedChangeHandler implements BiFunction<Project, Change, Optional<Set<MethodInfo>>> {
    @Override
    public Optional<Set<MethodInfo>> apply(Project project, Change change) {
        final PsiBuilder mapper = new PsiBuilder(project);
        final ContentRevision after = change.getAfterRevision();
        //TODO: update method names in dictionary

        try {
            if (after == null)
                return Optional.empty();

            return Optional.of(mapper.buildMethodInfoSetFromContent(after.getContent()));
        } catch (VcsException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}