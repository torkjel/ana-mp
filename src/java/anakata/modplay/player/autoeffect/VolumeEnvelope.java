package anakata.modplay.player.autoeffect;

import anakata.modplay.player.TrackState;
import anakata.modplay.module.Instrument;

/**
 * an effect controlling the volume of an instrument 
 * @author torkjel
 */
public class VolumeEnvelope implements Envelope
{
	private int sustainPoint;
	private int loopStart;
	private int loopEnd;
	private int type;

	public static final int ON = 1;
	public static final int SUSTAIN = 2;
	public static final int LOOP = 4;

	private boolean on;
	private boolean keyOff[];

	private double[] volumeCurve;

	private int[] tOffset;

	public VolumeEnvelope(
		int[] offset,
		double[] volume,
		int numberOfPoints,
		int sustainPoint,
		int loopStart,
		int loopEnd,
		int type)
	{
		// we don't do zero-length or negative-length loops
		if (loopEnd - loopStart <= 0 && ((type & LOOP) != 0))
			type -= LOOP;

		this.type = type;

		on = true;

		// if off or zero number of points -> make a really short envelope with volume always at max:
		// and clear the on flag;
		if ((type & ON) == 0 || numberOfPoints == 0)
		{
			volumeCurve = new double[] { 1.0 };
			on = false;
		}
		else if (numberOfPoints > 1)
		{
			volumeCurve = new double[offset[numberOfPoints - 1]];
			for (int n = 0; n < numberOfPoints - 1; n++)
				for (int m = offset[n]; m < offset[n + 1]; m++)
					volumeCurve[m] =
						volume[n]
							+ (volume[n + 1] - volume[n])
								* (m - offset[n])
								/ (offset[n + 1] - offset[n]);
		}
		else // if (numberOfPoints == 1)
			volumeCurve = new double[] { volume[0] };

		if ((type & SUSTAIN) != 0)
			this.sustainPoint = offset[sustainPoint];
		if ((type & LOOP) != 0)
		{
			this.loopStart = offset[loopStart];
			if (this.loopStart < 0)
				this.loopStart = 0;
			this.loopEnd = offset[loopEnd];
			if (this.loopEnd >= volumeCurve.length)
				this.loopEnd = volumeCurve.length - 1;
		}
	}

	public void setNumberOfTracks(int tracks)
	{
		tOffset = new int[tracks];
		keyOff = new boolean[tracks];
	}

    public void keyOff(int track) {
        keyOff[track] = true;
    }
    
	public void newNote(double note, int track)
	{
		if (note != Instrument.NO_NOTE)
		{
			tOffset[track] = 0;
			keyOff[track] = false;
		}
	}

	public void doEffect(TrackState state, int track)
	{
		int ofs = tOffset[track];
		// if not on set volume to max and return
		if (!on)
		{
			state.setEnvelopeVolume(volumeCurve[0]);
			return;
		}
		if ((type & LOOP) != 0)
		{
			if (ofs < loopEnd)
				state.setEnvelopeVolume(volumeCurve[ofs]);
			else
			{
				int rofs =
					(ofs - loopStart) % (loopEnd - loopStart) + loopStart;
				state.setEnvelopeVolume(volumeCurve[rofs]);
			}
		}
		else
		{
			if (ofs >= volumeCurve.length)
				state.setEnvelopeVolume(volumeCurve[volumeCurve.length - 1]);
			else
				state.setEnvelopeVolume(volumeCurve[ofs]);
		}
		if ((type & SUSTAIN) == 0 || ofs != sustainPoint || keyOff[track])
			tOffset[track]++;
	}

	public boolean isActive()
	{
		return on;
	}

	public void reset(int track)
	{
		keyOff[track] = false;
		tOffset[track] = 0;
	}

	public void setPosition(int pos, int track)
	{
		if (pos < volumeCurve.length)
			tOffset[track] = pos;
		else
			tOffset[track] = volumeCurve.length - 1;
	}
}
