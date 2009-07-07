package anakata.modplay.loader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import anakata.modplay.module.*;
import anakata.modplay.player.effect.Effect;
import anakata.util.Logger;
import anakata.util.Util;
import anakata.util.io.RandomAccess;
import anakata.util.io.RandomAccessArray;

/**
 * Makes it possible to load a module from a .MOD file

 * @author torkjel
 */
public class ModLoader extends ModuleLoader {

	private static final int ID_OFFSET = 1080;

    /**
     * Tests the loader.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Module mod = ModuleLoader.getModuleLoader(
                ModuleLoader.MOD, args[0], new File(args[0])).getModule();
            Logger.info(mod.getInfo() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Module module;

    /**
     * loads a module from a .MOD file
     *
     * @param name the name of the module
     * @param data the module data
     */
    public ModLoader(String name, byte[] data)
        throws InvalidFormatException, IOException {

        module = load(name, data);
    }

    /**
     * does all the dirty work :)
     *
     * @param name
     * @return a module object
     * @throws IOException
     */
    private Module load(String name, byte[] data)
        throws InvalidFormatException, IOException {

        RandomAccess raf = new RandomAccessArray(data);

        int numberOfInstruments;
        int tracks;
        String tracker = "";

        raf.seek(ID_OFFSET);
        String id = readID(raf);

        IDInfo idInfo = getIdInfo(id);
        tracker = idInfo.tracker;
        id = idInfo.id;
        tracks = idInfo.trackCount;
        numberOfInstruments = idInfo.instrumentCount;

        raf.seek(0);
        String modName = readName(raf);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        skip(dis,20);

        Instrument[] instruments = loadInstruments(dis, numberOfInstruments);

        int numPos = (int) dis.readByte() & 0x0ff;
        int restartPos = dis.readByte();

        raf.seek(20 + numberOfInstruments*30 + 2);
        int[] patternOrder = loadPositions(numPos,raf);
        int numPatterns = getPatternCount(patternOrder);

        skip(dis,128);

        if (!id.equals(IDInfo.ID_NONE))
            skip(dis,4);

        Pattern[] patterns = loadPatterns(dis, numPatterns, tracks,
                numberOfInstruments, new ModUnits(ModUnits.PAL, false));

        loadSampleData(dis, instruments);

        double r = 0.0, l = 1.0;
        // 64 channels should do...
        double[] panning = new double[] {
                l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l,
                l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l,
                r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r,
                r, l, l, r, r, l };
        double[] pann = new double[tracks];
        double[] vol = new double[tracks];
        for (int n = 0; n < tracks; n++) {
            pann[n] = panning[n];
            vol[n] = 1.0; // MODs don't have per track default volume, so just use 1.
        }

        return new Module(modName, id, tracker, instruments, patterns,
                patternOrder, restartPos, 125, 6, 1, Module.TRACK_PANNING,
                vol, pann);
    }

    /**
     * loads sample data...
     *
     * @param dis
     * @param instruments
     * @throws IOException
     */
    private void loadSampleData(DataInputStream dis, Instrument[] instruments)
            throws IOException {
    	for (Instrument instrument : instruments) {
            Sample sample = instrument.getSampleByNum(0);
            short[] data = new short[sample.getLength()];
            for (int m = 0; m < data.length; m++)
                data[m] = (short) (((int) dis.readByte() & 0x0ff) << 8);
            sample.setData(data);
        }
    }

