package navigation.finders;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import navigation.wrappers.ReferenceCollection;

public class MethodUsageFinder extends AbstractUsageFinder {
    private PsiMethod[] superMethods;

    public MethodUsageFinder(PsiElement psiElement) {
        super(psiElement);
        this.superMethods = ((PsiMethod) psiElement).findDeepestSuperMethods();
        buildFinderChain();
    }

    private void buildFinderChain() {
        if (hasSuperMethods() && shouldSearchForBaseMethod()) {
            PsiElementUsageFinder currentNextFinder = this;
            for (PsiMethod superMethod : superMethods) {
                currentNextFinder = currentNextFinder.setNext(new MethodUsageFinder(superMethod));
            }
        }
    }

    private boolean hasSuperMethods() {
        return superMethods != null && superMethods.length > 0;
    }

    private boolean shouldSearchForBaseMethod() {
        int YES = 0;
        final int[] dialogResult = new int[1];
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                dialogResult[0] = Messages.showYesNoDialog(buildPrompt(), "Find usages of base methods?", Messages.getQuestionIcon());
            }
        });
        return dialogResult[0] == YES;
    }

    private boolean useSingular() {
        return superMethods.length == 1;
    }

    private boolean usePlural() {
        return superMethods.length > 1;
    }

    private boolean isInterfaceMethod(PsiMethod psiMethod) {
        return psiMethod.getContainingClass().isInterface();
    }

    private String buildMethodSignatureString(PsiMethod psiMethod) {
        StringBuffer methodSignature = new StringBuffer();
        methodSignature.append(psiMethod.getName()).append("(");
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            methodSignature.append(parameter.getTypeElement().getType().getPresentableText()).append(" ").append(parameter.getName());
        }
        methodSignature.append(")");
        return methodSignature.toString();
    }

    private String buildPrompt() {
        StringBuffer question = new StringBuffer();
        String className = ((PsiMethod) psiElement).getContainingClass().getName();
        question.append("Method ").append(buildMethodSignatureString(((PsiMethod) psiElement))).append(" of class ").append(className).append("\n");
        if (usePlural()) {
            question.append("implements methods of the following classes/interfaces:\n");
            for (PsiMethod superMethod : superMethods) {
                question.append("  ").append(superMethod.getContainingClass().getName()).append("\n");
            }
        } else if (useSingular()) {
            question.append(isInterfaceMethod(superMethods[0]) ? "implements" : "overrides").append(" method of the ");
            question.append(isInterfaceMethod(superMethods[0]) ? "interface" : "class").append(" ");
            question.append(superMethods[0].getContainingClass().getName()).append(".\n");
        }
        question.append("Do you want to find usages of the base method").append(usePlural() ? "s?" : "?");
        return question.toString();
    }

    protected ReferenceCollection findCurrentElementUsages() {
//        JavaPsiFacade.getInstance().findClass().findMethodsByName()
        MethodReferencesSearch.SearchParameters searchParameters = new MethodReferencesSearch.SearchParameters((PsiMethod) this.psiElement, searchScope, false);
//        MethodReferencesSearch.search()
        return new ReferenceCollection(MethodReferencesSearch.INSTANCE.createQuery(searchParameters).findAll());
    }
}
