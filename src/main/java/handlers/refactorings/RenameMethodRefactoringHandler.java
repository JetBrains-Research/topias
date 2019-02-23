package handlers.refactorings;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import processing.MethodUtils;
import state.RefactoringData;
import state.MethodInfo;

import java.util.function.Function;

public class RenameMethodRefactoringHandler implements Function<Refactoring, RefactoringData> {
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
