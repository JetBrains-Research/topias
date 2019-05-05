package editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.InlayModelImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import db.dao.StatisticsViewDAO;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.PsiBuilder;
import settings.TopiasSettingsState;
import settings.enums.DiscrType;
import state.MethodInfo;

import java.util.List;
import java.util.stream.Collectors;

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
        final DiscrType period = state == null ? DiscrType.MONTH : state.discrType;

        if (editor == null)
            return;

        final Document doc = editor.getDocument();
        final InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        final VirtualFile file = ((EditorImpl) editor).getVirtualFile();
        if (file == null || !file.getPath().substring(file.getPath().lastIndexOf('.') + 1).equals("java"))
            return;


        final List<MethodInfo> methodsInFile =
                new PsiBuilder(editor.getProject()).buildMethodInfoSetFromContent(doc.getText(), file.getName());
        final List<StatisticsViewEntity> entities = dao.getStatDataForFile(file.getPath(), period);


        entities.forEach(x -> {
            methodsInFile.stream().filter(y -> y.getMethodFullName().equals(x.getFullSignature())).
                    findFirst().ifPresent(z -> x.setStartOffset(z.getStartOffset()));
        });

        final List<Pair<StatisticsViewEntity, List<Integer>>> pairs =
                entities.stream()
                        .filter(x -> x.getStartOffset() != 0)
                        .map(x -> new Pair<>(x, dao.selectChangesCountDaily(x.getFullSignature(), period)))
                        .collect(Collectors.toList());

        ApplicationManager.getApplication().invokeLater(() -> pairs
                .forEach(x -> {
                    inlay.addBlockElement(doc.getLineStartOffset(x.getFirst().getStartOffset()) + countStartColumn(x.getFirst().getStartOffset(), doc),
                            false,
                            true,
                            0,
                            new LabelRenderer("Method was changed " + x.getFirst().getChangesCount() + " times for last " + period.getTextValue(), x,
                                    countStartColumn(x.getFirst().getStartOffset(), doc))
                    );
                }));
    }

    public void cleanInlayInEditor(Editor editor) {
        InlayModelImpl inlay = (InlayModelImpl) editor.getInlayModel();
        final VirtualFile file = ((EditorImpl) editor).getVirtualFile();
        if (file == null
                || !file.getPath().substring(file.getPath().lastIndexOf('.') + 1).equals("java")
                || !inlay.hasBlockElements())
            return;

        inlay.getBlockElementsInRange(0, editor.getDocument().getTextLength(), LabelRenderer.class)
                .forEach(Disposer::dispose);
    }

    public static void clearBlockInlays(@NotNull Editor editor) {

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
