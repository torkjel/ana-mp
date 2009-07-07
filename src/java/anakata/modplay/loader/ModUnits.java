package anakata.modplay.loader;

import anakata.modplay.module.ModuleUnits;

/**
 * Conversions between notes, rates and periods used by .MODs
 * 
 * @author torkjel
 *  
 */
public class ModUnits implements ModuleUnits {

	public static final String NAME = "mod";

    private static double maxNote;

    private static double minNote;

    private static double minPeriod;

    private static double maxPeriod;

    private static double ac;

    /** The traditional maximum period */
    public static final double TRADITIONAL_MAX_PERIOD = 856;

    /** The traditional minimum period */
    public static final double TRADITIONAL_MIN_PERIOD = 113;

    /** The "new" maximum period */
    public static final double NEW_MAX_PERIOD = 1712;

    /** The "new" minimum period */
    public static final double NEW_MIN_PERIOD = 57;

    /** the clock frequency used by PAL amiga machines */
    public static final double PAL = 7093789.2;

    /** the clock frequency used by NTSC amiga machines */
    public static final double NTSC = 7159090.5;

    /**
     * @param amigaClock
     *            the clock-frequency of the machine where the module was
     *            created
     * @param traditional
     *            if true: use traditional octaves 1 - 3, if false: use octaves
     *            0 - 4
     */
    public ModUnits(double amigaClock, boolean traditional) {
        ac = amigaClock;
        if (traditional) {
            minPeriod = TRADITIONAL_MIN_PERIOD;
            maxPeriod = TRADITIONAL_MAX_PERIOD;
            maxNote = period2note(minPeriod);
            minNote = period2note(maxPeriod);
        } else {
            minPeriod = NEW_MIN_PERIOD;
            maxPeriod = NEW_MAX_PERIOD;
            maxNote = period2note(minPeriod);
            minNote = period2note(maxPeriod);
        }
    }

    public double period2rate(double period) {
        return ac / (2 * period);
    }

    public double rate2period(double rate) {
        return ac / (2 * rate);
    }

    public double period2note(double period) {
        return rate2note(period2rate(period));
    }

    public double note2period(double note) {
        return rate2period(note2rate(note));
    }

    public double note2rate(double note) {
        double c0rate = ac / (2 * maxPeriod);
        return c0rate * Math.pow(2, note / 12);
    }

    public double rate2note(double rate) {
        double c0rate = ac / (2 * maxPeriod);
        return 12 * Math.log(rate / c0rate) / Math.log(2);
    }

    public double addPeriod(double note, double period) {
        return period2note(note2period(note) + period);
    }

    public double getUpperNoteLimit() {
        return maxNote;
    }

    public double getLowerNoteLimit() {
        return minNote;
    }

	public double getAmigaClock() {
		return ac;
	}
	
	public String getName() {
		return NAME;
	}
}