    /**
     * loads instruments...
     *
     * @param dis
     * @param numberOfInstruments
     * @return an array of instruments
     * @throws IOException
     */
    private Instrument[] loadInstruments(DataInputStream dis,
            int numberOfInstruments) throws IOException {
        Instrument[] instruments = new Instrument[numberOfInstruments];
        Sample[] samples = new Sample[numberOfInstruments];

        ModuleUnits moduleUnits = new ModUnits(ModUnits.PAL,false);

        for (int n = 0; n < numberOfInstruments; n++) {
            String name = Util.readZeroPaddedString(dis, 22);
            int length = 2 * (((int) dis.readShort()) & 0x0ffff); // -> unsigned
            int fineTune = dis.readByte();
            if (fineTune > 7)
                fineTune = fineTune | 0x0fffffff0;
            //if nibble is singed -> extend sign
            double fTune = (double) fineTune / 8; // [-8 - 7]
            double volume = (double) dis.readByte() / 64;
            int loopStart = 2 * (((int) dis.readShort()) & 0x0ffff);
            // -> unsigned
            int loopLength = 2 * (((int) dis.readShort()) & 0x0ffff);
            // -> unsigned
            int loopType;
            if (loopLength > 2)
                loopType = Sample.FORWARD;
            else
                loopType = Sample.NO_LOOP;
            //		System.out.println(name + " " + n + " " + volume + " " + loopType
            // + " " + loopStart + " " + loopLength + " " + fTune + " " +
            // fineTune);
            samples[n] = new Sample("sample-" + n, name, volume, 0.0, length, loopType,
                    loopStart, loopLength, 0.0, fTune, moduleUnits);
        }
        int[] noteToSample = new int[5 * 12]; // 5 octaves...
        for (int n = 0; n < numberOfInstruments; n++)
            instruments[n] = new Instrument(samples[n].getName(), noteToSample,
                    new Sample[] { samples[n] }, null, 0);
        return instruments;
    }

    /**
     * loads patterns...
     *
     * @param dis
     * @param numPatterns
     * @param numTracks
     * @param numSamples
     * @param modUnits
     * @return an array of patterns
     * @throws IOException
     */
    private Pattern[] loadPatterns(DataInputStream dis, int numPatterns,
        int numTracks, int numSamples, ModuleUnits modUnits)
        throws InvalidFormatException, IOException {

        Pattern[] patterns = new Pattern[numPatterns];
        for (int p = 0; p < numPatterns; p++) {
            Track[] tracks = new Track[numTracks];
            for (int n = 0; n < numTracks; n++)
                tracks[n] = new Track(64);
            for (int n = 0; n < 64; n++)
                for (int m = 0; m < numTracks; m++) {
                    int data = dis.readInt();
                    int effectNum = (data & 0x000000f00) >> 8;
                    int effectArg1 = (data & 0x0000000f0) >>> 4;
                    int effectArg2 = data & 0x00000000f;
                    int period = (data & 0x00fff0000) >> 16;
                    int note;
                    effectNum = translateEffectNum(effectNum, effectArg1);
                    if (period > ModUnits.NEW_MAX_PERIOD)
                        period = (int) ModUnits.NEW_MAX_PERIOD;
                    else if (period > 0 && period < ModUnits.NEW_MIN_PERIOD)
                        period = (int) ModUnits.NEW_MIN_PERIOD;
                    if (period != 0)
                        //just casting to int will make half the notes one note
                        // to low
                        note = (int) Math.round(modUnits.period2note(period));
                    else
                        note = Instrument.NO_NOTE;
                    int sampleNum = ((data & 0x0f0000000) >>> 24)
                            | ((data & 0x00000f000) >> 12);
                    sampleNum--;
                    if (sampleNum >= numSamples || sampleNum < 0)
                        sampleNum = Track.NO_INSTRUMENT;

                    tracks[m].initDivision(n, sampleNum, note,
                            new int[] { effectNum }, new int[] { effectArg1 },
                            new int[] { effectArg2 });
                }
            patterns[p] = new Pattern(tracks, 64);
        }
        return patterns;
    }

    /**
     * @return the module loaded by this loader
     */
    public Module getModule() {
        return module;
    }

