package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.psi.PsiMethod;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import processing.Utils;

import java.util.AbstractMap;
import java.util.Objects;

public final class MethodInfo implements Comparable<MethodInfo>, PersistentStateComponent<MethodInfo> {
    @Attribute
    private Integer startOffset;
    @Attribute
    private Integer endOffset;
    @Attribute
    private String methodFullName;

    private String fileName;

    private long timeChangeMade;
    private String authorInfo;
    private String branchName;

    public MethodInfo(Integer startOffset, Integer endOffset, PsiMethod method, String fileName) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = Utils.calculateSignature(method);
        this.fileName = fileName;
    }

    public MethodInfo(int startOffset, int endOffset, String methodFullName) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.methodFullName = methodFullName;
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
    public int hashCode() {
        return Objects.hash(methodFullName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return methodFullName.equals(that.methodFullName);
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Integer getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

    public String getMethodFullName() {
        return methodFullName;
    }

    public void setMethodFullName(String methodFullName) {
        this.methodFullName = methodFullName;
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

    public String getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(String authorInfo) {
        this.authorInfo = authorInfo;
    }

    public long getTimeChangeMade() {
        return timeChangeMade;
    }

    public void setTimeChangeMade(long timeChangeMade) {
        this.timeChangeMade = timeChangeMade;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
