//Modified oct 04

package cowrat.recoled.shared;

import java.io.Serializable ;
import java.awt.Color ;

public class Message implements Serializable {
    
    public static final int TEXT_INSERT  = 1 ;
    public static final int TEXT_DELETE  = 2 ;
    public static final int LOCK_REQUEST = 3 ;
    public static final int LOCK_GRANT   = 4 ;
    public static final int LOCK_DENY    = 5 ;
    public static final int LOCK_RELEASE = 6 ;
    public static final int SCROLL_MOVE  = 7 ; 
    public static final int CLIENT_JOIN = 8 ;
    public static final int CLIENT_REJECT = 9 ;
    public static final int CLIENT_LEAVE = 10 ;
    public static final int GESTURE_LINE = 11 ;
    public static final int GESTURE_CIRCLE = 12 ;
    public static final int DOCUMENT_STATE = 13 ;
    public static final int HIGHLIGHT_ADD = 14 ;
    public static final int HIGHLIGHT_DELETE = 15 ;
    public static final int HIGHLIGHT_EDIT = 16 ;
    public static final int HIGHLIGHT_TYPE = 17 ;
    public static final int TEXT_CUT  = 18 ;
    public static final int TEXT_PASTE  = 19 ;
    
    private int type ;
    private int clientId ;
    
    public int getType() { return type ;}
    public void setType(int t) { type = t ;}
    public int getClientId() { return clientId ;}
    public void setClientId(int i) { clientId = i ;}
    
    public static class TextInsertMsg extends Message {
        private long par ;
        private int offset ;
        private String text ;
        
        public long getPar() { return par ; }
        public int getOffset() { return offset ; }
        public String getText() { return text ; } 
        
        public TextInsertMsg(int c, long p, int o, String s) {
            setType(TEXT_INSERT) ;
            setClientId(c) ;
            par = p ;
            offset = o ;
            text = s ;
        }
    }

 public static class TextPasteMsg extends Message {
        private long par ;
        private int offset ;
        private String text ;
        
        public long getPar() { return par ; }
        public int getOffset() { return offset ; }
        public String getText() { return text ; } 
        
        public TextPasteMsg(int c, long p, int o, String s) {
            setType(TEXT_PASTE) ;
            setClientId(c) ;
            par = p ;
            offset = o ;
            text = s ;
        }
    }
    
    public static class TextDeleteMsg extends Message {
        private long startPar, endPar ;
        private int startOffset, endOffset ;
        
        public long getStartPar() { return startPar ; }
        public long getEndPar() { return endPar ; } 
        public int getStartOffset() { return startOffset ; }
        public int getEndOffset() { return endOffset ; } 
        
        public TextDeleteMsg(int c, long sp, long ep, int so, int eo) {
            setType(TEXT_DELETE) ;
            setClientId(c) ;
            startPar = sp ;
            endPar = ep ;
            startOffset = so ;
            endOffset = eo ;
        }
    }

public static class TextCutMsg extends Message {
        private long startPar, endPar ;
        private int startOffset, endOffset ;
        
        public long getStartPar() { return startPar ; }
        public long getEndPar() { return endPar ; } 
        public int getStartOffset() { return startOffset ; }
        public int getEndOffset() { return endOffset ; } 
        
        public TextCutMsg(int c, long sp, long ep, int so, int eo) {
            setType(TEXT_CUT) ;
            setClientId(c) ;
            startPar = sp ;
            endPar = ep ;
            startOffset = so ;
            endOffset = eo ;
        }
    }
    
    public static class LockRequestMsg extends Message {
        private long startPar, endPar ;
        private int idNumber ;
        
        public long getStartPar() { return startPar ; } 
        public long getEndPar() { return endPar ; } 
        public int getIdNumber() { return idNumber ; }
        
        public LockRequestMsg(int c, long sp, long ep, int id) {
            setType(LOCK_REQUEST) ;
            setClientId(c) ;
            startPar = sp ;
            endPar = ep ;
            idNumber = id ;
        }
    }
    
    public static class LockGrantMsg extends Message{
        private long startPar, endPar ;
        private int idNumber ;
        
        public long getStartPar() { return startPar ; } 
        public long getEndPar() { return endPar ; } 
        public int getIdNumber() { return idNumber ; }
        
        public LockGrantMsg(int c, long sp, long ep, int id) {
            setType(LOCK_GRANT) ;
            setClientId(c) ;
            startPar = sp ;
            endPar = ep ;
            idNumber = id ;
        }
    }	
    
    public static class LockDenyMsg extends Message {
        private int idNumber ;
        
        public int getIdNumber() {return idNumber ; }
        
        public LockDenyMsg(int c, int id) {
            setType(LOCK_DENY) ;
            setClientId(c) ;
            idNumber = id ;
        }
    }
    
    public static class LockReleaseMsg extends Message {
        private long startPar, endPar ;
        
        public long getStartPar() { return startPar ; }
        public long getEndPar() { return endPar ; }
        
        public LockReleaseMsg(int c, long sp, long ep) {
            setType(LOCK_RELEASE) ;
            setClientId(c) ;
            startPar = sp ;
            endPar = ep ;
        }
    }
    
