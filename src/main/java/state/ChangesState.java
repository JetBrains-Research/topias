package state;

import com.intellij.openapi.components.*;
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
        public Map<String, List<ShortInfo>> persistentState;
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
        changedMethods.keySet().stream().ma
        final List<MethodInfo> infos = innerState.persistentState.getOrDefault(fileName, new LinkedList<>());
        //increment all existing
        infos.stream().filter(x -> changedMethods.contains(x.getMethodFullName())).forEach(MethodInfo::incrementChanges);

        //add new
        innerState.persistentState.putAll();
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
