/*
 * Created on Sep 13, 2004
 */
package anakata.modplay.gui.config;

/**
 * @author torkjel
 */
public  class PlayListConfig {
    private boolean visible = true;
    private int xPos = 450;
    private int yPos = 100;
    private int width = 300;
    private int height = 400;
    private String list = Config.CONFIG_DIR + "default.list";
    private int listPosition = 0;
    private String modFileChooserDir = "."; 
    private String listFileChooserDir = "."; 
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setXPos(int xPos) {
        this.xPos = xPos;
    }
    public int getXPos() {
        return xPos;
    }
    public void setYPos(int yPos) {
        this.yPos = yPos;
    }
    public int getYPos() {
        return yPos;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getWidth() {
        return width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getHeight() {
        return height;
    }
    public void setList(String list) {
        this.list = list;
    }
    public String getList() {
        return list;
    }
    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }
    public int getListPosition() {
        return listPosition;
    }

    public void setModFileChooserDir(String modFileChooserDir) {
        this.modFileChooserDir = modFileChooserDir;
    }
    public String getModFileChooserDir() {
        return modFileChooserDir;
    }
    public void setListFileChooserDir(String listFileChooserDir) {
        this.listFileChooserDir = listFileChooserDir;
    }
    public String getListFileChooserDir() {
        return listFileChooserDir;
    }
    
    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<playlist>\n");
        sb.append("<attribute key=\"visible\" value=\"" + isVisible() + "\"/>\n");
        sb.append("<attribute key=\"xpos\" value=\"" + getXPos() + "\"/>\n");
        sb.append("<attribute key=\"ypos\" value=\"" + getYPos() + "\"/>\n");
        sb.append("<attribute key=\"width\" value=\"" + getWidth() + "\"/>\n");
        sb.append("<attribute key=\"height\" value=\"" + getHeight() + "\"/>\n");
        sb.append("<attribute key=\"list\" value=\"" + getList() + "\"/>\n");
        sb.append("<attribute key=\"list-position\" value=\"" + getListPosition() + "\"/>\n");
        sb.append("<attribute key=\"mod-file-chooser-dir\" value=\"" + getModFileChooserDir() + "\"/>\n");
        sb.append("<attribute key=\"list-file-chooser-dir\" value=\"" + getListFileChooserDir() + "\"/>\n");
        sb.append("</playlist>\n");
        return sb.toString();
    }
}