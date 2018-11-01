package diff;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import helper.MethodUtils;
import state.ChangesState;
import state.MethodInfo;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.util.ArrayUtil.contains;

//@todo: rename
public final class FileMapper {
    @FunctionalInterface
    private interface TriFunction<A, B, C, D> {
        public D apply(A a, B b, C c);
    }

    final private PsiManager psiManager;
    final private PsiDocumentManager psiDocumentManager;
    final private TriFunction<PsiFile, Map<String, List<MethodInfo>>, PsiMethod, Boolean> isExists = (file, state, method) ->
            state.get(file.getVirtualFile().getCanonicalPath()).stream()
            .map(MethodInfo::getMethodFullName)
            .anyMatch(x -> x.equals(MethodUtils.calculateSignature(method)));

    public FileMapper(Project project) {
        this.psiManager = PsiManager.getInstance(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
    }

    public Map<String, List<MethodInfo>> vfsToMethodsData(Collection<VirtualFile> files) {
        final List<SimpleEntry<String, MethodInfo>> temporaryListOfTuples = new LinkedList<>();
        final Application app = ApplicationManager.getApplication();

        final Map<String, List<MethodInfo>> state = ChangesState.getInstance().getState().persistentState;

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
                                    final MethodInfo info = isExists.apply(x, state, method) ? state.get(x.getVirtualFile().getCanonicalPath());
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

