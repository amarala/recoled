package cowrat.recoled.client;

import java.awt.* ;
import java.awt.event.* ;
import java.util.* ;
import javax.swing.* ;
import javax.swing.border.* ;
import cowrat.recoled.shared.MyColors; 
import cowrat.recoled.shared.MyImageIcon; 

public class Clients extends JPanel {
    
    private HashMap clients ;
    private EditorClient parent ;
    private Client myself ;
    private Vector sharedScrollPanels ;
    private DocumentView documentView ;
    
    JPanel others ;
    
    public Vector getOthers() {
        Vector v = new Vector() ;
        Iterator i = clients.values().iterator() ;
        while (i.hasNext()) {
            Client c = (Client) i.next() ;
            if (c != myself)
                v.add(c) ;
        }
        return v ;
    }
    
    public void setDocumentView(DocumentView dv) {
    	documentView = dv ;
    }
    
    public Clients(EditorClient p) {
        super() ;
        parent = p ;
        clients = new HashMap() ;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
        setBorder(new EmptyBorder(5, 5, 0, 5)) ;
        setMaximumSize(new Dimension(85, 1000)) ;
        setMinimumSize(new Dimension(85, 0)) ;
        setPreferredSize(new Dimension(85, 1000)) ;
        
        others = new JPanel() ;
        others.setLayout(new BoxLayout(others, BoxLayout.Y_AXIS)) ;
        add(others) ;
        
        sharedScrollPanels = new Vector() ;
    }
    
    public int getMyColorCode() {
        if (myself == null) 
            return -1 ;
        else
            return myself.getColorCode() ;
        
    }
    
    public Color getMyColor()
    	{ return myself.getMainColor();}
    
    public int getMyId() {
        if (myself == null) 
            return -1 ;
        else
            return myself.getIdNumber() ;
    }
    
    public String getMyName() {
        if (myself == null) 
            return "Not Set" ;
        else
            return myself.getName() ;
    }
    
    public Client getMyself() {
        return myself ;
    }
    
    public void setMyself(int idNumber, String name, int color) {
        
        if (myself  == null){
            myself = new Client(idNumber, name, color) ;
            add(myself, 0) ;
            add(Box.createVerticalStrut(5), 1) ;
            add(new JSeparator(), 2) ;
            add(Box.createVerticalStrut(5), 3) ;
            add(Box.createGlue(), 4) ;
            repaint() ;
            parent.validate() ;//doLayout() ;
        }
        
        
    }
    
    public void removeClient(int idNumber) {
        
        Client c = getClient(idNumber) ;
        if (c.isFollowed())
        	documentView.setFollowingClient(null) ;
        
        for (int i = 0 ; i < others.getComponentCount() ; i ++) {
            
            if (others.getComponent(i) == c) {
                others.remove(i) ; 
                others.remove(i- 1) ; // remove spacer
                break ;
            }
        }
        
        Iterator i = sharedScrollPanels.iterator() ;
        while (i.hasNext()) {
            SharedScrollPanel s = (SharedScrollPanel) i.next() ;
            s.removeSharedScroll(idNumber) ;
        }
        parent.validate() ;
        
    }
    
    public void addClient(int idNumber, String name, int color) {
        
        if (!clients.containsKey(new Integer(idNumber))) {
            Client c = new Client(idNumber, name, color) ;
            others.add(Box.createVerticalStrut(5)) ;
            others.add(c) ;
            
            clients.put(new Integer(idNumber), c) ;
            
            Iterator i = sharedScrollPanels.iterator() ;
            while (i.hasNext()) {
                SharedScrollPanel s = (SharedScrollPanel)i.next() ;
                s.addSharedScroll(c) ;
            }	
            others.repaint() ;
            repaint() ;
            
            parent.validate() ;
        }
    } 
    
    public Client getClient(int id) {
        if(id >= 0)
            return (Client) clients.get(new Integer(id)) ;
        else
            return myself ;
    }
    
    public void addSharedScrollPanel(SharedScrollPanel s) {
        sharedScrollPanels.add(s) ;
        
        Iterator i = clients.values().iterator() ;
        while (i.hasNext()) {
            Client c = (Client)i.next() ;
            s.addSharedScroll(c) ;
        }
        
    }
    
    public void scrollMoved(int clientID, int value, int view, int max, boolean isPrimary) {
        
        Client c = getClient(clientID) ;
        double scrollProportion = (double)value/(double)max ;
        c.setScrollProportion(scrollProportion) ;
        if (c.isFollowed()) 
        	documentView.followClientTo(scrollProportion) ;
        
        Iterator i = sharedScrollPanels.iterator() ;
        while (i.hasNext()) {
            SharedScrollPanel s = (SharedScrollPanel)i.next() ;
            s.scrollMoved(clientID, value, view, max, isPrimary) ;
        }
        
    }
    
    
    
    public class Client extends JPanel{
        
        // Constants.....................................................................................................
        public static final int WAITING		 = 0;
        public static final int TYPING 		 = 1;
        public static final int WAS_TYPING 	 = 2;
        public static final int HIGHLIGHTING = 3;
        public static final int WAS_HIGHLIGHTING = 4;
        public static final int POINTING 	 = 5;
        public static final int WAS_POINTING = 6;
        public static final int IDLE 	 = 7;
        public static final int GONE 		 = 8;
        
