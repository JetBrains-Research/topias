package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.PsiBuilder;
import state.MethodInfo;
import state.Storage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class MovedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    private final static Logger logger = LoggerFactory.getLogger(MovedChangeHandler.class);

    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final PsiBuilder mapper = new PsiBuilder(project);
        final ContentRevision after = change.getAfterRevision();
        final ContentRevision before = change.getBeforeRevision();
        final Storage storage = Storage.getInstance();

        try {
            if (after == null)
                return Optional.empty();

            if (before != null)
                storage.storeMovedMethods(mapper.buildMethodInfoSetFromContent(before.getContent()));

            return Optional.of(mapper.buildMethodInfoSetFromContent(after.getContent()));
        } catch (VcsException e) {
            logger.error("Vcs exception occured while trying to build PsiTree for moved file", e);
            return Optional.empty();
        }
    }
}