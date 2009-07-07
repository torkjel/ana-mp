/*
 * Created on Aug 16, 2004
 */
package anakata.modplay.loader;

/**
 * @author torkjel
 */
public class InvalidFormatException extends Exception {

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
