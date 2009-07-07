/*
 * Created on Apr 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package anakata.modplay.player;

/**
 * @author torkjel
 *
 * This class implements a mixer doing quick and dirty mixing
 */
public class FastMixer implements LowLevelMixer
{
	int outOffset;
	double inOffset;
	int inSizeM1;

	public void mix(
		int[] outBuffer,
		int[] outOffsetH,
		int outLength,
		short[] inBuffer,
		double[] inOffsetH,
		int inLength,
		int inSize,
		double grad)
	{
		outOffset = outOffsetH[0];
		inOffset = inOffsetH[0];
		inSizeM1 = inSize-1;

		while (outOffset < outLength && inLength - inOffset > 16)
		{
			outBuffer[outOffset++] += inBuffer[(int)inOffset & inSizeM1];
			inOffset += grad;
		}

		inOffsetH[0] = inOffset;
		outOffsetH[0] = outOffset; 
	}

}
