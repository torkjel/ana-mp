package anakata.modplay;

import java.util.*;
import java.io.*;

/**
 * A list of module names to be played
 *
 * @author torkjel
 */
public class PlayList implements Iterator<String> {
    private List<String> list;

    private int position;

    private String playListName = "no-name";

    /**
     * makes a playlist from a file containing filenames seperated by newlines
     *
     * @param fileName the name of a module
     */
    public PlayList(String fileName) throws IOException {
        this.playListName = fileName;
        LineNumberReader lnr = new LineNumberReader(new FileReader(fileName));
        loadList(lnr);
    }

    /**
     * makes a playlist containing several modules
     *
     * @param modules
     */
    public PlayList(String[] modules) throws IOException {

        StringBuffer sb = new StringBuffer();
        for (String module : modules) {
            sb.append(module);
            sb.append("\n");
        }
        LineNumberReader lnr = new LineNumberReader(new StringReader(sb
                .toString()));
        loadList(lnr);

    }

    /**
     * loads a list from a LineNumberReader. Each line contains the name of a module.
     *
     * @param lnr
     * @throws IOException
     */
    private void loadList(LineNumberReader lnr) throws IOException {
        position = -1;
        list = new ArrayList<String>();
        String line = null;
        while ((line = lnr.readLine()) != null) {
            line = line.trim();
            File file = new File(line);
            if (file.exists() && file.isFile())
                list.add(line);
        }
    }

    public String getPlayListName() {
        return playListName;
    }

    /**
     * @return true if there are more modulenames in the list
     */
    public boolean hasNext() {
        return position + 1 < list.size();
    }

    /**
     * @return the next file name in the playlist.
     */
    public String next() {
        if (hasNext())
            return list.get(++position);
        else
            throw new NoSuchElementException();
    }

    /**
     * @return the next file name in the playlist.
     */
    public String nextFileName() {
        return (String) next();
    }

    /**
     * removes the current module from the playlist
     */
    public void remove() {
        list.remove(position);
    }

    /**
     * @return the number of files in the playlist
     */
    public int getNumberOfFileNames() {
        return list.size();
    }

    /**
     * @return a random file name from the playlist. This also changes the
     *         position in the playlist
     */
    public String getRandomFileName() {
        int pos = (int) (Math.random() * list.size());
        position = pos - 1;
        return nextFileName();
    }
}
