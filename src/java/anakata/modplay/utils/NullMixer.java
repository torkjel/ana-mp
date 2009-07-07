package anakata.modplay.utils;

import anakata.modplay.player.AbstractMixer;
import anakata.modplay.player.PlayerException;

/**
 * A mixer that doesn't do anything
 * @author torkjel
 */
public class NullMixer extends AbstractMixer {

	public NullMixer(int numberOfTraks) {
		super(numberOfTraks);
	}
	
	public boolean isMute(int track) {
		return false;
	}

	public void play(double millisecs) throws PlayerException {
	}

	public void setMute(int track, boolean mute) {
	}

	public void setTrack(
		short[] sampleData, double offset, double rate, double volume, double panning, 
		int loopType, int loopStart, int loopLength, int track) throws PlayerException {
	}
}
