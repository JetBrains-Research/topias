package settings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import editor.DrawingUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import settings.enums.DiscrType;
import ui.TopChangedMethodsListPanel;

import javax.swing.*;

import java.util.Arrays;
import java.util.List;

import static processing.Utils.buildPathForSystem;

public class PluginSettingsConfigurable implements Configurable {

    private final String pluginName = "Topias";
    private PluginSettingsUI pluginSettingsUI;
    private TopiasSettingsState.InnerState settingsState;
    private Project project;

    public PluginSettingsConfigurable(Project project) {
        this.project = project;
        this.pluginSettingsUI = new PluginSettingsUI(project);
        this.settingsState = TopiasSettingsState.getInstance(project).getState();
        if (settingsState == null) {
            settingsState = new TopiasSettingsState.InnerState();
            settingsState.discrTypeId = 1;
            settingsState.showHistograms = true;
        }
    }

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
        final DiscrType settingsDiscrType = DiscrType.getById(settingsState.discrTypeId);
        final boolean histEnabled = pluginSettingsUI.isHistogramsEnabled();
        final boolean histsEnabledSet = settingsState.showHistograms;
        return pluginSettingsUI.getDiscrType().getId() != settingsState.discrTypeId ||
                pluginSettingsUI.isHistogramsEnabled() != settingsState.showHistograms;
    }



    @Override
    public void apply() throws ConfigurationException {
        settingsState.showHistograms = pluginSettingsUI.isHistogramsEnabled();
        settingsState.discrTypeId = pluginSettingsUI.getDiscrType().getId();
        TopChangedMethodsListPanel.refreshList(project);
        final List<Editor> editors = Arrays.asList(EditorFactory.getInstance().getAllEditors());
        final DrawingUtils drawingUtils = DrawingUtils.getInstance(buildPathForSystem(project));
        editors.forEach(drawingUtils::cleanInlayInEditor);
        editors.forEach(drawingUtils::drawInlaysInEditor);
    }

    @Override
    public void reset() {
        pluginSettingsUI.setHistogramsEnabled(settingsState.showHistograms);
        pluginSettingsUI.setDiscrType(DiscrType.getById(settingsState.discrTypeId));
    }

    @Override
    public void disposeUIResources() {
        pluginSettingsUI = null;
    }
}
