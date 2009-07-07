/*
 * Created on Aug 9, 2004
 */
package anakata.modplay.loader;

import java.io.IOException;

import anakata.modplay.module.Instrument;
import anakata.modplay.module.Module;
import anakata.modplay.module.ModuleUnits;
import anakata.modplay.module.Pattern;
import anakata.modplay.module.Sample;
import anakata.modplay.module.Track;
import anakata.util.Util;
import anakata.util.io.RandomAccess;
import anakata.util.io.RandomAccessArray;

/**
 * @author torkjel
 */
public class STMLoader extends ModuleLoader {

    private Module module;

    private static final int TYPE_SONG = 1;
    private static final int TYPE_MODULE = 2;


    public STMLoader(String name, byte[] d) throws IOException {

        ModuleUnits stmUnits = new ModUnits(ModUnits.PAL, false);

        RandomAccess raf = new RandomAccessArray(d);

        try {
            int data = -1;

            StringBuffer nameSB = new StringBuffer();
            while (raf.getPosition() < 20 && (data = raf.read()) != 0) {
                nameSB.append((char)data);
            }
            raf.seek(20);

            String stmname = nameSB.toString();

            StringBuffer trackerSB = new StringBuffer();
            while (raf.getPosition() < 28 && (data = raf.read()) != 0) {
                trackerSB.append((char)data);
            }
            raf.seek(28);

            String tracker = trackerSB.toString();

            int id = raf.read();

            int fileType = raf.read();
            if (fileType != TYPE_MODULE)
                System.err.println("Wrong file type: " + fileType);

            int versionMajor = raf.read();
            int versionMinor = raf.read();
            int initialTempo = raf.read();
            int numPatterns = raf.read();
            int initialGlobalVolume = raf.read();

            System.out.println("Name: " + stmname);
            System.out.println("Tracker: " + tracker);
            System.out.println("ID: " + Integer.toString(id,16));
            System.out.println("FileType: " + fileType);
            System.out.println("Version: " + versionMajor + "." + versionMinor);
            System.out.println("Initial tempo: " + initialTempo);
            System.out.println("Number of patterns: " + numPatterns);
            System.out.println("Initial global volume: " + initialGlobalVolume);

            raf.seek(48);

            Instrument[] instruments = new Instrument[31];

            for (int n = 0; n < 31; n++) {
                raf.seek(48 + n*32);
                StringBuffer instrumentSB = new StringBuffer();
                int count = 0;
                while (count++ < 12 && (data = raf.read()) != 0)
                    instrumentSB.append((char)data);
                String instrumentName = instrumentSB.toString();

                int instumentId = raf.read();
                int instrumentDisk = raf.read();
                raf.skipBytes(2);
                int sampleLen = readWord(raf);
                int loopStart = readWord(raf);
                int loopEnd = readWord(raf);
                int sampleVolume = raf.read();
                raf.skipBytes(1);
                int c3 = readWord(raf);
                raf.skipBytes(4);
                int paragraphs = readWord(raf);

                int loopType;
                if ((loopStart >= 0 && loopStart < sampleLen) && (loopEnd > loopStart && loopEnd <= sampleLen))
                    loopType = Sample.FORWARD;
                else
                    loopType = Sample.NO_LOOP;

/*                System.out.println("Instrument name: " + instrumentName);
                System.out.println("\tID: " + instumentId);
                System.out.println("\tDisk: " + instrumentDisk);
                System.out.println("\tLength: " + sampleLen);
                System.out.println("\tLoop start: " + loopStart);
                System.out.println("\tLoop end: " + loopEnd);
                System.out.println("\tVolume: " + sampleVolume);
                System.out.println("\tC3 Hz: " + c3);
                System.out.println("\tParagraphs: " + paragraphs);
*/
                ModuleUnits units = new STMUnits(c3);

                int note2sample[] = new int[10*12];
                Sample sample = new Sample("sample-" + n,
                    instrumentName,
                    (double)sampleVolume / 64.0,
                    0.5,
                    sampleLen,
                    loopType,
                    loopStart,
                    loopEnd - loopStart,
                    0,
                    0,
                    units);

                instruments[n] = new Instrument(
                    instrumentName,
                    note2sample,
                    new Sample[] {sample},
                    null,
                    0.5);
            }

            raf.skipBytes(4);

            int[] orders = new int[128];
            for (int n = 0; n < orders.length; n++) {
                orders[n] = raf.read();
//                System.out.println("Order: " + n+ "/" + orders[n]);
            }


            Pattern[] patterns = new Pattern[numPatterns];
            for (int n = 0; n < numPatterns; n++) {
                Track[] tracks = new Track[4];
                for (int t = 0; t < 4; t++)
                    tracks[t] = new Track(64);
                Pattern pattern = new Pattern(tracks, 64);

                System.out.println();

                for (int y = 0; y < 64; y++) {

                    System.out.println();

                    for (int x = 0; x < 4; x++) {
                        int byte1 = (raf.read() & 0x0ff);
                        int note = 0;
                        int volume = 0;
                        int octave = 0;
                        int instrument = 0;
                        int command = 0;
                        int arg1 = 0;
                        int arg2 = 0;

                        if (byte1 == 251 || byte1 == 252 || byte1 == 253) {
                            note = 15; // out of range -> no note played
                            octave = 0;
                            volume = 0;
                            instrument = 0;
                            command = 0;
                            arg1 = 0;
                            arg2 = 0;
                            System.out.print("-");
                        } else {
                            note = byte1 & 0x0f;
                            octave = (byte1 >>> 4) & 0x0f;
                            instrument = (byte1 & 0x0f0) << 4;
                            int byte2 = (raf.read() & 0x0ff);
                            volume = byte2 & 7;
                            instrument = byte2 >> 3;
                            int byte3 = (raf.read() & 0x0ff);
                            command = byte3 & 0x0f;
                            volume += ((byte3 >>> 4)) << 3;
                            int byte4 = (raf.read() & 0x0ff);
                            arg2 = byte4 & 0x0f;
                            arg1 = byte4 >>> 4;
                        }


                        int noteNum;
                        if (note <= 12)
                            noteNum = octave * 12 + note;
                        else
                            noteNum = Instrument.NO_NOTE;

                        int instrumentNum;
                        if (instrument > 0)
                            instrumentNum = instrument-1;
                        else
                            instrumentNum = Track.NO_INSTRUMENT;

//                        pattern.getTrack(x).initDivision(y,instrumentNum,noteNum,);



                        if (n < 2)
                            System.out.print(getNoteSymbol(note, octave) + " " + getInstrument(instrument) + " " + getVolume(volume) + " " + getEffect(command,arg1,arg2) + " | ");
                        else {Util.exit(1);}
//                        pattern.getTrack(x).initDivision(y,-1,note,);
//                        if (note)
                    }
                }
            }

            new Module(stmname,"", tracker, instruments, null, orders, 0, 0, 0, 0, 0, null, null);


        } catch (IOException e) {
            e.printStackTrace();
            Util.exit(1);
        }

        Util.exit(1);
    }

/*    public int[] getEffectType(int command, int arg1, int arg2) {
        switch (command) {
            case 0: // no effect
                return new int[] {Effect.MOD_ARPEGGIO,0,0};
            case 1: // A
                return new int[] {Effect.MOD_SET_SPEED,arg1,arg2};
            case 2:
                return new int[] {Effect.MOD_PATTERN_BREAK,0,0};
            case 3:
//                return new int[] {Effect.MOD_VOLUME_SLIDE,
        }
        return null;
    }
*/
    public String getVolume(int vol) {
        if (vol > 64) return "..";
        String str = vol + "";
        if (str.length() == 1) str = "0" + str;
        return str;
    }

