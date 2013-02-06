package cowrat.recoled.client;

import javax.swing.text.* ;
import java.awt.Color;
import java.util.* ;

public class Highlights {
    private int myClientId ;
    private int nextHighlightId ;
    
    private EditorClient parent ;
    private LockManager lockManager ;
    private DefaultHighlighter highlighter ;
    private Document document ;
    private Vector highlights ;
    private Vector highlightTypes ;
    
    public HighlightType addHighlightType(String name, Color color) {
        
        HighlightType t = new HighlightType(nextHighlightId, name, color) ;
        nextHighlightId ++ ;
        highlightTypes.add(t) ;
        return t ;
    }
    
    public HighlightType addHighlightType(int id, String name, Color color) {
        
        Iterator i = highlightTypes.iterator() ;
        while(i.hasNext()) {
            HighlightType t = (HighlightType) i.next() ;
            if (t.getId() == id)
                return t ;
        }
        
        HighlightType t = new HighlightType(id, name, color) ;
        nextHighlightId = id + 1 ;
        highlightTypes.add(t) ;
        return t ;
    }
    
    public Vector getHighlights(int startPos, int endPos) {
        
        Vector v = new Vector() ;
        
        Iterator i = ((Vector)highlights.clone()).iterator() ;
        while (i.hasNext()) {
            Highlight h = (Highlight) i.next() ;
            
            if (!(h.getEnd() <= startPos || h.getStart() >= endPos))
                v.add(h) ;
        }
        
        return v ;
    }
    
    public Vector getUsedColors() {
        
        Vector v = new Vector() ;
        
        Iterator i = ((Vector)highlightTypes.clone()).iterator() ;
        while (i.hasNext()) {
            HighlightType t = (HighlightType) i.next() ;
            v.add(t.getColor()) ;
        }
        
        return v ;
    }
    
    public void setMyClientId(int cid) {
        myClientId = cid ;
    }
    
    public void setHighlighter(DefaultHighlighter h) {
        highlighter = h ;
        //highlighter.setDrawsLayeredHighlights( false ); 
    }
    
    public Vector getHighlightTypes() {
        return highlightTypes ;
    }
    
    public HighlightType getHighlightType(int id) {
        Iterator i = highlightTypes.iterator() ;
        while (i.hasNext()) {
            HighlightType t = (HighlightType) i.next() ;
            if(t.getId() == id)
                return t ;
        }
        
        return null ;
    }
    
    public void addHighlight(int id, int type, long sPar, int sOffset, long ePar, int eOffset) {
        
        Iterator i = highlights.iterator() ;
        while(i.hasNext()) {
            Highlight h = (Highlight) i.next() ;
            if (h.getId() == id)
                return ;
        }
        
        
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
        for (int index = 0 ; index < highlights.size() ; index ++){
            Highlight h = (Highlight) highlights.elementAt(index) ;
            if (h.getId() == id) {   
                h.clear() ;
                highlights.remove(h) ;
                break ;
            }
        }
    }
    
    public void editHighlight(int id, int type, long sPar, int sOffset, long ePar, int eOffset) {
        int start = lockManager.getParFromId(sPar).getOffset() + sOffset ;
        int end = lockManager.getParFromId(ePar).getOffset() + eOffset ;
        
        for (int index = 0 ; index < highlights.size() ; index ++){
            Highlight h = (Highlight) highlights.elementAt(index) ;
            if (h.getId() == id) {   
                h.setStart(start, false) ;
                h.setEnd(end, false) ;
                
                break ;
            }
        }
    }
    
