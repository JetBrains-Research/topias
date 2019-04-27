package settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import settings.enums.DiscrType;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

public class PluginSettingsUI {

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    public PluginSettingsUI() {
        final TopiasSettingsState.SettingsState state = TopiasSettingsState.getInstance().getState();
        final DiscrType[] elems = {DiscrType.MONTH, DiscrType.WEEK};
        dateComboBox.setModel(new DefaultComboBoxModel<>(elems));
        dateComboBox.setSelectedItem(state.discrType);
        graphicsCheckBox = new JBCheckBox(null, state.showHistograms);
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
        return graphicsCheckBox.isEnabled();
    }

    public void setDiscrType(DiscrType discrType) {
        this.dateComboBox.setSelectedItem(discrType);
    }

    public void setHistogramsEnabled(boolean showHistograms) {
        this.graphicsCheckBox.setEnabled(showHistograms);
    }
}
