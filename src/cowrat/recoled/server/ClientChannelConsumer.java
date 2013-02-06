package cowrat.recoled.server;

import com.sun.media.jsdt.*;
import cowrat.recoled.shared.*;

public class ClientChannelConsumer 
  implements ChannelConsumer, EditorDebugFlags 
{
	
    EditorServer parent ;
	
    public ClientChannelConsumer(EditorServer p) {
	parent = p ;
    }
	
    public synchronized void dataReceived(Data data) {
		
	try {
	    Message msg = (Message) data.getDataAsObject() ;
			
	    if (msg.getType() == Message.SCROLL_MOVE)
		parent.scrollMoved((Message.ScrollMoveMsg)msg) ;
			
	    if (msg.getType() == Message.CLIENT_JOIN)
		parent.clientJoined((Message.ClientJoinMsg)msg) ;

	    if (msg.getType() == Message.CLIENT_LEAVE)
		parent.clientLeft((Message.ClientLeaveMsg)msg) ;
			
	    if (msg.getType() == Message.GESTURE_CIRCLE) 
		parent.gestureCircle((Message.GestureCircleMsg)msg) ;
			
	    if (msg.getType() == Message.GESTURE_LINE) 
		parent.gestureLine((Message.GestureLineMsg)msg) ;
			
	} catch (Exception e) {
	    System.out.println("EditorServer: clientChannelConsumer: error recieving message") ;
	    if (EditorServer_Debug)
		e.printStackTrace() ;
	}
    }
	
}
