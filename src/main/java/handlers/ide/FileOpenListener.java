package handlers.ide;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import editor.LabelRenderer;
import org.jetbrains.annotations.NotNull;
import state.ChangesState;

public class FileOpenListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        final Project project = source.getProject();

        final ChangesState state = ChangesState.getInstance(project);

        if (state == null || state.getState() == null || state.getState().persistentState.isEmpty())
            return;

        final Editor editor = source.getSelectedTextEditor();

        if (editor == null)
            return;

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
}
