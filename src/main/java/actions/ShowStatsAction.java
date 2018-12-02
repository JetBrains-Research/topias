package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import state.ChangesState;

import java.awt.*;
import java.util.Collection;

public class ShowStatsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final ChangesState state = ChangesState.getInstance(e.getProject());
        final StringBuilder builder = new StringBuilder();
        assert state.getState() != null;
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        editor.getInlayModel().addBlockElement(10,
                false,
                true,
                1,
                new EditorCustomElementRenderer() {
                    @Override
                    public int calcWidthInPixels(@NotNull Inlay inlay) {
                        return 30;
                    }

                    @Override
                    public int calcHeightInPixels(@NotNull Inlay inlay) {
                        return 15;
                    }

                    @Override
                    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
                        Editor editor = inlay.getEditor();
                        g.setColor(JBColor.GRAY);
                        g.setFont(getFont(editor));
                        g.drawString("WOW TXT ABOVE TXT", targetRegion.x, targetRegion.y);
                    }
                });

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
}
