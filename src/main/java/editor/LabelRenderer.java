package editor;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.JBColor;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import settings.TopiasSettingsState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LabelRenderer extends HintRenderer {
    private int lineStartOffset;
    private XYSeries xySeries;
    private int upperBound;
    private int multiplier;
    LabelRenderer(@Nullable String text, Pair<StatisticsViewEntity, List<Integer>> methodData, int lineOffset, Project project) {
        super(text);
        this.lineStartOffset = lineOffset;
        this.xySeries = new XYSeries("");
        final AtomicInteger index = new AtomicInteger(1);
        this.upperBound = Collections.max(methodData.getSecond());
//        Collections.reverse(methodData.getSecond());
        multiplier = TopiasSettingsState.getInstance(project).getState().showHistograms ? 3 : 2;
        methodData.getSecond().forEach(val -> xySeries.add(index.getAndIncrement(), val));
    }

    @Override
    public int calcWidthInPixels(Inlay inlay) {
        final CustomFontMetrics metrics = getMetrics(inlay.getEditor());
        return metrics.getSymbolWidth() * 35 * 3;
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return inlay.getEditor().getLineHeight() * multiplier;
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        final Editor editor = inlay.getEditor();
        if (!(editor instanceof EditorImpl))
            return;
        final EditorImpl impl = (EditorImpl) editor;
        final int ascent = impl.getAscent();
        final int descent = impl.getDescent();
        final Graphics2D g2d = (Graphics2D) g;
        final TextAttributes attributes = getTextAttributes(editor);

        BufferedImage bufferedImage = null;
        final CustomFontMetrics fontMetrics = getMetrics(editor);
        final boolean showHistograms = TopiasSettingsState.getInstance(editor.getProject()).getState().showHistograms;
        final int period = TopiasSettingsState.getInstance(editor.getProject()).getState().discrTypeId == 0 ? 7 : 30;
        if (showHistograms) {
            final XYSeriesCollection data = new XYSeriesCollection(xySeries);
            final JFreeChart chart = ChartFactory.createHistogram(
                    null,
                    null,
                    null,
                    data,
                    PlotOrientation.VERTICAL,
                    false,
                    false,
                    false
            );

            chart.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            chart.getXYPlot().setBackgroundPaint(attributes.getBackgroundColor());
            chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(0, 0, 255));
            final XYPlot xyPlot = chart.getXYPlot();
            final ChartPanel chartPanel = new ChartPanel(chart);
            //let our histogram be like 3/5 of caption text length
            final int multiplier = period == 7 ? 14 : 21;
            final double maxBarWidth = 1.0 / (period + 1.0);
            final int chartWidth = fontMetrics.getSymbolWidth() * multiplier;
            // 1.5 of char height
            final int chartHeight = (int) (fontMetrics.lineHeight() * 4.5) - 5;
            chartPanel.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
            final Font font = new Font("Dialog", Font.PLAIN, (int) (fontMetrics.getFont().getSize() * 0.8));

            ((NumberAxis) xyPlot.getRangeAxis()).setTickUnit(new NumberTickUnit(upperBound));
            final NumberAxis domainNumberAxis = (NumberAxis) xyPlot.getDomainAxis();
            domainNumberAxis.setTickUnit(new NumberTickUnit(period));

            xyPlot.getRangeAxis().setRange(new Range(0, upperBound), false, false);
            xyPlot.getRangeAxis().setUpperMargin(0.3);
            xyPlot.getRangeAxis().setUpperBound(upperBound);
            xyPlot.getDomainAxis().setTickLabelFont(font);
            xyPlot.getRangeAxis().setTickLabelFont(font);
            xyPlot.getDomainAxis().setLabelFont(font);
            xyPlot.getRangeAxis().setLabelFont(font);
            xyPlot.getDomainAxis().setRange(new Range(0, period));
            xyPlot.getDomainAxis().setTickLabelPaint(xyPlot.getBackgroundPaint());



            XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
            renderer.setDrawBarOutline(false);
            // flat bars look best...
            renderer.setBarPainter(new StandardXYBarPainter());
            renderer.setShadowVisible(false);

            bufferedImage = chart.createBufferedImage(chartWidth, chartHeight);
            final Graphics2D imageGraphics = bufferedImage.createGraphics();
            imageGraphics.setFont(font);
            imageGraphics.setColor(JBColor.DARK_GRAY);
            imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(true));
            final LocalDate now = LocalDate.now();
            final LocalDate from = now.minusDays(period);
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-dd");
            final int xStartOffset = period == 30 ? (int) (chartWidth * 0.07) : (int) (chartWidth * 0.1);
            final int xEndOffset = period == 30 ? (int) (chartWidth * 0.8) : (int) (chartWidth * 0.75);
            final int yOffset = (int) (chartHeight * 0.87);
            imageGraphics.drawString(from.format(formatter), xStartOffset , yOffset);
            imageGraphics.drawString(now.format(formatter), xEndOffset + 3, yOffset);

        }

        if (super.getText() != null && attributes != null) {
            final Color foregroundColor = attributes.getForegroundColor();
            if (foregroundColor != null) {
                final Object savedHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                final Shape savedClip = g.getClip();

                g.setColor(foregroundColor);
                g.setFont(getFont(editor));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(true));
                g2d.setClip(r.x, r.y, calcWidthInPixels(inlay), calcHeightInPixels(inlay));
                final FontMetrics metrics = fontMetrics.metrics();
                final int startX = r.x + 7 + fontMetrics.metrics().stringWidth(String.format("%" +
                        lineStartOffset + "s", ""));
                final int startY = r.y + Math.max(ascent, (r.height + metrics.getAscent() - metrics.getDescent()) / 2) - 1;

                final int widthAdjustment = calcWidthAdjustment(editor, g.getFontMetrics());
                if (widthAdjustment == 0) {
                    g.drawString(super.getText(), startX + 7, startY - 4);
                    if (showHistograms)
                        g2d.drawImage(bufferedImage, null, startX + fontMetrics.getSymbolWidth() * 40, startY + 5 -
                                (int) (fontMetrics.lineHeight() * 4.5 * 0.6));

                } else {
                    final int adjustmentPosition = this.getWidthAdjustment().getAdjustmentPosition();
                    final String firstPart = this.getText().substring(0, adjustmentPosition);
                    final String secondPart = this.getText().substring(adjustmentPosition);
                    g.drawString(firstPart, startX, startY);
                    g.drawString(secondPart, startX + g.getFontMetrics().stringWidth(firstPart) + widthAdjustment, startY);
                }

                g.setClip(savedClip);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedHint);
            }
        }
    }

    private int calcWidthAdjustment(Editor editor, FontMetrics fontMetrics) {
        if (super.getWidthAdjustment() == null || !(editor instanceof EditorImpl))
            return 0;
        final int editorTextWidth = ((EditorImpl) editor).getFontMetrics(Font.PLAIN)
                .stringWidth(super.getWidthAdjustment().getEditorTextToMatch());
        return Math.max(0, editorTextWidth + doCalcWidth(super.getWidthAdjustment().getHintTextToMatch(), fontMetrics)
                - doCalcWidth(super.getText(), fontMetrics));
    }

    private CustomFontMetrics getMetrics(Editor editor) {
        Key<CustomFontMetrics> LABEL_FONT_METRICS = new Key<>("ParameterHintFontMetrics");
        final String familyName = UIManager.getFont("Label.font").getFamily();
        final int size = Math.max(1, editor.getColorsScheme().getEditorFontSize() - 1);
        CustomFontMetrics metrics = editor.getUserData(LABEL_FONT_METRICS);
        if (metrics != null && !metrics.isActual(editor, familyName, size)) {
            metrics = null;
        }
        if (metrics == null) {
            metrics = new CustomFontMetrics(editor, familyName, size);
            editor.putUserData(LABEL_FONT_METRICS, metrics);
        }
        return metrics;
    }

    private Font getFont(@NotNull Editor editor) {
        return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
    }
}
