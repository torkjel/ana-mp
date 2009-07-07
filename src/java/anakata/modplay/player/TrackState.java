package anakata.modplay.player;

import anakata.modplay.module.*;
import anakata.modplay.player.effect.*;
import anakata.modplay.player.autoeffect.*;

/**
 * Together with ModuleState this is the heart of ANA-MP. TrackState represents 
 * the state of a track in the module. It deals with local effects, notes and 
 * instruments started in a track.
 * @author torkjel
 *
 */
public class TrackState
{
	private ModuleState moduleState;
	private Module module;
	private Mixer mixer;
	private LocalEffects effects;
	private int trackNumber;

	// changed by track and effects:
	private double volume;
	private double panning;
	private int note;
	private double relativeNote;
	private double fineTune;
	private int instrument;
	private double sampleOffset;
    private Sample sample;
	private int loopType, loopStart, loopLength;
	private int sampleDelay;

	// changed by autoEffects:
	private double envelopeVolume, fadeoutVolume;
	private double envelopePanning;

	private AutoEffect[] autoEffects;

	/**
	 * @param moduleState the associated module state 
	 * @param module the module being played
	 * @param mixer the mixer 
	 * @param trackNumber the track number of this track
	 */
	public TrackState(
		ModuleState moduleState,
		Module module,
		Mixer mixer,
		int trackNumber)
	{
		this.moduleState = moduleState;
		this.module = module;
		this.mixer = mixer;
		effects = new LocalEffects();
		this.trackNumber = trackNumber;
		panning = module.getInitialPanning(trackNumber);
		note = Instrument.NO_NOTE;
		instrument = Track.NO_INSTRUMENT;
		sampleDelay = 0;
		envelopeVolume = 1;
		fadeoutVolume = 1;
		envelopePanning = 0.5;
	}

