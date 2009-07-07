/*
 * Created on Sep 23, 2004
 */
package anakata.modplay.gui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import anakata.modplay.module.ModuleInfo;

/**
 * @author torkjel
 */
public class InfoWindow extends Frame implements ActionListener, WindowListener {
    
    private JButton hide = new JButton("Hide");
    
    private JLabel name = new JLabel("Title: ");
    private JTextField nameTF = new JTextField(20);
    private JLabel type = new JLabel("Type: ");
    private JTextField typeTF = new JTextField(20);
    private JLabel tracker = new JLabel("Tracker: ");
    private JTextField trackerTF = new JTextField(20);
    private JLabel file = new JLabel("File: ");
    private JTextField fileTF = new JTextField(20);
    private JLabel id = new JLabel("ID: ");
    private JTextField idTF = new JTextField(20);
    private JLabel instr = new JLabel("Instruments: ");
    private JTextField instrTF = new JTextField(20);
    private JLabel tracks = new JLabel("Tracks: ");
    private JTextField tracksTF = new JTextField(20);
    private JLabel patterns = new JLabel("Patterns: ");
    private JTextField patternsTF = new JTextField(20);
    private JLabel positions = new JLabel("Positions: ");
    private JTextField positionsTF = new JTextField(20);
    
    public InfoWindow() {
        super();

        addWindowListener(this);
        
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.X_AXIS));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(9,1));
        leftPanel.add(name); 
        leftPanel.add(file); 
        leftPanel.add(type); 
        leftPanel.add(tracker);
        leftPanel.add(id); 
        leftPanel.add(instr); 
        leftPanel.add(tracks); 
        leftPanel.add(patterns); 
        leftPanel.add(positions);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(9,1));   
        rightPanel.add(nameTF);
        rightPanel.add(fileTF);
        rightPanel.add(typeTF);
        rightPanel.add(trackerTF);
        rightPanel.add(idTF);
        rightPanel.add(instrTF);
        rightPanel.add(tracksTF);
        rightPanel.add(patternsTF);
        rightPanel.add(positionsTF);

        mainPanel.add(Box.createHorizontalStrut(5));
        mainPanel.add(leftPanel);
        mainPanel.add(Box.createHorizontalStrut(5));
        mainPanel.add(rightPanel);
        mainPanel.add(Box.createHorizontalStrut(5));

        hide.addActionListener(this);
        
        add(Box.createVerticalStrut(5));
        add(mainPanel);
        add(Box.createVerticalStrut(5));
        add(hide);
        add(Box.createVerticalStrut(5));
    }
    
    public InfoWindow(ModuleInfo info) {
        this();
        setInfo(info);
    }

    public void setInfo(ModuleInfo info) {
        setTitle("Info: " + info.getName());
        nameTF.setText(info.getName());
        fileTF.setText(info.getFileName());
        typeTF.setText(info.getType());
        trackerTF.setText(info.getTracker());
        idTF.setText(info.getId());
        instrTF.setText(info.getInstrumentCount()+"");
        tracksTF.setText(info.getTrackCount()+"");
        patternsTF.setText(info.getPatternCount()+"");
        positionsTF.setText(info.getPositionCount()+"");
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == hide) {
            this.setVisible(false);
        }
    }

    public void minimize() {
        setExtendedState(Frame.ICONIFIED);
    }

    public void unminimize() {
        if ((getExtendedState() & Frame.ICONIFIED) != 0) {
            setExtendedState(Frame.NORMAL);
        }
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
