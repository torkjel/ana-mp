/*
 * Created on Aug 17, 2004
 */
package anakata.modplay.loader;

import java.io.IOException;

import anakata.modplay.module.Instrument;
import anakata.modplay.module.Module;
import anakata.modplay.module.Pattern;
import anakata.modplay.module.Sample;
import anakata.modplay.module.Track;
import anakata.modplay.player.autoeffect.AutoEffect;
import anakata.modplay.player.autoeffect.Fadeout;
import anakata.modplay.player.effect.Effect;
import anakata.util.io.RandomAccess;
import anakata.util.io.RandomAccessArray;

/**
 * @author torkjel
 */
public class S3MLoader extends ModuleLoader {

    public Module module;

    public S3MLoader(String name, byte[] data)
        throws InvalidFormatException, IOException {

        RandomAccess raf = new RandomAccessArray(data);
        module = loadModule(raf);
    }

    public Module getModule() {
        return module;
    }

    private Module loadModule(RandomAccess raf)
        throws InvalidFormatException, IOException {

        String moduleName = readModuleName(raf);

        verifyID(raf);
        verifyFileType(raf);

        raf.skipBytes(2); // reserved

        int numOrders = readWord(raf);
        int numInstruments = readWord(raf);
        int numPatterns = readWord(raf);

        int flags = readWord(raf);

        boolean st2Vibrato = (flags & 1) != 0;
        boolean st2Tempo = (flags & 2) != 0;
        boolean amigaSlides = (flags & 4) != 0;
        boolean volumeOptimize = (flags & 8) != 0;
        boolean amigaLimits = (flags & 16) != 0;
        boolean enableFilters = (flags & 32) != 0;

        int trackerVersion = readWord(raf) & 0x0fff;
        int formatVersion = raf.read();

        raf.skipBytes(1);

        String id = ModLoader.readID(raf);

        int globalVolume = raf.read();
        int initialSpeed = raf.read();
        int initialTempo = raf.read();
        int masterVolume = raf.read() & 127;
        int gusTickRemoval = raf.read();
        boolean readTrackPanning = raf.read() == 252;

        raf.skipBytes(10);

        int [] channelSettings = new int[32];
        for (int n = 0; n  < 32; n++)
            channelSettings[n] = raf.read();

        int[] orders = new int[numOrders];
        for (int n = 0; n  < numOrders; n++)
            orders[n] = raf.read();

        for (int n = 0; n < numOrders; n++) {
            if (orders[n] > numPatterns)
                orders[n] = numPatterns;
        }

        Instrument[] instruments = new Instrument[numInstruments];

        for (int n = 0; n < numInstruments; n++)
            instruments[n] = loadInstrument(raf,n);

        Pattern[] patterns = new Pattern[numPatterns+1];

        for (int n = 0; n < numPatterns; n++)
            patterns[n] = loadPattern(raf);
        patterns[numPatterns] = new Pattern(loadEmptyTracks(32),64);

        // TODO: load panning settings if present
        double l = 3.0/16.0, r = 12.0/16.0;
        double[] panning = new double[32];
        double[] volume = new double[32];
        for (int n = 0; n < channelSettings.length; n++) {
            int channelSetting = channelSettings[n];
            if (channelSetting == 255 || channelSetting == 127) // unused or disabled
                panning[n] = 0.5;
            else {
                int pan = channelSetting & 0x07f;
                if (pan >= 16)
                    throw new InvalidFormatException("Adlib channels not supported: " + pan);
                panning[n] = pan < 8 ? l : r;
            }
            volume[n] = 1; // S3Ms don't have per track default volume, so just use 1.
        }

        module = new Module(moduleName,id,
            "ScreamTracker " + parseTrackerVersion(trackerVersion),
            instruments, patterns, orders, 0,initialTempo, initialSpeed, 1,
            Module.TRACK_PANNING, volume, panning);

        return module;
    }

    public static String parseTrackerVersion(int version) {
        return ((version >> 8) & 0x0f) + "." + (version & 0x0ff);

    }

    private Track[] loadEmptyTracks(int numtracks) {
        Track[] tracks = new Track[numtracks];
        for (int n = 0; n < numtracks; n++) {
            tracks[n] = new Track(64);
            for (int d = 0; d < 64; d++)
                tracks[n].initDivision(d,Track.NO_INSTRUMENT, Instrument.NO_NOTE,null,null,null);
        }
        return tracks;
    }

