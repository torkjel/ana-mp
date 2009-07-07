package anakata.modplay.module;

/**
 * A pattern contains a number of tracks, each with the same number of divisions
 * @author torkjel
 */
public class Pattern
{
	private Track[] tracks;
	private int divisions;

	/**
	 * @param tracks
	 * @param divisions
	 */
	public Pattern(Track[] tracks, int divisions)
	{
		this.tracks = tracks;
		this.divisions = divisions;
	}

	/**
*	 * @return the n'th track
	 */
	public Track getTrack(int n)
	{
		if (n >= tracks.length || n < 0)
			return null;
		return tracks[n];
	}

	/**
	 * @return the number of tracks in this pattern
	 */
	public int getTrackCount()
	{
		return tracks.length;
	}

	/**
	 * @return the number of divisions in this pattern
	 */
	public int getDivisions()
	{
		return divisions;
	}

	/**
	 * @return a code similar to the codes used in a tracker, representing the 
	 *  given division of this pattern 
	 */
	public String getCode(int division)
	{
		StringBuffer sb = new StringBuffer();
		for (Track t : tracks)
			sb.append(t.getInfo(division) + " ");
		return sb.toString();
	}

	/**
	 *  @return information about this pattern in human readable form
  	 */
	public String getInfo()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(
			"#tracks: " + tracks.length + " #divisions:" + divisions + "\n");
		for (int n = 0; n < divisions; n++)
		{
			for (Track t : tracks)
				sb.append(t.getInfo(n) + " ");
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}
}