    /**
     * translates effect numbers used by the the .MOD format to effect numbers
     * used by ana-mp.
     *
     * @param effectNum
     * @param arg1 is needed for extended effects
     * @return effect number used by ana-mp
     */
    public static int translateEffectNum(int effectNum, int arg1)
        throws InvalidFormatException {

        switch (effectNum) {
        case 0x00:
            return Effect.MOD_ARPEGGIO;
        case 0x01:
            return Effect.MOD_SLIDE_UP;
        case 0x02:
            return Effect.MOD_SLIDE_DOWN;
        case 0x03:
            return Effect.MOD_SLIDE_TO_NOTE;
        case 0x04:
            return Effect.MOD_VIBRATO;
        case 0x05:
            return Effect.MOD_SLIDE_TO_NOTE_AND_VOLUME_SLIDE;
        case 0x06:
            return Effect.MOD_VIBRATO_AND_VOLUME_SLIDE;
        case 0x07:
            return Effect.MOD_TREMOLO;
        case 0x08:
            return Effect.MOD_PANNING;
        case 0x09:
            return Effect.MOD_SET_SAMPLE_OFFSET;
        case 0x0a:
            return Effect.MOD_VOLUME_SLIDE;
        case 0x0b:
            return Effect.MOD_POSITION_JUMP;
        case 0x0c:
            return Effect.MOD_SET_VOLUME;
        case 0x0d:
            return Effect.MOD_PATTERN_BREAK;
        case 0x0e:
            switch (arg1) {
            case 0x00:
                return Effect.MOD_EXTENDED_SET_FILTER;
            case 0x01:
                return Effect.MOD_EXTENDED_FINE_SLIDE_UP;
            case 0x02:
                return Effect.MOD_EXTENDED_FINE_SLIDE_DOWN;
            case 0x03:
                return Effect.MOD_EXTENDED_SET_GLISSANDO;
            case 0x04:
                return Effect.MOD_EXTENDED_SET_VIBRATO_WAVEFORM;
            case 0x05:
                return Effect.MOD_EXTENDED_FINETUNE;
            case 0x06:
                return Effect.MOD_EXTENDED_LOOP;
            case 0x07:
                return Effect.MOD_EXTENDED_SET_TREMOLO_WAVEFORM;
            case 0x08:
                return Effect.MOD_EXTENDED_ROUGH_PANNING;
            case 0x09:
                return Effect.MOD_EXTENDED_RETRIGGER_SAMPLE;
            case 0x0a:
                return Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_UP;
            case 0x0b:
                return Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_DOWN;
            case 0x0c:
                return Effect.MOD_EXTENDED_CUT_SAMPLE;
            case 0x0d:
                return Effect.MOD_EXTENDED_DELAY_SAMPLE;
            case 0x0e:
                return Effect.MOD_EXTENDED_DELAY_PATTERN;
            case 0x0f:
                return Effect.MOD_EXTENDED_INVERT_LOOP;
            }
            break;
        case 0x0f:
            return Effect.MOD_SET_SPEED;
        }
        throw new InvalidFormatException(
            "Illegal effect number: " + effectNum + ":" + arg1);
    }

    public static IDInfo getIdInfo(String id) {
    	return IDInfo.getIDInfo(id);
    }

    public static class IDInfo {

    	public static final String PROTRACKER = "ProTracker";
    	public static final String FASTTRACKER_2 = "FastTracker 2";
    	public static final String NOISETRACKER = "NoiseTracker";
    	public static final String STARTREKKER = "StarTrekker";
    	public static final String UNKNOWN_TRACKER = "Unknown";
    	public static final String SOUNDTRACKER = "SoundTracker";
    	public static final String TAKETRACKER = "TakeTracker";
    	public static final String OCTALYZER = "Octalyzer";
    	public static final String OKTALYZER = "Oktalyzer";

    	public static final String ID_MK = "M.K.";
    	public static final String ID_MK_EXT = "M!K!";
    	public static final String ID_NT = "N.T.";
    	public static final String ID_FLT4 = "FLT4";
    	public static final String ID_FLT8 = "FLT8";
    	public static final String ID_2CHN = "2CHN";
    	public static final String ID_4CHN = "4CHN";
    	public static final String ID_6CHN = "6CHN";
    	public static final String ID_8CHN = "8CHN";
    	public static final String ID_OKTA = "OKTA";
    	public static final String ID_CD81 = "CD81";
    	public static final String ID_OCTA = "OCTA";
    	public static final String ID_POSTFIX_CH = "CH";
    	public static final String ID_PREFIX_TDZ = "TDZ";

