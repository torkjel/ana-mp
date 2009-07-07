/*
 * Created on Dec 21, 2004
 */
package anakata.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author torkjel
 */
public class RandomAccessFile extends AbstractRandomAccess {

    private java.io.RandomAccessFile raf;

    public RandomAccessFile(File file) throws FileNotFoundException {
        this.raf = new java.io.RandomAccessFile(file,"r");
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    public void skipBytes(int skip) throws IOException {
        raf.skipBytes(skip);
    }

    public long getPosition() throws IOException {
        return raf.getFilePointer();
    }

    public byte readByte() throws IOException {
        return raf.readByte();
    }

    public short readShort() throws IOException {
        return raf.readShort();
    }

    public int readInt() throws IOException {
        return raf.readInt();
    }

    public int read() throws IOException {
        return raf.read();
    }

    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    public void close() throws IOException {
        raf.close();
    }
}