    private Pattern loadPattern(RandomAccess raf) throws IOException {
        int patternOfs = readWord(raf);

        long backupOfs = raf.getPosition();

        raf.seek(patternOfs*16);

        Track[] tracks = loadEmptyTracks(32);

        int len = readWord(raf);

        for (int n = 0; n < 64; n ++) {
            int data = -1;
            while ((data = raf.read() & 0x0ff) != 0) {
                int trackNum = data & 31;
                int note = Instrument.NO_NOTE;
                int instrument = Track.NO_INSTRUMENT;
                int volume = -1;
                int command = -1;
                int arg1 = 0;
                int arg2 = 0;
                boolean keyOff = false;

                if ((data & 32) != 0) {
                    note = raf.read() & 0x0ff;
                    if (note == 255)
                        note = Instrument.NO_NOTE;
                    else if (note == 254) {
                        keyOff = true;
                        note = Instrument.NO_NOTE;
                    } else {
                        int nt = note & 15;
                        int oc = note >>> 4;
                        note = nt + oc*12;
                    }
                    instrument = (raf.read() & 0x0ff) - 1;
                }
                if ((data & 64) != 0) {
                    volume = raf.read()  & 0x0ff;
                }
                if ((data & 128) != 0) {
                    command = raf.read() & 0x0ff;
                    int args = raf.read() & 0x0ff;
                    arg1 = (args >>> 4);
                    arg2 = args & 15;
                }

                int effNum = 0;
                if (command != -1) effNum++;
                if (volume != -1) effNum++;
                if (keyOff) effNum++;

                int[] eff = new int[effNum];
                int[] a1 = new int[effNum];
                int[] a2 = new int[effNum];

                int effOfs = 0;
                if (volume != -1) {
                    eff[effOfs] = Effect.MOD_SET_VOLUME;
                    a1[effOfs] = volume >>> 4;
                    a2[effOfs] = volume & 0x0f;
                    effOfs++;
                }
                if (command != -1) {
                    int[] a = translateEffect(command,arg1,arg2);
                    eff[effOfs] = a[0];
                    a1[effOfs] = a[1];
                    a2[effOfs] = a[2];
                    effOfs++;
                }
                if (keyOff) {
                    eff[effOfs] = Effect.XM_KEY_OFF;
                    a1[effOfs] = 0;
                    a2[effOfs] = 0;
                    effOfs++;
                }

                tracks[trackNum].initDivision(n,instrument,note,eff,a1,a2);
            }
        }

        Pattern pattern = new Pattern(tracks,64);

        raf.seek(backupOfs);
        return pattern;
    }

    private boolean first = true;

    private int[] translateEffect(int effect, int arg1, int arg2) {
        switch (effect) {
            case -1:
                return new int[] {-1,0,0};
            case 1: // A
                return new int[] {Effect.MOD_SET_SPEED,arg1, arg2};
            case 2: // B
                return new int[] {Effect.MOD_POSITION_JUMP,arg1,arg2};
            case 3: // C
                return new int[] {Effect.MOD_PATTERN_BREAK,arg1,arg2};
            case 4: // D
                if (arg1 != 0x0f && arg2 != 0x0f)
                    return new int[] {Effect.MOD_VOLUME_SLIDE,arg1,arg2};
                else if (arg1 == 0x0f)
                    return new int[] {Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_DOWN,0,arg2};
                else if (arg2 == 0x0f)
                    return new int[] {Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_UP,0,arg1};
            case 5: // E
                if (arg1 < 0x0e)
                    return new int[] {Effect.XM_SLIDE_DOWN,arg1,arg2};
                else if (arg1 == 0x0e)
                    return new int[] {Effect.XM_EXTENDED_FINE_SLIDE_DOWN,0,arg2};
                else if (arg1 == 0x0f)
                    return new int[] {Effect.XM_EXTRA_FINE_SLIDE_DOWN,0,arg2};
            case 6: // F
                if (arg1 < 0x0e)
                    return new int[] {Effect.XM_SLIDE_UP,arg1,arg2};
                else if (arg1 == 0x0e)
                    return new int[] {Effect.XM_EXTENDED_FINE_SLIDE_UP,0,arg2};
                else if (arg1 == 0x0f)
                    return new int[] {Effect.XM_EXTRA_FINE_SLIDE_UP,0,arg2};
            case 7: // G
                return new int[] {Effect.XM_SLIDE_TO_NOTE,arg1,arg2};
            case 8: // H
                break; // TODO
            case 9: // I
                return new int[] {Effect.S3M_TREMOR,arg1,arg2};
            case 10: // J
            case 11: // K
            case 12: // L
            case 13: // M
            case 14: // N
            case 15: // O
            case 16: // P
            case 17: // Q
            case 18: // R
            case 19: // S
                break;
            case 20: // T
                return new int[] {Effect.MOD_SET_SPEED,arg1,arg2};
        }
        return new int[] {-1,0,0};
    }

