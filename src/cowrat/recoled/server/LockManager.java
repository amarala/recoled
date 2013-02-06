//feb version 

package cowrat.recoled.server;

import java.io.* ;
import java.util.* ;
import java.text.SimpleDateFormat ;
import javax.swing.DefaultListModel;
import cowrat.recoled.audio.*;

public class LockManager implements EditorDebugFlags {
    
  private Paragraphs paragraphs ;
  private EditorDocument document ;
  private Vector clients ;
  private Map textCutsToTimestamps ;	
  
  private static final int newline = 10 ;
  
  //const
  
  
  public LockManager(Vector c, EditorDocument d, Paragraphs p) {
    clients = c ;
    document = d ;
    paragraphs = p ;
    textCutsToTimestamps = Collections.synchronizedMap ( new HashMap() );
  }
  
  //maps textCuts to Vector of timestamps
    
    public void mapTextCuts( String textCut, long parId  )
    
    	{	    	
	    	
	    	Long LongParId = new Long(parId);
	    	
	    	Vector timestampsVector = paragraphs.fetchTimestamps( LongParId );
	    	
	    	if (LockManagerDebug)
	    		System.out.print("\n\n^^^^^^LockManager->mapTextCuts  recovering timestampVector !!! " +  timestampsVector  );
	    	
	    	if (timestampsVector==null)
	    		    System.out.print("LockManager->mapTextCuts ERROR recovering timestampVector !!! ");
	    		    
	    	synchronized ( textCutsToTimestamps )	    	
	    		{		    				    		    			
			    textCutsToTimestamps.put( textCut, timestampsVector);  				    			    				
				}	
				if (LockManagerDebug)
					System.out.print("LockManager->mapTextCuts : MAPPING text :  \n\n\n--**--"+ textCut +"--**--\n\nTo timestampsVector : "+ timestampsVector.toString() +"---\n\n\n");		    		    	   
	    		    		    	
    	}
  
    	//try to match a new textInsertedmaps to textCuts key in textCutsToTimestamps HashMap
    
    public void matchInsertToTextCuts( String textInserted, long parId  )
    
    	{	  
	    	 
	    if ( textCutsToTimestamps.containsKey( textInserted) )
	    	{
		    if (LockManagerDebug)
				System.out.print("\n\n%%%%%%LockManager->matchInsertToTextCuts Match found for new? par :  " +  parId    +  " for text :  \n\n%%*"+ textInserted +   "*%%\n\n");
				
	    	synchronized ( textCutsToTimestamps )	    	
	    		{	
		    		Long LongParId = new Long(parId);
		    		
		    		//recover Vector of previous timestamps and...
		    		//removes textCut from  textCutsToTimestamps HashMap
		    				
		    		Vector timestampsVector = (Vector) ( textCutsToTimestamps.remove( textInserted ) );	 
		    		if (LockManagerDebug)
		    			System.out.print("\n\n%%%%%%LockManager->matchInsertToTextCuts : Vector :  " + timestampsVector );
		    		
		    		if (timestampsVector!=null)
		    			paragraphs.mapTimestamps( LongParId,timestampsVector );		    	
				}
			    					    			    				
			}	 
				
		}//endof matchInsertToTextCuts
	    	
	  
  public synchronized String getText(long startPar, int startOffset, long endPar, int endOffset) 
  {
    Paragraph sPar = getParFromId(startPar) ;
    Paragraph ePar = getParFromId(endPar) ;
    
    int s = sPar.getOffset() + startOffset ;
    int e = ePar.getOffset() + endOffset ;
    
    try {
      return document.getText(s, e-s) ;
    } catch (Exception x) {
      return "" ;
    }
  }
  
  public synchronized DefaultListModel getListModel() {
    
    DefaultListModel model = new DefaultListModel() ;
    
    Iterator i = paragraphs.iterator() ;
    
    while (i.hasNext()) {
      Paragraph p = (Paragraph) i.next() ;
      String newModelElement = p.toString() + "       " + String.valueOf(p.getOffset());
      model.addElement ( newModelElement ) ;
	//if (LockManagerDebug)
		//System.out.println("LockManager->getListModel : "+ newModelElement) ;

    }
    
    return model ;
  }
  
