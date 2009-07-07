package anakata.modplay.player.effect;

/**
 * Defines all effect numbers.
 * This is not the effect numbers found in the module file, but numbers used 
 * internally by the player. The module loader must translate form the numbers 
 * found in the module file to these numbers.
 * 
 * @author torkjel
 */
public interface Effect
{
    public static final int NO_EFFECT = -1;
    public static final int MOD_ARPEGGIO = 0; /* L */
    public static final int MOD_SLIDE_UP = 1; /* L */
    public static final int MOD_SLIDE_DOWN = 2; /* L */
    public static final int MOD_SLIDE_TO_NOTE = 3; /* L */
    public static final int MOD_VIBRATO = 4; /* L */
    public static final int MOD_SLIDE_TO_NOTE_AND_VOLUME_SLIDE = 5; /* L */
    public static final int MOD_VIBRATO_AND_VOLUME_SLIDE = 6; /* L */
    public static final int MOD_TREMOLO = 7; /* L */
    public static final int MOD_PANNING = 8; /* L */
    public static final int MOD_SET_SAMPLE_OFFSET = 9; /* L */
    public static final int MOD_VOLUME_SLIDE = 10; /* L */
    public static final int MOD_POSITION_JUMP = 11; /* G */
    public static final int MOD_SET_VOLUME = 12; /* L */
    public static final int MOD_PATTERN_BREAK = 13; /* G */
    
    public static final int MOD_SET_SPEED = 15; /* G */
    
    public static final int MOD_EXTENDED_SET_FILTER = 16; /* L */
    public static final int MOD_EXTENDED_FINE_SLIDE_UP = 17; /* L */
    public static final int MOD_EXTENDED_FINE_SLIDE_DOWN = 18; /* L */
    public static final int MOD_EXTENDED_SET_GLISSANDO = 19; /* L */
    public static final int MOD_EXTENDED_SET_VIBRATO_WAVEFORM = 20; /* L */
    public static final int MOD_EXTENDED_FINETUNE = 21; /* L */
    public static final int MOD_EXTENDED_LOOP = 22; /* G */
    public static final int MOD_EXTENDED_SET_TREMOLO_WAVEFORM = 23; /* L */
    public static final int MOD_EXTENDED_ROUGH_PANNING = 24; /* L */
    public static final int MOD_EXTENDED_RETRIGGER_SAMPLE = 25; /* L */
    public static final int MOD_EXTENDED_FINE_VOLUME_SLIDE_UP = 26; /* L */
    public static final int MOD_EXTENDED_FINE_VOLUME_SLIDE_DOWN = 27; /* L */
    public static final int MOD_EXTENDED_CUT_SAMPLE = 28; /* L */
    public static final int MOD_EXTENDED_DELAY_SAMPLE = 29; /* L */
    public static final int MOD_EXTENDED_DELAY_PATTERN = 30; /* G */
    public static final int MOD_EXTENDED_INVERT_LOOP = 31; /* L */
    
    public static final int XM_SLIDE_UP = 40; /* L */
    public static final int XM_SLIDE_DOWN = 41; /* L */
    public static final int XM_SLIDE_TO_NOTE = 42;  /* L */
    public static final int XM_VOLUME_SLIDE = 43; /* L */
    public static final int XM_EXTENDED_FINE_SLIDE_UP = 44; /* L */
    public static final int XM_EXTENDED_FINE_SLIDE_DOWN = 45; /* L */
    public static final int XM_EXTENDED_FINE_VOLUME_SLIDE_UP = 46; /* L */
    public static final int XM_EXTENDED_FINE_VOLUME_SLIDE_DOWN = 47; /* L */
    
    public static final int XM_SET_GLOBAL_VOLUME = 48; /* G */
    public static final int XM_GLOBAL_VOLUME_SLIDE = 49; /* G */
    public static final int XM_KEY_OFF = 50; /* L */
    public static final int XM_SET_ENVELOPE_POSITION = 51; /* L */
    public static final int XM_PANNING_SLIDE = 52; /* L */
    public static final int XM_MULTI_RETRIGGER_NOTE = 53; /* L */
    public static final int XM_W = 54; /* L */
    public static final int XM_EXTRA_FINE_SLIDE_UP = 55; /* L */
    public static final int XM_EXTRA_FINE_SLIDE_DOWN = 56; /* L */
    
    public static final int S3M_TREMOR = 60;
}