	/**
	 * the effects may need to do some stuff before a tick is played
	 * @param pattern the current pattern position
	 * @param division the current division
	 * @param tick the current tick
	 */
	public void preEffect(int pattern, int division, int tick)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		for (int m = 0; m < track.getNumberOfEffects(division); m++)
		{
			int effNum = track.getEffect(division, m);
			int arg1 = track.getEffectArg1(division, m);
			int arg2 = track.getEffectArg2(division, m);
			effects.preEffect(
				this,
				trackNumber,
				pattern,
				division,
				tick,
				effNum,
				arg1,
				arg2);
		}
	}

	/**
	 * load new intruments, notes and autoeffects, and do whatever needs to be 
	 * done to volume and panning 
	 * @param pattern
	 * @param division
	 * @param tick
	 */
	public void loadTick(int pattern, int division, int tick)
	{

        if (tick == sampleDelay)
		{
			sampleDelay = 0;

			int newNote = getNote(pattern, division, trackNumber);
			int newInstrument = getInstrument(pattern, division, trackNumber);
			if (newNote != Instrument.NO_NOTE
				&& !noteIsArgument(pattern, division, trackNumber)
				&& newInstrument != Track.NO_INSTRUMENT)
				newNoteAndInstrument(newNote, newInstrument);
			else if (
				newNote != Instrument.NO_NOTE 
				&& !noteIsArgument(pattern, division, trackNumber)
				&& newInstrument == Track.NO_INSTRUMENT)
				newNote(newNote);
			else if (
				(newNote == Instrument.NO_NOTE
					|| noteIsArgument(pattern, division, trackNumber))
					&& newInstrument != Track.NO_INSTRUMENT)
				newInstrument(newInstrument);
		}
	}

	/**
	 * do the effects
	 * @param pattern current 
	 * @param division
	 * @param tick
	 */
	public void doEffects(int pattern, int division, int tick)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		for (int m = 0; m < track.getNumberOfEffects(division); m++)
		{
			int effNum = track.getEffect(division, m);
			int arg1 = track.getEffectArg1(division, m);
			int arg2 = track.getEffectArg2(division, m);
			effects.doEffect(
				this,
				trackNumber,
				pattern,
				division,
				tick,
				effNum,
				arg1,
				arg2);
		}

		envelopeVolume = 1;
		fadeoutVolume = 1;
		envelopePanning = 0.5;

		for (int n = 0; autoEffects != null && n < autoEffects.length; n++) {
			autoEffects[n].doEffect(this, trackNumber);
        }
	}

	/**
	 * Initialize the mixer for playing this track
	 * volume formula used: 
	 * volume = (trackVolume + volumeSlide + volumeTune) * 
	 *  envelopeVolume * fadeoutVolume
	 * panning formula used:
	 * p = panning + effects.panningSlide
	 * panning = p + min(p,1-p) * (envelopePanning - 0.5) * 2
	 * the rate is calculated in a format specific way using:
	 * note + fineTune + relativeNote + noteTune + noteSlide
	 * @param pattern
	 * @param division
	 * @param tick
	 * @param time
	 */
	public void setupMixer(int pattern, int division, int tick, double time) 
        throws PlayerException
	{
		double rate = sample != null ?
            sample.getUnits().note2rate(
			//module.getModuleUnits().note2rate(
				note
					+ fineTune
					+ relativeNote
					+ effects.noteTune
					+ effects.noteSlide) : 0;
		double vol =
			(volume + effects.volumeSlide + effects.tremoloValue)
				* envelopeVolume
				* fadeoutVolume;

		if (vol < 0)
			vol = 0;
		else if (vol > 1)
			vol = 1;
		double pan = panning + effects.panningSlide;
		pan = pan + Math.min(pan, 1 - pan) * (envelopePanning - 0.5) * 2;
		if (pan < 0)
			pan = 0;
		else if (pan > 1)
			pan = 1;

		mixer.setTrack(
			sample != null ? sample.getData() : null,//sampleData,
			sampleOffset,
			rate,
			vol,
			pan,
			loopType,
			loopStart,
			loopLength,
			trackNumber);

		sampleOffset += time * rate / 1000;
	}

	/**
	 * the effects may need to do some stuff after a tick is played
	 * @param pattern
	 * @param division
	 * @param tick
	 */
	public void postEffects(int pattern, int division, int tick)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		for (int m = 0; m < track.getNumberOfEffects(division); m++)
		{
			int effNum = track.getEffect(division, m);
			int arg1 = track.getEffectArg1(division, m);
			int arg2 = track.getEffectArg2(division, m);
			effects.postEffect(
				this,
				trackNumber,
				pattern,
				division,
				tick,
				effNum,
				arg1,
				arg2);
		}
	}

	/**
	 * @return the modulestate associated with this trackstate 
	 */
	public ModuleState getModuleState()
	{
		return moduleState;
	}

	/**
	 * @return the module played
	 */
	public Module getModule()
	{
		return module;
	}

	/**
	 * sets the volume of this track
	 * @param vol new volume
	 */
	public void setVolume(double vol)
	{
		volume = vol;
	}

	/**
	 * @return the volume of this track
	 */
	public double getVolume()
	{
		return volume;
	}

	/**
	 * sets the panning used by this track
	 * @param pan the new panning
	 */
	public void setPanning(double pan)
	{
		panning = pan;
	}

	/**
	 * sets the finetune
	 * @param tune
	 */
	public void setFineTune(double tune)
	{
		fineTune = tune;
	}
	/**
	 * sets the current position in the sample playing in this track
	 * @param offset
	 */
	public void setSampleOffset(double offset)
	{
		sampleOffset = offset;
	}

	/**
	 * sets the delay of the sample playing in this track
	 * @param delay
	 */
	public void setSampleDelay(int delay)
	{
		sampleDelay = delay;
	}

	/**
	 * sets the current position in any volume/panning envelope for the instrument 
	 * playing in this track
	 * @param pos
	 */
	public void setEnvelopePosition(int pos)
	{
		for (int n = 0; autoEffects != null && n < autoEffects.length; n++)
			if (autoEffects[n] instanceof Envelope)
				 ((Envelope)autoEffects[n]).setPosition(pos, trackNumber);

	}

	/**
	 * @return the note playing in this track
	 */
	public int getNote()
	{
		return note;
	}

	/**
	 * sets the envelope panning for this track
	 * @param envPan
	 */
	public void setEnvelopePanning(double envPan)
	{
		envelopePanning = envPan;
	}

	/**
	 * sets the envelope volume for this track 
	 * @param envVol
	 */
	public void setEnvelopeVolume(double envVol)
	{
		envelopeVolume = envVol;
	}

	/**
	 * 
	 * @param fadeVol
	 */
	public void setFadeoutVolume(double fadeVol)
	{
		fadeoutVolume = fadeVol;
	}

	/**
	 * tests if key off is toggled in the spesific pattern position and division.
	 * This happens if a key off note or a key off effect is played.
	 * @param pattern
	 * @param division
	 * @param trackNumber
	 * @return true if key off is toggled, false else
	 */
