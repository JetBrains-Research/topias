package processing;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
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
import editor.DrawingUtils;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import handlers.ide.FileOpenListener;
import handlers.ide.GitRepoChangeListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.ChangesState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitCommitsProcessor {
    private final static Logger logger = LoggerFactory.getLogger(GitCommitsProcessor.class);

    public static void processGitHistory(Project project, String dbFilePath, boolean isFirstTime) {

        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);
        instance.addInitializationRequest(VcsInitObject.AFTER_COMMON, () -> {
            try {

                final VcsRoot gitRootPath = Arrays.stream(instance.getAllVcsRoots()).filter(x -> x.getVcs() != null)
                        .filter(x -> x.getVcs().getName().equalsIgnoreCase("git"))
                        .findAny().orElse(null);

                if (gitRootPath == null || gitRootPath.getPath() == null) {
                    logger.warn("VCS root not found for project {}", project.getName());
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
                    if (isFirstTime) {
                        MessageBus bus = project.getMessageBus();
                        bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                                new FileOpenListener(dbFilePath));
                        bus.connect().subscribe(GitRepository.GIT_REPO_CHANGE, new GitRepoChangeListener());
                        final List<Editor> editors = Arrays.asList(EditorFactory.getInstance().getAllEditors());
                        final DrawingUtils drawingUtils = DrawingUtils.getInstance(dbFilePath);
                        editors.forEach(drawingUtils::drawInlaysInEditor);
                    }
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

                final Task.Backgroundable backgroundable = new Task.Backgroundable(project, "Topias Plugin: Processing Git Commit History", false) {
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

                    @Override
                    public void onFinished() {
                        final List<Editor> editors = Arrays.asList(EditorFactory.getInstance().getAllEditors());
                        final DrawingUtils drawingUtils = DrawingUtils.getInstance(dbFilePath);
                        if (isFirstTime) {
                            MessageBus bus = project.getMessageBus();
                            bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileOpenListener(dbFilePath));
                            bus.connect().subscribe(GitRepository.GIT_REPO_CHANGE, new GitRepoChangeListener());
                            System.out.println("Applying results to all opened editors");
                        } else {
                            editors.forEach(drawingUtils::cleanInlayInEditor);
                        }
                        editors.forEach(drawingUtils::drawInlaysInEditor);
                        super.onFinished();
                    }
                };
                ProgressManager.getInstance().run(backgroundable);

            } catch (Exception e) {
                logger.debug("Exception has occured, stacktrace: {}", (Object) e.getStackTrace());
            }
        });
    }
}
