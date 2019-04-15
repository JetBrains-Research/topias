package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@State(name = "ChangesState",
        storages = {@Storage(file = "counter.xml")})
public final class ChangesState implements ProjectComponent,
        PersistentStateComponent<ChangesState.InnerState> {
    private static final Logger log = LoggerFactory.getLogger(ChangesState.class);
    private InnerState innerState = new InnerState();

    public static ChangesState getInstance(Project project) {
        return project.getComponent(ChangesState.class);
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

    public static class InnerState {
        @NotNull
        @MapAnnotation
        public Map<String, String> persistentState;

        InnerState() {
            persistentState = new HashMap<>();
        }
    }
}
