package anakata.modplay;

import java.io.File;
import java.io.IOException;

import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.loader.ModuleLoader;
import anakata.modplay.module.Module;
import anakata.modplay.player.DefaultMixer;
import anakata.modplay.player.FastMixer;
import anakata.modplay.player.InterpolatingMixer;
import anakata.modplay.player.Mixer;
import anakata.modplay.player.ModuleState;
import anakata.modplay.player.PlayerException;
import anakata.sound.output.Output;

/**
 * This is an application's main interface with ANA-MP.
 *
 * It contain methods for initalizing the player, loading, playing and closing a module. In addition
 * it has varius methods for getting and setting the most important parameters like volume,
 * balance and playing position. More fine grained control can be achieved by manipulating the
 * {@link anakata.modplay.player.ModuleState} and {@link anakata.modplay.module.Module} object
 * directly.
 *
 * @author torkjel
 */
public class Player {

    private ModuleState ms;
    private Output out;
    private Class lowLevelMixerClass;

    public Player() {
    }

    /**
     * initialize the player
     *
     * @param output
     * @return true if initialization was successful
     */
    public boolean init(Output output, boolean interpolate) {
        out = output;
        lowLevelMixerClass = interpolate ? InterpolatingMixer.class : FastMixer.class;
        return true;
    }

    /**
     * Load a module from a file
     *
     * @param fileName the file name of the module
     * @return true if loading was successful, false else
     */
    public boolean load(String fileName) throws InvalidFormatException, IOException {
        ModuleLoader ml = ModuleLoader.getModuleLoader(new File(fileName));
        Module module = ml.getModule();
        return load(module);
    }

    /**
     * Load a module
     * @param module the module to load
     * @return true if loading was successful, false else
     */
    public boolean load(Module module) {
        int tracks = module.getPatternAtPos(0).getTrackCount();

        Mixer mixer = new DefaultMixer(out, lowLevelMixerClass, tracks);

        ms = new ModuleState(module, mixer);

        // do some mixer initialization...
        mixer.setAmplification(getDefaultAmplification());

        return out.open();
    }

    /**
     * play a tick of the the module
     *
     * @return true if the module is still playing, false if the module is finished.
     */
    public boolean play() throws PlayerException {
        return ms.play();
    }

    public boolean close() {
        return out.close();
    }

    /**
     * get the state of the module. The ModuleState can be used for
     * finding information about the playing module and for manipulating how the
     * module should be played
     *
     * @return the state of the playing module
     */
    public ModuleState getModuleState() {
        return ms;
    }

    /**
     * get the current module;
     * @return
     */
    public Module getModule() {
        return getModuleState().getModule();
    }

    /**
     * get the default amplification. The default amplification is calculated using the following
     * formula: <code>#tracks / 4</code>. This should maintain a reasonable volume when playing
     * modules with many channels while avoiding clipping in most cases. Note that for 4channel mods
     * this will result in an amplification of 1.
     * @return
     */
    public double getDefaultAmplification() {
        return getModuleState().getModule().getPatternAtPos(0).getTrackCount() / 4.0;
    }

    public void setAmplification(double amp) {
        getModuleState().getMixer().setAmplification(amp);
    }

    public double getAmplification() {
        return getModuleState().getMixer().getAmplification();
    }

    public void mute(int track, boolean mute) {
        getModuleState().getMixer().setMute(track, mute);
    }

    public void mute(boolean[] mute) {
        for (int n = 0; n < mute.length && n < getModule().getTrackCount(); n++)
            mute(n,mute[n]);
    }

    public void setVolume(double volume) {
        getModuleState().getMixer().setVolume(volume);
    }

    public double getVolume() {
        return getModuleState().getMixer().getVolume();
    }

    public void setBalance(double balance) {
        getModuleState().getMixer().setBalance(balance);
    }

    public double getBalance() {
        return getModuleState().getMixer().getBalance();
    }

    public void setSeparation(double separation) {
        getModuleState().getMixer().setSeparation(separation);
    }

    public double getSeparation() {
        return getModuleState().getMixer().getSeparation();
    }

    public void setPosition(int pos) {
        getModuleState().setPosition(pos);
    }

    public int getPosition() {
        return getModuleState().getPosition();
    }

    public int getDivision() {
        return getModuleState().getDivision();
    }

    public int getTick() {
        return getModuleState().getTick();
    }

    public int getNote(int track) {
        return getModuleState().
            getModule().getPatternAtPos(getPosition()).getTrack(track).getNote(getDivision());
    }
}