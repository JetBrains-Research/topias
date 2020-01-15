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

@State(name = "MyPluginSettingsState",
        storages = {@Storage("settings_state.xml")})
public final class MyPluginSettingsState implements ProjectComponent,
        PersistentStateComponent<MyPluginSettingsState.InnerState> {
    private static final Logger log = LoggerFactory.getLogger(MyPluginSettingsState.class);
    private InnerState innerState = new InnerState();

    public static MyPluginSettingsState getInstance(Project project) {
        return project.getComponent(MyPluginSettingsState.class);
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
        public Boolean showHistograms;

        @NotNull
        public Integer discrTypeId;

        @NotNull
        public Boolean isRefreshEnabled;

        @NotNull
        public Boolean isFirstTry;

        public String gitRootPath;

        InnerState() {
            showHistograms = true;
            discrTypeId = 1;
            isRefreshEnabled = false;
            isFirstTry = true;
        }
    }
}

