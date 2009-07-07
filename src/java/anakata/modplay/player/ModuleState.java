package anakata.modplay.player;

import anakata.modplay.player.effect.*;
import anakata.modplay.module.*;

/**
 * This is the heart of the player code. This represents the state of the playing
 * module
 * @author torkjel
 *
 */
public class ModuleState
{
    private Module module;
    private Mixer mixer;

    // global effects are effects that affects the way patterns are played (not
    // just single tracks) Examples are loops, jump to pattern, global volume,
    // global panning, aso...
    private GlobalEffects effects = new GlobalEffects();
    private TrackState[] trackStates;

    private int bpm;
    private int position, division, tick;
    private int positionsInModule, divisionsInPattern, ticksInDivision;

    private int patternDelay;

    // how long the module has been playing
    private int playTime;

    /**
     * Creates a ModuleState playing the supplied module and using the supplied mixer
     * @param module module to be played
     * @param mixer the mixer...
     */
    public ModuleState(Module module, Mixer mixer)
    {
        this.module = module;
        this.mixer = mixer;

        bpm = module.getInitialBpm();
        ticksInDivision = module.getInitialSpeed();
        divisionsInPattern = module.getPatternAtPos(0).getDivisions();
        positionsInModule = module.getNumberOfPositions();

        trackStates = new TrackState[module.getPatternAtPos(0).getTrackCount()];
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n] = new TrackState(this, module, mixer, n);

