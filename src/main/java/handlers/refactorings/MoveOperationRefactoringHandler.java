package handlers.refactorings;

import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import processing.MethodUtils;
import state.RefactoringData;
import state.MethodInfo;

import java.util.function.Function;

public class MoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
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
