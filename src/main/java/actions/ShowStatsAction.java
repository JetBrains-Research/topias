package actions;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.impl.XDebuggerInlayUtil;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;
import state.ChangesState;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class ShowStatsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final ChangesState state = ChangesState.getInstance(e.getProject());
        final StringBuilder builder = new StringBuilder();
        assert state.getState() != null;
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final Document doc = editor.getDocument();
        editor.getInlayModel().addBlockElement(doc.getLineStartOffset(4),
                false,
                true,
                0,
                new HintRenderer("Cool txt"));

//        state.getState().persistentState.get("master").getMethods().values().stream().flatMap(Collection::stream)
//                .forEach(x -> builder.append("Method ")
//                        .append(x.getMethodFullName())
//                        .append(" has been changed ")
//                        .append(x.getChangesCount())
//                        .append(" times!\n"));
//
//        Messages.showInfoMessage(builder.toString(), "Topias");
    }
    private Font getFont(@NotNull Editor editor) {
        return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
    }

    private static class MyBlockRenderer implements EditorCustomElementRenderer  {
        private final SortedSet<MyBlockRenderer.ValueInfo> values = new TreeSet<>();

        void addValue(int refStartOffset, int refEndOffset, @NotNull String value) {
            MyBlockRenderer.ValueInfo info = new MyBlockRenderer.ValueInfo(refStartOffset, refEndOffset, value);
            values.remove(info);
            values.add(info); // retain latest reported value for given offset
        }

        @Override
        public int calcWidthInPixels(@NotNull Inlay inlay) {
            return 50;
        }

        @Override
        public void paint(@NotNull Inlay inlay,
                          @NotNull Graphics g,
                          @NotNull Rectangle targetRegion,
                          @NotNull TextAttributes textAttributes) {
            if (values.isEmpty()) return;
            Editor editor = inlay.getEditor();
            EditorColorsScheme colorsScheme = editor.getColorsScheme();
            TextAttributes attributes = colorsScheme.getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);
            if (attributes == null) return;
            Color fgColor = attributes.getForegroundColor();
            if (fgColor == null) return;
            g.setColor(fgColor);
            g.setFont(new Font(colorsScheme.getEditorFontName(), attributes.getFontType(), colorsScheme.getEditorFontSize()));

            int curX = 0;
            for (MyBlockRenderer.ValueInfo value : values) {
                curX += JBUI.scale(5); // minimum gap between values
                int xStart = editor.offsetToXY(value.refStartOffset, true, false).x;
                int xEnd = editor.offsetToXY(value.refEndOffset, false, true).x;
                int width = g.getFontMetrics().stringWidth(value.value);
                curX = Math.max(curX, (xStart + xEnd - width) / 2);
                g.drawString(value.value, curX, targetRegion.y + ((EditorImpl)editor).getAscent());
                g.drawLine(Math.min(xEnd, Math.max(xStart, curX + width / 2)), targetRegion.y, curX + width / 2, targetRegion.y + 2);
                g.drawLine(curX, targetRegion.y + 2, curX + width, targetRegion.y + 2);
                curX += width;
            }
        }

        private static class ValueInfo implements Comparable<MyBlockRenderer.ValueInfo> {
            private final int refStartOffset;
            private final int refEndOffset;
            private final String value;

            private ValueInfo(int refStartOffset, int refEndOffset, String value) {
                this.refStartOffset = refStartOffset;
                this.refEndOffset = refEndOffset;
                this.value = value;
            }

            @Override
            public int compareTo(@NotNull MyBlockRenderer.ValueInfo o) {
                return refStartOffset - o.refStartOffset;
            }
        }
    }
}
