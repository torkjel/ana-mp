/*
 * Created on Sep 5, 2004
 */
package anakata.modplay.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

//import anakata.modplay.PlayList;
import anakata.modplay.loader.InvalidFormatException;
import anakata.modplay.module.ModuleInfo;
import anakata.util.Logger;

/**
 * @author torkjel
 */
public class PlayListWindow extends Frame implements ActionListener, MouseListener, WindowListener {

	private JList list;
    private Vector<ModuleInfo> playList;
    private IActions player;
    private String playListFile;
    private JFileChooser listFileChooser;
    private JFileChooser modFileChooser;
    private InfoWindow infoWindow;

    private JButton add;
    private JButton remove;
    private JButton loadList;
    private JButton saveList;
    private JButton showInfo; 
    private JButton hide; 
    
    public PlayListWindow(File plFile, IActions player, InfoWindow infoWindow) 
        throws InvalidFormatException, IOException {

        super("Playlist");

        this.player = player;
        
        addWindowListener(this);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.infoWindow = infoWindow;
        
        // load playlist
        
        this.playListFile = plFile.getAbsolutePath();
        playList = loadPlayList(new FileInputStream(plFile));
        
        // set up gui
        
        list = new JList(playList);
        list.setBackground(new Color(0x0ff000000));
        list.setForeground(new Color(0x0ffffffff));
        list.setCellRenderer(new MyListRenderer());
        list.addMouseListener(this);
        
        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new GridLayout(3,2));

        buttons1.setMaximumSize(new Dimension(1000,50));
        buttons1.setMinimumSize(new Dimension(10,50));
        
        add = new JButton("Add");
        add.addActionListener(this);
        buttons1.add(add);
        
        remove = new JButton("Remove");
        remove.addActionListener(this);
        buttons1.add(remove);

        loadList = new JButton("Load list");
        loadList.addActionListener(this);
        buttons1.add(loadList);
        
        saveList = new JButton("Save list as");
        saveList.addActionListener(this);
        buttons1.add(saveList);

        showInfo = new JButton("Info");
        showInfo.addActionListener(this);
        buttons1.add(showInfo);

        hide = new JButton("Hide");
        hide.addActionListener(this);
        buttons1.add(hide);
        
        add(new JScrollPane(list));

        add(buttons1);

        // set up filechoosers
        
