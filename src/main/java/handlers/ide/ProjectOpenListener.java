package handlers.ide;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import db.DatabaseInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.GitCommitsProcessor;

import java.io.File;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    public ProjectOpenListener(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        logger.info("Project {} opened", project.getName());

        final File sqliteFile = new File(project.getBasePath() + "/.idea/state.db");
        if (!sqliteFile.exists()) {
            DatabaseInitialization.createNewDatabase(project.getBasePath() + "/.idea/state.db");
        }
        logger.info("DB file is located at {}", sqliteFile.getAbsolutePath());
        logger.info("Starting processing of git history");
        GitCommitsProcessor.processGitHistory(project, sqliteFile.getPath());
    }
}
