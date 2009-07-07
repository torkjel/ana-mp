/*
 * Created on Aug 24, 2004
 */
package anakata.util.audiodiff.logic;

/**
 * @author torkjel
 */
public interface IWaveModel {
    public int getSize();
    public int getValue(int offset);
    public int[] getSelection(int start, int end, int len);
    public void setScale(double scale);
    public double getScale();
    public String getName();
}