        // initialize the auto effects
        Instrument[] instruments = module.getInstruments();
        for (int n = 0; n < instruments.length; n++)
            if (instruments[n] != null && instruments[n].getNumberOfAutoEffects() > 0)
                for (int m = 0; m < instruments[n].getNumberOfAutoEffects(); m++)
                    instruments[n].getAutoEffects()[m].setNumberOfTracks(trackStates.length);
    }

    /**
     * play one "tick" of the module. (A tick is the smallest time interval
     * used by modules. The speed value is actually the number of ticks per
     * division, and the number of divisions played per minute is:
     * div_per_min = 24 * BPM / speed.) The state of the module is only changed
     * by effects between ticks.
     * @return true if still playing, false if end is reached
     */
    public boolean play() throws PlayerException
    {
        // first let the global effects do anything that must be done before
        // the tick is played
        for (int n = 0; n < trackStates.length; n++)
        {
            Track track = module.getPatternAtPos(position).getTrack(n);
            for (int m = 0; m < track.getNumberOfEffects(division); m++)
            {
                int effNum = track.getEffect(division, m);
                int arg1 = track.getEffectArg1(division, m);
                int arg2 = track.getEffectArg2(division, m);
                effects.preEffect(
                    this,
                    n,
                    position,
                    division,
                    tick,
                    effNum,
                    arg1,
                    arg2);
            }
        }

        // let the local effects do anything that must be done before the
        // tick is played
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n].preEffect(position, division, tick);

        // do the tick! load new instruments, notes and autoeffects, do what
        // is needed with volume and panning
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n].loadTick(position, division, tick);

        // do the global effects
        for (int n = 0; n < trackStates.length; n++)
        {
            Track track = module.getPatternAtPos(position).getTrack(n);
            for (int m = 0; m < track.getNumberOfEffects(division); m++)
            {
                int effNum = track.getEffect(division, m);
                int arg1 = track.getEffectArg1(division, m);
                int arg2 = track.getEffectArg2(division, m);
                effects.doEffect(
                    this,
                    n,
                    position,
                    division,
                    tick,
                    effNum,
                    arg1,
                    arg2);
            }
        }

        // do the local effects
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n].doEffects(position, division, tick);


        // play the tick
        double time = getTickLength(ticksInDivision, bpm);
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n].setupMixer(position, division, tick, time);

        mixer.play(time);
        playTime += time;

        // let global effects do anything that needs to be done after the tick
        for (int n = 0; n < trackStates.length; n++)
            trackStates[n].postEffects(position, division, tick);

        // let local effects to anyting that needs to be done after the tick
        for (int n = 0; n < trackStates.length; n++)
        {
            Track track = module.getPatternAtPos(position).getTrack(n);
            for (int m = 0; m < track.getNumberOfEffects(division); m++)
            {
                int effNum = track.getEffect(division, m);
                int arg1 = track.getEffectArg1(division, m);
                int arg2 = track.getEffectArg2(division, m);
                effects.postEffect(
                    this,
                    n,
                    position,
                    division,
                    tick,
                    effNum,
                    arg1,
                    arg2);
            }
        }

        // we might need to play the same tick over again, depending on the pattern
        // delay effect...
        if (tick == ticksInDivision - 1 && patternDelay > 0)
        {
            patternDelay--;
            return true;
        }
        else
            return nextTick();
    }

    /**
     * calculates the length of a tick in milliseconds
     * @param speed the speed (ticks per division)
     * @param bpm beats per minute
     * @return the length of a tick in milliseconds
     */
    private double getTickLength(int speed, int bpm)
    {
        double dpm = 24.0 * bpm / speed;
        double dps = dpm / 60.0;
        double tl = (1000 / (dps * speed));
        return tl;
    }

    /**
     * go to the next tick in the module.
     * @return true if the module is stil playing, false if the end is reached
     */
    private boolean nextTick()
    {
        if ((++tick % ticksInDivision) == 0)
        {
            tick = 0;
            // in case the next pattern has a different number of divisions
            divisionsInPattern = module.getPatternAtPos(position).getDivisions();
            if ((++division % divisionsInPattern) == 0)
            {
                division = 0;
                if ((++position % positionsInModule) == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * @return the playing module
     */
    public Module getModule()
    {
        return module;
    }

    /**
     * @return the mixer used
     */
    public Mixer getMixer()
    {
        return mixer;
    }

    /**
     * @return the current position
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * @return the number of pattern positions in the module (Not the number
     * of patterns since each pattern may be played several times)
     */
    public int getPatternsInModule()
    {
        return positionsInModule;
    }

    /**
     * @return the number of divisions in the current pattern (often 64)
     */
    public int getDivisionsInPattern()
    {
        return divisionsInPattern;
    }

    /**
     * @return the number of ticks in a division (a.k.a speed)
     */
    public int getTicksInDivision()
    {
        return ticksInDivision;
    }

    /**
     * @return the currently playing tick in the currently playing division
     */
    public int getTick()
    {
        return tick;
    }

    /**
     * @return the currently playing division
     */
    public int getDivision()
    {
        return division;
    }

    /**
     * Changes the currently playing pattern to the pattern at the given
     * position
     * @param position the position to play
     */
    public void setPosition(int position)
    {
        this.position = position;
    }

    /**
     * sets the speed (a.k.a ticksInDivision)
     * @param speed new speed of module
     */
    public void setSpeed(int speed)
    {
        ticksInDivision = speed;
    }

    /**
     * sets the beats per minute
     * @param bpm beats per minute
     */
    public void setBpm(int bpm)
    {
        this.bpm = bpm;
    }

    /**
     * jump to the given module position
     * @param position the pattern position to jump to
     * @param division the division to jump to
     * @param tick the tick to jump to
     */
    public void jump(int position, int division, int tick)
    {
        if (position < 0)
            position = 0;
        if (position >= positionsInModule)
            position = positionsInModule - 1;
        this.position = position;
        if (division < 0)
            division = 0;
        if (division >= divisionsInPattern)
            division = divisionsInPattern - 1;
        this.division = division;
        if (tick < 0)
            tick = 0;
        if (tick >= ticksInDivision)
            tick = ticksInDivision - 1;
        this.tick = tick;
    }

    /**
     * causes the current division to be played <code>delay</code> times. Any notes, or effects
     * started in this division should not be reset each time the division is played
     * @param delay the times to play the current division
     */
    public void setPatternDelay(int delay)
    {
        patternDelay = delay * ticksInDivision;
    }

    public int getPlayTime() {
        return playTime;
    }
}
