package settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@State(name = "TopiasSettingsState",
        storages = {@Storage("settings_state.xml")})
public final class TopiasSettingsState implements ProjectComponent,
        PersistentStateComponent<TopiasSettingsState.InnerState> {
    private static final Logger log = LoggerFactory.getLogger(TopiasSettingsState.class);
    private InnerState innerState = new InnerState();

    public static TopiasSettingsState getInstance(Project project) {
        return project.getComponent(TopiasSettingsState.class);
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
        System.out.println("No state was loaded");
    }

    public static class InnerState {
        @NotNull
        public Boolean showHistograms;

        @NotNull
        public Integer discrTypeId;

        InnerState() {
            showHistograms = true;
            discrTypeId = 1;
        }
    }
}

