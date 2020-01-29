package editor

import java.awt.font.FontRenderContext
import java.awt.{Canvas, Font, FontMetrics, RenderingHints}

import com.intellij.ide.ui.AntialiasingType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.{EditorImpl, FontInfo}
import com.intellij.util.ui.UIUtil
import javax.swing.plaf.FontUIResource

class CustomFontMetrics(val editor: Editor, val familyName: String, val size: Int) {
  lazy val font: FontUIResource = UIUtil.getFontWithFallback(familyName, Font.PLAIN, size)
  lazy val context: FontRenderContext = getCurrentContext(editor)
  lazy val metrics: FontMetrics = FontInfo.getFontMetrics(font, context)
  lazy val lineHeight: Int = Math.ceil(font.createGlyphVector(context, "Ap").getVisualBounds.getHeight).toInt

  def getFont: Font = metrics.getFont

  def getSymbolWidth: Int = {
    val metrics = new Canvas().getFontMetrics(font)
    metrics.charWidth('A')
  }

  def isActual(editor: Editor, familyName: String, size: Int): Boolean = {
    val font = metrics.getFont
    if (familyName != font.getFamily || size != font.getSize) return false
    val currentContext = getCurrentContext(editor)
    currentContext.equals(metrics.getFontRenderContext)
  }

  private def getCurrentContext(editor: Editor): FontRenderContext = {
    val editorContext = FontInfo.getFontRenderContext(editor.getContentComponent)
    new FontRenderContext(editorContext.getTransform,
      AntialiasingType.getKeyForCurrentScope(false),
      editor match {
        case impl: EditorImpl => impl.myFractionalMetricsHintValue
        case _ => RenderingHints.VALUE_FRACTIONALMETRICS_OFF
      })
  }

}
