package ui;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class TopChangedMethodsListPanel extends SimpleToolWindowPanel {
    public TopChangedMethodsListPanel(boolean vertical, boolean borderless) {
        super(false, true);
        setContent(new MySideBar().getPanel());
    }
}
