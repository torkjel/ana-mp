/*
 * Created on Sep 13, 2004
 */
package anakata.util;

import java.io.PrintStream;

/**
 * @author torkjel
 */
public class Logger {
    private static boolean debug = false;
    private static boolean info = true;

    private static String STATUS_EXCEPTION = "EXP";
    private static String STATUS_DEBUG = "DBG";
    private static String STATUS_WARNING = "WRN";

    private static PrintStream infoOut = System.out;
    private static PrintStream debugOut = System.err;
    
    public static void enableDebug(boolean enable) {
        debug = enable;
    }

    public static void enableInfo(boolean enable) {
        info = enable;
    }

    public static void setDebugStream(PrintStream ps) {
        debugOut = ps;
    }
    
    public static void setInfoStream(PrintStream ps) {
        infoOut = ps;
    }

    public static void debug(String msg) {
        printDebug(msg,STATUS_DEBUG);
    }

    public static void warning(String msg) {
        printDebug(msg,STATUS_WARNING);
    }

    public static void exception(Throwable t) {
        while (t != null) {
            printDebug(t.getMessage(),STATUS_EXCEPTION);
            for (int n = 0; n < t.getStackTrace().length; n++) {
                printDebug(t.getStackTrace()[n].toString(),STATUS_EXCEPTION);
            }
            t = t.getCause();
            if (t != null) 
                printDebug(
                    "Caused by: " + t.getClass().getName(),STATUS_EXCEPTION);
        }
    }
    
    public static void info(String msg) {
        if (info) return;
            infoOut.print(msg);
    }
    
    public static void info(char c) {
        if (info) return;
            infoOut.print(c);
    }

    private static void printDebug(String msg, String type) {
        if (debug)
            debugOut.println(type + " : " + getPrefix() + " : " + msg);
    }

    private static String getPrefix() {
        StackTraceElement elem = null;
        try {
            throw new Exception();
        } catch (Exception e) {
            elem = e.getStackTrace()[3];
        }
        return elem.getFileName() + ":" + elem.getLineNumber();
    }
}
