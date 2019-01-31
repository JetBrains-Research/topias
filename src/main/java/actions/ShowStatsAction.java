package actions;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import state.ChangesState;

import java.awt.*;

public class ShowStatsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null)
            return;

        final ChangesState state = ChangesState.getInstance(e.getProject());

        if (state == null || state.getState() == null || state.getState().persistentState.isEmpty())
            return;

        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        final Document doc = editor.getDocument();
        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        state.getState().persistentState.get("master").getMethods().get(file.getCanonicalPath()).forEach(x -> {
            inlay.addBlockElement(doc.getLineStartOffset(x.getStartOffset()),
                    false,
                    true,
                    0,
                    new LabelRenderer("Method was changed " + x.getChangesCount() + " times", countStartColumn(x.getStartOffset(), doc))
            );
        });
    }

    private static int countStartColumn(int lineNumber, Document doc) {
        final String line = doc.getText(new TextRange(doc.getLineStartOffset(lineNumber), doc.getLineEndOffset(lineNumber)));
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ')
                count++;
            else if (c == '\t')
                count += 4;
            else
                return count;
        }
        return count;
    }

    private Font getFont(@NotNull Editor editor) {
        return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
    }

    private class LabelRenderer extends HintRenderer {
        private final int lineStartOffset;

        public LabelRenderer(@Nullable String text, int lineStartOffset) {
            super(text);
            this.lineStartOffset = lineStartOffset;
        }

        @Override
        public int calcWidthInPixels(Inlay inlay) {
            return 300;
//            Editor editor = inlay.getEditor();
//            FontMetrics fontMetrics = getFontMetrics(editor).getMetrics();
//            return doCalcWidth(super.getText(), fontMetrics) + calcWidthAdjustment(editor, fontMetrics);
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
            if (super.getText() != null && attributes != null) {
                MyFontMetrics fontMetrics = getFontMetrics(editor);
                final int gap = r.height < fontMetrics.getLineHeight() + 2 ? 1 : 2;
                final Color foregroundColor = attributes.getForegroundColor();
                if (foregroundColor != null) {
                    final Object savedHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                    final Shape savedClip = g.getClip();

                    g.setColor(foregroundColor);
                    g.setFont(getFont(editor));
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(false));
                    g.clipRect(r.x + 3, r.y + 2, r.width - 6, r.height - 4);
                    final FontMetrics metrics = fontMetrics.getMetrics();
                    final int startX = r.x + 7 + fontMetrics.getMetrics().stringWidth(String.format("%"+ lineStartOffset +"s", ""));
                    final int startY = r.y + Math.max(ascent, (r.height + metrics.getAscent() - metrics.getDescent()) / 2) - 1;

                    final int widthAdjustment = calcWidthAdjustment(editor, g.getFontMetrics());
                    if (widthAdjustment == 0) {
                        g.drawString(super.getText(), startX, startY);
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
    }
}
