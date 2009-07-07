/*
 * Created on Apr 28, 2003
 */
package anakata.modplay.player;

/**
 * @author torkjel
 * Innterface for low level mixers.
 */
public interface LowLevelMixer
{
	/**
	 * Resamples the data in inBuffer, placing the result in outBuffer.
	 *
	 * @param outBuffer is the output buffer
	 * @param outOffsetH outOffset[0] is where the mixer should start writing in outBuffer
	 * @param outLength s how far the mixer should fill outBuffer whith data
	 * @param inBuffer is a circular buffer containing raw data 
	 * @param inOffsetH (outOffset[0] & (inSize-1)) is where the mixer should start to read data from inBuffer
	 * @param inLength the mixer should never read inBuffer further than (inLength & (inSize-1))
	 * @param inSize is the size of inBuffer
	 * @param grad is the realtionship between the samplig rates of the data in inBuffer an outBuffer
	 * 
	 * It IS legal to read data from inBuffer before inOffset[0] and it IS legal 
	 * to not use all the data in inBuffer. This way higher order interpolating can
	 * be implemented.<p>
	 * On return, outOffsetH[0] must contain the index of the last mixed sample 
	 * pluss one in outBuffer and inOffsetH[0] must contain the last mixed sample
	 * pluss one in inBuffer.
	 */
	public void mix(
		int[] outBuffer,
		int[] outOffsetH,
		int outLength,
		short[] inBuffer,
		double[] inOffsetH,
		int inLength,
		int inSize,
		double grad);
}
