package state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class BranchInfo implements PersistentStateComponent<BranchInfo> {
    @Attribute
    private final Map<String, SortedSet<MethodInfo>> methods;

    @Attribute
    private String hashValue;

    public Map<String, SortedSet<MethodInfo>> getMethods() {
        return methods;
    }

    public BranchInfo() {
        methods = new HashMap<>();
        hashValue = "";
    }

    public void updateHashValue(Hash hash) {
        this.hashValue = hash.asString();
    }

    public Hash getHashValue() {
        return HashImpl.build(hashValue);
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
