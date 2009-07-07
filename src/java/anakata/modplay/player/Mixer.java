package anakata.modplay.player;

/**
 * By implementing this interface it's possible to use a different mixer
 * than the default. A mixer mixes sound from a set of tracks (channels).
 * Use setTrack to initialize a track. The mixer is free to handle output
 * anyway it likes, but it's a good idea to use one of the supplied output
 * plugins form anakata.modplay.output.
 * @author torkjel
 */
public interface Mixer
{
    /**
     * set up a track. The track will continue to play the given sample with the given
     * parameters until the next time this method is called on the same track.
     * @param sampleData the data of the sample that should be played
     * @param offset the offset from where the sample should start playing
     * @param rate the rate the sample should be played at
     * @param volume the volume of the sample
     * @param panning the panning of the sample
     * @param loopType what kind of looping (if any) should be done on this sample
     * @param loopStart the start of the loop
     * @param loopLength the lenght of the loop
     * @param track the track there this sample should be played
     */
    void setTrack (
        short[] sampleData,
        double offset,
        double rate,
        double volume,
        double panning,
        int loopType,
        int loopStart,
        int loopLength,
        int track) throws PlayerException;

    /**
     * mix and play the indicated number of millisecounds of sound
     * @param millisecs the number of milliseconds of sound to play
     */
    void play(double millisecs) throws PlayerException;

    /**
     * @return the maximal number of tracks this mixer can handle
     */
    int getNumberOfTracks();

    /**
     * sets the amplification
     * The legal values are [0-inf]. Default value is 1.
     * If this value is set > 1 clipping may occur.
     * To avoid clipping, the the mixer must divide the volume of each track
     * by the number of tracks. This will somethimes result in too low volume.
     * This can be fixed by setting the amplification to a value > 1.0.
     * @param amp amplification
     */
    void setAmplification(double amp);

    /**
     * get the amplification
     * @return
     */
    double getAmplification();

    /**
     * sets the volume
     * The legal values are [0-1]. Default value is 1
     * @param volume
     */
    void setVolume(double volume);

    /**
     * @return the volume
     */
    double getVolume();

    /**
     * sets the balance
     * The legal values are [0-1]. Default value is 0.5
     * balance >= 0.5 gives 100% in right channel and 2*(1-balance)*100% in left channel
     * balance <= 0.5 gives 100% in left channel and 2*balance*100% in right channel
     * @param balance
     */
    void setBalance(double balance);

    /**
     * get the balance
     * @return
     */
    double getBalance();

    /**
     * sets the channel separation
     * The legal values are [0-1]. Default value is 1
     * Some module formats dictates that some tracks are played in the left
     * channel and others in the right. This often sound strange, especially when using headphones.
     * The separation is used to put the sound more "in the midle". The following formulas are used
     * to calculate the volume in the right an left channels:
     * <ul>
     * <li>volume_right = volume_right * separation + volume_left * (1-separation)</li>
     * <li>volume_left = volume_left * separation + volume_right * (1-separation)</li>
     * </ul>
     * @param separation the separation of left and right channel
     */
    void setSeparation(double separation);

    /**
     * get the channel separation
     * @return
     */
    double getSeparation();

    /**
     *  mutes a track
     *  @param mute mute if true, unmute if false
     */
    void setMute(int track, boolean mute);

    boolean isMute(int track);
}
