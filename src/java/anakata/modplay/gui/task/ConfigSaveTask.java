/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui.task;

import anakata.modplay.gui.PlayerControl;

/**
 * @author torkjel
 */
public class ConfigSaveTask extends Task {

    public static final String NAME = "configsave";
    public static final int INTERVAL = 1000*60;

    private PlayerControl controller;

    public ConfigSaveTask(PlayerControl controller) {
        super(NAME,INTERVAL);
        this.controller = controller;
    }
    
    public boolean doTask() {
        controller.saveConfig();
        return true;
    }

    public void stop() {
    }
    
}
