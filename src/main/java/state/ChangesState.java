package state;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

@State(name = "ChangesState",
        storages = { @Storage( file = "counter.xml") })
public final class ChangesState implements ProjectComponent,
        PersistentStateComponent<ChangesState.InnerState> {
    private InnerState innerState = new InnerState();
    private static final Logger log = LoggerFactory.getLogger(ChangesState.class);

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

    @Override
    public void noStateLoaded() {
        log.debug("No state was loaded");
    }

    public static ChangesState getInstance(Project project) {
        return project.getComponent(ChangesState.class);
    }
}
