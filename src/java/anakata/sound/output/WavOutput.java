/*
 * Created on 28.apr.03
 */
package anakata.sound.output;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Writes output to a Wav file. Only 44100Hz, 16 bits, stereo supported
 * @author hongve
 *
 */
public class WavOutput implements Output
{
	private int[] supportedRates = { 44100 };
	private int[] supportedBits = { 16 };
	private int[] supportedChannels = { 2 };

	// header fields


    public static final  String RIFF_MAGIC = "RIFF";
    private int length = 0;
	public static final String WAVE_MAGIC = "WAVE";

	public static final  String FORMAT_MAGIC = "fmt ";
	public static final int FORMAT_LENGTH = 0x10;
	public static final short MORE_MAGIC = 0x01;
	private short channels = 2;
	private int rate = INPUT_RATE;

	// argh!!! 
	// bytes per sample is for both channels : 4 bytes
	// bits per sample is for each channel : 16 bits
	private short bytes_per_sample = 4;
	private int bytes_per_sec = rate * bytes_per_sample;
	private short bits_per_sample = (short)((bytes_per_sample/2) * 8);
	
	public static final String DATA_MAGIC = "data";
	private int data_length = 0;

	// end header fields

	private RandomAccessFile out;
	private String filename;

	public WavOutput(String filename, SoundDataFormat format) throws IOException
	{
        if (!supports(format)) 
            throw new IOException("Format now supported: " + format.toString());
		this.filename = filename;
	}

	public boolean supports(SoundDataFormat format)
	{
		boolean rateSupported = false;
		int rate = format.getRate();
		for (int n = 0; n < supportedRates.length; n++)
			if (supportedRates[n] == rate)
				rateSupported = true;

		boolean bitsSupported = false;
		int bits = format.getBits();
		for (int n = 0; n < supportedBits.length; n++)
			if (supportedBits[n] == bits)
				bitsSupported = true;

		boolean channelsSupported = false;
		int channels = format.getChannels();
		for (int n = 0; n < supportedChannels.length; n++)
			if (supportedChannels[n] == channels)
				channelsSupported = true;

		return rateSupported && bitsSupported && channelsSupported;
	}


	/**
	 * @see anakata.sound.output.Output#isOpen()
	 */
	public boolean isOpen()
	{
		return out != null;
	}

	/**
	 * @see anakata.sound.output.Output#open()
	 */
	public boolean open()
	{
		boolean res = true;
		try
		{
			out = new RandomAccessFile(filename, "rw");

			out.seek(0);
			out.write(RIFF_MAGIC.getBytes());
			writeInt(length);
			out.write(WAVE_MAGIC.getBytes());
			out.write(FORMAT_MAGIC.getBytes());
			writeInt(FORMAT_LENGTH);
			writeShort(MORE_MAGIC);
			writeShort(channels);
			writeInt(rate);
			writeInt(bytes_per_sec);
			writeShort(bytes_per_sample);
			writeShort(bits_per_sample);
			out.write(DATA_MAGIC.getBytes());
			writeInt(data_length);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			res = false;
		}
		return res;
	}


	/**
	 * writes an int
	 * @param val
	 */
	private void writeInt(int val) throws IOException
	{
		out.write((val & 0x0000000ff) >>> 0);
		out.write((val & 0x00000ff00) >>> 8);
		out.write((val & 0x000ff0000) >>>16);
		out.write((val & 0x0ff000000) >>>24);
	}

	/**
	 * writes a short
	 * @param val
	 */
	private void writeShort(short val) throws IOException
	{
		out.write((val & 0x000ff) >>> 0);
		out.write((val & 0x0ff00) >>> 8);
	}

	/**
	 * @see anakata.sound.output.Output#close()
	 */
	public boolean close()
	{
		boolean res = true;
		try
		{
			out.seek(4);
			int length = 4+24+8+data_length;
			writeInt(length);
			out.seek(12+24+4);
			writeInt(data_length);
			out.setLength(8+4+24+8+data_length);
			out.close();
			out = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			res = false;
		}
		return res;
	}

	/**
	 * @see anakata.sound.output.Output#write(byte[], int, int)
	 */
	public int write(byte[] data, int ofs, int len) throws IOException
	{
		data_length += len;
		out.write(data,ofs,len);
		return data.length;
	}

}
