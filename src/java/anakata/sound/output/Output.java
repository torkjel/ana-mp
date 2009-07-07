/*
 * Created on Apr 27, 2003
 *
 */
package anakata.sound.output;

import java.io.IOException;

/**
 * @author torkjel
 *
 */
public interface Output
{
	/**
	 * the assumed sampeling rate of the input to write(byte[], int, int)
	 */
	public static final int INPUT_RATE = 44100; 


	/**
	 * tests if an output plugin is open for writeing
	 * @return true if the Output is open, false else 
	 */
	public boolean isOpen();

	/**
	 * opens the output plugin for writing
	 * @return true if the plugin was successfully opened, false else
	 */
	public boolean open();

	/**
	 * closes the output plugin
	 * @return true if the plugin was successfully closed, false else
	 */
	public boolean close();

	/**
	 * try to write len bytes from offset ofs from the array data.
	 * @param data the array containing data to write
	 * @param ofs the offset in data to write from
	 * @param len the number of samples to write
	 * @return the number of samples actually written
	 */
	public int write(byte[] data, int ofs, int len) throws IOException;
}
