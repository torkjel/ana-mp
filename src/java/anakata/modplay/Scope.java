/*
 * Created on Aug 31, 2004
 */
package anakata.modplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Some fake ascii scopes... It doesn't really consider the actual volume of each channel, it
 * just has a trigger and a gradual falloff.
 * @author torkjel
 */
public final class Scope {

    char[] levels = new char[] {' ', ' ', '.','.','o','o','O','O','@','@'};

    double[] scopes;
    int delay;

    private static final double FALLOFF = 2;

    public EventDispatcher dispatcher;

    /**
     * create a scope with <code>channels</code> channels. The <code>delay</code> value is necessary
     * in cases where there is a noticable delay from the audio data is sent to the sound system
     * until it appears at the speakers. 200ms seems to be a appropriate value for the java sound
     * system (in addition to any buffers in the program itself of course).
     * @param channels
     * @param delay
     */
    public Scope(int channels, int delay) {
        this.delay = delay;
        scopes = new double[channels];
        dispatcher = new EventDispatcher();
        new Thread(dispatcher).start();
    }

    private void nextInternal() {
        for (int n = 0; n < scopes.length; n++) {
            synchronized (scopes) {
                scopes[n] -= FALLOFF;
                    if (scopes[n] < 0) scopes[n] = 0;
            }
        }
    }

    /**
     * decrease the intensity value in all scopes.
     */
    public void next() {
        Event e = new Event();
        e.type = Event.NEXT;
        e.time = System.currentTimeMillis() + delay;
        queue.add(e);
    }

    private void pokeInternal(int channel) {
        synchronized (scopes) {
            scopes[channel] = levels.length-1;
        }
    }

    /**
     * "poke" a channel, causing it's intensity value to be set to the max.
     * @param channel
     */
    public void poke(int channel) {
        Event e = new Event();
        e.type = Event.POKE;
        e.channel = channel;
        e.time = System.currentTimeMillis() + delay;
        queue.add(e);
    }

    /**
     * get the character representing the current intencity value of a channel.
     * @param channel
     * @return
     */
    public char getChannel(int channel) {
        synchronized (scopes) {
            return levels[(int)scopes[channel]];
        }
    }

    /**
     * get a string representation of the scopes.
     * @return
     */
    public String getAllChannels() {
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < scopes.length; n++) {
            sb.append(getChannel(n));
        }
        return sb.toString();
    }

    public void stop() {

    }

    private static class Event {
        public static final int POKE = 1;
        public static final int NEXT = 2;
        public int type;
        public long time;
        public int channel;

        public String toString() {
            return type + " " + time + " " + channel;
        }
    }

    private List<Event> queue = new ArrayList<Event>();

    private class EventDispatcher implements Runnable {
        public boolean stop = false;

        public void run() {
            while (!stop) {
                do {
                    try {Thread.sleep(1);} catch (InterruptedException e) {}
                    if (queue.size() == 0)
                        continue;
                    Event e = queue.get(0);
                    if (e.time <= System.currentTimeMillis()) {
                        queue.remove(0);
                        if (e.type == Event.POKE) {
                            pokeInternal(e.channel);
                        } else if (e.type == Event.NEXT) {
                            nextInternal();
                        }
                    } else break;
                } while (true);
            }
        }
    }
}


