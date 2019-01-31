package state;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginSettingsState implements PersistentStateComponent<PluginSettingsState> {
    @Nullable
    @Override
    public PluginSettingsState getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull PluginSettingsState state) {

    }
}
