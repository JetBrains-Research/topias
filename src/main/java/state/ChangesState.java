package state;

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

@State(name = "ChangesState",
        storages = { @Storage( file = "counter.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public final class ChangesState implements ApplicationComponent,
        PersistentStateComponent<Map<PsiMethod, SimpleEntry<Integer, Integer>>> {
    private Map<PsiMethod, SimpleEntry<Integer, Integer>> persistentState;

    @Nullable
    @Override
    public Map<PsiMethod, SimpleEntry<Integer, Integer>> getState() {
        return persistentState;
    }

    @Override
    public void loadState(@NotNull Map<PsiMethod, SimpleEntry<Integer, Integer>> state) {
        this.persistentState = state;
    }

    @Override
    public void noStateLoaded() {
        this.persistentState = new HashMap<>();
    }

    public static ChangesState getInstance() {
        return ServiceManager.getService(ChangesState.class);
    }
}
