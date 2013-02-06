//EditorClient Modified last Modified April 05

package cowrat.recoled.client;


import java.net.InetAddress;
import java.net.UnknownHostException;

import java.awt.* ;

import com.sun.media.jsdt.*;
import java.awt.event.* ;

import javax.swing.* ;
import javax.swing.border.* ;

import java.util.* ;
import cowrat.recoled.shared.* ;

public class EditorClient implements EditorDebugFlags {
    
    private boolean connected ;

    private boolean ShowMessages = false ;

    private EditorDocument document ;
    private Paragraphs paragraphs ;
    private Clients clients ;
    private LockManager lockManager ;
    private Interface gui ;
    private Gestures gestures ;
    private Highlights highlights ;
    private Login login ;
    
    private NetworkedClient client ;
    private Session session ;
    private Channel textChannel ;
    private Channel clientChannel ;
    private TextChannelConsumer textChannelConsumer ;
    private ClientChannelConsumer clientChannelConsumer ;
    private MessageBuffer textBuffer ;
    private MessageBuffer clientBuffer ;
    
    private int keyValue ;
    private String sessionType ;
    private String hostname ;
    private int hostport ;
    private String name ;
    private int myID ;

   private static boolean adjustParagraphInsertion = false ;
   // this variable adjust the position of "\n" in the "special case"
   // paragraph insertion

	//Client IP address...

    private String clientIPaddress;
    
    public EditorClient() {
        clients = new Clients(this) ;
        lockManager = new LockManager(this) ;
        paragraphs = new Paragraphs(this) ;
        document = new EditorDocument(this, lockManager) ;
        gestures = new Gestures(lockManager) ;
        highlights = new Highlights(this, lockManager, document);
        
        paragraphs.setDocument(document) ;
        lockManager.setDocument(document) ;
        lockManager.setParagraphs(paragraphs) ;
        lockManager.setClients(clients) ;

        try{

		clientIPaddress = (InetAddress.getLocalHost()).toString();

		int offset = clientIPaddress.indexOf('/');

		clientIPaddress = clientIPaddress.substring(offset+1);


	}

	catch ( UnknownHostException uhe)
		{System.out.println("Unable to Retrieve localhost IPaddress in->Client.EditorClient !!!");} 

/*matt...
On Linux installations InetAddress.getLocalHost() may return an InetAddress corresponding to the loopback address (127.0.0.1). This arises because the default installation creates an association in /etc/hosts between the hostname of the machine and the loopback address. To ensure that InetAddress.getLocalHost() returns the actual host address, update the /etc/hosts file or the name service configuration file (/etc/nsswitch.conf) to query dns or nis before searching hosts.
*/ 

	if( EditorClient_Debug )
		System.out.println(">>>IP address of local host is : *"+ clientIPaddress +"*" ); 
        
        gui = new Interface(this, document, clients, paragraphs, gestures, lockManager, highlights) ;
        //gui.setSize(new Dimension(390, 560)) ;
        gui.setSize(new Dimension(512, 740)) ;
      
        
    }
    
    public Interface getGUI() {
        return gui ;
    }
    
    public void validate() {
        gui.validate() ;
    }
    
    public Paragraph getParFromOffset(int offset) {
        return lockManager.getParFromOffset(offset) ;
    }
    
    // Client stuff - Session management...........
    
    public void clientJoined(Message.ClientJoinMsg msg) {
        System.out.println("EditorClient -> clientJoined: " + msg.getName() + " " + String.valueOf(msg.getClientId())) ;
        
        //this sets up THIS client
        if (msg.getKeyValue() == keyValue){
            if (login != null)
              	{	            
	            myID = msg.getClientId();
	            
                clients.setMyself(myID, msg.getName(), msg.getColorCode()) ;
                                
                gui.setTitle("SharedEditor: " + msg.getName()) ;
                highlights.setMyClientId(myID) ;
                login.dispose() ;
                login = null ;
            }
        }
        //registers a new (remote) client...
        else {
	                
            clients.addClient(msg.getClientId(), msg.getName(), msg.getColorCode()) ;
            
            if (login != null)
                login.addClient(msg.getName(), msg.getColorCode()) ;
            else
                gui.getMainView().sendScrollUpdate() ;
        }
    }
    
