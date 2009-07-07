/*
 * Created on Aug 23, 2004
 */
package anakata.util.audiodiff;

import anakata.util.audiodiff.gui.MainWindow;

/**
 * @author torkjel
 */
public class ADiff {

    public static void main(String[] args) {
        try {
            MainWindow mw = new MainWindow(args[0], args[1]);
            mw.pack();
            mw.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
