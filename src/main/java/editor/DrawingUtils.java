package editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import db.dao.StatisticsViewDAO;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settings.TopiasSettingsState;
import settings.enums.DiscrType;

import java.util.List;

public class DrawingUtils {
    private static DrawingUtils instance = null;
    private final StatisticsViewDAO dao;
    private final static Logger logger = LoggerFactory.getLogger(DrawingUtils.class);

    public static DrawingUtils getInstance(String url) {
        return instance == null ? new DrawingUtils(url) : instance;
    }

    private DrawingUtils(String url) {
        this.dao = new StatisticsViewDAO(url);
    }

    public void drawInlaysInEditor(Editor editor) {
        final TopiasSettingsState.SettingsState state = TopiasSettingsState.getInstance().getState();
        final DiscrType period = state == null? DiscrType.MONTH : state.discrType;

        if (editor == null)
            return;

        final Document doc = editor.getDocument();
        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        final VirtualFile file = ((EditorImpl) editor).getVirtualFile();

        final List<StatisticsViewEntity> entities = dao.getStatDataForFile(file.getPath(), period);
        entities.stream().map(x -> new Pair<>(x, dao.selectChangesCountDaily(x.getFullSignature(), period)))
                .forEach(x -> {
                    inlay.addBlockElement(doc.getLineStartOffset(x.getFirst().getStartOffset()) + countStartColumn(x.getFirst().getStartOffset(), doc),
                            false,
                            true,
                            0,
                            new LabelRenderer("Method was changed " + x.getFirst().getChangesCount() + " times for last " + period.getTextValue(),
                                    x)
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