  public synchronized void saveXML(String fileName, Highlights highlights, EditorServer es) {
    
    File file ;
    
    try {
      SavedDocument doc = new SavedDocument(document, paragraphs, clients, highlights, es) ;
      if (fileName != null)
        file = new File(fileName) ;
      else
        return;
      
      file.createNewFile() ;
      
      BufferedWriter out = new BufferedWriter(new FileWriter(file)) ;
      out.write(doc.getXML()) ;
      out.close() ;
    } catch(Exception e) {
      System.out.println("LockManager: saveXML. error") ;
      e.printStackTrace() ;
    }
  }
  
  // Matt will add code to generate text profiles and merge it into this method.
  public synchronized void saveProfile(String fileName, Profile prof) {
    
    File file ;
    
    try {
      if (fileName != null)
        file = new File(fileName) ;
      else
        return;
      
      file.createNewFile() ;
      
      BufferedWriter out = new BufferedWriter(new FileWriter(file)) ;
      out.write(prof.toString()) ;
      out.close();
    } catch(Exception e) {
      System.out.println("LockManager: saveProfile. error") ;
      e.printStackTrace() ;
    }
  }
  
    
  public synchronized void saveDocument(String fileName, Highlights highlights, EditorServer es) {
    	/*
    File file ;
    	
    try {
      SavedDocument doc = new SavedDocument(document, paragraphs, clients, highlights, es) ;
      if (fileName != null) {
        String name = fileName ;
        if (!name.endsWith(".cde"))
          name = name + ".cde" ;
    			
        file = new File(name) ;
      }else {
        Date date = new Date(System.currentTimeMillis()) ;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh-mma") ;
        String dateString = format.format(date) ;
    			
        System.out.println("Saves/" + document.getTitle() + dateString + ".cde") ;
        file = new File("Saves/" + document.getTitle() + dateString + ".cde") ;
      }
      file.createNewFile() ;
    		
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file)) ;
      out.writeObject(doc) ;
    } catch(Exception e) {
      System.out.println("LockManager: saveDocument. error") ;
      e.printStackTrace() ;
    }
*/
  }
    
  public synchronized Paragraph getParFromOffset(int offset) {
    return paragraphs.getParFromIndex(paragraphs.getParIndexFromOffset(offset)) ;        
  }
    
  public synchronized Paragraph getParFromId(long id) {
    return paragraphs.getParFromID(id) ;
  }
    
  
    
  public synchronized boolean lockRequested(long startPar, long endPar, int clientId) {
    if (isEditableBy(startPar, endPar, clientId)) {
            
      Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
      Iterator i = range.iterator() ;
      while (i.hasNext()) {
        Paragraph p = (Paragraph) i.next() ;
        p.lock(clientId) ;
      }
      return true ;
    }
    else
      return false ;
  }
    
  public synchronized void lockReleased(long startPar, long endPar, int clientId) {
    Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
    Iterator i = range.iterator() ;
    while (i.hasNext()) {
      Paragraph p = (Paragraph) i.next() ;
            
      if (p.getLockOwner() == clientId)
        p.unlock() ;
    }
  }
    
  public synchronized Vector textInserted (long par, int insertOffset, String text, int clientId)

