/*
 * Created on Apr 5, 2005
 */
package anakata.modplay;

public interface Meta {

    public static final int VERSION_MAJOR= 0;
    public static final int VERSION_MINOR = 9;
    public static final int VERSION_MICRO = 7;
    public static final int VERSION_NANO = 4;

    public static final String VERSION =
        VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_MICRO + "." + VERSION_NANO;
    public static final String PROJECT_NAME = "ANA-MP";
    public static final String PROJECT_HOMEPAGE = "http://ana-mp.sourceforge.net";

    public static final String AUTHOR_NAME = "Torkjel Hongve";
    public static final String AUTHOR_EMAIL = "torkjelh@conduct.no";

    public static final String LICENSE = "LGPL";

    public static final int COPYRIGHT_START_YEAR = 2002;
    public static final int COPYRIGHT_NOW_YEAR = 2009;

    public static final String COPYRIGHT_MESSAGE =
        PROJECT_NAME + "v" + VERSION + "\n" +
        "(C) " + COPYRIGHT_START_YEAR + "-" + COPYRIGHT_NOW_YEAR + " " +
            AUTHOR_NAME + ", " + AUTHOR_EMAIL + "\n" +
        PROJECT_HOMEPAGE + "\n" +
        PROJECT_NAME + " is Free software, released under the " + LICENSE + " license.\n";

}
