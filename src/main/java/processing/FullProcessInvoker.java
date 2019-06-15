package processing;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import db.DatabaseInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.IsRunning;

import java.io.File;

import static processing.GitCommitsProcessor.*;
import static processing.Utils.buildDBUrlForSystem;

public class FullProcessInvoker {
    private final static Logger logger = LoggerFactory.getLogger(FullProcessInvoker.class);

    public static void invoke(Project project, boolean isFirstTry) {
        File sqliteFile = new File(buildDBUrlForSystem(project));
        if (!sqliteFile.exists()) {
            DatabaseInitialization.createNewDatabase(buildDBUrlForSystem(project));
        }
        final String sqlitePath = sqliteFile.getPath();
        sqliteFile = null;
        logger.info("DB file is located at {}", sqlitePath);
        logger.info("Starting processing of git history");
        if (IsRunning.getInstance().isRunning()) {
            ApplicationManager.getApplication().invokeLater(() ->
                    Messages.showWarningDialog("History processing is already running!", "Topias"));
            return;
        }
        final DumbService dumbService = DumbService.getInstance(project);
        dumbService.runWhenSmart(() -> processGitHistory(project, sqlitePath, isFirstTry));
    }
}
