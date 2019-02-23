package handlers.refactorings;

import gr.uom.java.xmi.diff.PullUpOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import state.RefactoringData;

import java.util.function.Function;

public class PullUpOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
    @Override
    public RefactoringData apply(Refactoring refactoring) {
        final PullUpOperationRefactoring ref = (PullUpOperationRefactoring) refactoring;
        return null;
    }
}
