
package cowrat.recoled.server;

import java.io.* ;
import java.text.* ;
import java.util.* ;
import java.awt.* ;

/**
 * @author David King
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EditorAction implements Serializable, Comparable{

    private int idNumber ;
	private int clientId ;
	private int type ;
	//private long startPar, endPar ;
	private TreeSet paragraphIds ;
	private long startTime, endTime ;
	
	public static final int TEXT_INSERT = 0 ;
	public static final int TEXT_DELETE = 1 ;
	public static final int GESTURE = 2 ;
	public static final int KEYWORD_ADD = 3 ;
	public static final int KEYWORD_REMOVE = 4 ;
	public static final int TEXT_PASTE = 5 ;
	public static final int TEXT_CUT = 6 ;
	public static final int PARAGRAPH_MERGE = 7 ;
    public static final int NEWLINE_INSERT = 8;
	public void setIdNumber(int id) {
	    idNumber = id ;
	}
	
	public int getIdNumber() {
	    return idNumber ;
	}
	
	public String getActionXML(long startOfDocTime) {
	    return getXMLHead(startOfDocTime,"placebo") + ">\n" + getXMLTail() ;
	}
	
	public String getParagraphs() {
	    String s = " paragraphs=\"" ;
	    Iterator i = paragraphIds.iterator() ;
	    while (i.hasNext()) {
	        Long parId = (Long) i.next() ;
	        s = s + Paragraph.getIdAsString(parId.longValue()) ;
	        if(i.hasNext())
	            s = s + "," ;
	    }
	    s = s + "\"" ;
	    return s ;
	}
	
	public Object[] getTableRow() {
		
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss") ;
		
		String s = df.format(new Date(startTime)) ;
		String e = df.format(new Date(endTime)) ;
		
		String row[] =  { s, e, getTypeAsString() } ;
		return row ;
	}
	
	public String getTypeAsString() {
	    switch(type) {
	    	case TEXT_INSERT: return "Insert" ;
	    	case TEXT_DELETE: return "Delete" ;
	    	case GESTURE: return "Gesture" ;
			case TEXT_PASTE: return "Paste" ;
			case TEXT_CUT: return "Cut" ;
			case PARAGRAPH_MERGE : return "Paragraph_Merge";
			case NEWLINE_INSERT : return  "NewLine_Insert";
	    }	
	    return "unknown" ;
	}
	
	public String getTimeStampXML(long startOfDocTime) {
	    String s = "" ;
	    
	    s = s + "   <timestamp" ;
	    s = s + " actionid=\"" + idNumber + "\""; 
	    s = s + " agent=\"" + clientId + "\"" ;
	    s = s + " action=\"" + getTypeAsString() + "\"" ;
	    s = s + " start=\"" + (startTime - startOfDocTime)/1000 + "\"" ;
	    s = s + " end=\"" + (endTime - startOfDocTime)/1000 + "\"" ;
	    s = s + " />\n" ;
	    return s ;
	}
	
	public String getXMLHead(long startOfDocTime, String type) {
	    String s = "    <action" ;
	    s = s + " id=\"" + String.valueOf(idNumber) + "\"" ;
	    s = s + " type=\"" + type +  "\"" ;
	    s = s + " startT=\"" + (startTime - startOfDocTime)/1000 + "\"" ;
	    s = s + " endT=\"" + (endTime - startOfDocTime)/1000 + "\"" ;
	    s = s + getParagraphs() ;
	    return s ;	    
	}
	
	public String getXMLTail() {
	    return "    </action>\n" ;
	}
	
	public int compareTo(Object o) throws ClassCastException{
	    EditorAction ea = (EditorAction) o ;
	    
	    if(ea.getStartTime() < startTime)
	        return 1 ;
	    
	    if(ea.getStartTime() > startTime)
	        return -1 ;
	    
	    if(ea.getEndTime() < endTime)
	        return 1 ;
	    
	    if(ea.getEndTime() > endTime)
	    	return -1 ;
	    
	    return 0 ;
	}
	
	/**
	 * @return Returns the client.
	 */
	public int getClientId() {
		return clientId;
	}
	/**
	 * @param client The client to set.
	 */
	public void setClientId(int id) {
		clientId = id;
	}
	/**
	 * @return Returns the endTime.
	 */
	public long getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime The endTime to set.
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return Returns the startTime.
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime The startTime to set.
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	
	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		
	String temp="type :"+ getTypeAsString() + " startTime :"+ startTime + " endTime :"+ endTime ;
	
	return temp;
	}
	
	public static class TextInsertAction extends EditorAction {
		private long startOffset ;
		private String text ;
		
		public String getActionXML(long startOfDocTime) {
		    String s = getXMLHead(startOfDocTime,"Insert") ;
		    s = s + " startOffset=\"" + startOffset + "\">\n" ;
		    s = s + "      " + SavedDocument.replaceReservedCharacters(text) + "\n" ;
		    s = s + getXMLTail() ;
		    return s ;
		}
		
		public TextInsertAction(int timestampId,int cId, long sTime, Vector pars, long sOffset, String t) {
			
			setIdNumber( timestampId);
			
			if ( t.equals("\n") )
				{ setType(NEWLINE_INSERT) ; }
			else
				{ setType(TEXT_INSERT) ; }
			setClientId(cId) ;
			setStartTime(sTime) ;
			setEndTime(sTime) ;
			
			setParagraphIds(pars) ;
			startOffset = sOffset ;
			text = t;
		}
		
		public boolean appendAction(long time, Vector pars, int sOffset, String t) {
		    long sPar = ((Long)pars.firstElement()).longValue() ;
		    
			if (time > getEndTime() + 10000)
				return false ;
			
			if (sPar != getStartPar())
				return false ;
			
			if (sOffset != startOffset + text.length())
				return false ;
			
			Iterator i = pars.iterator() ;
			while (i.hasNext()) {
			    long parId = ((Long)i.next()).longValue() ;
			
			    if (!effectedParagraph(parId)) 
			        addParagraphId(parId) ;
			}
			setEndTime(time) ;		
			text = text + t ;
			
			return true ;	
		}
		
		/**
		 * @return Returns the startOffset.
		 */
		public long getStartOffset() {
			return startOffset;
		}
		/**
		 * @return Returns the text.
		 */
		public String getText() {
			return text;
		}
	}