/*	private boolean isKeyOff(int pattern, int division, int trackNumber)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		if (track.getNote(division) == Instrument.KEY_OFF)
			return true;
		for (int n = 0; n < track.getNumberOfEffects(division); n++)
		{
			int effect = track.getEffect(division, n);
			if (effect == Effect.XM_KEY_OFF)
				return true;
		}
		return false;
	}
*/

	/**
	 * tests if the note in this pattern position and division isn't realy a note
	 * but an argument to an effect.
	 * @param pattern
	 * @param division
	 * @param trackNumber
	 * @return true if the note value should be seen as an argument to an effect
	 */
	private boolean noteIsArgument(int pattern, int division, int trackNumber)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		for (int n = 0; n < track.getNumberOfEffects(division); n++)
		{
			int effect = track.getEffect(division, n);
			if (effect == Effect.MOD_SLIDE_TO_NOTE
				|| effect == Effect.XM_SLIDE_TO_NOTE)
				return true;
		}
		return false;
	}

	/**
	 * @param pattern the pattern position
	 * @param division
	 * @param trackNumber
	 * @return the note played in the given position/division
	 */
	private int getNote(int pattern, int division, int trackNumber)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		int tmpNote = track.getNote(division);
		return tmpNote;
	}

	/**
	 * @param pattern the pattern position
	 * @param division
	 * @param trackNumber
	 * @return the instrument played in the given position/division
	 */
	private int getInstrument(int pattern, int division, int trackNumber)
	{
		Track track = module.getPatternAtPos(pattern).getTrack(trackNumber);
		int tmpInstrument = track.getInstrumentNumber(division);
		return tmpInstrument;
	}

	/**
	 * returns the sample played by the given instrument for the given note.
	 * May return null if the instrument is undefined or the instrument does 
	 * not define a sample for the given note. 
	 * @param instrument
	 * @param note
	 * @return the sample played by the given instrument for the given note
	 */
	private Sample getSample(int instrument, int note)
	{
		Instrument instr = module.getInstrument(instrument);
		Sample sample = null;
		if (instr != null)
			sample = instr.getSampleByNote(note);
		return sample;
	}

	/** 
	 * What should happen if new note and instrument:
	 * set note
	 * set instrument
	 * reload sampleData
	 * reset sampleOffset
	 * reset volume
	 * reset panning
	 * reload autoeffects
	 * reset autoeffects
	 * reset loop
	 * reset keyOff
 	 */
	private void newNoteAndInstrument(int newNote, int newInstrument)
	{
        effects.newNoteAndInstrument(newNote, newInstrument);
/*		if (newNote != Instrument.KEY_OFF) */
			note = newNote;
		instrument = newInstrument;
        Sample sample = getSample(instrument, note);
		if (sample != null)
		{
			fineTune = sample.getFineTune();
			relativeNote = sample.getRelativeNote();
			this.sample = sample;
			volume = sample.getVolume();
			if (module.getPanningType() == Module.SAMPLE_PANNING)
				panning = sample.getPanning();
			else if (module.getPanningType() == Module.TRACK_PANNING)
				panning = module.getInitialPanning(trackNumber);
			else if (module.getPanningType() == Module.INSTRUMENT_PANNING)
				panning = module.getInstrument(instrument).getPanning();
			loopType = sample.getLoopType();
			loopStart = sample.getLoopStart();
			loopLength = sample.getLoopLength();
		}
		else
		{
			fineTune = 0;
			relativeNote = 0;
			this.sample = null;
			volume = 1;
			panning = 0.5;
		}
		sampleOffset = 0;
        
        Instrument instr = module.getInstrument(instrument); 
        if (instr != null)
            autoEffects = instr.getAutoEffects();

        for (int n = 0; autoEffects != null && n < autoEffects.length; n++)
		{
			autoEffects[n].reset(trackNumber);
			autoEffects[n].newNote(newNote, trackNumber);
		}
	}

	/**
	 * What should happen if new note, but keep instrument:
	 * set note
	 * keep instrument
	 * reload sampleData
	 * reset sampleOffset
	 * keep volume
	 * keep panning
	 * reset autoeffects
	 * reset loop
	 * reset keyOff
	 */
	private void newNote(int newNote)
	{
		effects.newNote(newNote);

		for (int n = 0; autoEffects != null && n < autoEffects.length; n++)
			autoEffects[n].newNote(newNote, trackNumber);

		note = newNote;
			
		Sample sample = getSample(instrument, note);

		if (sample != null)
		{
			fineTune = sample.getFineTune();
			relativeNote = sample.getRelativeNote();
			this.sample = sample;//sampleData = sample.getData();
			loopType = sample.getLoopType();
			loopStart = sample.getLoopStart();
			loopLength = sample.getLoopLength();
		}
		else
		{
			fineTune = 0;
			relativeNote = 0;
			sample = null;
		}
		sampleOffset = 0;
	}


	/**
	 * What should happen if keep note, but new instrument:
	 * keep note
	 * set instrument
	 * reload sampleData
	 * reset sampleOffset
	 * reset volume
	 * reset panning
	 * reload autoeffects
	 * reset autoeffects
	 * reset loop
	 * reset keyOff
	 */
	private void newInstrument(int newInstrument)
	{
        
        volume = 1;
        panning = 0.5;
        
        return;
/*        effects.newInstrument(newInstrument);
		instrument = newInstrument;
		Sample sample = getSample(instrument, note);
		if (sample != null)
		{
//			this.sample = sample;
			volume = sample.getVolume();
			if (module.getPanningType() == Module.SAMPLE_PANNING)
				panning = sample.getPanning();
			else if (module.getPanningType() == Module.TRACK_PANNING)
				panning = module.getInitialPanning(trackNumber);
			else if (module.getPanningType() == Module.INSTRUMENT_PANNING)
				panning = module.getInstrument(instrument).getPanning();
//			loopType = sample.getLoopType();
//			loopStart = sample.getLoopStart();
//			loopLength = sample.getLoopLength();
		}
		else
		{
			volume = 1;
			panning = 0.5;
		}
		sampleOffset = 0;
        
        Instrument instr = module.getInstrument(newInstrument);
        if (instr != null)
            autoEffects = instr.getAutoEffects();
        
		for (int n = 0; autoEffects != null && n < autoEffects.length; n++)
			autoEffects[n].reset(trackNumber);
            */
	}

	public Sample getSample() {
	    return sample;
    }
    
    public int getInstrument() {
        return instrument;
    }
}
