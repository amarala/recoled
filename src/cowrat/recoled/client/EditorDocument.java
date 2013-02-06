package cowrat.recoled.client;

import javax.swing.text.* ;
import javax.swing.event.* ;
import javax.swing.ImageIcon;
import java.awt.Toolkit ;
import java.awt.Color ;
import cowrat.recoled.shared.MyImageIcon;

public class EditorDocument extends DefaultStyledDocument implements EditorDebugFlags {
	
    // Constants.......................................................................................................
    private static final int newline = 10 ;
	
    // Variables.......................................................................................................
	
    private EditorClient parent ;
    private LockManager lockManager ;
	
    private boolean notify ;
    private boolean restructure ;
    private boolean initialized = false;
    private boolean isRemoteInsert = false;
    private int clientID ;
	
    // Variable Access Methods.........................................................................................
	
	
    // Constructors.....................................................................................................
	
    public EditorDocument(EditorClient p, LockManager l) {
	super() ;
	parent = p ;
	lockManager = l ;
	clientID = -1 ;
	notify = true ;
	if (EditorDoc_Debug)
		System.out.println("\nNOTIFY->TRUE : EditorDocument Constructor") ;

	restructure = true ;
	
	ImageIcon lineIcon = new MyImageIcon ("images/blueline1.gif");
		
		Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
		Style line = this.addStyle("line", def);
		
		StyleConstants.setAlignment(line, StyleConstants.ALIGN_CENTER );
		if ( lineIcon != null)
			{ StyleConstants.setIcon(line, lineIcon);}
		
	MutableAttributeSet standard = new SimpleAttributeSet();
	StyleConstants.setSpaceAbove(standard, 10) ;
	StyleConstants.setFontFamily(standard, "Arial") ;
	StyleConstants.setFontSize(standard, 14) ;
	StyleConstants.setForeground(standard, Color.BLACK) ;
	setParagraphAttributes(0, 0, standard, true);
		
	addDocumentListener(new EC_DocumentListener()) ;
	setDocumentFilter(new EC_DocumentFilter()) ;
    }
	
    // Methods..........................................................................................................
	
