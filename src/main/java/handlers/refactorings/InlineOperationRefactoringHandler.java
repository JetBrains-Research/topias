package handlers.refactorings;

import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import processing.MethodUtils;
import state.RefactoringData;
import state.MethodInfo;

import java.util.function.Function;

public class InlineOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
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
