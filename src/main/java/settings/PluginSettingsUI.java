package settings;

import javax.swing.*;

public class PluginSettingsUI {
    public PluginSettingsUI() {}

    public JPanel getSettingsPanel() {
        return settingsPanel;
    }

    private JPanel settingsPanel;
    private JComboBox dateCheckBox;
    private JComboBox fontCheckBox;
    private JComboBox fontSizeCheckBox;
    private JCheckBox graphticsCheckBox;
    private JLabel graphicsLabel;
    private JLabel fontSizeLabel;
    private JLabel fontLabel;
    private JLabel showDataLabel;
}