 {
	 boolean PASTE = false;
	 
	 if ( text.length() >1 )
	 	PASTE = true;
	 
    if (LockManagerDebug)
	{
	System.out.print("\nLockManager-> textInserted. ") ;
    	System.out.print(" par:" + String.valueOf(par)) ;
    	System.out.print(" off:" + String.valueOf(insertOffset)) ;
    	System.out.print("\n Text inserted is :\n\n*" + text+"*\n\n") ;
	}
        
    Vector pars = new Vector() ;
            
    // insert text into document

    Paragraph currPar = paragraphs.getParFromID(par) ;
    
    long currParId = currPar.getID() ;

    String currStringId = currPar.toString();
   
    pars.add(new Long(currParId )) ;

    
    int absOffset = currPar.getOffset() + insertOffset ;
    
     if (LockManagerDebug)
    	System.out.print("\n abOff:" + String.valueOf(absOffset)) ;
    
    document.insertText(absOffset, text) ;

   
    // ..calculate level of paragraph after current one
    int parIndex = paragraphs.indexOf(currPar) ;
    int nextLevel ;

   Paragraph nextParagraph =null;

    //currPar is not the last Paragraph

    if ( parIndex < paragraphs.size() - 1)
	{

	nextLevel = (paragraphs.getParFromIndex(parIndex + 1).getLevel()) ;

	 //find the offset of following paragraph...

      	nextParagraph = (Paragraph) (paragraphs.elementAt( parIndex +1 )) ;

	}
    	
   //currPar is the last Paragraph
   else
      nextLevel = currPar.getLevel() - 1 ;

 //view content of doc

  String ParContent="";
  
  String DocumentContent ="";

   	 try{
   	DocumentContent = document.getText( 0, document.getLength() );
   		   
     	 }//endtry

   	catch  ( Exception bde ) {System.out.print("\nLockManager->textInserted Error recovering THIS Content" ) ;}
   	
    int previousNewLineIndex = -1;
    
	int newLineIndex = text.indexOf(newline) ;
	
	int lastNewLineIndex = text.lastIndexOf(newline) ;	
	
	//case where text inserted contains no new line...
	if (newLineIndex<0)
		{
		if (LockManagerDebug)
				System.out.print("\nLockManager--+-->textInserted : CASE1 content of THIS paragraph is : \n\n***" +  text +"***\n\n" ) ;
		if (PASTE)
			matchInsertToTextCuts(text,currParId );
		}
	
	/*This loop only executed if a newLine is present in the text inserted
	*  which will create new paragraphs...
	*  need to segment this to the par format for comparison
    *  with cutsTimestamps map...
    */
	
   	while (newLineIndex >= 0)
		{
      		parIndex ++ ;
      		
      		//id of the new paragraph being inserted...
      		
      		long newParId = currPar.getNextID(nextLevel);
      		
      		//int currParOffset = currPar.getOffset();
      		
      		//int nextParOffset = -1;      		
      		
      		///segmenting textInsert in order to match with previous cuts...      		
      		
				//first iteration
			if (previousNewLineIndex == -1)
			
				{ 
					try{ParContent = ( DocumentContent.substring (absOffset, absOffset+ newLineIndex ) ).trim(); }
					
					catch(Exception e) {System.out.print("\nLockManager--+-->textInserted : CASE2 BIT A BONE!!!");} 
					
					if (LockManagerDebug)
						System.out.print("\nLockManager--+-->textInserted : CASE2 content of THIS paragraph is : \n\n***" + ParContent +"***\n\n" ) ;
					if (PASTE)
						matchInsertToTextCuts(ParContent,newParId-1);
				}
				
			else 
			
				{
					try{ParContent = ( DocumentContent.substring (absOffset+previousNewLineIndex+1, absOffset+ newLineIndex  ) ).trim();	}
					
					catch(Exception e) {System.out.print("\nLockManager--+-->textInserted : CASE3 BIT A BONE!!!");} 
					
					if (LockManagerDebug)
						System.out.print("\nLockManager--+-->textInserted : CASE3 content of THIS paragraph is : \n\n***" + ParContent +"***\n\n" ) ;
					
					if (PASTE)
						matchInsertToTextCuts(ParContent,newParId-1);
					
				}
			
			//System.out.print("\nLockManager---->textInserted : content of THIS paragraph is : \n\n***" +  ParContent+"***\n\n" ) ;
	 		 	    
      		currPar = paragraphs.addParagraph(parIndex, currPar.getNextID(nextLevel), absOffset + newLineIndex + 1) ;
		
      		pars.add(new Long(currPar.getID())) ;

			if (LockManagerDebug)
      			System.out.println("\nLockManager -> Par added: id=" + currPar.toString() + " offset=" + String.valueOf(currPar.getOffset()) + " docLength=" + document.getLength()) ;
			
      		currPar.lock(clientId) ;
      		
      		previousNewLineIndex = newLineIndex ;
     		newLineIndex = text.indexOf(newline, newLineIndex + 1) ;
    		} 
    		
    		if ( previousNewLineIndex >=0 &&  !( text.endsWith("\n") ) )
    			{
	    		try{ParContent = ( DocumentContent.substring (absOffset+previousNewLineIndex+1, absOffset+ text.length()  ) ).trim();}
	    		
	    		catch(Exception e) {System.out.print("\nLockManager--+-->textInserted : CASE4 BIT A BONE!!!");} 
	    		
				//System.out.print("\nLockManager--=-->textInserted : content of THIS paragraph is : \n\n***" +  ParContent+"***\n\n" ) ;
				
				if (LockManagerDebug)
					System.out.print("\nLockManager--+-->textInserted : CASE4 content of THIS paragraph is : \n\n***" + ParContent +"***\n\n" ) ;
				
				//what happens here ???
				if (PASTE)
					matchInsertToTextCuts(ParContent,0);
	 		 	 }
	

    try {
	    if (LockManagerDebug)
      		System.out.println(" endOff:" + String.valueOf(document.getEndPosition().getOffset())) ;
    } catch(Exception e) {} 

   
    return pars ;

  }//endof textInserted ()
    
