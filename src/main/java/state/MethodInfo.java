package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import helper.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Objects;

public final class MethodInfo implements Comparable<MethodInfo>, PersistentStateComponent<MethodInfo> {
    @Attribute
    private final Integer startOffset;
    @Attribute
    private final Integer endOffset;
    @Attribute
    private final String methodFullName;
    @Attribute
    private int changesCount;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = MethodUtils.calculateSignature(method);
        this.changesCount = 0;
    }

    public MethodInfo() {
        this.startOffset = 0;
        this.endOffset = 0;
        this.methodFullName = "";
        this.changesCount = 0;
    }

    @Nullable
    public MethodInfo ifWithin(AbstractMap.SimpleEntry<Integer, Integer> diapason) {
        return startOffset <= diapason.getKey() && endOffset >= diapason.getValue() ? this : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return methodFullName.equals(that.methodFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodFullName);
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

    @Override
    public int compareTo(@NotNull MethodInfo o) {
        return this.getStartOffset() - o.getStartOffset();
    }

    @Override
    public MethodInfo getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MethodInfo state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
