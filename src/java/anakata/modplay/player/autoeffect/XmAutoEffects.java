package anakata.modplay.player.autoeffect;

import anakata.modplay.player.TrackState;

/**
 * Tiis class is necessery because the envelopes of an .XM module doesn't operate 
 * entirely independently. The only real work done by this class is to, in the 
 * constructor, test if the volume envelope is active and, if so, activate
 * the fadeout effect. All other calls are passed on to the individual effects. 
 * This should really be reimplemented as a test in Fadeout instead....
 * @author torkjel
 */
public class XmAutoEffects implements Envelope
{
	private VolumeEnvelope volEnv;
	private PanningEnvelope panEnv;
	private AutoVibrato autoVib;
	private Fadeout fadeout;

	public XmAutoEffects(
		VolumeEnvelope volEnv,
		PanningEnvelope panEnv,
		AutoVibrato autoVib,
		Fadeout fadeout)
	{
		this.volEnv = volEnv;
		this.panEnv = panEnv;
		this.autoVib = autoVib;
		this.fadeout = fadeout;
		if (volEnv.isActive())
			fadeout.activate();
		else
			fadeout.deActivate();
	}

	public void setNumberOfTracks(int tracks)
	{
		volEnv.setNumberOfTracks(tracks);
		panEnv.setNumberOfTracks(tracks);
		fadeout.setNumberOfTracks(tracks);
        autoVib.setNumberOfTracks(tracks);
	}

	public void doEffect(TrackState state, int track)
	{
		volEnv.doEffect(state, track);
		panEnv.doEffect(state, track);
		fadeout.doEffect(state, track);
        autoVib.doEffect(state, track);
	}

    public void keyOff(int track) {
        volEnv.keyOff(track);
        panEnv.keyOff(track);
        fadeout.keyOff(track);
        autoVib.keyOff(track);
    }
    
	public void newNote(double note, int track)
	{
		volEnv.newNote(note, track);
		panEnv.newNote(note, track);
		fadeout.newNote(note, track);
        autoVib.newNote(note, track);
	}

	public void reset(int track)
	{
		volEnv.reset(track);
		panEnv.reset(track);
		fadeout.reset(track);
        autoVib.reset(track);
	}

	public void setPosition(int pos, int track)
	{
		volEnv.setPosition(pos, track);
		panEnv.setPosition(pos, track);
	}
}
