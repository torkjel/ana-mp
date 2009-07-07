package anakata.modplay.loader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import anakata.modplay.module.*;
import anakata.modplay.player.autoeffect.*;
import anakata.modplay.player.effect.Effect;
import anakata.util.Logger;
import anakata.util.Util;

/**
 * Makes it possible to load a module from a .XM file
 *
 * @author torkjel
 */
public class XMLoader extends ModuleLoader {
    private Module module;

    public XMLoader(String name, byte[] data) throws InvalidFormatException, IOException {
        module = load(name, data);
    }

    private Module load(String name, byte[] fileData)
        throws InvalidFormatException, IOException {

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(fileData));
        String id = Util.readZeroPaddedString(dis, 17);
        String xmname = Util.readZeroPaddedString(dis, 20);
        skip(dis,1);
        String trackerName = Util.readZeroPaddedString(dis, 20);
        int vMinor = dis.readByte();
        int vMajor = dis.readByte();
        if (vMajor != 1 || vMinor < 4)
        	throw new InvalidFormatException("Unknown version: " + vMajor + "." + vMinor);
        int headerSize = Util.readLEInt(dis);
        int numOrders = Util.readLEShort(dis);
        int restart = Util.readLEShort(dis);
        int numChannels = Util.readLEShort(dis);
        int numPatterns = Util.readLEShort(dis);
        int numInstruments = Util.readLEShort(dis);
        int flags = Util.readLEShort(dis);
        int tempo = Util.readLEShort(dis);
        if (tempo == 0) tempo = 1; // just in case...
        int BPM = Util.readLEShort(dis);
        int[] patternOrderTab = new int[numOrders];
        for (int n = 0; n < numOrders; n++)
            patternOrderTab[n] = (int) dis.readByte() & 0x0ff;
        skip(dis,256 - numOrders);

        Instrument[] instruments = loadInstruments(fileData, numInstruments);

        int highestPattern = 0;
        for (int n = 0; n < patternOrderTab.length; n++)
            if (patternOrderTab[n] > highestPattern)
                highestPattern = patternOrderTab[n];

        Pattern[] patterns = new Pattern[(int) Math.max(highestPattern + 1,
                numPatterns)];
        // not numPatterns bc not all patterns is saved to the file...
        for (int n = 0; n < numPatterns; n++)
            patterns[n] = loadPattern(dis, numChannels, instruments,n);
        for (int n = numPatterns; n <= highestPattern; n++) {
            Track[] emptyTracks = new Track[numChannels];
            for (int m = 0; m < numChannels; m++) {
                emptyTracks[m] = new Track(64);
                for (int p = 0; p < 64; p++)
                    emptyTracks[m].initDivision(p, Track.NO_INSTRUMENT,
                            Instrument.NO_NOTE, null, null, null);
            }

            patterns[n] = new Pattern(emptyTracks, 64);
        }

        double r = 1, l = 0;
        // 128 channels should do...
        double[] panning = new double[] {
                l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l,
                l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l,
                r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r,
                r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r,
                l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l,
                l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l, r, r, l, l,
                r, r, l, l, r, r, l };
        double[] pann = new double[numChannels];
        double[] volume = new double[numChannels];
        for (int n = 0; n < numChannels; n++) {
            pann[n] = panning[n];
            volume[n] = 1; // XMs don't have per track default volume, so just use 1.
        }

