package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.psi.PsiMethod;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import processing.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Objects;

public final class MethodInfo implements Comparable<MethodInfo>, PersistentStateComponent<MethodInfo> {
    @Attribute
    private Integer startOffset;
    @Attribute
    private Integer endOffset;
    @Attribute
    private String methodFullName;
    @Attribute
    private int changesCount;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = MethodUtils.calculateSignature(method);
        this.changesCount = 0;
    }

    public MethodInfo(int startOffset, int endOffset, String methodFullName, int changesCount) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = methodFullName;
        this.changesCount = changesCount;
    }

    public MethodInfo() {
        this.startOffset = 0;
        this.endOffset = 0;
        this.methodFullName = "";
        this.changesCount = 0;
    }

    public void update(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public void update(int startOffset, int endOffset, String newName) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = newName;
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

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

    public void setMethodFullName(String methodFullName) {
        this.methodFullName = methodFullName;
    }

    public void setChangesCount(int changesCount) {
        this.changesCount = changesCount;
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
