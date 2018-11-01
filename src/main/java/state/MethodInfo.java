package state;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import helper.MethodUtils;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;

public final class MethodInfo {
    private final Integer startOffset;
    private final Integer endOffset;
    private final String methodFullName;
    private int changesCount;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = MethodUtils.calculateSignature(method);
        this.changesCount = 0;
    }

    @Nullable
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

    public void incrementChangesCount() {
        changesCount++;
    }

    public int getChangesCount() {
        return changesCount;
    }
}
