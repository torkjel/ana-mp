package anakata.modplay.module;

import anakata.util.Util;

/**
 * A track is a column in a pattern.
 * @author torkjel
 */
public class Track
{
	private int[] instruments;
	private int[]  notes;
	private int[][] effects;
	private int[][] effectArg1;
	private int[][] effectArg2;

	/**
	 * a constant that indicates that no new intrument shall be played in a track
	 */
	public static final int NO_INSTRUMENT = -1;

	/**
	 * creates an empty track
	 * @param divisions number of divisions in the track
	 */
	public Track(int divisions)
	{
		instruments = new int[divisions];
		notes = new int[divisions];
		effects = new int[divisions][];
		effectArg1 = new int[divisions][];
		effectArg2 = new int[divisions][];
	}

	/**
	 * initializes a division int the track. Each division can have several 
	 * effects.
	 * It is unsafe to not initialize all the divisions in the track!
	 * @param division the division to be initialized
	 * @param instrumentNumber the number of the instrument to be played at 
	 *  this division in the track (or NO_INSTRUMENT)
	 * @param note the note to be played
	 * @param effects the effects to be played (can be null)
	 * @param effectArg1 the first argument to the effects (can be null)
	 * @param effectArg2 the second argument to the effects (can be null)
	 */
	public void initDivision(
		int division,
		int instrumentNumber,
		int note,
		int[] effects,
		int[] effectArg1,
		int[] effectArg2)
	{
		instruments[division] = instrumentNumber;
		notes[division] = note;
		this.effects[division] = effects;
		this.effectArg1[division] = effectArg1;
		this.effectArg2[division] = effectArg2;
	}

	/**
	 * @return the number of the instrument to be played at the div'th division in 
	 *  this track
	 */
	public int getInstrumentNumber(int div)
	{
		return instruments[div];
	}

	/**
	 * @return the note to be played in the div'th division.
	 */
	public int getNote(int div)
	{
		return notes[div];
	}

	/**
	 * @return the number of effects to be played at division div
	 */
	public int getNumberOfEffects(int div)
	{
		if (effects[div] != null)
			return effects[div].length;
		else
			return 0;
	}
	
	/**
	 * @param div
	 * @param n
	 * @return the n'th effect at the div'th division
	 */
	public int getEffect(int div, int n)
	{
		return effects[div][n];
	}

	/**
	 * @param div
	 * @param n
	 * @return the 1st argument to the n'th effect at the div'th division
	 */
	public int getEffectArg1(int div, int n)
	{
		return effectArg1[div][n];
	}

	/**
	 * @param div
	 * @param n
	 * @return the 2nd argument to the n'th effect at the div'th division
	 */
	public int getEffectArg2(int div, int n)
	{
		return effectArg2[div][n];
	}

	/**
	 * @return information about the div'th division of this track in 
	 *  "human readable" form :) 
	 */
	public String getInfo(int div)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getNoteSymbol(div) + " ");
		sb.append(getInstrumentSymbol(div) + " ");
		for (int n = 0; n < getNumberOfEffects(div); n++)
			sb.append(getEffectSymbol(div, n) + " ");
		for (int n = getNumberOfEffects(div); n < 2; n++)
			sb.append("--- ");
		return sb.toString();
	}

	/**
	 * The note symbol consist of a note and a octave. The note C in the 2nd 
	 * octave is represented as "C-2". The key off note is "###" and no note is "---".
	 * @param div
	 * @return the note symbol of the note in the div'th division
	 */
	private String getNoteSymbol(int div)
	{
		int note = notes[div];
//		if (note == Instrument.KEY_OFF)
//			return "###";
//		else
        if (note == Instrument.NO_NOTE)
			return "---";
		int noteNum = note % 12;
		int period = note / 12;
		String code = null;
		switch (noteNum)
		{
			case 0 :
				code = "C-";
				break;
			case 1 :
				code = "C#";
				break;
			case 2 :
				code = "D-";
				break;
			case 3 :
				code = "D#";
				break;
			case 4 :
				code = "E-";
				break;
			case 5 :
				code = "F-";
				break;
			case 6 :
				code = "F#";
				break;
			case 7 :
				code = "G-";
				break;
			case 8 :
				code = "G#";
				break;
			case 9 :
				code = "A-";
				break;
			case 10 :
				code = "A#";
				break;
			case 11 :
				code = "B-";
				break;
			default :
				code = "XX";
		}
		code += period;
		return code;
	}

	/**
	 * @param div
	 * @return a two digit hex number representing the instrument played in the 
	 *  div'th division.
	 */
	private String getInstrumentSymbol(int div)
	{
		return Util.nibbleToHex((instruments[div]) >> 4)
			+ Util.nibbleToHex(instruments[div]);
	}

	/**
	 * @param div
	 * @param n
	 * @return a tree digit hex number representing the n'th effect played in the
	 *  div'th division.
	 */
	private String getEffectSymbol(int div, int n)
	{
		return Util.nibbleToHex(effects[div][n]) +
		// Util.nibbleToHex(effects[div][n] >> 4) +  // effect > 0x0f isn't shown right...
		Util.nibbleToHex(effectArg1[div][n])
			+ Util.nibbleToHex(effectArg2[div][n]);
	}
}
