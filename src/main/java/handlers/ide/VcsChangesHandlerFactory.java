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
import git4idea.GitReference;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import processing.CommitProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.util.Arrays;
import java.util.Objects;

public final class VcsChangesHandlerFactory extends CheckinHandlerFactory {
    private final static Logger logger = LoggerFactory.getLogger(VcsChangesHandlerFactory.class);

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        final String userData = commitContext.getUserDataString();
        return new GitCommitHandler(panel);
    }

    private class GitCommitHandler extends CheckinHandler {
        @NotNull
        private final CheckinProjectPanel panel;
        @NotNull
        private final Project project;

        private CommitProcessor utils;

        private GitCommitHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;
            this.project = panel.getProject();
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
            utils.processCommit(panel);
            super.checkinSuccessful();
        }
    }
}
