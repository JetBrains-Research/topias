package state;

public class IsRunning {
    private static IsRunning ourInstance = new IsRunning();
    private volatile boolean isRunning = false;

    public static IsRunning getInstance() {
        return ourInstance;
    }

    private IsRunning() {
        isRunning = false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void setRunning(boolean running) {
        isRunning = running;
    }
}
