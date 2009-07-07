/*
 * Created on Sep 15, 2004
 */
package anakata.modplay.gui.skin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import anakata.modplay.gui.config.Config;

/**
 * @author torkjel
 */
public abstract class SkinResourceLocator {

    protected static final String SKIN_DESCRIPTOR = "skin.xml";
    
    public static SkinResourceLocator getLocator(String skinName) 
        throws IOException {
        
        try {
            return new InternalSkinResourceLocator(skinName);
        } catch (FileNotFoundException e) {
            try {
                return new InstalledSkinResourceLocator(skinName);
            } catch (FileNotFoundException e2) {
                return new LocalSkinResourceLocator(skinName);
            }
        }
    }
    
    public SkinResourceLocator() {
    }
    
    public abstract URL getResourceURL(String resource) 
        throws FileNotFoundException;

    public URL getSkinDefinition() throws FileNotFoundException {
        return getResourceURL(SKIN_DESCRIPTOR);
    }
    
}


class InternalSkinResourceLocator extends SkinResourceLocator {

    private final String skinPath;
    
    public InternalSkinResourceLocator(String skinName) 
        throws FileNotFoundException {
        super();
        skinPath = "data/skin/" + skinName;
        if (getSkinDefinition() == null) throw new FileNotFoundException(
                "Skin definition for skin \"" + skinName + "\" not found");
    }
    
    public URL getResourceURL(String resource) {
        URL url = ClassLoader.getSystemResource(skinPath + "/" + resource);
        return url;
    }
}

abstract class FileSystemSkinResourceLocator extends SkinResourceLocator {

    protected String skinPath;

    protected FileSystemSkinResourceLocator() {
    }
    
    public URL getResourceURL(String resource) throws FileNotFoundException {
        URL url = null;
        try {
            url = new URL("file","",skinPath + "/" + resource);
        } catch (MalformedURLException e) {
            throw new FileNotFoundException(
                "Could not find resource: " + resource);
        }
        return url;
    }
    
}

class LocalSkinResourceLocator extends FileSystemSkinResourceLocator {

    public LocalSkinResourceLocator(String skinName) 
        throws FileNotFoundException {
        super();
        skinPath = "data/skin/" + skinName;
        if (getSkinDefinition() == null) throw new FileNotFoundException(
            "Skin definition for skin \"" + skinName + "\" not found");
    }
}

class InstalledSkinResourceLocator extends FileSystemSkinResourceLocator {

    public InstalledSkinResourceLocator(String skinName) 
        throws IOException {
        super();
        skinPath = Config.CONFIG_DIR + "skin/" + skinName;
        if (getSkinDefinition() == null || getSkinDefinition().openStream() == null) throw new FileNotFoundException(
            "Skin definition for skin \"" + skinName + "\" not found");
    }
}