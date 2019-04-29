package navigation.ui;

import com.intellij.openapi.editor.Editor;
import navigation.wrappers.DataHolder;

import javax.swing.*;
import java.awt.*;

public class PresentationLocation extends Point {

    public PresentationLocation(Point p) {
        super(p);
    }

    public static PresentationLocation currentCaretLocation() {
        Editor editor = DataHolder.getInstance().EDITOR;
        Point point = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        SwingUtilities.convertPointToScreen(point, editor.getContentComponent());
        return new PresentationLocation(point);
    }
}
