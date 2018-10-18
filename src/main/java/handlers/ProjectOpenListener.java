package handlers;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.messages.MessageBus;

import static git4idea.history.GitHistoryUtils.loadDetails;

public class ProjectOpenListener implements ProjectComponent {
    public ProjectOpenListener(Project project) {
        super();
    }
}
