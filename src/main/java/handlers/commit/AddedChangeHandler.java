package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import processing.PsiBuilder;
import state.MethodInfo;
import state.MethodsStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class AddedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision newRevision = change.getAfterRevision();
        final PsiBuilder mapper = new PsiBuilder(project);
        try {
            if (newRevision == null)
                return Optional.empty();

            final String content = newRevision.getContent();

            if (content == null || content.isEmpty())
                return Optional.empty();

            final List<MethodInfo> addedMethods = mapper.buildMethodInfoSetFromContent(content);

            final MethodsStorage methodsStorage = MethodsStorage.getInstance();
            methodsStorage.storeAddedMethods(addedMethods);

            return Optional.of(addedMethods);
        } catch (VcsException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
