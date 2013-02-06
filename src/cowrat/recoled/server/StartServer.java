package cowrat.recoled.server;

import cowrat.recoled.shared.MyFileFilter;
import javax.swing.* ;
import javax.swing.border.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;

/**
 * @author David King, S. Luz, M. Bouamrane
 *
 */
public class StartServer {
  // implements ProfileMakerControl{

  private JRadioButton optNew, optExisting ;
  private JTextField txtName, txtFile ;
  private JLabel lblName, lblDescription ;
  private JTextArea txtDescription ;
  private JButton cmdBrowse, cmdStart, cmdCancel, cmdQuit;
  private File file;
  private String profilename;
  private DefaultListModel plistModel = new DefaultListModel();
  public boolean fileselected = false;

  // profile making stuff
  private JList plist;
  private JLabel session_label, file_label, port_label;
  private JTextField session_text, file_text, port_text;
  private JPanel session_panel, file_panel, button_panel, participant_panel;
  private JButton connect;
  private JButton startrec;
  private JButton stoprec;
  private JCheckBox AudioListen_check; //Audio On/Off listening option
  private JCheckBox reca_check; //Audio On/Off recording option
  private boolean isAudioOptionSelected; 
  
  // deprecated stuff
  //private ProfileMaker pm;

  public StartServer() {
    super() ;

    try{
      FileInfoDialog fid = new FileInfoDialog();
      while (!fileselected)
        Thread.sleep(500);
    }
    catch (Exception e){
      System.err.println("Error getting file details"+e);
    }

    startServers();

    /** ProfileMaker deprecation

    //setTitle("Start Editor Server") ;
    //getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)) ;		
    //JTabbedPane tp = new JTabbedPane();
    // audio profiling tab


    JPanel apl = new JPanel();
    JPanel toppanel = new JPanel(new GridLayout(4,1));
    apl.setLayout(new GridLayout(2,1));
    
    session_panel = new JPanel();
    session_panel.setLayout(new FlowLayout());
    file_panel = new JPanel();
    button_panel = new JPanel();

    session_label = new JLabel("Enter IP and Port for RTP: ");
    session_text = new JTextField(12);
    session_text.setText("127.0.0.1");

    port_label = new JLabel(" : ");
    port_text = new JTextField(7);
    port_text.setText("20000");


    //reca_label = new JLabel("Record audio?    ");

    AudioListen_check= new JCheckBox(" Listen to (RTP) audio? ");

    reca_check = new JCheckBox(" Record audio ? ");

    connect = new JButton("Start Session");

    session_panel.add(session_label);
    session_panel.add(session_text);
    session_panel.add(port_label);
    session_panel.add(port_text);

    file_panel.add(AudioListen_check);
    file_panel.add(reca_check);


    participant_panel = new JPanel();

    plist = new JList(plistModel);
    plist.setPreferredSize(new Dimension(300, 150)) ;
    plist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    participant_panel.add(plist);

    cmdQuit = new JButton("Quit") ;

    button_panel.add(connect);
    button_panel.add(cmdQuit);
    
    //button_panel.add(stoprec);

    toppanel.add(session_panel);                                 
    toppanel.add(session_panel);
    toppanel.add(file_panel);
    toppanel.add(button_panel);
    apl.add(toppanel);
    apl.add(participant_panel);

    //tp.addTab("Audio settings", apl);
    //tp.addTab("Text settings", tpl);
    //getContentPane().add(boxAll, BorderLayout.CENTER) ;
    //getContentPane().add(btnBox, BorderLayout.SOUTH) ;
    getContentPane().add(apl);
   		
    cmdQuit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0) ;
        }
      }) ;
		
    connect.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {

          isAudioOptionSelected = AudioListen_check.isSelected() ;

          if ( ((JButton)e.getSource()).getText().equals("Start Session") && isAudioOptionSelected)

            {
              startListening();
              connect.setText(" Start Editor Server ");
            }
          else
            startServers();
        }
      }) ;    
    */
  }
	
  public void addParticipant(String cname)
  {
    plistModel.addElement(cname);	
  }

