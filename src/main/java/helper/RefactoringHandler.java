package helper;

import com.intellij.openapi.project.Project;
import gr.uom.java.xmi.diff.*;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import state.ChangesState;
import state.MethodInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.refactoringminer.api.RefactoringType.*;

public final class RefactoringHandler {

    private final String branchName;
    private final Map<String, Set<MethodInfo>> info;

    public RefactoringHandler(String branchName, Project project) {
        this.branchName = branchName;
        this.info = ChangesState.getInstance(project).getState().persistentState;
    }

    private final Map<RefactoringType, Function<Refactoring, RefactoringData>> handlers =
            new HashMap<RefactoringType, Function<Refactoring, RefactoringData>>() {{
                put(EXTRACT_OPERATION, new ExtractOperationHandler());
                put(RENAME_METHOD, new RenameMethodRefactoringHandler());
                put(MOVE_OPERATION, new MoveOperationRefactoringHandler());
                /*put(PULL_UP_OPERATION, new PullUpOperationRefactoringHandler());
                put(PUSH_DOWN_OPERATION, new PushDownOperationRefactoringHandler());
                put(EXTRACT_AND_MOVE_OPERATION, new ExtractAndMoveOperationRefactoringHandler());*/
                put(INLINE_OPERATION, new InlineOperationRefactoringHandler());
            }};

    public RefactoringData process(Refactoring refactoring) {
        return handlers.get(refactoring.getRefactoringType()).apply(refactoring);
    }

    private class RenameMethodRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final RenameOperationRefactoring ref = (RenameOperationRefactoring) refactoring;
            final String path = ref.getTargetOperationCodeRangeAfterRename().getFilePath();
            final int startLineBefore = ref.getSourceOperationCodeRangeBeforeRename().getStartLine();
            final int startLine = ref.getTargetOperationCodeRangeAfterRename().getStartLine();
            final int endLine = ref.getTargetOperationCodeRangeAfterRename().getEndLine();
            final MethodInfo methodInfo = info.get(path).stream().filter(x -> x.getStartOffset() == startLineBefore)
                    .findFirst().get();

            return new RefactoringData(methodInfo, new MethodInfo(startLine,
                    endLine,
                    MethodUtils.calculateSignatureForEcl(ref.getRenamedOperation()),
                    methodInfo.getChangesCount()
            ));
        }
    }

    private class InlineOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final InlineOperationRefactoring ref = (InlineOperationRefactoring) refactoring;
            final String path = ref.getTargetOperationCodeRangeBeforeInline().getFilePath();
            final int startLineBefore = ref.getTargetOperationCodeRangeBeforeInline().getStartLine();
            final int startLine = ref.getTargetOperationCodeRangeAfterInline().getStartLine();
            final int endLine = ref.getTargetOperationCodeRangeAfterInline().getEndLine();
            final MethodInfo methodInfo = info.get(path).stream().filter(x -> x.getStartOffset() == startLineBefore)
                    .findFirst().get();

            return new RefactoringData(methodInfo, new MethodInfo(startLine,
                    endLine,
                    MethodUtils.calculateSignatureForEcl(ref.getTargetOperationAfterInline()),
                    methodInfo.getChangesCount()
            ));
        }
    }

    private class MoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final MoveOperationRefactoring ref = (MoveOperationRefactoring) refactoring;
            final String path = ref.getTargetOperationCodeRangeAfterMove().getFilePath();
            final int startLineBefore = ref.getSourceOperationCodeRangeBeforeMove().getStartLine();
            final int startLine = ref.getTargetOperationCodeRangeAfterMove().getStartLine();
            final int endLine = ref.getTargetOperationCodeRangeAfterMove().getEndLine();
            final MethodInfo methodInfo = info.get(path).stream().filter(x -> x.getStartOffset() == startLineBefore)
                    .findFirst().get();

            return new RefactoringData(methodInfo, new MethodInfo(startLine,
                    endLine,
                    MethodUtils.calculateSignatureForEcl(ref.getMovedOperation()),
                    methodInfo.getChangesCount()
            ));
        }

    }

    private class ExtractOperationHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final ExtractOperationRefactoring ref = (ExtractOperationRefactoring) refactoring;

            final String beforeFilePath = ref.getSourceOperationCodeRangeBeforeExtraction().getFilePath();

            return null;
        }
    }

    private class PullUpOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final PullUpOperationRefactoring ref = (PullUpOperationRefactoring) refactoring;
            return null;
        }
    }

    private class PushDownOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final PushDownOperationRefactoring ref = (PushDownOperationRefactoring) refactoring;
            return null;
        }
    }

    private class ExtractAndMoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final ExtractAndMoveOperationRefactoring ref =
                    (ExtractAndMoveOperationRefactoring) refactoring;
            return null;
        }
    }
}
