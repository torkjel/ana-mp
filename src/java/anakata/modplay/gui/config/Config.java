/*
 * Created on Sep 12, 2004
 */
package anakata.modplay.gui.config;

import java.io.IOException;

import anakata.modplay.loader.InvalidFormatException;

/**
 * @author torkjel
 */
public class Config {

    public static final String CONFIG_DIR = 
        System.getProperty("user.home") + "/.ana-mp/";
    
    private static Config config;
    
    public static Config get() throws IOException, InvalidFormatException {
        if (config == null) {
            config = ConfigLocator.findConfig();
        }
        return config;
    }

    public void set() throws IOException {
        ConfigLocator.storeConfig(this);
    }
    
    private PlayerConfig playerConfig;
    private PlayListConfig listConfig;
    
    public Config() {
        playerConfig = new PlayerConfig();
        listConfig = new PlayListConfig();
        
    }
    
    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public PlayListConfig getPlayListConfig() {
        return listConfig;
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<ana-mp-config version=\"1.0\">\n");
        sb.append(playerConfig.toXML() + "\n");
        sb.append(listConfig.toXML());
        sb.append("</ana-mp-config>\n");
        return sb.toString();
    }
}

