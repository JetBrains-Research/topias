package state;

import com.intellij.psi.PsiMethod;
import com.intellij.vcs.log.Hash;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class BranchInfo {
    private Hash lastCommitId;
    private final List<MethodInfo> methodsHistory = new LinkedList<>();

    public BranchInfo(Hash commitId) {
        lastCommitId = commitId;
    }

    public BranchInfo() { }

    public void add(MethodInfo info) {
        methodsHistory.add(info);
    }

    public void bumpCommitId(Hash id) {
        lastCommitId = id;
    }

    public void addCommit(Collection<MethodInfo> infos, Hash id) {
        methodsHistory.addAll(infos);
        lastCommitId = id;
    }

    public MethodInfo findMethod(PsiMethod method) {
        return methodsHistory.stream().filter(x -> x.getPsiMethod().equals(method)).findFirst().orElse(null);
    }

    public Hash getLastCommitId() {
        return lastCommitId;
    }
}
