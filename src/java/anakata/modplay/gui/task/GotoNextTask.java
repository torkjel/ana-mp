/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui.task;

import anakata.modplay.gui.Player;
import anakata.modplay.gui.PlayerControl;

/**
 * a little task who's only mission in life is to poll the
 * player thread, and start playing the next module if the 
 * player has stopped
 * @author torkjel
 */
public class GotoNextTask extends Task {

    public static final String NAME = "next";
    public static final int INTERVAL = 200;

    private PlayerControl controller;
    private boolean stop = false;
    
    public GotoNextTask(PlayerControl playerControl) {
        super(NAME,INTERVAL);
        this.controller = playerControl;
    }
        
    public boolean doTask() {
        Player player = controller.getPlayer();
        if (player != null && player.hasStopped())
            controller.next();
        if (player != null) {
            controller.updatePosition();
        }
        return !stop;
    }

    public void stop() {
        stop = true;
    }
}
