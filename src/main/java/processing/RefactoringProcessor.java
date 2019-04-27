package processing;

import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.*;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import state.MethodInfo;
import state.MethodsStorage;
import state.RefactoringData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.refactoringminer.api.RefactoringType.*;
import static processing.Utils.*;

public final class RefactoringProcessor {

    private final List<MethodInfo> changedMethods;
    private final Map<RefactoringType, Function<Refactoring, RefactoringData>> handlers =
            new HashMap<RefactoringType, Function<Refactoring, RefactoringData>>() {{
//                put(EXTRACT_OPERATION, new ExtractOperationHandler());
                put(RENAME_METHOD, new RenameMethodRefactoringHandler());
                put(MOVE_OPERATION, new MoveOperationRefactoringHandler());
                put(PULL_UP_OPERATION, new PullUpOperationRefactoringHandler());
                put(PUSH_DOWN_OPERATION, new PushDownOperationRefactoringHandler());
//                put(EXTRACT_AND_MOVE_OPERATION, new ExtractAndMoveOperationRefactoringHandler());
//                put(INLINE_OPERATION, new InlineOperationRefactoringHandler());
            }};

    public RefactoringProcessor(List<MethodInfo> changedMethods) {
        this.changedMethods = changedMethods;
    }

    public RefactoringData process(Refactoring refactoring) {
        return handlers.get(refactoring.getRefactoringType()).apply(refactoring);
    }

//    private class ExtractAndMoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
//        @Override
//        public RefactoringData apply(Refactoring refactoring) {
//            final ExtractAndMoveOperationRefactoring ref =
//                    (ExtractAndMoveOperationRefactoring) refactoring;
//            final MethodsStorage storage = MethodsStorage.getInstance();
//            final UMLOperation operation = ref.getExtractedOperation();
//            storage.storeAddedMethods(new MethodInfo(
//                    ref
//            ));
//            return null;
//        }
//    }

//    private class ExtractOperationHandler implements Function<Refactoring, RefactoringData> {
//        @Override
//        public RefactoringData apply(Refactoring refactoring) {
//            final ExtractOperationRefactoring ref = (ExtractOperationRefactoring) refactoring;
//
//
//            return null;
//        }
//    }


    private class MoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final MoveOperationRefactoring ref = (MoveOperationRefactoring) refactoring;

            final CodeRange before = ref.getSourceOperationCodeRangeBeforeMove();
            final CodeRange after = ref.getTargetOperationCodeRangeAfterMove();

            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation())
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation())
            );

            return new RefactoringData(methodBefore, methodAfter);
        }

    }

    private class PullUpOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final PullUpOperationRefactoring ref = (PullUpOperationRefactoring) refactoring;
            final CodeRange before = ref.getSourceOperationCodeRangeBeforeMove();
            final CodeRange after = ref.getTargetOperationCodeRangeAfterMove();

            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation())
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation())
            );

            return new RefactoringData(methodBefore, methodAfter);
        }
    }

    private class PushDownOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final PullUpOperationRefactoring ref = (PullUpOperationRefactoring) refactoring;
            final CodeRange before = ref.getSourceOperationCodeRangeBeforeMove();
            final CodeRange after = ref.getTargetOperationCodeRangeAfterMove();

            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation())
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation())
            );

            return new RefactoringData(methodBefore, methodAfter);
        }
    }

    private class RenameMethodRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final RenameOperationRefactoring ref = (RenameOperationRefactoring) refactoring;
            final String path = ref.getTargetOperationCodeRangeAfterRename().getFilePath();
            final int startLineBefore = ref.getSourceOperationCodeRangeBeforeRename().getStartLine();
            final int startLine = ref.getTargetOperationCodeRangeAfterRename().getStartLine();
            final int endLine = ref.getTargetOperationCodeRangeAfterRename().getEndLine();

            final MethodInfo methodInfo = new MethodInfo(startLineBefore,
                    endLine,
                    calculateSignatureForEcl(ref.getOriginalOperation()));

            return new RefactoringData(methodInfo, new MethodInfo(startLine,
                    endLine,
                    calculateSignatureForEcl(ref.getRenamedOperation())
            ));
        }
    }
}
