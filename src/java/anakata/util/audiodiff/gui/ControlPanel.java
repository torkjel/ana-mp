/*
 * Created on Aug 26, 2004
 */
package anakata.util.audiodiff.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import anakata.modplay.player.PlayerException;
import anakata.util.audiodiff.logic.IWaveModel;
import anakata.util.audiodiff.logic.Player;

/**
 * @author torkjel
 */
public class ControlPanel extends JPanel implements ActionListener {

    private JTextField startOfs;
    private JTextField endOfs;
    private JButton range;
    private JButton out;
    private JButton in;
    private JButton full;
    private JButton left;
    private JButton right;

    private WavePanel wPanel;

    private JTextField step;
    private JTextField scale;
    private JButton scaleUp;
    private JButton scaleDown;
    private JButton reset;
    private JButton max;
    private JRadioButton fst, snd, both;

    private JPanel file;
    private JButton quit;

    private JLabel file1;
    private JLabel file2;

    private JButton all;
    private JRadioButton play1st;
    private JRadioButton play2nd;

    public void setWavePanel(WavePanel wPanel) {
        this.wPanel = wPanel;
        startOfs.setText("0");
        endOfs.setText((wPanel.getModel(0).getSize()-1) + "");
        /*length.setText((wPanel.getModel(0).getSize())+"");*/
        String name1 = wPanel.getModel(0).getName();
        if (name1.length() > 13) name1 = name1.substring(0,10) + "...";
        file1.setText(name1);
        String name2 = wPanel.getModel(1).getName();
        if (name2.length() > 13) name2 = name2.substring(0,10) + "...";
        file2.setText(name2);
    }

    public ControlPanel() {

        file = new JPanel();
        file.setBorder(new TitledBorder("File"));
        file.setLayout(new GridLayout(4,2));

        file1 = new JLabel("                    ");
        file.add(file1);

        file2 = new JLabel("                    ");
        file.add(file2);

        JButton reload1 = new JButton("Reload #1");
        reload1.addActionListener(this);
        file.add(reload1);

        JButton reload2 = new JButton("Reload #2");
        reload2.addActionListener(this);
        file.add(reload2);

        JButton open1 = new JButton("Open #1");
        open1.addActionListener(this);
        file.add(open1);

        JButton open2 = new JButton("Open #2");
        open2.addActionListener(this);
        file.add(open2);

        quit = new JButton("Quit");
        quit.addActionListener(this);
        file.add(quit);

        JButton settings = new JButton("Settings");
        settings.addActionListener(this);
        file.add(settings);

        add(file);

        JPanel scaleP = new JPanel();
        scaleP.setBorder(new TitledBorder("Scale"));
        scaleP.setLayout(new GridLayout(4,3));

        ButtonGroup bgroup = new ButtonGroup();
        fst = new JRadioButton("1st"); scaleP.add(fst);
        snd = new JRadioButton("2nd"); scaleP.add(snd);
        both = new JRadioButton("Both"); scaleP.add(both);
        bgroup.add(fst); bgroup.add(snd); bgroup.add(both);

        reset = new JButton("1:1");
        reset.addActionListener(this);
        scaleP.add(reset);

        scaleUp = new JButton("Up");
        scaleUp.addActionListener(this);
        scaleP.add(scaleUp);

        step = new JTextField(5);
        step.setText("1.1");
        step.addActionListener(this);
        scaleP.add(step);

        max = new JButton("Max");
        max.addActionListener(this);
        scaleP.add(max);

        scaleDown = new JButton("Down");
        scaleDown.addActionListener(this);
        scaleP.add(scaleDown);

        scale = new JTextField(5);
        scale.setText("1");
        scale.setEditable(false);
        scaleP.add(scale);

        add(scaleP);

        JPanel controls = new JPanel();
        controls.setBorder(new TitledBorder("Zoom"));
        controls.setLayout(new GridLayout(4,2));

        out = new JButton("Out");
        controls.add(out);
        out.addActionListener(this);

        in = new JButton("In");
        controls.add(in);
        in.addActionListener(this);

        left = new JButton("Left");
        controls.add(left);
        left.addActionListener(this);

        right = new JButton("Right");
        controls.add(right);
        right.addActionListener(this);

        range = new JButton("Range");
        controls.add(range);
        range.addActionListener(this);

        full = new JButton("1:1");
        controls.add(full);
        full.addActionListener(this);

        startOfs = new JTextField(5);
        controls.add(startOfs);
        startOfs.setEditable(false);

        endOfs = new JTextField(5);
        controls.add(endOfs);
        endOfs.setEditable(false);

/*        length = new JTextField(5);
        controls.add(length);
        length.setEditable(false);*/

        add(controls);

        JPanel play = new JPanel();
        play.setBorder(new TitledBorder("Play"));
        play.setLayout(new GridLayout(4,2));

        ButtonGroup playGroup = new ButtonGroup();

        play1st = new JRadioButton("1st");
        play1st.setSelected(true);
        play.add(play1st);
        playGroup.add(play1st);

        play2nd = new JRadioButton("2nd");
        play.add(play2nd);
        playGroup.add(play2nd);

        all = new JButton("All");
        all.addActionListener(this);
        play.add(all);

        JButton range = new JButton("Range");
        range.addActionListener(this);
        play.add(range);

        JButton stop = new JButton("Stop");
        stop.addActionListener(this);
        play.add(stop);

        add(play);

    }

