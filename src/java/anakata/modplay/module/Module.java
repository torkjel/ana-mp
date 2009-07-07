package anakata.modplay.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A module. Should be generic enough to support all module formats. A module
 * contains a set of patterns and instruments, Each pattern contains several
 * tracks (often 4), and each instrument contain several samples.
 *
 * @author torkjel
 */
public class Module {

    private String name;
    private String id;
    private String tracker;

    private String description;

    private Instrument[] instruments;
    private Pattern[] patterns;
    private int[] patternOrder;

    private int restartPos;
    private int initialBPM;
    private int initialSpeed;
    private double initialVolume;
    private double[] initialPanning;

    // TODO: support relative track volume in player.
    private double[] initialTrackVolume;

    private int panningType;

    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Indicates that the panning of a sample is decided by which track it is
     * played in (This is used by MODs.)
     */
    public static final int TRACK_PANNING = 1;

    /**
     * indicates that the panning of a sample is decided by the panning value of
     * that sample (This is uses by XMs)
     */
    public static final int SAMPLE_PANNING = 2;

    /**
     * indicates that the panning of a sample is decided by the panning value of
     * the instrument that this sample is a part of
     */
    public static final int INSTRUMENT_PANNING = 3;

    /**
     * @param name the name of the module
     * @param id the id (type) ofthis module
     * @param tracker the tracker this module was made with
     * @param instruments the instruments that this module will play
     * @param patterns the patterns of this instrument
     * @param patternOrder the order the patterns is to be played in
     * @param restartPos the position to restart the module at when it is finnished
     * @param initialBPM the speed in Beats Per Minute to start playing this module in
     * @param initialSpeed the initial speed in ticks per division to play this module in
     * @param initialVolume the initial volume
     * @param initialPanning the initial panning of each track
     */
    public Module(String name, String id, String tracker,
            Instrument[] instruments, Pattern[] patterns, int[] patternOrder,
            int restartPos, int initialBPM, int initialSpeed,
            double initialVolume, int panningType, double[] initialTrackVolume, double[] initialPanning) {
        this.name = name;
        this.id = id;
        this.tracker = tracker;
        this.instruments = instruments;
        this.patterns = patterns;
        this.patternOrder = patternOrder;
        this.restartPos = restartPos;
        this.initialBPM = initialBPM;
        this.initialSpeed = initialSpeed;
        this.initialVolume = initialVolume;
        this.panningType = panningType;
        this.initialTrackVolume = initialTrackVolume;
        this.initialPanning = initialPanning;
    }

    /**
     * @return info about this module in human readable form
     */
    public String getInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Name:    ");
        sb.append(pad(getName(), 25, ' '));
        sb.append(" Chan: ");
        sb.append(pad(getPatternAtPos(0).getTrackCount() + "", 3, ' '));
        sb.append(" Pos: ");
        sb.append(getNumberOfPositions());
        sb.append("\n");
        sb.append("Type:    ");
        sb.append(pad(getId(), 25, ' '));
        sb.append(" Inst: ");
        sb.append(pad(getNumberOfInstruments() + "", 3, ' '));
        sb.append(" Pat: ");
        sb.append(getNumberOfPatterns());
        sb.append("\n");
        sb.append("Tracker: ");
        sb.append(getTracker());
        sb.append("\n");
        return sb.toString();
    }

    private String pad(String str, int len, char padding) {
        StringBuffer sb = new StringBuffer();
        if (str.length() > len)
            sb.append(str.substring(0, len));
        else
            sb.append(str);
        while (sb.length() < len)
            sb.append(padding);
        return sb.toString();
    }

    /**
     * @return the name of the module
     */
    public String getName() {
        return name;
    }

    /**
     * @return the id of the module
     */
    public String getId() {
        return id;
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
     * @return the tracker used to make the module
     */
    public String getTracker() {
        return tracker;
    }

    /**
     * @return the number of instruments in the module
     */
    public int getNumberOfInstruments() {
        return instruments.length;
    }

    /**
     * @return the n'th instrument
     */
    public Instrument getInstrument(int n) {
        if (n >= instruments.length || n < 0)
            return null;
        return instruments[n];
    }

    /**
     * @return all the instruments used by the module
     */
    public Instrument[] getInstruments() {
        return instruments;
    }

    /**
     * @return the number of patterns in the module
     */
    public int getNumberOfPatterns() {
        return patterns.length;
    }

    /**
     * @return the number of positions in the module (the length of the module).
     */
    public int getNumberOfPositions() {
        return patternOrder.length;
    }

    /**
     * @return the pattern number at position n
     */
    public int getPatternIndexAtPos(int n) {
        return patternOrder[n];
    }

    /**
     * @return the n'th pattern
     */
    public Pattern getPattern(int n) {
        return patterns[n];
    }

    /**
     * @return the pattern at position n
     */
    public Pattern getPatternAtPos(int n) {
        return getPattern(getPatternIndexAtPos(n));
    }

    /**
     * @return the restart position
     */
    public int getRestartPos() {
        return restartPos;
    }

    /**
     * @return the initial speed (in BPM) of this module
     */
    public int getInitialBpm() {
        return initialBPM;
    }

    /**
     * @return the initial speed (in ticks per division) of this module
     */
    public int getInitialSpeed() {
        return initialSpeed;
    }

    /**
     * @return the initial volume of this module
     */
    public double getInitialVolume() {
        return initialVolume;
    }

    /**
     * @return the panning type (track, sample or instrument) of this module
     */
    public int getPanningType() {
        return panningType;
    }

    /**
     * get the value of the initial relative volume of a track.
     * @param track
     * @return
     */
    public double getInitialVolume(int track) {
    	return initialTrackVolume[track];
    }

    /**
     * @return the initial panning of the given track
     */
    public double getInitialPanning(int track) {
        return initialPanning[track];
    }

    /**
     * utility method for finding the number of tracks (channels) in the module. Note that this
     * isn't reliable if you've managed to construct a module that has a different number of
     * tracks per pattern. That's pretty unlikely though... It also doesn't work unless you
     * have atleast one pattern.
     * @return
     */
    public int getTrackCount() {
        return getPatternAtPos(0).getTrackCount();
    }

    public boolean hasProperty(String name) {
    	return properties.containsKey(name);
    }

    public String getProperty(String name) {
    	return properties.get(name);
    }

    public void setProperty(String name, boolean value) {
    	setProperty(name, String.valueOf(value));
    }

    public void setProperty(String name, double value) {
    	setProperty(name, String.valueOf(value));
    }

    public void setProperty(String name, int value) {
    	setProperty(name, String.valueOf(value));
    }

    public void setProperty(String name, String value) {
    	properties.put(name, value);
    }

    public Set<String> getPropertyNames() {
    	return properties.keySet();
    }
}