//

	public static class TextPasteAction extends EditorAction {
		private long startOffset ;
		private String text ;
		
		public String getActionXML(long startOfDocTime) {
		    String s = getXMLHead(startOfDocTime,"Paste") ;
		    s = s + " startOffset=\"" + startOffset + "\">\n" ;
		    s = s + "      " + SavedDocument.replaceReservedCharacters(text) + "\n" ;
		    s = s + getXMLTail() ;
		    return s ;
		}
		
		public TextPasteAction ( int timestampId, int cId, long sTime, Vector pars, long sOffset, String t) {
						
			setIdNumber( timestampId);			
			setType(TEXT_PASTE) ;
			setClientId(cId) ;
			setStartTime(sTime) ;
			setEndTime(sTime) ;
			
			setParagraphIds(pars) ;
			startOffset = sOffset ;
			text = t;
		}
		
		public boolean appendAction(long time, Vector pars, int sOffset, String t) {
		    long sPar = ((Long)pars.firstElement()).longValue() ;
		    
			if (time > getEndTime() + 10000)
				return false ;
			
			if (sPar != getStartPar())
				return false ;
			
			if (sOffset != startOffset + text.length())
				return false ;
			
			Iterator i = pars.iterator() ;
			while (i.hasNext()) {
			    long parId = ((Long)i.next()).longValue() ;
			
			    if (!effectedParagraph(parId)) 
			        addParagraphId(parId) ;
			}
			setEndTime(time) ;		
			text = text + t ;
			
			return true ;	
		}
		
		/**
		 * @return Returns the startOffset.
		 */
		public long getStartOffset() {
			return startOffset;
		}
		/**
		 * @return Returns the text.
		 */
		public String getText() {
			return text;
		}
	}//endof class TextPasteAction 
