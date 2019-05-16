package handlers.ide;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import editor.DrawingUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOpenListener implements FileEditorManagerListener {
    private final static Logger logger = LoggerFactory.getLogger(FileOpenListener.class);
    private final String dbURL;

    public FileOpenListener(String dbURL) {
        logger.info("FILE OPEN LISTENER CREATED");
        this.dbURL = dbURL;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        final Editor editor = source.getSelectedTextEditor();
        DrawingUtils.getInstance(dbURL).cleanInlayInEditor(editor);
        DrawingUtils.getInstance(dbURL).drawInlaysInEditor(editor);
    }
}
