package navigation.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import navigation.ReferenceNavigator;
import navigation.wrappers.DataHolder;
import navigation.wrappers.Reference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class PopupListPresentation implements Presentable {
    private Collection collection;
    private String popupTitle;
    private Project project;

    public PopupListPresentation(Collection collection, String popupTitle, Project project) {
        this.collection = collection;
        this.popupTitle = popupTitle;
        this.project = project;
    }

    public void present(PresentationLocation location) {
        createPopup().showInScreenCoordinates(DataHolder.getInstance().EDITOR.getContentComponent(), location);
    }

    private JBPopup createPopup() {
        PopupChooserBuilder popupChooserBuilder = new PopupChooserBuilder(createList());
        popupChooserBuilder.setTitle(popupTitle);
        JBPopup popup = popupChooserBuilder.createPopup();
        Component popupContentPane = popup.getContent();
        LookAndFeel.installBorder((JComponent) popupContentPane, "PopupMenu.border");
        LookAndFeel.installColorsAndFont((JComponent) popupContentPane, "PopupMenu.background",
                "PopupMenu.foreground", "PopupMenu.font");
        return popup;
    }

    private JList createList() {
        final JBList list = new JBList<>(collection.toArray());
        list.setCellRenderer(new ReferenceListCellRenderer());
        list.setSelectedIndex(collection.size() > 0 ? 0 : -1);
        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (list.getSelectedIndex() != -1) {
                        new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference(project);
                    }
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedIndex() != -1) {
                    new ReferenceNavigator(((Reference) list.getSelectedValue())).navigateToReference(project);
                }
            }
        });

        LookAndFeel.installBorder(list, "List.border");
        LookAndFeel.installColorsAndFont(list, "List.background", "List.foreground", "List.font");

        return list;
    }
}
