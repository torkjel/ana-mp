/*
 * Created on Dec 21, 2004
 */
package anakata.util.io;

import java.io.IOException;


/**
 * @author torkjel
 */
public class RandomAccessArray extends AbstractRandomAccess {

    private byte[] data;
    private int pos;

    public RandomAccessArray(byte[] data) {
        this.data = data;
        this.pos = 0;
    }

    public void seek(long pos) throws IOException {
        checkBounds(pos);
        this.pos = (int)pos;
    }

    public void skipBytes(int skip) throws IOException {
        checkBounds(pos+skip);
        pos += skip;
    }

    public long getPosition() {
        return pos;
    }

    private void checkBounds(long newPos) throws IOException {
        if (newPos < 0 || newPos >= data.length)
            throw new IOException("Array index out of bounds: " + newPos);
    }

    public byte readByte() throws IOException {
        int data = read();
        if (data == -1) throw new IOException("End of data");
        return (byte)data;
    }

    public short readShort() throws IOException {
        return (short)((readByte() << 8 ) | readByte());
    }

    public int readInt() throws IOException {
        return
            (readByte() << 24) |
            (readByte() << 16) |
            (readByte() << 8) |
            (readByte() << 0);
    }

    public int readUnsignedByte() throws IOException {
        return read() & 0xff;
    }

    public int read() throws IOException {
        checkBounds(pos);
        return data[pos++] & 0xff;
    }

    public void close() {
    }
}