    	// used for mudules that has no recognizable id. That should only be the case for
    	// the original 15-instrument/4-channel soundtracker format.
    	public static final String ID_NONE = "NOID";

    	public String tracker;
    	public String id;
    	public int trackCount;
    	public int instrumentCount;

    	public static IDInfo getIDInfo(String id) {
            String tracker = null;
            int trackCount = -1;
            int instrumentCount = -1;

            if (id.equals(ID_2CHN)) {
                tracker = FASTTRACKER_2;
                trackCount = 2;
                instrumentCount = 31;
            } else if (id.equals(ID_MK) || id.equals(ID_MK_EXT)) {
                tracker = PROTRACKER;
                trackCount = 4;
                instrumentCount = 31;
            } else if (id.equals(ID_NT)) {
                tracker = NOISETRACKER;
                trackCount = 4;
                instrumentCount = 31;
            } else if (id.equals(ID_FLT4)) {
                tracker = STARTREKKER;
                trackCount = 4;
                instrumentCount = 31;
            } else if (id.equals(ID_4CHN)) {
            	// afaik, fasttracker uses "M.K." for 4-channel mods, so "4CHN" is labled as
            	// "unknown"... never seen one of these anyway.
                tracker = UNKNOWN_TRACKER;
                trackCount = 4;
                instrumentCount = 31;
            } else if (id.equals(ID_6CHN)) {
                tracker = FASTTRACKER_2;
                trackCount = 6;
                instrumentCount = 31;
            } else if (id.equals(ID_8CHN)) {
                tracker = FASTTRACKER_2;
                trackCount = 8;
                instrumentCount = 31;
            } else if (id.equals(ID_FLT8)) {
            	// this is not going to work, as 8-channel strartrecker mods has some weird
            	// track-layout... (and it's probably using midi anyway...)
            	tracker = STARTREKKER;
                trackCount = 8;
                instrumentCount = 31;
            } else if (id.equals(ID_OKTA) || id.equals(ID_CD81)) {
                tracker = OKTALYZER;
                trackCount = 8;
                instrumentCount = 31;
            } else if (id.equals(ID_OCTA)) {
                tracker = OCTALYZER;
                trackCount = 8;
                instrumentCount = 31;
            } else if (id.endsWith(ID_POSTFIX_CH)) {
                trackCount = (short) Integer.parseInt(id.substring(0, 2));
                // fasttracker is more common that taketracker, so assume ft if track-count is even
                // only taketracker support odd track-counts(?)
                if ((trackCount & 1) == 0)
                    tracker = FASTTRACKER_2;
                else
                    tracker = TAKETRACKER;
                instrumentCount = 31;
            } else if (id.startsWith(ID_PREFIX_TDZ)) {
                tracker = TAKETRACKER;
                trackCount = (short) Integer.parseInt(id.substring(3, 1));
                instrumentCount = 31;
            } else {
                id = ID_NONE;
                tracker = SOUNDTRACKER;
                trackCount = 4;
                instrumentCount = 15;
            }

            IDInfo info = new IDInfo();
            info.trackCount = trackCount;
            info.instrumentCount = instrumentCount;
            info.tracker = tracker;
            info.id = id;
            return info;
    	}
    }

    public static String readID(RandomAccess raf) throws IOException {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        while (count++ < 4)
            sb.append((char)raf.read());
        return sb.toString();
    }

    public static String readName(RandomAccess raf) throws IOException {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        int data = -1;
        while (count++ < 20 && ((data = raf.read()) != 0))
            sb.append((char)data);
        raf.skipBytes(20-count);
        return sb.toString();
    }

    public static int[] loadPositions(int numPos, RandomAccess raf)
        throws IOException {

        int[] patternOrder = new int[numPos];
        for (int n = 0; n < numPos; n++)
            patternOrder[n] = raf.readByte() & 0x0ff;
        return patternOrder;
    }

    public static int getPatternCount(int[] positions) {
        int numPatterns = 0;
        for (int n = 0; n < positions.length; n++) {
            if (numPatterns < positions[n])
                numPatterns = positions[n];
        }
        return ++numPatterns;
    }
}