    public String getInstrument(int vol) {
        if (vol == 0) return "..";
        String str = vol + "";
        if (str.length() == 1) str = "0" + str;
        return str;
    }

    public String getEffect(int eff, int a1, int a2) {
        char effc = (char)(eff > 0 ? ('A'-1) + eff : '.');
        char a1c = (char)(a1 <= 9 ? '0' + a1 : 'A' + a1 - 10);
        char a2c = (char)(a2 <= 9 ? '0' + a2 : 'A' + a2 - 10);
        return effc + "" + a1c + "" + a2c;
    }


    private String getNoteSymbol(int note, int period)
    {
//        if (note == Instrument.KEY_OFF)
//            return "###";
//        else
        if (note == Instrument.NO_NOTE)
            return "---";
        String code = null;
        switch (note)
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
        code += period;
        return code;
    }

    public Module getModule() {
        return module;
    }

    int readWord(RandomAccess raf) throws IOException {
        return ((raf.read() & 0x0ff) + (raf.read() & 0x0ff) * 256) & 0x0ffff;
    }

    int readDWord(RandomAccess raf) throws IOException {
        return
            (raf.read() & 0x0ff) |
            ((raf.read() & 0x0ff) >>> 8) |
            ((raf.read() & 0x0ff) >>> 16) |
            ((raf.read() & 0x0ff) >>> 24);
    }

    public static void main(String[] args) {
        ModuleUnits units = new STMUnits(1000);
        double rate1 = units.note2rate(12*4);
        double rate2 = units.note2rate(12*5);
        System.out.println(rate1);
        System.out.println(rate2);

        double note1 = units.rate2note(1000);
        double note2 = units.rate2note(2000);
        System.out.println(note1);
        System.out.println(note2);

        System.out.println(units.rate2note(units.note2rate(10)));
        System.out.println(units.note2rate(units.rate2note(10000)));

    }

}