        // Variables.....................................................................................................
        private String name ;
        private int idNumber ;
        private int color ;		
        private int state ;
        private boolean present ;
        private boolean followed ;
        private Client self ;
        
        private int pValue, sValue ;
        private int pView, sView ;
        private int pMax, sMax ;
        
        private JLabel titleLabel ;
        private JLabel imageLabel ;
        private ImageIcon image ;
        
        private java.util.Timer timer ;
        
        // Variable Access Methods........................................................................................
        
        public String getName() { return name ;}		
        public int getIdNumber() { return idNumber ;}
        public int getColorCode() { return color ;}
        public Color getLightColor() { return MyColors.getLightColor(color) ;} 
        public Color getMainColor() { return MyColors.getMainColor(color) ;} 
        public void setPresent(boolean p) { present = p ;}
        public boolean isPresent() { return present ;}
        
        public int lastGestureId ;
        public long lastGesturePar ;
        public int lastGestureParY ;
        public int lastGestureY ;
        public boolean lastGestureVisible ;
        private double scrollProportion ;
        
        public Gesture lastGesture ;
        
        public boolean isFollowed() {
        	return followed ;
        }
        
        public void setFollowed(boolean b) {
        	followed = b ;
        }
        
        public void setStateIdle() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            state = IDLE ;
            imageLabel.setIcon(new MyImageIcon("images/client_idle.gif")) ;		
        }
        
        public void setStateWaiting() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateIdle() ;
                }
            }, 60000) ;
            
            state = WAITING ;
            imageLabel.setIcon(new MyImageIcon("images/client_waiting.gif")) ;		
        }
        
        public void setStatePointing() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWasPointing() ;
                }
            }, 1000) ;
            
            state = POINTING ;
            imageLabel.setIcon(new MyImageIcon("images/client_pointing.gif")) ;		
        }
        
        public void setStateWasPointing() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWaiting() ;
                }
            }, 5000) ;
            
            state = WAS_POINTING ;
            imageLabel.setIcon(new MyImageIcon("images/client_wasPointing.gif")) ;		
        }
        
        public void setStateHighlighting() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWasHighlighting() ;
                }
            }, 1000) ;
            
            state = HIGHLIGHTING ;
            imageLabel.setIcon(new MyImageIcon("images/client_highlighting.gif")) ;		
        }
        
        public void setStateWasHighlighting() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWaiting() ;
                }
            }, 5000) ;
            
            state = WAS_HIGHLIGHTING ;
            imageLabel.setIcon(new MyImageIcon("images/client_wasHighlighting.gif")) ;		
        }
        public void setStateTyping() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWasTyping() ;
                }
            }, 1000) ;
            
            state = TYPING ;
            imageLabel.setIcon(new MyImageIcon("images/client_typing.gif")) ;		
        }
        
        public void setStateWasTyping() {
            timer.cancel() ;
            timer = new java.util.Timer(true) ;
            timer.schedule( new TimerTask() {
                public void run() {
                    setStateWaiting() ;
                }
            }, 5000) ;
            
            state = WAS_TYPING ;
            imageLabel.setIcon(new MyImageIcon("images/client_wasTyping.gif")) ;	
        }
        public double getScrollProportion() {
            return scrollProportion ;
        }
        public void setScrollProportion(double d) {
        	scrollProportion = d ;
        }
        // Constructor.....................................................................................................
        
        public Client(int id, String n, int c) {
            super() ;
            self = this ;
            setLayout(new BorderLayout()) ; 
            idNumber = id ;
            name = n ;
            color = c ;
            present = true ;
            timer = new java.util.Timer() ;
            
            
            titleLabel = new JLabel(name, JLabel.CENTER) ;
            titleLabel.setBackground(MyColors.getMainColor(color)) ;
            titleLabel.setForeground(Color.WHITE) ;
            titleLabel.setOpaque(true) ;
            IncompleteLineBorder b = new IncompleteLineBorder(Color.BLACK) ;
            b.setExcludeBottom(true) ;
            titleLabel.setBorder(b) ;
            add(titleLabel, BorderLayout.NORTH) ;
            
            
            
            imageLabel = new JLabel() ;
            imageLabel.setBackground(MyColors.getLightColor(color)) ;
            imageLabel.setOpaque(true) ;
            imageLabel.setBorder(new LineBorder(Color.BLACK)) ;	
            add(imageLabel, BorderLayout.CENTER) ;
            
            setMaximumSize(new Dimension(80, 90)) ;
            setMinimumSize(new Dimension(80, 90)) ;
            setStateWaiting() ;
            
            addMouseListener(new MouseAdapter() {
            	public void mouseClicked(MouseEvent e) {
            		if (self != myself) {
            			if (followed) {
            				documentView.setFollowingClient(null) ;
            				followed = false ;
            			} else {
            				documentView.setFollowingClient(self) ;
            				documentView.followClientTo(scrollProportion) ;
            				followed = true ;
            			}
            		}
            	}
            }) ;
        }
    }
}
