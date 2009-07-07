/*
 * Created on Sep 6, 2004
 */
package anakata.modplay.gui;

/**
 * @author torkjel
 */
public interface IActions {
    public void exit();
    public void minimize();
    public void configure();
    public void showInfo();
    public void showPlayList();
    public void play();
    public void stop();
    public void pause();
    public void prev();
    public void next();
    public void setVolume(double volume);
    public void setPosition(double volume);
}
