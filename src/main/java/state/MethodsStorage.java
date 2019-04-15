package state;

import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class MethodsStorage {
    private static MethodsStorage instance = null;
    private final List<MethodInfo> deletedMethods;
    private final List<Pair<MethodInfo, MethodInfo>> movedMethods;
    private final List<MethodInfo> addedMethods;

    private MethodsStorage() {
        this.deletedMethods = new ArrayList<>();
        this.movedMethods = new ArrayList<>();
        this.addedMethods = new ArrayList<>();
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

    public List<Pair<MethodInfo, MethodInfo>> getMovedMethods() {
        return movedMethods;
    }

    public List<MethodInfo> getAddedMethods() {
        return addedMethods;
    }

    public void storeDeletedMethods(List<MethodInfo> deletedMethods) {
        this.deletedMethods.addAll(deletedMethods);
    }

    public void storeMovedMethods(List<Pair<MethodInfo, MethodInfo>> movedMethods) {
        this.movedMethods.addAll(movedMethods);
    }

    public void storeAddedMethods(List<MethodInfo> addedMethods) {
        this.addedMethods.addAll(addedMethods);
    }

    public void clear() {
        deletedMethods.clear();
        movedMethods.clear();
    }

}
