/*
 * Created on Apr 4, 2005
 */
package anakata.modplay.example.cli;

import anakata.modplay.Meta;
import anakata.modplay.ThreadedPlayer;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.Output;
import anakata.sound.output.SoundDataFormat;

/**
 * This example demonstrates how a module player can be created in the least possible amount of
 * code. This is very similar to the {@link anakata.modplay.example.cli.SimplePlayer} (take a look
 * at that one first, it's better documented), but uses the asynchrounus version of the player
 * interface ({@link anakata.modplay.ThreadedPlayer}).
 * @author torkjel
 */
public class SimpleThreadedPlayer {

    public static final String INFO = Meta.PROJECT_NAME + " " + Meta.VERSION;

    public static final int BITS = 16;
    public static final int RATE = 44100;
    public static final int CHANNELS = 2;
    public static final boolean INTERPOLATE = true;
    public static final int BUFFERSIZE = 500;

    /**
     * main method. The player is expecting to be invoked with one argument on the command line,
     * the filename of the module to play
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println(INFO);

        if (args.length == 0) {
            System.out.println("Usage:\n\tjava -cp ana-mp-<version>.jar " +
                    "anakata.modplay.example.cli.SimpleThreadedPlayer <module file>");
            System.exit(1);
        }

        String module = args[0];

        SoundDataFormat format = new SoundDataFormat(BITS, RATE, CHANNELS);

        Output out = new JavaSoundOutput(format, BUFFERSIZE);

        ThreadedPlayer player = new ThreadedPlayer();

        player.init(out, INTERPOLATE);

        player.load(module);

        player.start();

        while (player.isRunning()) {
            // do usefull stuff here or just sleep a bit.
            try { Thread.sleep(1); } catch (InterruptedException e) { }

            // if you decide you've had enough, call player.stop() to stop the music (don't
            // forget to call player.close() also...) :
            /*
             * if (something) {
             *     player.stop();
             *     break;
             * }
             */
        }

        if (player.hasFailed())
            player.getFailiureCause().printStackTrace();

        player.close();
    }
}