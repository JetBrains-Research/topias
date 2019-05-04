package handlers.ide;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import org.jetbrains.annotations.NotNull;
import processing.GitCommitsProcessor;

public class GitRepoChangeListener implements GitRepositoryChangeListener {
    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        final Project project = repository.getProject();
        GitCommitsProcessor.processGitHistory(project, project.getBasePath() + "/.idea/state.db", false);
    }
}