        modFileChooser = new JFileChooser(new File("."));
        modFileChooser.setFileFilter(new ModFileFilter());
        modFileChooser.setMultiSelectionEnabled(true);
        listFileChooser = new JFileChooser(new File("."));
        listFileChooser.setFileFilter(new ListFileFilter());
        listFileChooser.setMultiSelectionEnabled(false);
    }
        
    public void addFile(String file) throws FileNotFoundException, InvalidFormatException{
        playList.add(ModuleInfo.get(file));
        list.setListData(playList);
    }

    public void removeFile(int index) {
        playList.remove(index);
        list.setListData(playList);
    }

    public void removeFile(String file) {
    	for (ModuleInfo mi : playList) {
            if (mi.getFileName().equals(file))
                playList.remove(mi);
        }
        list.setListData(playList);
    }

    public String getFile(int index) {
        return ((ModuleInfo)playList.get(index)).getFileName();
    }
    
    public int getLength() {
        return playList.size();
    }

    private int current = 0;
    
    public void setCurrent(int index) {
        this.current = index;
        list.repaint();
        infoWindow.setInfo(getCurrentInfo());
    }

    public String getCurrent() {
        return ((ModuleInfo)playList.get(current)).getFileName();
    }

    public ModuleInfo getCurrentInfo() {
        return (ModuleInfo)playList.get(current);
    }
    
    public int getCurrentIndex() {
        return current;
    }
    
    public void next() {
        current++;
        if (current >= getLength())
            current = 0;
        list.repaint();
        infoWindow.setInfo(getCurrentInfo());
    }
    
    public void prev() {
        current--;
        if (current < 0 || current >= getLength())
            current = getLength()-1;
        list.repaint();
        infoWindow.setInfo(getCurrentInfo());
    }

    /**
     * tell the playlist to save itself to the current playlist file
     * @throws IOException
     */
    public void savePlayList() throws IOException {
        savePlayList(new File(playListFile),playList);
        Logger.debug("Saved playlist: " + playListFile);
    }
    
    public String getPlayListFile() {
        return playListFile;
    }

    public String getModFileChooserDir() {
        return modFileChooser.getCurrentDirectory().getAbsolutePath();
    }
    
    public String getListFileChooserDir() {
        return listFileChooser.getCurrentDirectory().getAbsolutePath();
    }

    public void setModFileChooserDir(String dir) {
        modFileChooser.setCurrentDirectory(new File(dir));
    }
    
    public void setListFileChooserDir(String dir) {
        listFileChooser.setCurrentDirectory(new File(dir));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == add) {
            if (modFileChooser.showOpenDialog(this) == 
                JFileChooser.APPROVE_OPTION) {
            	for (File f : modFileChooser.getSelectedFiles()) {
	                try {
	                    addFile(f.getAbsolutePath());
	                    savePlayList();
	                } catch (Exception ex) {
	                    Logger.exception(ex);
	                    return;
	                }
            	}
            }
            
        } else if (e.getSource() == loadList) {
            if (listFileChooser.showOpenDialog(this) == 
                JFileChooser.APPROVE_OPTION) {
                File file = listFileChooser.getSelectedFile();
                try {
                    playList = loadPlayList(
                        new FileInputStream(file.getAbsolutePath()));
                } catch (Exception ex) {
                    Logger.exception(ex);
                    Logger.warning("Could not load playlist: " + 
                        file.getAbsolutePath());
                    return;
                }
                this.list.setListData(playList);
                playListFile = file.getAbsolutePath();
            }
            player.stop();

        } else if (e.getSource() == saveList) {
            if (listFileChooser.showSaveDialog(this) == 
                JFileChooser.APPROVE_OPTION) {
                File file = listFileChooser.getSelectedFile();

                try {
                    savePlayList(file,playList);
                    playListFile = file.getAbsolutePath();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
            }

        } else if (e.getSource() == remove) {
            for (Object o : list.getSelectedValues()) {
                removeFile(((ModuleInfo)o).getFileName());
                try {
                    savePlayList();
                } catch (IOException ex) {
                    Logger.exception(ex);
                    Logger.warning("Could not save playlist: " + playListFile); 
                }
            }

        } else if (e.getSource() == showInfo) {
            if (infoWindow.isVisible()) {
                infoWindow.setVisible(false);
            } else {
                infoWindow.setVisible(true);
            }
            showInfo.repaint();

        } else if (e.getSource() == hide) {
            setVisible(false);
        }
        
    }
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < getLength()) {
                setCurrent(selectedIndex);
                player.play();
            }
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    public void minimize() {
        setExtendedState(Frame.ICONIFIED);
    }

    public void unminimize() {
        if ((getExtendedState() & Frame.ICONIFIED) != 0) {
            setExtendedState(Frame.NORMAL);
        }
    }
    
    class MyListRenderer extends DefaultListCellRenderer {

        private final Color FG_SELECTED = Color.BLACK;
        private final Color BG_SELECTED = Color.WHITE;
        private final Color FG_NORMAL = Color.WHITE;
        private final Color BG_NORMAL = Color.BLACK;
        private final Color FG_CURRENT = Color.YELLOW;
        private final Color BG_CURRENT = Color.BLACK;
        private Font font = null;
        public MyListRenderer() {
            super();
        }
        
        public Component getListCellRendererComponent(JList list,Object value,
            int index,boolean isSelected,boolean cellHasFocus) {
         
            
           Component comp = super.getListCellRendererComponent(
                list,value,index,isSelected,cellHasFocus);

           if (font == null) {
               Font oldFont = comp.getFont();
               font = new Font("monospaced",Font.BOLD,oldFont.getSize());
           }

           comp.setFont(font);
            if (index == getCurrentIndex() && !isSelected) {
                comp.setForeground(FG_CURRENT);
                comp.setBackground(BG_CURRENT);
            } else if (isSelected){
                comp.setForeground(FG_SELECTED);
                comp.setBackground(BG_SELECTED);
            } else {
                comp.setForeground(FG_NORMAL);
                comp.setBackground(BG_NORMAL);
            }
            return comp;
        }
    }

    private static Vector<ModuleInfo> loadPlayList(InputStream plFile) 
        throws IOException, InvalidFormatException {
        Vector<ModuleInfo> playList = new Vector<ModuleInfo>();
        BufferedReader br = new BufferedReader(new InputStreamReader(plFile));
        String line = null;
        while (
            (line = br.readLine()) != null && 
            (line = line.trim()).length() > 0) {

            if (!new File(line).exists()) {
                Logger.warning("File in playlist does not exist: " + line);
                continue;
            }

            if (line.length() > 0) 
                playList.add(ModuleInfo.get(line));
        }
        return playList;
    }
    
    private static void savePlayList(File plFile, Vector playList) 
        throws IOException {
        
        Iterator pli = playList.iterator();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(plFile)); 
            while (pli.hasNext()) {
                pw.println(((ModuleInfo)pli.next()).getFileName());
            }
            pw.flush();
        } finally {
            if (pw != null)
                pw.close();
        }
    }
    
    private static class ModFileFilter extends FileFilter {
        public boolean accept(File f) {
            String name = f.getName().toLowerCase(); 
            return 
                f.isDirectory() ||
                name.endsWith(".mod") || 
                name.endsWith(".xm") || 
                name.endsWith(".s3m");
        }

        public String getDescription() {
            return "Modules (.mod, .xm, or .s3m)";
        }
    }

    private static class ListFileFilter extends FileFilter {
        public boolean accept(File f) {
            String name = f.getName().toLowerCase(); 
            return 
                f.isDirectory() || 
                name.endsWith(".list") || 
                name.endsWith(".lst");
        }

        public String getDescription() {
            return "Playlists (.list or .lst)";
        }
    }

    public static void main(String[] args) {
        Frame frame = null;
        try {
            frame = new PlayListWindow(null,null,null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        frame.setVisible(true);
        frame.pack();
    }

    public void windowActivated(WindowEvent e) {
    }
    public void windowClosed(WindowEvent e) {
    }
    public void windowClosing(WindowEvent e) {
        this.setVisible(false);
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
    }
}