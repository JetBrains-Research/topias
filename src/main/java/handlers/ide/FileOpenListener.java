package handlers.ide;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import jdbc.dao.StatisticsViewDAO;
import org.jetbrains.annotations.NotNull;
import state.ChangesState;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileOpenListener implements FileEditorManagerListener {
    private final Future gitHistoryFuture;
    private final StatisticsViewDAO dao;

    public FileOpenListener(Future gitHistoryFuture, String dbURL) {
        this.gitHistoryFuture = gitHistoryFuture;
        this.dao = new StatisticsViewDAO(dbURL);
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

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        final Editor editor = source.getSelectedTextEditor();
        assert editor != null;
        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        inlay.dispose();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        final Project project = source.getProject();

        final ChangesState state = ChangesState.getInstance(project);

        final Editor editor = source.getSelectedTextEditor();


        if (editor == null)
            return;

        final Document doc = editor.getDocument();

        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        try {
            gitHistoryFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

/*        final XYSeries series = new XYSeries("Random data");
        series.add(1, 3);
        series.add(2, 4);
        series.add(3, 0);
        series.add(4, 1);
        series.add(5, 2);
        series.add(6, 10);
        series.add(7, 1);
        inlay.addBlockElement(doc.getLineStartOffset(15),
                false,
                true,
                0,
                new LabelRenderer("Method was changed " + 5 + " times", countStartColumn(16, doc), series)
        );*/
    }
}
