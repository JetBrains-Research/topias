package processing;

import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.PullUpOperationRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import state.MethodInfo;
import state.RefactoringData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.refactoringminer.api.RefactoringType.*;
import static processing.Utils.calculateSignatureForEcl;

public final class RefactoringProcessor {
    private final String projectPath;
    private final Map<RefactoringType, Function<Refactoring, RefactoringData>> handlers =
            new HashMap<RefactoringType, Function<Refactoring, RefactoringData>>() {{
                put(RENAME_METHOD, new RenameMethodRefactoringHandler());
                put(MOVE_OPERATION, new MoveOperationRefactoringHandler());
                put(PULL_UP_OPERATION, new PullUpOperationRefactoringHandler());
                put(PUSH_DOWN_OPERATION, new PushDownOperationRefactoringHandler());
            }};

    public RefactoringProcessor(String projectPath) {
        this.projectPath = projectPath + "/";

    }

    public RefactoringData process(Refactoring refactoring) {
        return handlers.getOrDefault(refactoring.getRefactoringType(), x -> null).apply(refactoring);
    }

    private class MoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final MoveOperationRefactoring ref = (MoveOperationRefactoring) refactoring;
            final CodeRange before = ref.getSourceOperationCodeRangeBeforeMove();
            final CodeRange after = ref.getTargetOperationCodeRangeAfterMove();
            final String pathBefore = projectPath + ref.getOriginalOperation().getLocationInfo().getFilePath();
            final String pathAfter = projectPath + ref.getMovedOperation().getLocationInfo().getFilePath();

            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation()),
                    pathBefore
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation()),
                    pathAfter
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
            final String pathAfter = projectPath + after.getFilePath();
            final String pathBefore = projectPath + before.getFilePath();
            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation()),
                    pathBefore
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation()),
                    pathAfter
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
            final String pathAfter = projectPath + after.getFilePath();
            final String pathBefore = projectPath + before.getFilePath();
            final MethodInfo methodBefore = new MethodInfo(
                    before.getStartLine(),
                    before.getEndLine(),
                    calculateSignatureForEcl(ref.getOriginalOperation()),
                    pathBefore
            );

            final MethodInfo methodAfter = new MethodInfo(
                    after.getStartLine(),
                    after.getEndLine(),
                    calculateSignatureForEcl(ref.getMovedOperation()),
                    pathAfter
            );

            return new RefactoringData(methodBefore, methodAfter);
        }
    }

    private class RenameMethodRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final RenameOperationRefactoring ref = (RenameOperationRefactoring) refactoring;
            final String path = projectPath + ref.getTargetOperationCodeRangeAfterRename().getFilePath();
            final String pathBefore = projectPath + ref.getSourceOperationCodeRangeBeforeRename().getFilePath();
            final int startLineBefore = ref.getSourceOperationCodeRangeBeforeRename().getStartLine();
            final int startLine = ref.getTargetOperationCodeRangeAfterRename().getStartLine();
            final int endLine = ref.getTargetOperationCodeRangeAfterRename().getEndLine();

            final MethodInfo methodInfo = new MethodInfo(startLineBefore,
                    endLine,
                    calculateSignatureForEcl(ref.getOriginalOperation()),
                    pathBefore
            );

            return new RefactoringData(methodInfo, new MethodInfo(startLine,
                    endLine,
                    calculateSignatureForEcl(ref.getRenamedOperation()),
                    path
            ));
        }
    }
}
