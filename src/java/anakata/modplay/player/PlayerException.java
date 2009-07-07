/*
 * Created on Aug 29, 2004
 */
package anakata.modplay.player;

/**
 * @author torkjel
 */
public class PlayerException extends Exception {

    public PlayerException(String msg, Throwable cause) {
        super(msg,cause);
    }

    public PlayerException(String msg) {
        super(msg);
    }

}
