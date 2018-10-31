package state;

import com.intellij.psi.PsiMethod;
import helper.MethodUtils;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;

public class ShortInfo {
    private Integer startOffset;
    private Long changesCount;
    private String methodFullName;

    public ShortInfo(Integer startOffset, String methodFullName) {
        this.startOffset = startOffset;
        this.changesCount = 1L;
        this.methodFullName = methodFullName;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Long getChangesCount() {
        return changesCount;
    }

    public void incrementChangesCount() {
        changesCount++;
    }

    public String getMethodFullName() {
        return methodFullName;
    }

    public void setMethodFullName(String methodFullName) {
        this.methodFullName = methodFullName;
    }
}
