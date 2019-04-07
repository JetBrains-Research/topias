package handlers.ide;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.CommitProcessor;
import processing.Utils;

import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

public final class VcsChangesHandlerFactory extends CheckinHandlerFactory {
    private final static Logger logger = LoggerFactory.getLogger(VcsChangesHandlerFactory.class);
    private String authorData;

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        this.authorData = commitContext.getUserDataString();
        return new GitCommitHandler(panel);
    }

    private class GitCommitHandler extends CheckinHandler {
        @NotNull
        private final CheckinProjectPanel panel;
        @NotNull
        private final Project project;
        final ProjectLevelVcsManagerImpl instance;
        private CommitProcessor utils;

        private GitCommitHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;

            this.project = panel.getProject();
            this.instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);

            final VcsRoot gitRootPath = Arrays.stream(instance.getAllVcsRoots()).filter(x -> x.getVcs() != null)
                    .filter(x -> x.getVcs().getName().equalsIgnoreCase("git"))
                    .findAny().orElse(null);
            assert gitRootPath != null;
            GitRepository repository = GitRepositoryImpl.getInstance(gitRootPath.getPath(), project, true);
            final String currentBranchName;
            try {
                currentBranchName = Utils.getCurrentBranchName(project);
                this.utils = new CommitProcessor(project, currentBranchName);
            } catch (VcsException e) {
                logger.error("No, just no");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void checkinSuccessful() {
            utils.processCommit(panel, authorData, currentTimeMillis());
            super.checkinSuccessful();
        }
    }
}
