package helper;

import com.intellij.psi.PsiMethod;

import java.util.AbstractMap;

public final class MethodInfo {
    private final Integer startOffset;
    private final Integer endOffset;
    private final String methodFullName;
    private final PsiMethod psiMethod;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.psiMethod = method;
        this.methodFullName = MethodUtils.calculateSignature(method);
    }

    public MethodInfo ifWithin(AbstractMap.SimpleEntry<Integer, Integer> diapason) {
        return startOffset <= diapason.getKey() && endOffset >= diapason.getValue() ? this : null;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public Integer getEndOffset() {
        return endOffset;
    }

    public String getMethodFullName() {
        return methodFullName;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }
}
