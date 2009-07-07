/*
 * Created on Aug 23, 2004
 */
package anakata.util.audiodiff.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import anakata.util.audiodiff.logic.IWaveModel;

/**
 * @author torkjel
 */
public class WavePanel extends JPanel implements MouseListener, MouseMotionListener {
    
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 600;
    private IWaveModel[] model;
    private ControlPanel control;

    private int selectionStart, selectionEnd;
    
    public WavePanel(IWaveModel[] model, ControlPanel panel) {
        super();
        this.control = panel;
        this.model = model;
        setSize(WIDTH, HEIGHT);
        setMaximumSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        selectionStart = 0;
        selectionEnd = getMaxSize()-1;
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    private int getMaxSize() {
        int max = -1;
        for (int n = 0; n < model.length; n++) {
            if (model[n].getSize() > max) {
                max = model[n].getSize();
            }
        }
        return max;
    }

    private int[] colors = {
        0x0ffa0a0ff, 0x0ffa0ff00, 0x0ffffa0a0, 
        0x0ffffffa0, 0x0ffffa0ff, 0x0ffa0ffff};
    
    public void paint(Graphics g) {
        setBackground(new Color(0x0ff000000));
        super.paint(g);

        for (int m = 0; m < model.length; m++) {
            g.setColor(new Color(colors[m]));
            int[] data = model[m].getSelection(selectionStart,selectionEnd,WIDTH);
            for (int n = 0; n < data.length-1; n++) {
                int d1 = (int)(data[n+0] * 600.0 / 65536 + 300);  
                int d2 = (int)(data[n+1] * 600.0 / 65536 + 300);  
                g.drawLine(n,d1,n+1,d2);
            }
        }

        g.setColor(Color.BLACK);
        int start = startSelection < endSelection ? 
            startSelection : endSelection;
        int end = startSelection < endSelection ? 
            endSelection : startSelection;
        for (int n = start; n <= end; n++) {
            g.setXORMode(Color.YELLOW);
            g.drawLine(n,0,n,HEIGHT-1);
        }

        g.setPaintMode();
        g.setColor(new Color(0x0ff404040));        
        g.drawLine(0,HEIGHT/2,WIDTH,HEIGHT/2);
    }

    public IWaveModel getModel(int index) {
        return model[index];
    }

    public void setSelelection(int start, int end) {
        this.selectionStart = start;
        this.selectionEnd = end;
    }

    public int getSelectionStart() {
        return selectionStart;
    }
    
    public int getSelectionEnd() {
        return selectionEnd;
    }
    
    public void selectRange() {
        int start = selectionStart + startSelection * Math.abs(selectionStart - selectionEnd)/WIDTH;
        int end = selectionStart + endSelection * Math.abs(selectionStart - selectionEnd)/WIDTH;
        selectionStart = start < end ? start : end;
        selectionEnd = start < end ? end : start;
        update();

    }
    
    public void update() {
        repaint();
        control.setSelectionEnd(selectionEnd);
        control.setSelectionStart(selectionStart);
            
    }

    private boolean dragging = false;;
    private int startSelection = 0;
    private int endSelection = 0;

    public void clearSelection() {
        startSelection = 0;
        endSelection = 0;
    }
    
    public void mouseClicked(MouseEvent e) {
        startSelection = 0;
        endSelection = 0;
        update();
    }

    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            dragging = false;
            endSelection = (int)e.getPoint().getX();
            if (endSelection < 0) endSelection = 0;
            else if (endSelection > getWidth()) endSelection = getWidth()-1;
        }
        update();
    }

    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        if (!dragging) {
            dragging = true;
            startSelection = (int)p.getX();
            if (startSelection < 0) startSelection = 0;
            else if (startSelection > getWidth()) startSelection = getWidth()-1;
        } else {
            endSelection = (int)p.getX();
            if (endSelection < 0) endSelection = 0;
            else if (endSelection > getWidth()) endSelection = getWidth()-1;
        }
        update();
    }

    public void mouseMoved(MouseEvent e) {
    }
}
