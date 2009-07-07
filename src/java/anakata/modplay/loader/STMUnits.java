/*
 * Created on Aug 11, 2004
 */
package anakata.modplay.loader;

import anakata.modplay.module.ModuleUnits;

/**
 * @author torkjel
 * STM actually has a sane way of specifying the sample rate / note 
 * relationship. The sample decides what rate the sample should be played at 
 * when played as a c-3 note. The rest follows from that, by fundamental note 
 * theory. (The rate of one note is 2^(1/12) times higher than the previous one)
 */
public class STMUnits implements ModuleUnits {

    private static final String NAME = "stm";
    
    private double c3hz; 
    private static final double root2_12 = 1.059463094; 
    private static final double c3 = 12*4;
    
    public STMUnits(int c3hz) {
        this.c3hz = c3hz;
    }
    
    public double period2note(double period) {
        return rate2note(period);
    }

    public double note2period(double period) {
        return note2rate(period);
    }
    
    public double note2rate(double note) {
        double diff = note - c3;
        return c3hz * Math.pow(root2_12,diff); 
    }

    public double rate2note(double rate) {
        return Math.log(rate/c3hz)/Math.log(root2_12) + c3;
    }

    public double addPeriod(double note, double period) {
        return period2note(note2period(note) + period); 
    }

    public double getUpperNoteLimit() {
        return 10*12;
    }
    
    public double getLowerNoteLimit() {
        return 0;
    }

    public String getName() {
        return NAME;
    }
}
