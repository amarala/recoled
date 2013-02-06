package cowrat.recoled.client;

import java.util.Vector ;
import java.util.Iterator ;
import java.util.StringTokenizer ;

public class Paragraphs extends Vector implements EditorDebugFlags  {
    
    private EditorClient parent ;
    private EditorDocument document ;	    
    private Vector paragraphPanels ;
    //private static String smalestParagraphId="1";
    //private static String largestParagraphId=null;
    private boolean initialized = false;
    //to sort the fact that no paragraph may be inserted before par 1
    private static boolean caretInEmptyParagraph=true;
   
    // Variable access methods...................................................................................
    
    public void setDocument(EditorDocument d) { document = d ; } 
    
    // Constructors..............................................................................................
    
    public Paragraphs(EditorClient p) {
        super() ;
        parent = p ;
        paragraphPanels = new Vector() ;
        
    }  
    
    public void addParagraphPanel(ParagraphPanel p) {
        paragraphPanels.add(p) ;
        int count = 0 ;
        
        Iterator i = iterator() ;
        while (i.hasNext()) {
            
            Paragraph par = (Paragraph)i.next() ;
            p.addParagraphBox(par, count) ;
            count ++ ;
        }
    }


    
//this is a good place to update info about paragraphs as this
// function is called whenever there is some editing action...

    public void resizeParagraph(Paragraph p) {
        int index = indexOf(p) ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) {
            ParagraphPanel pp = (ParagraphPanel) i.next() ;
            pp.resizeParagraph(index) ;
        }
	//toString();

//update smalestParagraphId && largestParagraphId
//This is not being used after all...

