package handlers.commit;

import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import state.MethodInfo;

import java.util.AbstractMap;
import java.util.Set;
import java.util.function.Function;

public class DeletedChangeHandler implements Function<Change, AbstractMap.SimpleEntry<String, Set<MethodInfo>>> {
    @Override
    public AbstractMap.SimpleEntry<String, Set<MethodInfo>> apply(Change change) {
        final ContentRevision revision = change.getBeforeRevision();
        return new AbstractMap.SimpleEntry<>(revision.getFile().getPath(), null);
    }
}