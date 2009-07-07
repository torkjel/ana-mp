package anakata.modplay.player.autoeffect;

import anakata.modplay.player.TrackState;
import anakata.modplay.module.Instrument;

/**
 * an effect controlling the panning of an instrument
 * @author torkjel
*/
public class PanningEnvelope implements Envelope
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

	private double[] panningCurve;

	private int[] tOffset;

	public PanningEnvelope(
		int[] offset,
		double[] panning,
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

		// if off or zero number of points -> make a really short envelope 
		// with panning always at center:
		// and clear the on flag;
		if ((type & ON) == 0 || numberOfPoints == 0)
		{
			panningCurve = new double[] { 0.5 };
			on = false;
		}
		else if (numberOfPoints > 1)
		{
			panningCurve = new double[offset[numberOfPoints - 1]];
			for (int n = 0; n < numberOfPoints - 1; n++)
				for (int m = offset[n]; m < offset[n + 1]; m++)
					panningCurve[m] =
						panning[n]
							+ (panning[n + 1] - panning[n])
								* (m - offset[n])
								/ (offset[n + 1] - offset[n]);
		}
		else // if (numberOfPoints == 1)
			panningCurve = new double[] { panning[0] };

		if ((type & SUSTAIN) != 0)
			this.sustainPoint = offset[sustainPoint];
		if ((type & LOOP) != 0)
		{
            this.loopStart = offset[loopStart];
			if (this.loopStart < 0)
				this.loopStart = 0;
			this.loopEnd = offset[loopEnd];
			if (this.loopEnd > panningCurve.length)
				this.loopEnd = panningCurve.length;
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
			state.setEnvelopePanning(panningCurve[0]);
			return;
		}
		if ((type & LOOP) != 0)
		{
			if (ofs < loopEnd)
				state.setEnvelopePanning(panningCurve[ofs]);
			else
			{
				int rofs = 
                    (ofs - loopStart) % (loopEnd - loopStart) + loopStart;
				state.setEnvelopePanning(panningCurve[rofs]);
			}
		}
		else
		{
			if (ofs >= panningCurve.length)
				state.setEnvelopePanning(panningCurve[panningCurve.length - 1]);
			else
				state.setEnvelopePanning(panningCurve[ofs]);
		}
		if ((type & SUSTAIN) == 0 || ofs != sustainPoint || keyOff[track])
			tOffset[track]++;
	}

	public void reset(int track)
	{
		keyOff[track] = false;
		tOffset[track] = 0;
	}

	public void setPosition(int pos, int track)
	{
		if (pos < panningCurve.length)
			tOffset[track] = pos;
		else
			tOffset[track] = panningCurve.length - 1;
	}
}