        return module = new Module(xmname, id, trackerName, instruments,
                patterns, patternOrderTab, restart, BPM, tempo, 1.0,
                Module.SAMPLE_PANNING, volume, pann);
    }

    /*
     * private double getRate(double note, double fineTune) { double c4 = 8363;
     * double c0 = c4 * Math.pow(2, -4); double rate = c0 * Math.pow(2, (note +
     * fineTune) / 12); return rate; }
     */

    private Instrument[] loadInstruments(byte[] fileData, int numInstruments)
            throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
                fileData));
        skip(dis,60);
        int hlen = Util.readLEInt(dis);
        skip(dis,6);
        int numPatterns = Util.readLEShort(dis);
        skip(dis,hlen - 12);
        for (int n = 0; n < numPatterns; n++) {
            skip(dis,7);
            int plen = Util.readLEShort(dis);
            skip(dis,plen);
        }

        Instrument[] instruments = new Instrument[numInstruments];

        // there at last...

        for (int n = 0; n < numInstruments; n++) {
            int instSize = Util.readLEInt(dis);
            String name = Util.readZeroPaddedString(dis, 22);
            int type = dis.readByte();
            int numSamples = Util.readLEShort(dis);
            int[] note2sample = null;
            AutoEffect[] autoEffects = null;
            if (numSamples > 0) {
                skip(dis,4); //header size...
                note2sample = new int[98];
                for (int m = 0; m < 96; m++)
                    note2sample[m] = (int) dis.readByte() & 0x0ff;
                //0 - no note, 1-96 - notes, 97 - key-off
                int[] volEnvOfs = new int[12];
                double[] volEnvVol = new double[12];
                for (int m = 0; m < 12; m++) {
                    volEnvOfs[m] = Util.readLEShort(dis);
                    volEnvVol[m] = Util.readLEShort(dis) / 64.0; // [0-64]
                }
                int[] panEnvOfs = new int[12];
                double[] panEnvPan = new double[12];
                for (int m = 0; m < 12; m++) {
                    panEnvOfs[m] = Util.readLEShort(dis);
                    panEnvPan[m] = (Util.readLEShort(dis)) / 64.0; // [0-64]
                }
                int numVolPoints = dis.readByte();
                int numPanPoints = dis.readByte();
                int vSust = dis.readByte();
                int vLoopStart = dis.readByte();
                int vLoopEnd = dis.readByte();
                int pSust = dis.readByte();
                int pLoopStart = dis.readByte();
                int pLoopEnd = dis.readByte();
                int vType = dis.readByte();
                int pType = dis.readByte();
                VolumeEnvelope volEnv = new VolumeEnvelope(volEnvOfs,
                        volEnvVol, numVolPoints, vSust, vLoopStart, vLoopEnd,
                        vType);
                PanningEnvelope panEnv = new PanningEnvelope(panEnvOfs,
                        panEnvPan, numPanPoints, pSust, pLoopStart, pLoopEnd,
                        pType);
                AutoVibrato vibrato = new AutoVibrato(dis.readByte(), dis
                        .readByte(), dis.readByte(), dis.readByte());
                // this is a problem... The docs say 65536, mikmod uses 32768
                // (which also sound more right...)
                double fade = (Util.readLEShort(dis) & 0x0ffff) / 32768.0;
                Fadeout volFade = new Fadeout(fade);
                AutoEffect xmAuto = new XmAutoEffects(volEnv, panEnv, vibrato,
                        volFade);
                autoEffects = new AutoEffect[] { xmAuto };
                skip(dis,2); // reserved
                skip(dis,instSize - 243);
            } else
                skip(dis,instSize - 29);

            Sample[] samples = null;
            if (numSamples > 0)
                samples = new Sample[numSamples];

            int[] sampleTypes = new int[numSamples];

            ModuleUnits xmUnits = new XmUnits();

            for (int m = 0; m < numSamples; m++) // sample header
            {
                int sampleLen = Util.readLEInt(dis);
                int loopStart = Util.readLEInt(dis);
                int loopLen = Util.readLEInt(dis);
                double volume = (double) dis.readByte() / 64.0; // [0-64]
                double fineTune = (double) dis.readByte() / 128.0;
                // [-128 - 127], might encounter .XMs with finetune [-16 -
                // 15]...
                int sampleType = sampleTypes[m] = dis.readByte();
                if ((sampleType & 0x03) == 0)
                    loopStart = loopLen = 0;
                int p = (dis.readByte() & 0x0ff) - 128;
                double panning = ((p / 128.0) + 1) / 2; // [0 - 255]
                int relNote = dis.readByte(); // signed
                int reserved = dis.readByte(); // reserved...
                String sampleName = Util.readZeroPaddedString(dis, 22);
                int loopType = Sample.NO_LOOP;
                if ((sampleType & 0x03) == 0 || loopLen == 0)
                    loopType = Sample.NO_LOOP;
                else if ((sampleType & 0x03) == 1)
                    loopType = Sample.FORWARD;
                else if ((sampleType & 0x03) == 2)
                    loopType = Sample.PING_PONG;
                if ((sampleType & 16) != 0) // 16 bits
                {
                    sampleLen /= 2;
                    loopStart /= 2;
                    loopLen /= 2;
                }

                //System.out.println(n+ " " +m + " " + loopType + " " +
                // sampleLen + " " + loopStart + " " + loopLen + " " +
                //relNote + " " +reserved + " " + fineTune + " " + sampleType +
                // " " + panning + " " + p);
                samples[m] = new Sample("sample-" + n + "-" + m, sampleName, volume,
						panning, sampleLen, loopType, loopStart, loopLen,
						relNote, fineTune, xmUnits);
            }
            for (int m = 0; m < numSamples; m++) // sample data
            {
                int sampleLen = samples[m].getLength();
                short[] sampleData;
                if ((sampleTypes[m] & 16) != 0) // 16 bits
                {
                    sampleData = new short[sampleLen];
                    short old = 0;
                    short newData;
                    for (int i = 0; i < sampleLen; i++) {
                        newData = (short) (old + (short) Util.readLEShort(dis));
                        sampleData[i] = newData;
                        old = newData;
                    }
                } else // 8 bits
                {
                    sampleData = new short[sampleLen];
                    int old = 0;
                    for (int i = 0; i < sampleLen; i++) {
                        short newData = (short) (old + dis.readByte() * 256);
                        sampleData[i] = newData;
                        old = newData;
                    }
                }
                samples[m].setData(sampleData);
            }
            instruments[n] = new Instrument(name, note2sample, samples,
                    autoEffects, 0.0);
        }
        return instruments;
    }

    private Pattern loadPattern(DataInputStream dis, int numChannels,
        Instrument[] instruments, int patternNum)
        throws InvalidFormatException, IOException {

        int headerLen = Util.readLEInt(dis);
        int packingType = dis.readByte();
        int numRows = Util.readLEShort(dis);
        int packedSize = Util.readLEShort(dis);

        Track[] tracks = new Track[numChannels];
        for (int n = 0; n < numChannels; n++)
            tracks[n] = new Track(numRows);

        int division = 0;
        int n = 0;

        if (packedSize == 0) {
            Track[] emptyTracks = new Track[numChannels];
            for (int m = 0; m < numChannels; m++) {
                emptyTracks[m] = new Track(64);
                for (int p = 0; p < 64; p++)
                    emptyTracks[m].initDivision(p, Track.NO_INSTRUMENT,
                            Instrument.NO_NOTE, null, null, null);
            }
            return new Pattern(emptyTracks, 64);
        }
        while (n < packedSize) {
            for (int m = 0; m < numChannels; m++) {
                int numEffects = 0;

                int note = 0, instrument = 0, volume = 0, effect = 0, effectParam = 0;
                int fst = dis.readByte();
                n++;
                if (fst >= 0) {
                    note = fst & 0x0ff;
                    instrument = (int) dis.readByte() & 0x0ff;
                    n++; //0-128 (not a signed byte)
                    volume = (int) dis.readByte() & 0x0ff;
                    n++;
                    effect = (int) dis.readByte() & 0x0ff;
                    n++;
                    effectParam = (int) dis.readByte() & 0x0ff;
                    n++;
                } else {
                    if ((fst & 1) != 0) {
                        note = (int) dis.readByte() & 0x0ff;
                        n++;
                    }
                    if ((fst & 2) != 0) {
                        instrument = (int) dis.readByte() & 0x0ff;
                        n++;
                    }
                    if ((fst & 4) != 0) {
                        volume = (int) dis.readByte() & 0x0ff;
                        n++;
                    }
                    if ((fst & 8) != 0) {
                        effect = (int) dis.readByte() & 0x0ff;
                        n++;
                    }
                    if ((fst & 16) != 0) {
                        effectParam = (int) dis.readByte() & 0x0ff;
                        n++;
                    }
                }

                boolean keyOff = false;

                if (note == 0)
                    note = Instrument.NO_NOTE;
                else if (note == 97) {
                    keyOff = true;
                    note = Instrument.NO_NOTE;
                } else
                    note--;

                if (instrument == 0)
                    instrument = Track.NO_INSTRUMENT;
                else
                    instrument--;

                if (effect != 0 || effectParam != 0)
                    numEffects++;
                if (volume >= 0x10)
                    numEffects++;
                if (keyOff)
                    numEffects++;

                int[] effects = new int[numEffects];
                int[] effectArg1 = new int[numEffects];
                int[] effectArg2 = new int[numEffects];
                int effectIndex = 0;

                if (volume >= 0x10 && volume <= 0x50) // set volume
                {
                    effects[effectIndex] = Effect.MOD_SET_VOLUME;
                    effectArg1[effectIndex] = ((volume - 0x10) >>> 4) & 0x0f;
                    effectArg2[effectIndex] = (volume - 0x10) & 0xf;
                    effectIndex++;
                } else if (volume >= 0x60 && volume <= 0x6f) // volume slide
                                                             // down
                {
                    effects[effectIndex] = Effect.MOD_VOLUME_SLIDE;
                    effectArg1[effectIndex] = 0;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (volume >= 0x70 && volume <= 0x7f) // volume slide up
                {
                    effects[effectIndex] = Effect.MOD_VOLUME_SLIDE;
                    effectArg1[effectIndex] = volume & 0x0f;
                    effectArg2[effectIndex] = 0;
                    effectIndex++;
                } else if (volume >= 0x80 && volume <= 0x8f) // fine volume down
                {
                    effects[effectIndex] = Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_DOWN;
                    effectArg1[effectIndex] = 0x0b;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (volume >= 0x90 && volume <= 0x9f) // fine volume up
                {
                    effects[effectIndex] = Effect.MOD_EXTENDED_FINE_VOLUME_SLIDE_UP;
                    effectArg1[effectIndex] = 0x0a;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (volume >= 0x0a0 && volume <= 0x0af) // set vibrato
                                                               // speed
                {
                    effects[effectIndex] = Effect.MOD_VIBRATO;
                    effectArg1[effectIndex] = volume & 0x0f;
                    effectArg2[effectIndex] = 0;
                    effectIndex++;
                } else if (volume >= 0x0b0 && volume <= 0x0bf) // set vibrato
                                                               // depth
                {
                    effects[effectIndex] = Effect.MOD_VIBRATO;
                    effectArg1[effectIndex] = 0;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (volume >= 0x0c0 && volume <= 0x0cf) // set panning
                {
                    effects[effectIndex] = Effect.MOD_PANNING;
                    effectArg1[effectIndex] = volume & 0x0f;
                    effectArg2[effectIndex] = 0;
                    effectIndex++;
                } else if (volume >= 0x0d0 && volume <= 0x0df) // slide panning
                                                               // left
                {
                    effects[effectIndex] = Effect.XM_PANNING_SLIDE;
                    effectArg1[effectIndex] = 0;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (volume >= 0x0e0 && volume <= 0x0ef) // slide panning
                                                               // right
                {
                    effects[effectIndex] = Effect.XM_PANNING_SLIDE;
                    effectArg1[effectIndex] = volume & 0x0f;
                    effectArg2[effectIndex] = 0;
                    effectIndex++;
                } else if (volume >= 0x0f0 && volume <= 0x0ff) // slide to note
                {
                    effects[effectIndex] = Effect.MOD_SLIDE_TO_NOTE;
                    effectArg1[effectIndex] = 0;
                    effectArg2[effectIndex] = volume & 0x0f;
                    effectIndex++;
                } else if (keyOff) // keyOff
                {
                    effects[effectIndex] = Effect.XM_KEY_OFF;
                    effectArg1[effectIndex] = 0;
                    effectArg2[effectIndex] = 0;
                    effectIndex++;
                }

                if (effect != 0 || effectParam != 0) {
                    int arg1 = (effectParam >>> 4) & 0x0f;
                    int arg2 = effectParam & 0x0f;
                    effects[effectIndex] = translateEffectNum(effect, arg1);
                    effectArg1[effectIndex] = arg1;
                    effectArg2[effectIndex] = arg2;
                    effectIndex++;
                }

                tracks[m].initDivision(division, instrument, note, effects,
                        effectArg1, effectArg2);
            }
            division++;
        }
        return new Pattern(tracks, numRows);
    }

    private int translateEffectNum(int effectNum, int arg1)
        throws InvalidFormatException {

        switch (effectNum) {
            case 0x00:
                return Effect.MOD_ARPEGGIO;
            case 0x01:
                return Effect.XM_SLIDE_UP;
            case 0x02:
                return Effect.XM_SLIDE_DOWN;
            case 0x03:
                return Effect.XM_SLIDE_TO_NOTE;
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
                return Effect.XM_VOLUME_SLIDE;
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
                    return Effect.XM_EXTENDED_FINE_SLIDE_UP;
                case 0x02:
                    return Effect.XM_EXTENDED_FINE_SLIDE_DOWN;
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
                    return Effect.XM_EXTENDED_FINE_VOLUME_SLIDE_UP;
                case 0x0b:
                    return Effect.XM_EXTENDED_FINE_VOLUME_SLIDE_DOWN;
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
            case 0x10:
                return Effect.XM_SET_GLOBAL_VOLUME;
            case 0x11:
                return Effect.XM_GLOBAL_VOLUME_SLIDE;
            case 0x14:
                return Effect.XM_KEY_OFF;
            case 0x15:
                return Effect.XM_SET_ENVELOPE_POSITION;
            case 0x19:
                return Effect.XM_PANNING_SLIDE;
            case 0x1b:
                return Effect.XM_MULTI_RETRIGGER_NOTE;
            case 0x1d:
                return Effect.S3M_TREMOR;
            case 0x20:
                return Effect.XM_W;
            case 0x21:
                if (arg1 == 1)
                    return Effect.XM_EXTRA_FINE_SLIDE_UP;
                else if (arg1 == 2)
                    return Effect.XM_EXTRA_FINE_SLIDE_DOWN;
        }
        Logger.warning("Bad effect number: " + effectNum);
        return Effect.NO_EFFECT;
    }

    public Module getModule() {
        return module;
    }
}