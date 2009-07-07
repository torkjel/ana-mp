/*
 * Created on Sep 12, 2004
 */
package anakata.modplay.gui.config;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import anakata.modplay.loader.InvalidFormatException;
import anakata.util.Logger;

/**
 * @author torkjel
 */
public class ConfigParser {
    
    private InputStream in;
    
    public ConfigParser(InputStream in) {
        this.in = in;
    }
    
    public Config parse() throws InvalidFormatException {
        Logger.debug("Parsing config...");
        Config conf = new Config();
        PlayerConfig pconf = conf.getPlayerConfig();
        PlayListConfig lconf = conf.getPlayListConfig();
        
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
        
            Document doc = builder.parse(in);
            Element root = doc.getDocumentElement();
            NodeList list = root.getElementsByTagName("player");
            if (list.getLength() > 0) 
                parsePlayerConfig(pconf,(Element)list.item(0));
        
            list = root.getElementsByTagName("playlist");
            if (list.getLength() > 0) 
                parsePlayListConfig(lconf,(Element)list.item(0));

        } catch (Exception e) {
            throw new InvalidFormatException("Could not load config file",e);
        }
        
        Logger.debug("Parsed config");
        return conf;
    }


    private void parsePlayerConfig(PlayerConfig pconf, Element elem) {
        NodeList attribs = elem.getElementsByTagName("attribute");
        for (int n = 0; n < attribs.getLength(); n++) {
            Element attr = (Element)attribs.item(n);
            String key = attr.getAttribute("key");
            String value = attr.getAttribute("value");
            if (key.equals("xpos"))
                pconf.setXPos(Integer.parseInt(value));
            else if (key.equals("ypos"))
                pconf.setYPos(Integer.parseInt(value));
            else if (key.equals("skin"))
                pconf.setSkin(value);
            else if (key.equals("volume"))
                pconf.setVolume(Double.parseDouble(value));
        }
    }

    private void parsePlayListConfig(PlayListConfig pconf, Element elem) {
        NodeList attribs = elem.getElementsByTagName("attribute");
        for (int n = 0; n < attribs.getLength(); n++) {
            Element attr = (Element)attribs.item(n);
            String key = attr.getAttribute("key");
            String value = attr.getAttribute("value");
            if (key.equals("visible"))
                pconf.setVisible(value.equals("true"));
            else if (key.equals("xpos"))
                pconf.setXPos(Integer.parseInt(value));
            else if (key.equals("ypos"))
                pconf.setYPos(Integer.parseInt(value));
            else if (key.equals("width"))
                pconf.setWidth(Integer.parseInt(value));
            else if (key.equals("height"))
                pconf.setHeight(Integer.parseInt(value));
            else if (key.equals("list"))
                pconf.setList(value);
            else if (key.equals("list-position"))
                pconf.setListPosition(Integer.parseInt(value));
            else if (key.equals("mod-file-chooser-dir"))
                pconf.setModFileChooserDir(value);
            else if (key.equals("list-file-chooser-dir"))
                pconf.setListFileChooserDir(value);
        }
    }

}


/*


    <ana-mp-config>

        <player>
            <attribute key="xpos" value="..."/>
            <attribute key="ypos" value="..."/>
            <attribute key="skin" value="jungle"/>
            <attribute key="vulume" value="..."/>
        </player>
                        
        <playlist>
            <attribute key="visible" value="true"/>
            <attribute key="xpos" value="..."/>
            <attribute key="ypos" value="..."/>
            <attribute key="width" value="..."/>
            <attribute key="height" value="..."/>
            <attribute key="list" value="..."/>
            <attribute key="list-position" value="..."/>
        </playlist>
    </ana-mp-config>

*/