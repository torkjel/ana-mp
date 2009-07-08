package anakata.modplay.player;

import java.io.IOException;

import anakata.sound.output.*;
import anakata.modplay.module.*;

/**
 * This is the default mixer used by ANA-MP. For simplicity, it is hardcoded to
 * produce 16 bits, 44100Hz samplerate, stereo sound. If lower quality is
 * desired, downsampling must be done by the used output plugin.
 *
 * @author torkjel
 *
 */
public class DefaultMixer extends AbstractMixer {

    // buffers for storing mixed sound. Must be big enough to hold one tick.
    // I assume that no ticks are longer than 1 sec...
    private int[] mixerBufferLeft = new int[44100];
    private int[] mixerBufferRight = new int[44100];
    private byte[] mixedData = new byte[44100 * 4 * 2];

    private Output output;
    private Class<? extends LowLevelMixer> lowLevelMixerClass;

    private Track[] tracks;

    /**
     * @param output the output plugin used by this mixer
     * @param lowLevelMixerClass class who implements the LowLevelMixer interface
     * @param numberOfTracks the number of tracks this mixer should be able to mix
     */
    public DefaultMixer(Output output, Class<? extends LowLevelMixer> lowLevelMixerClass, int numberOfTracks) {
        super(numberOfTracks);
        this.lowLevelMixerClass = lowLevelMixerClass;
        this.output = output;
        this.tracks = new Track[numberOfTracks];
        for (int n = 0; n < numberOfTracks; n++)
            tracks[n] = new Track();
    }

    public void setTrack(short[] sampleData, double offset, double rate, double volume,
            double panning, int loopType, int loopStart, int looplength, int track)
            throws PlayerException {
        try {
            if (tracks[track] == null)
                tracks[track] = new Track(lowLevelMixerClass.newInstance(),
                    sampleData, offset, rate, volume, panning, loopType, loopStart, looplength);
            else
                tracks[track].init(lowLevelMixerClass.newInstance(),
                    sampleData, offset, rate, volume, panning, loopType, loopStart, looplength);
        } catch (Exception e) {
            throw new PlayerException("Could not initialize track " + track, e);
        }
    }

    public void setMute(int track, boolean mute) {
        if (tracks[track] != null)
            tracks[track].setMute(mute);
    }

    public boolean isMute(int track) {
        return tracks[track].isMute();
    }

    public void play(double millisecs) throws PlayerException {
        try {
            // always 44100Hz, 16 bits, stereo...
            int len = (int) ((millisecs * 44100) / 1000) * (16 / 8) * 2;
            mix(mixedData, len);
            output.write(mixedData, 0, len);
        } catch (IOException e) {
            throw new PlayerException("Could not ply mixed data", e);
        }
    }

    /**
     * mix 16 bit stereo sound into the given array. The entire array is filled
     * with data.
     *
     * @param data
     */
    private void mix(byte[] data, int len) {
        double separation = getSeparation();
        double balance = getBalance();
        int numberOfTracks = getNumberOfTracks();

        int length = len / 4;
        for (int n = 0; n < numberOfTracks; n++)
            if (tracks[n] != null) {
                tracks[n].mix(length, Track.LEFT);
                tracks[n].mix(length, Track.RIGHT);
            }
        double mulL, mulR;
        mulR = mulL = getAmplification() * getVolume() / numberOfTracks;
        if (balance < 0.5)
            mulR *= 2 * balance;
        else if (balance > 0.5)
            mulL *= 2 * (1 - balance);

        double rval, lval;
        int irval, ilval;
        for (int n = 0, m = 0; n < len; n += 4, m++) {
            rval = (mixerBufferRight[m] * mulR);
            if (rval > 32767)
                rval = 32767; // some simple overflow protection..
            else if (rval < -32768)
                rval = -32768;

            lval = mixerBufferLeft[m] * mulL;
            if (lval > 32767)
                lval = 32767;
            else if (lval < -32768)
                lval = -32768;

            irval = (int) (rval * separation + lval * (1.0 - separation));
            // this is slow... calc for each track instead??? (faster)
            ilval = (int) (lval * separation + rval * (1.0 - separation));
            data[n + 0] = (byte) (ilval);
            data[n + 1] = (byte) (ilval >>> 8);
            data[n + 2] = (byte) (irval);
            data[n + 3] = (byte) (irval >>> 8);
        }

        for (int n = 0; n < length; n++) {
            mixerBufferLeft[n] = 0;
            mixerBufferRight[n] = 0;
        }
    }

