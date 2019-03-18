package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import state.MethodInfo;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DeletedChangeHandler implements BiFunction<Project, Change, Optional<Set<MethodInfo>>> {
    @Override
    public Optional<Set<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision revision = change.getBeforeRevision();
        //@TODO add methods to Storage.deleteFromDictionary
        return Optional.empty();
    }
}