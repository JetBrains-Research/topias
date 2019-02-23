package handlers.refactorings;

import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import state.RefactoringData;

import java.util.function.Function;

public class ExtractOperationHandler implements Function<Refactoring, RefactoringData> {
    @Override
    public RefactoringData apply(Refactoring refactoring) {
        final ExtractOperationRefactoring ref = (ExtractOperationRefactoring) refactoring;

        final String beforeFilePath = ref.getSourceOperationCodeRangeBeforeExtraction().getFilePath();

        return null;
    }
}

