package handlers.ide;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.VcsInitObject;
import com.intellij.util.messages.MessageBus;
import git4idea.GitUtil;
import git4idea.commands.*;
import git4idea.history.GitHistoryUtils;
import db.DatabaseInitialization;
import git4idea.history.GitLogUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.CommitProcessor;
import processing.Utils;
import state.ChangesState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

                final GitLineHandler lineHandler = new GitLineHandler(project,
                        gitRootPath.getPath(),
                        GitCommand.REV_LIST
                );


                final List<String> sinceWhat = new ArrayList<>();
                final ChangesState.InnerState state = ChangesState.getInstance(project).getState();
                if (state != null && state.persistentState.get(currentBranchName) != null) {
                    final String hash = state.persistentState.get(currentBranchName);
                    sinceWhat.add(hash + "..HEAD");
                    sinceWhat.add("--count");
                } else {
                    sinceWhat.add("HEAD");
                    sinceWhat.add("--count");
                    sinceWhat.add("--since=\"last month\"");
                }

                lineHandler.addParameters(sinceWhat);

                final GitCommandResult result = Git.getInstance().runCommand(lineHandler);
                final int commitCountToProcess = Integer.parseInt(result.getOutputAsJoinedString());

                if (commitCountToProcess == 0) {
                    MessageBus bus = project.getMessageBus();
                    bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                            new FileOpenListener(null, sqliteFile.getAbsolutePath()));
                    return;
                }


                sinceWhat.clear();
                if (state != null && state.persistentState.get(currentBranchName) != null) {
                    final String hash = state.persistentState.get(currentBranchName);
                    sinceWhat.add(hash + "..HEAD");
                } else {
                    sinceWhat.add("--since=\"last month\"");
                }
                sinceWhat.add("--reverse");
                String[] paramsArray = new String[sinceWhat.size()];
                paramsArray = sinceWhat.toArray(paramsArray);
                final String[] finalParamsArray = paramsArray;
                final Future gitHistoryFuture = ApplicationManager.getApplication().executeOnPooledThread(() ->
                        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Topias", false) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                try {
                                    final CommitProcessor commitProcessor = new CommitProcessor(project,
                                            currentBranchName,
                                            indicator,
                                            commitCountToProcess);

                                    GitHistoryUtils.loadDetails(project, gitRootPath.getPath(), commitProcessor::processCommit,
                                            finalParamsArray);
                                    logger.info("Git history processing finished");
                                } catch (VcsException e) {
                                    logger.debug("Exception has occured, stacktrace: {}", (Object) e.getStackTrace());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })

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
