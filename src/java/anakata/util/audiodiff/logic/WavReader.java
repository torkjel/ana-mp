/*
 * Created on Aug 24, 2004
 */
package anakata.util.audiodiff.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import anakata.modplay.loader.InvalidFormatException;
import anakata.sound.output.WavOutput;

/**
 * @author torkjel
 */
public class WavReader {
    
    private RandomAccessFile raf;
    private int length;
    private int channels;
    private int bits;
    private int rate;
    
    public static void main(String[] args) {
        try {
            new WavReader(new File(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public WavReader(File file) 
        throws IOException, FileNotFoundException, InvalidFormatException  {
        
        raf = new RandomAccessFile(file, "r");

        String riff_magic = readString(raf,WavOutput.RIFF_MAGIC.length()); 
        
        if (!WavOutput.RIFF_MAGIC.equals(riff_magic))
            throw new InvalidFormatException(
                "\"" + WavOutput.RIFF_MAGIC + "\" not found"); 
        
        int len = readInt(raf);

        String wave_magic = readString(raf,WavOutput.WAVE_MAGIC.length());
        if (!wave_magic.equals(WavOutput.WAVE_MAGIC))
            throw new InvalidFormatException(
                "\"" + WavOutput.WAVE_MAGIC + "\" not found");
        
        String frmt = readString(raf,WavOutput.FORMAT_MAGIC.length());
        int frmt_length = readInt(raf);
        int more_magic = readShort(raf);
        channels = readShort(raf);
        rate = readInt(raf);
        int bytes_sec = readInt(raf);
        int bytes_samp = readShort(raf);
        bits = readShort(raf);
        String data_magic = readString(raf,WavOutput.DATA_MAGIC.length()); 
        length = readInt(raf);
        
        System.out.println("File: " + file.getPath());
        System.out.println("Riff: " + riff_magic);
        System.out.println("Length: " + len);
        System.out.println("Wave: " + wave_magic);
        System.out.println(frmt);
        System.out.println(frmt_length);
        System.out.println(more_magic);
        System.out.println("Channel: " + channels);
        System.out.println("Rate: " + rate);
        System.out.println("Bytes per sec: " + bytes_sec);
        System.out.println("Bytes per sample: " + bytes_samp);
        System.out.println("Bits per sample: " + bits);
        System.out.println("Data: " + data_magic);
        System.out.println("Length: " + length);
    }

    private String readString(RandomAccessFile raf, int len) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < len; n++)
            sb.append((char)raf.read());
        return sb.toString();
    }
    
    private int readInt(RandomAccessFile raf) throws IOException {
        int val = 0;
        val |= (raf.read() & 0x0ff);
        val |= (raf.read() & 0x0ff) << 8;
        val |= (raf.read() & 0x0ff) << 16;
        val |= (raf.read() & 0x0ff) << 24;
        return val;
    }

    private int readShort(RandomAccessFile raf) throws IOException {
        int val = 0;
        val |= (raf.read() & 0x0ff);
        val |= (raf.read() & 0x0ff) << 8;
        return val;
    }

    public InputStream getInputStream() {
        return new InputStream() {
            public int read() throws IOException {
                return raf.read();
            }
        };
    }

    public int getLength() {
        return length;
    }

    public int getChannels() {
        return channels;
    }
    
    public int getBits() {
        return bits;
    }
}
