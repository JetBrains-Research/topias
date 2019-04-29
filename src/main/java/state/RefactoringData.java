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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RefactoringData refactoringData = (RefactoringData) o;
        return oldMethod.equals(refactoringData.oldMethod) && newMethod.equals(refactoringData.newMethod);
    }
}