    /**
     * This is the mixers internal representation of a track ( channel)
     *
     * @author torkjel
     *
     */
    private class Track {
        public static final int LEFT = 1;

        public static final int RIGHT = 2;

        public static final int MONO = 3;

        private short[] sampleData;

        private double offset, offsetLeft, offsetRight, rate, volume, panning;

        private int loopType, loopStart, loopLength;

        private boolean mute = false;

        private LowLevelMixer llm;

        public Track() {
        }

        /**
         * creates an initialized track
         *
         * @param llm a low level mixer
         * @param sampleData the sample to be mixed
         * @param offset the start of the part of the sample to be mixed
         * @param rate the rate the sample should be mixed at
         * @param volume the volume of the sample
         * @param panning the panning of the sample
         * @param loopType the type of sample loop
         * @param loopStart the start of the loop (if any)
         * @param looplength the length of the loop (if any)
         */
        public Track(LowLevelMixer llm, short[] sampleData, double offset,
                double rate, double volume, double panning, int loopType,
                int loopStart, int loopLength) {
            init(llm, sampleData, offset, rate, volume, panning, loopType,
                    loopStart, loopLength);
        }

        /**
         * initializes a track
         *
         * @param sampleData the sample to be mixed
         * @param offset the start of the part of the sample to be mixed
         * @param rate the rate the sample should be mixed at
         * @param volume the volume of the sample
         * @param panning the panning of the sample
         * @param loopType the type of sample loop
         * @param loopStart the start of the loop (if any)
         * @param looplengththe length of the loop (if any)
         * @param interpolate indicates if the resulting sound should be interpolated
         */
        public void init(LowLevelMixer llm, short[] sampleData, double offset,
                double rate, double volume, double panning, int loopType,
                int loopStart, int loopLength) {
            this.llm = llm;
            this.sampleData = sampleData;
            this.offset = offsetLeft = offsetRight = offset;
            this.rate = rate;
            this.volume = volume;
            this.panning = panning;
            this.loopType = loopType;
            this.loopStart = loopStart;
            this.loopLength = loopLength;
        }

        /**
         * sets if this track should be muted
         *
         * @param mute if true: mute, if false: unmute
         */
        public void setMute(boolean mute) {
            this.mute = mute;
        }

        public boolean isMute() {
            return mute;
        }

        int TBL = 256;

        short[] tmpBuffer = new short[TBL];

        int tmpBufferStart = 0;

        int tmpBufferEnd = 0;

        /**
         * @param channel which channel to mix to. This is for making the panning
         *            work. Can be LEFT, RIGHT or MONO.
         */
        public void mix(int length, int channel) {
            double vol = volume;
            if (channel == LEFT)
                vol = volume * (1 - panning);
            else if (channel == RIGHT)
                vol = volume * panning;
            else if (channel == MONO)
                vol = volume;

            if (sampleData == null || mute || vol == 0)
                return;

            double grad = (double) rate / 44100.0; // format.getRate();

            int[] outBuffer;
            if (channel == MONO || channel == LEFT) {
                outBuffer = mixerBufferLeft;
                offset = offsetLeft;
            } else {
                outBuffer = mixerBufferRight;
                offset = offsetRight;
            }

            int intOffset = (int) offset;
            tmpBufferStart = intOffset;
            tmpBufferEnd = intOffset;

            int outOffset = 0;
            while (outOffset < length) {

                if (tmpBufferEnd - tmpBufferStart <= 16) {
                    tmpBufferStart = tmpBufferEnd;
                    tmpBufferEnd += TBL / 2;
                    getTrackData(tmpBuffer, tmpBufferStart, tmpBufferEnd,
                            intOffset, vol);
                    intOffset += TBL / 2;
                }

                int[] outOffsetH = new int[] { outOffset };
                double[] inOffsetH = new double[] { offset };

                llm.mix(outBuffer, outOffsetH, length, tmpBuffer, inOffsetH,
                        tmpBufferEnd, TBL, grad);

                outOffset = outOffsetH[0];
                offset = inOffsetH[0];
                tmpBufferStart = (int) offset;
            }

            if (channel == MONO || channel == LEFT) {
                offsetLeft = offset;
            } else {
                offsetRight = offset;
            }
        }

