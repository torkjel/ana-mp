/*
 * Created on Apr 3, 2005
 */
package anakata.modplay.example.cli;

import java.util.StringTokenizer;

import anakata.modplay.Meta;
import anakata.modplay.PlayList;
import anakata.modplay.Scope;
import anakata.modplay.module.Instrument;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.Output;
import anakata.sound.output.SoundDataFormat;
import anakata.sound.output.WavOutput;
import anakata.util.Logger;
import anakata.util.Util;

public class Player {

    private static anakata.modplay.Player player;

    private static boolean random = false;
    private static int channels = 2;
    private static String modName;
    private static boolean interpolate = true;
    private static int rate = 44100;
    private static int bits = 16;
    private static double volume = 1;
    private static double balance = 0.5;
    private static double separation = -1;
    private static int bufferTime = 500;
    private static boolean[] mute = new boolean[256]; // all false initially
    private static int startPosition = 0;
    private static boolean onlyInfo = false;
    private static boolean verbose = true;
    private static boolean debug = false;
    private static double amplification = -1;
    private static boolean writewav = false;
    private static long maxtime = -1;

    private static String bug =
            "\n"
            + "An error has occured. Please report bugs at:\n"
            + "http://sourceforge.net/projects/ana-mp/\n"
            + "or email torkjel@fast.no\n";

    private static String copyleft = Meta.COPYRIGHT_MESSAGE;

    private static String usage =
            "usage:  java anakata.modplay.Player [options] <modname|playlist>\n"
            + "        modname is the filename of a module (.mod or .xm)\n"
            + "        playlist a file containing a list of modules separated by newlines\n"
            + "options:\n"
            + "\tampX            - set amplification in percent, (0 - inf)\n"
            + "\t                  [#tracks*100/4]\n"
            + "\tbalanceX        - set balance, (-100 - 100) [0]\n"
            + "\tbitsX           - 8 or 16 bits playback [16]\n"
            + "\tbufferX         - set size of buffer to X milliseconds [500]\n"
            + "\tdebug           - display debug information\n"
            + "\tinfo            - show module info, don't play\n"
            + "\tnoint           - don't use interpolation\n"
            + "\tmaxtimeX        - exit/go to the next module if it has been\n" +
              "\t                  playing for more than X minutes\n"
            + "\tmono            - play module in mono\n"
            + "\tmuteX[:Y...]    - mute track X [and Y and...]\n"
            + "\tpatternX        - start playing at pattern X [0]\n"
            + "\tquiet           - don't display any information\n"
            + "\trandom          - play modules from the playlist in random order\n"
            + "\trateX           - mix at rate X (X = <11025|22050|44100>) [44100]\n"
            + "\tseperationX     - set the seperation of the channels.\n"
            + "\t                  0 = mono, 100 = \"total\" stereo (0-100)\n"
            + "\t                  [100 for .xm, 50 for .mod]\n"
            + "\tvolumeX         - set volume to X (0-100) [100]\n"
            + "\twav             - write to a wav file instead of speakers\n";

