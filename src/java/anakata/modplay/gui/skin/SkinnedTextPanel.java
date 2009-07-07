/*
 * Created on Sep 4, 2004
 */
package anakata.modplay.gui.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public class SkinnedTextPanel implements ISkinnedComponent {

    private SkinnedPanel parent;
    private int xpos, ypos, width, height;
    private String text = "Welcome :)";
    
    private int borderWidth = 3;
    private Color borderColor = new Color(0x0ff00ff00);
    private Color backgroundColor = new Color(0x0ff0000ff);
    private Color textColor = new Color(0x0ffffffff);
    
    public SkinnedTextPanel(SkinnedPanel parent, int xpos, int ypos, int width, int height) {
        this.parent = parent;
        this.xpos = xpos;
        this.ypos = ypos;
        this.width = width;
        this.height = height;
    }
    
    public void setText(String text) {
        this.text = text;
        parent.repaint(this);
    }

    public void setAttribute(String key, String value) 
        throws InvalidFormatException {
        
        if (key.equalsIgnoreCase("background-color")) {
            int val = parseColor(value);
            backgroundColor = new Color(parseColor(value),true);
        } else if (key.equalsIgnoreCase("text-color")) {
            textColor = new Color(parseColor(value),true);
        } else if (key.equalsIgnoreCase("border-width")) {
            borderWidth = Integer.parseInt(value);
        } else if (key.equalsIgnoreCase("border-color")) {
            borderColor = new Color(parseColor(value),true);
        } else 
            throw new InvalidFormatException(
                "unknown attribute: " + key +":" + value);
    }

    public int getXPos() {
        return xpos;
    }
    
    public int getYPos() {
        return ypos;
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
        g.fillRect(xpos+borderWidth,ypos+borderWidth,width-borderWidth*2,height-borderWidth*2);

        int fontSize = height-borderWidth*2;
        int scaledFontSize = (int)(fontSize*0.85);
        int fontDispl = fontSize-scaledFontSize;
            
        g.setFont(new Font("monospaced",Font.BOLD,scaledFontSize));

        Rectangle2D textSize = 
            g.getFontMetrics(g.getFont()).getStringBounds(text,g);
        
        g.setColor(textColor);
        Shape oldClip = g.getClip();

        int textClipWidth = width-borderWidth*2-4;
        int textClipHeight = height-borderWidth*2-4;        
        int scrollLength = (int)(textClipWidth + textSize.getWidth());

        int pos = (int)((scrollLength - ((System.currentTimeMillis() / 30) % scrollLength)) - textSize.getWidth());
        if (textClipWidth >= textSize.getWidth())
            pos = 0;
        
        g.setClip(xpos+borderWidth+2, ypos+borderWidth+2, textClipWidth, textClipHeight);
        g.drawChars(text.toCharArray(),0,text.length(),pos+xpos+borderWidth+2,ypos+height-borderWidth-2-fontDispl);
        g.setClip(oldClip);
    }

    public static int parseColor(String color) throws InvalidFormatException {
        if (color.length() != 8) throw new InvalidFormatException(
            "must be 8 characters in a color specification");
        int alpha = hexToInt(color.charAt(0)) * 16 + hexToInt(color.charAt(1));
        int red = hexToInt(color.charAt(2)) * 16 + hexToInt(color.charAt(3));
        int green = hexToInt(color.charAt(4)) * 16 + hexToInt(color.charAt(5));
        int blue = hexToInt(color.charAt(6)) * 16 + hexToInt(color.charAt(7));
        return (alpha << 24) | (red << 16) | (green << 8) | blue; 
    }
    
    public static int hexToInt(char c) throws InvalidFormatException {
        if (c >= '0' && c <= '9') return c-'0';
        else if (c >= 'a' && c <= 'f') return c-'a' + 10;
        else if (c >= 'A' && c <= 'F') return c-'A' + 10;
        else throw new InvalidFormatException(
            "\"" + c + "\" is not a hex value");
    }

}
