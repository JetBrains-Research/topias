package state;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ChangesState implements ApplicationComponent, PersistentStateComponent<Map<PsiMethod, Long>> {
    private final Map<PsiMethod, Long> methodsToCallsCount;

    @Nullable
    @Override
    public Map<PsiMethod, Long> getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Map<PsiMethod, Long> state) {

    }

    @Override
    public void noStateLoaded() {

    }
}
