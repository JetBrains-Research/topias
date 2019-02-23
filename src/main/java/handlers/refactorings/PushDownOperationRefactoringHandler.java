package handlers.refactorings;

import gr.uom.java.xmi.diff.PushDownOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import state.RefactoringData;

import java.util.function.Function;

public class PushDownOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
    @Override
    public RefactoringData apply(Refactoring refactoring) {
        final PushDownOperationRefactoring ref = (PushDownOperationRefactoring) refactoring;
        return null;
    }
}
