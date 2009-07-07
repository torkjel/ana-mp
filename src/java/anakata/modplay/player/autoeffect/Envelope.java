package anakata.modplay.player.autoeffect;

/**
 * An interface inplemented by any envelope autoeffects
 * @author torkjel
 */
public interface Envelope extends AutoEffect
{
	/**
	 * set the position of an envelope
	 * @param pos
	 * @param track
	 */
    public void setPosition(int pos, int track);
}

