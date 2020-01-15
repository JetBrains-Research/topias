package settings;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import editor.DrawingUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import settings.enums.DiscrType;
import ui.TopChangedMethodsListPanel;

import javax.swing.*;

import java.util.Arrays;
import java.util.List;

import static processing.Utils.buildDBUrlForSystem;
import static processing.Utils.getCurrentBranchName;

public class PluginSettingsConfigurable implements Configurable, ProjectComponent {

    private final String pluginName = "vcs_analysis_plugin";
    private PluginSettingsUI pluginSettingsUI;
    private MyPluginSettingsState.InnerState settingsState;
    private Project project;

    public PluginSettingsConfigurable(Project project) {
        this.project = project;
        this.pluginSettingsUI = new PluginSettingsUI(project);
        this.settingsState = MyPluginSettingsState.getInstance(project).getState();
        if (settingsState == null) {
            settingsState = new MyPluginSettingsState.InnerState();
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
        return pluginSettingsUI.getDiscrType().getId() != settingsState.discrTypeId ||
                pluginSettingsUI.isHistogramsEnabled() != settingsState.showHistograms ||
                !pluginSettingsUI.getGitRepoRootPath().equals(settingsState.gitRootPath);
    }



    @Override
    public void apply() throws ConfigurationException {
        settingsState.showHistograms = pluginSettingsUI.isHistogramsEnabled();
        settingsState.discrTypeId = pluginSettingsUI.getDiscrType().getId();
        TopChangedMethodsListPanel.refreshList(project);
        final List<Editor> editors = Arrays.asList(EditorFactory.getInstance().getAllEditors());
        final DrawingUtils drawingUtils = DrawingUtils.getInstance(buildDBUrlForSystem(project));
        editors.forEach(drawingUtils::cleanInlayInEditor);
        String branchName;
        try {
            branchName = getCurrentBranchName(project);
        } catch (VcsException e) {
            branchName = "master";
        }
        final String finalBranchName = branchName;
        editors.forEach(x -> drawingUtils.drawInlaysInEditor(x, finalBranchName));
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
