package handlers;


import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.DiffFragment;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory;
import com.intellij.openapi.vcs.history.VcsDiffUtil;
import com.intellij.openapi.vcs.history.VcsHistoryUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;
import diff.FileMapper;
import git4idea.branch.GitBrancher;
import git4idea.diff.GitDiffProvider;
import org.jetbrains.annotations.NotNull;
import state.ChangesState;

import com.intellij.diff.requests.DiffRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class VcsChangesHandlerFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        return new GitCommitHandler(panel);
    }

    private static class GitCommitHandler extends CheckinHandler {
        @NotNull private final CheckinProjectPanel panel;
        @NotNull private final Project project;


        private GitCommitHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;
            this.project = panel.getProject();
        }

        @Override
        public void checkinSuccessful() {
            final List<String> changes = panel.getSelectedChanges().stream().map(x -> {
                try {
                    return x.getBeforeRevision().getContent();
                } catch (VcsException e) {
                    return "";
                }
            }).collect(Collectors.toList());
            final List<String> changesAfter = panel.getSelectedChanges().stream().map(x -> {
                try {
                    return x.getAfterRevision().getContent();
                } catch (VcsException e) {
                    return "";
                }
            }).collect(Collectors.toList());


            final List<LineFragment> fragments = ComparisonManager.getInstance().compareLines(changes.get(0), changesAfter.get(0), ComparisonPolicy.DEFAULT, new EmptyProgressIndicator());
            final FileMapper mapper = new FileMapper(project, panel.getVirtualFiles());
            ChangesState.getInstance().loadState(mapper.getMethodToBounds());
            super.checkinSuccessful();
        }
    }
}
