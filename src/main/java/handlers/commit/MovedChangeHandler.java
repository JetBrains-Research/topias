package handlers.commit;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import processing.PsiBuilder;
import state.MethodInfo;

import java.util.AbstractMap;
import java.util.Set;
import java.util.function.Function;

public class MovedChangeHandler implements Function<Change, AbstractMap.SimpleEntry<String, Set<MethodInfo>>> {
    @Override
    public AbstractMap.SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
        final PsiBuilder mapper = new PsiBuilder(project);
        final ContentRevision after = change.getAfterRevision();
        final ContentRevision before = change.getBeforeRevision();
        try {
            return mapper.vfsToMethodsData(after.getContent(), after.getFile().getPath(), branchName);
        } catch (VcsException e) {
            e.printStackTrace();
            return null;
        }
    }
}