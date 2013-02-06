package cowrat.recoled.server;

import java.util.* ;
import java.awt.* ;
import java.io.* ;
import cowrat.recoled.server.SavedDocument.* ;

import javax.swing.text.* ;


/**
 * @author dnk2
 *
 */
public class Highlights {

    private Vector highlights ;
    private Vector highlightTypes;
    private LockManager lockManager ;
    private EditorDocument document ;
    
    public Vector getHighlightTypes() {
        return highlightTypes ;
    }
    
    public Vector getHighlights() {
        return highlights ;
    }
    
    
    
    /**
     * 
     */
    public Highlights(LockManager l, EditorDocument d) {
        lockManager = l ;
        document = d ;
        
        highlights = new Vector() ;
        highlightTypes = new Vector() ;
        highlightTypes.add(new HighlightType(1, "Task", new Color(240,30,35, 100))) ;
    	highlightTypes.add(new HighlightType(2, "Person", new Color(140,200,60, 100))) ;
    	highlightTypes.add(new HighlightType(3, "Place", new Color(0,85,165, 100))) ;
        // TODO Auto-generated constructor stub
    }
    
    public Highlights(LockManager l, EditorDocument d, Vector sHighlightTypes, Vector sHighlights) {
    	lockManager = l ;
        document = d ;
        
        highlights = new Vector() ;
        highlightTypes = sHighlightTypes ;
        
        highlights = new Vector() ;
        
        
        Iterator i = sHighlights.iterator() ;
        while (i.hasNext()) {
        		SavedHighlight sh = (SavedHighlight) i.next() ;
        		try {
        			Highlight h = new Highlight(sh.getId(), sh.getStartOffset(), sh.getEndOffset(), getHighlightType(sh.getType())) ;
        			highlights.add(h) ;
        		} catch (Exception e) {
        			System.out.println("Highlights: constructor. error") ;
        			e.printStackTrace() ;
        		}
        }
    	
    	
    }
    
    public void addHighlightType(int id, String name, Color color){
        HighlightType t = new HighlightType(id, name, color) ;
        highlightTypes.add(t) ;
    }
    
    public HighlightType getHighlightType(int id) {
        System.out.println("Highlights: getHighlightType. " + String.valueOf(id)) ;
        
        Iterator i = highlightTypes.iterator() ;
        while (i.hasNext()) {
            HighlightType t = (HighlightType) i.next() ;
            if(t.getId() == id)
                return t ;
        }
        
        return null ;
    }
    
    public void addHighlight(int id, int type, long sPar, int sOffset, long ePar, int eOffset) {
        int start = lockManager.getParFromId(sPar).getOffset() + sOffset ;
        int end = lockManager.getParFromId(ePar).getOffset() + eOffset ;
        
        int index ;
        for (index = 0 ; index < highlights.size() ; index ++){
            Highlight h = (Highlight) highlights.elementAt(index) ;
            if (h.getStart() < start)
                break ;
        }
        
        HighlightType hType = getHighlightType(type) ;
        
        try {
            Highlight h = new Highlight(id, start, end, hType) ;
            highlights.add(index, h) ;
        } catch (Exception e) {
            System.out.println("Highlights: addHighlight. error") ;
            e.printStackTrace() ;
        }
    }
    
    public void deleteHighlight(int id) {
        Iterator i = highlights.iterator() ;
        
        while (i.hasNext()) {
            Highlight h = (Highlight) i.next() ;
        	if (h.getId() == id) {
        	    i.remove() ;
        	    return ;
        	}
        }
    }
    
    public void editHighlight(int id, long sPar, int sOffset, long ePar, int eOffset) {
        int start = lockManager.getParFromId(sPar).getOffset() + sOffset ;
        int end = lockManager.getParFromId(ePar).getOffset() + eOffset ;
        
        Iterator i = highlights.iterator() ;
        while (i.hasNext()) {
            Highlight h = (Highlight) i.next() ;
        	if (h.getId() == id) {
        	    h.setStart(start) ;
        	    h.setEnd(end) ;
        	    return ;
        	}
        }
    
	}

    public class Highlight {
        
        private int id ;
        private Position start ;
        private Position end ;
        private HighlightType type ;
        
        public Highlight(int id, int s, int e, HighlightType t) throws Exception {
            System.out.println("Highlight Added " + String.valueOf(id)) ;
            this.id = id ;
            start = document.createPosition(s) ;
            end = document.createPosition(e) ;
            type = t ;
        }
        
        public int getId() { return id ; }
        public int getStart() { return start.getOffset() ; }
        public int getEnd() { return end.getOffset() ; }
        public HighlightType getType() { return type ; }
        
        public void setStart(int s) {
            try {
                start = document.createPosition(s) ; 
            } catch (Exception e) {
                e.printStackTrace() ; 
            }
        }
        
        public void setEnd(int e) {
            try {
                end = document.createPosition(e) ;
            } catch (Exception x) {
                x.printStackTrace() ; 
            }
        }
        
    }
}
