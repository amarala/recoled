package cowrat.recoled.server;

/*

  Recieves messages from text channel (i.e: inserts, removals, locks)

*/

import com.sun.media.jsdt.*;
import cowrat.recoled.shared.*;

public class TextChannelConsumer implements ChannelConsumer, EditorDebugFlags{
	
	EditorServer parent ;
	
	public TextChannelConsumer(EditorServer p) {
		parent = p ;
	}
	
	public synchronized void dataReceived(Data data) {
         
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
			
			else if (Type == Message.LOCK_REQUEST)
				{
				if ( TextChannel_Debug  )
    					System.out.println("\nclient.TextChannelConsumer : LOCK_GRANT msg Received") ;
				parent.lockRequested((Message.LockRequestMsg) msg) ;
				}
			
			else if (Type == Message.LOCK_RELEASE)
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
			System.out.println("EditorServer: TextChannelConsumer: error converting message") ;
			if (TextChannel_Debug) 
				e.printStackTrace() ;
		}

  }
	
}
