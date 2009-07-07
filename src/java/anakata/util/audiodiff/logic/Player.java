/*
 * Created on Aug 28, 2004
 */
package anakata.util.audiodiff.logic;

import anakata.modplay.player.PlayerException;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.SoundDataFormat;
import anakata.util.Util;

/**
 * @author torkjel
 */
public class Player {

    private IWaveModel model;
    private PlayerThread thread;

    public Player(IWaveModel model) {
        this.model = model;
    }

    public void play() throws PlayerException {
        this.thread = new PlayerThread();
        new Thread(thread).start();
    }

    public void stop() {
        thread.stop();
    }

    public int getPosition() {
        return thread.getPosition();
    }


    class PlayerThread implements Runnable {

        private boolean stop = false;

        private int ofs;
        private JavaSoundOutput out;
        public PlayerThread() throws PlayerException {
            out = new JavaSoundOutput(new SoundDataFormat(16,44100,2), 100);
        }

        public void run() {
            try {
            out.open();
            ofs = 0;
            for (ofs = 0; ofs < model.getSize(); ofs++) {
                int d = model.getValue(ofs);
                byte[] data = new byte[4];
                data[0] = (byte)(d & 0x0ff);
                data[1] = (byte)(d >>> 8);
                data[2] = (byte)(d & 0x0ff);
                data[3] = (byte)(d >>> 8);
                out.write(data,0,4);
                if (stop) break;
            }
            out.close();
            ofs = 0;
            } catch (Exception e) {
                e.printStackTrace();
                Util.exit(1);
            }
        }

        public void stop() {
            stop = true;
        }

        public int getPosition() {
            return ofs;
        }
    }
}
