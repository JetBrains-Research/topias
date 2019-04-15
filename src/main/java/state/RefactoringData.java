package state;

public class RefactoringData {
    private final MethodInfo oldMethod;
    private final MethodInfo newMethod;

    public RefactoringData(MethodInfo oldMethod, MethodInfo newMethod) {
        this.oldMethod = oldMethod;
        this.newMethod = newMethod;
    }

    public MethodInfo getOldMethod() {
        return oldMethod;
    }

    public MethodInfo getNewMethod() {
        return newMethod;
    }
}
