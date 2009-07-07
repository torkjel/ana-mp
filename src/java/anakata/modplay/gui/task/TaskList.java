/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author torkjel
 */
public class TaskList implements Runnable {

    private List<Task> tasks;
    private boolean stop = false;

    public static TaskList newInstance() {
        TaskList tl = new TaskList();
        tl.init();
        return tl;
    }
    
    private TaskList() {
        tasks = new ArrayList<Task>();
    }

    private void init() {
        new Thread(this).start();
    }
    
    public void run() {
        while (!stop) {

            // perform the action of each task
            Map<Task, Boolean> resultMap = new HashMap<Task, Boolean>();
            synchronized(tasks) {
            	for (Task t : tasks) {
                    if (t.isTime()) {
                        resultMap.put(t, t.doTask());
                    }
                }
            }
            
            // remove those tasks that returned false
            Iterator keys = resultMap.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                Object value = resultMap.get(key); 
                if (value.equals(Boolean.FALSE))
                    remove((Task)key);
            }
            
            try {Thread.sleep(20);} catch (InterruptedException e) {}
        }

        // stop each task
        synchronized(tasks) {
        	for (Task t : tasks) {
                t.stop();
            }
        }
    }

    public void stop() {
        stop = true;
    }

    public void add(Task t) {
        synchronized(tasks) {
            tasks.add(t);
        }
    }

    public Task get(String name) {
        synchronized(tasks) {
        	for (Task t : tasks) {
                if (t.getName().equals(name)) return t;
            }
        }
        return null;
    }

    public void remove(Task t) {
        synchronized(tasks) {
            tasks.remove(t);
        }
    }
}
