/*
 * Created on Sep 13, 2004
 */
package anakata.modplay.gui.config;

/**
 * @author torkjel
 */
public class PlayerConfig {
    private int xpos = 100;
    private int ypos = 100;
    private String skin = "standard";
    private double volume = 0.5;

    public void setXPos(int xpos) {
        this.xpos = xpos;
    }
    public int getXPos() {
        return xpos;
    }
    public void setYPos(int ypos) {
        this.ypos = ypos;
    }
    public int getYPos() {
        return ypos;
    }
    public void setSkin(String skin) {
        this.skin = skin;
    }
    public String getSkin() {
        return skin;
    }
    public void setVolume(double volume) {
        this.volume = volume;
    }
    public double getVolume() {
        return volume;
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<player>\n");
        sb.append("<attribute key=\"xpos\" value=\"" + getXPos() + "\"/>\n");
        sb.append("<attribute key=\"ypos\" value=\"" + getYPos() + "\"/>\n");
        sb.append("<attribute key=\"skin\" value=\"" + getSkin() + "\"/>\n");
        sb.append("<attribute key=\"volume\" value=\"" + getVolume() + "\"/>\n");
        sb.append("</player>\n");
        return sb.toString();
    }
}