package state;

import com.intellij.openapi.components.*;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@State(name = "ChangesState",
        storages = { @Storage( file = "counter.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public final class ChangesState implements ApplicationComponent, PersistentStateComponent<Map<PsiMethod, Long>> {
    private Map<PsiMethod, Long> persistentState;

    @Nullable
    @Override
    public Map<PsiMethod, Long> getState() {
        return persistentState;
    }

    @Override
    public void loadState(@NotNull Map<PsiMethod, Long> state) {
        this.persistentState = state;
    }

    @Override
    public void noStateLoaded() {
        this.persistentState = new HashMap<>();
    }
}