    private void sendJoinMsg(String name, int colorCode, int keyValue) {
        try {
	
	//matt : clientIPaddress added here in 	ClientJoinMsg
            Message.ClientJoinMsg msg = new Message.ClientJoinMsg(-1, name, colorCode, keyValue,clientIPaddress) ;
            clientBuffer.addMessage(msg) ;
            //clientChannel.sendToClient(client, "Server", new Data(msg)) ;
        } catch(Exception e) {
            System.out.println("EditorClient -> sendJoinMsg: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void clientRejected(Message.ClientRejectMsg msg) {
        System.out.println("EditorClient -> clientRejected.") ;
        if (msg.getKeyValue() == keyValue){
            if (msg.getReason() == Message.ClientRejectMsg.REASON_NAME)
                login.showUsedNameMessage() ;
            else
                login.showUsedColorMessage() ;
        }
    }
    
    public void leaveSession() {
        lockManager.releaseEverything() ;
        try {
            Message.ClientLeaveMsg msg = new Message.ClientLeaveMsg(myID) ;
            clientBuffer.addMessage(msg) ;
        } catch(Exception e) {
            System.out.println("EditorClient -> leaveSession: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
        
        gui.dispose() ;
    }
    
    public void clientLeft(Message.ClientLeaveMsg msg) {
        System.out.println("Client Left: ") ;
        if(msg.getClientId() != myID)
            clients.removeClient(msg.getClientId()) ;
    }
    
    public void documentStateRecieved(Message.DocumentStateMsg msg) {
        
	     System.out.println("\nEditorClient->documentStateReceived...") ;
	    
        if (msg.getClientId() == myID) 
            lockManager.setDocumentState(msg.getXml()) ;
        
        gui.setVisible(true) ;
        gui.getMainView().sendScrollUpdate() ;
    }
    
    // Highlight stuff.....................................................................................................
    
    public void highlightAdded(Message.HighlightAddMsg msg) {
        if (msg.getClientId() != myID) {
	if ( ShowMessages )
            System.out.println("EditorClient: highlightAdded.") ;
            highlights.addHighlight(msg.getId(), msg.getHighlightType(), msg.getStartPar(), msg.getStartOffset(), msg.getEndPar(), msg.getEndOffset()) ;
            clients.getClient(msg.getClientId()).setStateHighlighting() ;
        }
    }
    
    public void highlightDeleted(Message.HighlightDeleteMsg msg) {
        if (msg.getClientId() != myID) {
	if ( ShowMessages )
            System.out.println("EditorClient: highlightDeleted.") ;
            highlights.deleteHighlight(msg.getId()) ;
            clients.getClient(msg.getClientId()).setStateHighlighting() ;
        }
    }
    
    public void highlightEdited(Message.HighlightEditMsg msg) {
        if (msg.getClientId() != myID) {
	if ( ShowMessages )
            System.out.println("EditorClient: highlightDeleted.") ;
            highlights.editHighlight(msg.getId(), msg.getHighlightType(), msg.getStartPar(), msg.getStartOffset(), msg.getEndPar(), msg.getEndOffset()) ;
            clients.getClient(msg.getClientId()).setStateHighlighting() ;
        }
    }
    
    public void highlightTypeAdded(Message.HighlightTypeMsg msg) {
        if (msg.getClientId() != myID) {
	if ( ShowMessages )
            System.out.println("EditorClient: highlightTypeAdded.") ;
            highlights.addHighlightType(msg.getId(), msg.getName(), msg.getColor()) ;
            clients.getClient(msg.getClientId()).setStateHighlighting() ;
        }
        
    }
    
    public void sendHighlightAdded(int id, int type, long sp, int so, long ep, int eo) {
	if ( ShowMessages )
        System.out.println("EditorClient: sendHighlightAdded") ;
        try {
            Message.HighlightAddMsg msg = new Message.HighlightAddMsg(myID, id, type, sp, ep, so, eo) ;
            textBuffer.addMessage(msg) ;
            clients.getMyself().setStateHighlighting() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendHighlightAdded: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void sendHighlightDeleted(int id) {
	if ( ShowMessages )
        System.out.println("EditorClient: sendHighlightDeleted") ;
        try {
            Message.HighlightDeleteMsg msg = new Message.HighlightDeleteMsg(myID, id) ;
            textBuffer.addMessage(msg) ;
            clients.getMyself().setStateHighlighting() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendHighlightDeleted: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void sendHighlightEdited(int id, int type, long sp, int so, long ep, int eo) {
	if ( ShowMessages )
        System.out.println("EditorClient: sendHighlightEdited") ;
        try {
            Message.HighlightEditMsg msg = new Message.HighlightEditMsg(myID, id, type, sp, ep, so, eo) ;
            textBuffer.addMessage(msg) ;
            clients.getMyself().setStateHighlighting() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendHighlightEdited: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void sendHighlightTypeAdded(int id, String name, Color color) {
	if ( ShowMessages )
        System.out.println("EditorClient: sendHighlightTypeAdded") ;
        try {
            Message.HighlightTypeMsg msg = new Message.HighlightTypeMsg(myID, id, name, color) ;
            textBuffer.addMessage(msg) ;
            clients.getMyself().setStateHighlighting() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendHighlightTypeAdded: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
     
    // Gesture stuff........................................................................................................
    
    public void gestureCircle(Message.GestureCircleMsg msg) {
        if (msg.getClientId() != myID) 
        
        {
           if( EditorClient_Debug ) 
	        	System.out.println("EditorClient: gestureCircle.") ;
            gui.getMainView().addGestureCircle(msg.getClientId(), msg.getPar(), msg.getId(), msg.getX(), msg.getY()) ;
            //gestures.addCircle(c, msg.getPar(), msg.getX(), msg.getY()) ;
            clients.getClient(msg.getClientId()).setStatePointing() ;
        }
    }
    
    public void gestureLine(Message.GestureLineMsg msg) {
        if (msg.getClientId() != myID) 
        {
	        
	       if( EditorClient_Debug ) 
            	System.out.println("EditorClient: gestureLine.") ;
            gui.getMainView().addGestureLine(msg.getClientId(), msg.getPar(), msg.getId(), msg.getAX(), msg.getAY(), msg.getBX(), msg.getBY()) ;
            //gestures.addLine(c, msg.getPar(), msg.getAX(), msg.getAY(), msg.getBX(), msg.getBY()) ;
            clients.getClient(msg.getClientId()).setStatePointing() ;
        }
    }	
    
    public void sendGestureCircle(int id, long par, int x, int y) {
        try {
            Message.GestureCircleMsg msg = new Message.GestureCircleMsg(myID, id, par, x, y) ;
            clientBuffer.addMessage(msg) ;
            clients.getMyself().setStatePointing() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendGestureCircle: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void sendGestureLine(int id, long par, int aX, int aY, int bX, int bY) {
        try {
            Message.GestureLineMsg msg = new Message.GestureLineMsg(myID, id, par, aX, aY, bX, bY) ;
            clientBuffer.addMessage(msg) ;
            clients.getMyself().setStatePointing() ;
        } catch(Exception e) {
            System.out.println("EditorClient: sendGestureLine: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    
    
    
    // Lock stuff...................................................................................................................
    public void requestLock(long sPar, long ePar, int lockID) {
        if (ToServer_Debug) System.out.println("EditorClient -> requestLock.") ;
        
        try {
            Message.LockRequestMsg msg = new Message.LockRequestMsg(myID, sPar, ePar, lockID) ;
            textBuffer.addMessage(msg) ;
            //textChannel.sendToClient(client, "Server", new Data(msg)) ;
        } catch(Exception e) {
            System.out.println("EditorClient -> requestLock: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void releaseParagraphs(long sPar, long ePar) {
        if (ToServer_Debug) System.out.println("EditorClient -> releaseParagraphs.") ;
        
        try {
            Message.LockReleaseMsg msg = new Message.LockReleaseMsg(myID, sPar, ePar) ;
            textBuffer.addMessage(msg) ;
            //textChannel.sendToClient(client, "Server", new Data(msg)) ;
        } catch(Exception e) {
            System.out.println("EditorClient -> releaseParagraphs: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    public void lockGranted(Message.LockGrantMsg msg) {
	
        if (EditorClient_Debug) System.out.println("EditorClient -> lockGranted." + String.valueOf(msg.getClientId()) + ", " + String.valueOf(myID)) ;
        if (msg.getClientId() == myID)
            lockManager.grantLock(msg.getIdNumber()) ; 
        else
            lockManager.lockMade(msg.getClientId(), msg.getStartPar(), msg.getEndPar()) ;
    }
    
    public void lockDenied(Message.LockDenyMsg msg) {
        if (EditorClient_Debug) System.out.println("EditorClient -> lockDenied.") ;
        if(msg.getClientId() == myID)
            lockManager.denyLock(msg.getIdNumber()) ;
    }
    
    public void lockReleased(Message.LockReleaseMsg msg) {
       /* if (EditorClient_Debug) System.out.println("EditorClient -> lockReleased.") ;
       */
        if(msg.getClientId() != myID)
            lockManager.lockReleased(msg.getStartPar(), msg.getEndPar()) ;
    }
    
    // text stuff........................................................................................................................


//This function used for remote insert of a single character... 
   
    public void textInserted(Message.TextInsertMsg msg) 
{

	int SenderId = msg.getClientId() ;
	String textInserted = msg.getText();

        if ( SenderId != myID ) 
	{
	if (EditorClient_Debug) 
		System.out.println("\nEditorClient -> textInserted : *" +textInserted+"*"+" by Remote User : " + String.valueOf( SenderId )+"\n");
           	gui.getMainView().ignoreScroll = true ; 
            	lockManager.insertText(msg.getPar(), msg.getOffset(), textInserted, SenderId) ;
            	clients.getClient(SenderId).setStateTyping() ;
        	}
 }
//This function used for remote Pasting... 
   
    public void textInserted(Message.TextPasteMsg msg) 
{
	int SenderId = msg.getClientId() ;
	String textPasted = msg.getText();

        if (  SenderId != myID ) 
	{
	if (EditorClient_Debug) 
		System.out.println("\nEditorClient -> textInserted : *" + textPasted +"*"+" by Remote User : " + String.valueOf( SenderId )+"\n");
           	gui.getMainView().ignoreScroll = true ; 
            	lockManager.insertText(msg.getPar(), msg.getOffset(), textPasted , SenderId ) ;
            	clients.getClient(SenderId).setStateTyping() ;
        	}
 }
 
  public void loadText(String textLoaded) 
{
	
	if (EditorClient_Debug) 
		System.out.println("\nEditorClient ->textLoaded : *" + textLoaded +"*\n");
           	//gui.getMainView().ignoreScroll = true ;
           	
        lockManager.insertText(2,0, textLoaded , myID) ; 
        
            try{
	            Message.TextPasteMsg msg = new Message.TextPasteMsg(myID,2,0,textLoaded) ;
           		textBuffer.addMessage(msg) ;
        		clients.getMyself().setStateTyping() ;
    		}
    		
    		catch(Exception e) {
           	System.out.println("EditorClient-> loadText: error sending message") ;
            	if (ToServer_Debug) 
                	e.printStackTrace() ;}
 }//endof loadText() 

//This function used for local insert...
// It is at this stage that distinction between Insertion & Paste is made...
    
    public void textInserted(String text, long par, int offset)
 {       
        try {
	
	if (text.length()==1)
		{
				
		if (ToServer_Debug) 
		System.out.println("\nEditorClient -- - -->text Inserted by local user : *"+text+"* \n") ;
		
		Message.TextInsertMsg msg = new Message.TextInsertMsg(myID, par, offset, text) ;
		
           	 	textBuffer.addMessage(msg) ;
		clients.getMyself().setStateTyping() ;

		}

		else if (text.length()>1)

		{
		if (ToServer_Debug) 
		System.out.println("\nEditorClient -- - -->text PASTED by local user : *"+text+"* \n") ;

		
		Message.TextPasteMsg msg = new Message.TextPasteMsg(myID, par, offset, text) ;
           	 	textBuffer.addMessage(msg) ;
            		clients.getMyself().setStateTyping() ;

		}
            	
        } 

        catch(Exception e) {
           	System.out.println("EditorClient  - --> textInserted: error sending message") ;
            	if (ToServer_Debug) 
                	e.printStackTrace() ;}
    }

//This function is used for (from) remote deletion of a single character   

    public void textDeleted(Message.TextDeleteMsg msg) {

	int SenderId = msg.getClientId() ;

        if (EditorClient_Debug) System.out.println("EditorClient -> textDeleted by " + String.valueOf(msg.getClientId())) ;
        if ( SenderId != myID ) 
        {
            gui.getMainView().ignoreScroll = true ; 
            lockManager.deleteText(msg.getStartPar(), msg.getEndPar(), msg.getStartOffset(), msg.getEndOffset(), SenderId) ;
            clients.getClient(SenderId).setStateTyping() ;
        }
    }

//This function is used for (from) remote cuting   

    public void textDeleted(Message.TextCutMsg msg) {

	int SenderId = msg.getClientId() ;

        if (EditorClient_Debug) System.out.println("EditorClient -> textCut by " + String.valueOf(msg.getClientId())) ;
        if ( SenderId != myID ) 
       {
            gui.getMainView().ignoreScroll = true ; 
            lockManager.deleteText(msg.getStartPar(), msg.getEndPar(), msg.getStartOffset(), msg.getEndOffset(), SenderId) ;
            clients.getClient(SenderId).setStateTyping() ;
        }
    }
    
//This function used for local delete...
// It is at this stage that distinction between Delete & Cut is made...

    public void textDeleted(long sPar, long ePar, int sOffset, int eOffset) 

{
	int NumberOfCharactersDeleted = eOffset - sOffset;
	if (ToServer_Debug) 
		System.out.println("\nEditorClient->textDELETED : NumberOfCharactersDeleted  = " + NumberOfCharactersDeleted ) ;
       
        try {

	if (NumberOfCharactersDeleted==1)
		{
	 	if (ToServer_Debug) 
			System.out.println("\nEditorClient->textDELETED : sOffset = " + sOffset+ " - eOffset  = " + eOffset + " from par : " + String.valueOf(sPar) + " to " + String.valueOf(ePar) ) ;
	
            		Message.TextDeleteMsg msg = new Message.TextDeleteMsg(myID, sPar, ePar, sOffset, eOffset) ;
            		textBuffer.addMessage(msg) ;
            		clients.getMyself().setStateTyping() ;
		}
//will tailor this...
	else if (NumberOfCharactersDeleted>1)

		{
		if (ToServer_Debug) 
			System.out.println("\nEditorClient - - - - ->textCUT : sOffset = " + sOffset+ " - eOffset  = " + eOffset + " from par : " + String.valueOf(sPar) + "to " + String.valueOf(ePar)+"\n" ) ;

		Message.TextCutMsg msg = new Message.TextCutMsg(myID, sPar, ePar, sOffset, eOffset) ;
            		textBuffer.addMessage(msg) ;
            		clients.getMyself().setStateTyping() ;
		}

	//this is when one or more paragraphs disappear as a result of deletion
	// will need to be dealt with, check LockManager-> remove for origin
	//of this function call...

	else	{
		if (ToServer_Debug) 
			System.out.println("\nEditorClient->textDELETED SPECIAL CASE: sOffset = " + sOffset+ " - eOffset  = " + eOffset + " from par : " + String.valueOf(sPar) + " to " + String.valueOf(ePar) ) ;
	
            		Message.TextDeleteMsg msg = new Message.TextDeleteMsg(myID, sPar, ePar, sOffset, eOffset) ;
            		textBuffer.addMessage(msg) ;
            		clients.getMyself().setStateTyping() ;
		}
        } 

        catch(Exception e) {
            System.out.println("EditorClient -> textCut: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;}
    }
    
    // scroll stuff........................................................................................................................
    
    public void scrollMoved(Message.ScrollMoveMsg msg) {
        if (msg.getClientId() != myID)
            clients.scrollMoved(msg.getClientId(), msg.getValue(), msg.getView(), msg.getMax(), msg.getIsPrimary()) ;
    }
    
    public void scrollMoved(int value, int view, int max, boolean isPrimary) {
        //if (ToServer_Debug) System.out.println("EditorClient -> scrollMoved.") ;
        if (clientChannel == null) return ;
        try {
            Message.ScrollMoveMsg msg = new Message.ScrollMoveMsg(myID, value, view, max, isPrimary) ;
            clientBuffer.addMessage(msg) ;
            //clientChannel.sendToClient(client, "Server", new Data(msg)) ;
        } catch(Exception e) {
            System.out.println("EditorClient -> scrollMoved: error sending message") ;
            if (ToServer_Debug) 
                e.printStackTrace() ;
        }
    }
    
    // main.................................................................................................................................
    
    public static void main(String args[]) {
        
        final EditorClient self = new EditorClient() ;
        if (EditorClient_Debug) {
            System.err.println("EditorClient: main.");
        }
        
        self.hostport    = 4461;
        self.sessionType = "socket";
        
        
        SelectServerDialog dialog = self.new SelectServerDialog(getHost(args)) ;
        dialog.show() ;
        //self.connect() ;
    }
    
    
    
    private void doLogin() {
        login = new Login() ;
        login.cmdLogin.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                //System.out.println(login.getName() + String.valueOf(login.getName().length())) ;
                if (login.getName().length() == 0) 
                    JOptionPane.showMessageDialog(login, "You must enter a name", 
                            "No Name Entered", JOptionPane.ERROR_MESSAGE) ;
                else
                    sendJoinMsg(login.getName(), login.getColor(), keyValue) ;	
            }
        }) ;
        
        login.setSize(450, 300) ;
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
        login.show() ;
    }
    
    private void connect() {
        Random r = new Random() ;
        keyValue = r.nextInt() ;
        
        String sessionName = "EditorSession" ;
        boolean sessionExists = false ;
        URLString url = null ;
        
        if(EditorClient_Debug) {
            System.err.println("EditorClient: connect.") ;
        }
        
        if (connected) {
            return ;
        }
        
        try {
            try {
                url = URLString.createSessionURL(hostname, hostport, sessionType, sessionName) ;
                System.out.println("EditorClient: connect: checking url: " + url) ;
                int tryCount = 0 ;
                
                while (!sessionExists & tryCount < 3) {
                	tryCount ++ ;
                    try {
                        if (SessionFactory.sessionExists(url)) {
                            System.out.println("EditorClient: connect: found session.") ;
                            sessionExists = true ;
                        }
                    } catch (NoRegistryException nre) {
                        System.out.println("EditorClient: connect: no registy: sleeping.") ;
                        Thread.sleep(1000) ;
                    } catch (ConnectionException ce) {
                        System.out.println("EditorClient: connect: connection exception: sleeping.") ;
                        Thread.sleep(1000) ;
                    }
                }
                
                /* Create editor client */
                
                System.err.println("Creating a EditorClient") ;
                client = new NetworkedClient(String.valueOf(keyValue)) ;
                
                /* Resolve the chat session */
                
                session = SessionFactory.createSession(client, url, true) ;
                
                // setup textchannel stuff
                textChannel = session.createChannel(client, "TextChannel", true, true, true) ;
                textChannelConsumer = new TextChannelConsumer(this) ;
                textChannel.addConsumer(client, textChannelConsumer) ;
                textBuffer = new MessageBuffer(textChannel, client, 50) ;
                textBuffer.start() ;
                
                // setup clientchannel stuff
                clientChannel = session.createChannel(client, "ClientChannel", true, true, true) ;
                clientChannelConsumer = new ClientChannelConsumer(this) ;
                clientChannel.addConsumer(client, clientChannelConsumer) ;
                clientBuffer = new MessageBuffer(clientChannel, client, 10) ;
                clientBuffer.start() ;
                
                connected = true ;
                System.err.println("Finished Connecting") ;
                
                
                doLogin() ;
                
                
                //gui.setVisible(true) ;
                //textChannel.sendToAll(client, new Data(new Message.TextInsertMsg(1,1,0, "Blah Blah"))) ;
            } catch (Exception e) {
                System.err.print("Caught exception in EditorClient.connect:" + e) ;
                if (EditorClient_Debug) {
                    e.printStackTrace() ;
                }
            }
        } catch (Throwable th) {
            System.err.println("EditorClient: connect caught: " + th) ;
            if (EditorClient_Debug) {
                th.printStackTrace() ;
            }
            throw new Error("EditorClient.connect failed : " + th) ;
            
        }
    }
    
    
    private static String getHost(String args[]) {
        String defHost = "localhost";  /* Default host name for connections. */
        int length = args.length;
        
        if (EditorClient_Debug) {
            System.err.println("ChatServer: getHost.");
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
        
        if (EditorClient_Debug) {
            System.err.println("ChatServer: getPort.");
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
        
        if (EditorClient_Debug) {
            System.err.println("ChatServer: getType.");
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
    
    public class SelectServerDialog extends JDialog{

    	JTextField txtServer ;
    	JButton cmdConnect, cmdCancel ;
    	
    	public SelectServerDialog(String server) {
    		super() ;
    		setTitle("Select Server") ;
    		
    		JLabel lblServer = new JLabel("IP or Name: ") ;
    		txtServer = new JTextField(server) ;
    		txtServer.setMaximumSize(new Dimension(10000, 20)) ;
    		Box boxServer = new Box(BoxLayout.X_AXIS) ;
    		boxServer.add(lblServer) ;
    		boxServer.add(txtServer) ;
    		
    		cmdConnect = new JButton("Connect") ; 
    		cmdConnect.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				hostname = txtServer.getText();
    				connect() ;
    				if (connected)
    					dispose() ;
    			}
    		}) ;
    		
    		
    		cmdCancel = new JButton("Cancel") ;
    		cmdCancel.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				System.exit(0) ;
    			}
    		}) ;
    		
    		Box boxButtons = new Box(BoxLayout.X_AXIS) ;
    		boxButtons.add(Box.createGlue()) ;
    		boxButtons.add(cmdConnect) ;
    		boxButtons.add(Box.createHorizontalStrut(10)) ;
    		boxButtons.add(cmdCancel) ;
    		boxButtons.add(Box.createGlue()) ;
    		
    		Box boxAll = new Box(BoxLayout.Y_AXIS) ;
    		boxAll.setBorder(new EmptyBorder(10, 10, 10, 10)) ;
    		boxAll.add(boxServer) ;
    		boxAll.add(Box.createVerticalStrut(10)) ;
    		boxAll.add(boxButtons) ;
    		
    		getContentPane().add(boxAll, BorderLayout.CENTER) ;
    		setSize(300, 200) ;
    		
    		addWindowListener(new WindowAdapter() {
    			public void windowClosed(WindowEvent e) {
    				if(!connected)
    					System.exit(0) ;
    			}
    		}) ;
    	}
    }

}
