package helper;

import com.intellij.psi.PsiMethod;

import java.util.AbstractMap;

public final class MethodInfo {
    private final Integer startOffset;
    private final Integer endOffset;
    private final PsiMethod method;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.method = method;
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

    public PsiMethod getMethod() {
        return method;
    }
}
