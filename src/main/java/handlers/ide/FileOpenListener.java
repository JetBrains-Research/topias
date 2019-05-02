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
import db.dao.StatisticsViewDAO;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settings.enums.DiscrType;
import state.ChangesState;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileOpenListener implements FileEditorManagerListener {
    private final Future gitHistoryFuture;
    private final StatisticsViewDAO dao;
    private final static Logger logger = LoggerFactory.getLogger(FileOpenListener.class);

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
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        final Project project = source.getProject();

        final ChangesState state = ChangesState.getInstance(project);

        final Editor editor = source.getSelectedTextEditor();

        final DiscrType period = DiscrType.WEEK; //todo: get from settings state
        if (editor == null)
            return;

        final Document doc = editor.getDocument();

        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        try {
            //waiting for computation to complete
            if (gitHistoryFuture != null)
                gitHistoryFuture.get();

            final List<StatisticsViewEntity> entities = dao.getStatDataForFile(file.getPath(), DiscrType.MONTH);
            entities.stream().map(x -> new Pair<>(x, dao.selectChangesCountDaily(x.getFullSignature(), DiscrType.MONTH)))
                    .forEach(x -> {
                        inlay.addBlockElement(doc.getLineStartOffset(x.getFirst().getStartOffset()) + countStartColumn(x.getFirst().getStartOffset(), doc),
                                false,
                                true,
                                0,
                                new LabelRenderer("Method was changed " + x.getFirst().getChangesCount() + " times for last " + period.getTextValue(),
                                        x)
                        );
                    });
        } catch (InterruptedException e) {
            logger.error("Interruped exception has occured", e);
        } catch (ExecutionException e) {
            logger.error("Execution exception has occured", e);
        }
    }
}
