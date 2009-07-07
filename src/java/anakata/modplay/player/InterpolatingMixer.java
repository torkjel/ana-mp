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
 * This class implements a mixer doing simple linear interpolating
 */
public class InterpolatingMixer implements LowLevelMixer
{
	int v1Index, v2Index, v1, v2, inSizeM1;
	double u;
	int outOffset;
	double inOffset;

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
			v1Index = (int)inOffset;
			v2Index = v1Index + 1;
			v1 = inBuffer[v1Index & inSizeM1];
			v2 = inBuffer[v2Index & inSizeM1];
			u = inOffset - (int)inOffset;
			outBuffer[outOffset++]
				+= (int) ((v1 * (1 - u)) + (v2 * u));
			inOffset += grad;
		}

		inOffsetH[0] = inOffset;
		outOffsetH[0] = outOffset;
	}
}