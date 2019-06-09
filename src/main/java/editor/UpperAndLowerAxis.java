package editor;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class UpperAndLowerAxis extends NumberAxis {

    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        return super.refreshTicksVertical(g2, dataArea, edge);
    }
}
