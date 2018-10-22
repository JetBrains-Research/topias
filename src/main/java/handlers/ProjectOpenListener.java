package handlers;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.VcsInitObject;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static helper.CommitUtils.processCommit;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);
    private final Project project;

    @Override
    public void projectOpened() {
        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);
        
        instance.addInitializationRequest(VcsInitObject.AFTER_COMMON, () -> {
            try {
                GitHistoryUtils.loadDetails(project, project.getBaseDir(), commit -> processCommit(commit, project));
//            serializator.saveState(state);
            } catch (VcsException e) {
                logger.debug("Vcs exception has occured while reading list of all project commits. Here goes the stacktrace:");
                e.printStackTrace();
            }
        });
    }

    public ProjectOpenListener(Project project) {
        this.project = project;
    }
}
