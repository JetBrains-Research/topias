package handlers.refactorings;

import gr.uom.java.xmi.diff.ExtractAndMoveOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import state.RefactoringData;

import java.util.function.Function;

public class ExtractAndMoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
    @Override
    public RefactoringData apply(Refactoring refactoring) {
        final ExtractAndMoveOperationRefactoring ref =
                (ExtractAndMoveOperationRefactoring) refactoring;
        return null;
    }
}
