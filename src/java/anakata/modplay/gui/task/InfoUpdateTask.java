/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui.task;

import anakata.modplay.gui.PlayerControl;
import anakata.modplay.module.ModuleInfo;

/**
 * a little task who's only mission in life is to update the 
 * info panel with info on the current module 
 * @author torkjel
 */
public class InfoUpdateTask extends Task {

    public static final String NAME = "info";
    public static final int INTERVAL = 300;

    private PlayerControl controller;
    private boolean stop = false;
    
    public InfoUpdateTask(PlayerControl controller) {
        super(NAME,INTERVAL);
        this.controller = controller;
    }
    
    private String def = "Welcome to ANA-Player :)";

    public boolean doTask() {

        ModuleInfo mi = controller.getModuleInfo();
        if (mi != null)
            controller.updateInfo(mi.getName());
        else 
            controller.updateInfo(def);
        return true;
    }

    public void stop() {
    }
}