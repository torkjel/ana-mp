package anakata.modplay.module;

/**
 * A sample. Should be generic enough to support all module formats
 * @author torkjel
 */
public class Sample
{
	private String id;
	private String name;
	private double volume;
	private double panning;
	private int length;
	private int loopType;
	private int loopStart;
	private int loopLength;
	private double relativeNote;
	private double fineTune;

	private ModuleUnits units;
    
    private short[] data;

	/** no loop */
	public static final int NO_LOOP = 0;
	/** forward loop */
	public static final int FORWARD = 1;
	/** ping-pong loop*/
	public static final int PING_PONG = 2;

	/**
	 * @param id id of sample. must be unique
	 * @param name name of sample
	 * @param volume volume of sample
	 * @param panning panning of sample (0 = far left, 0.5 = middle, 1 = far right) 
	 *  (ignored if Module.panningType != Module.SAMPLE_PANNING)
	 * @param length length of the sample data
	 * @param loopType type of loop
	 * @param loopStart start offset of loop
	 * @param loopLength length of loop
	 * @param relativeNote add relativeNote notes to the rate when playing this 
	 *  sample (can be negative)
	 * @param fineTune same function as relativeNote
	 */
	public Sample(
		String id, 
		String name,
		double volume,
		double panning,
		int length,
		int loopType,
		int loopStart,
		int loopLength,
		double relativeNote,
		double fineTune,
        ModuleUnits units)
	{
		this.id = id;
		this.name = name;
		this.volume = volume;
		if (panning < 0)
			panning = 0;
		if (panning > 1)
			panning = 1;
		this.panning = panning;
		this.length = length;
		this.loopType = loopType;
		this.loopStart = loopStart;
		this.loopLength = loopLength;
		this.relativeNote = relativeNote;
		this.fineTune = fineTune;
        this.units = units;
	}

	/**
	 * @return human readable info about this sample
	 */
	public String getInfo()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Sample name: " + name + "\n");
		sb.append("Length: " + length + "\n");
		String loop = null;
		if (loopType == NO_LOOP)
			loop = "no loop ";
		else if (loopType == FORWARD)
			loop = "forward ";
		else if (loopType == PING_PONG)
			loop = "ping pong ";
		sb.append(loop + " " + loopStart + " " + loopLength + "\n");
		return sb.toString();
	}

	/**
	 * Sets the sample data. The sample data is often not available when the 
	 * Sample object is constructed. Use this method to set it later
	 */
	public void setData(short[] data)
	{
		this.data = data;
	}

	/**
	 * @return the sample data
	 */
	public short[] getData()
	{
		return data;
	}

	/**
	 * @return the unique id of the sample
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the name of the sample
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the volume of the sample
	 */
	public double getVolume()
	{
		return volume;
	}

	/**
	 * @return the panning of the sample
	 */
	public double getPanning()
	{
		return panning;
	}

	/**
	 * @return the length of the sample
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * @return the loop type of the sample
	 */
	public int getLoopType()
	{
		return loopType;
	}

	/**
	 * @return the start position of the loop
	 */
	public int getLoopStart()
	{
		return loopStart;
	}

	/**
	 * @return the length of the samples loop
	 */
	public int getLoopLength()
	{
		return loopLength;
	}

	/**
	 * @return the relative note of the sample
	 */
	public double getRelativeNote()
	{
		return relativeNote;
	}

	/**
	 * @return the fine tune of the sample
	 */
	public double getFineTune()
	{
		return fineTune;
	}

    public void setUnits(ModuleUnits units) {
        this.units = units;
    }

    public ModuleUnits getUnits() {
        return units;
    }
}
