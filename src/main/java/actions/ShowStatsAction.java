package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import state.ChangesState;

public class ShowStatsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final ChangesState state = ChangesState.getInstance();
        final StringBuilder builder = new StringBuilder();
        assert state.getState() != null;

        state.getState().forEach((x, y) -> builder.append("Method ")
                .append(x.getName())
                .append(" has range from ")
                .append(y.getKey())
                .append(" to ")
                .append(y.getValue())
                .append("\n"));

        Messages.showInfoMessage(builder.toString(), "Topias");
    }
}
