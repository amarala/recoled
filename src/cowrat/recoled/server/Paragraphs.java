package cowrat.recoled.server;

import java.util.*;

import cowrat.recoled.server.SavedDocument.* ;

public class Paragraphs extends Vector implements EditorDebugFlags {
    
    private EditorDocument document ;
    private boolean initialized = false;
    private Map ParagraphTimestampMap ;	
    
    
    // Constructors..............................................................................................
    

    
    public Paragraphs(EditorDocument d) {
        super() ;
        document = d ;
        addParagraph(0, 1, 0) ;
        addParagraph(1,2,11);
       
       ParagraphTimestampMap = Collections.synchronizedMap ( new HashMap() );
    }
    
    
    //when is that const called ?
    
    public Paragraphs(EditorDocument d, Vector pars) {
    	super() ;
        document = d ;
        ParagraphTimestampMap = Collections.synchronizedMap ( new HashMap() );
        
        int index = 0 ;
        Iterator i = pars.iterator() ;
        while (i.hasNext()) {
        	SavedParagraph par = (SavedParagraph)i.next() ;
        	addParagraph(index, par.getId(), par.getStartOffset()) ;
        	index ++ ;
        }
    	
    }
    
    //returns true if par already mapped else false
    
    public boolean mapTimestamp( Long parId, int  timestampId )
    
    	{	    	
	    	Integer newTimestamp = new Integer (timestampId);
		    		
		    Vector timestampVector ;
	    		    	
	    	synchronized ( ParagraphTimestampMap )
	    	
	    		{
		    				    		    			
			    if ( ParagraphTimestampMap.containsKey ( parId) )
			    			
			    	{
				    timestampVector = (Vector) ( ParagraphTimestampMap.get ( parId ) );
				    
				    if(Paragraphs_Debug)			
				   			 System.out.print("\n````Paragraphs->mapTimestamp :  added par : " + parId + " timestampId : " + timestampId );
				    	
				    				
				    timestampVector.add(newTimestamp);	
				    
				    ParagraphTimestampMap.put( parId, timestampVector);  				    			    				
				    	
				    if(Paragraphs_Debug)			
				   			 System.out.print("\nParagraphs->mapTimestamp :  added par : " + parId + " timestampId : " + timestampId );
				    				    				    				   				
				    return true;
			    	}
			    				
			    	//paragraph not mapped ... add it
			    			
			    	timestampVector = new Vector();			
			    							    				
			    	timestampVector.add(newTimestamp);			    			
			    				
			    	ParagraphTimestampMap.put( parId, timestampVector);
			    				    		 
			    	if(Paragraphs_Debug)		
			    		System.out.print("\nParagraphs->mapTimestamp :  added par : " + parId + " timestampId : " + timestampId );
			    						    			
			    		
	    		}//endof 	synchronized...
	    		    	   
	    	return false;		    	
    	}
    	
    	//
    	
    	//This function called when a cut paragraph has been matched 
    	// with a Paste => old Vector timestamps is added (or appended) to
    	// new location of par
    
    public boolean mapTimestamps( Long parId, Vector  newTimestampVector )
    
    	{	    		    		
		    Vector oldTimestampVector ;
	    		    	
	    	synchronized ( ParagraphTimestampMap )	    	
	    		{
		    				    		    			
			    if ( ParagraphTimestampMap.containsKey ( parId) )			    			
			    	{				    	
				    	//There must be an easier way of doing this ???
				    	
				    oldTimestampVector = (Vector) ( ParagraphTimestampMap.get ( parId ) );
				    
				    Enumeration newTimestamps = newTimestampVector.elements();
				    
				    while ( newTimestamps.hasMoreElements() )				    
				    	{				    				
				    	oldTimestampVector.add( (Integer) ( newTimestamps.nextElement() ) );
				    	}
				    
				    ParagraphTimestampMap.put( parId, oldTimestampVector);  				    			    				
				    	
				    if(Paragraphs_Debug)			
				   			 System.out.print("\nParagraphs->mapTimestamps1 :  added par : " + parId + " timestamp Vector : " + oldTimestampVector );
				    				    				    				   				
				    return true;
			    	}
			    				
			    	//paragraph not mapped ... add it
			    			
			    	ParagraphTimestampMap.put( parId, newTimestampVector);
			    				    		 
			    	if(Paragraphs_Debug)		
			    		System.out.print("\nParagraphs->mapTimestamps2 :  added par : " + parId + " timestamp Vector : " + newTimestampVector );
			    						    			
			    		
	    		}//endof 	synchronized...
	    		    	   
	    	return false;
	    			    	
    	}//endof mapTimestamps (...,Vector ...)
    	
    	
    	// returns Vector of timestamps corresponding to parId and removes them
    	// from ParagraphTimestampMap : called when a paragraph disappears...
    	
