package anakata.sound.output;

/**
 * A class representing the format of a stream of raw sound data. The sample rate,
 * number of bits per sample and the number of channels can be described.
 * @author torkjel
 *
 */
public class SoundDataFormat
{
	private int bits;
	private int rate;
	private int channels;

	/**
	 * @param bits the number of bits per sample, per channel (often 8 or 16)
	 * @param rate the sample rate (often 44100, 22050 or 11025)
	 * @param channels the number of channels (often 1, for mono, or 2, for stereo)
	 */
	public SoundDataFormat(int bits, int rate, int channels)
	{
		this.bits = bits;
		this.rate = rate;
		this.channels = channels;
	}

	/**
	 * @return the number of bits per sample
	 */
	public int getBits()
	{
		return bits;
	}

	/**
	 * @return the sample rate
	 */
	public int getRate()
	{
		return rate;
	}

	/**
	 * @return the number of channels
	 */
	public int getChannels()
	{
		return channels;
	}

	public String toString() {
	    return this.getClass().getName() + "[" +
            "bits=" + getBits() + ":" +
            "rate=" + getRate() + ":" +
            "channels=" + getChannels() + "]";
    }
}