    public static void main(String[] args) {

        System.out.println(copyleft);

        if (args.length == 0) {
            System.out.println(usage);
            Util.exit(1);
        }

        try {

            // The player can be invoked with a module or a playlist. A playlist
            // object is used in both cases.
            PlayList pl = null;
            if (isList(args[args.length - 1]))
                pl = new PlayList(args[args.length - 1]);
            else
                pl = new PlayList(new String[] { args[args.length - 1] });

            // for each module in the playlist...
            while (pl.hasNext()) {

                initialize(args, pl);

                Logger.enableDebug(debug);
                Logger.enableInfo(!verbose);

                player = new anakata.modplay.Player();

                SoundDataFormat sdf = new SoundDataFormat(bits, rate, channels);

                Output out;
                if (writewav)
                    out = new WavOutput(modName + ".wav", sdf);
                else
                    out = new JavaSoundOutput(sdf, bufferTime);

                if (!player.init(out, interpolate))
                    throw new Exception("could not init player");

                if (!player.load(modName))
                    throw new Exception("could not load module");

                // TODO: use ModuleInfo here...
                Logger.info(player.getModule().getInfo());
                if (onlyInfo)
                    continue;

                player.setPosition(startPosition);
                player.setBalance(balance);
                player.setVolume(volume);
                player.setSeparation(separation);

                if (amplification >= 0) // use default amp for values < 0...
                    player.setAmplification(amplification);

                // set muting...
                player.mute(mute);

                int tracks = player.getModule().getTrackCount();
                Scope scope = new Scope(tracks, bufferTime+200);

                // play loop...
                long startTime = System.currentTimeMillis();
                do {

                	if (maxtime != -1 && System.currentTimeMillis() - startTime > maxtime)
                		break;
                	
                    // update scopes
                    int position = player.getPosition();
                    int division = player.getDivision();
                    int tick = player.getTick();
                    if (tick == 0) {
                        scope.next();
                        for (int n = 0; n < tracks; n++)
                            if (player.getNote(n)!= Instrument.NO_NOTE)
                                scope.poke(n);
                    }

                    // move cursor to start of current line and print position info and scopes.
                    Logger.info((char) 13);
                    Logger.info(
                        "Pos:     " +
                        pad(tick, 2) + "/" +
                        pad(division, 3) + "/" +
                        pad(position, 3) +
                        "  [" + scope.getAllChannels() + "]");

                } while (player.play());

                player.close();

                Logger.info("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n" + bug);
            System.exit(1);
        }

        Util.exit(0);
    }


    /**
     * initializes the example player
     *
     * @param args the arguments used when starting the player
     * @param pl a playlist
     */
    private static void initialize(String[] args, PlayList pl) throws Exception {

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("amp"))
                amplification = Integer.parseInt(args[i].substring(3)) / 100.0;
            else if (args[i].equalsIgnoreCase("quiet"))
                verbose = false;
            else if (args[i].equalsIgnoreCase("debug"))
                debug = true;
            else if (args[i].equalsIgnoreCase("info"))
                onlyInfo = true;
            else if (args[i].startsWith("mute")) {
                StringTokenizer st = new StringTokenizer(args[i].substring(4), ":");
                while (st.hasMoreTokens())
                    mute[Integer.parseInt(st.nextToken())] = true;
            } else if (args[i].startsWith("mono"))
                channels = 1;
            else if (args[i].startsWith("pattern"))
                startPosition = Integer.parseInt(args[i].substring(7));
            else if (args[i].startsWith("rate")) {
                int r = Integer.parseInt(args[i].substring(4));
                if (r != 11025 && r != 44100 && r != 22050)
                    throw new Exception("rate must be 11025, 22050 or 44100");
                else
                    rate = r;
            } else if (args[i].startsWith("bits")) {
                int b = Integer.parseInt(args[i].substring(4));
                if (b != 8 && b != 16)
                    throw new Exception("bits must be 8 or 16");
                else
                    bits = b;
            } else if (args[i].equals("random"))
                random = true;
            else if (args[i].startsWith("noint"))
                interpolate = false;
            else if (args[i].startsWith("maxtime"))
            	maxtime = Integer.parseInt(args[i].substring(7))*60*1000;
            else if (args[i].startsWith("volume")) {
                int v = Integer.parseInt(args[i].substring(6));
                if (v < 0 || v > 255)
                    throw new Exception("volume must be 0-100");
                else
                    volume = v / 100.0;
            } else if (args[i].startsWith("balance")) {
                int b = Integer.parseInt(args[i].substring(7));
                if (b < -100 || b > 100)
                    throw new Exception("balance must be -100 - 100");
                else
                    balance = b / 200.0 + 0.5;
            } else if (args[i].startsWith("seperation")) {
                int s = Integer.parseInt(args[i].substring(10));
                if (s < 0 || s > 100)
                    throw new Exception("seperation must be 0-100");
                else
                    separation = s / 100.0;
            } else if (args[i].startsWith("buffer")) {
                int t = Integer.parseInt(args[i].substring(6));
                bufferTime = t;
            } else if (args[i].equalsIgnoreCase("wav")) {
                writewav = true;
            }
        }

        if (!random)
            modName = pl.nextFileName();
        else
            modName = pl.getRandomFileName();

        // TODO: filetype detection is done far too many places and only looks at the extension...
        if (separation == -1) {
            if (modName.toLowerCase().endsWith(".xm"))
                separation = 1;
            else if (modName.toLowerCase().endsWith(".mod"))
                separation = 0.5;
            else if (modName.toLowerCase().endsWith(".s3m"))
                separation = 1;
        }
    }

    /**
     * determines if the filename given is the name of a playlist
     *
     * @param name the name of a file
     * @return true if the file is a playlist
     */
    private static boolean isList(String name) {
        return
            name.toLowerCase().endsWith(".lst") ||
            name.toLowerCase().endsWith(".list") ||
            name.toLowerCase().endsWith(".txt");

    }

    private static String pad(int value, int len) {
        StringBuffer sb = new StringBuffer();
        int val = value;
        int count = 0;
        while (val != 0 && count++ < len) {
            sb.insert(0,val % 10);
            val /= 10;
        }
        while (count++ < len)
            sb.insert(0,'0');
        return sb.toString();
    }
}
