package anakata.modplay.module;

import anakata.modplay.player.autoeffect.AutoEffect;

/**
 * An instrument. Should be generic enough to support all module formats
 * @author torkjel
 */
public class Instrument
{
	private String name;
	private Sample[] samples;
	private int[] note2sample;
	private AutoEffect[] autoEffects;
	private double panning;

	/** 
	 * note number representing a key-off note
	 * When this note is encountered the previously playing note (if any) 
	 * should (gradually) stop playing. This is controlled by the Fadeout autoeffect
	 */
//	public static final int KEY_OFF = -1;

	/** 
	 * note number representing no spesific note
	 * Playing this note has no effect. Any previously playing note should 
	 * continue playing
	 */
	public static final int NO_NOTE = -2;

	/**
	 * @param name the name of the instrument
	 * @param note2sample for each note it is possible to play 
	 * 	(excluding KEY_OFF and NO_NOTE) this table tells which sample should be 
	 * played. This can be null if this is an empty instrument without any samples
	 * @param samples the samples that correspond to this instrument
	 * @param autoEffects a collection of effects that works on this 
	 * 	instrument each time it is played (panning and volume envelopes, 
	 * 	fadeout etc.) Can be null
	 * @param panning panning for this instrument (ignored by the mixer if 
	 * 	Module.panningType != Module.INSTRUMENT_PANNING) (0.0 - 1.0)
	 */
	public Instrument(
		String name,
		int[] note2sample,
		Sample[] samples,
		AutoEffect[] autoEffects,
		double panning)
	{
		this.name = name;
		this.note2sample = note2sample;
		this.samples = samples;
		this.autoEffects = autoEffects;
		this.panning = panning;
	}

	/**
	 * @return human readable info about this instrument
	 */
	public String getInfo()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Instrument name: ");
		sb.append(name);
		sb.append("\n");
		sb.append("#samples:        ");
		sb.append(getNumberOfSamples());
		sb.append("\n");
		for (int n = 0; n < getNumberOfSamples(); n++)
			sb.append(samples[n].getInfo());
		return sb.toString();
	}

	/**
	 * @return the name of this instrument
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @return the num'th sample of this intrument
	 */
	public Sample getSampleByNum(int num)
	{
		if (num > samples.length || num < 0)
			return null;
		return samples[num];
	}
	/**
	 * @return the sample corresponding to the given note
	 */
	public Sample getSampleByNote(int note)
	{
		if (note == NO_NOTE
			|| note2sample == null
			|| note >= note2sample.length
			|| note < 0)
			return null;
		return samples[note2sample[note]];
	}
	/**
	 * @return the number of samples in this instrument
 	 */
	public int getNumberOfSamples()
	{
		if (samples == null)
			return 0;
		return samples.length;
	}
	/**
	 * @return the number of instrument effects assosiated with this instrument
	 */
	public int getNumberOfAutoEffects()
	{
		if (autoEffects == null)
			return 0;
		return autoEffects.length;
	}
	/**
	 * @return the num'th instrument effect of this intrument
	 */
	public AutoEffect getAutoEffect(int num)
	{
		if (autoEffects == null || num >= autoEffects.length || num < 0)
			return null;
		return autoEffects[num];
	}

	/**
	 * @return all the instrument effects of this effects
	 */
	public AutoEffect[] getAutoEffects()
	{
		return autoEffects;
	}

	/**
	 * @return this instruments panning
	 */
	public double getPanning()
	{
		return panning;
	}
}