    	public Vector fetchTimestamps( Long LongParId )    
    	 {
	    	
	    	Vector timestampsVector=null;
	    		    		    	
	    	synchronized ( ParagraphTimestampMap )	    	
	    		{		    				    		    			
			    timestampsVector = (Vector) ( ParagraphTimestampMap.remove(LongParId) );
		 		}
	    		    	   
	    	return timestampsVector ;		    	
    	}    	
    	
    	//
    	
    	// returns Vector of timestamps corresponding to parId 
    	// DOES NOT removes them from ParagraphTimestampMap 
    	// called by SavedDocument for XML file generation...
    	// Note : no need for synchronization here as HashMap not
    	// modified...
    	
    	public Vector getTimestamps( Long LongParId )    
    	 {	    	
	    	Vector timestampsVector=null;
	    		    				    		    			
			 timestampsVector = (Vector) ( ParagraphTimestampMap.get(LongParId) );
		 		 	   
	    	return timestampsVector ;		    	
    	}    	
    	
    	
    	// Merges timestamps of current Par and previous one
    	// when newline removed from currPar
    	
    public void mergeTimestamps ( long previousParId, long currParId )
    
    {
	     	    
	    Long currLongParId = new Long(currParId);
	    
	    Long previousLongParId = new Long(previousParId);
	    
	    //Note : no need to synchronize here as methods called are synchronized...
	    
	    // retrieve timestamps from current...    		    		    	
	    	    				    		    			
		Vector currParTimestampsVector = fetchTimestamps( currLongParId  ) ;
		
		if(Paragraphs_Debug)		
			    System.out.print("\n\n\niiiiiiiParagraphs->mergeTimestamps Vector : " + currParTimestampsVector );
					
		// map them to previous...
		
		mapTimestamps( previousLongParId, currParTimestampsVector);    
	    
	    
    }//endof mergeTimestamps()
    	
    	
    	//
    
    // paragraph access stuff....................................................................................

   
    public Vector getRangeFromIds(long startId, long endId) {
        
	/*if (Paragraphs_Debug)
        System.out.println("Paragrahs -> Extracting paragraph range from ids (" + 
                String.valueOf(startId) + "," + String.valueOf(endId) + ")") ;
          */      
        Iterator i = iterator() ;
        Vector range = new Vector() ;
        
        while (i.hasNext()) {
            Paragraph p = (Paragraph)i.next() ;
            if (p.compareId(endId) < 0)
                break ;
            
            if (p.compareId(startId) <= 0) 
                range.add(p) ;
        }
        
        if (Paragraphs_Debug)
        	{
        
        	System.out.print("Extracted: ") ;
        	i = range.iterator() ;
       		 while (i.hasNext())
            	System.out.print(i.next().toString() + " " ) ;
        
        	System.out.println(" ") ;
    		}
        return range ;
    }
    
    public Vector getRangeFromOffsets(int startOffset, int endOffset) {
        System.out.println("Extracting paragraph range from offsets (" + 
                String.valueOf(startOffset) + "," + String.valueOf(endOffset) + ")") ;
        
        Iterator i = iterator() ;
        Vector range = new Vector() ;
        Paragraph lastPar = null ;
        
        while (i.hasNext()) { 
            Paragraph p = (Paragraph) i.next() ;
            //System.out.print("Checking par " + p.toString() + " (" + String.valueOf(p.getOffset()) + ")") ;
            int currOffset = p.getOffset() ;
            
            if (currOffset > startOffset && lastPar != null) {
                //System.out.println(" - previous par extracted") ;
                range.add(lastPar) ;
                lastPar = null ;
            }
            
            if (currOffset >= startOffset && currOffset <= endOffset) {
                //System.out.println(" - extracted") ;
                range.add(p) ;
            }
            
            if (currOffset < startOffset) {
                lastPar = p ;
                //System.out.println(" - maybe extracted later") ;
            }
            
            if (currOffset == startOffset) {
                lastPar = null ;
                //System.out.println(" - last par will not be extracted") ;
            }
            
            if (currOffset > endOffset) {
                //System.out.println(" - not extracted") ;
                break ;
                //return range ;
            }
        }
        
        if (lastPar != null)
            range.add(lastPar) ;
            
            if (Paragraphs_Debug)            
            	{
        
        		System.out.print("Extracted: ") ;
       			 i = range.iterator() ;
        		while (i.hasNext())
            		System.out.print(i.next().toString() + " " ) ;
        
        		System.out.println(" ") ;        		
    			}
        return range ;
    }
    
