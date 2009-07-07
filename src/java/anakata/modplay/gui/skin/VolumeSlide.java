/*
 * Created on Sep 10, 2004
 */
package anakata.modplay.gui.skin;

import anakata.modplay.gui.Action;

/**
 * @author torkjel
 */
public class VolumeSlide extends SkinnedSlide {

    public VolumeSlide(SkinnedPanel parent, Action action, int xpos, int ypos, int width, int height) {
        super(parent, action, xpos, ypos, width, height);
    }

    public void setVolume(double vol) {
        super.setLevel(1-vol);
    }
}
