package navigation.ui;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import navigation.wrappers.ReferenceCollection;

public class ReferencePresentation implements Presentable {
    private ReferenceCollection references;
    private PsiElement psiElement;

    public ReferencePresentation(ReferenceCollection collection, PsiElement originalElement) {
        this.references = collection;
        this.psiElement = originalElement;
    }

    public void present(PresentationLocation location) {
        createPresentation().present(location);
    }

    private Presentable createPresentation() {
        if (references.size() == 0) {
            return new TooltipPresentation("No usages found");
        } else {
            String popupTitle = "Choose usage of '" + ((PsiNamedElement) psiElement).getName() + "': (Found " + references.size() + ")";
            return new PopupListPresentation(references, popupTitle);
        }
    }
}
