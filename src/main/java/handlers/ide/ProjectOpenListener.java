package handlers.ide;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import db.DatabaseInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.GitCommitsProcessor;
import state.IsRunning;

import java.io.File;
import java.sql.SQLException;

import static processing.FullProcessInvoker.invoke;
import static processing.Utils.buildDBUrlForSystem;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    public ProjectOpenListener(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        logger.info("Project {} opened", project.getName());
        invoke(project, true);
    }

    @Override
    public void projectClosed() {
        IsRunning.getInstance().setRunning(project, false);
        IsRunning.getInstance().onProjectClose(project);
        try {
            logger.info("Closing db connection for project " + project.getName());
            DatabaseInitialization.closeConnection();
        } catch (SQLException e) {
            logger.error("Unable to close connection", e);
        }
    }
}
