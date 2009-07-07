/*
 * Created on Sep 2, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JWindow;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import anakata.modplay.gui.Action;
import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public class SkinParser {

    public static final int BUTTON_STATE_NORMAL = 1;
    public static final int BUTTON_STATE_MOUSEOVER = 2;
    public static final int BUTTON_STATE_CLICKED = 4;
    
    public static final int BUTTON_PLAY = 8;
    public static final int BUTTON_STOP = 16;
    public static final int BUTTON_PAUSE = 32;
    public static final int BUTTON_SKIP = 64;
    public static final int BUTTON_PREV = 128;
    public static final int BUTTON_EXIT = 256;
    public static final int BUTTON_MINIMIZE = 512;
    public static final int BUTTON_CONFIGURE = 1024;
    public static final int BUTTON_PLAYLIST = 2048;
    public static final int BUTTON_INFO = 4096;

    public static final int SLIDER_VOLUME = 8192;
    public static final int SLIDER_POSITION = 16384;
    
    private static final int[] ACTIONS = {
        BUTTON_PLAY, BUTTON_PAUSE, BUTTON_CONFIGURE, BUTTON_INFO, BUTTON_PLAYLIST, 
        BUTTON_EXIT, BUTTON_MINIMIZE, BUTTON_PREV, BUTTON_SKIP, BUTTON_STOP};
    
    private Map imageMap = new HashMap();
    private ImageObserver imgObserver;
    private JWindow window;
    
    private Element root;
    private SkinResourceLocator locator;
    
    public SkinParser(String name, ImageObserver imgObserver, JWindow window) 
        throws InvalidFormatException, IOException {

        locator = SkinResourceLocator.getLocator(name);
        InputStream in = locator.getSkinDefinition().openStream();
        
        this.imgObserver = imgObserver;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            root = doc.getDocumentElement();
        } catch (Exception e) {
            throw new InvalidFormatException("could not parse skin",e);
        }
        this.window = window;
    }

    public SkinnedButton getButton(int type, SkinnedPanel parent) 
        throws InvalidFormatException {
        Image norm = getButtonImage(type,BUTTON_STATE_NORMAL);
        Image mouse = getButtonImage(type,BUTTON_STATE_MOUSEOVER);
        Image click = getButtonImage(type,BUTTON_STATE_CLICKED);
        Point pos = getButtonPos(getButtonElement(type));
        return new SkinnedButton(
            parent,new Action(type,window),
            norm,mouse,click,pos.x,pos.y,0.5);
    }
    
    public SkinnedPanel getPanel(Container parent) throws InvalidFormatException {
        Dimension size = getPanelSize();
        Image bg = getBackgroundImage();
        SkinnedPanel panel = new SkinnedPanel(getName(), parent, size.width,size.height,bg,window);
        for (int action : ACTIONS) {
            if (hasButton(action))
                panel.addButton(getButton(action, panel));
        }
        Iterator panels = getPanels(parent,panel).iterator();
        while (panels.hasNext()) {
            panel.addComponent((ISkinnedComponent)panels.next()); 
        }

        return panel;
    }
    
    private String getName() {
        return root.getAttribute("name");
    }
    
    private List getPanels(Container parent, SkinnedPanel panel) throws InvalidFormatException {
        List<ISkinnedComponent> list = new ArrayList<ISkinnedComponent>();
        NodeList panels = root.getElementsByTagName("panel");
        for (int n = 0; n < panels.getLength(); n++) {
            list.add(getPanel(panel,(Element)panels.item(n)));
        }
        return list;
    }
    
    private ISkinnedComponent getPanel(SkinnedPanel parent, Element panel) 
        throws InvalidFormatException {
        
        String type = panel.getAttribute("type");
        int xpos = Integer.parseInt(panel.getAttribute("x-position"));
        int ypos = Integer.parseInt(panel.getAttribute("y-position"));
        int width = Integer.parseInt(panel.getAttribute("width"));
        int height = Integer.parseInt(panel.getAttribute("height"));

        ISkinnedComponent iPanel = null;
        if (type.equals("info")) { 
            iPanel = new InfoPanel(parent,xpos,ypos,width, height);
        } else if (type.equals("volume")) { 
            Action a = new Action(Action.VOLUME,window);
            iPanel = new VolumeSlide(parent,a,xpos,ypos,width, height);
        } else if (type.equals("position")) { 
            Action a = new Action(Action.POSITION,window);
            iPanel = new PositionSlide(parent,a,xpos,ypos,width, height);
        }

        NodeList attribs = panel.getElementsByTagName("attribute");
        for (int n = 0; n < attribs.getLength(); n++) {
            Element attr = (Element)attribs.item(n);
            iPanel.setAttribute(
                attr.getAttribute("key"), attr.getAttribute("value"));
        }
        return iPanel;
    }
        
    private Image getBackgroundImage() throws InvalidFormatException {
        Element bg = (Element)root.getElementsByTagName("background-image").item(0);
        return loadImage(bg.getAttribute("url"));
    }
    
    private Dimension getPanelSize() {
        Element elem = (Element)root.getElementsByTagName("geometry").item(0);
        return new Dimension(
            Integer.parseInt(elem.getAttribute("width")),
            Integer.parseInt(elem.getAttribute("height")));
    }
    
    private Image getButtonImage(int type, int state) throws InvalidFormatException {
        Integer key = new Integer(type|state);
        Object imgobj = imageMap.get(key);
        if (imgobj != null) return (Image)imgobj; 
        Image img = loadImage(type,state);
        imageMap.put(key,img);
        return img;
    }
    
    private Image loadImage(int type, int state) throws InvalidFormatException {
        Element element = getButtonElement(type);
        Image img = getButtonImage(element,state);
        return img;
    }

    private Element getButtonElement(int type) throws InvalidFormatException {
        NodeList buttons = root.getElementsByTagName("button");
        for (int n = 0; n < buttons.getLength(); n++) {
            Element button = (Element)buttons.item(n);
            if (getType(button.getAttribute("action")) == type) return button;
        }
        throw new InvalidFormatException("unknown button type: " + type);
    }

    private boolean hasButton(int type) {
        try {
            return getButtonElement(type) != null;
        } catch (InvalidFormatException e) {
            return false;
        }
    }
    
    private Image getButtonImage(Element buttonElem, int state) throws InvalidFormatException {
        NodeList images = buttonElem.getElementsByTagName("image");
        for (int n = 0; n < images.getLength(); n++) {
            Element image = (Element)images.item(n);
            if (getState(image.getAttribute("state")) == state) return loadImage(image.getAttribute("url"));
        }
        throw new InvalidFormatException("image has no image for state: " + state);
    }
    
    private Point getButtonPos(Element button) {
        int xpos = Integer.parseInt(button.getAttribute("x-position"));
        int ypos = Integer.parseInt(button.getAttribute("y-position"));
        return new Point(xpos,ypos);
    }
    
    private Image loadImage(String file) throws InvalidFormatException {

        URL res = null;
        try {
            res = locator.getResourceURL(file);
        } catch (IOException e) {
            throw new InvalidFormatException(
                "Could not load skin resource: " + file,e);
        }

        Image img = Toolkit.getDefaultToolkit().createImage(res);
        while(img.getHeight(imgObserver) == -1) {
            try {Thread.sleep(100);} catch (InterruptedException e) {}
        }
        return img;
    }
    
    private int getType(String typeStr) throws InvalidFormatException {
        if (typeStr.equals("play")) return BUTTON_PLAY;
        else if (typeStr.equals("stop")) return BUTTON_STOP;
        else if (typeStr.equals("pause")) return BUTTON_PAUSE;
        else if (typeStr.equals("prev")) return BUTTON_PREV;
        else if (typeStr.equals("skip")) return BUTTON_SKIP;
        else if (typeStr.equals("exit")) return BUTTON_EXIT;
        else if (typeStr.equals("minimize")) return BUTTON_MINIMIZE;
        else if (typeStr.equals("configure")) return BUTTON_CONFIGURE;
        else if (typeStr.equals("playlist")) return BUTTON_PLAYLIST;
        else if (typeStr.equals("info")) return BUTTON_INFO;
        else if (typeStr.equals("volume")) return SLIDER_VOLUME;
        else if (typeStr.equals("position")) return SLIDER_POSITION;
        else throw new InvalidFormatException("unknown action: " + typeStr);
    }

    private int getState(String stateStr) throws InvalidFormatException {
        if (stateStr.equals("normal")) return BUTTON_STATE_NORMAL;
        else if (stateStr.equals("mouseover")) return BUTTON_STATE_MOUSEOVER;
        else if (stateStr.equals("clicked")) return BUTTON_STATE_CLICKED;
        else throw new InvalidFormatException("unknown state: " + stateStr);
    }
}
