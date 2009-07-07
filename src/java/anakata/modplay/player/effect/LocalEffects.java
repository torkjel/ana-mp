package anakata.modplay.player.effect;

import anakata.modplay.player.*;
import anakata.modplay.player.autoeffect.AutoEffect;
import anakata.modplay.module.*;
import anakata.util.Logger;

/**
 * Local effects are effects that only effects the track they are invoked in
 * @author torkjel
 *
 */
public class LocalEffects implements Effect
{

    /* -- volume control variables -- */

    public double tremoloValue;
	public double volumeSlide;
    private double volumeSlideSpeed;
    private double volumeFineSlideSpeed;

    private int tremoloWaveform;
    private boolean tremoloRetrigger;
    
    
    /* -- panning control variables -- */

    /** Panning slide, accumulator */
    public double panningSlide;
    /** Panning side, speed*/
    private double panningSlideSpeed;

    /* -- note control variables -- */
    
    public double noteTune;
    public double noteSlide;
    
    private double noteSlideSmooth;

    /** Slide to note, destination note */
    private int noteSlideDest;
    /** Slide to note, slide speed */
    private int noteSlideToSpeed;

    private double noteSlideSpeed;
    private double noteFineSlideSpeed;
    private double noteExtraFineSlideSpeed;


    
    private int vibratoWaveform;
	private boolean vibratoRetrigger;

	private boolean glissando;

	private int retriggerInterval;
	private int retriggerVolumeChange;

    public static final int SINE_TREMOLO = 0;
    public static final int SAWTOOTH_TREMOLO = 1;
    public static final int SQUARE_TREMOLO = 2;

    private double tremoloPeriod;
    private double tremoloAmplitude;
    private int tremoloTick;

    public static final int SINE_VIBRATO = 0;
    public static final int SAWTOOTH_VIBRATO = 1;
    public static final int SQUARE_VIBRATO = 2;

    private double vibratoPeriod;
    private double vibratoAmplitude;
    private int vibratoTick;


    
    public LocalEffects()
	{
		vibratoWaveform = SINE_VIBRATO;
		tremoloWaveform = SINE_TREMOLO;
		vibratoRetrigger = true;
		tremoloRetrigger = true;
		glissando = false;
		panningSlide = 0;
		retriggerInterval = 0;
		retriggerVolumeChange = 0;
		reset();
	}

