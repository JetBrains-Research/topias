package handlers.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.PsiBuilder;
import processing.Utils;
import state.MethodInfo;
import state.MethodsStorage;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovedChangeHandler implements BiFunction<Project, Change, Optional<List<MethodInfo>>> {
    private final static Logger logger = LoggerFactory.getLogger(MovedChangeHandler.class);

    private static boolean compareMethodSigs(MethodInfo first, MethodInfo second) {
        final String sigFst = first.getMethodFullName();
        final String sigSnd = second.getMethodFullName();
        return sigFst.substring(sigFst.lastIndexOf('.') + 1).equalsIgnoreCase(sigSnd.substring(sigSnd.lastIndexOf('.') + 1));
    }

    private static boolean notContains(List<MethodInfo> infos, MethodInfo info) {
        return infos.stream().noneMatch(x -> compareMethodSigs(x, info));
    }

    @Override
    public Optional<List<MethodInfo>> apply(Project project, Change change) {
        final PsiBuilder mapper = new PsiBuilder(project);
        final ContentRevision after = change.getAfterRevision();
        final ContentRevision before = change.getBeforeRevision();
        final MethodsStorage methodsStorage = MethodsStorage.getInstance();

        try {
            if (after == null || before == null)
                return Optional.empty();

            final List<MethodInfo> newRevMethods = mapper.buildMethodInfoSetFromContent(after.getContent(), Utils.getNewFileName(change));
            final List<MethodInfo> oldRevMethods = mapper.buildMethodInfoSetFromContent(before.getContent(), Utils.getOldFileName(change));

            //saving to add to dictionary
            final List<MethodInfo> addedInNewRevision = newRevMethods.stream().filter(
                    x -> notContains(oldRevMethods, x)
            ).collect(Collectors.toList());
            methodsStorage.storeAddedMethods(addedInNewRevision);

            //saving to update dictionary
            final List<Pair<MethodInfo, MethodInfo>> moved = new LinkedList<>();
            newRevMethods.removeAll(addedInNewRevision);
            newRevMethods.forEach(x -> {
                moved.add(new Pair<>(x, oldRevMethods.stream().filter(y -> compareMethodSigs(y, x)).findFirst().get()));
            });
            methodsStorage.storeMovedMethods(moved);

            //saving to delete from dictionary or find refactorings
            final List<MethodInfo> deletedInNewRevision = oldRevMethods.stream().filter(x -> notContains(newRevMethods, x)).collect(Collectors.toList());
            methodsStorage.storeDeletedMethods(deletedInNewRevision);

            return Optional.of(mapper.buildMethodInfoSetFromContent(after.getContent(), Utils.getNewFileName(change)));
        } catch (VcsException e) {
            logger.error("Vcs exception occured while trying to build PsiTree for moved file", e);
            return Optional.empty();
        }
    }
}