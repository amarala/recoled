//Modified Version Feb04

package cowrat.recoled.server;

import cowrat.recoled.shared.*;
import cowrat.recoled.server.EditorAction.* ; 
import java.io.* ;
import java.util.* ;
import javax.swing.* ;
import java.awt.* ;

public class EditorClient implements Serializable, EditorDebugFlags {
	
  private EditorServer parent;

  private int idNumber ;
  private String name ;
  private int colorCode ;
  private int keyValue ;
  private boolean present ;
  private String IPaddress; //added...
  //private Paragraphs paragraphs;
    
  private Vector actions ;
  private TextInsertAction lastTextInsertAction ;
  private TextDeleteAction lastTextDeleteAction ;  
  private GestureAction lastGestureAction ; 
  private TextPasteAction lastTextPasteAction ;   
  private TextCutAction lastTextCutAction ;   
    
  public int getIdNumber() { return idNumber ; }
  public String getName() { return name ; }
  public int getColorCode() { return colorCode ; }
  public boolean isPresent() { return present ; }
  public void setPresent(boolean b) { present = b ; }
    
  public Vector getActions(EditorServer es) {
    Vector v = (Vector)actions.clone() ;
        
    if(lastTextInsertAction != null)
      v.add(lastTextInsertAction) ;

    if ( lastTextPasteAction != null )
      v.add(lastTextPasteAction) ;

    if (lastTextDeleteAction != null)
      v.add(lastTextDeleteAction) ;

    if (lastTextCutAction != null)
      v.add(lastTextCutAction) ;

    if(lastGestureAction != null) {
      lastGestureAction.setText(getTextForLastGesture(es)) ;
      v.add(lastGestureAction) ;
    }
        
    return v ;
  }
    
  public String getXML() {
    String s = "" ;
    s = s + "      <participant id=\"" + idNumber + "\">\n" ;
    s = s + "        " + SavedDocument.replaceReservedCharacters(name)  + "\n" ;
    s = s + "      </participant>\n" ;
        
    return s ; 
  }
  
  //mach : added server parent in order to query unique key identifier
  // when timestamp is generated (for timestamp manipulation) 
    
  public EditorClient(EditorServer server,int id, String n, int cc, int kv, String ClientIPaddress) {
   
	parent = server;
	idNumber = id ;
    name = n ;
    colorCode = cc ;
    keyValue = kv ;
    IPaddress = ClientIPaddress;

    present = true ;
    actions = new Vector() ;
  }
    
  public Message.ClientJoinMsg getMessage() {
    return new Message.ClientJoinMsg(idNumber, name, colorCode, keyValue, IPaddress) ;
  }
  
  //mach added Paste & Cut ...
  //mach : added : call to timestampId = parent.getTimestampId() to get unique id for timestamp
  // prior timestamp generation
  // also modified all related constructors in EditorAction
    
  public void addTextInsertAction(long time, Vector pars, int sOffset, String text) {
    	
    if(lastTextInsertAction != null) {
      if(lastTextInsertAction.appendAction(time, pars, sOffset, text))
        return ;
      else
        actions.add(lastTextInsertAction) ;
    }
    	
    int timestampId = parent.getTimestampId();
    lastTextInsertAction = new TextInsertAction(timestampId,idNumber, time, pars, sOffset, text) ;
    
    if ( EditorClient_Debug )
    	System.out.print("EdClient->addTextInsertAction : adding ACTION : " + lastTextInsertAction );
    
    //notify parent of timestamp insertion for mapping
    // pars : a vector of type "long"
    parent.addToMapTimestamp(pars,timestampId);
  }

//new...

public void addTextPasteAction(long time, Vector pars, int sOffset, String text) 

{
    	
    if ( lastTextPasteAction != null ) 
	{
      	if( lastTextPasteAction.appendAction(time, pars, sOffset, text) )
        		return ;
      	else
        		actions.add(lastTextPasteAction) ;
   	}
    int timestampId = parent.getTimestampId();
    
    lastTextPasteAction = new TextPasteAction(timestampId,idNumber, time, pars, sOffset, text) ;
    
    parent.addToMapTimestamp(pars,timestampId);
    }
    
  public void addTextDeleteAction(long time, Vector pars, int sOffset, int eOffset, String text) {
    	
    if(lastTextDeleteAction != null) {
      if(lastTextDeleteAction.appendAction(time, pars, sOffset, eOffset, text))
        return ;
      else
        actions.add(lastTextDeleteAction) ;
    }
    
    int timestampId = parent.getTimestampId();
   
     lastTextDeleteAction = new TextDeleteAction(timestampId,idNumber, time, pars, sOffset, eOffset, text) ;
    
     parent.addToMapTimestamp(pars,timestampId);
    }
    //New

public void addTextCutAction(long time, Vector pars, int sOffset, int eOffset, String text) {
    	
    if(lastTextCutAction != null) {
      if(lastTextCutAction.appendAction(time, pars, sOffset, eOffset, text))
        return ;
      else
        actions.add(lastTextCutAction) ;
    }
    
    int timestampId = parent.getTimestampId();
    
    lastTextCutAction = new TextCutAction( timestampId, idNumber, time, pars, sOffset, eOffset, text) ;
    
    parent.addToMapTimestamp(pars,timestampId);
    
  }
    
  public void addGestureAction(long time, int id, long par, int x, int y, EditorServer es) {
    	
    if(lastGestureAction != null) {
      if(lastGestureAction.appendAction(time, id, par, x, y))
        return ;
      else{
        //ToDo: identify text of gesture 	
        lastGestureAction.setText(getTextForLastGesture(es)) ;
        actions.add(lastGestureAction) ;
      }
    } 
    
    int timestampId = parent.getTimestampId();
    
    lastGestureAction = new GestureAction(timestampId,idNumber, time, id, par, x, y) ;
    
    Vector pars = new Vector();
    
    pars.add( new Long(par) );
    
    parent.addToMapTimestamp(pars,timestampId);
  }
    
  public String getTextForLastGesture(EditorServer es) {
    Point topLeft = new Point(lastGestureAction.getMinX(), lastGestureAction.getMinY()) ;
    Point bottomRight = new Point(lastGestureAction.getMaxX(), lastGestureAction.getMaxY()) ;
	    
    return es.getTextForGesture(lastGestureAction.getStartPar(), topLeft, bottomRight) ;
  }
}
