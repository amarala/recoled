
package cowrat.recoled.audio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.io.File;
import javax.media.*;
/**
 *  Sample GUI element for ProfileMaker
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: ProfileMakerGUI.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated The JMF-based classes in this package have been dropped
 * in favour of our own implementations. Use
 * cowrat.recplaj.recorder.Recorder for audio recording and
 * cowrat.rexplore.hnm.MakeHanmerProfile for activity logging
 * (post-processing) instead. THIS PACKAGE IS NO LONGER USED IN RECOLED AND WILL SOON BE REMOVED.
*/
public class ProfileMakerGUI extends JFrame implements ProfileMakerControl {

  ProfileMaker pm;
  private int count = 0;
  private JTextArea participant_text;
  private JLabel session_label, file_label, port_label;
  private JTextField session_text, file_text, port_text;
  private JPanel session_panel, file_panel, button_panel, participant_panel;
  private JButton connect;
  private JButton startrec;
  private JButton stoprec;
  private JCheckBox reca_check;
  private static final String CONNECT = "Connect";
  private static final String CONNARC = "Connect & Start Recoding";
  private static final String STOPREC = "Quit";

  public ProfileMakerGUI(ProfileMaker parent){
    super("RTP Profile Maker"); 
    pm = parent;
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception exc) {
      System.err.println("Error loading L&F: " + exc);
    }
    
    JPanel toppanel = new JPanel(new GridLayout(4,1));
    getContentPane().setLayout(new GridLayout(2,1));
    
    this.addWindowListener(
                           new WindowAdapter(){
                             public void windowClosing(WindowEvent e)
                             {
                               System.exit(0);
                             }
                           }
                           );

    session_panel = new JPanel();
    file_panel = new JPanel();
    button_panel = new JPanel();

    session_label = new JLabel("Enter IP and Port: ");
    session_text = new JTextField(12);
    session_text.setText("127.0.0.1");

    port_label = new JLabel("   :  ");
    port_text = new JTextField(10);
    port_text.setText("20000");

    file_label = new JLabel("Profile Name: ");
    file_text = new JTextField(15);
    file_text.setText("aaa");

    //reca_label = new JLabel("Record audio?    ");
    reca_check = new JCheckBox("Record audio? ");

    connect = new JButton(CONNECT);
    connect.addActionListener(new Continue());
    startrec = new JButton(CONNARC);
    startrec.addActionListener(new Continue());
    stoprec = new JButton(STOPREC);
    stoprec.setEnabled(false);
    stoprec.addActionListener(new Continue());

    session_panel.add(session_label);
    session_panel.add(session_text);
    session_panel.add(port_label);
    session_panel.add(port_text);

    file_panel.add(file_label);
    file_panel.add(file_text);
    file_panel.add(reca_check);


    participant_panel = new JPanel();
    participant_text = new JTextArea(10,20);

    participant_panel.add(participant_text);

    button_panel.add(connect);
    button_panel.add(startrec);
    button_panel.add(stoprec);

    toppanel.add(session_panel);                                 
    toppanel.add(session_panel);
    toppanel.add(file_panel);
    toppanel.add(button_panel);
    getContentPane().add(toppanel);
    getContentPane().add(participant_panel);

    pack();
    show();
  }

  private void setupPM() {
    String ip;
    ip = session_text.getText() + "/" + port_text.getText();
    String file = file_text.getText();
    System.out.println(file);
    pm.setArgs(ip, file);
    // this will always set to true (see comment in ProfileMaker.setEnableAudioRecording())
    pm.setEnableAudioRecording(reca_check.isSelected());
    // set this to avoid confusing the user (and ourselves)
    reca_check.setSelected(true);
    pm.start();
  }

  public void stopPM()
  {
    pm.quit = true;
    int n = 0;
    try {
      while(!pm.completed)
        {
          Thread.sleep(1000);
          System.out.println("Waiting to close");
          n++;
        }
    } catch (Exception e){}
    System.out.println("CLOSED");
    System.exit(0);
  }

  public void addParticipant(String cname)
  {
    String current_text = participant_text.getText();
    String new_text = current_text + cname + "\n";
    participant_text.setText(new_text);
    count++;
    pack();
    show();
  }
  public void removeParticipant(String cname)
  {
    // never mind
  }
  public void hiliteActiveParticipant(String cname)
  {
    // never mind
  }

  public void unhiliteInactiveParticipant(String cname)
  {
    // never mind
  }


  //When the user has entered all info start the PLIST.
  public class Continue implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      JButton src = (JButton) e.getSource();
      if(src == connect)
        {
          setupPM();
          stoprec.setEnabled(true);
          connect.setEnabled(false);
          reca_check.setEnabled(false);
          startrec.setText("Start recording");
        } 
      else if (src == startrec)
        {
          if (connect.isEnabled())
            setupPM();
          connect.setEnabled(false);
          startrec.setEnabled(false);
          reca_check.setEnabled(false);
          stoprec.setEnabled(true);
          //pm.wanted_num_part = count - 1;
          pm.startRecording();
        }
      else
        {
          stoprec.setEnabled(false);
          stopPM();
          connect.setEnabled(true);
          startrec.setEnabled(true);
          reca_check.setEnabled(true);
          startrec.setText(CONNARC);
        }
    }
  }
}
