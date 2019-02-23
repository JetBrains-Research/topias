package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import processing.PsiBuilder;
import state.MethodInfo;

import java.util.AbstractMap;
import java.util.Set;
import java.util.function.BiFunction;

public class AddedChangeHandler implements BiFunction<Project, Change, AbstractMap.SimpleEntry<String, Set<MethodInfo>>> {
    @Override
    public AbstractMap.SimpleEntry<String, Set<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision newRevision = change.getAfterRevision();
        final PsiBuilder mapper = new PsiBuilder(project);
        try {
            return new AbstractMap.SimpleEntry<>(newRevision.getFile().getPath(),
                    mapper.vfsToMethodsData(newRevision.getContent(), newRevision.getFile().getPath(), branchName).getValue());
        } catch (VcsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