  ////////////////////////////////////////////////////////////
  
  
  public synchronized Vector textDeleted(long startPar, int startOffset, long endPar, int endOffset) {
   
	  	boolean CUT = false;
	  		  
	  if (LockManagerDebug)
	  	System.out.println ("LockManager: textDeleted. " + String.valueOf(startPar) + " " + String.valueOf(endPar)) ;
    
	  	Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
	  	Vector pars = new Vector() ;
        
    Paragraph sP = (Paragraph) range.firstElement() ; 
    Paragraph eP = (Paragraph) range.lastElement() ;
    
    int absStartOffset =  sP.getOffset() + startOffset;
    int absEndOffset =  eP.getOffset() + endOffset;
    
     if ( Math.abs(absStartOffset-absEndOffset)>1 )
    	{
	    CUT=true;
	    
	    if (LockManagerDebug)
    		{
	    		System.out.println ("LockManager: textDeleted : THIS IS A CUT!!!");
    			System.out.println ("LockManager: textDeleted : startOffset : " +absStartOffset + " endOffset" + absEndOffset) ;    			
			}			
		}        
        
    String DocumentContent = "";
    //String ParContent ="";
    String textDeleted = "";
    
    try{
   		DocumentContent = document.getText( 0, document.getLength() );   		
   		 }   		 
   		 catch  ( Exception bde ) {System.out.print("LockManager ->textDeleted : Error recovering Doc content.");}
        //
        
    Iterator i = range.iterator() ;
    while (i.hasNext()) 
    
    	{	    	
	    	//Current par variables...
	    	
      	Paragraph currPar = (Paragraph) i.next() ;
      	long  currParlongId =  currPar.getID() ;
      	pars.add(new Long(currParlongId)) ;      
      	int currParOffset =  currPar.getOffset() ;
      	int parIndex = paragraphs.getParIndexFromOffset(currParOffset);
      	
      	if (LockManagerDebug)
      		System.out.print("Checking par " + currPar.toString()) ;
      
     	 //Define next parOffset :
     	 
     	 int nextParOffset = -1;
      
      	//currPar not last one :
      
      	 if (parIndex < paragraphs.size() - 1)
      
      		{
	      		Paragraph nextParagraph = paragraphs.getParFromIndex (parIndex + 1) ;
			
				nextParOffset = nextParagraph.getOffset();				
			}
		
		else // last par
      			{nextParOffset = document.getLength();}
      
      // there are 4 cut cases :
      // (and plenty subcases...)
      
      if (LockManagerDebug)
      			System.out.println("\n\n\n\n>>>>>>>>START:  " + absStartOffset + "End : "+ absEndOffset + "Doc length : "+ DocumentContent.length() );
      
      
      //only middle of par is affected by Cut...paragraph stays...)
      
      if ( absStartOffset > currParOffset &&   absEndOffset < nextParOffset )
      		{
	      		if (LockManagerDebug)
      				System.out.print("\n>>>>LockManager ->textDeleted MIDDLECut : par is kept..." ) ;	      		   			
      		}
      		
      		//whole par cut 
      		
      	else if (absStartOffset <= currParOffset && absEndOffset >= nextParOffset )
      	
      		{
	      		if (LockManagerDebug)
      				System.out.print("\n>>>>LockManager ->textDeleted WHOLECut : par is removed..." ) ;	      		
	      			      
      				if (CUT)
      				{
	      				// text content of THIS par...
	      				textDeleted = (DocumentContent.substring(currParOffset,nextParOffset ) ).trim() ;
	      				
	      				mapTextCuts(textDeleted, currParlongId);		
	      					      	
	      				//There is one case where we want the paragraph to remain
	      				// and that is one the cut started at the same offset as the paragraph...
	      					      	
	      				if ( ( currParlongId != 1) && ( currParOffset != absStartOffset) )	      				
	      					paragraphs.removeParagraph(currPar) ;				      				
      				}	
      					
      				//case last paragraph which is empty removed by deleting last "\n"...
      				
      				      				
      				else if ( absEndOffset == DocumentContent.length() && ( absStartOffset < currParOffset ) ) 
      				{		
      				System.out.println("\n\n\n\n>>>>>>>>START:  " + absStartOffset + "End : "+ absEndOffset + "Doc length : "+ DocumentContent.length() );
      				
      				paragraphs.removeParagraph(currPar) ;	
  					}      				
	      	}
      		
      		//end of WHOLE par cut
      		
      	else if (absStartOffset > currParOffset &&   absEndOffset >= nextParOffset )
      	
      		{	      		
	      		if (LockManagerDebug)
      				System.out.print("\n>>>>LockManager ->textDeleted ENDParCut : par is kept" ) ;	      			      		
      		}
      		
      		// beginning of par cut  : par disapears...
      		// Merging Paragraph case...	
      
 		else if (currParOffset >absStartOffset  && currParOffset <= absEndOffset)
      		{
	      		
	      	if (LockManagerDebug)
      				System.out.print("\n>>>>LockManager ->textDeleted BEGININGParCut : par is removed" ) ;
      			
      	// NOTE : need to implement paragraphs methods to make this smoother...
      					
      	  	try{
	      	  	
	      	  	int mergingParIndex = paragraphs.getParIndexFromOffset(absStartOffset) ;
      				
      			Paragraph mergingPar = paragraphs.getParFromIndex(mergingParIndex) ;
      	
      			long mergingParId = mergingPar.getID();
      	
      			paragraphs.mergeTimestamps(mergingParId, currParlongId);  
	      		
        		paragraphs.removeParagraph(currPar) ;
        		
    			}
    			
    		catch (Exception e) {
	  		System.out.print("\n>>>>LockManager ->textDeleted EXCEPTION : BEGININGParCut " ) ;
	  		paragraphs.removeParagraph(currPar) ;}
        	
     		 }//end beginCut
     	 
    	else
    		{ 
	    		if (LockManagerDebug)
    				System.out.print("\n>>>>!!!LockManager ->textDeleted : text deleted but par is kept !!!");
    		
    				if (CUT)
      				{
	      				// text content of THIS par...
	      				 textDeleted = ( DocumentContent.substring(currParOffset,nextParOffset ) ).trim() ;
	      				
	      				mapTextCuts(textDeleted, currParlongId);	      					      				
      				}	
      				
  			}//endelse last case	
    		
    		
    		}//endwhile
        
    // delete text from document
    document.deleteText(absStartOffset, absEndOffset - absStartOffset) ;
    return pars ;
	  
  }//end LockManager ->textDeleted 
	  	
    
  public synchronized String getDocumentAsXML() {
    String xml = "" ;
        
    try {
      int length = document.getLength() ;
      int endOffset = document.getLength() - 1 ;
      
      for (int i = paragraphs.size() - 1 ; i >= 0 ; i--)
      
      	{
        String line = "<par" ;
        Paragraph p = (Paragraph)paragraphs.elementAt(i) ;
        int startOffset = p.getOffset() ;
        if (startOffset > length)
          startOffset = startOffset - length ;
                
        line = line + " id=" + String.valueOf(p.getID()) ;
        line = line + " offset=" + String.valueOf(startOffset) ;
        line = line + " lockOwner=" + String.valueOf(p.getLockOwner()) ;                
         
        if (LockManagerDebug)
       	 {       
        	System.out.println("Getting text from document: " + 
                           " length=" + String.valueOf(document.getLength()) + 
                           " start=" + String.valueOf(startOffset) + 
                           " end=" + String.valueOf(endOffset)) ;
         }
                           
        if(i==0)
        	 line = line + ">·-·+·*=·x·\n</par>" ; 
        else if (endOffset > startOffset) 
          line = line + ">" + document.getText(startOffset, endOffset - startOffset) + "</par>" ;
        else
          line = line + "></par>" ;	
                
        xml = line + xml ;
        endOffset = startOffset ;
      }
    } catch (Exception e) {
      System.out.println("LockManager: getDocumentAsXML: error getting state") ;
      e.printStackTrace() ;
    }
        
    System.out.println("LockManager-> getDocumentAsXML :\n" + xml) ;
    return xml ;
  }
    
  // Private methods...................................................................................................
    
  /*
    Indicates weither a range of text can be edited (whether it is possible to obtain a lock for this region)
  */
  public boolean isEditableBy(long startId, long endId, int clientId) {
        
    Vector range = paragraphs.getRangeFromIds(startId, endId) ;
    Iterator i = range.iterator() ;
        
    while (i.hasNext()) {
      Paragraph p = (Paragraph) i.next() ;
      if(!p.isEditableBy(clientId))
        return false ;
    }
    return true ;
  }
      
}//endclass
