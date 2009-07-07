/*
 * Created on Sep 9, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import anakata.modplay.gui.Action;
import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public class SkinnedSlide implements ISkinnedComponent, MouseListener, MouseMotionListener {

    private Color borderColor = new Color(0x0ff00ff00);
    private Color backgroundColor = new Color(0x0ff0000ff);
    private Color foregroundColor = new Color(0x0ff0a0aff);
    private int borderWidth = 3;
    
    protected SkinnedPanel parent;
    private int xpos, ypos, height, width;
    protected double level = 0.5; 
    private boolean vertical = true;
    
    private Action action;
    
    public SkinnedSlide(SkinnedPanel parent, Action action, int xpos, int ypos, int width, int height) {
        this.parent = parent;
        this.action = action;
        this.xpos = xpos;
        this.ypos = ypos;
        this.width = width;
        this.height = height;
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
    }

    
    public int getXPos() {
        return 0;
    }

    public int getYPos() {
        return 0;
    }
    
    public void paint(Graphics g) {
        if (borderWidth > 0) {
            g.setColor(borderColor);
            g.fillRect(xpos+0,ypos+0,width,borderWidth);
            g.fillRect(xpos+0,ypos+height-borderWidth,width,borderWidth);
            g.fillRect(xpos+0,ypos+borderWidth,borderWidth,height-borderWidth*2);
            g.fillRect(xpos+width-borderWidth,ypos+0+borderWidth,borderWidth,height-borderWidth*2);
        }
        g.setColor(backgroundColor);

        if (vertical) {
            int div = (int)(((double)(height-borderWidth*2) * level));
            g.setColor(backgroundColor);
            g.fillRect(xpos+borderWidth,ypos+borderWidth,width-borderWidth*2,div);
            g.setColor(foregroundColor);
            g.fillRect(xpos+borderWidth,ypos+div+borderWidth,width-borderWidth*2,(height-div)-borderWidth*2);
        } else { 
            int div = (int)(((double)(width-borderWidth*2) * (1-level)));
            g.setColor(foregroundColor);
            g.fillRect(xpos+borderWidth,ypos+borderWidth,div,height-borderWidth*2);
            g.setColor(backgroundColor);
            g.fillRect(xpos+div+borderWidth,ypos+borderWidth,(width-div)-borderWidth*2,height-borderWidth*2);
        }
    }

    public void setAttribute(String key, String value) 
        throws InvalidFormatException {
    
    if (key.equalsIgnoreCase("background-color")) {
        int val = SkinnedTextPanel.parseColor(value);
        backgroundColor = new Color(SkinnedTextPanel.parseColor(value),true);
    } else if (key.equalsIgnoreCase("foreground-color")) {
        int val = SkinnedTextPanel.parseColor(value);
        foregroundColor = new Color(SkinnedTextPanel.parseColor(value),true);
    } else if (key.equalsIgnoreCase("border-width")) {
        borderWidth = Integer.parseInt(value);
    } else if (key.equalsIgnoreCase("border-color")) {
        borderColor = new Color(SkinnedTextPanel.parseColor(value),true);
    } else if (key.equalsIgnoreCase("orientation")) {
        vertical = value.equals("vertical");
    } else 
        throw new InvalidFormatException(
            "unknown attribute: " + key +":" + value);
    }
   
    protected void setLevel(double l) {
        if (l < 0) l = 0;
        else if (l > 1) l = 1;
        this.level = l;
        action.fireAction(1-level);
        parent.repaint(this);
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
        if (!isIn(e)) return;
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && vertical) {
            setLevel((e.getY()-ypos)/(double)height);
        } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && !vertical) {
            setLevel((width-(e.getX()-xpos))/(double)width);
        }
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
        if (!isIn(e)) return;
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && vertical) {
            setLevel((e.getY()-ypos)/(double)height);
        } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && !vertical) {
            setLevel((width-(e.getX()-xpos))/(double)width);
        }
    }
    public void mouseMoved(MouseEvent e) {
    }

    private boolean isIn(MouseEvent e) {
        return 
            !(e.getX() < xpos || e.getX() > xpos+width ||
            e.getY() < ypos || e.getY() > ypos+height);
    }

    public double getLevel() {
        return 1-level;
    }
}