    public static class ScrollMoveMsg extends Message {
        private int value, view, max ;
        private boolean isPrimary ;
        
        public int getValue() { return value ; }
        public int getView() { return view ; }
        public int getMax() { return max ; }
        public boolean getIsPrimary() { return isPrimary ; }
        
        public ScrollMoveMsg(int c, int va, int vi, int m, boolean p) {
            setType(SCROLL_MOVE) ;
            setClientId(c) ;
            value = va ;
            view = vi ;
            max = m ;
            isPrimary = p ;
        }
    }
    
    public static class ClientJoinMsg extends Message {
        private String name ;
        private int colorCode ;
        private int keyValue ;
	private String ClientIPaddress;
        
        public String getName() { return name ; }
        public int getColorCode() { return colorCode ; }
        public int getKeyValue() { return keyValue ; }
	public String getClientIPaddress() { return ClientIPaddress ; }
        
        public ClientJoinMsg(int c, String n, int cc, int kv, String IPaddress) {
            setType(CLIENT_JOIN) ;
            setClientId(c) ;
            name = n ;
            colorCode = cc ;
            keyValue = kv ;
	    ClientIPaddress = IPaddress;
        }
    }
    
    public static class ClientRejectMsg extends Message {
        private int keyValue ;
        private int reason ;
        
        public static final int REASON_NAME = 0 ;
        public static final int REASON_COLOR = 1 ;
        
        public int getKeyValue() { return keyValue ; }
        public int getReason() { return reason ; }
        
        public ClientRejectMsg(int c, int kv, int r ) {
            setType(CLIENT_REJECT) ;
            setClientId(c) ;
            keyValue = kv ;
            reason = r ;
        }
    }
    
    public static class ClientLeaveMsg extends Message {
        
        public ClientLeaveMsg(int c) {
            setType(CLIENT_LEAVE) ;
            setClientId(c) ;
        }
    }
    
    public static class GestureLineMsg extends Message {
        private int id, aX, aY, bX, bY ;
        private long par ;
        
        public int getAX() { return aX ;}
        public int getAY() { return aY ;}
        public int getBX() { return bX ;}
        public int getBY() { return bY ;}
        public int getId() { return id;}
        public long getPar() { return par; }
        
        public GestureLineMsg(int c, int i, long p, int aX, int aY, int bX, int bY) {
            setType(GESTURE_LINE) ;
            setClientId(c) ;
            par = p ;
            id = i ;
            this.aX = aX ;
            this.aY = aY ;
            this.bX = bX ;
            this.bY = bY ;
        }
    }
    
    public static class GestureCircleMsg extends Message {
        private int id, x, y ;
        private long par ;
        
        public int getX() { return x ;}
        public int getY() { return y ;}
        public int getId() { return id;}
        public long getPar() { return par; }
        
        public GestureCircleMsg(int c, int i, long p, int x, int y) {
            setType(GESTURE_CIRCLE) ;
            setClientId(c) ;
            par = p ;
            id = i ;
            this.x = x ;
            this.y = y ;
        }
    }
    
    public static class DocumentStateMsg extends Message {
        private String xml ;
        
        public String getXml(){ return xml ;}
        
        public DocumentStateMsg(int c, String x) {
            setType(DOCUMENT_STATE) ;
            setClientId(c) ;
            xml = x ;
        }
    }
    
    public static class HighlightAddMsg extends Message {
        private int id ;
        private int highlightType ;
        private long startPar, endPar ;
        private int startOffset, endOffset ;
        
        public int getId() { return id ; }
        public int getHighlightType() { return highlightType ; }
        public long getStartPar() { return startPar ; }
        public long getEndPar() { return endPar ; } 
        public int getStartOffset() { return startOffset ; }
        public int getEndOffset() { return endOffset ; } 
        
        public HighlightAddMsg(int c, int id, int t, long sp, long ep, int so, int eo) {
            setType(HIGHLIGHT_ADD) ;
            setClientId(c) ;
            this.id = id ;
            highlightType = t ;
            startPar = sp ;
            endPar = ep ;
            startOffset = so ;
            endOffset = eo ;
        }
    }
    
    public static class HighlightDeleteMsg extends Message {
        private int id ;
        
        public int getId() { return id ; }
        
        public HighlightDeleteMsg(int c, int id) {
            setType(HIGHLIGHT_DELETE) ;
            setClientId(c) ;
            this.id = id ;
        }
    }
    
    public static class HighlightEditMsg extends HighlightAddMsg {
        
        public HighlightEditMsg(int c, int id, int t, long sp, long ep, int so, int eo) {
            super(c, id, t, sp, ep, so, eo) ;
            setType(HIGHLIGHT_EDIT) ;
        }
    }
    
    public static class HighlightTypeMsg extends Message {
        int id ;
        String name ;
        Color color ;
        
        public int getId() { return id ; }
        public String getName() { return name ; }
        public Color getColor() { return color ; }
        
        public HighlightTypeMsg(int c, int id, String n, Color col){
            setType(HIGHLIGHT_TYPE) ;
            setClientId(c) ;
            this.id = id ;
            name = n ;
            color = col ;
        }
        
    }
}




