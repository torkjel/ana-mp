/*
 * Created on Apr 4, 2005
 */
package anakata.modplay.example.cli;

import anakata.modplay.Meta;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.Output;
import anakata.sound.output.SoundDataFormat;

/**
 * This example demonstrates how a module player can be created in the least possible amount of
 * code.
 * @author torkjel
 */
public class SimplePlayer {

    public static final String INFO = Meta.PROJECT_NAME + " " + Meta.VERSION;

    /** use 16 bit playback */
    public static final int BITS = 16;

    /** use a playback rate of 44100 samples per second */
    public static final int RATE = 44100;

    /** play in stereo */
    public static final int CHANNELS = 2;

    /** use interpolated mixing */
    public static final boolean INTERPOLATE = true;

    /** use an output buffer with space for half a second of audio data. An output buffer is
     * necessary to avoid stuttering if your maching gets busy doing something else for a while.
     * The larger the output buffer the less is the chance of stuttering. */
    public static final int BUFFERSIZE = 500;

    /**
     * main method. The player is expecting to be invoked with one argument on the command line,
     * the filename of the module to play
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println(INFO);

        // if no argument is given, print a usage string an exit
        if (args.length == 0) {
            System.out.println("Usage:\n\tjava -cp ana-mp-<version>.jar " +
                    "anakata.modplay.example.cli.SimplePlayer <module file>");
            System.exit(1);
        }

        // get the module file name
        String module = args[0];

        // the SoundDataFormat object is a description of the data format (and thus quality) of
        // the sound output.
        SoundDataFormat format = new SoundDataFormat(BITS, RATE, CHANNELS);

        // create the output plugin to use. The JavaSoundOutput plugin is used to play sound using
        // java's built in sound system.
        Output out = new JavaSoundOutput(format, BUFFERSIZE);

        // create the player object.
        anakata.modplay.Player player = new anakata.modplay.Player();

        // initialize the player object. This tells it to use the output plugin we created and to
        // mix the sound in an interpolated fashion (which gives better sound quality than
        // non-interpolated mixing, but needs some more CPU power)
        player.init(out, INTERPOLATE);

        // load the module itself.
        player.load(module);

        // play the module. the play method generates a up to a "tick" of sound data and sends it
        // to the output plugin each time it is called. It is important that it is called once
        // every few milliseconds to prevent the output buffer from emptying. Note that it is safe
        // to run it in a tight loop like this because it will automatically throttle itself if
        // data is added to quickly to the output buffer. This also means that you should really
        // run this code in a sepatate thread if you want to do synything else, like updating an
        // animation, while the module is playing.
        //
        // The play method returns false, thus breaking the loop, as soon as the module is
        // finished playing
        while (player.play());

        // close the player
        player.close();
    }
}