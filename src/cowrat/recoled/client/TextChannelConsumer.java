package cowrat.recoled.client;

import com.sun.media.jsdt.*;
import cowrat.recoled.shared.*;

public class TextChannelConsumer implements ChannelConsumer, EditorDebugFlags {
	
	EditorClient parent ;
	
	public TextChannelConsumer(EditorClient p) {
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

			int Type = msg.getType();
			
			if ( Type == Message.TEXT_INSERT)
				 {
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : TEXT_INSERT msg Received") ;
				parent.textInserted((Message.TextInsertMsg) msg) ;
				}

			else if ( Type == Message.TEXT_PASTE)		
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : TEXT_PASTE msg Received") ;
				parent.textInserted((Message.TextPasteMsg) msg) ;
				}
			
			else if (Type == Message.TEXT_DELETE)
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : TEXT_DELETE msg Received") ;
				parent.textDeleted((Message.TextDeleteMsg) msg) ;
				}

			else if ( Type == Message.TEXT_CUT)		
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : TEXT_CUT msg Received") ;
				parent.textDeleted((Message.TextCutMsg) msg) ;
				}
			
			else if (Type == Message.LOCK_GRANT)
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : LOCK_GRANT msg Received") ;
				parent.lockGranted((Message.LockGrantMsg) msg) ;
				}
			
			else if (Type == Message.LOCK_DENY)
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : LOCK_DENY msg Received") ;
				parent.lockDenied((Message.LockDenyMsg) msg) ;
				}
			
			if (Type == Message.LOCK_RELEASE)
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : LOCK_RELEASE msg Received") ;
				parent.lockReleased((Message.LockReleaseMsg) msg) ;
				}
			
			if (msg.getType() == Message.HIGHLIGHT_ADD)
				parent.highlightAdded((Message.HighlightAddMsg) msg) ;
		
			if (msg.getType() == Message.HIGHLIGHT_DELETE)
				parent.highlightDeleted((Message.HighlightDeleteMsg) msg) ;
			
			if (msg.getType() == Message.HIGHLIGHT_EDIT)
				parent.highlightEdited((Message.HighlightEditMsg) msg) ;
			
			if (msg.getType() == Message.HIGHLIGHT_TYPE)
				parent.highlightTypeAdded((Message.HighlightTypeMsg) msg) ;
			
		} catch (Exception e) {
			System.out.println("EditorClient-> textChannelConsumer: error converting message") ;
			if (EditorClient_Debug)
				e.printStackTrace() ;
		}
        
    
		// call appropriate method in parent
  }
	
}
