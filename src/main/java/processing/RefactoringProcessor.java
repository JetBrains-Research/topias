package processing;

import com.intellij.openapi.project.Project;
import handlers.refactorings.*;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import state.BranchInfo;
import state.ChangesState;
import state.MethodInfo;
import state.RefactoringData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static org.refactoringminer.api.RefactoringType.*;

public final class RefactoringProcessor {

    private final String branchName;
    private final Map<String, Set<MethodInfo>> info;

    public RefactoringProcessor(String branchName, Project project) {
        this.branchName = branchName;
        ChangesState.InnerState info = Objects.requireNonNull(ChangesState.getInstance(project).getState());
        if (info.persistentState.get(branchName) == null)
            Objects.requireNonNull(ChangesState.getInstance(project).getState()).persistentState.put(branchName, new BranchInfo("", new HashMap<>()));

        this.info = info.persistentState.get(branchName).getMethods();
    }

    private final Map<RefactoringType, Function<Refactoring, RefactoringData>> handlers =
            new HashMap<RefactoringType, Function<Refactoring, RefactoringData>>() {{
                put(EXTRACT_OPERATION, new ExtractOperationHandler());
                put(RENAME_METHOD, new RenameMethodRefactoringHandler());
                put(MOVE_OPERATION, new MoveOperationRefactoringHandler());
                put(PULL_UP_OPERATION, new PullUpOperationRefactoringHandler());
                put(PUSH_DOWN_OPERATION, new PushDownOperationRefactoringHandler());
                put(EXTRACT_AND_MOVE_OPERATION, new ExtractAndMoveOperationRefactoringHandler());
                put(INLINE_OPERATION, new InlineOperationRefactoringHandler());
            }};

    public RefactoringData process(Refactoring refactoring) {
        return handlers.get(refactoring.getRefactoringType()).apply(refactoring);
    }
}
