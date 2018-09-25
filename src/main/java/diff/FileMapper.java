package diff;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.*;

public final class FileMapper {

    final private Map<PsiMethod, AbstractMap.SimpleEntry<Integer, Integer>> methodToBounds;
    final private PsiManager psiManager;
    final private PsiDocumentManager psiDocumentManager;

    public FileMapper(Project project, Collection<VirtualFile> files) {
        methodToBounds = new HashMap<>();
        this.psiManager = PsiManager.getInstance(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        init(files);
    }

    private void init(Collection<VirtualFile> files) {
        files.parallelStream()
                .map(psiManager::findFile)
                .filter(Objects::nonNull)
                .forEach(x -> x.acceptChildren(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (element instanceof PsiMethod) {
                            final PsiMethod method = (PsiMethod) element;
                            final Document document = psiDocumentManager.getDocument(x);
                            final TextRange range = method.getTextRange();
                            final int start = document.getLineNumber(range.getStartOffset());
                            final int end = document.getLineNumber(range.getEndOffset());
                            methodToBounds.put(method, new AbstractMap.SimpleEntry<>(start, end));
                        }

                        super.visitElement(element);
                    }
                }));
    }

    public Map<PsiMethod, AbstractMap.SimpleEntry<Integer, Integer>> getMethodToBounds() {
        return methodToBounds;
    }
}

