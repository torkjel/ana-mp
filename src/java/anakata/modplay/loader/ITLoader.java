package anakata.modplay.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anakata.modplay.module.Instrument;
import anakata.modplay.module.Module;
import anakata.modplay.module.Pattern;
import anakata.modplay.module.Sample;
import anakata.modplay.module.Track;
import anakata.modplay.player.autoeffect.AutoEffect;
import anakata.util.io.RandomAccess;
import anakata.util.io.RandomAccessArray;

public class ITLoader extends ModuleLoader {

    private Module module;

    public ITLoader(String name, byte[] data) throws IOException, InvalidFormatException {
        load(name, new RandomAccessArray(data));
    }

    private void load(String name, RandomAccess ra) throws IOException, InvalidFormatException {
        String id = ra.readString(4);
        String modName = ra.readZeroPaddedString(26);
        ra.skipBytes(2); // pattern hilight info. irrelevant for players
        int positionCount = ra.readUnsignedShort();
        int instrumentCount = ra.readUnsignedShort();
        int sampleCount = ra.readUnsignedShort();
        int patternCount = ra.readUnsignedShort();

        int trackerVersion = ra.readShort();
        int trackerMinor= (trackerVersion & 0xff00) >> 8;
        int trackerMajor = trackerVersion & 0x0ff;
        String tracker = "ImpulseTracker " + trackerMajor + "." + trackerMinor;

        int compatVersoin = ra.readShort();
        int compatMinor = (compatVersoin & 0xff00) >> 8;
        int compatMajor = compatVersoin  & 0x0ff;
        String compat = compatMajor + "." + compatMinor;

        int flags = ra.readUnsignedShort();

        int special = ra.readUnsignedShort();
        boolean hasSongMessage = (special & 1) > 0;


        double gVolume = ra.readUnsignedByte() / 128.0;
        double mixVolume = ra.readUnsignedByte() / 128.0;

        double globalVolume = gVolume * mixVolume;

        int speed = ra.readUnsignedByte();
        int tempo = ra.readUnsignedByte();

        double channelSeparation = ra.readUnsignedByte() / 128.0;
        int midiPWD = ra.readUnsignedByte();

        int messageLenght = ra.readUnsignedShort();
        int messageOffset = ra.readUnsignedInt();
        String message = hasSongMessage ?
            readMessage(ra, messageOffset, messageLenght) :
            null;

        ra.skipBytes(4); // reserved

        double[] panning = new double[64];
        for (int n = 0; n < panning.length; n++) {
            int p = ra.readUnsignedByte();
            if ((p & 128) > 0) // TODO: handle disabled channels
                p &= ~128;
            else if (p == 100) // TODO: handle surround channels
                p = 0;
            panning[n] = (n - 32) / 32.0;
        }

        double[] volume = new double[64];
        for (int n = 0; n < volume.length; n++)
            volume[n] = ra.readUnsignedByte() / 64.0;

        int[] positions = new int[positionCount];
        for (int n = 0; n < positions.length; n++)
            positions[n] = ra.readUnsignedByte();

        long instrumentList = ra.getPosition();
        ra.skipBytes(instrumentCount*4);
        long sampleList = ra.getPosition();
        ra.skipBytes(sampleCount*4);
        long patternList = ra.getPosition();

/*		int instrumentOffset = ra.readUnsignedInt();
        int sampleOffset = ra.readUnsignedInt();
        int patternOffset = ra.readUnsignedInt();
*/
        ra.seek(sampleList);

        Sample[] samples = loadSamples(ra, sampleCount);

        ra.seek(instrumentList);

        Instrument[] instruments = loadInstruments(ra, instrumentCount, samples);

        ra.seek(patternList);

        Pattern[] patterns = loadPatterns(ra, patternCount);

        module = new Module(
            modName,
            id,
            tracker,
            instruments,
            patterns,
            positions,
            0,
            tempo,
            speed,
            globalVolume,
            0,
            volume,
            panning);

        module.setDescription(message);

        module.setProperty("compatibility", compat);
        module.setProperty("sample-count", sampleCount);
        module.setProperty("stereo", (flags & 1) > 0);
        module.setProperty("zero-volume-optimizaton", (flags & 2) > 0);
        module.setProperty("use-instruments", (flags & 4) > 0);
        module.setProperty("linear-slides", (flags & 8) > 0);
        module.setProperty("old-effects", (flags & 16) > 0);
        module.setProperty("GEF-linked", (flags & 32) > 0);
        module.setProperty("midi-pitch", (flags & 64) > 0);
        module.setProperty("request-midi-config", (flags & 128) > 0);
        module.setProperty("flags", flags);
        module.setProperty("special", special);
        module.setProperty("song-message-attached", (special & 1) > 0);
        module.setProperty("midi-config-attached", (special & 8) > 0);
        module.setProperty("global-volume", globalVolume);
        module.setProperty("mix-volume", mixVolume);
        module.setProperty("separation", channelSeparation);
        module.setProperty("PWD", midiPWD);
    }

