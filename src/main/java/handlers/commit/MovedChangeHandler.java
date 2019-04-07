package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
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
import java.util.stream.Collectors;

public class MovedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    private final static Logger logger = LoggerFactory.getLogger(MovedChangeHandler.class);

    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final PsiBuilder mapper = new PsiBuilder(project);
        final ContentRevision after = change.getAfterRevision();
        final ContentRevision before = change.getBeforeRevision();
        final MethodsStorage methodsStorage = MethodsStorage.getInstance();

        try {
            if (after == null)
                return Optional.empty();

            if (before != null)
                methodsStorage.storeMovedMethods(mapper.buildMethodInfoSetFromContent(before.getContent()));

            final List<MethodInfo> newRevMethods = mapper.buildMethodInfoSetFromContent(after.getContent());
            final List<MethodInfo> oldRevMethods = mapper.buildMethodInfoSetFromContent(before.getContent());

            if (change.getFileStatus().equals(FileStatus.MODIFIED)) {
                methodsStorage.storeAddedMethods(newRevMethods.stream().filter(x -> notContains(oldRevMethods, x)).collect(Collectors.toList()));
            }

            return Optional.of(mapper.buildMethodInfoSetFromContent(after.getContent()));
        } catch (VcsException e) {
            logger.error("Vcs exception occured while trying to build PsiTree for moved file", e);
            return Optional.empty();
        }
    }

    private static boolean compareMethodSigs(MethodInfo first, MethodInfo second) {
        final String sigFst = first.getMethodFullName();
        final String sigSnd = second.getMethodFullName();
        return sigFst.substring(sigFst.lastIndexOf('.') + 1).equalsIgnoreCase(sigSnd.substring(sigSnd.lastIndexOf('.') + 1));
    }

    private static boolean notContains(List<MethodInfo> infos, MethodInfo info) {
        return infos.stream().noneMatch(x -> compareMethodSigs(x, info));
    }
}