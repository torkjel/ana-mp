/*
 * Created on Sep 21, 2004
 */
package anakata.modplay.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.loader.ModLoader;
import anakata.modplay.loader.S3MLoader;
import anakata.modplay.loader.ModLoader.IDInfo;
import anakata.util.Logger;
import anakata.util.Util;
import anakata.util.io.RandomAccess;
import anakata.util.io.RandomAccessFile;

/**
 * @author torkjel
 */
public abstract class ModuleInfo {
    private String type;
    private String fileName;
    private String name;
    private String id;
    private String tracker;
    private int instrumentCount;
    private int trackCount;
    private int patternCount;
    private int positionCount;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        if (name.trim().length() > 0)
            return name;
        else 
            return new File(getFileName()).getName();
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setTracker(String tracker) {
        this.tracker = tracker;
    }
    public String getTracker() {
        return tracker;
    }
    public void setInstrumentCount(int instrumentCount) {
        this.instrumentCount = instrumentCount;
    }
    public int getInstrumentCount() {
        return instrumentCount;
    }
    public void setPatternCount(int patternCount) {
        this.patternCount = patternCount;
    }
    public int getPatternCount() {
        return patternCount;
    }
    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }
    public int getPositionCount() {
        return positionCount;
    }
    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
    public int getTrackCount() {
        return trackCount;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public static ModuleInfo get(String file) throws InvalidFormatException, FileNotFoundException {
        ModuleInfo info = null;
        if (file.toLowerCase().endsWith(".mod"))
            info = new ModInfo();
        else if (file.toLowerCase().endsWith(".xm"))
            info = new XMInfo();
        else if (file.toLowerCase().endsWith(".s3m"))
            info = new S3MInfo();
        info.load(file);
        return info;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ModuleInfo)) return false;
        ModuleInfo mi = (ModuleInfo)o;
        return getFileName().equals(mi.getFileName());
    }
    
    protected abstract void load(String file) throws InvalidFormatException, FileNotFoundException;

    public String toString() {
        return 
            pad("[" + getType() + "]",5,' ',true) + "  " + 
            pad(shorten(getName().trim(),25),25,' ',true) + " " + 
            pad(getPositionCount()+"",4,' ',false);
    }
    
    /*    public String toString() {
        return getClass().getName() + 
            "[" +
                "file=" + getFileName() + ", " +
                "name=" + getName() + ", " +
                "id=" + getId() + ", " +
                "tracker=" + getTracker() + ", " +
                "instrumentCount=" + getInstrumentCount() + ", " +
                "trackCount=" + getTrackCount() + ", " +
                "patternCount=" + getPatternCount() + ", " +
                "positionCount=" + getPositionCount() +
            "]";
    }*/

    public static void main(String[] args) {
        try {
            Logger.info(ModuleInfo.get(args[0]).toString());
        } catch (Exception e) {
            Logger.exception(e);
            Util.exit(1);
        }
    }

    private static String pad(String str, int len, char padding, boolean left) {
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        int count = str.length();
        while (count++ < len)
            if (left)
                sb.append(padding);
            else
                sb.insert(0,padding);
        return sb.toString();
    }

    private static String shorten(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        else return str.substring(0,maxLen-3) + "...";
    }

    protected String readStringZ(RandomAccess raf, int len) 
        throws IOException {

        int count = len;
        int data = -1;
        StringBuffer sb = new StringBuffer();
        while (count-- > 0 && (data = raf.read()) != 0) {
            sb.append((char)data);
        }
        return sb.toString();
    }

    protected int readShort(RandomAccess raf) throws IOException {
        int b2 = raf.readUnsignedByte();
        int b1 = raf.readUnsignedByte();
        return (b1 << 8) + b2;
    }
}




class ModInfo extends ModuleInfo {

    protected void load(String file) 
        throws InvalidFormatException, FileNotFoundException {

        setFileName(new File(file).getAbsolutePath());
        setType("MOD");

        RandomAccess raf = new RandomAccessFile(new File(file));
        
        try {
            raf.seek(0);
            setName(readStringZ(raf,20));
            raf.seek(1080);
            String tmpId = readStringZ(raf,4);
            IDInfo idInfo = ModLoader.getIdInfo(tmpId);
            setTracker(idInfo.tracker);
            setId(idInfo.id);
            setTrackCount(idInfo.trackCount);
            setInstrumentCount(idInfo.instrumentCount);        
            
            raf.seek(20+30*getInstrumentCount());
            setPositionCount(raf.readUnsignedByte());
            raf.skipBytes(1);
            setPatternCount(ModLoader.getPatternCount(
                ModLoader.loadPositions(getPositionCount(),raf))+1);
        } catch (IOException e) {
            throw new InvalidFormatException(
                "Could not load module: " + file, e);
        }
    }
}

class XMInfo extends ModuleInfo {

    protected void load(String file) 
        throws InvalidFormatException, FileNotFoundException {
        
        setFileName(new File(file).getAbsolutePath());
        setType("XM");

        RandomAccess raf = new RandomAccessFile(new File(file));
        try {

            setId(readStringZ(raf,17));
            setName(readStringZ(raf,20));
            System.out.println("name: " + getName());
            
            raf.seek(38);
            setTracker(readStringZ(raf,20));

            raf.seek(64);
            setPositionCount(readShort(raf));

            raf.seek(68);
            setTrackCount(readShort(raf));
            setPatternCount(readShort(raf));
            setInstrumentCount(readShort(raf));
        } catch (IOException e) {
            throw new InvalidFormatException(
                "Could not load module: " + file, e);
        }
    }
}

class S3MInfo extends ModuleInfo {
    protected void load(String file) 
        throws InvalidFormatException, FileNotFoundException {

        setFileName(new File(file).getAbsolutePath());
        setType("S3M");

        RandomAccess raf = new RandomAccessFile(new File(file));
        try {

            
            raf.seek(0);
            setName(readStringZ(raf,28));

            raf.seek(0x02c);
            setId(readStringZ(raf,4));

            raf.seek(0x028);
            setTracker("ScreamTracker " + S3MLoader.parseTrackerVersion(readShort(raf)));
            
            setTrackCount(32);

            raf.seek(0x020);
            setPositionCount(readShort(raf));
            setInstrumentCount(readShort(raf));
            setPatternCount(readShort(raf));

        } catch (IOException e) {
            throw new InvalidFormatException(
                "Could not load module: " + file, e);
        }
    }
}