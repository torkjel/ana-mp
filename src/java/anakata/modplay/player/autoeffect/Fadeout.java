package anakata.modplay.player.autoeffect;

import anakata.modplay.module.Instrument;
import anakata.modplay.player.TrackState;

/**
 * an effect controlling how an instrument gradually fades out when it is 
 * released (key off)
 * This effect maintains a fadeout value and a counter. 
 * The volume is calculated by:
 * new_vol = vol * (1 - fadeout * counter)
 * The counter is increased each tick after the effect is activated.  
 * @author torkjel
 */
public class Fadeout implements AutoEffect
{
	private boolean on = true;
	private double fadeout;
	private boolean[] active;
	private int[] count;

	/**
	 * cerates an fadeout effect with a given fadeout level
	 * @param fadeout
	 */
	public Fadeout(double fadeout)
	{
		this.fadeout = fadeout;
	}
		
	public void setNumberOfTracks(int tracks)
	{
		active = new boolean[tracks];
		count = new int[tracks];
	}

	public void doEffect(TrackState state, int track)
	{
		if (on && active[track])
		{
			double d = 1 - count[track] * fadeout;
			if (d < 0)
				state.setFadeoutVolume(0);
			else
				state.setFadeoutVolume(d);
			count[track]++;
		}
		else if (!on && active[track])
			state.setFadeoutVolume(0);

	}

    public void keyOff(int track) {
        count[track] = 0;
        active[track] = true;
    }
    
	public void newNote(double note, int track)
	{
		if (note != Instrument.NO_NOTE)
			active[track] = false;
	}

	public void reset(int track)
	{
		active[track] = false;
		count[track] = 0;
	}

	/**
	 * The fadeout effect should only be active if the instrument has an active
	 * volume envelope
	 */
	public void activate()
	{
		on = true;
	}

	/**
	 * The fadeout effect should only be active if the instrument has an active
	 * volume envelope
	 */
	public void deActivate()
	{
		on = false;
	}
}
