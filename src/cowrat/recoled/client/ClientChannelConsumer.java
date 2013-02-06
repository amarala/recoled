package cowrat.recoled.client;

import com.sun.media.jsdt.*;
import cowrat.recoled.shared.* ;

public class ClientChannelConsumer implements ChannelConsumer, EditorDebugFlags {
	
    EditorClient parent ;
	
    public ClientChannelConsumer(EditorClient p) {
	parent = p ;
    }
	
    public synchronized void dataReceived(Data data) {
	//int     position   = 0;
	//int     priority   = data.getPriority();
	//String  senderName = data.getSenderName();
	//Channel channel    = data.getChannel();
	//String  theData    = data.getDataAsString();
	try {
	    Message msg = (Message) data.getDataAsObject() ;
			
	    if (msg.getType() == Message.CLIENT_JOIN)
		parent.clientJoined((Message.ClientJoinMsg)msg) ;

	    if (msg.getType() == Message.CLIENT_REJECT)
		parent.clientRejected((Message.ClientRejectMsg)msg) ;

	    if (msg.getType() == Message.CLIENT_LEAVE)
		parent.clientLeft((Message.ClientLeaveMsg)msg) ;
			
	    if (msg.getType() == Message.SCROLL_MOVE)
		parent.scrollMoved((Message.ScrollMoveMsg)msg) ;
			
	    if (msg.getType() == Message.GESTURE_CIRCLE) 
		parent.gestureCircle((Message.GestureCircleMsg)msg) ;
			
	    if (msg.getType() == Message.GESTURE_LINE) 
		parent.gestureLine((Message.GestureLineMsg)msg) ;

	    if (msg.getType() == Message.DOCUMENT_STATE)
		parent.documentStateRecieved((Message.DocumentStateMsg)msg) ;
		
	} catch (Exception e) {
	    System.out.println("EditorClient: clientChannelConsumer: error recieving message") ;
	    if (EditorClient_Debug)
		e.printStackTrace() ;
	}
    }
	
}
