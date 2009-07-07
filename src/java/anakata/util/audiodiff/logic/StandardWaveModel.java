/*
 * Created on Aug 23, 2004
 */
package anakata.util.audiodiff.logic;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author torkjel
 */
public class StandardWaveModel implements IWaveModel {

    private short[] data;
    private double scale = 1.0;
    private String name;
    
    public StandardWaveModel(InputStream is, String name) throws IOException {
        this.name = name;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int d = -1;
        while ((d = is.read()) != -1) {
            bos.write(d);
        }
        data = new short[bos.size()/4];
        byte[] bytes = bos.toByteArray();
        for (int n = 0; n < data.length; n++) {
            int c1 = bytes[n*4+0] + 256 * bytes[n*4+1];
            int c2 = bytes[n*4+2] + 256 * bytes[n*4+3];
            data[n] = (short)((c1 +c2) / 2);
        }
    }

    public StandardWaveModel(File file, String name) throws FileNotFoundException, IOException {
        this(new BufferedInputStream(new FileInputStream(file)),name);
    }

    public StandardWaveModel(byte[] data, String name) throws IOException {
        this(new ByteArrayInputStream(data), name);
    }

    public int getSize() {
        return data.length;
    }
    
    public int getValue(int offset) {
        return (int)(data[offset] * scale);
    }

    public int[] getSelection(int start, int end, int len) {
        int[] scaled = new int[len];
        for (int n = 0; n < len; n++) {
            double ofs = start + n*(end-start)/(double)len;
            if (ofs > data.length-2 || ofs < 0) scaled[n] = 0;
            else {
                int iofs = (int)ofs;
                double i = ofs - iofs;
                scaled[n] = (int)((data[iofs] * scale));// * (1.0 - i) + data[iofs+1] * i) * scale) ;
            }
        }
        return scaled;
    }

    public double getScale() {
        return scale;
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getName() {
        return name;
    }
}

