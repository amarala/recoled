package cowrat.recoled.server;

import java.util.* ;
import java.io.* ;
import java.text.* ;


/**
 * @author David King
 *
 */
public class SavedDocument implements Serializable{
	
	
	private long docStartTime ;
	private String title ;
	private String description ;
	private String text ;
	private Vector paragraphsVector ;
	private Paragraphs paragraphs;
	private Vector clients ;
	private Vector keywords ;
	private Vector highlights ;
	private Vector highlightTypes ;
	private TreeSet actions ;
	
	public SavedDocument(EditorDocument doc, Paragraphs pars, Vector clients, Highlights h, EditorServer es) 
    throws Exception 
  {
		title = doc.getTitle() ;
		description = doc.getDescription() ; 
		text = doc.getText(0, doc.getLength()) ;
		docStartTime = doc.getStartTime();
		paragraphs = pars;
		paragraphsVector = new Vector() ;
    Iterator i = pars.iterator() ;
    Paragraph lastPar = null ;
    while (i.hasNext()) {
      Paragraph p = (Paragraph) i.next() ;
      if(lastPar != null){
        String parText = text.substring(lastPar.getOffset(), p.getOffset()) ;
        SavedParagraph sp = new SavedParagraph(lastPar.getID(), lastPar.getOffset(), parText) ;
        paragraphsVector.add(sp) ;
      }
      lastPar = p ;
    }
    if(lastPar != null) {
      String parText = text.substring(lastPar.getOffset()) ;
      SavedParagraph sp = new SavedParagraph(lastPar.getID(), lastPar.getOffset(), parText) ;
      paragraphsVector.add(sp) ;
    }
		
		highlightTypes = h.getHighlightTypes() ;
		highlights = new Vector() ;
		i = h.getHighlights().iterator() ;
		while (i.hasNext()) {
			Highlights.Highlight hl = (Highlights.Highlight)i.next() ;
			String hlText = doc.getText(hl.getStart(), hl.getEnd() - hl.getStart()) ;
			long par = pars.getParFromIndex(pars.getParIndexFromOffset(hl.getStart())).getID() ;
			SavedHighlight sh = new SavedHighlight(hl.getId(),
                                             hl.getType().getId(),
                                             hlText,
                                             hl.getStart(),
                                             hl.getEnd(), par) ;
			highlights.add(sh) ;
		}
		
		this.clients = clients ;
		
		actions = new TreeSet() ;
		i = clients.iterator() ;
		while(i.hasNext()) {
		    EditorClient c = (EditorClient) i.next() ;
		    actions.addAll(c.getActions(es)) ;
		}
		
		//mach : actionId are now created at timestamp generation
		/*
		int count = 0 ;
		i = actions.iterator() ;
		while (i.hasNext()) {
		    count ++ ;
		    EditorAction a = (EditorAction) i.next() ;
		    a.setIdNumber(count) ;
		}
		*/
	}//endConst

    public String getXML() {
        SimpleDateFormat d = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss") ;
        
        
	    String s = "<?xml version=\"1.0\"?>\n" ;
	    s = s + "<!DOCTYPE comapdoc SYSTEM \"comapdoc.dtd\">\n" ;
	    s = s + "<comapdoc>\n" ;
	    
	    s = s + "  <meeting date=\"" + d.format(new Date(docStartTime)) + "\">\n" ;
	    
	    s = s + "    <title>\n" ;
	    s = s + "      " + replaceReservedCharacters(title) + "\n" ;
	    s = s + "    </title>\n" ;
	    
	    s = s + "    <description>\n" ;
	    s = s + "      " + replaceReservedCharacters(description) + "\n" ;
	    s = s + "    </description>\n" ;
	    
	    s = s + "    <partlist>\n" ;
	    Iterator i = clients.iterator() ;
	    while (i.hasNext()) {
	        EditorClient c = (EditorClient) i.next() ;
	        s = s + c.getXML() ;
	    }
	    s = s + "    </partlist>\n" ;

	    s = s + "  </meeting>\n" ;
	    
	    boolean skipFirstParagraph = true;
	    
	    i = paragraphsVector.iterator() ;
	    
	     while (i.hasNext()) 
	    
	    {
		    
		    if (skipFirstParagraph)
		    	{
			    	skipFirstParagraph = false;
			    	SavedParagraph p = (SavedParagraph) i.next() ;
		    	}
		   else
		    	{		    
	        	SavedParagraph p = (SavedParagraph) i.next() ;	        	
	        	s = s + p.getXML() ;
        		}
	    }
	    
	    s = s + "  <actions>\n" ;
	    i = actions.iterator() ;
	    while (i.hasNext()) {
		    
	        EditorAction a = (EditorAction) i.next() ;
	        s = s + a.getActionXML(docStartTime ) ;
	    }
	    s = s + "  </actions>\n" ;
	    
	    s = s + "  <keywordTypes>\n" ;
	    i = highlightTypes.iterator() ;
	    while (i.hasNext()) {
	        HighlightType ht = (HighlightType) i.next() ;
	        s = s + ht.getXML() ;
	    }
	    s = s + "  </keywordTypes>\n" ;	    
	    s = s + "</comapdoc>\n" ;
	    return s ;
	}
    
    public long getStartTime() {
        return docStartTime ;
    }
    