    public void setSelectionStart(int start) {
        startOfs.setText(start + "");
    }

    public void setSelectionEnd(int end) {
        endOfs.setText(end + "");
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == range) {
            wPanel.selectRange();
            wPanel.clearSelection();
            wPanel.update();

        } else if (e.getSource() == out) {
            int start = wPanel.getSelectionStart();
            int end = wPanel.getSelectionEnd();
            int size = end - start;
            if (size < 4) size = 4;
            wPanel.setSelelection(start - size/2, end + size/2);
            wPanel.update();

        } else if (e.getSource() == in) {
            int start = wPanel.getSelectionStart();
            int end = wPanel.getSelectionEnd();
            int size = end - start;
            wPanel.setSelelection(start + size/4, end - size/4);
            wPanel.update();

        } else if (e.getSource() == left) {
            int start = wPanel.getSelectionStart();
            int end = wPanel.getSelectionEnd();
            int size = end - start;
            wPanel.setSelelection(start - size/2, end - size/2);
            wPanel.update();

        } else if (e.getSource() == right) {
            int start = wPanel.getSelectionStart();
            int end = wPanel.getSelectionEnd();
            int size = end - start;
            wPanel.setSelelection(start + size/2, end + size/2);
            wPanel.update();

        } else if (e.getSource() == full) {
            wPanel.setSelelection(0,wPanel.getModel(0).getSize());
            wPanel.setSelelection(0,wPanel.getModel(1).getSize());
            wPanel.update();

        } else if (e.getSource() == scaleUp) {
            if (fst.isSelected() || both.isSelected()) {
                wPanel.getModel(0).setScale(
                    wPanel.getModel(0).getScale() *
                    Double.parseDouble(step.getText()));
            }
            if (snd.isSelected() || both.isSelected()) {
                wPanel.getModel(1).setScale(
                    wPanel.getModel(1).getScale() *
                    Double.parseDouble(step.getText()));
            }
            wPanel.update();
            String scaleText = wPanel.getModel(0).getScale() + "";
            if (scaleText.length() > 5) scaleText = scaleText.substring(0,5);
            scale.setText(scaleText);

        } else if (e.getSource() == scaleDown) {

            if (fst.isSelected() || both.isSelected()) {
                wPanel.getModel(0).setScale(
                    wPanel.getModel(0).getScale() /
                    Double.parseDouble(step.getText()));
            }
            if (snd.isSelected() || both.isSelected()) {

                wPanel.getModel(1).setScale(
                    wPanel.getModel(1).getScale() /
                    Double.parseDouble(step.getText()));
            }
            wPanel.update();
            String scaleText = wPanel.getModel(0).getScale() + "";
            if (scaleText.length() > 5) scaleText = scaleText.substring(0,5);
            scale.setText(scaleText);

        } else if (e.getSource() == reset) {
            if (fst.isSelected() || both.isSelected())
                wPanel.getModel(0).setScale(1);
            if (snd.isSelected() || both.isSelected())
                wPanel.getModel(1).setScale(1);
            wPanel.update();
            scale.setText("1");

        } else if (e.getSource() == max) {
            if (fst.isSelected() || both.isSelected()) {
                IWaveModel model = wPanel.getModel(0);
                autoScale(model,
                    wPanel.getSelectionStart(), wPanel.getSelectionEnd(),
                    WavePanel.WIDTH);
            }
            if (snd.isSelected() || both.isSelected()) {
                IWaveModel model = wPanel.getModel(1);
                autoScale(model,
                    wPanel.getSelectionStart(), wPanel.getSelectionEnd(),
                    WavePanel.WIDTH);
            }
            wPanel.update();
            String scaleText = wPanel.getModel(0).getScale() + "";
            if (scaleText.length() > 5) scaleText = scaleText.substring(0,5);
            scale.setText(scaleText);
        }

        else if (e.getSource() == all) {
            try {
                if (play1st.isSelected()) {
                    Player player = new Player(wPanel.getModel(0));
                    player.play();
                } else if (play2nd.isSelected()) {
                    Player player = new Player(wPanel.getModel(1));
                    player.play();
                }
            } catch (PlayerException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private void autoScale(IWaveModel model, int start, int end, int len) {
        model.setScale(1);
        int[] data = model.getSelection(start,end,len);
        int max = -1;
        for (int n = 0; n < len; n++) {
            int d = Math.abs(data[n]);
            if (d > max) max = d;
        }

        if (max == 0) return;

        double scale = 32767.0/max;
        model.setScale(scale);
    }
}
