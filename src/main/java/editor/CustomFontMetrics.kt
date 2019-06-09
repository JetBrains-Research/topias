package editor

import com.intellij.ide.ui.AntialiasingType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.util.ui.UIUtil
import java.awt.Canvas
import java.awt.Font
import java.awt.FontMetrics
import java.awt.RenderingHints
import java.awt.font.FontRenderContext

class CustomFontMetrics constructor(editor: Editor, familyName: String, size: Int){
    val metrics: FontMetrics
    val lineHeight: Int


    val font: Font
        get() = metrics.font

    init {
        val font = UIUtil.getFontWithFallback(familyName, Font.PLAIN, size)
        val context = getCurrentContext(editor)
        metrics = FontInfo.getFontMetrics(font, context)
        // We assume this will be a better approximation to a real line height for a given font
        lineHeight = Math.ceil(font.createGlyphVector(context, "Ap").visualBounds.height).toInt()
    }

    fun getSymbolWidth(): Int {
        val metrics = Canvas().getFontMetrics(font)
        return metrics.charWidth('A')
    }

    fun isActual(editor: Editor, familyName: String, size: Int): Boolean {
        val font = metrics.font
        if (familyName != font.family || size != font.size) return false
        val currentContext = getCurrentContext(editor)
        return currentContext.equals(metrics.fontRenderContext)
    }

    private fun getCurrentContext(editor: Editor): FontRenderContext {
        val editorContext = FontInfo.getFontRenderContext(editor.contentComponent)
        return FontRenderContext(editorContext.transform,
                AntialiasingType.getKeyForCurrentScope(false),
                if (editor is EditorImpl)
                    editor.myFractionalMetricsHintValue
                else
                    RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
    }
}