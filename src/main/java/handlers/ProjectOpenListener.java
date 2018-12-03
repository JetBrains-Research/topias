package handlers;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.VcsInitObject;
import git4idea.history.GitHistoryUtils;
import helper.CommitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    @Override
    public void projectOpened() {
        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);

        instance.addInitializationRequest(VcsInitObject.AFTER_COMMON, () -> {
            try {
                final CommitUtils utils = new CommitUtils(project);
                GitHistoryUtils.loadDetails(project, project.getBaseDir(), utils::processCommit, "--reverse");
            } catch (VcsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public ProjectOpenListener(Project project) {
        this.project = project;
    }
}
