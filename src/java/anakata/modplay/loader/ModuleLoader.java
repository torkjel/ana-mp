package anakata.modplay.loader;

import anakata.modplay.module.Module;

import java.io.*;
import java.net.URL;

/**
 * Must be extended by classes that load modules
 *
 * @author torkjel
 */
public abstract class ModuleLoader {

    public static final int UNKNOWN = 0;
    public static final int MOD = 1;
    public static final int XM = 2;
    public static final int S3M = 3;
    public static final int STM = 4;
    public static final int IT = 5;
    public static final int ZIP = 6;

    protected static final String EXT_MOD = ".mod";
    protected static final String EXT_XM = ".xm";
    protected static final String EXT_S3M = ".s3m";
    protected static final String EXT_STM = ".stm";
    protected static final String EXT_IT = ".it";
    protected static final String EXT_ZIP = ".zip";

    // and so on...

    public static ModuleLoader getModuleLoader(int type, String name,
            File modFile) throws InvalidFormatException, IOException {
        return getModuleLoader(type, name, getData(modFile));
    }

    public static ModuleLoader getModuleLoader(int type, String name, URL modURL)
            throws InvalidFormatException, IOException {
        return getModuleLoader(type, name, getData(modURL));
    }

    public static ModuleLoader getModuleLoader(URL modURL)
            throws InvalidFormatException, IOException {
        return getModuleLoader(
            getType(modURL.toExternalForm()),
            modURL.toExternalForm(),
            getData(modURL));
    }

    public static ModuleLoader getModuleLoader(File modFile)
    	throws InvalidFormatException, IOException {

        return getModuleLoader(
            getType(modFile.getAbsolutePath()),
            modFile.getAbsolutePath(),
            getData(modFile));
    }

    public static ModuleLoader getModuleLoader(String name, byte[] data)
            throws InvalidFormatException, IOException {
        return getModuleLoader(getType(name), name, data);
    }

    public static ModuleLoader getModuleLoader(int type, String name,
            byte[] data) throws InvalidFormatException, IOException {
        // Should be reimplemented using reflection (maybe with a config file).
        // Should be possible to add loaders without modifying the code.
        switch (type) {
	        case MOD:
	            return new ModLoader(name, data);
	        case S3M:
	            return new S3MLoader(name, data);
	        case STM:
	            return new STMLoader(name, data);
	        case XM:
	            return new XMLoader(name, data);
	        case IT:
	            return new ITLoader(name, data);
	        case ZIP:
	        	return new ZippedModuleLoader(name, data);
        }
        return null;
    }

    /**
     * @return the type of the module with the given name
     */
    private static int getType(String modName) {
        // maybe do more testing: id, magic number...
        if (modName.toLowerCase().endsWith(EXT_MOD))
            return MOD;
        else if (modName.toLowerCase().endsWith(EXT_S3M))
            return S3M;
        else if (modName.toLowerCase().endsWith(EXT_XM))
            return XM;
        else if (modName.toLowerCase().endsWith(EXT_STM))
            return STM;
        else if (modName.toLowerCase().endsWith(EXT_IT))
            return IT;
        else if (modName.toLowerCase().endsWith(EXT_ZIP)) {
        	return ZIP;
        }
        return UNKNOWN;
    }

    /**
     * @param url the url to the module
     * @return the raw module data
     * @throws IOException
     */
    private static byte[] getData(URL url) throws IOException {
        return getData(url.openStream());
    }

    /**
     * @param file the module file
     * @return the raw data of a module file
     * @throws IOException
     */
    private static byte[] getData(File file) throws IOException {
        if (!file.exists() || !file.isFile())
            throw new IOException("No such file: " + file.getAbsolutePath());
        return getData(new FileInputStream(file));
    }

    /**
     * @param in stream containing the module data
     * @return the raw module data
     * @throws IOException
     */
    protected static byte[] getData(InputStream in) throws IOException {
//        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[1024*100];
        int len = -1;
        while ((len = in.read(data)) != -1) {
            baos.write(data, 0, len);
        }
        baos.flush();
        return baos.toByteArray();
    }

    /**
     * @return the module loaded by a module loader.
     */
    public abstract Module getModule();

    protected static void skip(DataInputStream dis, int len) throws IOException {
        int l = len;
        while ((l -= dis.skip(l)) > 0)
            ;
    }

}