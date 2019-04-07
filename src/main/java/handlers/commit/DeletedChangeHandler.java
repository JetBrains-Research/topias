package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.PsiBuilder;
import state.MethodInfo;
import state.MethodsStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class DeletedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    private final static Logger logger = LoggerFactory.getLogger(DeletedChangeHandler.class);

    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final ContentRevision before = change.getBeforeRevision();
        final MethodsStorage methodsStorage = MethodsStorage.getInstance();
        final PsiBuilder mapper = new PsiBuilder(project);

        if (before != null) {
            try {
                methodsStorage.storeDeletedMethods(mapper.buildMethodInfoSetFromContent(before.getContent()));
            } catch (VcsException e) {
                logger.error("Vcs exception occurred while trying to build PsiTree for deleted class", e);
            }
        }

        return Optional.empty();
    }
}