    public Paragraph addParagraph(int index, long id, int offset) {

        Paragraph p = new Paragraph(id, offset, document) ;
        add(index, p) ;
        return p ;
    }
    
    public Paragraph addParagraph(long id, int offset) {
        int i = 0 ;
        
        while (i < size()) {
            Paragraph p = (Paragraph)elementAt(i) ;
            if (p.compareId(id) > 0)
                break ;
            
            i ++ ;
        }
        return addParagraph(i, id, offset) ;
    }

    
    public void removeParagraph(int index) {
        removeElementAt(index) ;
    }
    
    public void removeParagraph(Paragraph p) {
        int index = indexOf(p) ;
        remove(p) ;
    }
    
    public int getParIndexFromID(long parId) {
        
        int index = 0 ;
        while (index < size()) {
            Paragraph p = (Paragraph)elementAt(index) ;
            int result = p.compareId(parId) ;
            
            if (result == 0) return index ;
            
            if (result < 0) {
                System.out.println("\nParagraphs->getParIndexFromID : !!!!Paragraph did not exist") ;
                return -1 ;
            }
            
            index ++ ;
        }
        System.out.println("Paragraph did not exist") ;
        return -1 ;
    }
    
    public Paragraph getParFromID(long parId) {
        
        
        int index = getParIndexFromID(parId) ;
        
        if (index < 0) {
            System.out.println("Paragraph did not exist") ;
            return null ;
        } 
        else 
            return (Paragraph) elementAt(index) ;
    }
    
    public Paragraph getParFromIndex(int index) {
        return (Paragraph) elementAt(index) ;
    }
    
    public int getParIndexFromOffset(int offset) {
        
        
        
        int index = 0 ;
        while (index < size())
        
        {
            Paragraph p = (Paragraph)elementAt(index) ;
            
            if (p.getOffset() > offset)
            	{
	            	if (Paragraphs_Debug)
	            		System.out.println("\nParagraphs-> 1getParIndexFromOffset : " + String.valueOf(offset)+ "\n...Index is : " + (index - 1) ) ;
                	return index - 1 ;                
            	}
            index ++ ;
        }
        
        if (Paragraphs_Debug)
        	System.out.println("\nParagraphs-> 2getParIndexFromOffset : " + String.valueOf(offset)+ "\n...Index is : " + (index - 1) ) ;
        return index - 1 ;
    }
    
    public int compareIds(long id1, long id2) {
        int result = compare(id1, id2) ;
        System.out.println("Comparing "+String.valueOf(id1)+" with "+String.valueOf(id2)+": "+String.valueOf(result)) ;
        return result ;
    }
    
    public int compare(long id1, long id2) {
        
        if (id1 == id2)
            return 0 ;
        
        String s1[] = String.valueOf(id1).split("9") ;
        String s2[] = String.valueOf(id2).split("9") ;
        int index = 0 ;
        int i1, i2 ;
        
        while (index < s1.length && index < s2.length){
            
            if (s1[index].length() == 0) 
                i1 = 0 ;
            else
                i1 = Integer.valueOf(s1[index]).intValue() ;
            
            if (s2[index].length() == 0)
                i2 = 0 ;
            else
                i2 = Integer.valueOf(s2[index]).intValue() ;
            
            if ( i1 > i2) return 1 ; 
            if ( i2 > i1) return -1 ;
            
            index ++ ;
        }
        
        if (index < s1.length) return 1 ;
        if (index < s2.length) return -1 ;
        
        return 0 ;
    }

public boolean areParagraphsAtSameLevel( String stringId1,String stringId2 )

	{

	int count1= countLevel (stringId1);

	int count2 = countLevel (stringId2);

	if (count1 == count2)
		return true;

	return false;

	}

	//determine level :
	// ex 2 is level 0, 2.1 level 1, 2.1.1 level 2 etc...

	public int countLevel ( String StringId)
	{

	int count =0;

	StringTokenizer tokenizer = new StringTokenizer(StringId,".",true);
	

	while ( tokenizer.hasMoreTokens() )
		{		
		if (  (tokenizer.nextToken()).equals(".") )
			count++;
		}

	return count;

	}//end countLevel
    
    
}

















