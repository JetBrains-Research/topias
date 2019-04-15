package handlers.ide;

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
import editor.LabelRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.data.xy.XYSeries;
import state.ChangesState;

import java.awt.*;

public class Action extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final ChangesState state = ChangesState.getInstance(e.getProject());
        assert state.getState() != null;
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        final Document doc = editor.getDocument();
        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();

        final XYSeries series = new XYSeries("Random data");
        series.add(1, 0);
        series.add(2, 0);
        series.add(3, 0);
        series.add(4, 0);
        series.add(5, 0);
        series.add(6, 0);
        series.add(7, 0);
        series.add(8, 0);
        series.add(9, 0);
        series.add(10, 1);
        series.add(11, 0);
        series.add(12, 0);
        series.add(13, 0);
        series.add(14, 0);
        series.add(15, 2);
        series.add(16, 0);
        series.add(17, 0);
        series.add(18, 0);
        series.add(19, 0);
        series.add(20, 0);
        series.add(21, 0);
        series.add(22, 0);
        series.add(23, 0);
        series.add(24, 0);
        series.add(25, 0);
        series.add(26, 1);
        series.add(27, 0);
        series.add(28, 0);
        series.add(29, 0);
        series.add(30, 0);
        inlay.addBlockElement(doc.getLineStartOffset(163),
                false,
                true,
                0,
                new LabelRenderer("Method was changed " + 3 + " times for last 30 days", countStartColumn(163, doc),series )
        );

        final XYSeries series1 = new XYSeries("cool");
        series1.add(1, 0);
        series1.add(2, 0);
        series1.add(3, 0);
        series1.add(4, 0);
        series1.add(5, 0);
        series1.add(6, 0);
        series1.add(7, 0);
        series1.add(8, 0);
        series1.add(9, 0);
        series1.add(10, 0);
        series1.add(11, 0);
        series1.add(12, 0);
        series1.add(13, 0);
        series1.add(14, 1);
        series1.add(15, 0);
        series1.add(16, 0);
        series1.add(17, 0);
        series1.add(18, 0);
        series1.add(19, 0);
        series1.add(20, 0);
        series1.add(21, 0);
        series1.add(22, 0);
        series1.add(23, 0);
        series1.add(24, 0);
        series1.add(25, 0);
        series1.add(26, 0);
        series1.add(27, 0);
        series1.add(28, 0);
        series1.add(29, 0);
        series1.add(30, 0);
        inlay.addBlockElement(doc.getLineStartOffset(185),
                false,
                true,
                0,
                new LabelRenderer("Method was changed " + 1 + " times for last 30 days", countStartColumn(177, doc), series1)
        );
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
}