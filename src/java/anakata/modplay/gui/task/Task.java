/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui.task;

/**
 * @author torkjel
 */
public abstract class Task {

    private String name;
    private int interval;
    private int counter;
    private long time;
    
    protected Task(String name, int interval) {
        this.name = name;
        this.interval = interval;
        counter = 1;
        time = System.currentTimeMillis();
    }
    
    public int getInterval() {
        return interval;
    }

    public String getName() {
        return name;
    }
    
    public boolean isTime() {
        long newTime = System.currentTimeMillis();
        long p1 = time % interval;
        long p2 = newTime % interval;
        time = newTime;
        return p2 < p1;
    }
    
    public abstract boolean doTask();
    public abstract void stop();
}
