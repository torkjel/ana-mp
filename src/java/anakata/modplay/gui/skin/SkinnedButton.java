/*
 * Created on Sep 3, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.event.MouseInputListener;

import anakata.modplay.gui.Action;

/**
 * @author torkjel
 */
public class SkinnedButton implements MouseInputListener{
    private Image normal;
    private Image mouseover;
    private Image clicked;
    private int xpos;
    private int ypos;
    private int width;
    private int height;

    private SkinnedPanel parent;
    private Action action;
    
    private int state = SkinParser.BUTTON_STATE_NORMAL;

    private double[] alpha;
    
    public SkinnedButton(SkinnedPanel parent, Action action, Image normal, Image mouseover, Image clicked, int xpos, int ypos, double transp) {
        this.parent = parent;
        this.action = action;
        height = normal.getHeight(parent);
        width =  normal.getWidth(parent);
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        this.xpos = xpos;
        this.ypos = ypos;
        this.normal = normal;
        this.mouseover = mouseover;
        this.clicked = clicked;
        alpha = new double[width*height];
        for (int n = 0; n < alpha.length; n++) {
            alpha[n] = transp;
        }
    }
    
    public int getXPos() {
        return xpos;
    }
    
    public int getYPos() {
        return ypos;
    }
    
    public void paint(Graphics g) {
        Image img = null;
        if (state == SkinParser.BUTTON_STATE_NORMAL) {
            img = normal;
        } else if (state == SkinParser.BUTTON_STATE_MOUSEOVER) { 
            img = mouseover;
        } else if (state == SkinParser.BUTTON_STATE_CLICKED) { 
            img = clicked;
        }
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        bi.getGraphics().drawImage(img,0,0,parent);
        WritableRaster wr = bi.getRaster();
        int[] pix = wr.getSamples(0,0,width,height,3,(int[])null);
        for (int n = 0; n < pix.length; n++) {
            pix[n] = 0x0c0;
//            pix[n] = (pix[n] & 0x0ffffff) | 0x0a000000;
        }
        wr.setSamples(0,0,width,height,3,pix);
        bi.setData(wr);
        g.drawImage(bi,xpos,ypos,parent);
    }

    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            parent.repaint(this);
        }
    }

    public int getState() {
        return state;
    }

    public void mouseClicked(MouseEvent e) {
        setState(e,-1);
    }
    public void mouseEntered(MouseEvent e) {
        setState(e,-1);
    }
    public void mouseExited(MouseEvent e) {
        setState(e,-1);
    }
    public void mousePressed(MouseEvent e) {
        setState(e,1);
    }
    public void mouseReleased(MouseEvent e) {
        setState(e,2);
    }
    public void mouseDragged(MouseEvent e) {
        setState(e,-1);
    }
    public void mouseMoved(MouseEvent e) {
        setState(e,-1);
    }

    private void setState(MouseEvent e, int buttonStateChange) {
        int oldState = state;
        
        Point p = e.getPoint();
        boolean isIn = 
            p.getX() >= xpos && p.getX() <= xpos+width && 
            p.getY() >= ypos && p.getY() <= ypos+height; 
        
        if (isIn && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            if (buttonStateChange == 1)
                setState(SkinParser.BUTTON_STATE_CLICKED);
            else if (buttonStateChange == 2)
                setState(SkinParser.BUTTON_STATE_MOUSEOVER);
        } else if (isIn) {
            setState(SkinParser.BUTTON_STATE_MOUSEOVER);
        } else { 
            setState(SkinParser.BUTTON_STATE_NORMAL);
        }

        if (oldState == SkinParser.BUTTON_STATE_CLICKED && 
            state != SkinParser.BUTTON_STATE_CLICKED) {
            action.fireAction();
        }
    }
}
