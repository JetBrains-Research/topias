package handlers;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.Consumer;
import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProjectOpenListener implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(ProjectOpenListener.class);

    public ProjectOpenListener(Project project) {
        super();
        try {
            GitHistoryUtils.loadDetails(project, project.getBaseDir(), commitConsumer);
        } catch (VcsException e) {
            logger.debug("Vcs exception has occured while reading list of all project commits. Here goes the stacktrace:");
            e.printStackTrace();
        }
    }

    private final Consumer<GitCommit> commitConsumer = x -> {
        Hash commitHash = x.getId();
        x.getChanges();
    };
}
