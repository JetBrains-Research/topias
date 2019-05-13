package ui;

import com.intellij.psi.PsiReference;
import com.intellij.ui.components.JBList;
import navigation.ReferenceNavigator;
import navigation.ui.ReferenceListCellRenderer;
import navigation.wrappers.Reference;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MySideBar {
    private JBList<Reference> list;
    private JPanel panel;

    public MySideBar() {
        list.setCellRenderer(new ReferenceListCellRenderer());
        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (list.getSelectedIndex() != -1) {
                        new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference();
                    }
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedIndex() != -1) {
                    new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference();
                }
            }
        });
    }

    public JPanel getPanel() {
        panel.add(list);
        return panel;
    }

    public void updateListItems(List<Reference> references) {
        list.removeAll();
        final Reference[] referencesArray = new Reference[references.size()];
        list.setListData(references.toArray(referencesArray));
        list.setCellRenderer(new ReferenceListCellRenderer());
        list.setSelectedIndex(references.size() > 0 ? 0 : -1);
        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (list.getSelectedIndex() != -1) {
                        new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference();
                    }
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedIndex() != -1) {
                    new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference();
                }
            }
        });
    }
}
