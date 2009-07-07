/*
 * Created on Sep 5, 2004
 */
package anakata.modplay.gui;

import java.io.IOException;

import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.player.ModuleState;
import anakata.modplay.player.PlayerException;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.SoundDataFormat;
import anakata.util.Logger;

/**
 * @author torkjel
 */
public class Player {

    private PlayerThread pt;
    private ModuleState moduleState;

    public Player(String file) {
        pt = new PlayerThread(file);
    }

    public void play() {
        pt.play();
    }

    public void stop() {
        pt.stop();
    }

    public void pause() {
        pt.pause();
    }

    public boolean hasStopped() {
        return pt.hasStopped();
    }

    public boolean isPaused() {
        return pt.isPaused();
    }

    public boolean hasLoaded() {
        return pt.hasLoaded();
    }


    public String getTitle() {
        if (moduleState != null && moduleState.getModule() != null) {
            return moduleState.getModule().getName();
        } else
            return "No module";
    }

    public ModuleState getModuleState() {
        return moduleState;
    }

    class PlayerThread implements Runnable {

        private anakata.modplay.Player player;
        private String file;
        private boolean stop = false, pause = false, stopped = false, loaded = false;
        private Thread t = null;

        public PlayerThread(String file) {
            player = new anakata.modplay.Player();
            this.file = file;
        }

        public void run() {
            try {
                JavaSoundOutput out = new JavaSoundOutput(new SoundDataFormat(16,44100,2), 300);
                player.init(out,true);
                player.load(file);
            } catch (IOException e) {
                t = null;
                throw new RuntimeException(e);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
                Logger.warning("Not a module \"" + file + "\"???");
                t = null;
                return;
            } catch (PlayerException e) {
                e.printStackTrace();
                Logger.warning("Can't init output plugin");
                t = null;
                return;
            }
            loaded = true;
            moduleState = player.getModuleState();

            try {
                while (true) {
                    if (stop) break;
                    if (pause) {
                        try {Thread.sleep(10);} catch (InterruptedException e) {}
                        continue;
                    }
                    if (!player.play()) break;
                }

            } catch (PlayerException e) {
                e.printStackTrace();
            }

            if (!player.close())
                Logger.warning("Could not close player...");
            t = null;
            stopped = true;
        }

        public void stop() {
            stop = true;
        }

        public void pause() {
            pause = !pause;
        }

        public void play() {
            if (t == null) {
                t = new Thread(this);
                t.start();
            }
            pause = false;
        }

        public boolean hasStopped() {
            return stopped;
        }

        public boolean isPaused() {
            return pause;
        }

        public boolean hasLoaded() {
            return loaded;
        }
    }

}
