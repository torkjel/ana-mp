/*
 * Created on Aug 23, 2004
 */
package anakata.util.audiodiff.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import anakata.modplay.loader.InvalidFormatException;
import anakata.util.audiodiff.logic.IWaveModel;
import anakata.util.audiodiff.logic.StandardWaveModel;
import anakata.util.audiodiff.logic.WavReader;

/**
 * @author torkjel
 */
public class MainWindow extends JFrame implements WindowListener  {

    private IWaveModel model1;
    private IWaveModel model2;
    private WavePanel wPanel;
    
    public MainWindow(String file1, String file2) 
        throws IOException, FileNotFoundException, InvalidFormatException {

        getContentPane().setLayout(
            new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

        
        WavReader reader1 = new WavReader(new File(file1));
        WavReader reader2 = new WavReader(new File(file2));
        model1 = new StandardWaveModel(reader1.getInputStream(), file1); 
        model2 = new StandardWaveModel(reader2.getInputStream(), file2); 
        this.addWindowListener(this);
        ControlPanel controlPanel = new ControlPanel();
        wPanel = new WavePanel(new IWaveModel[] {model1,model2}, controlPanel);
        controlPanel.setWavePanel(wPanel);
        this.getContentPane().add(new JScrollPane(wPanel));
        this.getContentPane().add(controlPanel);
    }

    public void windowActivated(WindowEvent e) {    }
    public void windowClosed(WindowEvent e) {    }
    public void windowClosing(WindowEvent e) {
        this.dispose();
    }
    public void windowDeactivated(WindowEvent e) {    }
    public void windowDeiconified(WindowEvent e) {    }
    public void windowIconified(WindowEvent e) {    }
    public void windowOpened(WindowEvent e) {    }

}



