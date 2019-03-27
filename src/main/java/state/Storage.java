package state;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final List<MethodInfo> deletedMethods;
    private final List<MethodInfo> movedMethods;
    private final List<MethodInfo> addedMethods;
    private static Storage instance = null;

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }

        return instance;
    }

    private Storage() {
         this.deletedMethods = new ArrayList<>();
         this.movedMethods = new ArrayList<>();
         this.addedMethods = new ArrayList<>();
    }

    public List<MethodInfo> getDeletedMethods() {
        return deletedMethods;
    }

    public List<MethodInfo> getMovedMethods() {
        return movedMethods;
    }

    public List<MethodInfo> getAddedMethods() {
        return addedMethods;
    }

    public void storeDeletedMethods(List<MethodInfo> deletedMethods) {
        this.deletedMethods.addAll(deletedMethods);
    }

    public void storeMovedMethods(List<MethodInfo> movedMethods) {
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