	/**
	 * @return Returns the clients.
	 */
	public Vector getClients() {
		return clients;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return Returns the keywords.
	 */
	public Vector getKeywords() {
		return keywords;
	}
	/**
	 * @return Returns the paragraphsVector.
	 */
	public Vector getParagraphsVector() {
		return paragraphsVector;
	}
	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @return Returns the highlights.
	 */
	public Vector getHighlights() {
		return highlights;
	}
	/**
	 * @return Returns the highlightTypes.
	 */
	public Vector getHighlightTypes() {
		return highlightTypes;
	}
	
	
	
	public class SavedHighlight implements Serializable, Comparable {
	    
	    public int compareTo(Object o) {
	        SavedHighlight sh = (SavedHighlight) o ;
	        
	        if(startOffset < sh.getStartOffset())
	            return -1 ;
	        
	        if(startOffset > sh.getStartOffset())
	            return 1 ;
	        
	        if(endOffset < sh.getEndOffset())
	            return -1 ;
	        
	        if(endOffset > sh.getEndOffset())
	            return 1 ;
	        
	        return 0 ;
	    }
		/**
		 * @return Returns the endOffset.
		 */
		public int getEndOffset() {
			return endOffset;
		}
		/**
		 * @return Returns the startOffset.
		 */
		public int getStartOffset() {
			return startOffset;
		}
		/**
		 * @return Returns the text.
		 */
		public String getText() {
			return text;
		}
		/**
		 * @return Returns the type.
		 */
		public int getType() {
			return type;
		}
		
		public int getId() {
			return id ;
		}
		
		public long getPar() {
		    return par ;
		}
		
		private int startOffset, endOffset ;
		private String text ;
		private int type, id ;
		private long par ;
		
		public SavedHighlight(int id, int type, String text, int sOffset, int eOffset, long par) {
			this.id = id ;
			this.type = type ;
			this.text = text ;
			this.par = par ;
			startOffset = sOffset ;
			endOffset = eOffset ;
		}
		
		public String getStartXML(){
		    return "<keyword type=\"" + type + "\">" ;
		}
		
		public String getEndXML() {
		    return "</keyword>" ;
		}
	}
	
	public TreeSet getKeywordsForParagraph(long parId) {
	    TreeSet t = new TreeSet() ;
	    
	    Iterator i = highlights.iterator() ;
	    while (i.hasNext()) {
	        SavedHighlight sh = (SavedHighlight)i.next() ;
	        if(sh.getPar() == parId)
	            t.add(sh) ;
	    }
	    return t ;
	}
	
	public class SavedParagraph implements Serializable{
		long id ;
		int startOffset ;
		String text ;
		Vector actionsVector=null;
		
		//const
		
		public SavedParagraph(long id, int sOffset, String t) {
			this.id = id ;
			startOffset = sOffset ;
			text = t ;
			
			try{
			actionsVector = paragraphs.getTimestamps( new Long ( id ) );
		}
		
		catch (Exception e) {System.out.print("(((((SavedDoc: ERROR fetching timestamps...");}
		}
		
		/**
		 * @return Returns the id.
		 */
		public long getId() {
			return id;
		}
		/**
		 * @return Returns the startOffset.
		 */
		public int getStartOffset() {
			return startOffset;
		}
		
		public String getXML() {
            String s = "" ;
            s = s + "  <segment id=\"" + Paragraph.getIdAsString(id) + "\">\n" ;
            s = s + getActionXML() ;
            s = s + getTextXML() ;          
            s = s + "  </segment>\n" ;
            return s ;
        }
		
		public String getTextXML() {
		    if (text.length() == 0)
		        return "" ;
		    
		    String s = "    " ;
		    
		    System.out.println("\"" + text + "\"") ;
		    int offset = getStartOffset() ;
		    
		    TreeSet highlights = getKeywordsForParagraph(id) ;
            if(highlights.isEmpty())
                return s + replaceReservedCharacters(text) ;
		    
            Iterator i = highlights.iterator() ;
            while (i.hasNext()) {
                SavedHighlight sh = (SavedHighlight) i.next() ;
                
                // find text before highlight tag
                if (sh.getStartOffset() > offset)
                    s = s + replaceReservedCharacters(text.substring(offset-startOffset, 
                                                                     sh.getStartOffset()-startOffset)) ;
                
                offset = sh.getStartOffset() ;
                s = s + sh.getStartXML() ; 
                
                // find text within highlight tag ;
                if (sh.getEndOffset() > offset)
                    s = s + replaceReservedCharacters(text.substring(offset-startOffset, 
                                                                     sh.getEndOffset()-startOffset)) ;
                
                offset = sh.getEndOffset() ;
                s = s + sh.getEndXML() ;
            }
            
            if ((offset - startOffset) < text.length() && offset > 0)
                s = s + replaceReservedCharacters(text.substring(offset-startOffset)) ;
		    
		    return s ;
		}
        
        public String getActionXML() {	        
	     
            String s = "" ;
            
            //iterate to action id that affect THIS par   
                        
            try{ 
	            
	            Iterator timestampIterator = actionsVector.iterator();
            
                        
            while ( timestampIterator.hasNext() )
            
            	{   
	            	int actionId =  ( (Integer) timestampIterator.next() ).intValue();
	            	
            	//iterate through all actions and find match...
            		Iterator i = actions.iterator() ;
            		
            		while (i.hasNext()) 
            			{
                	EditorAction action = (EditorAction) i.next() ;
               		if ( ( action.getIdNumber() )== actionId ) 
                    	s = s + action.getTimeStampXML(docStartTime) ;                   
           				}
            
        		}
        		
    		}//endtry
    		
    		catch(Exception e){}
            
            
            return s ;
           
        } 
                
		
	}

	public static String replaceReservedCharacters(String string) {
	    String s = string.replaceAll("&", "&amp;") ;
	    s = s.replaceAll("'", "&apos;") ; 
	    s = s.replaceAll("\"", "&quot;") ;
	    s = s.replaceAll("<", "&lt;") ;
	    s = s.replaceAll(">", "&gt;") ;
	    return s ;
	}
}
