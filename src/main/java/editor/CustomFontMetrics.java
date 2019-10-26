package editor;

import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.util.ui.UIUtil;

import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.font.FontRenderContext;

class CustomFontMetrics {
    private Editor editor;
    private String familyName;
    private Integer size;
    private FontUIResource font;
    private FontRenderContext context;
    private FontMetrics metrics;
    private Integer lineHeight;

    CustomFontMetrics(Editor editor, String familyName, Integer size) {
        this.editor = editor;
        this.familyName = familyName;
        this.size = size;
        this.font = UIUtil.getFontWithFallback(familyName, Font.PLAIN, size);
        this.context = getCurrentContext(editor);
        this.metrics = FontInfo.getFontMetrics(font, context);
        this.lineHeight = (int) Math.ceil(font.createGlyphVector(context, "Ap").getVisualBounds().getHeight());
    }

    Font getFont() {
        return metrics.getFont();
    }

    Integer getSymbolWidth() {
        return new Canvas().getFontMetrics(font).charWidth('A');
    }

    Boolean isActual(Editor editor, String familyName, Integer size) {
        Font fontActual = metrics.getFont();

        if (!familyName.equals(fontActual.getFamily()) || size != fontActual.getSize()) return false;

        FontRenderContext currentContext = getCurrentContext(editor);
        return currentContext.equals(metrics.getFontRenderContext());
    }

    private FontRenderContext getCurrentContext(Editor editor) {
        FontRenderContext editorContext = FontInfo.getFontRenderContext(editor.getContentComponent());
        Object valueFractionalMetrics = editor.getClass().equals(EditorImpl.class) ? ((EditorImpl) editor).myFractionalMetricsHintValue : RenderingHints.VALUE_FRACTIONALMETRICS_OFF;

        return new FontRenderContext(editorContext.getTransform(), AntialiasingType.getKeyForCurrentScope(false), valueFractionalMetrics);
    }

    Integer lineHeight() {
        return lineHeight;
    }

    FontMetrics metrics() {
        return metrics;
    }
}
