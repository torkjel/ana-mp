/*
 * Created on Sep 6, 2004
 */
package anakata.modplay.gui;

import anakata.modplay.gui.skin.SkinParser;

/**
 * @author torkjel
 */
public class Action {

    public static final int EXIT = SkinParser.BUTTON_EXIT;
    public static final int MINIMIZE = SkinParser.BUTTON_MINIMIZE;
    public static final int PLAYLIST = SkinParser.BUTTON_PLAYLIST;
    public static final int CONFIGURE = SkinParser.BUTTON_CONFIGURE;
    public static final int INFO = SkinParser.BUTTON_INFO;
    public static final int PLAY = SkinParser.BUTTON_PLAY;
    public static final int STOP = SkinParser.BUTTON_STOP;
    public static final int PAUSE = SkinParser.BUTTON_PAUSE;
    public static final int SKIP = SkinParser.BUTTON_SKIP;
    public static final int PREV = SkinParser.BUTTON_PREV;
    public static final int VOLUME = SkinParser.SLIDER_VOLUME;
    public static final int POSITION = SkinParser.SLIDER_POSITION;
    
    private Object actionInteface;
    private int action;
    
    public Action(int action, Object actionInterface) {
        this.action = action;
        this.actionInteface = actionInterface;
    }

    public void fireAction() {
        switch (action) {
            case EXIT:
                ((IActions)actionInteface).exit();
                break;
            case MINIMIZE:
                ((IActions)actionInteface).minimize();
                break;
            case PLAYLIST:
                ((IActions)actionInteface).showPlayList();
                break;
            case CONFIGURE:
                ((IActions)actionInteface).configure();
                break;
            case INFO:
                ((IActions)actionInteface).showInfo();
                break;
            case PLAY:
                ((IActions)actionInteface).play();
                break;
            case STOP:
                ((IActions)actionInteface).stop();
                break;
            case PAUSE:
                ((IActions)actionInteface).pause();
                break;
            case PREV:
                ((IActions)actionInteface).prev();
                break;
            case SKIP:
                ((IActions)actionInteface).next();
                break;
        }
    }    

    public void fireAction(double value) {
        switch (action) {
            case VOLUME:
                ((IActions)actionInteface).setVolume(value);
                break;
            case POSITION:
                ((IActions)actionInteface).setPosition(value);
                break;
        }
    }    
}
