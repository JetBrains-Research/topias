package diff;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FileMapper {

    final private Map<PsiMethod, TextRange> methodToBounds;
    final private PsiManager pm;

    public FileMapper(Project project, List<VirtualFile> files) {
        methodToBounds = new HashMap<>();
        this.pm = PsiManager.getInstance(project);
    }

    private void init(List<VirtualFile> files) {
        files.parallelStream()
                .map(pm::findFile)
                .filter(Objects::nonNull)
                .forEach(x -> x.acceptChildren(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (element instanceof PsiMethod)
                            methodToBounds.put((PsiMethod) element, element.getTextRange());

                        super.visitElement(element);
                    }
                }));
    }

    public Map<PsiMethod, TextRange> getMethodToBounds() {
        return methodToBounds;
    }
}

