package state;

import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class MethodsStorage {
    private static MethodsStorage instance = null;
    private final List<MethodInfo> deletedMethods;
    private final List<RefactoringData> movedMethods;
    private final List<MethodInfo> addedMethods;
    private final List<MethodInfo> recalcMethods;

    private MethodsStorage() {
        this.deletedMethods = new ArrayList<>();
        this.movedMethods = new ArrayList<>();
        this.addedMethods = new ArrayList<>();
        this.recalcMethods = new ArrayList<>();
    }

    public static MethodsStorage getInstance() {
        if (instance == null) {
            instance = new MethodsStorage();
        }

        return instance;
    }

    public List<MethodInfo> getDeletedMethods() {
        return deletedMethods;
    }

    public List<RefactoringData> getMovedMethods() {
        return movedMethods;
    }

    public List<MethodInfo> getAddedMethods() {
        return addedMethods;
    }

    public List<MethodInfo> getRecalcMethods() {
        return recalcMethods;
    }

    public void storeDeletedMethods(List<MethodInfo> deletedMethods) {
        this.deletedMethods.addAll(deletedMethods);
    }

    public void storeMovedMethods(List<RefactoringData> movedMethods) {
        this.movedMethods.addAll(movedMethods);
    }

    public void storeAddedMethods(List<MethodInfo> addedMethods) {
        this.addedMethods.addAll(addedMethods);
    }

    public void storeAddedMethod(MethodInfo methodInfo) {
        this.addedMethods.add(methodInfo);
    }

    public void storeRecalcMethods(List<MethodInfo> recalcMethods) {
        this.recalcMethods.addAll(recalcMethods);
    }

    public void clear() {
        deletedMethods.clear();
        movedMethods.clear();
    }

}
