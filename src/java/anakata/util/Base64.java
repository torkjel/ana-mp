/*
 * Created on Apr 6, 2005
 */
package anakata.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * utility class for base64 encoding/decoding
 * @author torkjel
 */
public class Base64 {


    private static final char[] CHAR_TABLE = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V',
        'W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
        's','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/', '='};

    private static final int[] CODE_TABLE = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 64, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    private static int data = 0;
    private static char[] d4 = new char[4];

    private static char[] encodeChar(int[] d, int l) {
        data = 0;
        for (int n = 0; n < l; n++)
            data |= d[n] << (16-n*8);
        d4[0] = CHAR_TABLE[(data >>> 18) & 63];
        d4[1] = CHAR_TABLE[(data >>> 12) & 63];
        d4[2] = CHAR_TABLE[l > 1 ? (data >>> 6) & 63 : 64];
        d4[3] = CHAR_TABLE[l > 2 ? data & 63 : 64];
        return d4;
    }

    private static int[] e1 = new int[1];
    private static int[] e2 = new int[2];
    private static int[] e3 = new int[3];
    private static int[] ret;

    /**
     * @param d
     * @return
     */
    private static int[] decodeInternal(int[] d) {
        data = d[3] | (d[2] << 6) | (d[1] << 12) | (d[0] << 18);
        ret = d[2] == 64 ? e1 : d[3] == 64 ? e2 : e3;
        for (int n = 0; n < ret.length; n++)
            ret[n] = (data >>> (16-(n*8))) & 0x0ff;
        return ret;
    }

    private static int[] e4 = new int[4];

    private static int[] decode(char[] d) {
        e4[0] = CODE_TABLE[d[0]];
        e4[1] = CODE_TABLE[d[1]];
        e4[2] = CODE_TABLE[d[2]];
        e4[3] = CODE_TABLE[d[3]];
        return decodeInternal(e4);
        }

    public static void main(String[] args) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringWriter sw = new StringWriter();

        char[] b1 = new char[1024*64];
        byte[] b2 = new byte[1024*64];

        if (args[0].equals("e")) {

            Reader r = getEncodingReader(System.in);
            int data = -1;
            while ((data = r.read(b1)) != -1)
                sw.write(b1,0,data);

            System.out.print(sw.toString());

        } else if (args[0].equals("d")) {

            InputStream r = getDecodingInputStream(new InputStreamReader(System.in));
            int d = -1;
            while ((d = r.read(b2)) != -1)
                baos.write(b2, 0, d);

            System.out.print(baos.toString());

        }
    }

    /**
     * get a reader that base64 encodes an inputstream
     * @param in
     * @return
     */
    public static Reader getEncodingReader(InputStream in) {
        return new Base64EncodingReader(in);
    }

    /**
     * get an inputstream that decodes a base64 encoded reader
     * @param in
     * @return
     */
    public static InputStream getDecodingInputStream(Reader in) {
        return new Base64DecodingInputStream(in);
    }

    private static class Base64EncodingReader extends Reader {

        private char[] buffer = new char[0]; // the initial value is never used, but it needs to be
                                             // initailzed because of the test for null to see if
                                             // eos has been reached
        private int ofs = 4;
        private int count = 0;

        private InputStream in;

        public Base64EncodingReader(InputStream in) {
            this.in = in;
        }

        public int read(char[] cbuf, int off, int len) throws IOException {

            if (buffer == null) return -1;

            int readLen = 0;
            for (int n = 0; n < len; n++) {

                // break lines. each line is 76 characters long
                if (count == 76) {
                    count = 0;
                    cbuf[off+n] = '\n';
                    return readLen+1;
                }
                count++;

                // convert more data?
                if (ofs == 4) {
                    buffer = readData();
                    ofs = 0;
                }

                // are we finished?
                if (buffer == null) break;

                // store data in output buffer.
                cbuf[off+n] = buffer[ofs++];
                readLen++;
            }
            return readLen-1;
        }

        public void close() throws IOException {
            in.close();
        }

        private int[] d = new int[3];

        private char[] readData() throws IOException {
            int n = 0;
            int data = -1;
            while (n < 3 && (data = in.read()) != -1)
                d[n++] = data & 0x0ff;
            return n > 0 ? encodeChar(d, n) : null;
        }
    }

    private static class Base64DecodingInputStream extends InputStream {

        private Reader in;

        private int[] data = null;
        private int ofs = 1;
        private int count = 0;

        public Base64DecodingInputStream(Reader in) {
            this.in = in;
        }

        public int read() throws IOException {
            // read more data?
            if (data == null || ofs >= data.length) {

                //each line consists of 19 4-character groups
                if (count == 19) {
                    count = 0;
                    in.read(); // throw away the newline...
                }

                // read more data
                data = readData();
                ofs = 0;
                count++;
            }
            return data == null ? -1 : data[ofs++] & 0x0ff;
        }

        public void close() throws IOException {
            in.close();
        }

        private char[] buff = new char[4];

        private int[] readData() throws IOException {
            int len = -1;
            len = in.read(buff);
            return len == -1 ? null : decode(buff);
        }
    }
}