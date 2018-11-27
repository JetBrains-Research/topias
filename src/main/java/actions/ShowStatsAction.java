package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import state.ChangesState;

import java.util.Collection;

public class ShowStatsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final ChangesState state = ChangesState.getInstance(e.getProject());
        final StringBuilder builder = new StringBuilder();
        assert state.getState() != null;

        state.getState().persistentState.values().stream().flatMap(Collection::stream)
                .forEach(x -> builder.append("Method ")
                        .append(x.getMethodFullName())
                        .append(" has been changed ")
                        .append(x.getChangesCount())
                        .append(" times!\n"));

        Messages.showInfoMessage(builder.toString(), "Topias");
    }
}