//
	
	public static class TextDeleteAction extends EditorAction {
		
		private int startOffset, endOffset ;
		private String text ;
			
		public String getActionXML(long startOfDocTime) {
		    String s = getXMLHead(startOfDocTime,"Delete") ;
		    s = s + " startOffset=\"" + startOffset + "\"" ;
		    s = s + " endOffset=\"" + endOffset + "\">\n" ;
		    s = s + "      " + SavedDocument.replaceReservedCharacters(text) + "\n" ;
		    s = s + getXMLTail() ;
		    return s ;
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
		public TextDeleteAction(int timestampId,int cId, long time, Vector pars, int sOffset, int eOffset, String t) {
			
			setIdNumber( timestampId);
			if ( t.equals("\n") )
				{ setType(PARAGRAPH_MERGE) ; }
			else
				{ setType(TEXT_DELETE) ; }
			setClientId(cId) ;
			setStartTime(time) ;
			setEndTime(time) ;
			setParagraphIds(pars) ;
			
			startOffset = sOffset ;
			endOffset = eOffset ;
			text = t ;
		}
		
		public boolean appendAction(long time, Vector pars, int sOffset, int eOffset, String t) {
		    
		    long sPar = ((Long) pars.firstElement()).longValue() ;
		    long ePar = ((Long) pars.lastElement()).longValue() ;
		    
		    System.out.println("AppendDelete: sPar=" + getStartPar() + ", " + sPar) ;
		    System.out.println("              sOff=" + startOffset + ", " + sOffset) ;
		    System.out.println("              ePar=" + getEndPar() + ", " + ePar) ;
		    System.out.println("              eOff=" + endOffset + ", " + eOffset) ;
		    
			if (time > getStartTime() + 10000)
				return false ;
				
			if(sPar == getStartPar() && sOffset == startOffset) {
				// action belongs at end of prev action (deleting forwards)
				setEndTime(time) ;
				startOffset = sOffset ;
				endOffset = eOffset ;
				text = text + t ;
				Iterator i = pars.iterator() ;
				while (i.hasNext()) {
				    long parId = ((Long)i.next()).longValue() ;
				
				    if (!effectedParagraph(parId)) 
				        addParagraphId(parId) ;
				}
				return true ;				
			}
			
			if(ePar == getStartPar() && eOffset == startOffset) {
				// action belongs at beggining of prev action (deleting backwards) 
				setEndTime(time) ;
				startOffset = sOffset ;
				endOffset = eOffset ;
				text = t + text ;
				Iterator i = pars.iterator() ;
				while (i.hasNext()) {
				    long parId = ((Long)i.next()).longValue() ;
				
				    if (!effectedParagraph(parId)) 
				        addParagraphId(parId) ;
				}
				return true ;	
			}
			return false ;
		}
	}
	
//New... 
public static class TextCutAction extends EditorAction {
			
		private int startOffset, endOffset ;
		private String text ;
			
		public String getActionXML(long startOfDocTime) {
		    String s = getXMLHead(startOfDocTime,"Cut") ;
		    s = s + " startOffset=\"" + startOffset + "\"" ;
		    s = s + " endOffset=\"" + endOffset + "\">\n" ;
		    s = s + "      " + SavedDocument.replaceReservedCharacters(text) + "\n" ;
		    s = s + getXMLTail() ;
		    return s ;
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
		public TextCutAction ( int timestampId, int cId, long time, Vector pars, int sOffset, int eOffset, String t) {
						
			setIdNumber( timestampId);			
			setType(TEXT_CUT) ;
			setClientId(cId) ;
			setStartTime(time) ;
			setEndTime(time) ;
			setParagraphIds(pars) ;
			
			startOffset = sOffset ;
			endOffset = eOffset ;
			text = t ;
		}
		
		public boolean appendAction(long time, Vector pars, int sOffset, int eOffset, String t) {
		    
		    long sPar = ((Long) pars.firstElement()).longValue() ;
		    long ePar = ((Long) pars.lastElement()).longValue() ;
		    
		    System.out.println("AppendDelete: sPar=" + getStartPar() + ", " + sPar) ;
		    System.out.println("              sOff=" + startOffset + ", " + sOffset) ;
		    System.out.println("              ePar=" + getEndPar() + ", " + ePar) ;
		    System.out.println("              eOff=" + endOffset + ", " + eOffset) ;
		    
			if (time > getStartTime() + 10000)
				return false ;
				
			if(sPar == getStartPar() && sOffset == startOffset) {
				// action belongs at end of prev action (deleting forwards)
				setEndTime(time) ;
				startOffset = sOffset ;
				endOffset = eOffset ;
				text = text + t ;
				Iterator i = pars.iterator() ;
				while (i.hasNext()) {
				    long parId = ((Long)i.next()).longValue() ;
				
				    if (!effectedParagraph(parId)) 
				        addParagraphId(parId) ;
				}
				return true ;				
			}
			
			if(ePar == getStartPar() && eOffset == startOffset) {
				// action belongs at beggining of prev action (deleting backwards) 
				setEndTime(time) ;
				startOffset = sOffset ;
				endOffset = eOffset ;
				text = t + text ;
				Iterator i = pars.iterator() ;
				while (i.hasNext()) {
				    long parId = ((Long)i.next()).longValue() ;
				
				    if (!effectedParagraph(parId)) 
				        addParagraphId(parId) ;
				}
				return true ;	
			}
			return false ;
		}
	}
//	
	public static class GestureAction extends EditorAction {
		int id ;
		long startPar ;
		Vector points ;
		int minX, minY, maxX, maxY ;
		String text ;
		
		public void setText(String text) {
		    this.text = text ;
		}
				
		public String getActionXML(long startOfDocTime) {
		    String s = getXMLHead(startOfDocTime,"Gesture") ;
		    s = s + " startPar=\"" + Paragraph.getIdAsString(startPar) + "\"" ;  
		    s = s + " points=\"" ;
		    
		    Iterator i = points.iterator() ;
		    int count = 1 ;
		    while (i.hasNext()) {
		        Point p = (Point) i.next() ;
		        s = s + "(" + p.x + "," + p.y + ")" ;
		        if (!(count == points.size()))
		            s = s + "," ;
		        else
		            s = s + "\">\n" ;
		        count ++ ;
		    }
		    s = s + "      " + SavedDocument.replaceReservedCharacters(text) + "\n" ;
		    s = s + getXMLTail() ;
		    return s ;
		}
		
		public long getStartPar() {
		    return startPar ;
		}

		public GestureAction(int timestampId,int cId, long time, int id, long par, int x, int y) {
					
			setIdNumber( timestampId);
			setType(GESTURE) ;
			setClientId(cId) ;
			setStartTime(time) ;
			setEndTime(time) ;
			startPar = par ;
			
			this.id = id ;
			points = new Vector() ;
			points.add(new Point(x, y));
			minX = x ;
			minY = y ;
			maxX = x ;
			maxY = y ;
			
			Vector v = new Vector() ;
			v.add(new Long(par)) ;
			setParagraphIds(v) ;
		}
		
		public boolean appendAction(long time, int id, long par, int x, int y) {
			if (id != this.id)
				return false ;
			
			if(!effectedParagraph(par))
			    addParagraphId(par) ;
			
			points.add(new Point(x, y)) ;
			if (x < minX) minX = x ;
			if (x > maxX) maxX = x ;
			if (y < minY) minY = y ;
			if (y > maxY) maxY = y ;			
			
			setEndTime(time) ;
			return true ;
		}
		/**
		 * @return Returns the id.
		 */
		public int getId() {
			return id;
		}
        /**
         * @return Returns the maxX.
         */
        public int getMaxX() {
            return maxX;
        }
        /**
         * @return Returns the maxY.
         */
        public int getMaxY() {
            return maxY;
        }
        /**
         * @return Returns the minX.
         */
        public int getMinX() {
            return minX;
        }
        /**
         * @return Returns the minY.
         */
        public int getMinY() {
            return minY;
        }
	}
	
	public void addParagraphId(long parId) {
	    paragraphIds.add(new Long(parId)) ;
	}
	
	public long getStartPar() {
	    return ((Long)paragraphIds.first()).longValue() ;
	}
	
	public long getEndPar() {
	    return ((Long)paragraphIds.last()).longValue() ;
	}

	public void setParagraphIds(Vector v) {
	    paragraphIds = new TreeSet() ;
	    paragraphIds.addAll(v) ;
	}
	
	public boolean effectedParagraph(long parId) {
	    return paragraphIds.contains(new Long(parId)) ;
	}
}
