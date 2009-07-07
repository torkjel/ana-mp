/*
 * Created on Aug 11, 2004
 */
package anakata.modplay.loader;

import anakata.modplay.module.ModuleUnits;

/**
 * @author torkjel
 */
public class S3MUnits implements ModuleUnits {

    private static final String NAME = "s3m";
    
    private double c4hz; 
//    private double c4 = 12*4;

    private boolean amigaPeriods;

    private static final int MAX_PERIOD = 1712;
    private static final int MIN_PERIOD = 57;

    private static final double STANDARD_C4HZ = 8363;
    
    private double ac;
    
    public S3MUnits(int c4hz, boolean amigaPeriods) {
        this.c4hz = c4hz;
        this.amigaPeriods = amigaPeriods;
        this.ac = ModUnits.NTSC * c4hz / STANDARD_C4HZ;
    }
    
    public double note2period(double note) {
        return rate2period(note2rate(note));
    }

    public double period2note(double period) {
        return rate2note(period2rate(period));
    }
    
    public double period2rate(double period) {
        return ac / (amigaPeriods ? period*2 : (period*2));
    }

    public double rate2period(double rate) {
        return ac / (amigaPeriods ? rate*2 : (rate*2));
    }
    
    public double note2rate(double note) {
        double c0hz = c4hz / (2*2*2*2);//ModUnits.NTSC / MAX_PERIOD;
        return c0hz * Math.pow(2, note / 12);
    }

    public double rate2note(double rate) {
        double c0hz = c4hz / (2*2*2*2);//ModUnits.NTSC / (2 * MAX_PERIOD);
        return 12 * Math.log(rate / c0hz) / Math.log(2);
    }

    public double getUpperNoteLimit() {
        return period2note(MIN_PERIOD);
    }
    
    public double getLowerNoteLimit() {
        return period2note(MAX_PERIOD);
    }

    public double addPeriod(double note, double period) {
        return period2note(note2period(note) + period);
    }

    public String getName() {
        return NAME;
    }
    
    public static void main(String[] args) {
        S3MUnits u = new S3MUnits(10000, false);
        System.out.println(u.rate2note(20000));
        System.out.println(u.rate2note(10000));
        System.out.println(u.rate2note(5000));
        System.out.println();

        System.out.println(u.note2rate(12*7));
        System.out.println(u.note2rate(12*6));
        System.out.println(u.note2rate(12*5));
        System.out.println(u.note2rate(12*4));
        System.out.println(u.note2rate(12*3));
        System.out.println(u.note2rate(12*2));
        System.out.println(u.note2rate(12*1));
        System.out.println(u.note2rate(12*0));
        System.out.println();

        System.out.println(u.note2period(12*7));
        System.out.println(u.note2period(12*6));
        System.out.println(u.note2period(12*5));
        System.out.println(u.note2period(12*4));
        System.out.println(u.note2period(12*3));
        System.out.println(u.note2period(12*2));
        System.out.println(u.note2period(12*1));
        System.out.println(u.note2period(12*0));
        System.out.println();


        System.out.println(u.period2note(MAX_PERIOD));
        System.out.println(u.period2note(MIN_PERIOD));
        System.out.println(u.period2note(428));
        System.out.println();

        System.out.println(u.rate2period(20000));
        System.out.println(u.rate2period(10000));
        System.out.println(u.rate2period(5000));
        System.out.println();

        System.out.println(u.period2rate(MAX_PERIOD));
        System.out.println(u.period2rate(MIN_PERIOD));
        System.out.println(u.period2rate(428));
        System.out.println();

        System.out.println(u.rate2period(u.period2rate(MAX_PERIOD)));
        System.out.println(u.rate2period(u.period2rate(MIN_PERIOD)));
        System.out.println(u.rate2period(u.period2rate(1000)));

    }
}