    public void setCharAttributes(final int offset, final int length, final AttributeSet attrs) {

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
		public void run() {
		    setCharacterAttributes(offset, length, attrs, true) ;
		}
	    }) ;
    }
	
    public Position createPosition(int offset) {

	
	try {
	    return super.createPosition(offset) ;
	} catch (Exception e) {
	    System.out.println("Error: could not create position: " + String.valueOf(offset)) ;
	    return null ;
	}
    }
	
    public synchronized void appendText(final String text) {
	//javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
	//public void run() {
	try {

	 if (EditorDoc_Debug)			
		System.out.println("\n\nEditorDocument ->appendText : \n\n**"+text+"**\n\n") ;

	    notify = false ;
	if (EditorDoc_Debug)
		System.out.println("\nNOTIFY->FALSE : EditorDocument ->appendText ") ;
	    restructure = false ;
	    int offset = getEndPosition().getOffset() - 1 ;
	    
	    String whichCase="other";
	    
	    try{
		    
		    //This is our initial Blue line and the ascii "code" for it...	    
	   		if (  (text.substring(0,10) ).equals("·-·+·*=·x·") )	    
	    		{		   	   
		   		whichCase="blueLine";		   	    
	    		}
	    	
    	} 
    	catch (StringIndexOutOfBoundsException siobex ) 
    	{whichCase="catch";} 
    	
    	if ( whichCase.equals("blueLine"))
    		{insertString(offset, text , this.getStyle("line") );}	
	    
	    if ( whichCase.equals("other") )	    
	   	 { 
		   	
		   	 System.out.println("\nEdDoc->appending text @ offset : " + offset +" \n*" + text + "*\n" ) ;
		   	 insertString(offset, text, getCharacterElement(offset).getAttributes()) ; 
		   	 
		 }
	    
	    
	    restructure = true ;
	    notify = true ;
	if (EditorDoc_Debug)
		System.out.println("\nNOTIFY->TRUE : EditorDocument ->appendText ") ;
	} catch (Exception e) {
	    System.out.println("Error appending text") ;
	    e.printStackTrace() ;
	    //System.out.println(e.getStackTrace()) ;
	    
	}
	
    }
	
    public void undoInsertText(final int offset, final int length) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
		public void run() {
		    try {
			notify = false ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->FALSE : EditorDocument ->undoInsertText") ;
			restructure = false ;
			remove(offset, length) ;
			restructure = true ;
			notify = true ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->TRUE : EditorDocument ->undoInsertText") ;
		    } catch (Exception e) {
			System.out.println("Error undoing insert") ;
			e.printStackTrace() ;
			//System.out.println(e.getStackTrace()) ;
		    }
		}
	    }) ;
    }
	
    public void undoDeleteText(final int offset, final String text) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
		public void run() {
		    try {
			notify = false ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->FALSE : EditorDocument ->undoDeleteText") ;
			restructure = false ;
			insertString(offset, text, getDefaultRootElement().getAttributes()) ;
			restructure = true ;
			notify = true ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->TRUE : EditorDocument ->undoDeleteText") ;
		    } catch (Exception e) {
			System.out.println("Error undoing insert") ;
			//System.out.println(e.getMessage()) ;
		    }
		}
	    }) ;
    }	    
	
	
    /*
      Inserts a string of text into a position in a particular paragraph.
      Called when an insertion is made by someone other than this client
    */
    public void insertText(final int offset, final String text, final int cID) {
		
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
		public void run() {
		    try {

			//if (EditorDoc_Debug)			
				System.out.println("\n\nEditorDocument ->insertText : \n\n**"+text+"**\n\n") ;

			clientID = cID ;
			notify = false ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->FALSE : EditorDocument ->insertText") ;
			    isRemoteInsert = true;
			    //necessary to deal with presinsertion at start of doc in case
			    // of remote update (otherwise condition on offset won't permit the operation)..
				insertString(offset, text, getDefaultRootElement().getAttributes()) ;
				
			notify = true ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->TRUE : EditorDocument ->insertText") ;
			clientID = -1 ;
		    } catch (Exception exception) {
			System.out.println("EditorDocument ->insertText : insert failed: " +
					   " offset=" + String.valueOf(offset) + 
					   " text=\"" + text + "\"" ) ;
			System.out.println(exception.getMessage()) ;
			exception.printStackTrace() ;
		    }
		}
			
	    }) ;
	    
	    
    }
	
    /*
      Removes all text from a position in one paragraph to a position a certian 
      distance before the start of another paragraph.
      Called when a deletion is made by someone other than this client
    */   
    public void deleteText(final int offset, final int length, final int cID) {
		
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
				
		    try {
			clientID = cID ;
			notify = false ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->FALSE : EditorDocument ->deleteText") ;
			remove(offset, length) ;
			notify = true ;
			if (EditorDoc_Debug)
				System.out.println("\nNOTIFY->TRUE : EditorDocument ->deleteText") ;
			clientID = -1 ;
					
		    } catch (Exception exception) {
			//System.out.println(exception.getMessage()) ;
		    }
		}
			
	    }) ;
	    
	  
    }
	
    private class EC_DocumentListener implements DocumentListener {

	public void insertUpdate(DocumentEvent e) {

	    if(restructure) 
		try {	
		
		int offs = e.getOffset();
		int length =  e.getLength();
	
		    String text = getText( offs , length ) ;

		 	if (EditorClient_Debug)
				{
				System.out.println("\n\nEditorDocument -> insertUpdate: INSERTION FIRED @ offset : " + offs) ;		
				System.out.println("\n\nEditorDocument -> insertUpdate: insertion of text : \n\n**"+text+"**\n\n") ;
				}

		 if ( length==1 )

			{
			if (EditorClient_Debug)
				System.out.println("\nEditorDocument -> insertUpdate-->INSERTION") ;

			lockManager.insertUpdate( offs, text, clientID) ;

			}

		 if (e.getLength()>1)
			{
			if (EditorClient_Debug)
				System.out.println("\nEditorDocument -> insertUpdate-->PASTING");
			
		   	lockManager.insertUpdate(offs, text, clientID) ;
			}

		} catch (Exception x) {
		    System.out.println("client.EditorDocument ->insertUpdate: error retrieving text") ;
		}
	}
		
	public void removeUpdate(DocumentEvent e) {
	    lockManager.removeUpdate(e.getOffset()) ;
	}
		
	public void changedUpdate(DocumentEvent e) {
	    // Dont care about style changes
	}
		
    } 
	
    private class EC_DocumentFilter extends DocumentFilter { 
	    
	   		
	public void remove (DocumentFilter.FilterBypass fb, int offset, int length) 
	    throws BadLocationException {
		    
		     //This corresponds to the first "dummy paragraph"
		    // and deletion is not permitted...
		    if (offset <=10)
				Toolkit.getDefaultToolkit().beep() ;
				
			else //else1
			
			{
				
			String textRemoved = getText(offset, length);	
			
	    	if (restructure) 
				{

				if (EditorClient_Debug)			
					System.out.println("\nclient.EditorDocument-> remove : RESTRUCTURING...\n") ;
				if (lockManager.remove(offset, length,textRemoved , notify))
		    		fb.remove(offset,length);
				else
		    		Toolkit.getDefaultToolkit().beep() ;
	    		}
	    	else
			{
			if (EditorClient_Debug)			
				System.out.println("\nNOT RESTRUCTURING...") ;
			fb.remove(offset,length);
			}
		
		}//endelse1
	}         
	
//This method is called first for local insertion
	
	public void insertString (DocumentFilter.FilterBypass fb, int absOffset, String string, AttributeSet attr) 
	    throws BadLocationException {
		    
		      /*All conditions concerning <10, >10 correspond to the
		     * first "dummy paragraph"
		     * and inserts are not permitted... (a part when the initial state
		     * of the document is loaded via the lockManager.appendText() method
		     * which creates the lineIcon)... therefore the 
		     * initialized variable		     
		    */
		    if (EditorClient_Debug)			
					System.out.println("\n\nEditorDocument ->insertString : absOffset= " + absOffset + " isRemoteInsert : " + isRemoteInsert  );
		    
		    		    		    
		    if ( (absOffset <10 && string.length()<10 ) || (absOffset <10 && initialized) )
		    	{
			    	Toolkit.getDefaultToolkit().beep() ;		// editing operation not permitted      
			    	//System.out.println("\n\nEditorDocument ->insertString1");
		    	}
		    else if (absOffset <10 && !( ( string.substring(0,10) ).equals("·-·+·*=·x·") )  && !initialized )
				{
					Toolkit.getDefaultToolkit().beep() ;		// editing operation not permitted   		
					//System.out.println("\n\nEditorDocument ->insertString2");
				}
			else if ( (absOffset==10) && (isRemoteInsert==false ) )
				{
					Toolkit.getDefaultToolkit().beep() ;		// editing operation not permitted   		
					//System.out.println("\n\nEditorDocument ->insertString3");
				}
			else //else1			
				{  
					//System.out.println("\n\nEditorDocument ->insertString4");	
					isRemoteInsert = false;				
					initialized = true;
		    			
				if (EditorClient_Debug)			
					System.out.println("\n\nEditorDocument ->insertString : \n\n**"+string+"**\n\nrestructure : "+restructure ) ;
       		
	    		if(restructure) 
					{
						Color color ;
					try{
					color = lockManager.getClients().getClient(clientID).getMainColor() ;}
					catch(Exception e){
						color = lockManager.getClients().getMyColor();}
					StyleConstants.setForeground((MutableAttributeSet)attr, color) ;

					//first call (false as 4th argument) check wether it is a preInsertion case...
					//returns "PreInsertion" if it's the case...
					//returns "false" if doc is not editable...
		
					String answer = lockManager.insertString(absOffset, string, notify,false);
			
					if( answer.equals("PreInsertion") )
						{
						//2nd call (true as 4th argument) adjust offset
						if (EditorDoc_Debug)
							System.out.println("+++++++\n\nEditorDocument ->insertString PREINSERTION!!!" );
							
						absOffset--; //offset is recast...
						
						//The following is necessary to achieve correct segmentation
						// of paragraphs in case of a Paste

						if (string.length()>1)
							{
							if (  ( ( string.indexOf("\n") ) >= 0 ) && !(string.endsWith("\n") ) )
								{string = string.concat("\n") ; }
									
							if ( !(string.startsWith("\n") ) )
								{ string = "\n".concat(string) ; }							
							
							}

						answer = lockManager.insertString(absOffset, string, notify,true);							
						if (EditorDoc_Debug)
							System.out.println("\n\nEditorDocument ->insertString : lockManager.insertString answer is : " +answer );
				
						} //endof if( answer.equals("PreInsertion") )
				
				if ( answer.equals("true") )
						{
						if (EditorDoc_Debug)			
								System.out.println("\n\n***EditorDocument ->insertString : restructure TRUE : DOCUMENT HAS BEEN UPDATED!!!" ) ;
			   			//fb.insertString(absOffset,string,attr);
						super.insertString(fb,absOffset,string,attr);
						}
				else
		    	Toolkit.getDefaultToolkit().beep() ;
	    	}
	    	else
					{
					if (EditorDoc_Debug)
						System.out.println("\n\n***EditorDocument ->insertString : restructure FALSE : DOCUMENT HAS BEEN UPDATED!!!" ) ;
					fb.insertString(absOffset,string,attr); 
					}
					
		}//endelse1
		
		

	}//end insertString()
		
		
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
	    throws BadLocationException {
			
	    if (length > 0) 
	    {
		    if (EditorDoc_Debug)	
				System.out.print( " - ") ;
			remove(fb, offset, length) ;
	   	 }
		if (EditorDoc_Debug)		
	    	System.out.print( " - ") ;
	    insertString(fb, offset, text, attrs) ;
	}
    }
}


