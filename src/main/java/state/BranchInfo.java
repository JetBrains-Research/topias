package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.*;
import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class BranchInfo implements PersistentStateComponent<BranchInfo> {
    @MapAnnotation
    private Map<String, Set<MethodInfo>> methods;

    @Attribute
    private String hashValue;

    public Map<String, Set<MethodInfo>> getMethods() {
        return methods;
    }

    public BranchInfo() {
        methods = new HashMap<>();
        hashValue = "";
    }

    public BranchInfo(String hashValue, Map<String, Set<MethodInfo>> methods) {
        this.methods = new HashMap<>(methods);
        this.hashValue = hashValue;
    }

    public void updateHashValue(Hash hash) {
        this.hashValue = hash.asString();
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setMethods(Map<String, Set<MethodInfo>> methods) {
        this.methods = methods;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    @Nullable
    @Override
    public BranchInfo getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull BranchInfo state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