    private Instrument loadInstrument(RandomAccess raf, int instrumentNum)
        throws InvalidFormatException, IOException {

        int instrumentOfs = readWord(raf);
        long backupOfs = raf.getPosition();

        raf.seek(instrumentOfs*16);

        int type = raf.read();
/*        if (type == 0) {
            raf.seek(backupOfs);
            return null;
        }*/

        String instrumentFileName = readInstrumentFileName(raf);

        raf.skipBytes(1);

        int sampleOffset = readWord(raf) * 16;

        int sampleLength = readWord(raf);
        raf.skipBytes(2);

        int loopBegin = readWord(raf);
        raf.skipBytes(2);

        int loopEnd = readWord(raf);
        raf.skipBytes(2);

        int defaultVolume = raf.read();

        int disk = raf.read();

        int pack = raf.read();

        int flags = raf.read();

        boolean loop = (flags & 1) != 0;
        boolean stereo = (flags & 2) != 0;
        boolean bits16 = (flags & 4) != 0;

        int c2speed = readWord(raf);
        raf.skipBytes(2);

        raf.skipBytes(4+8);

        String instrumentName = readInstrumentName(raf);

        String id = ModLoader.readID(raf);

        int[] note2sample = new int[12*100];

        Sample sample = null;
        if (type != 0) {
            sample = new Sample("sample-" + instrumentNum,
                instrumentName, defaultVolume/64.0, 0.5, sampleLength,
                loop ? Sample.FORWARD : Sample.NO_LOOP, loopBegin,
                loopEnd-loopBegin, 0, 0, new S3MUnits(c2speed,false));

            sample.setData(readSampleData(
                raf,sampleOffset, sampleLength, stereo, bits16,instrumentNum));
        }

        // a fadeout effect with a level of 1 is needed because s3m defines
        // the key-off note to cut, not fade the instrument
        AutoEffect[] autoEff = new AutoEffect[] {new Fadeout(1)};

        Instrument instrument = new Instrument(
            instrumentName, note2sample, new Sample[] {sample},
            autoEff, 0.5);

        raf.seek(backupOfs);
        return instrument;
    }

    private short[] readSampleData(
        RandomAccess raf, int offset, int length, boolean stereo, boolean bits16, int sampleNum)
        throws InvalidFormatException, IOException {

        long pos = raf.getPosition();
        raf.seek(offset);
        short[] data = null;

        if (!stereo && !bits16) {
            data = new short[length];
            for (int m = 0; m < data.length; m++) {
                int d = raf.read() & 0x0ff;
                //TODO: something wrong with sample loading. d << 8 sounds crap...
                data[m] = (short) ((d << 8) - 32768);
            }

        } else if (!stereo && bits16){
            data = new short[length*2];
            for (int m = 0; m < data.length; m++)
                data[m] =
                    (short)((raf.read() & 0x0ff) + ((raf.read() & 0x0ff)) * 256 - 32768);
        } else
            throw new InvalidFormatException("Unsupported sample format: " +
                (stereo ? "stereo" : "mono") + " " +
                (bits16 ? "16" : "8") + " bit");

        raf.seek(pos);

        return data;
    }


    private String readInstrumentFileName(RandomAccess raf) throws IOException {
        StringBuffer nameSB = new StringBuffer();
        int count = 0;
        while (count++ < 12)
            nameSB.append((char)raf.read());
        return nameSB.toString();
    }

    private String readInstrumentName(RandomAccess raf) throws IOException {
        long pos = raf.getPosition();
        StringBuffer nameSB = new StringBuffer();
        int count = 0;
        int data = 0;
        while (count++ < 28 && ((data = raf.read()) != 0))
            nameSB.append((char)data);
        raf.seek(pos+28);
        return nameSB.toString();
    }


    private String readModuleName(RandomAccess raf) throws IOException {
        StringBuffer nameSB = new StringBuffer();
        int data = -1;
        while (raf.getPosition() < 20 && ((data = raf.read()) != 0)) {
            nameSB.append((char)data);
        }
        raf.seek(28);
        return nameSB.toString();
    }


    private void verifyID(RandomAccess raf)
        throws IOException, InvalidFormatException {

        int id = raf.read();
        if (id != 0x01a)
            throw new InvalidFormatException("Id = " + id + ", 0x01A expected!");
    }

    private int readWord(RandomAccess raf) throws IOException {
        return raf.read() + 256 * raf.read();
    }

    private void verifyFileType(RandomAccess raf)
        throws IOException, InvalidFormatException {

        int type = raf.read();
        if (type != 16)
            throw new InvalidFormatException("Song type = " + type + ", 16 expected");
    }



/*    private String getNoteSymbol(int note)
    {
        if (note == Instrument.KEY_OFF)
            return "###";
        else if (note == Instrument.NO_NOTE)
            return "---";
        String code = null;
        switch (note%12)
        {
            case 0 :
                code = "C-";
                break;
            case 1 :
                code = "C#";
                break;
            case 2 :
                code = "D-";
                break;
            case 3 :
                code = "D#";
                break;
            case 4 :
                code = "E-";
                break;
            case 5 :
                code = "F-";
                break;
            case 6 :
                code = "F#";
                break;
            case 7 :
                code = "G-";
                break;
            case 8 :
                code = "G#";
                break;
            case 9 :
                code = "A-";
                break;
            case 10 :
                code = "A#";
                break;
            case 11 :
                code = "B-";
                break;
            default :
                code = "...";
                return code;
        }
        code += (note/12);
        return code;
    }
*/
/*    private String getEffectSymbol(int effect) {
        if (effect == Effect.MOD_SET_SPEED)
            return "A";
        return "-";
    }*/
}


