package state;

import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public class IsRunning {
    private static IsRunning ourInstance = new IsRunning();
    private volatile Map<Project, Boolean> isRunning = new HashMap<>();

    public static IsRunning getInstance() {
        return ourInstance;
    }

    private IsRunning() {

    }

    public synchronized boolean isRunning(Project project) {
        if (!isRunning.containsKey(project))
            isRunning.put(project, false);

        return isRunning.get(project);
    }

    public synchronized void setRunning(Project project, boolean running) {
        if (!isRunning.containsKey(project))
            return;

        isRunning.replace(project, running);
    }

    public synchronized void onProjectClose(Project project) {
        isRunning.remove(project);
    }
}
