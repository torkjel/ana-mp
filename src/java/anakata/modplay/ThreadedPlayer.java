/*
 * Created on Apr 5, 2005
 */
package anakata.modplay;

/**
 * a threaded player interface. You should probably <b>not</b> call it's play() method directly
 * after starting the player thread. That is very likely to cause "issues"...
 *
 * @author torkjel
 */
public class ThreadedPlayer extends Player implements Runnable {

    private Thread t;
    private Throwable errorCause;
    private boolean error = false;

    private boolean running = false;
    private boolean paused = false;

    /**
     */
    public ThreadedPlayer() {
        super();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (running) {
                if (!paused) play();
                else try { Thread.sleep(10); } catch (InterruptedException e) { }
            }
        } catch (Exception e) {
            errorCause = e;
            error = true;
        }
        t = null;
        running = false;
    }

    /**
     * start playing the module
     */
    public void start() {
        running = true;
        t = new Thread(this);
        t.start();
    }

    /**
     * stop playing the module and the player thread
     */
    public void stop() {
        running = false;
    }

    /**
     * check if the player thread is still running
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * pause the player thread
     * @param pause
     */
    public void pause(boolean pause) {
        paused = pause;
    }

    /**
     * check if the module is paused
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * check if an error has occured while playing
     * @return
     */
    public boolean hasFailed() {
        return error;
    }

    /**
     * if an error occured while playing, use this to get the error cause
     * @return
     */
    public Throwable getFailiureCause() {
        return errorCause;
    }
}
