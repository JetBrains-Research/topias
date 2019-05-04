package handlers.ide;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import org.jetbrains.annotations.NotNull;
import processing.GitCommitsProcessor;

import static processing.Utils.buildPathForSystem;

public class GitRepoChangeListener implements GitRepositoryChangeListener {
    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        final Project project = repository.getProject();
        ProgressManager.getInstance().getProgressIndicator().isRunning();
        GitCommitsProcessor.processGitHistory(project, buildPathForSystem(project), false);
    }
}