    /**
	 * do whatever needs to be done before the track is played
	 * @param state
	 * @param track
	 * @param pattern
	 * @param division
	 * @param tick
	 * @param effectNumber
	 * @param arg1
	 * @param arg2
	 */
	public void preEffect(
		TrackState state,
		int track,
		int pattern,
		int division,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		switch (effectNumber)
		{
			case MOD_EXTENDED_DELAY_SAMPLE :
				modExtendedDelaySample(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
		}
	}

	/**
	 * do the effect
	 * @param state
	 * @param track
	 * @param pattern
	 * @param division
	 * @param tick
	 * @param effectNumber
	 * @param arg1
	 * @param arg2
	 */
	public void doEffect(
		TrackState state,
		int track,
		int pattern,
		int division,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		noteTune = 0;
		tremoloValue = 0;

        switch (effectNumber)
		{
			case MOD_ARPEGGIO :
				modArpeggio(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SLIDE_UP :
				modSlideUp(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SLIDE_DOWN :
				modSlideDown(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SLIDE_TO_NOTE :
				modSlideToNote(
					state,
					track,
					pattern,
					division,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_VIBRATO :
				modVibrato(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SLIDE_TO_NOTE_AND_VOLUME_SLIDE :
				modSlideToNote(
					state,
					track,
					pattern,
					division,
					tick,
					effectNumber,
					0,
					0);
				modVolumeSlide(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_VIBRATO_AND_VOLUME_SLIDE :
				modVibrato(state, track, tick, effectNumber, 0, 0);
				modVolumeSlide(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_TREMOLO :
				modTremolo(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_PANNING :
				modPanning(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SET_SAMPLE_OFFSET :
				modSetSampleOffset(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_VOLUME_SLIDE :
				modVolumeSlide(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_SET_VOLUME :
				modSetVolume(state, track, tick, effectNumber, arg1, arg2);
				break;
			case MOD_EXTENDED_FINE_SLIDE_UP :
				modExtendedFineSlideUp(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_FINE_SLIDE_DOWN :
				modExtendedFineSlideDown(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_SET_GLISSANDO :
				if (arg2 == 1)
					glissando = true;
				else if (arg2 == 0)
					glissando = false;
				break;
			case MOD_EXTENDED_SET_VIBRATO_WAVEFORM :
				modExtendedSetVibratoWaveform(arg2);
				break;
			case MOD_EXTENDED_FINETUNE :
				modExtendedFineTune(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_SET_TREMOLO_WAVEFORM :
				modExtendedSetTremoloWaveform(arg2);
				break;
			case MOD_EXTENDED_ROUGH_PANNING :
				modExtendedRoughPanning(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_RETRIGGER_SAMPLE :
				modExtendedRetriggerSample(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2,
                    pattern);
				break;
			case MOD_EXTENDED_FINE_VOLUME_SLIDE_UP :
				modExtendedFineVolumeSlideUp(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_FINE_VOLUME_SLIDE_DOWN :
				modExtendedFineVolumeSlideDown(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_CUT_SAMPLE :
				modExtendedCutSample(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case MOD_EXTENDED_INVERT_LOOP :
				Logger.warning("Invert loop not supported!");
				break;

			case XM_SLIDE_UP :
				xmSlideUp(state, track, tick, effectNumber, arg1, arg2);
				break;
			case XM_SLIDE_DOWN :
				xmSlideDown(state, track, tick, effectNumber, arg1, arg2);
				break;
			case XM_SLIDE_TO_NOTE :
				xmSlideToNote(
					state,
					track,
					pattern,
					division,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_VOLUME_SLIDE :
				xmVolumeSlide(state, track, tick, effectNumber, arg1, arg2);
				break;
			case XM_EXTENDED_FINE_SLIDE_UP :
				xmExtendedFineSlideUp(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_EXTENDED_FINE_SLIDE_DOWN :
				xmExtendedFineSlideDown(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_EXTENDED_FINE_VOLUME_SLIDE_UP :
				xmExtendedFineVolumeSlideUp(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_EXTENDED_FINE_VOLUME_SLIDE_DOWN :
				xmExtendedFineVolumeSlideDown(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;

			case XM_SET_ENVELOPE_POSITION :
				if (tick == 0)
					state.setEnvelopePosition(arg1 * 16 + arg2);
				break;
			case XM_PANNING_SLIDE :
				xmPanningSlide(state, track, tick, effectNumber, arg1, arg2);
				break;
			case XM_MULTI_RETRIGGER_NOTE :
				xmMultiRetriggerNote(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_EXTRA_FINE_SLIDE_UP :
				xmExtraFineSlideUp(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
			case XM_EXTRA_FINE_SLIDE_DOWN :
				xmExtraFineSlideDown(
					state,
					track,
					tick,
					effectNumber,
					arg1,
					arg2);
				break;
				
            case XM_KEY_OFF :
                keyOff(state,track,tick);
                break;

            case S3M_TREMOR :
				s3mTremor(state, track, tick, effectNumber, arg1, arg2);
				break;

		}
	}

	/**
	 * do whatever needs to be done after the tick is played
	 * @param state
	 * @param track
	 * @param pattern
	 * @param division
	 * @param tick
	 * @param effectNumber
	 * @param arg1
	 * @param arg2
	 */
	public void postEffect(
		TrackState state,
		int track,
		int pattern,
		int division,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		switch (effectNumber)
		{
			case S3M_TREMOR :
				s3mTremorStop(state, track, tick, effectNumber, arg1, arg2);
				break;
		}
    }

	public void newNote(int note)
	{
//		if (note != Instrument.KEY_OFF)
			reset();
	}

	public void newInstrument(int instrument)
	{}

	public void newNoteAndInstrument(int note, int instrument)
	{
		reset();
	}
	public void reset()
	{
		if (vibratoRetrigger)
			vibratoTick = 0;
		if (tremoloRetrigger)
			tremoloTick = 0;

		noteTune = 0;
		noteSlide = 0;
		noteSlideSmooth = 0;
		volumeSlide = 0;
		tremoloValue = 0;
		panningSlide = 0;
	}

	private void modArpeggio(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (arg1 == 0 && arg2 == 0)
			return;
		tick %= 3;
		if (tick == 0); // do nothing...
		else if (tick == 1)
			noteTune = noteTune + arg1;
		else if (tick == 2)
			noteTune = noteTune + arg2;
	}

	private void modSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick > 0)
		{
			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) - (arg1 * 16 + arg2);
			if (period < minPeriod)
				period = minPeriod;
			double newNote = mu.period2note(period);
			if (newNote > maxNote)
				newNote = maxNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void modSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick > 0)
		{		    
            ModuleUnits mu = sample.getUnits();
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) + (arg1 * 16 + arg2);
			if (period > maxPeriod)
				period = maxPeriod;
			double newNote = mu.period2note(period);
			if (newNote < minNote)
				newNote = minNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void modSlideToNote(
		TrackState state,
		int track,
		int pattern,
		int division,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{

        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			int note =
				(int)state.getModule().getPatternAtPos(pattern).getTrack(
					track).getNote(
					division);
			if (note != Instrument.NO_NOTE/* && note != Instrument.KEY_OFF*/)
			{
				noteSlideDest = note;
			}
			if (arg1 * 16 + arg2 != 0)
				noteSlideToSpeed = arg1 * 16 + arg2;
		}
		else
		{
			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double newNote, note;
			newNote = note = state.getNote() + noteSlideSmooth;
			if (noteSlideDest > note)
			{
				double period = mu.note2period(note) - noteSlideToSpeed;
				if (period < minPeriod)
					period = minPeriod;
				newNote = mu.period2note(period);
				if (newNote > noteSlideDest)
					newNote = noteSlideDest;
			}
			else if (noteSlideDest < note)
			{
				double period = mu.note2period(note) + noteSlideToSpeed;
				if (period > maxPeriod)
					period = maxPeriod;
				newNote = mu.period2note(period);
				if (noteSlideDest > newNote)
					newNote = noteSlideDest;
			}
			noteSlideSmooth += newNote - note;
			if (glissando)
				noteSlide = Math.round(noteSlideSmooth);
			else
				noteSlide = noteSlideSmooth;
		}
	}

	private void modVibrato(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg1 != 0)
				vibratoPeriod = arg1;
			if (arg2 != 0)
				vibratoAmplitude = arg2;
		}

		double dNote =
			getVibratoLevel(
				vibratoPeriod,
				vibratoAmplitude,
				vibratoTick,
				vibratoWaveform);
		noteTune = dNote;

		vibratoTick++;
	}

	private double getVibratoLevel(
		double period,
		double amplitude,
		int vTick,
		int type)
	{
		double level = 0;
		switch (type)
		{
			case SINE_VIBRATO :
				// ------------------------
				//      f = arg1 * ticks / 64 [periods/division] -> 64 / (arg1 * ticks) [divisions/period] = 64 / arg1 [ticks/period] = p
				//      amp * sin(tick * 2 * pi / p) = arg2/16 * sin(tick * 2 * pi * arg1 / 64);
				// ------------------------
				level = Math.sin(vTick * 2 * 3.1416 * period / 64);
				break;
			case SQUARE_VIBRATO :
				level =
					(((int) (16 * vTick * period / 64) % 16) < 16 / 2) ? 1 : -1;
				break;
			case SAWTOOTH_VIBRATO :
				int v = (int) (16 * vTick * period / 64) % 16;
				double v2 = 1 - (v / 8.0);
				level = v2;
				break;
		}
		level *= amplitude / 16.0;
		return level;
	}


	public void modTremolo(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg1 != 0)
				tremoloPeriod = arg1;
			if (arg2 != 0)
				tremoloAmplitude = arg2;
		}

		double dVolume =
			getTremoloLevel(tremoloPeriod, tremoloTick, tremoloWaveform);
		tremoloValue =
			dVolume
				* tremoloAmplitude
				* (state.getModuleState().getTicksInDivision() - 1)
				/ 64.0;

		tremoloTick++;
	}

	private double getTremoloLevel(double period, int tTick, int type)
	{
		double level = 0;
		switch (type)
		{
			case SINE_TREMOLO :
				level = Math.sin(tTick * 2 * 3.1416 * period / 64);
				break;
			case SQUARE_TREMOLO :
				level =
					(((int) (16 * tTick * period / 64) % 16) < 16 / 2) ? 1 : -1;
				break;
			case SAWTOOTH_TREMOLO :
				int v = (int) (16 * tTick * period / 64) % 16;
				double v2 = 1 - (v / 8.0);
				level = v2;
				break;
		}
		return level;
	}

	private void modPanning(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			state.setPanning((arg1 * 16 + arg2) / 256.0);
	}

	private void modSetSampleOffset(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			state.setSampleOffset(arg1 * 4096 + arg2 * 256);
	}

    private void modVolumeSlide(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick > 0)
		{
			if (arg1 != 0)
				volumeSlide += arg1 / 64.0;
			else if (arg2 != 0)
				volumeSlide -= arg2 / 64.0;
		}
	}

    private void modSetVolume(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			state.setVolume((arg1 * 16 + arg2) / 64.0);
	}

	private void modExtendedFineSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

        if (tick == 0)
		{
			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) - arg2;
			if (period < minPeriod)
				period = minPeriod;
			double newNote = mu.period2note(period);
			if (newNote > maxNote)
				newNote = maxNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}
	private void modExtendedFineSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			ModuleUnits mu = sample.getUnits();
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) + arg2;
			if (period > maxPeriod)
				period = maxPeriod;
			double newNote = mu.period2note(period);
			if (newNote < minNote)
				newNote = minNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void modExtendedFineTune(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if ((arg2 & 8) != 0)
				arg2 |= 0x0fffffff0; // sign extend
			state.setFineTune(arg2 / 16.0);
		}
	}

	private void modExtendedSetTremoloWaveform(int arg2)
	{
		if (arg2 > 3)
		{
			tremoloRetrigger = true;
			arg2 -= 4;
		}
		else
			tremoloRetrigger = false;
		if (arg2 == 3)
			arg2 = (int) (Math.random() * 3);
		tremoloWaveform = arg2;
	}

	private void modExtendedSetVibratoWaveform(int arg2)
	{
		if (arg2 > 3)
		{
			vibratoRetrigger = true;
			arg2 -= 4;
		}
		else
			vibratoRetrigger = false;
		if (arg2 == 3)
			arg2 = (int) (Math.random() * 3);
		vibratoWaveform = arg2;
	}

	private void modExtendedRoughPanning(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		state.setPanning(arg2 / 16.0);
	}

	private void modExtendedRetriggerSample(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2,
        int pattern)
	{
		if ((arg2 == 0 && tick == 0) || (arg2 != 0 && tick % arg2 == 0))
            state.setSampleOffset(0);
	}

	private void modExtendedFineVolumeSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			volumeSlide += arg2 / 64.0;
	}
	private void modExtendedFineVolumeSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			volumeSlide -= arg2 / 64.0;
	}

	private void modExtendedCutSample(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick >= arg2)
			state.setVolume(0);
	}

	private void modExtendedDelaySample(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
			state.setSampleDelay(arg2);
	}

	private void xmSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{

        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0 && !(arg1 == 0 && arg2 == 0))
			noteSlideSpeed = (arg1 * 16 + arg2) * 4;
		if (tick > 0)
		{
			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) - noteSlideSpeed;
			if (period < minPeriod)
				period = minPeriod;
			double newNote = mu.period2note(period);
			if (newNote > maxNote)
				newNote = maxNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}
	private void xmSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0 && !(arg1 == 0 && arg2 == 0))
			noteSlideSpeed = (arg1 * 16 + arg2) * 4;
		if (tick > 0)
		{
			ModuleUnits mu = sample.getUnits();
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) + noteSlideSpeed;
			if (period > maxPeriod)
				period = maxPeriod;
			double newNote = mu.period2note(period);
			if (newNote < minNote)
				newNote = minNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void xmSlideToNote(
		TrackState state,
		int track,
		int pattern,
		int division,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

        if (tick == 0)
		{
			int note =
				(int)state.getModule().getPatternAtPos(pattern).getTrack(
					track).getNote(
					division);
			if (note != Instrument.NO_NOTE/* && note != Instrument.KEY_OFF*/)
			{
				noteSlideDest = note;
			}
			if (arg1 * 16 + arg2 != 0)
				noteSlideToSpeed = arg1 * 16 + arg2;
		}
		else
		{
            ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double newNote, note;
			newNote = note = state.getNote() + noteSlideSmooth;
			if (noteSlideDest > note)
			{
				double period = mu.note2period(note) - noteSlideToSpeed * 4;
				if (period < minPeriod)
					period = minPeriod;
				newNote = mu.period2note(period);
				if (newNote > noteSlideDest)
					newNote = noteSlideDest;
			}
			else if (noteSlideDest < note)
			{
				double period = mu.note2period(note) + noteSlideToSpeed * 4;
				if (period > maxPeriod)
					period = maxPeriod;
				newNote = mu.period2note(period);
				if (noteSlideDest > newNote)
					newNote = noteSlideDest;
			}
			noteSlideSmooth += newNote - note;
			if (glissando)
				noteSlide = Math.round(noteSlideSmooth);
			else
				noteSlide = noteSlideSmooth;
		}
	}

	private void xmVolumeSlide(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg1 != 0)
				volumeSlideSpeed = arg1 / 64.0;
			else if (arg2 != 0)
				volumeSlideSpeed = -arg2 / 64.0;
		}
		if (tick > 0)
			volumeSlide += volumeSlideSpeed;
	}

	private void xmExtendedFineSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			if (arg2 != 0)
				noteFineSlideSpeed = arg2 * 4;

			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) - noteFineSlideSpeed;
			if (period < minPeriod)
				period = minPeriod;
			double newNote = mu.period2note(period);
			if (newNote > maxNote)
				newNote = maxNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}
	private void xmExtendedFineSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			if (arg2 != 0)
				noteFineSlideSpeed = arg2 * 4;

			ModuleUnits mu = sample.getUnits();
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) + noteFineSlideSpeed;
			if (period > maxPeriod)
				period = maxPeriod;
			double newNote = mu.period2note(period);
			if (newNote < minNote)
				newNote = minNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void xmExtendedFineVolumeSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg2 != 0)
				volumeFineSlideSpeed = arg2 / 64.0;
			volumeSlide += volumeFineSlideSpeed;
		}
	}
	private void xmExtendedFineVolumeSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg2 != 0)
				volumeFineSlideSpeed = arg2 / 64.0;
			volumeSlide -= volumeFineSlideSpeed;
		}
	}