        private int getTrackData(short[] buffer, int bufferStart,
                int bufferEnd, int sampleOffset, double vol) {
            int res = 0;
            if (loopType == Sample.NO_LOOP) {
                res = noLoop(buffer, bufferStart, bufferEnd, sampleData,
                        sampleOffset, vol);
            } else if (loopType == Sample.FORWARD) {
                res = forwardLoop(buffer, bufferStart, bufferEnd, sampleData,
                        sampleOffset, vol, loopStart, loopLength);
            } else if (loopType == Sample.PING_PONG) {
                res = pingPongLoop(buffer, bufferStart, bufferEnd, sampleData,
                        sampleOffset, vol, loopStart, loopLength);
            }
            return res;
        }

        public int noLoop(short[] buffer, int bufferStart, int bufferEnd,
                short[] sampleData, int sampleOffset, double vol) {

            int bufferLength = buffer.length - 1;
            int bufferOffset = bufferStart;

            int sampleLength = sampleData.length;

            int volume = (int) (vol * 256);

            while (sampleOffset < sampleLength && bufferOffset < bufferEnd) {
                buffer[bufferOffset & bufferLength] = (short)((sampleData[sampleOffset] * volume) >>> 8);
                sampleOffset++;
                bufferOffset++;
            }
            while (bufferOffset < bufferEnd) {
                buffer[bufferOffset & bufferLength] = 0;
                bufferOffset++;
            }
            return bufferOffset;
        }

        public int forwardLoop(short[] buffer, int bufferStart, int bufferEnd,
                short[] sampleData, int sampleOffset, double vol,
                int loopStart, int loopLength) {
            int bufferLength = buffer.length - 1;
            int bufferOffset = bufferStart;

            int sampleLength = loopStart + loopLength;
            if (sampleLength >= sampleData.length)
                sampleLength = sampleData.length;

            int volume = (int) (vol * 256);

            while (sampleOffset < loopStart && bufferOffset < bufferEnd) {
                buffer[bufferOffset & bufferLength] = (short)((sampleData[sampleOffset] * volume) >>> 8);
                sampleOffset++;
                bufferOffset++;
            }

            // loop
            while (bufferOffset < bufferEnd) {
                if (sampleOffset >= sampleLength)
                    sampleOffset = ((sampleOffset - loopStart) % loopLength)
                            + loopStart;
                buffer[bufferOffset & bufferLength] = (short)((sampleData[sampleOffset] * volume) >>> 8);
                sampleOffset++;
                bufferOffset++;
            }
            return bufferOffset;
        }

        public int pingPongLoop(short[] buffer, int bufferStart, int bufferEnd,
                short[] sampleData, int sampleOffset, double vol,
                int loopStart, int loopLength) {

            int bufferLength = buffer.length - 1;
            int bufferOffset = bufferStart;

            int volume = (int) (vol * 256);

            int sampleLength = loopStart + loopLength;
            if (sampleLength >= sampleData.length)
                sampleLength = sampleData.length;

            // before the loop end is reached first time
            while (sampleOffset < sampleLength && bufferOffset < bufferEnd) {
                buffer[bufferOffset & bufferLength] = (short) ((sampleData[sampleOffset] * volume) >>> 8);
                sampleOffset++;
                bufferOffset++;
            }

            // loop
            sampleOffset = ((sampleOffset - loopStart) % (loopLength * 2))
                    + loopStart;
            int sampleLengthX2 = sampleLength * 2;
            while (bufferOffset < bufferEnd) {
                while (sampleOffset < sampleLength && bufferOffset < bufferEnd) {
                    buffer[bufferOffset & bufferLength] = (short) ((sampleData[sampleOffset] * volume) >>> 8);
                    sampleOffset++;
                    bufferOffset++;
                }
                while (sampleLengthX2 - sampleOffset > loopStart
                        && bufferOffset < bufferEnd) {
                    sampleOffset++;
                    buffer[bufferOffset & bufferLength] = (short) ((sampleData[sampleLengthX2
                            - sampleOffset] * volume) >>> 8);
                    bufferOffset++;
                }
                // it has either reached loopStart or buffer is full -> no if needed
                // if (sampleLengthX2 - sampleOffset <= loopStart)
                sampleOffset = loopStart;
            }
            return bufferOffset;
        }
    }
}