  public void removeParticipant(String cname)
  {
    plistModel.removeElement(cname);
  }

  public void hiliteActiveParticipant(String cname)
  {
    int i = plistModel.indexOf(cname);
    plist.addSelectionInterval(i,i);
  }

  public void unhiliteInactiveParticipant(String cname)
  {
    int i = plistModel.indexOf(cname);
    plist.removeSelectionInterval(i,i);
  }

  //need to distinguish case where audio profile is on or not...

  private void startServers() {
    /* pm deprecation
      if (pm == null){
      JOptionPane.showMessageDialog(null, "Please set RTP-listening on 'Audio settings' tab");
      return;
      }


    if (isAudioOptionSelected)
      {    //Need to start Editor without pm if ListenAudio_check is not selected...
        if(optNew.isSelected()) 
          {
            String fname = txtName.getText();
            setupPM();
            String pars[] = {fname, txtDescription.getText()} ;
            EditorServer es = new EditorServer(null, pars[0], pars[1], pm);
            es.start();
            // hand over control of  pm
            es.setAudioClientTab(plistModel);
            pm.setGUI(es);
            dispose() ;
      		}
        else if(file != null)
          {
            String fname = file.getPath();
            setupPM();
            String pars[] = {fname} ;
            EditorServer es = new EditorServer(pars[0], "No name", "No description", pm);
            es.start();
            // hand over control of  pm
            es.setAudioClientTab(plistModel);
            pm.setGUI(es);
            dispose() ;
      		}
      }//endif isAudioOptionSelected
    else 
      {
    */
    if(optNew.isSelected())
      {
        String pars[] = {txtName.getText(), txtDescription.getText()} ;				
        if(pars[0].length() > 0 & pars[1].length() > 0)
          {
            EditorServer es = new EditorServer(null, pars[0], pars[1]);
            es.start();
            // dispose() ;					
          }
      }
    else 
      {
        if(file != null)
          {
            String pars[] = {file.getPath()} ;
            EditorServer es = new EditorServer(pars[0], "No name", "No description");
            es.start();
            //dispose() ;	
          }
      }
    //}//endelse AudioOption not Selected
  }//end startServers() 
  
  /* pm deprecation
  public void setupPM(){
    try {
      // this will always set to true (see comment in ProfileMaker.setEnableAudioRecording())
      reca_check.setSelected(true);
      pm.setEnableAudioRecording(reca_check.isSelected());
      // set this to avoid confusing the user (and ourselves)
      pm.startRecording();
      // block until ProfileMaker is ready
      while(pm.startTime == 0){
        Thread.sleep(100);
      }
      System.err.println("PM started");
    }
    catch (Exception e){
      System.err.println("Error startin PM"+e);
      e.printStackTrace(System.err);
      System.exit(0);
    }
  }

  private void startListening(){
    String ip = session_text.getText() + "/" + port_text.getText();
    String fn = txtName.getText();
    profilename = Profile.makeProfileName(fn);
    System.out.println(profilename);
    pm = new ProfileMaker(ip, profilename);
    pm.setGUI(this);
    pm.start();
  }
  */
	
  public Box makeLeftAlignment(Component c) {
		
    Box box = new Box(BoxLayout.X_AXIS) ;
    box.add(c) ;
    box.add(Box.createGlue()) ;
    return box ;		
  }

  public static void main(String args[]) {
    
    StartServer self = new StartServer() ;
    //    self.startGUI();
    //self.pack();
    //self.setSize(430, 380) ;
    //self.show() ;
  }

  class FileInfoDialog extends JFrame{

