/*
 * Created on Sep 2, 2004
 */
package anakata.modplay.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JWindow;

import anakata.modplay.gui.config.Config;
import anakata.modplay.gui.skin.SkinParser;
import anakata.modplay.gui.skin.SkinnedPanel;
import anakata.modplay.gui.task.ConfigSaveTask;
import anakata.modplay.gui.task.GotoNextTask;
import anakata.modplay.gui.task.InfoUpdateTask;
import anakata.modplay.gui.task.TaskList;
import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.module.ModuleInfo;
import anakata.util.Logger;
import anakata.util.Util;

/**
 * @author torkjel
 */
public class MainWindow extends JWindow implements ComponentListener, IActions, PlayerControl, WindowListener {

    private Frame dummyFrame;
    private PlayListWindow playList;
    private InfoWindow infoWindow;
    private SkinnedPanel panel;
    private Config config;
    private TaskList taskList;

    public static void main(String[] args) {

        Logger.enableDebug(true);

        Frame dummyFrame = new Frame();
        try {
            MainWindow window = new MainWindow(dummyFrame);
        } catch (Exception e) {
            Logger.exception(e);
            Util.exit(1);
        }
    }

    public MainWindow(Frame dummyFrame)
        throws InvalidFormatException, IOException  {

        super(dummyFrame);

        // load configuration

        try {
            config = Config.get();
        } catch (IOException e) {
            Logger.warning("Could not load configuration. Loading default");
            config = new Config();
        }

        // initalize window/skin

        this.dummyFrame = dummyFrame;

        Container pane = getContentPane();

        SkinParser parser = new SkinParser(
            config.getPlayerConfig().getSkin(),pane, this);

        panel = parser.getPanel(pane);
        setSize(panel.getSize());
        setVisible(true);
        Dimension winSize = new Dimension(
            panel.getSize().width + getInsets().left + getInsets().right,
            panel.getSize().height + getInsets().bottom + getInsets().top);
        setVisible(false);
        pane.setSize(winSize);
        setSize(winSize);
        pane.add(panel);
        panel.setLocation(
            config.getPlayerConfig().getXPos(),
            config.getPlayerConfig().getYPos());

        dummyFrame.setVisible(true);
        dummyFrame.setSize(1,1);
        dummyFrame.setTitle("ANA-MP");

        setVisible(true);

        this.addComponentListener(this);
        this.addWindowListener(this);

        // initialize module-info window

        infoWindow = new InfoWindow();
        infoWindow.pack();
        infoWindow.setVisible(false);

        // initialize playlist-window

        File plFile = new File(config.getPlayListConfig().getList());
        if (!plFile.exists()) {
            plFile.getParentFile().mkdirs();
            plFile.createNewFile();
            if (!plFile.exists())
                throw new IOException("Could not create default playlist: " +
                    plFile.getAbsolutePath());
        }

        playList = new PlayListWindow(plFile,this,infoWindow);
        playList.setCurrent(config.getPlayListConfig().getListPosition());
        playList.pack();
        playList.setVisible(false);


        panel.setVolume(config.getPlayerConfig().getVolume());
        this.setLocation(new Point(config.getPlayerConfig().getXPos(), config.getPlayerConfig().getYPos()));

        playList.setLocation(config.getPlayListConfig().getXPos(), config.getPlayListConfig().getYPos());
        playList.setSize(config.getPlayListConfig().getWidth(), config.getPlayListConfig().getHeight());
        playList.setVisible(config.getPlayListConfig().isVisible());
        playList.setModFileChooserDir(
            config.getPlayListConfig().getModFileChooserDir());
        playList.setListFileChooserDir(
                config.getPlayListConfig().getListFileChooserDir());


        // initialize task-list

        taskList = TaskList.newInstance();
        taskList.add(new ConfigSaveTask(this));
    }