	private void xmPanningSlide(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == 0)
		{
			if (arg1 != 0)
				panningSlideSpeed = arg1 / 256.0;
			else if (arg2 != 0)
				panningSlideSpeed = -arg2 / 256.0;
		}
		if (tick > 0)
			panningSlide += panningSlideSpeed;
	}

	private void xmMultiRetriggerNote(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{

		if (tick == 0)
		{
			if (arg1 != 0)
				retriggerVolumeChange = arg1;
			if (arg2 != 0)
				retriggerInterval = arg2;
		}
		// test for retriggerInterval == 0 incase retrigger is used without first 
		// being initialized 
		if (retriggerInterval != 0 && tick % retriggerInterval == 0 && tick != 0)
		{
			state.setSampleOffset(0);
			switch (retriggerVolumeChange)
			{
				case 0 :
					//			state.setVolume(0);
					break;
				case 1 :
					state.setVolume(state.getVolume() - 1 / 64.0);
					break;
				case 2 :
					state.setVolume(state.getVolume() - 2 / 64.0);
					break;
				case 3 :
					state.setVolume(state.getVolume() - 4 / 64.0);
					break;
				case 4 :
					state.setVolume(state.getVolume() - 8 / 64.0);
					break;
				case 5 :
					state.setVolume(state.getVolume() - 16 / 64.0);
					break;
				case 6 :
					state.setVolume(state.getVolume() * 2 / 3);
					break;
				case 7 :
					state.setVolume(state.getVolume() * 2);
					break;
				case 9 :
					state.setVolume(state.getVolume() + 1 / 64.0);
					break;
				case 10 :
					state.setVolume(state.getVolume() + 2 / 64.0);
					break;
				case 11 :
					state.setVolume(state.getVolume() + 4 / 64.0);
					break;
				case 12 :
					state.setVolume(state.getVolume() + 8 / 64.0);
					break;
				case 13 :
					state.setVolume(state.getVolume() + 16 / 64.0);
					break;
				case 14 :
					state.setVolume(state.getVolume() * 3 / 2);
					break;
				case 15 :
					state.setVolume(state.getVolume() * 2);
					break;
			}
		}
	}

	private void xmExtraFineSlideUp(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			if (arg2 != 0)
				noteExtraFineSlideSpeed = arg2;

			ModuleUnits mu = sample.getUnits();
			double maxNote = mu.getUpperNoteLimit();
			double minPeriod = mu.note2period(maxNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) - noteExtraFineSlideSpeed;
			if (period < minPeriod)
				period = minPeriod;
			double newNote = mu.period2note(period);
			if (newNote > maxNote)
				newNote = maxNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void xmExtraFineSlideDown(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
        Sample sample = state.getSample();
        if (sample == null) return;

		if (tick == 0)
		{
			if (arg2 != 0)
				noteExtraFineSlideSpeed = arg2;

			ModuleUnits mu = sample.getUnits();
			double minNote = mu.getLowerNoteLimit();
			double maxPeriod = mu.note2period(minNote);

			double note = state.getNote() + noteSlideSmooth;
			if (note == Instrument.NO_NOTE)
				return;
			double period = mu.note2period(note) + noteExtraFineSlideSpeed;
			if (period > maxPeriod)
				period = maxPeriod;
			double newNote = mu.period2note(period);
			if (newNote < minNote)
				newNote = minNote;
			noteSlide = noteSlideSmooth += newNote - note;
		}
	}

	private void s3mTremor(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick < arg1 * 16 + arg2)
			volumeSlide -= 5; //guarantied to give silence..
	}
	private void s3mTremorStop(
		TrackState state,
		int track,
		int tick,
		int effectNumber,
		int arg1,
		int arg2)
	{
		if (tick == arg1 * 16 + arg2
			|| arg1 * 16 + arg2 >= state.getModuleState().getTicksInDivision())
			volumeSlide += 5;
	}

	public void keyOff(TrackState state, int track, int tick) {
        if (tick != 0) return;
        int instr = state.getInstrument();
        if (instr == Track.NO_INSTRUMENT) return;
	    Instrument i = state.getModule().getInstrument(instr);
        AutoEffect[] af = i.getAutoEffects();
        for (int n = 0; af != null && n < af.length; n++) {
            af[n].keyOff(track);
        }
    }
}