    FileInfoDialog(){

      super("Enter file details");
      optNew = new JRadioButton("New Document") ;
      optNew.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setNewDocument(optNew.isSelected()) ;
          }	
        }) ;
      
      optExisting = new JRadioButton("Existing Document") ;
      optExisting.setHorizontalAlignment(JRadioButton.LEFT) ;
      optExisting.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setNewDocument(!optExisting.isSelected()) ;
          }
        }) ;
      
      lblName = new JLabel("Name: ") ;
      txtName = new JTextField() ;
      txtName.setMaximumSize(new Dimension(10000, 20)) ;
      txtName.setPreferredSize(new Dimension(200, 20)) ;
      lblDescription = new JLabel("Description: ") ;
      txtDescription = new JTextArea() ;
      JScrollPane scrDescription = new JScrollPane(txtDescription) ;
      scrDescription.setPreferredSize(new Dimension(100, 80)) ;
      
      
      Box nameBox = new Box(BoxLayout.X_AXIS) ;
      nameBox.add(lblName) ;
      nameBox.add(txtName) ;
      nameBox.setPreferredSize(new Dimension(200, 30)) ;
      
      Box newBox = new Box(BoxLayout.Y_AXIS) ;
      newBox.setBorder(new EmptyBorder(0, 25, 0, 0)) ;
      newBox.add(nameBox) ;
      newBox.add(Box.createVerticalStrut(5)) ;
      newBox.add(makeLeftAlignment(lblDescription)) ;
      newBox.add(scrDescription) ;
      
      txtFile = new JTextField() ;
      txtFile.setMaximumSize(new Dimension(10000, 20)) ;
      cmdBrowse = new JButton("Browse") ;
      cmdBrowse.setMaximumSize(new Dimension(80, 20)) ;
      
      Box extBox = new Box(BoxLayout.X_AXIS) ;
      extBox.setBorder(new EmptyBorder(0, 25, 0, 0)) ;
      extBox.add(txtFile) ;
      extBox.add(Box.createHorizontalStrut(5)) ;
      extBox.add(cmdBrowse) ;
      
      cmdCancel = new JButton("Quit") ;
      cmdStart = new JButton("Next...") ;
      
      Box btnBox = new Box(BoxLayout.X_AXIS) ;
      btnBox.setBorder(new EmptyBorder(10, 10, 10, 10)) ;
      btnBox.add(Box.createGlue()) ;
      btnBox.add(cmdCancel) ;
      btnBox.add(Box.createHorizontalStrut(10)) ;
      btnBox.add(cmdStart) ;
      btnBox.add(Box.createGlue()) ;
      
      Box boxAll = new Box(BoxLayout.Y_AXIS) ;
      boxAll.setBorder(new EmptyBorder(10, 10, 10, 20)) ;
      
      boxAll.add(makeLeftAlignment(optNew));
      boxAll.add(newBox);
      boxAll.add(Box.createGlue());
      boxAll.add(makeLeftAlignment(optExisting));
      boxAll.add(extBox);
      
      JPanel tpl = new JPanel();
      tpl.setPreferredSize(new Dimension(300, 300));
      //tp.setPreferredSize(new Dimension(310, 310));
      
      tpl.add(boxAll, BorderLayout.CENTER);
      tpl.add(btnBox, BorderLayout.SOUTH);
      
      cmdBrowse.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFileChooser dialog = new JFileChooser() ;
            dialog.addChoosableFileFilter(new MyFileFilter("Collabortive Document File (*.cde)", ".cde")) ;
            dialog.setAcceptAllFileFilterUsed(false) ;
            if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
              file = dialog.getSelectedFile() ;		
              txtFile.setText(file.getPath()) ;
            }
          }
        }) ;
      
      cmdCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.exit(0) ;
          }
        }) ;
      
      cmdStart.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if ( file == null && 
                 (txtName.getText().length() == 0 || txtDescription.getText().length() == 0 )){
              JOptionPane.showMessageDialog(null, "Enter filename and description");
              return;    
            }
            fileselected = true;
            dispose();
          }
        }) ; 
      getContentPane().add(tpl);
      setNewDocument(true);
      pack();
      setSize(430, 380) ;
      show();
    }

    public void setNewDocument(boolean b) {
      optNew.setSelected(b) ;
      lblName.setEnabled(b) ;
      txtName.setEnabled(b) ;
      lblDescription.setEnabled(b) ;
      txtDescription.setEnabled(b) ;
      
      optExisting.setSelected(!b) ;
      txtFile.setEnabled(!b) ;
      cmdBrowse.setEnabled(!b) ;
    }
  }
}
