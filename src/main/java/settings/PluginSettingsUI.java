package settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import settings.enums.DiscrType;

import javax.swing.*;

public class PluginSettingsUI {

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    public PluginSettingsUI(Project project) {
        TopiasSettingsState.InnerState state = TopiasSettingsState.getInstance(project).getState();
        if (state == null)
            state = new TopiasSettingsState.InnerState();

        final DiscrType[] elems = {DiscrType.MONTH, DiscrType.WEEK};
        dateComboBox.setModel(new DefaultComboBoxModel<>(elems));
        dateComboBox.setSelectedItem(DiscrType.getById(state.discrTypeId));
        graphicsCheckBox.setSelected(state.showHistograms);
    }

    private JPanel settingsPanel;
    private JComboBox<DiscrType> dateComboBox;
    private JCheckBox graphicsCheckBox;
    private JLabel graphicsLabel;
    private JLabel showDataLabel;


    public DiscrType getDiscrType() {
        return (DiscrType) dateComboBox.getSelectedItem();
    }

    public boolean isHistogramsEnabled() {
        return graphicsCheckBox.isSelected();
    }

    public void setDiscrType(DiscrType discrType) {
        this.dateComboBox.setSelectedItem(discrType);
    }

    public void setHistogramsEnabled(boolean showHistograms) {
        this.graphicsCheckBox.setSelected(showHistograms);
    }
}
