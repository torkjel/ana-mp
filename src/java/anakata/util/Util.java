package anakata.util;

import java.io.*;

public abstract class Util {

    public static String readZeroPaddedString(DataInputStream dis, int length)
            throws IOException {
        byte[] data = new byte[length];
        int nLen = dis.read(data);
        if (nLen != length)
            throw new IOException("can't read zeropaddedstring");
        int len = 0;
        while (len < length && data[len] != 0)
            len++;
        return new String(data, 0, len);
    }

    public static String nibbleToHex(int nibble) {
        nibble &= 0x0f;
        return nibble < 10 ?
            nibble + "" :
            ((char)('A' + nibble - 10)) + "";
    }

    public static int readLEShort(DataInputStream dis) throws IOException {
        return
            ((int) dis.readByte() & 0x0ff) +
            ((int) dis.readByte() & 0x0ff) * 256;
    }

    public static int readLEInt(DataInputStream dis) throws IOException {
        return
            ((int) dis.readByte() & 0x0ff) +
            ((int) dis.readByte() & 0x0ff) * 256 +
            ((int) dis.readByte() & 0x0ff) * 256 * 256 +
            ((int) dis.readByte() & 0x0ff) * 256 * 256 * 256;
    }

    public static void exit(int code) {
        System.exit(code);
    }

}
