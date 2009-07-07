/*
 * Created on Sep 10, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import anakata.modplay.gui.Action;
import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public class PositionSlide extends SkinnedSlide {

    private Color textColor;
    private boolean showText = false;
    
    public PositionSlide(SkinnedPanel parent, Action action, int xpos, int ypos, int width, int height) {
        super(parent, action, xpos, ypos, width, height);
        level = 0;
    }

    public void paint(Graphics g) {
        super.paint(g);
    }

    public void mouseDragged(MouseEvent e) {
        // don't react to dragging
    }

    public void setAttribute(String key, String value) 
        throws InvalidFormatException {
        
        if (key.equals("text-color"))
            textColor = new Color(SkinnedTextPanel.parseColor(value));
        else if (key.equals("show-text"))
            showText = value.equals("true");
        else 
            super.setAttribute(key,value);
    }

    public void setPosition(int current, int length) {
        setPos(current/(double)length);
    }
    
    private void setPos(double l) {
        l = 1-l;
        if (l < 0) l = 0;
        else if (l > 1) l = 1;
        super.level = l;
        parent.repaint(this);
    }

}
