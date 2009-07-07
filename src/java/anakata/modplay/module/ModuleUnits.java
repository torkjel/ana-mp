package anakata.modplay.module;

/**
 * Different module formats uses different relationships between notes, rates and 
 * periods. They also have different limits on maximum and minimum notes.
 * @author torkjel
 *
 */
public interface ModuleUnits
{
    /**
     * converts a period value to a note value 
     */
    public double period2note(double period);

    /** 
     * converts a note value to a period value 
     */    
    public double note2period(double period);

    /** 
     * converts a note value to a rate value 
     */
    public double note2rate(double note);

    /** 
     * converts a rate value to a note value 
     */
    public double rate2note(double note);
    
    /** 
     * adds a period value to a note value 
     */
    public double addPeriod(double note, double period);
    
    /** 
     * gets the highest note that can be played in a module 
     */
    public double getUpperNoteLimit();
    
    /** 
     * gets the lowest note that can be played in a module 
     */
    public double getLowerNoteLimit();

	String getName();
}
