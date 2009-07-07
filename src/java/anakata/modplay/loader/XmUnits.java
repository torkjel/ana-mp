package anakata.modplay.loader;

import anakata.modplay.module.ModuleUnits;

/**
 * Conversions between notes, rates and periods used by .XMs
 * @author torkjel
 */
public class XmUnits implements ModuleUnits {

    private static final String NAME = "xm";

    private static double maxNote;
	private static double minNote;

	/** The maximum note value used by .XMs */
	public final static double MAX_NOTE = 96;
	/** The minimum note value used by .XMs */
	public final static double MIN_NOTE = 0;

	public XmUnits()
	{
        maxNote = MAX_NOTE;
        minNote = MIN_NOTE;
	}

	public double period2rate(double period)
	{
        return 8363 * Math.pow(2, ((6 * 12 * 16 * 4 - period) / (12 * 16 * 4)));
    }
	public double rate2period(double rate)
	{
		return
			- 12 * 16 * 4 * Math.log(rate / 8363) / Math.log(2)
			+ 6 * 12 * 16 * 4;
	}
	public double period2note(double period)
	{
        return (period - 10 * 12 * 16 * 4) / (-16 * 4);
		//	return (period-10*12*16*4-4*16)/(-2*16);
	}
	public double note2period(double note)
	{
		return 10 * 12 * 16 * 4 - note * 16 * 4;
		//	return 10*12*16*4 + 4*16 - 2*16*note;
	}
	public double note2rate(double note)
	{
		return period2rate(note2period(note));
	}
	public double rate2note(double rate)
	{
		return period2note(rate2period(rate));
	}
	public double addPeriod(double note, double period)
	{
		return period2note(note2period(note) + period);
	}
	public double getUpperNoteLimit()
	{
		return maxNote;
	}
	public double getLowerNoteLimit()
	{
		return minNote;
	}

    public String getName() {
        return NAME;
    }

}
