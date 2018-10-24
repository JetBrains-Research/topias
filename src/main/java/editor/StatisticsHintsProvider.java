package editor;

import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import state.BranchInfo;
import state.MethodInfo;
import state.Serializator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StatisticsHintsProvider implements InlayParameterHintsProvider {

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(PsiElement element) {
        if (element instanceof PsiMethod) {
            final PsiMethod method = (PsiMethod) element;
            final Document document = PsiDocumentManager.getInstance(method.getProject()).getDocument(method.getContainingFile());
            assert document != null;
            final TextRange range = method.getTextRange();
            final int start = document.getLineNumber(range.getStartOffset());
            return new LinkedList<InlayInfo>() {{
                add(
                        new InlayInfo(
                                "This is " + method.getName(),
                                start)
                );
            }};
        }
        return null;
    }

    @Nullable
    @Override
    public HintInfo getHintInfo(PsiElement element) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return null;
    }

    @Override
    public boolean canShowHintsWhenDisabled() {
        return true;
    }
}
