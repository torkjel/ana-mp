/*
 * Created on Sep 20, 2004
 */
package anakata.modplay.gui;

import anakata.modplay.module.ModuleInfo;

/**
 * @author torkjel
 */
public interface PlayerControl {
    void next();
    void prev();
    Player getPlayer();
    void updatePosition();
    void updateInfo(String info);
    void saveConfig();
    ModuleInfo getModuleInfo();

}
