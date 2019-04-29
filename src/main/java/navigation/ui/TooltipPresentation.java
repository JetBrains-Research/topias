package navigation.ui;

import com.intellij.codeInsight.hint.HintManager;
import navigation.wrappers.DataHolder;

import javax.swing.*;

public class TooltipPresentation implements Presentable {
    private String toolTipMessage;

    public TooltipPresentation(String toolTipMessage) {
        this.toolTipMessage = toolTipMessage;
    }

    public void present(PresentationLocation location) {
       SwingUtilities.invokeLater(new Runnable() {
          public void run() {
             HintManager.getInstance().showInformationHint(DataHolder.getInstance().EDITOR, toolTipMessage);
          }
       });
    }
}