    public void setLocation(Point p) {
        Point oldLocation = new Point(super.getLocation().x, super.getLocation().y);

        Dimension screenSize =  Toolkit.getDefaultToolkit().getScreenSize();

        // the JWindow can't be moved outside of the screen, but the location
        // value move outside anyway... fix it.
        if (p.x < 0) p.x = 0;
        if (p.x+super.getWidth() > screenSize.width)
            p.x = screenSize.width-super.getWidth();
        if (p.y < 0) p.y = 0;
        if (p.y+super.getHeight() > screenSize.height)
            p.y = screenSize.height-super.getHeight();

        super.setLocation(p.x, p.y);

        // sticky playlist
        if (playList != null) {
            stickyWindow(playList, oldLocation);
            stickyWindow(infoWindow,oldLocation);
        }
    }

    private void stickyWindow(Frame frame, Point oldLocation) {
        if (frame.isVisible() &&
            Math.abs(frame.getX() - (oldLocation.x+super.getWidth())) < 5 &&
            (isAdjacentRight(oldLocation,this.getSize(),frame.getLocation(), frame.getSize()))) {
            frame.setLocation(super.getX()+super.getWidth(),frame.getY()+(super.getY()-oldLocation.y));
        } else if (frame.isVisible() &&
            Math.abs((frame.getX() + frame.getWidth()) - oldLocation.x) < 5 &&
            isAdjacentRight(frame.getLocation(),frame.getSize(),oldLocation,this.getSize())) {
            frame.setLocation(super.getX()-frame.getWidth(),frame.getY()+(super.getY()-oldLocation.y));
        } else if (frame.isVisible() &&
            Math.abs(frame.getY() - (oldLocation.y+super.getHeight())) < 5 &&
            (isAdjacentTop(oldLocation,this.getSize(),frame.getLocation(), frame.getSize()))) {
            frame.setLocation(frame.getX()+(super.getX()-oldLocation.x), super.getY()+super.getHeight());
        } else if (frame.isVisible() &&
            Math.abs((frame.getY() + frame.getHeight()) - oldLocation.y) < 5 &&
            isAdjacentTop(frame.getLocation(),frame.getSize(),oldLocation,this.getSize())) {
            frame.setLocation(frame.getX()+(super.getX()-oldLocation.x),super.getY()-frame.getHeight());
        }
    }

    private boolean isAdjacentRight(Point p1, Dimension s1, Point p2, Dimension s2) {

        if (Math.abs((p1.getX()+s1.getWidth()) - p2.getX()) > 5) return false;

        int y1u = p1.y;
        int y1d = (int)(p1.y + s1.getHeight());

        int y2u = p2.y;
        int y2d = (int)(p2.y + s2.getHeight());

        if      (y1u >= y2u && y1u <= y2d) return true;
        else if (y1d >= y2u && y1d <= y2d) return true;
        else if (y2u >= y1u && y2u <= y1d) return true;
        else if (y2d >= y1u && y2d <= y1d) return true;

        return false;
    }

    private boolean isAdjacentTop(Point p1, Dimension s1, Point p2, Dimension s2) {

        if (Math.abs((p1.getY()+s1.getHeight()) - p2.getY()) > 5) return false;

        int x1u = p1.x;
        int x1d = (int)(p1.x + s1.getWidth());

        int x2u = p2.x;
        int x2d = (int)(p2.x + s2.getWidth());

        if      (x1u >= x2u && x1u <= x2d) return true;
        else if (x1d >= x2u && x1d <= x2d) return true;
        else if (x2u >= x1u && x2u <= x1d) return true;
        else if (x2d >= x1u && x2d <= x1d) return true;

        return false;
    }


    public void componentHidden(ComponentEvent e) {
    }
    public void componentMoved(ComponentEvent e) {
        getParent().setLocation(getX()+10,getY());
        getParent().setSize(new Dimension(getWidth()-20,1));
    }
    public void componentResized(ComponentEvent e) {
    }
    public void componentShown(ComponentEvent e) {
    }