    public void unhighlight(int start, int end) throws Exception {
        
        int index = 0 ;
        int s, e ;
        
        while (index < highlights.size()) {
            Highlight h = (Highlight) highlights.elementAt(index) ;
            
            s = h.getStart() ;
            e = h.getEnd() ;
            
            if (e < start) {
                // completely before new section
                System.out.println(h.toString() + " is completely before " + 
                        String.valueOf(start) + " " + String.valueOf(end)) ;
                
                // just skip along to next one
            }
            else if (s < start && e <= end) {
                // partly before new section
                System.out.println(h.toString() + " is partly before " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                h.setEnd(start, true) ;
            }
            else if (s < start && e > end) {
                // encloses new section
                System.out.println(h.toString() + " completely encloses " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                deleteHighlight(h) ;
                break ;
            }
            else if (s >= start && e <= end) {
                // enclosed by new section
                System.out.println(h.toString() + " is completely enclosed by " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                deleteHighlight(h) ;
                index = index -1 ;
            }
            else if (s >= start && s < end && e > end) {
                // partly after new section
                System.out.println(h.toString() + " is partly after " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                h.setStart(end, true) ;
                break ;
            }
            else {
                // completely after new section
                System.out.println(h.toString() + " is completely after " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                break ;
            }
            index ++ ;
        }
    }
    
    public void addHighlight(int start, int end, HighlightType t) throws Exception {
        int index = 0 ;
        //int start = pStart.getOffset() ;
        //int end = pEnd.getOffset() ;
        
        int s, e ;
        
        while (index < highlights.size()) {
            Highlight h = (Highlight) highlights.elementAt(index) ;
            
            s = h.getStart() ;
            e = h.getEnd() ;
            
            if (e < start) {
                // completely before new section
                System.out.println(h.toString() + " is completely before " + 
                        String.valueOf(start) + " " + String.valueOf(end)) ;
                
                // just skip along to next one
            }
            else if (s < start && e <= end) {
                // partly before new section
                System.out.println(h.toString() + " is partly before " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                if (t == h.getType()) {
                    deleteHighlight(h) ;
                    index = index - 1 ;
                    start = s ;
                }
                else {
                    h.setEnd(start, true) ;
                }
            }
            else if (s < start && e > end) {
                // encloses new section
                System.out.println(h.toString() + " completely encloses " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                if (t == h.getType()) {
                    return ;
                }
                else {
                    addHighlight(end, e, index + 1, h.getType()) ;
                    h.setEnd(start, true) ;
                    index = index + 1 ;
                    break ;
                }
            }
            else if (s >= start && e <= end) {
                // enclosed by new section
                System.out.println(h.toString() + " is completely enclosed by " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                deleteHighlight(h) ;
                index = index -1 ;
            }
            else if (s >= start && s < end && e > end) {
                // partly after new section
                System.out.println(h.toString() + " is partly after " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                if(t == h.getType()) {
                    deleteHighlight(h) ;
                    end = e ;
                }
                else {
                    h.setStart(end, true) ;
                }
                break ;
            }
            else {
                // completely after new section
                System.out.println(h.toString() + " is completely after " 
                        + String.valueOf(start) + " " + String.valueOf(end)) ;
                
                break ;
            }
            index ++ ;
        }
        addHighlight(start, end, index, t) ;
    }
    
    public void addHighlight(int start, int end, int index, HighlightType t) throws Exception{
        
        Highlight h = new Highlight((nextHighlightId*100) + myClientId, start, end, t) ;
        highlights.add(index, h) ;
        nextHighlightId ++ ;
        
        Paragraph sPar = lockManager.getParFromOffset(start) ;
        Paragraph ePar = lockManager.getParFromOffset(end) ;
        
        parent.sendHighlightAdded(h.getId(), h.getType().getId(), sPar.getID(), start - sPar.getOffset(), ePar.getID(), end - ePar.getOffset()) ;
    }
    
    public void deleteHighlight(Highlight h) {
        highlights.remove(h) ;
        h.clear() ;
        
        parent.sendHighlightDeleted(h.getId()) ;
    }
    
    public Highlights(EditorClient p, LockManager l, Document d) {
        parent = p ;
        lockManager = l ;
        document = d ;
        highlights = new Vector() ;
        
        highlightTypes = new Vector() ;
    }
    
    public class Highlight {
        
        private int id ;
        private Position start ;
        private Position end ;
        private HighlightType type ;
        private Object tag ;
        
        public Highlight(int id, int s, int e, HighlightType t) throws Exception {
            System.out.println("Highlight Added " + String.valueOf(id)) ;
            this.id = id ;
            start = document.createPosition(s) ;
            end = document.createPosition(e) ;
            type = t ;
            
            try {
                tag = highlighter.addHighlight(s, e, new DefaultHighlighter.DefaultHighlightPainter(t.getColor())) ;
            } catch (Exception x) {
                System.out.println("Couldnt add highlight") ;
                x.printStackTrace() ;
            }
        }
        
        public int getId() {
            return id ;
        }
        
        public int getStart() {
            return start.getOffset() ;
        }
        
        public int getEnd() {
            return end.getOffset() ;
        }
        
        public HighlightType getType() {
            return type ;
        }
        
        public void setStart(int s, boolean broadcast) {
            try {
                start = document.createPosition(s) ; 
                highlighter.changeHighlight(tag, getStart(), getEnd()) ;
                
                if (broadcast) {
                    Paragraph sPar = lockManager.getParFromOffset(getStart()) ;
                    Paragraph ePar = lockManager.getParFromOffset(getEnd()) ;
                
                    parent.sendHighlightEdited(id, type.getId(), sPar.getID(), getStart() - sPar.getOffset(), ePar.getID(), getEnd() - ePar.getOffset()) ;
                }
            } catch (Exception e) {
                e.printStackTrace() ; 
            }
        }
        
        public void setEnd(int e, boolean broadcast) {
            try {
                end = document.createPosition(e) ;
                highlighter.changeHighlight(tag, getStart(), getEnd()) ;
                
                if (broadcast) {
                    Paragraph sPar = lockManager.getParFromOffset(getStart()) ;
                    Paragraph ePar = lockManager.getParFromOffset(getEnd()) ;
                
                    parent.sendHighlightEdited(id, type.getId(), sPar.getID(), getStart() - sPar.getOffset(), ePar.getID(), getEnd() - ePar.getOffset()) ;
                }
            } catch (Exception x) {
                x.printStackTrace() ; 
            }
        }
        
        public void clear() {
            try {
                highlighter.removeHighlight(tag) ;
            } catch (Exception e) {
                e.printStackTrace() ; 
            }
        }
    }
    
    
    public class HighlightType {
        private int id ;
        private String name ;
        private Color color ;
        
        public int getId() {return id ; }
        public String getName() { return name ; }
        public Color getColor() { return color ; }
                
        public HighlightType(int id, String n, Color c) {
            this.id = id ;
            name = n ;
            color = c ;
        }
        
        public String toString() {
            return name ;
        }
    }
}



