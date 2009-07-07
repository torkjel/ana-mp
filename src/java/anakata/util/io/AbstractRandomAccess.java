package anakata.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractRandomAccess implements RandomAccess {

    public String readString(int len)  throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int n = 0; n < len; n++)
            baos.write(readUnsignedByte());
        return new String(baos.toByteArray());
    }

    public String readZeroPaddedString(int maxLen) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int n = 0;
        for (; n < maxLen; n++) {
            int data = readUnsignedByte();
            if (data == 0) {
                skipBytes((maxLen-1)-n);
                break;
            }
            baos.write(data);
        }
        return new String(baos.toByteArray());
    }

    public int readUnsignedShort() throws IOException {
        return readUnsignedByte() + (readUnsignedByte() << 8);
    }

    public int readUnsignedInt() throws IOException {
        return readUnsignedShort() + (readUnsignedShort() << 16);
    }

}
