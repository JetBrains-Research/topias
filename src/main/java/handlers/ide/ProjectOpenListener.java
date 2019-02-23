package handlers.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.VcsInitObject;
import com.intellij.util.messages.MessageBus;
import git4idea.history.GitHistoryUtils;
import processing.CommitProcessor;
import jdbc.DatabaseInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    @Override
    public void projectOpened() {
        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);

        // Register file opened listener
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileOpenListener());

        instance.addInitializationRequest(VcsInitObject.AFTER_COMMON, () -> {
            try {
                final CommitProcessor utils = new CommitProcessor(project);
                DatabaseInitialization.createNewDatabase(project.getBasePath() + "/.idea/state.db");
                final VcsRoot gitRootPath = Arrays.stream(instance.getAllVcsRoots()).filter(x -> x.getVcs() != null)
                        .filter(x -> x.getVcs().getName().equalsIgnoreCase("git"))
                        .findAny().orElse(null);

                if (gitRootPath == null || gitRootPath.getPath() == null) {
                    logger.debug("VCS root not found for project {}", project.getName());
                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showWarningDialog("Git VCS Root unfortunately not found!", "Topias"));

                    return;
                }

                GitHistoryUtils.loadDetails(project, gitRootPath.getPath(), utils::processCommit, "--reverse");
            } catch (Exception e) {
                logger.debug("Exception has occured, stacktrace: {}", (Object) e.getStackTrace());
                e.printStackTrace();
            }
        });

    }

    public ProjectOpenListener(Project project) {
        this.project = project;
    }
}
