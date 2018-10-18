package diff;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import helper.MethodInfo;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;

//@todo: rename
public final class FileMapper {
    final private PsiManager psiManager;
    final private PsiDocumentManager psiDocumentManager;

    public FileMapper(Project project) {
        this.psiManager = PsiManager.getInstance(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
    }

    public Map<String, List<MethodInfo>> vfsToMethodsData(Collection<VirtualFile> files) {
        final List<SimpleEntry<String, MethodInfo>> temporaryListOfTuples = new LinkedList<>();
        final Application app = ApplicationManager.getApplication();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                files.stream()
                        .map(psiManager::findFile)
                        .filter(Objects::nonNull)
                        .forEach(x -> x.acceptChildren(new JavaRecursiveElementVisitor() {
                            @Override
                            public void visitElement(PsiElement element) {
                                if (element instanceof PsiMethod) {
                                    final PsiMethod method = (PsiMethod) element;
                                    final Document document = psiDocumentManager.getDocument(x);
                                    final String fullClassName = x.getVirtualFile().getCanonicalPath();
                                    assert document != null;
                                    final TextRange range = method.getTextRange();
                                    final int start = document.getLineNumber(range.getStartOffset());
                                    final int end = document.getLineNumber(range.getEndOffset());
                                    temporaryListOfTuples.add(new SimpleEntry<>(fullClassName, new MethodInfo(start, end, method)));
                                }
                                super.visitElement(element);

                            }
                        }));
            }
        };

        app.invokeAndWait(runnable);

        return temporaryListOfTuples.stream().
                collect(Collectors.groupingBy(
                        SimpleEntry::getKey, Collectors.mapping(SimpleEntry::getValue, Collectors.toList())
                ));
    }
}

