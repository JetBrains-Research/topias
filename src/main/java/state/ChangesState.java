package state;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "ChangesState",
        storages = { @Storage( file = "counter.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public final class ChangesState implements ApplicationComponent,
        PersistentStateComponent<ChangesState.InnerState> {
    private InnerState innerState = new InnerState();

    public static class InnerState {
        InnerState() {
            persistentState = new HashMap<>();
        }

        @NotNull
        public Map<String, Integer> persistentState;
    }

    @Nullable
    @Override
    public InnerState getState() {
        return innerState;
    }

    @Override
    public void loadState(@NotNull InnerState state) {
        this.innerState = state;
    }

    public void update(List<String> changedMethods) {
        changedMethods.forEach(x -> innerState.persistentState.merge(x, 1, (a, b) -> a + b));
    }

    @NotNull
    public static ChangesState getInstance() {
        return ServiceManager.getService(ChangesState.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return String.valueOf(ChangesState.class);
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }
}
