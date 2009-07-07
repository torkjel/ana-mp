package anakata.modplay.player.effect;

import anakata.modplay.player.*;
import anakata.util.Logger;

/**
 * This class handles all gloval effects. These are effects that works
 * independently of which track the are invoked in.
 * @author torkjel
 */
public class GlobalEffects implements Effect
{
    public GlobalEffects()
    {
    }

    /**
     * Do whatever needs to be done before the next tick is loaded
     * @param state
     * @param track
     * @param pattern
     * @param division
     * @param tick
     * @param effectNumber
     * @param arg1
     * @param arg2
     */
    public void preEffect(ModuleState state, int track, int pattern, int division, int tick, int effectNumber, int arg1, int arg2)
    {
    switch(effectNumber)
        {
        }
    }

    /**
     * do the effect
     * @param state
     * @param track
     * @param pattern
     * @param division
     * @param tick
     * @param effectNumber
     * @param arg1
     * @param arg2
     */
    public void doEffect(ModuleState state, int track, int pattern, int division, int tick, int effectNumber, int arg1, int arg2)
    {
    switch(effectNumber)
        {
        case MOD_SET_SPEED:
        modSetSpeed(state, tick, arg1, arg2);
        break;
        case MOD_EXTENDED_SET_FILTER:
        Logger.warning("Set filter not supported!");
        break;
        case MOD_EXTENDED_LOOP:
        modLoopPatternStart(state, tick, arg1, arg2);
        break;
        case MOD_EXTENDED_DELAY_PATTERN:
        if (tick == 0) state.setPatternDelay(arg2);
        break;
        case XM_SET_GLOBAL_VOLUME:
        if (tick == 0) state.getMixer().setVolume((arg1*16+arg2)/64.0);
        break;
        case XM_GLOBAL_VOLUME_SLIDE:
        xmGlobalVolumeSlide(state,tick,arg1,arg2);
        break;
        case XM_W:
        break;
        }
    }

    /**
     * Do whatever needs to be done after the tick is played
     * @param state
     * @param track
     * @param pattern
     * @param division
     * @param tick
     * @param effectNumber
     * @param arg1
     * @param arg2
     */
    public void postEffect(ModuleState state, int track, int pattern, int division, int tick, int effectNumber, int arg1, int arg2)
    {
    switch(effectNumber)
        {
        case MOD_POSITION_JUMP:
        modPositionJump(state, tick, arg1, arg2);
        break;
        case MOD_PATTERN_BREAK:
        modPatternBreak(state, tick, arg1, arg2);
        break;
        case MOD_EXTENDED_LOOP:
        modLoopPatternEnd(state, tick, arg1, arg2);
        break;
        }
    }

    /**
     * jump to a position/division to a
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    private void modPositionJump(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == state.getTicksInDivision()-1)
        {
        if (arg1*16+arg2 > 0)
            state.jump(arg1*16+arg2-1,state.getDivisionsInPattern()-1,state.getTicksInDivision()-1);
        else
            state.jump(0,0,state.getTicksInDivision()-1);
        }
    }

    /**
     * break to the next pattern position
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    private void modPatternBreak(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == state.getTicksInDivision()-1)
        {
        if (arg1*10+arg2 == 0)
            state.jump(state.getPosition(),state.getDivisionsInPattern()-1,state.getTicksInDivision()-1);
        else
            state.jump(state.getPosition()+1,arg1*10+arg2-1,state.getTicksInDivision()-1);
        }
    }

    private int loopStart;
    private int patternToLoop;
    private int loopCount = -1;

    /**
     * start a pattern loop
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    //TODO:  bugs if the loop starts at the beginning of the first pattern... :(
    private void modLoopPatternStart(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == 0 && arg2 == 0)
        {
        loopStart = state.getDivision();
        patternToLoop = state.getPosition();
        }
    }

    /**
     * mark the end of a pattern loop
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    private void modLoopPatternEnd(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == state.getTicksInDivision()-1 && arg2 != 0)
        {
        // fix things if the composer hasn't set the loop start
        if (patternToLoop != state.getPosition())
            {
            patternToLoop = state.getPosition();
            loopStart = 0;
            }

        if (loopCount == -1)
            {
            loopCount = arg2-1;
            state.jump(state.getPosition(), loopStart-1, state.getTicksInDivision()-1);
            }
        else if (loopCount > 0)
            {
            loopCount--;
            state.jump(state.getPosition(), loopStart-1, state.getTicksInDivision()-1);
            }
        else
            loopCount = -1;
        }
    }

    private double globalVolumeSlideSpeed;

    /**
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    private void xmGlobalVolumeSlide(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == 0)
        {
        if (arg1 != 0)
            globalVolumeSlideSpeed = arg1/64.0;
        else if (arg2 != 0)
            globalVolumeSlideSpeed = -arg2/64.0;
        }
    if (tick > 0)
        {
        double vol = state.getMixer().getVolume();
        vol += globalVolumeSlideSpeed;
        state.getMixer().setVolume(vol);
        }
    }

    /**
     * @param state
     * @param tick
     * @param arg1
     * @param arg2
     */
    private void modSetSpeed(ModuleState state, int tick, int arg1, int arg2)
    {
    if (tick == 0)
        {
        int z = arg1*16+arg2;
        if (z == 0) z = 1;
        if (z <= 32) state.setSpeed(z);
        else state.setBpm(z);
        }
    }
}
