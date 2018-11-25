package helper;

import gr.uom.java.xmi.diff.*;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.refactoringminer.api.RefactoringType.*;

public final class RefactoringHandler {

    private final Map<RefactoringType, Function<Refactoring, RefactoringData>> handlers =
            new HashMap<RefactoringType, Function<Refactoring, RefactoringData>>() {{
                put(EXTRACT_OPERATION, new ExtractOperationHandler());
                put(MOVE_OPERATION, new MoveOperationRefactoringHandler());
                put(PULL_UP_OPERATION, new PullUpOperationRefactoringHandler());
                put(PUSH_DOWN_OPERATION, new PushDownOperationRefactoringHandler());
                put(EXTRACT_AND_MOVE_OPERATION, new ExtractAndMoveOperationRefactoringHandler());
                put(INLINE_OPERATION, new InlineOperationRefactoringHandler());
            }};

    public RefactoringData process (Refactoring refactoring) {
        return handlers.get(refactoring.getRefactoringType()).apply(refactoring);
    }

    private class InlineOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final InlineOperationRefactoring ref = (InlineOperationRefactoring) refactoring;
            return null;
        }
    }

    private class ExtractOperationHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final ExtractOperationRefactoring ref = (ExtractOperationRefactoring) refactoring;
            return null;
        }
    }

    private class MoveOperationRefactoringHandler implements Function<Refactoring, RefactoringData> {
        @Override
        public RefactoringData apply(Refactoring refactoring) {
            final MoveOperationRefactoring ref = (MoveOperationRefactoring) refactoring;
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