	/*smalestParagraphId="1";
   	largestParagraphId=null;

	Iterator ii = iterator();

	while( ii.hasNext() )

	{
	Paragraph par = (Paragraph)ii.next();

	String StringParId = par.toString();

	//if ( Paragraphs_Debug  )
		//System.out.print("\n\nParagraphs-->addParagraph : id = "+ StringParId  +"\n" ) ;

        	if ( (Paragraph.CompareStringId(smalestParagraphId,StringParId) )==1 )
		smalestParagraphId = StringParId;

	if (largestParagraphId==null)
		largestParagraphId = StringParId;

	else if ( (Paragraph.CompareStringId(StringParId,largestParagraphId) )==1 )
		largestParagraphId = StringParId;

	
	 }

	if ( Paragraphs_Debug  )
		{
		//System.out.print("\n\nParagraphs------------> smalestParagraphId = "+ smalestParagraphId  + " largestParagraphId = "+ largestParagraphId+ "\n\n" ) ;
		}
		
		*/

	
    }

	//says wether the paragraph in which  the caret is empty or not

   public void setCaretInEmptyParagraph (boolean answer)
	{
		if ( Paragraphs_Debug  )
			System.out.print("\n\n-+-+-+--Paragraphs----> setCaretInEmptyParagraph : status is :  " +  answer  ) ;
	
		caretInEmptyParagraph = answer;
	}

   public boolean isCaretInEmptyParagraph()
	{ return caretInEmptyParagraph;}
    
    // locking stuff.............................................................................................
    public void lock(Paragraph p, int clientId) {
        int index = indexOf(p) ;
        p.lock(clientId) ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) {
            ParagraphPanel pp = (ParagraphPanel) i.next() ;
            pp.lock(index, clientId) ;
        }
    }
    
    public void tempLock(Paragraph p) {
        int index = indexOf(p) ;
        p.tempLock() ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) {
            ParagraphPanel pp = (ParagraphPanel) i.next() ;
            pp.tempLock(index) ;
        }
    }
    
    public void unlock(Paragraph p) {
        int index = indexOf(p) ;
        p.unlock() ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) {
            ParagraphPanel pp = (ParagraphPanel) i.next() ;
            pp.unlock(index) ;
        }
    }
    
    // paragraph access stuff....................................................................................
    
    public Vector getRangeFromIds(long startId, long endId) {
        //int blah = 1/0 ;
        if ( Paragraphs_Debug  )
        	{
        	System.out.println("Extracting paragraph range from ids (" + 
                String.valueOf(startId) + "," + String.valueOf(endId) + ")") ;
            }
        Iterator i = iterator() ;
        Vector range = new Vector() ;
        
        while (i.hasNext()) {
            Paragraph p = (Paragraph)i.next() ;
            if (p.compareId(endId) < 0)
                break ;
            
            if (p.compareId(startId) <= 0) 
                range.add(p) ;
        }
        if ( Paragraphs_Debug  )
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
	    
	    if ( Paragraphs_Debug  )
	    	{
        	System.out.println("Extracting paragraph range from offsets (" + 
                String.valueOf(startOffset) + "," + String.valueOf(endOffset) + ")") ;
            }
        
        Iterator i = iterator() ;
        Vector range = new Vector() ;
        Paragraph lastPar = null ;
        
        while (i.hasNext()) { 
            Paragraph p = (Paragraph) i.next() ;
            if ( Paragraphs_Debug  )
            	System.out.print("Checking par " + p.toString() + " (" + String.valueOf(p.getOffset()) + ")") ;
            int currOffset = p.getOffset() ;
            
            if (currOffset > startOffset && lastPar != null)
            	{
	            if ( Paragraphs_Debug  )
                	System.out.println(" - previous par extracted") ;
                range.add(lastPar) ;
                lastPar = null ;
            	}
            
            if (currOffset >= startOffset && currOffset <= endOffset)
             	{
	             if ( Paragraphs_Debug  )
                	System.out.println(" - extracted") ;
                range.add(p) ;
            	}
            
            if (currOffset < startOffset) 
            	{
                lastPar = p ;
                //System.out.println(" - maybe extracted later") ;
            	}
            
            if (currOffset == startOffset) 
            	{
                lastPar = null ;
                //System.out.println(" - last par will not be extracted") ;
            	}
            
            if (currOffset > endOffset) 
            	{
                //System.out.println(" - not extracted") ;
                break ;
                //return range ;
            	}
        }
        
        if (lastPar != null)
            range.add(lastPar) ;
            
        if ( Paragraphs_Debug  )            
            {
            System.out.print("Extracted: ") ;
       		i = range.iterator() ;
        		while (i.hasNext())
            	System.out.print(i.next().toString() + " " ) ;
            	        
        	System.out.println(" ") ;        	
    		}
        return range ;
    }//end getRangeFromOffsets()
    
    public Paragraph addParagraph(int index, long id, int offset) {

	Paragraph p = new Paragraph(id, offset, document) ;

	String StringParId = Paragraph.getIdAsString(id);

	if ( Paragraphs_Debug  )
		System.out.print("\n\nParagraphs-->addParagraph : id = "+ StringParId  +"\nINDEX : " + index + " @ OFFSET : "+  offset ) ;

        add(index, p) ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) 
            ((ParagraphPanel)i.next()).addParagraphBox(p, index) ;
        
        return p ;
        
    }//end addParagraph()
    
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
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) 
            ((ParagraphPanel)i.next()).removeParagraphBox(index) ;
    }
    
    public void removeParagraph(Paragraph p) {
        int index = indexOf(p) ;
        remove(p) ;
        
        Iterator i = paragraphPanels.iterator() ;
        while (i.hasNext()) 
            ((ParagraphPanel)i.next()).removeParagraphBox(index) ;
    }
    
    public int getParIndexFromID(long parId) {
        
        int index = 0 ;
        while (index < size()) {
            Paragraph p = (Paragraph)elementAt(index) ;
            int result = p.compareId(parId) ;
            
            if (result == 0) return index ;
            
            if (result < 0) {
                System.out.println("Paragraph did not exist") ;
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
        while (index < size()) {
            Paragraph p = (Paragraph)elementAt(index) ;
            
            if (p.getOffset() > offset)
                return index - 1 ;
            index ++ ;
        }
        return index - 1 ;
    }
    
    public int compareIds(long id1, long id2) {
        int result = compare(id1, id2) ;
         if ( Paragraphs_Debug  )
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

	public String toString()

	{
	System.out.println("\nPARAGRAPHS CONTAINS :\n") ;
	
	Iterator i = iterator();

	int startOf = 0;

	int Of = 0;

	String ParContent ="";

	try{
	String DocumentContent = document.getText(0,document.getLength() );

	while( i.hasNext() )

		{
		Paragraph p = (Paragraph)i.next();

		Of = p.getOffset();

		ParContent = DocumentContent.substring( startOf, Of) ;
		
		if (Of != 0)
		System.out.println("\n\n***"+ParContent +"***\n\n" );

		System.out.println(p.toString2()) ;

		startOf = Of;
	
		}

	ParContent = DocumentContent.substring(Of) ;

	System.out.println("\n\n***"+ParContent +"***\n\n" );

	}//endtry

	catch (Exception ble) {}

	

	return "blablah";

	}

	/*public static boolean IsFirstParagraph (String stringId)

	{
	if ( stringId.equals(smalestParagraphId ) )
		return true;

	return false;    
	}
	
	public static boolean isLastParagraph (String stringId)

	{
	if ( stringId.equals(largestParagraphId ) )
		return true;

	return false;    
	}
	
	*/

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

	public int getParagraphLength(int parIndex)

	{

	Paragraph thisPar = this.getParFromIndex( parIndex);

	Paragraph nextPar = this.getParFromIndex( parIndex+1);

	int thisParStart = thisPar.getOffset();
	
	int  nextParStart = nextPar.getOffset();

	return (nextParStart - thisParStart-1) ;



	}//end getParagraphLength()



}















