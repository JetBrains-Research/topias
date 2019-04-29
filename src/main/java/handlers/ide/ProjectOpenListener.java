package handlers.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.VcsInitObject;
import com.intellij.util.messages.MessageBus;
import git4idea.history.GitHistoryUtils;
import db.DatabaseInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.CommitProcessor;
import processing.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Future;

import static java.lang.System.currentTimeMillis;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    public ProjectOpenListener(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);

        final File sqliteFile = new File(project.getBasePath() + "/.idea/state.db");
        if (!sqliteFile.exists()) {
            DatabaseInitialization.createNewDatabase(project.getBasePath() + "/.idea/state.db");
        }

        instance.addInitializationRequest(VcsInitObject.AFTER_COMMON, () -> {
            try {

                final VcsRoot gitRootPath = Arrays.stream(instance.getAllVcsRoots()).filter(x -> x.getVcs() != null)
                        .filter(x -> x.getVcs().getName().equalsIgnoreCase("git"))
                        .findAny().orElse(null);

                if (gitRootPath == null || gitRootPath.getPath() == null) {
                    logger.debug("VCS root not found for project {}", project.getName());
                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showWarningDialog("Git VCS Root unfortunately not found!", "Topias"));

                    return;
                }
                final String currentBranchName = Utils.getCurrentBranchName(project);
                final CommitProcessor commitProcessor = new CommitProcessor(project, currentBranchName);
                final Future gitHistoryFuture = ApplicationManager.getApplication().executeOnPooledThread(() ->
                        {
                            try {
                                long start = currentTimeMillis();
                                GitHistoryUtils.loadDetails(project, gitRootPath.getPath(), commitProcessor::processCommit,
                                "--reverse", "--since=\"last month\"");
                                logger.info("Git history processing finished");
                                System.out.println("MA BOI TOPSON IS DONE " + (currentTimeMillis() - start) / 1000.0);
                            } catch (VcsException e) {
                                logger.debug("Exception has occured, stacktrace: {}", (Object) e.getStackTrace());
                            }
                        }
                );

                MessageBus bus = project.getMessageBus();
                bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                        new FileOpenListener(gitHistoryFuture, sqliteFile.getAbsolutePath()));

            } catch (Exception e) {
                logger.debug("Exception has occured, stacktrace: {}", (Object) e.getStackTrace());
            }
        });

    }
}