    public Module getModule() {
        return module;
    }

    private String readMessage(RandomAccess ra, int offset, int length) throws IOException {
        long pos = ra.getPosition();
        ra.seek(offset);
        String msg = ra.readZeroPaddedString(length);
        String nl = ((char)0x0d)+"";
        StringBuffer sb = new StringBuffer(msg);
        int ofs;
        while ((ofs = sb.indexOf(nl)) > -1) {
            sb.replace(ofs, ofs+nl.length(), "\n");
        }
        ra.seek(pos);
        return sb.toString();
    }

    private Sample[] loadSamples(RandomAccess ra, int sampleCount)
        throws IOException, InvalidFormatException {
        List<Sample> samples = new ArrayList<Sample>();
        for (int n = 0; n < sampleCount; n++) {
            int samplePos = ra.readUnsignedInt();
            long currentPos = ra.getPosition();
            ra.seek(samplePos);
            samples.add(loadSample(ra));
            ra.seek(currentPos);
        }
        return (Sample[])samples.toArray(new Sample[0]);
    }

    private Sample loadSample(RandomAccess ra) throws IOException, InvalidFormatException {
        String id = ra.readString(4);
        if (!id.equals("IMPS"))
            throw new InvalidFormatException("Sample ID should be 'IMPS', was '" + id + "'");
        String fileName = ra.readString(12);
        ra.readUnsignedByte(); // 0x00h
        double globalVolume = ra.readUnsignedByte() / 64.0;
        int flags = ra.readUnsignedByte();
        boolean hasSampleData = isEnabled(flags, 0);
        boolean bits16 = isEnabled(flags, 1);
        boolean stereo = isEnabled(flags, 2);
        boolean compressed = isEnabled(flags, 3);
        boolean loop = isEnabled(flags, 4);
        boolean sustainLoop = isEnabled(flags, 5);
        boolean pingPongLoop = isEnabled(flags, 6);
        boolean sustainPingPongLoop = isEnabled(flags, 7);
        double volume = ra.readUnsignedByte() / 64.0;
        String name = ra.readZeroPaddedString(26);

        boolean signedSampleData = isEnabled(ra.readUnsignedByte(), 0);
        int pan = ra.readUnsignedByte();
        boolean usePanning = isEnabled(pan, 7);
        double panning = clear(pan, 7) / 64.0;

        int length = ra.readUnsignedInt(); // length in samples, not bytes
        int loopStart = ra.readUnsignedInt();
        int loopEnd = ra.readUnsignedInt();
        int c5speed = ra.readUnsignedInt();
        int sustainLoopStart = ra.readUnsignedInt();
        int sustainLoopEnd = ra.readUnsignedInt();

        int samplePointer = ra.readUnsignedInt();

        double vibratoSpeed = ra.readUnsignedByte() / 64.0;
        double vibratoDepth = ra.readUnsignedByte() / 64.0;
        int vibratoWaveform = ra.readUnsignedByte();
        double vibratoRate = ra.readUnsignedByte() / 64.0;

        // TODO: sustain loop support, in model and player logic
        // add separate model class for loops?
        int loopType = loop ? pingPongLoop ? Sample.PING_PONG : Sample.FORWARD : Sample.NO_LOOP;

        Sample s = new Sample(id, name, volume, panning, length, loopType, loopStart, loopEnd, 0, 0, new S3MUnits(c5speed, false));

        long currentPos = ra.getPosition();
        ra.seek(samplePointer);
        s.setData(loadSampleData(ra, length, signedSampleData, bits16));
        ra.seek(currentPos);
        return s;
    }

    private short[] loadSampleData(RandomAccess ra, int length, boolean singed, boolean bits16)
        throws IOException {

        System.out.println(ra.getPosition() + " -> " + length);

        short[] data = new short[length];
        for (int n = 0; n < length; n++) {
            if (bits16)
                data[n] = (short)ra.readShort();
            else
                data[n] = (short)(ra.readByte() << 8);
        }
        return new short[0];
    }

    private Instrument[] loadInstruments(RandomAccess ra, int instrumentCount, Sample[] samples) {
        Instrument[] instruments = new Instrument[instrumentCount];
        for (int n = 0; n < instruments.length; n++) {
            instruments[n] = new Instrument("", new int[0], samples, new AutoEffect[0], 0);
        }
        return instruments;
    }

    private Pattern[] loadPatterns(RandomAccess ra, int patternCount) {
        Track[] tracks = new Track[64];
        for (int n = 0; n < tracks.length; n++)
            tracks[n] = new Track(1);

        Pattern[] patterns = new Pattern[patternCount];
        for (int n = 0; n < patterns.length; n++) {
            patterns[n] = new Pattern(tracks, 1);
        }
        return patterns;
    }

    private boolean isEnabled(int flags, int bit) {
        return (flags & (1 << bit)) > 0;
    }

    private int clear(int flags, int bit) {
        return flags & ~(1 << bit);
    }

}
