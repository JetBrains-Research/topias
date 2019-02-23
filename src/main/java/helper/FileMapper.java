package helper;

import com.intellij.lang.java.JavaLanguage;
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
import java.util.Map.Entry;
import java.util.stream.Collectors;

//@todo: rename
public final class FileMapper {
    @FunctionalInterface
    private interface TriFunction<A, B, C, D> {
        D apply(A a, B b, C c);
    }

    private final PsiFileFactory psiFileFactory;
    final private PsiManager psiManager;
    final private PsiDocumentManager psiDocumentManager;
    final private Project project;
    final private TriFunction<String, Map<String, Set<MethodInfo>>, PsiMethod, Boolean> isExists = (fileName, state, method) ->
            state.getOrDefault(fileName, new HashSet<>()).stream()
                    .map(MethodInfo::getMethodFullName)
                    .anyMatch(x -> x.equals(MethodUtils.calculateSignature(method)));

    public FileMapper(Project project) {
        this.psiManager = PsiManager.getInstance(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.psiFileFactory = PsiFileFactory.getInstance(project);
        this.project = project;
    }

    public SimpleEntry<String, Set<MethodInfo>> vfsToMethodsData(String content, String fileName, String branchName) {
        final List<SimpleEntry<String, MethodInfo>> temporaryListOfTuples = new LinkedList<>();
        final Application app = ApplicationManager.getApplication();
        final Map<String, Set<MethodInfo>> state = Objects.requireNonNull(ChangesState.getInstance(project).getState())
                .persistentState
                .get(branchName)
                .getMethods();

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
                            final String fullName = MethodUtils.calculateSignature(method);

                            final MethodInfo info = isExists.apply(fileName, state, method) ?
                                    state.get(fileName).stream()
                                            .filter(x -> x.getMethodFullName().equals(fullName))
                                            .findFirst().get() :
                                    new MethodInfo(start, end, method);

                            info.update(start, end);

                            temporaryListOfTuples.add(new SimpleEntry<>(fileName, info));
                        }
                        super.visitElement(element);

                    }
                });
            }
        };

        app.invokeAndWait(runnable);

        return temporaryListOfTuples.stream().
                collect(Collectors.groupingBy(
                        SimpleEntry::getKey, Collectors.mapping(SimpleEntry::getValue, Collectors.toCollection(HashSet::new))
                )).entrySet().stream()
                .filter(x -> x.getKey() != null && x.getValue() != null)
                .map(x -> new SimpleEntry<String, Set<MethodInfo>>(x.getKey(), x.getValue()))
                .findFirst()
                .orElseGet(() -> new SimpleEntry<>(fileName, new HashSet<>()));
    }
}

