/*
 * Created on Apr 4, 2005
 */
package anakata.modplay.example.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import anakata.modplay.Meta;
import anakata.modplay.Player;
import anakata.modplay.loader.ModuleLoader;
import anakata.modplay.module.Module;
import anakata.modplay.player.PlayerException;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.SoundDataFormat;

/**
 * An example of ana-mp being used to play music in an applet.
 * @author torkjel
 */
public class AppletPlayer extends Applet implements ActionListener {

    public static final String INFO = Meta.PROJECT_NAME + " " + Meta.VERSION;

    public static final int BITS = 16;
    public static final int RATE = 44100;
    public static final int CHANNELS = 2;
    public static final boolean INTERPOLATE = true;
    public static final int BUFFERSIZE = 500;

    private Module module;
    private PlayerThread pt;
    private URL theUrl;

    private JButton pauseButton;
    private JButton stopButton;
    private JButton playButton;
    private JButton nextButton;
    private JButton prevButton;

    private JLabel moduleLabel;

    private String protocol;
    private String host;
    private int port;
    private List<String> fileList = new ArrayList<String>();
    private int nextModule = 0;

    public void init() {

        protocol = getParameter("protocol");
        host = getParameter("host");
        port = Integer.parseInt(getParameter("port"));
        StringTokenizer st = new StringTokenizer(getParameter("files"), ",");
        while (st.hasMoreTokens())
            fileList.add(st.nextToken());

        module = loadModule(nextModule++);

        // gui code
        setLayout(new BorderLayout());

        moduleLabel = new JLabel(module.getName());

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(INFO));
        p.add(moduleLabel);

        add(p, BorderLayout.NORTH);

        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.X_AXIS));
        playButton = new JButton(">");
        playButton.addActionListener(this);
        pauseButton = new JButton("\u2225");
        pauseButton.addActionListener(this);
        stopButton = new JButton("\u25A1");
        stopButton.addActionListener(this);
        nextButton = new JButton("\u226B");
        nextButton.addActionListener(this);
        prevButton = new JButton("\u226A");
        prevButton.addActionListener(this);
        playerPanel.add(playButton);
        playerPanel.add(pauseButton);
        playerPanel.add(stopButton);
        playerPanel.add(prevButton);
        playerPanel.add(nextButton);
        add(playerPanel, BorderLayout.SOUTH);

        // start the thread
        pt = createPlayerThread(module);
        pt.start();
    }

    /**
     * create a player thread for playing the
     * @param module
     * @return
     */
    private PlayerThread createPlayerThread(Module module) {
        Player player = new Player();
        try {
            player.init(
                new JavaSoundOutput(new SoundDataFormat(BITS, RATE, CHANNELS), BUFFERSIZE),
                INTERPOLATE);
            player.load(module);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlayerThread playerthread = new PlayerThread(player);
        playerthread.pause(true);
        return playerthread;
    }

    /**
     * load the <code>nextModule</code>'th file in the file list.
     * @param nextModule
     * @return
     */
    private Module loadModule(int nextModule) {
        int mc = fileList.size();
        while (nextModule <= 0) nextModule += mc;
        try {
            theUrl = new URL(protocol, host, port, fileList.get((nextModule + mc) % mc));
            return ModuleLoader.getModuleLoader(theUrl).getModule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        pt.pause(false);
    }

    public void stop() {
        pt.pause(true);
    }

    public void destroy() {
        pt.quit();
    }

    /**
     * a separate thread for running the module, to a void stalling anything else the applet is
     * doing
     *
     * @author torkjel
     */
    private static class PlayerThread extends Thread {

        private Player player;
        private boolean run = true;
        private boolean pause = false;

        public PlayerThread(Player player) {
            this.player = player;
        }

        public void run() {
            while (run) {
                if (!pause) {
                    try {
                        if (!player.play()) break;
                    } catch (PlayerException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try { Thread.sleep(10); } catch (InterruptedException e) { }
                }
            }

            run = false;
            player.close();
        }

        /**
         * make the player thread die as soon as possible.
         */
        public void quit() {
            run = false;
        }

        public void pause(boolean pause) {
            this.pause = pause;
        }

        public boolean isPause() {
            return pause;
        }

        public boolean isRunning() {
            return run;
        }
    }


    public void actionPerformed(ActionEvent e) {

        // pause pressed
        if (e.getSource() == pauseButton) {
            pt.pause(!pt.isPause());

        // stop pressed
        } else if (e.getSource() == stopButton) {
            pt.quit();

        // play pressed, start the current module if it isn't already playing. unpause it if
        // it is paused
        } else if (e.getSource() == playButton) {
            if (!pt.isRunning()) {
                pt = createPlayerThread(module);
                pt.pause(false);
                pt.start();
            } else if (pt.isPause())
                pt.pause(false);

        // next pressed, stop the current module and start the next
        } else if (e.getSource() == nextButton) {
            pt.quit();
            module = loadModule(nextModule++);
            moduleLabel.setText(module.getName());
            this.validate();
            pt = createPlayerThread(module);
            pt.start();
            pt.pause(false);

        // prev pressed, stop the current module and start the previous
        } else if (e.getSource() == prevButton) {
            pt.quit();
            module = loadModule(nextModule--);
            moduleLabel.setText(module.getName());
            this.validate();
            pt = createPlayerThread(module);
            pt.start();
            pt.pause(false);
        }
    }
}
