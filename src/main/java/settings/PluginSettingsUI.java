package settings;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.ui.FontComboBox;
import com.intellij.ui.components.JBCheckBox;
import settings.enums.DiscrType;

import javax.swing.*;

public class PluginSettingsUI {
    public PluginSettingsUI() {

    }

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    private JPanel settingsPanel;
    private JComboBox<DiscrType> dateComboBox;
    private FontComboBox fontComboBox;
    private JCheckBox graphticsCheckBox;
    private JLabel graphicsLabel;
    private JLabel fontSizeLabel;
    private JLabel fontLabel;
    private JLabel showDataLabel;
    private JTextField fontSizeTextField;
    private JBCheckBox myOnlyMonospacedCheckBox;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        fontComboBox = new FontComboBox();
        myOnlyMonospacedCheckBox = new JBCheckBox(ApplicationBundle.message("checkbox.show.only.monospaced.fonts"));
    }
}
