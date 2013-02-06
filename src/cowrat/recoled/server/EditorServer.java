package cowrat.recoled.server;

import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;
import java.util.Iterator ;
import java.util.Vector ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.event.* ;
import javax.swing.table.* ;
import javax.swing.text.BadLocationException;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.* ;
import cowrat.recoled.audio.*;
import cowrat.recoled.shared.*;
import cowrat.recplaj.recorder.Recorder;

public class EditorServer extends JFrame 
  implements EditorDebugFlags //, ProfileMakerControl
{

  // Variables...........................................................................................................
  private Vector clients ;
  private EditorDocument document ;
  private LockManager lockManager ;
  private Paragraphs paragraphs ;
  private Highlights highlights ;
  private int nextClientId ;
  
  private Recorder recorder;  
    
  private NetworkedClient client ;
  private TextChannelConsumer textChannelConsumer ;
  private ClientChannelConsumer clientChannelConsumer ;
  private Channel textChannel ;
  private Channel clientChannel ;
    
  private DocumentPanel documentPanel ;
  private ClientsPanel clientsPanel ;
  private EditorServer self ;
  private DefaultListModel plistModel = new DefaultListModel();
  private JTabbedPane tabPane = new JTabbedPane() ;
  private JList plist;

  // PM DEPRECATION
  //private ProfileMaker pm;

  private JButton cmdStopAudio; //used by profmaker
  private JButton startRTPrecording;
  private JButton stopRTPrecording; //used by recplaj
  
  private boolean isAudioOptionSelected; 
  
  private static int timestampsCounter = 0;  
  
  // Constructors......................................................................................................
  
  //const without pm (audio option not selected )

  public EditorServer(String fileName, String name, String desc) {
    super() ;
    self = this ;
    if(fileName==null || !loadDocument(fileName)) {
      clients = new Vector() ;
      document = new EditorDocument(name, desc, "", System.currentTimeMillis()) ;
            
      try{
        //ascii code for first blue line...
        document.insertString(0,"·-·+·*=·x·\n",document.getStyle("line") );}
            
      catch(BadLocationException ble){
        System.out.println("EditorServer->const: BadLocationException") ;}
	            
           
      paragraphs = new Paragraphs(document) ;
      lockManager = new LockManager(clients, document, paragraphs) ;
      highlights = new Highlights(lockManager, document) ;
            
      nextClientId = 1 ;
    }

    isAudioOptionSelected = false ;
    //to avoid unecessary Audio - Text participant matching

    documentPanel = new DocumentPanel() ;
    updateParagraphList() ;
        
    clientsPanel = new ClientsPanel() ;
    clientsPanel.updateClientList() ;
        
    Icon clockIcon = getImageIcon ("images/clock.gif");
        
    Icon clockIcon2 = getImageIcon ("images/clock2.gif");
        
    startRTPrecording = new JButton("START RTP Recording", clockIcon);
        
    startRTPrecording.setRolloverIcon ( clockIcon2 );
        
    RecordingHandler recHandler = new RecordingHandler(self);
        
    startRTPrecording.addActionListener ( recHandler );
        
    Icon stopIcon = getImageIcon ("images/stop.gif");
        
    stopRTPrecording = new JButton("STOP RTP recording",stopIcon);
        
    stopRTPrecording.setEnabled(false);
        
    stopRTPrecording.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          
	        recorder.endRecording();
	        
	        stopRTPrecording.setEnabled(false);	        
	        startRTPrecording.setEnabled(true);
        }
        
      });
        
        
    Container container = getContentPane();
        
    container.setLayout( new FlowLayout() );
        
    container.add( startRTPrecording );
    container.add( stopRTPrecording  );
                
    //JTabbedPane tabPane = new JTabbedPane() ;
    tabPane.add("Document", documentPanel) ;
    tabPane.add("Text Clients", clientsPanel) ;
    container.add(tabPane, BorderLayout.CENTER) ;
                
    setTitle("EditorServer") ;
    setSize(new Dimension(800, 600)) ;
        
    addWindowListener(new WindowAdapter() {
        	
        public void windowClosing(WindowEvent e) {
          lockManager.saveDocument(null, highlights, self) ;
          System.exit(0) ;
        }
      }) ;
        
    JMenuItem mnuSave = new JMenuItem("Save") ;
    mnuSave.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser dialog = new JFileChooser() ;
          dialog.addChoosableFileFilter(new MyFileFilter("Collabortive Document File (*.cde)", ".cde")) ;
          dialog.setAcceptAllFileFilterUsed(false) ;
				
          if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {	
            lockManager.saveDocument(dialog.getSelectedFile().getPath(), highlights, self) ;
          }
        }
      }) ;
        
    JMenuItem mnuSaveXML = new JMenuItem("Save XML") ;
    mnuSaveXML.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser dialog = new JFileChooser() ;
          dialog.addChoosableFileFilter(new MyFileFilter("XML File (*.xml)", ".xml")) ;
          dialog.setAcceptAllFileFilterUsed(false) ;
				
          if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {	
            lockManager.saveXML(dialog.getSelectedFile().getPath(), highlights, self) ;
          }
        }
      }) ;
        
    JMenu mnuFile = new JMenu("File") ;
    mnuFile.add(mnuSave) ;
    mnuFile.add(mnuSaveXML) ;
    JMenuBar menu = new JMenuBar() ;
    menu.add(mnuFile) ;
    setJMenuBar(menu) ;
        
    Thread backupThread = new Thread() {
        public void run() {
          while(1==1) {
            yield() ;
            try {
              //sleep(1000) ;
              sleep(5*60*1000) ;
              lockManager.saveDocument(null, highlights, self) ;
            } catch (Exception e) {
              System.out.println("EditorServer: backupThread. error") ;
              e.printStackTrace() ;
            }
          }
        		
        }
      } ;
    backupThread.setDaemon(true) ;
    backupThread.start() ;

  }//endof const WITHOUT audio profile maker

  //const with audio profile maker

  /*  PM DEPRECATION
  public EditorServer(String fileName, String name, String desc, ProfileMaker pm) {
    super() ;
    self = this;
    this.pm = pm;
    if(fileName==null || !loadDocument(fileName)) {
      clients = new Vector() ;
      document = new EditorDocument(name, desc, "", pm.startTime) ;
      
      try{
        document.insertString(0,"·-·+·*=·x·\n",document.getStyle("line") );
      }
            
      catch(BadLocationException ble){
        System.out.println("EditorServer (PM)->const: BadLocationException") ;}
      
      paragraphs = new Paragraphs(document) ;
      lockManager = new LockManager(clients, document, paragraphs) ;
      highlights = new Highlights(lockManager, document) ;
            
      nextClientId = 1 ;
    }

    isAudioOptionSelected = true ;
    //for Audio - Text participant matching
       
    documentPanel = new DocumentPanel() ;
    updateParagraphList() ;
        
    clientsPanel = new ClientsPanel() ;
    clientsPanel.updateClientList() ;
    
    Icon clockIcon = new ImageIcon ("images/clock.gif");
        
    Icon clockIcon2 = new ImageIcon ("images/clock2.gif");
        
    startRTPrecording = new JButton("Reset START Time", clockIcon);
        
    startRTPrecording.setRolloverIcon ( clockIcon2 );
        
    RecordingHandler clockHandler = new RecordingHandler(this);
        
    startRTPrecording.addActionListener ( clockHandler );
        
    Container container = getContentPane();
        
    container.setLayout( new FlowLayout() );
        
    container.add( startRTPrecording );
        
    tabPane.add("Document", documentPanel) ;
    tabPane.add("Text Clients", clientsPanel) ;
    container.add(tabPane, BorderLayout.CENTER) ;
        
    setTitle("EditorServer") ;
    setSize(new Dimension(800, 600)) ;

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          Object[] options = { "Quit", "Cancel" };
          int d = JOptionPane.showOptionDialog(null, "Are you sure?", "Quit editor?",
                                               JOptionPane.YES_NO_OPTION, 
                                               JOptionPane.QUESTION_MESSAGE,
                                               null, options, options[1]);
          if (d == JOptionPane.YES_OPTION){
            lockManager.saveDocument(null, highlights, self);
            stopAudioProfiling();
            dispose();
            System.exit(0) ;
          }
        }
      }) ;
        
    JMenuItem mnuSave = new JMenuItem("Save") ;
    mnuSave.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser dialog = new JFileChooser(self.pm.getSaveDir()) ;
          dialog.addChoosableFileFilter(new MyFileFilter("Collabortive Document File (*.cde)", ".cde")) ;
          dialog.setAcceptAllFileFilterUsed(false) ;
				
          if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {	
            lockManager.saveDocument(dialog.getSelectedFile().getPath(), highlights, self) ;
          }
        }
      }) ;
        
    JMenuItem mnuSaveXML = new JMenuItem("Save XML") ;
    mnuSaveXML.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser dialog = new JFileChooser(self.pm.getSaveDir()) ;
          dialog.addChoosableFileFilter(new MyFileFilter("XML File (*.xml)", ".xml")) ;
          dialog.setAcceptAllFileFilterUsed(false) ;
				
          if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            lockManager.saveXML(dialog.getSelectedFile().getPath(), highlights, self);
            lockManager.saveProfile(Profile.makeProfileName(dialog.getSelectedFile().getPath()), 
                                    self.pm.getProfile());
          }
        }
      }) ;
        
    JMenu mnuFile = new JMenu("File") ;
    mnuFile.add(mnuSave) ;
    mnuFile.add(mnuSaveXML) ;
    JMenuBar menu = new JMenuBar() ;
    menu.add(mnuFile) ;
    setJMenuBar(menu) ;
        
    Thread backupThread = new Thread() {
        public void run() {
          while(1==1) {
            yield() ;
            try {
              //sleep(1000) ;
              sleep(5*60*1000) ;
              lockManager.saveDocument(null, highlights, self) ;
            } catch (Exception e) {
              System.out.println("EditorServer: backupThread. error") ;
              e.printStackTrace() ;
            }
          }
        		
        }
      } ;
    backupThread.setDaemon(true) ;
    backupThread.start() ;
    
  }//end const with audioPM
  */
  
  //assign unique Id number to timestamps;
  public synchronized int getTimestampId()
  
  {
    timestampsCounter++;
	  	
    return timestampsCounter;
	  	
  }
  	
  public void addToMapTimestamp(Vector pars, int timestampId)
  	
  {        
    for (int i=0 ; i<pars.size() ; i++)
	  	
      {
        Long parId = (Long) ( pars.elementAt(i) ) ;
		  		
        paragraphs.mapTimestamp( parId, timestampId);
		  		
      }	  	
	  	
  }

  

  public void updateParagraphList() {
    documentPanel.parList.setModel(lockManager.getListModel()) ;
  }
    
  private static String sessionName = "EditorSession";
    
  public EditorClient getEditorClient(NetworkedClient c) {
    Iterator i = clients.iterator() ;
        
    while (i.hasNext()) {
      EditorClient ec = (EditorClient) i.next() ;
      if (ec.getName() == c.getName()) {
        return ec ;
      }
    }
    return null ;
  }
    
  public EditorClient getEditorClient(int idNumber) {
    Iterator i = clients.iterator() ;
        
    while (i.hasNext()) {
      EditorClient ec = (EditorClient) i.next() ;
      if (ec.getIdNumber() == idNumber) {
        return ec ;
      }
    }
    return null ;
  }
    
	public ImageIcon getImageIcon(String path){
    java.net.URL i = ClassLoader.getSystemResource(path);
  
		return new ImageIcon(i) ;
	}    

  public void addParticipant(String cname)
  {
    plistModel.addElement(cname);
  }

  public void removeParticipant(String cname)
  {
    plistModel.addElement(cname);
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

  /* PM DEPRECATION
  public void setAudioClientTab (DefaultListModel plm)
  {
    this.plistModel = plm;
    plist = new JList(plistModel);
    plist.setPreferredSize(new Dimension(500, 350)) ;
    final JPanel p = new JPanel();
    p.setLayout(new GridLayout(2,1));
    final JPanel p1 = new JPanel();
    final JPanel p2 = new JPanel();
    cmdStopAudio = new JButton("Stop audio monitoring");
    cmdStopAudio.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          stopAudioProfiling();
          tabPane.remove(p);
        }
      });
    p1.add(plist);
    p2.add(cmdStopAudio);
    p.add(p1); p.add(p2);
    tabPane.add("Audio Clients", p);
  }

  public void stopAudioProfiling()
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
  }
  */

  
    
    
  public boolean loadDocument(String fileName) {
    try {    		
      File file = new File(fileName) ;
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(file)) ;
      SavedDocument doc = (SavedDocument) in.readObject() ;
    		
      document = new EditorDocument(doc.getTitle(), doc.getDescription(),
                                    doc.getText(), doc.getStartTime());
      paragraphs = new Paragraphs(document, doc.getParagraphsVector());
      clients = doc.getClients();
      Iterator i = clients.iterator();
      while (i.hasNext()){
        EditorClient c = (EditorClient) i.next();
        c.setPresent(false) ;
    			
        if(c.getIdNumber() > nextClientId)
          nextClientId = c.getIdNumber() ;
      }
      nextClientId ++ ;
    		
      lockManager = new LockManager(clients, document, paragraphs) ;
      highlights = new Highlights(lockManager, document, doc.getHighlightTypes(), doc.getHighlights()) ;

      return true ;
    } catch(Exception e) {
      System.out.println("EditorServer: loadDocument. error") ;
      e.printStackTrace() ;
      return false ;
    }
    	
    	
  }//endof const WITH audio profile maker
    
  // highlight stuff...................................................................................................
    
  public void highlightAdded(Message.HighlightAddMsg m) {
    if (EditorServer_Debug) 
      System.err.println("EditorServer: highlightAdded.") ;
        
    highlights.addHighlight(m.getId(), m.getHighlightType(), m.getStartPar(), m.getStartOffset(), m.getEndPar(), m.getEndOffset()) ;
    EditorClient c = getEditorClient(m.getClientId()) ;
    try {
      textChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: highlightAdded: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void highlightDeleted(Message.HighlightDeleteMsg m) {
    if (EditorServer_Debug) 
      System.err.println("EditorServer: highlightDeleted.") ;
        
    highlights.deleteHighlight(m.getId()) ;
    EditorClient c = getEditorClient(m.getClientId()) ;
    try {
      textChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: highlightDeleted: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void highlightEdited(Message.HighlightEditMsg m) {
    if (EditorServer_Debug) 
      System.err.println("EditorServer: highlightEdited.") ;
        
    highlights.editHighlight(m.getId(), m.getStartPar(), m.getStartOffset(), m.getEndPar(), m.getEndOffset()) ;
        
    try {
      textChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: highlightEdited: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void highlightTypeAdded(Message.HighlightTypeMsg m) {
    if (EditorServer_Debug) 
      System.err.println("EditorServer: highlightTypeAdded.") ;
        
    highlights.addHighlightType(m.getId(), m.getName(), m.getColor()) ;
        
    try {
      textChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: highlightTypeAdded: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  // gesture stuff.....................................................................................................
    
  public void gestureCircle(Message.GestureCircleMsg m) {
	  
    if (EditorServer_Debug) 
      System.err.println("\nEditorServer: gestureCircle." 
                         + String.valueOf(m.getId())) ;
        
    try {
      clientChannel.sendToOthers(client, new Data(m)) ;
      EditorClient c = getEditorClient(m.getClientId()) ; 
      c.addGestureAction(System.currentTimeMillis(), m.getId(), m.getPar(), m.getX(), m.getY(), this) ;
      clientsPanel.updateActionTableFor(c) ;
    } catch (Exception e) {
      System.err.println("EditorServer: gestureCircle: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public long getParIdForGesture(long startParId, int y) {
    Paragraph startPar = lockManager.getParFromId(startParId) ;
    int startParY = documentPanel.getYValueFromOffset(startPar.getOffset()) ;
        
    int offset = documentPanel.getOffsetFromPoint(new Point(25, startParY + y)) ;
    return lockManager.getParFromOffset(offset).getID() ;
  }
    
  public void gestureLine(Message.GestureLineMsg m) {
    if (EditorServer_Debug) 
      System.err.println("EditorServer: gestureLine." 
                         + String.valueOf(m.getId())) ;
        
    try {
      clientChannel.sendToOthers(client, new Data(m)) ;
      EditorClient c = getEditorClient(m.getClientId()) ;
      long par = getParIdForGesture(m.getPar(), m.getAY()) ;
      c.addGestureAction(System.currentTimeMillis(), m.getId(), par, m.getAX(), m.getAY(), this) ;
      c.addGestureAction(System.currentTimeMillis(), m.getId(), par, m.getBX(), m.getBY(), this) ;
      clientsPanel.updateActionTableFor(c) ;
    } catch (Exception e) {
      System.err.println("EditorServer: gestureLine: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }	
    
  public String getTextForGesture(long parId, Point topLeft, Point bottomRight) {
	  
	  
	  
    try {
      Paragraph p = lockManager.getParFromId(parId) ;
	        
      int parY = documentPanel.textPane.modelToView(p.getOffset()).y ;
	        
      topLeft.y = topLeft.y + parY ;
      bottomRight.y = bottomRight.y + parY ;

      int startOffset = documentPanel.textPane.viewToModel(topLeft) ;
      int endOffset = documentPanel.textPane.viewToModel(bottomRight) ;

      while (startOffset > 0 && Character.isLetterOrDigit((document.getText(startOffset-1, 1).charAt(0))))
        startOffset -- ;
            
      while (endOffset < document.getLength() && Character.isLetterOrDigit((document.getText(endOffset, 1).charAt(0))))
        endOffset ++ ;
		    
      String text = document.getText(startOffset, endOffset- startOffset) ;
      return text ;
    } catch (Exception e) {
      System.out.println("EditorClient: addGestureAction. error identifying text") ;
      e.printStackTrace() ;
      return "" ;
    }
    
    
    
    // return "PLACEBO";
  }
    
  // client stuff.........................................................................................................
    
  public void clientAppeared() {
    if(EditorServer_Debug) 
    	System.out.println("EditorServer: clientAppeared.") ;

    // send client list to everybody
    try {
      Iterator i = clients.iterator() ;
            
      while (i.hasNext()) {
        EditorClient ec = (EditorClient) i.next() ;
        if (ec.isPresent()) 
          clientChannel.sendToOthers(client, new Data(ec.getMessage())) ;
      }
            
            
    } catch (Exception e) {
      System.err.println("EditorServer: clientAppeared: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void clientJoined(Message.ClientJoinMsg m) {
	  
	         
    Iterator i = clients.iterator() ;
        
    while (i.hasNext()) {
      EditorClient ec = (EditorClient) i.next() ;
      if (ec.isPresent()){
        if (m.getName().equals(ec.getName())){
          sendClientReject(m.getKeyValue(), Message.ClientRejectMsg.REASON_NAME) ;
          return ;
        }
        if (m.getColorCode() == ec.getColorCode()) {
          sendClientReject(m.getKeyValue(), Message.ClientRejectMsg.REASON_COLOR) ;
          return ;
        }
      }
    }
        
    // clientAccepted
    // mach... added ClientIPaddress + added server to EditorClient const
    EditorClient newClient = new EditorClient(this,nextClientId, m.getName(), m.getColorCode(), m.getKeyValue(),m.getClientIPaddress()) ;

    if( EditorServer_Debug )
      System.out.println(">>> In EditorServer.clientJoined  : client NAME is : *"+m.getName() +"* IPAddress : is : *"+ m.getClientIPaddress() +"*");

    clients.add(newClient) ;
    nextClientId ++ ;
    clientsPanel.updateClientList() ;

    //here... match Audio & Text client
    //iterate through list of audio client

    String audioClientIP="";

    int offset = -1; 

    if( isAudioOptionSelected )

      {

        if( EditorServer_Debug )
          System.out.println("\n>>>In Client Accepted !!!");

        for(int n=0; n< plistModel.getSize();n++)

          {

            audioClientIP = (plistModel.getElementAt(n)).toString();

            offset = audioClientIP.indexOf('@');

            audioClientIP = audioClientIP.substring(offset+1);

            if ( audioClientIP.equals( m.getClientIPaddress() ) )

              {
				
                System.out.println("MATCH found!!!" + m.getName() + "<>" + audioClientIP);

                //change ...

                plistModel.set(n,m.getName());

              }
		
          }

      }//endif isAudioOptionSelected
        
    try {
      clientChannel.sendToOthers(client, new Data(newClient.getMessage())) ;
      sendDocumentState(nextClientId - 1) ;
      sendHighlightTypes() ;
      sendHighlights() ;
    } catch (Exception e) {
      System.err.println("EditorServer: clientJoined: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void clientLeft(Message.ClientLeaveMsg m) {
    System.out.println("EditorServer: clientLeft") ;
        
    EditorClient ec = getEditorClient(m.getClientId()) ;
    ec.setPresent(false) ;
    clientsPanel.updateClientList() ;
        
    try {
      clientChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: clientLeft: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void sendClientReject(int keyValue, int reason) {
    Message.ClientRejectMsg msg = new Message.ClientRejectMsg(-1, keyValue, reason) ;
    try {
      clientChannel.sendToOthers(client, new Data(msg)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: clientRejected: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void sendDocumentState(int clientId) {
    Message.DocumentStateMsg msg = new Message.DocumentStateMsg(clientId, lockManager.getDocumentAsXML()) ;
    try {
      clientChannel.sendToOthers(client, new Data(msg)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: sendDocumentState: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
        
  }
    
  public void sendHighlightTypes() {
    //      send highlightTypes to everybody
    try {
      Iterator i = highlights.getHighlightTypes().iterator() ;
            
      while (i.hasNext()){
        HighlightType type = (HighlightType) i.next() ;
        
        if (EditorServer_Debug) 
        	System.out.println("EditorServer: sendHighlightTypes. " + String.valueOf(type.getId())) ;
        	
        Message.HighlightTypeMsg msg = new Message.HighlightTypeMsg(-1, type.getId(), type.getName(), type.getColor()) ;
        textChannel.sendToOthers(client, new Data(msg)) ;
      }
    } catch (Exception e) {
      System.err.println("EditorServer: sendHighlightTypes: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  public void sendHighlights() {
        
    // send highlights to everybody
    try {
      Iterator i = highlights.getHighlights().iterator() ;
      while (i.hasNext()){
        Highlights.Highlight h = (Highlights.Highlight) i.next() ;
        Paragraph sPar = lockManager.getParFromOffset(h.getStart()) ;
        Paragraph ePar = lockManager.getParFromOffset(h.getEnd()) ;
	            
        if (EditorServer_Debug) 
        	{
	        	System.out.println("StartPar: " + sPar.toString()) ;
        		System.out.println("EndPar: " + ePar.toString()) ;
        		
          }
	
        Message.HighlightAddMsg msg = new Message.HighlightAddMsg(-1, h.getId(), h.getType().getId(), sPar.getID(), ePar.getID(), h.getStart() - sPar.getOffset(), h.getEnd() - ePar.getOffset()) ;
        textChannel.sendToOthers(client, new Data(msg)) ;
      }
    } catch (Exception e) {
      System.err.println("EditorServer: sendHighlights: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
        
  }
  // text stuff..........................................................................................................
   
  //text insertion for single character insertion
 
  public void textInserted(Message.TextInsertMsg m) {
    if (EditorServer_Debug) 
    	System.err.println("EditorServer-> textInserted.") ;
        
    try {
      int ClientId = m.getClientId() ;
      int offset = m.getOffset();
      String characterInserted = m.getText();
      
      if (EditorServer_Debug) 
      	System.out.println("EditorServer-> textInserted : *" + characterInserted +"*");

      Vector pars = lockManager.textInserted(m.getPar(), offset,characterInserted , ClientId ) ;

      textChannel.sendToOthers(client, new Data(m)) ;
      EditorClient c = getEditorClient(ClientId) ;

      // new condition inserted to avoid timestamp generation if the character
      // is a newline
      
     
      if ( characterInserted.equals("\n") )
        {
          if (EditorServer_Debug) 
            System.out.println("EditorServer-> textInserted : attempting to insert a newLine") ;
        }
      c.addTextInsertAction( System.currentTimeMillis(), pars, offset, characterInserted) ; 
      clientsPanel.updateActionTableFor(c) ;
      updateParagraphList() ;

    
    } catch (Exception e) {
      System.err.println("EditorServer-> textInserted: error receiving-sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
  //text insertion for Pasting operation

  public void textInserted(Message.TextPasteMsg m) {
    
        
    try {

      int SenderId = m.getClientId();
      int offset = m.getOffset();
      String textPasted = m.getText();

      if (EditorServer_Debug)
        System.err.println("EditorServer->textinserted : PASTED by : " + SenderId) ;

      Vector pars=null;
      try { 
	      pars = lockManager.textInserted(m.getPar(), offset, textPasted  ,SenderId ) ;
	      if (EditorServer_Debug) 
          System.err.println("\n+=*%EditorServer--> textInserted recovered VECTOR..."); }
      
      catch (Exception e) {
        System.err.println("\n+=*%EditorServer--> textInserted VECTOR error ") ;}
      
      textChannel.sendToOthers(client, new Data(m)) ;

      EditorClient SenderClient = getEditorClient(SenderId) ;
      SenderClient.addTextPasteAction(System.currentTimeMillis(), pars, offset, textPasted ) ;
      clientsPanel.updateActionTableFor(SenderClient ) ;
      updateParagraphList() ;
    } catch (Exception e) {
      System.err.println("\nEditorServer--> textPasted: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }

  //text deletion for single character insertion

  public void textDeleted(Message.TextDeleteMsg m) {
    if (EditorServer_Debug) System.err.println("EditorServer: textDeleted.") ;
        
    try{

      long StartPar = m.getStartPar();
      long EndPar = m.getEndPar();
      int StartOffset = m.getStartOffset();
      int EndOffset = m.getEndOffset();


      String text = lockManager.getText(StartPar,StartOffset ,EndPar ,EndOffset  ) ;
      Vector pars = lockManager.textDeleted(StartPar, StartOffset, EndPar, EndOffset ) ;
      textChannel.sendToOthers(client, new Data(m)) ;
      EditorClient c = getEditorClient(m.getClientId()) ;
      c.addTextDeleteAction(System.currentTimeMillis(), pars, StartOffset, EndOffset , text) ;
      clientsPanel.updateActionTableFor(c) ;
      updateParagraphList() ;
    } catch (Exception e) {
      System.err.println("EditorServer: textDeleted---> error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }

  //text Cut...

  public void textDeleted(Message.TextCutMsg m)
  {
    if (EditorServer_Debug) 
    	System.err.println("EditorServer ->textCut.") ;
        
    try{

      long StartPar = m.getStartPar();
      long EndPar = m.getEndPar();
      int StartOffset = m.getStartOffset();
      int EndOffset = m.getEndOffset();


      String text = lockManager.getText(StartPar,StartOffset ,EndPar ,EndOffset  ) ;
      Vector pars = lockManager.textDeleted(StartPar, StartOffset, EndPar, EndOffset ) ;
      textChannel.sendToOthers(client, new Data(m)) ;
      EditorClient c = getEditorClient(m.getClientId()) ;
      c.addTextCutAction(System.currentTimeMillis(), pars, StartOffset, EndOffset , text) ;
      clientsPanel.updateActionTableFor(c) ;
      updateParagraphList() ;
    } catch (Exception e) {
      System.err.println("EditorServer---> textDeleted(cut): error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }

    
  // lock stuff..........................................................................................................
    
  public void lockRequested(Message.LockRequestMsg m) {
    if (EditorServer_Debug) System.err.println("EditorServer -> lockRequested.") ;
        
    /*
      java.util.Timer t = new java.util.Timer() ;
      t.schedule(new TimerTask() { 
      public void run() {
      client.lockDenied(id) ;
      }
      }, 3000) ;
    */  
        
    if(lockManager.lockRequested(m.getStartPar(), m.getEndPar(), m.getClientId())) {
	    
	    if (EditorServer_Debug) 
        System.out.println("Lock Granted") ;
            
      Message.LockGrantMsg reply = new Message.LockGrantMsg(m.getClientId(), m.getStartPar(), m.getEndPar(), m.getIdNumber()) ;
      try {
        textChannel.sendToOthers(client, new Data(reply)) ;
      } catch (Exception e) {
        System.err.println("EditorServer: lockRequested: error sending lock granted") ;
        if (EditorServer_Debug) 
          e.printStackTrace() ;
      }
    }
    else {
	    if (EditorServer_Debug) 
        System.out.println("Lock Denied") ;
      // send lock denied to sender
            
      Message.LockDenyMsg reply = new Message.LockDenyMsg(m.getClientId(), m.getIdNumber()) ;
      try {
        textChannel.sendToOthers(client, new Data(reply)) ;
      } catch (Exception e) {
        System.err.println("EditorServer: lockRequested: error sending lock denied") ;
        if (EditorServer_Debug) 
          e.printStackTrace() ;
      }
    }
        
        
  }
    
  public void lockReleased(Message.LockReleaseMsg m) {
    if (EditorServer_Debug) 
    	System.err.println("EditorServer: lockReleased.") ;
        
    lockManager.lockReleased(m.getStartPar(), m.getEndPar(), m.getClientId()) ;
    try {
      textChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: lockReleased: error sending lock released") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
        
  }
    
  // scroll stuff...................................................................................................................
    
  public void scrollMoved(Message.ScrollMoveMsg m) {
    // if(EditorServer_Debug) System.err.println("EditorServer: scrollMoved.") ;
    try {
      clientChannel.sendToOthers(client, new Data(m)) ;
    } catch (Exception e) {
      System.err.println("EditorServer: scrollMoved: error sending msg") ;
      if (EditorServer_Debug) 
        e.printStackTrace() ;
    }
  }
    
  // main ..........(SL: now renamed).................................................
  public void start() {
        
    //EditorServer server ;
    	
    //
    //if(args.length == 1)
    // server = new EditorServer(args[0], "No Name", "No Description") ;
    // else
    // server = new EditorServer(null, args[0], args[1]) ;

    //final EditorServer self = server ;
        
    Session editorSession = null;
    this.textChannel = null ;
    this.textChannelConsumer = null ;
        
    URLString  url         = null;
    String     sessionType = null;
    String     hostname    = null;
    int        hostport    = 0;
        
    if (EditorServer_Debug) 
      System.err.println("EditorServer: main.");
    
        
    hostname    = "localhost";
    hostport    = 4461 ;
    sessionType = "socket";
    url         = URLString.createSessionURL(hostname, hostport, sessionType, sessionName);
        
    if (EditorServer_Debug) 
      System.err.println("EditorServer: main: url: " + url.toString());
    
        
    try {
            
      /* Registry running?  Start it if it isn't. */
            
      if (RegistryFactory.registryExists(sessionType) == false) {
        RegistryFactory.startRegistry(sessionType);
      }
            
      /* Create a session, [re]bind it and create a channel. */
            
      this.client      = new NetworkedClient("Server");
      editorSession = SessionFactory.createSession(this.client, url, true);
            
      // setup text channel
      this.textChannel = editorSession.createChannel(this.client, "TextChannel", true, true, true);
      this.textChannelConsumer = new TextChannelConsumer(this) ;
      this.textChannel.addConsumer(this.client, this.textChannelConsumer) ;
            
      // setup client channel
      this.clientChannel = editorSession.createChannel(this.client, "ClientChannel", true, true, true) ;
      this.clientChannelConsumer = new ClientChannelConsumer(this) ;
      this.clientChannel.addConsumer(this.client, this.clientChannelConsumer) ;
      this.clientChannel.addChannelListener( new ChannelAdaptor() {
          public void channelConsumerAdded(ChannelEvent event) {
            self.clientAppeared() ;
          }
        }) ;

      System.err.println("Setup and bound Editor server.");
    } catch (JSDTException e) {
      System.err.println("EditorServer: main: shared data exception: " + e);
      if (EditorServer_Debug) {
        e.printStackTrace();
      }
    }
        
    this.pack() ;
    // on Linux, this seems to be the only way to set JFrame sizes
    // (i.e. resize it after packing (which seems to defeat the
    // purpose of packing)!)
    this.setSize(new Dimension(800, 600)) ;
    this.setVisible(true) ;


  }
    
    
  // Arg stuff (SL: now obsolete).................................................
  private static String getHost(String args[]) {
    String defHost = "localhost";  /* Default host name for connections. */
    int length = args.length;
        
    if (EditorServer_Debug) {
      System.err.println("EditorServer: getHost.");
    }
        
    for (int i = 0; i < length; i++) {
      if (args[i].equals("-server")) {
        if (++i < length) {
          return(args[i]);
        }
      }
    }
    return(defHost);
  }
    
    
  private static int getPort(String args[]) {
    int defPort = 4461;   /* Default port number for connections. */
    int length = args.length;
        
    if (EditorServer_Debug) {
      System.err.println("EditorServer: getPort.");
    }
        
    for (int i = 0; i < length; i++) {
      if (args[i].equals("-port")) {
        if (++i < length) {
          return(Integer.parseInt(args[i]));
        }
      }
    }
    return(defPort);
  }
    
    
  private static String getType(String args[]) {
    String defType = "socket";   /* Default Session type. */
    int length = args.length;
        
    if (EditorServer_Debug) {
      System.err.println("EditorServer: getType.");
    }
        
    for (int i = 0; i < length; i++) {
      if (args[i].equals("-type")) {
        if (++i < length) {
          return(args[i]);
        }
      }
    }
    return(defType);
  }
  
  //Inner classes...
  
 
    
  public class DocumentPanel extends JPanel {
    	
    public JTextPane textPane ;
    public JList parList ;
    public JLabel lblCursor ;
    	
    public int getOffsetFromPoint(Point p) {
      return textPane.viewToModel(p) ;
    }
    	
    public int getYValueFromOffset(int offset) {
      try {
        return textPane.modelToView(offset).y ;
      } catch(Exception e) {
        System.out.println("EditorServer: DocumentPanel: getYValueFromOffset. error") ;
        e.printStackTrace() ;
        return 0 ;
      }
    }
    	
    public DocumentPanel() {
      super(new BorderLayout()) ;
    		
      JLabel lblDocument = new JLabel("Document: " + document.getTitle()) ;
      lblDocument.setBorder(new EtchedBorder()) ;
    		
      textPane = new JTextPane(document) ;
      textPane.setEditable(false) ;
      textPane.setMargin(new Insets(5,20,5,5));
      textPane.setMaximumSize(new Dimension(364, 1000000000)) ;
      textPane.setPreferredSize(new Dimension(364,400)) ;
      textPane.setMinimumSize(new Dimension(364, 10)) ;
      textPane.addCaretListener(new CaretListener() {
          public void caretUpdate(CaretEvent e) {
            int length = document.getLength() ;
            int offset = e.getDot() ;
            		
            if(e.getDot() == e.getMark())
              textPane.getCaret().moveDot(offset+1) ;
            		
            Paragraph p = lockManager.getParFromOffset(offset) ;
            int pOffset = p.getOffset() ;
            		
            lblCursor.setText("Document Length=" + String.valueOf(length) +
                              ", CaretOffset=" + String.valueOf(offset) + 
                              ", Paragraph=" + p.toString() +
                              ", Offset in Paragraph=" + String.valueOf(offset - p.getOffset())) ;
          }
        }) ;
      Box box = new Box(BoxLayout.X_AXIS) ;
      box.add(textPane) ;
      box.add(Box.createGlue()) ;
      box.setBackground(Color.WHITE) ;
      box.setOpaque(true) ;
      box.setPreferredSize(new Dimension(600, 10000)) ;
    		
      lblCursor = new JLabel("Cursor") ;
      lblCursor.setBorder(new EtchedBorder()) ;
    		
      JPanel boxText = new JPanel(new BorderLayout()) ;
      boxText.setBorder(new EmptyBorder(5, 5, 5, 5)) ;
      boxText.add(lblDocument, BorderLayout.NORTH) ;
      boxText.add(new JScrollPane(box), BorderLayout.CENTER) ;
      boxText.add(lblCursor, BorderLayout.SOUTH) ;
    		
      JLabel lblPars = new JLabel("Paragraphs: ") ;
      lblPars.setBorder(new EtchedBorder()) ;
    		
      parList = new JList() ;
      parList.setPreferredSize(new Dimension(100, 300)) ;
      parList.setEnabled(false) ;
    		
      JPanel boxPars = new JPanel(new BorderLayout()) ;
      boxPars.setBorder(new EmptyBorder(5, 5, 5, 5)) ;
      boxPars.add(lblPars, BorderLayout.NORTH) ;
      boxPars.add(new JScrollPane(parList), BorderLayout.CENTER) ;
    		
      add(boxText, BorderLayout.CENTER) ;
      add(boxPars, BorderLayout.EAST) ;
    }
  }
    
  public class ClientsPanel extends JPanel {
    	
    public EditorClient currClient ;
    public JList lstClients ;
    public JTable tblActions ;
    public JLabel lblActions ;
    	
    public ClientsPanel() {
      super(new BorderLayout()) ;
      lstClients = new JList() ;
      lstClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION) ;
      lstClients.setCellRenderer( new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            EditorClient c = (EditorClient)value ;
            JLabel label = new JLabel(c.getName()) ;
					
            if(c.isPresent()) 
              label.setForeground(MyColors.getMainColor(c.getColorCode()));
            else
              label.setForeground(Color.GRAY) ;
					
            if (isSelected)
              label.setBackground(lstClients.getSelectionBackground()) ;
					
            return label ;
          }
        }) ;
    		
      lstClients.addMouseListener( new MouseAdapter()
        {
          public void mouseClicked( MouseEvent e ) 
          {
	          if (EditorServer_Debug) 
            	System.out.println( " mouseClicked " );
            int ndx = lstClients.locationToIndex( new Point( e.getX(), e.getY() ) );
            if( ndx > -1 && ndx < lstClients.getModel().getSize())
              lstClients.setSelectedIndex( ndx );
            else
              {
                if (EditorServer_Debug) 
              		System.out.println(" invalid mouse click on the JList");              
              }
          }
        }) ;
			
      lstClients.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            EditorClient c = (EditorClient)lstClients.getSelectedValue() ;
            currClient = c ;
            updateActionTableFor(currClient) ;
          }
        }) ;

    		
      tblActions = new JTable(getClearTableModel()) ;
    		
    		
      add(new JScrollPane(lstClients), BorderLayout.WEST) ;
      add(new JScrollPane(tblActions), BorderLayout.CENTER) ;
    }
    		
    public void updateClientList() {
      Iterator i = clients.iterator() ;
      DefaultListModel model = new DefaultListModel() ;
      
      EditorClient placebo = new EditorClient(self,0,"                                            ",0,0,"");                                     
      model.addElement(placebo) ;
      
      while (i.hasNext()) {
        EditorClient c = (EditorClient)i.next() ;
        model.addElement(c) ;
      }
    		
      lstClients.setModel(model) ;
      lstClients.setSelectedValue(currClient, true) ;
    		
    }
    	
    public DefaultTableModel getClearTableModel() {
      DefaultTableModel model = new DefaultTableModel() ;
      model.addColumn("Start") ;
      model.addColumn("End") ;
      model.addColumn("Type") ;
      return model ;
    }
    	
    public void updateActionTableFor(EditorClient client) {
    		
      if (client == null) {
        DefaultTableModel model = new DefaultTableModel() ;
        tblActions.setModel(getClearTableModel()) ;
        return ;
      } else if(currClient == client) {
        DefaultTableModel model = getClearTableModel() ;
    			
        Iterator i = client.getActions(self).iterator() ;
        while (i.hasNext()) {
          EditorAction ea = (EditorAction) i.next() ;
          model.addRow(ea.getTableRow()) ;
        }
        tblActions.setModel(model) ;
      }
    }	
  }
    
  public class SelectServerDialog extends JDialog{

    JTextField txtServer ;
    JButton cmdConnect, cmdCancel ;
    	
    public SelectServerDialog() {
      super() ;
      setTitle("Select Server") ;
    		
      JLabel lblServer = new JLabel("IP or Name:") ;
      txtServer = new JTextField() ;
      txtServer.setMaximumSize(new Dimension(10000, 20)) ;
      Box boxServer = new Box(BoxLayout.X_AXIS) ;
    		
      cmdConnect = new JButton("Connect") ; 
      cmdConnect.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
    				
          }
        }) ;
    		
    		
      cmdCancel = new JButton("Cancel") ;
      cmdCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose() ;
          }
        }) ;
    		
    }
  }
  
  public class RecordingHandler implements ActionListener 
  
  {
	  private EditorServer parent;
	  
	  public RecordingHandler (EditorServer es)
	  
	  {
      parent=es; 
		  
	  }
	  
    public void actionPerformed ( ActionEvent clickEvent )
	  	
    {			  	
      RecorderDialog recorderInfo = new RecorderDialog(parent);
		  	
      recorderInfo.show();
		  	
      JOptionPane.showMessageDialog ( null,"Clock Reset\nrecordingRTP session...");
		  	
      stopRTPrecording.setEnabled(true); 
		  	
      startRTPrecording.setEnabled(false); 	  		
		  	  	
    }  
	  
  }//endof RecordingHandler class
  

  
  
  public class RecorderDialog extends JDialog {
	    
    private JTextField addressPortTTL, outFile, durationField;
	  
	  public RecorderDialog(EditorServer parent)
    {
		  	
      super(parent,true);
		  	
      setTitle("Recorder Info");
		  			  	
      //fieldsPanel
		  	
      JPanel fieldsPanel = new JPanel();
      GridBagLayout gbLayout = new GridBagLayout ();
      GridBagConstraints constraints;
		  	
      //		  	Address/Port/TTL :
		  	
      constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.EAST;
      constraints.insets = new Insets(5,5,0,0);		  	
		  	
      fieldsPanel.setLayout(gbLayout);		  	
		  	
      JLabel AddPTTLabel = new JLabel ("Address/Port/TTL :");
      gbLayout.setConstraints(AddPTTLabel, constraints);
      fieldsPanel.add(AddPTTLabel,constraints);
		  	
      constraints = new GridBagConstraints();
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.insets = new Insets(5,5,0,5);
      constraints.weightx = 1.0D;
		  	
      addressPortTTL = new JTextField(25);
      addressPortTTL.setText("224.20.20.20/20002");
		  	
      gbLayout.setConstraints(addressPortTTL, constraints);
		  	
      fieldsPanel.add(addressPortTTL,constraints);
		  	
      //Outputfile...
		  	
      constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.EAST;
      constraints.insets = new Insets(5,5,0,0);		  	
		  	
      JLabel  OFileLabel = new JLabel ("Save file as... :");
      gbLayout.setConstraints(OFileLabel, constraints);
      fieldsPanel.add(OFileLabel,constraints);
		  	
      constraints = new GridBagConstraints();
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.insets = new Insets(5,5,0,5);
      constraints.weightx = 1.0D;
		  	
      outFile = new JTextField(25);
      outFile.setText("Placebo.rtp");
		  	
      gbLayout.setConstraints(outFile, constraints);
		  	
      fieldsPanel.add(outFile,constraints);
		  	
      //Recording time...
		  	
      constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.EAST;
      constraints.insets = new Insets(5,5,0,0);
		  	
      JLabel  timeLabel = new JLabel ("Recording time (in seconds) : ");
      gbLayout.setConstraints(timeLabel, constraints);
      fieldsPanel.add(timeLabel,constraints);
		  	
      constraints = new GridBagConstraints();
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.insets = new Insets(5,5,0,5);
      constraints.weightx = 1.0D;
		  	
      durationField = new JTextField(25);
      durationField.setText("2700");
		  	
      gbLayout.setConstraints(durationField, constraints);
		  	
      fieldsPanel.add(durationField,constraints);		  	
		  	
      //The buttons...
		  	
      JPanel buttonPanel = new JPanel();
		  	
      JButton startRecordingButton = new JButton ("Start Recording");
		  	
      startRecordingButton.addActionListener( new ActionListener() {
			  	
			  	public void actionPerformed( ActionEvent buttonPressed) {
				  	
				  	//startThread here !!!
				  	
				  	String apt = addressPortTTL.getText().trim();
            String ofile = outFile.getText().trim();
            String duration = durationField.getText().trim();
      				
            System.out.println("\napt : *"+ apt +"* ofile : *"+ ofile +"* duration: *" + 	duration +"*" );		  	
				  	
				  	long dur = new Integer(duration).intValue()*1000 ; //turned into ms
				  	
				  	recorder = new Recorder(apt, ofile, dur);
				  	
				  	recorder.start();
				  	
				  	document.resetStartTime( System.currentTimeMillis() );				  	
		  			  	
		  					  					
		  			//close dialog Box
		  			
		  			dispose();	  			  						  	
			  	}//
			  	
		  	});//endActionListener
		  	
      buttonPanel.add(startRecordingButton);
		  	
      Container dialogContainer = getContentPane();
		  	
      dialogContainer.setLayout( new BorderLayout() );
      dialogContainer.add(fieldsPanel,BorderLayout.CENTER) ;	
      dialogContainer.add(buttonPanel,BorderLayout.SOUTH) ;
		  	
      pack();
		  	
      setLocationRelativeTo (parent);
		  	
    }
	  
  
  }//endof RecorderInfo
  
  

}//endof EditorServer Class
