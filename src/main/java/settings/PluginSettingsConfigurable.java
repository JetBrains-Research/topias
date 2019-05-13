package settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import settings.enums.DiscrType;

import javax.swing.*;

public class PluginSettingsConfigurable implements Configurable {
    private final String pluginName = "Topias";
    private PluginSettingsUI pluginSettingsUI = new PluginSettingsUI();
    private TopiasSettingsState.SettingsState settingsState = TopiasSettingsState.getInstance().getState();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return pluginName;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return pluginSettingsUI.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        final DiscrType discrType = pluginSettingsUI.getDiscrType();
        final DiscrType settingsDiscrType = settingsState.discrType;
        final boolean histEnabled = pluginSettingsUI.isHistogramsEnabled();
        final boolean histsEnabledSet = settingsState.showHistograms;
        return pluginSettingsUI.getDiscrType().equals(settingsState.discrType) &&
                pluginSettingsUI.isHistogramsEnabled() == settingsState.showHistograms;
    }



    @Override
    public void apply() throws ConfigurationException {
        settingsState.showHistograms = pluginSettingsUI.isHistogramsEnabled();
        settingsState.discrType = pluginSettingsUI.getDiscrType();
    }

    @Override
    public void reset() {
        pluginSettingsUI.setHistogramsEnabled(settingsState.showHistograms);
        pluginSettingsUI.setDiscrType(settingsState.discrType);
    }

    @Override
    public void disposeUIResources() {
        pluginSettingsUI = null;
    }
}
