/*
 * Created on Dec 21, 2004
 */
package anakata.util.io;

import java.io.IOException;

/**
 * @author torkjel
 */
public interface RandomAccess {
    public void seek(long pos) throws IOException;
    public void skipBytes(int skip) throws IOException;
    public long getPosition() throws IOException;

    public byte readByte() throws IOException;
    public short readShort() throws IOException;
    public int readInt() throws IOException;
    public int read() throws IOException;

    public String readString(int len) throws IOException;

    /**
     * read a string field of maximum <code>maxLen</code> characters. The string may be shorter,
     * and is then assumed to be padded with zeros. Only the non-zero part is returned, but the
     * position will always be moved maxLen bytes forward.
     * @param maxLen
     * @return
     * @throws IOException
     */
    public String readZeroPaddedString(int maxLen) throws IOException;

    public int readUnsignedInt() throws IOException;

    public int readUnsignedShort() throws IOException;

    public int readUnsignedByte() throws IOException;

    public void close() throws IOException;
}
