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
import anakata.modplay.ThreadedPlayer;
import anakata.modplay.loader.ModuleLoader;
import anakata.modplay.module.Module;
import anakata.sound.output.JavaSoundOutput;
import anakata.sound.output.SoundDataFormat;

/**
 * An example of ana-mp being used to play music in an applet.
 * @author torkjel
 */
public class ThreadedAppletPlayer extends Applet implements ActionListener {

    public static final String INFO = Meta.PROJECT_NAME + " " + Meta.VERSION;

    public static final int BITS = 16;
    public static final int RATE = 44100;
    public static final int CHANNELS = 2;
    public static final boolean INTERPOLATE = true;
    public static final int BUFFERSIZE = 500;

    private Module module;
    private URL theUrl;
    private ThreadedPlayer tp;

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
        tp = createPlayer(module);
        tp.start();
    }

    /**
     * create a player thread for playing the
     * @param module
     * @return
     */
    private ThreadedPlayer createPlayer(Module module) {
        ThreadedPlayer player = new ThreadedPlayer();
        try {
            player.init(
                new JavaSoundOutput(new SoundDataFormat(BITS, RATE, CHANNELS), BUFFERSIZE),
                INTERPOLATE);
            player.load(module);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        player.pause(true);
        return player;
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
        tp.pause(false);
    }

    public void stop() {
        tp.pause(true);
    }

    public void destroy() {
        tp.stop();
    }

    public void actionPerformed(ActionEvent e) {

        // pause pressed
        if (e.getSource() == pauseButton) {
            tp.pause(!tp.isPaused());

        // stop pressed
        } else if (e.getSource() == stopButton) {
            tp.stop();

        // play pressed, start the current module if it isn't already playing. unpause it if
        // it is paused
        } else if (e.getSource() == playButton) {
            if (!tp.isRunning()) {
                tp = createPlayer(module);
                tp.pause(false);
                tp.start();
            } else if (tp.isPaused())
                tp.pause(false);

        // next pressed, stop the current module and start the next
        } else if (e.getSource() == nextButton) {
            tp.stop();
            module = loadModule(nextModule++);
            moduleLabel.setText(module.getName());
            this.validate();
            tp = createPlayer(module);
            tp.start();
            tp.pause(false);

        // prev pressed, stop the current module and start the previous
        } else if (e.getSource() == prevButton) {
            tp.stop();
            module = loadModule(nextModule--);
            moduleLabel.setText(module.getName());
            this.validate();
            tp = createPlayer(module);
            tp.start();
            tp.pause(false);
        }
    }
}
