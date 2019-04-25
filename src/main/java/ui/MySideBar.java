package ui;

import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.awt.*;

public class MySideBar {
    private JList dataList;
    private JPanel panel;

    public JPanel getPanel() {
        panel.add(dataList);
        dataList.add("qwertty", new StatisticsComponent());
        return panel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        dataList = new JBList();
    }
}
