package anakata.modplay.player.autoeffect;

import anakata.modplay.player.TrackState;

/**
 * Interface inplemented by all autoefects. 
 * The .XM format defines autoeffects for each instrument and new instances of 
 * the effects are created for each time the instrument is played. This means that
 * there ar maximum numberOfTracks autoeffects of a given type active for a given
 * instrument at any one time.
 * All AutoEffect objects are associated with an instrument and each AutoEffect 
 * object handles numberOfTracks instances of that spesific autoeffect, one for
 * each track. 
 * @author torkjel
 */
public interface AutoEffect
{
	/**
	 * sets the number of track in a module
	 * @param tracks the number of tracks in a module
	 */
    public void setNumberOfTracks(int tracks);

    /**
     * tells the autoeffect that a new note is played.
     * @param note the new note
     * @param track the track where the note is played
     */
    public void newNote(double note, int track);

	/**
	 * perform the effect
	 * @param state the state of the track where the effect is used
	 * @param track the number of the track where effect is used
	 */
    public void doEffect(TrackState state, int track);

	/**
	 * reset the effect in the given track
	 * @param track
	 */
    public void reset(int track);
    
    /**
     * signal to the autoeffect that a key-off note/effect was played
     * @param track
     */
    public void keyOff(int track);
}
