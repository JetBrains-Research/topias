package state;

import com.intellij.openapi.components.*;
import com.intellij.util.Functions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(name = "ChangesState",
        storages = { @Storage( file = "counter.xml") })
public final class ChangesState implements ApplicationComponent,
        PersistentStateComponent<ChangesState.InnerState> {
    private InnerState innerState = new InnerState();

    public static class InnerState {
        InnerState() {
            persistentState = new HashMap<>();
        }

        @NotNull
        public Map<String, Set<MethodInfo>> persistentState;
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

    public void update(Map<String, Set<MethodInfo>> changedMethods) {

        changedMethods.forEach((x, y) -> innerState.persistentState.merge(x, y, (a, b) -> {
            a.addAll(b);
            return a;
        }));
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
