/*
 * Created on Sep 4, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Graphics;

import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public interface ISkinnedComponent {
    public int getXPos();
    public int getYPos();
    public void paint(Graphics g);
    public void setAttribute(String key, String value) throws InvalidFormatException;
}
