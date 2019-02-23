package handlers.ide;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import processing.CommitProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VcsChangesHandlerFactory extends CheckinHandlerFactory {
    private final static Logger logger = LoggerFactory.getLogger(VcsChangesHandlerFactory.class);

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        return new GitCommitHandler(panel);
    }

    private static class GitCommitHandler extends CheckinHandler {
        @NotNull
        private final CheckinProjectPanel panel;
        @NotNull
        private final Project project;

        private CommitProcessor utils = null;

        private GitCommitHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;
            this.project = panel.getProject();
            try {
                this.utils = new CommitProcessor(project);
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