    public void exit() {
        stop();
        saveConfig();
        taskList.stop();

        this.dispose();
        playList.dispose();
        ((Frame)getParent()).dispose();
    }

    public void saveConfig() {
        config.getPlayerConfig().setVolume(panel.getVolume());
        config.getPlayerConfig().setSkin(panel.getSkinName());
        config.getPlayerConfig().setXPos(this.getX());
        config.getPlayerConfig().setYPos(this.getY());
        config.getPlayListConfig().setXPos(playList.getX());
        config.getPlayListConfig().setYPos(playList.getY());
        config.getPlayListConfig().setWidth(playList.getWidth());
        config.getPlayListConfig().setHeight(playList.getHeight());
        config.getPlayListConfig().setVisible(playList.isVisible());
        config.getPlayListConfig().setList(playList.getPlayListFile());
        config.getPlayListConfig().setListPosition(playList.getCurrentIndex());
        config.getPlayListConfig().setModFileChooserDir(playList.getModFileChooserDir());
        config.getPlayListConfig().setListFileChooserDir(playList.getListFileChooserDir());
        try {
            config.set();
            playList.savePlayList(); // save playlist
        } catch (IOException e) {
            e.printStackTrace();
            Logger.warning("Could not write config: " + e.toString());
        }
    }

    public void minimize() {
        ((Frame)getParent()).setExtendedState(Frame.ICONIFIED);
        playList.minimize();
        infoWindow.minimize();
    }

    public void showPlayList() {
        if (playList != null)
            playList.setVisible(!playList.isVisible());
    }

    public void showInfo() {
        infoWindow.setVisible(!infoWindow.isVisible());
    }

    private Player player;

    public void play() {
        if (player != null && player.isPaused()) {
            pause();
            return;
        }

        if (taskList.get(InfoUpdateTask.NAME) == null)
            taskList.add(new InfoUpdateTask(this));
        if (taskList.get(GotoNextTask.NAME) == null)
            taskList.add(new GotoNextTask(this));

        if (player != null) player.stop();
        String file = playList.getCurrent();
        player = new Player(file);
        player.play();

        while (!player.hasLoaded()) {
            try {Thread.sleep(10); } catch (InterruptedException e) {}
        }
        setVolume();
    }

    public void stop() {
        if (player != null) {
            player.stop();
            taskList.remove(taskList.get(GotoNextTask.NAME));
        }
    }

    public void pause() {
        if (player != null)
            player.pause();
    }

    public void prev() {
        playList.prev();
        play();
    }

    public void next() {
        playList.next();
        play();
    }

    public void configure() {
        Logger.warning("Action not implemented: configure");
    }

    public void updateInfo(String info) {
        panel.setInfoText(info);
    }

    public ModuleInfo getModuleInfo() {
        if (playList != null)
            return playList.getCurrentInfo();
        return null;
    }

    public void setVolume(double volume) {
        if (player == null) return;
        int tracks = player.getModuleState().getModule().getPatternAtPos(0).getTrackCount();
        double multiplier = tracks / 2.0;
        double v = volume*multiplier;
        player.getModuleState().getMixer().setAmplification (v);
    }

    public void setVolume() {
        setVolume(panel.getVolume());
    }

    public void setPosition(double position) {
        if (player == null) return;
        int positions = player.getModuleState().getModule().getNumberOfPositions();
        int pos = (int)(positions*position);
        if (pos == positions) pos--;
        player.getModuleState().jump(pos,0,0);
    }

    public void updatePosition() {
        if (player == null || player.getModuleState() == null) return;
        panel.setPosition(
            player.getModuleState().getPosition(),
            player.getModuleState().getModule().getNumberOfPositions());
    }

    public Player getPlayer() {
        return player;
    }

    public void windowActivated(WindowEvent e) {
    }
    public void windowClosed(WindowEvent e) {
    }
    public void windowClosing(WindowEvent e) {
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
        playList.unminimize();
        infoWindow.unminimize();
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
    }
}
