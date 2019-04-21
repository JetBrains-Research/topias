package processing;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import state.MethodInfo;

import java.util.LinkedList;
import java.util.List;

public final class PsiBuilder {
    private final PsiFileFactory psiFileFactory;
    final private PsiDocumentManager psiDocumentManager;

    public PsiBuilder(Project project) {
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.psiFileFactory = PsiFileFactory.getInstance(project);
    }

    public List<MethodInfo> buildMethodInfoSetFromContent(String content, String fileName) {
        final Application app = ApplicationManager.getApplication();
        final List<MethodInfo> infos = new LinkedList<>();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final PsiFile psiFile = psiFileFactory.createFileFromText(JavaLanguage.INSTANCE, content);
                psiFile.acceptChildren(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (element instanceof PsiMethod) {
                            final PsiMethod method = (PsiMethod) element;
                            final Document document = psiDocumentManager.getDocument(psiFile);
                            assert document != null;
                            final TextRange range = method.getTextRange();
                            final int start = document.getLineNumber(range.getStartOffset());
                            final int end = document.getLineNumber(range.getEndOffset());
                            infos.add(new MethodInfo(start, end, method, fileName));
                        }
                        super.visitElement(element);

                    }
                });
            }
        };

        app.invokeAndWait(runnable);

        return infos;
    }
}

