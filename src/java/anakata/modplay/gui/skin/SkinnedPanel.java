/*
 * Created on Sep 3, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * @author torkjel
 */
public class SkinnedPanel extends JPanel implements MouseMotionListener, MouseListener {

    private List<SkinnedButton> buttons = new ArrayList<SkinnedButton>();
    private List<ISkinnedComponent> panels = new ArrayList<ISkinnedComponent>();
    private int widht, height;
    private Image background;
    private Container parent;
    private JWindow window;
    private String skinName;
    
    public SkinnedPanel(String skinName, Container parent, int width, int height, Image background, JWindow window) {
        super();
        this.skinName = skinName;
        Dimension sizeDim = new Dimension(width,height); 
        setSize(sizeDim);
        setPreferredSize(sizeDim);
        setMaximumSize(sizeDim);
        setMinimumSize(sizeDim);
        
        this.widht = width;
        this.height = height;
        this.background = background;
        parent.setSize(sizeDim);
        this.parent = parent;
        this.window = window;
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public String getSkinName() {
        return skinName;
    }
    
    public void addButton(SkinnedButton button) {
        synchronized(buttons) {
            buttons.add(button);
        }
    }

    public void addComponent(ISkinnedComponent info) {
        synchronized(panels) {
            panels.add(info);
        }
    }

    boolean first = true;
    
    public void paint(Graphics g) {
        super.paint(g);
//        if (first) {
            g.drawImage(background,0,0,this);
            first = false;
//        }

/*        synchronized(repaintQueue) {
           Iterator rpq = repaintQueue.iterator();
           while (rpq.hasNext()) {
               Object o = rpq.next();
               if (o instanceof SkinnedButton) {
                   ((SkinnedButton)o).paint(g);
               } else if (o instanceof ISkinnedComponent)
                   ((ISkinnedComponent)o).paint(g);
           }
           while (repaintQueue.size() > 0)
               repaintQueue.remove(0);
        }
*/        
        synchronized(buttons) {
        	for (SkinnedButton sb : buttons) {
                sb.paint(g);
        	}
        	for (ISkinnedComponent sp : panels) {
        		sp.paint(g);
        	}
        }
    }

    private boolean dragging = false;
    private int startDragX = 0;
    private int startDragY = 0;
    private Object dragSource = null;
    
    public void mouseDragged(MouseEvent e) {
        System.out.println(e.getPoint());
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) return;
        if (!dragging) {
            dragging = true;
            startDragX = e.getPoint().x;
            startDragY = e.getPoint().y;
            dragSource = e.getSource();
            return;
        } else if (e.getSource() == dragSource) {
            Point p = e.getPoint();
            int oldx = window.getLocation().x;
            int dx = p.x-startDragX;
            int oldy = window.getLocation().y;
            int dy = p.y-startDragY;
            window.setLocation(new Point(oldx+dx,oldy+dy));
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }
    
    public void setInfoText(String info) {
        Iterator pi = panels.iterator();
        while (pi.hasNext()) {
            Object next = pi.next();
            if ((next instanceof InfoPanel)) {
                ((InfoPanel)next).setText(info);
            }
        }
    }
    
    public double getVolume() {
        Iterator pi = panels.iterator();
        while (pi.hasNext()) {
            Object next = pi.next();
            if ((next instanceof VolumeSlide)) {
                return ((VolumeSlide)next).getLevel();
            }
        }
        return 0.5;
    }

    public void setVolume(double volume) {
        Iterator pi = panels.iterator();
        while (pi.hasNext()) {
            Object next = pi.next();
            if ((next instanceof VolumeSlide)) {
                ((VolumeSlide)next).setVolume(volume);
            }
        }
    }
    
    public void setPosition(int position, int length) {
        Iterator pi = panels.iterator();
        while (pi.hasNext()) {
            Object next = pi.next();
            if ((next instanceof PositionSlide)) {
                ((PositionSlide)next).setPosition(position,length);
            }
        }
    }

    private List repaintQueue = new ArrayList();

    public void repaint() {
        super.repaint();
    }
    
    public void repaint(Object obj) {
        synchronized(repaintQueue) {
            repaintQueue.add(obj);
        }
        repaint();
    }
}