/*
 * Created on Sep 12, 2004
 */
package anakata.modplay.gui.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import anakata.modplay.loader.InvalidFormatException;
import anakata.util.Logger;

/**
 * @author torkjel
 */
public abstract class ConfigLocator {

    private static ConfigLocator locator;
    
    public static Config findConfig() throws IOException, InvalidFormatException {
        if (locator == null) {
            locator = new FileSystemConfigLocator();
        }
        return locator.getConfig();
    }

    public static void storeConfig(Config conf) throws IOException {
        locator.writeConfig(conf);
    }

    protected ConfigLocator() throws IOException, InvalidFormatException {
        setConfig(parse(locateConfig()));
    }
    
    protected Config config;
    
    protected void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig () {
       return config; 
    }

    protected abstract InputStream locateConfig() throws IOException;
    protected abstract void writeConfig(Config config) throws IOException;

    private Config parse(InputStream in) throws InvalidFormatException {
        return new ConfigParser(in).parse();
    }
}

class FileSystemConfigLocator extends ConfigLocator {
    private static final String configFile = "ana-mp.conf.xml";
    private static final String[] locations = {Config.CONFIG_DIR};
    
    private static File path = new File(locations[0],configFile);
    
    public FileSystemConfigLocator() throws IOException, InvalidFormatException {
        super();
    }

    protected InputStream locateConfig() throws IOException {
        Logger.debug("Locating config...");
        File f = null;
        for (int n = 0; n < locations.length; n++) {
            f = new File(locations[n],configFile);
            if (f.exists()) {
                path = f;
                Logger.debug("Located config: " + path);
                return new FileInputStream(f);
            }
        }

        // create config file
        path.getParentFile().mkdirs();
        writeConfig(new Config());
        Logger.debug("Created new config: " + path);
        return new FileInputStream(path);
    }

    protected void writeConfig(Config conf) throws IOException {
        Logger.debug("Writing config: " + path + "...");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(conf.toXML().getBytes());
            fos.flush();
            Logger.debug("Wrote config");
        } finally {
            if (fos != null)
                fos.close();
        }
    }
}