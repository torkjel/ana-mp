package anakata.modplay.utils;

import anakata.modplay.module.Module;
import anakata.modplay.player.Mixer;
import anakata.modplay.player.ModuleState;
import anakata.modplay.player.PlayerException;

public class ModuleLengthFinder {

	public static final int DEFAULT_MAX_LENGTH = 5 * 60 * 1000;
	public static final int NO_MAX_LENGTH = -1;
	
	public double getLength(Module module) throws PlayerException {
		return getLength(module, DEFAULT_MAX_LENGTH);
	}

	public double getLength(Module module, double maxLength) throws PlayerException {
        int tracks = module.getPatternAtPos(0).getTrackCount();
        Mixer mixer = new NullMixer(tracks);
        ModuleState ms = new ModuleState(module, mixer);
        while (ms.play() && (maxLength == NO_MAX_LENGTH || ms.getPlayTime() < maxLength));
        return ms.getPlayTime();
	}
}
