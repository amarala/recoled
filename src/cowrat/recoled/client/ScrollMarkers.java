package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.border.* ;

import java.util.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import cowrat.recoled.shared.MyImageIcon;

public class ScrollMarkers extends JLayeredPane {
    
    private Vector gestureMarkers ;
    private Vector undoMarkers ;
    private JScrollBar scrollbar ;
    private boolean updateUndoMarkers ;
    private boolean moving ;
    
    private int lastValue ;
    DocumentView parent ;
    
    public void setShowPacker(int height) {
    	System.out.println("Showing packer " + String.valueOf(height)) ;
    	setBorder(new EmptyBorder(height, 0, 0, 0)) ;
    	invalidate() ;
    }

    
    public ScrollMarkers(JScrollBar sb, DocumentView p) {
        super() ;
        setLayout(null) ;
        setMinimumSize(new Dimension(30, 1)) ;
        setMaximumSize(new Dimension(30, 100000)) ;
        setPreferredSize(new Dimension(30, 1000)) ;
        
        scrollbar = sb ;
        parent = p ;
        gestureMarkers = new Vector() ;		
        undoMarkers = new Vector() ;
        updateUndoMarkers = true ;
        
        scrollbar.addAdjustmentListener(new AdjustmentListener () { 
            
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int diff = Math.abs(e.getValue()- lastValue) ;
                if (!scrollbar.getValueIsAdjusting() && !moving) {
                    playVisableGestures() ;
                    if (updateUndoMarkers && diff > 70) {
                        //addUndoMarker(lastValue) ;
                    }
                    lastValue = e.getValue() ;
                }
            }			
        }) ;
    }
    
    public void playVisableGestures() {
        
        Thread thread = new Thread() {
            public void run() {
                Iterator i = gestureMarkers.iterator() ;
                
                while(i.hasNext()) {
                    ScrollMarker sm = (ScrollMarker) i.next() ;
                    
                    if (parent.isVisible(sm.getVerticalPosition())){
                        sm.stopFading() ;
                        i.remove() ;
                        remove(sm) ;
                        parent.paintImmediately() ;
                        parent.playGesture(sm.getGesture()) ;
                    }
                    try {
                        sleep(2000) ;
                    } catch(Exception e) {
                        System.out.println("ScrollMarkers: playVisableGestures. error") ;
                        e.printStackTrace() ;
                    }
                }
            }
        } ;
        thread.start() ;
    }
    
    public void addGestureMarker(Gesture g) {
        ScrollMarker marker = new ScrollMarker(g) ;
        g.setMarker(marker) ;
        gestureMarkers.add(marker) ;
        add(marker, new Integer(2)) ;
        parent.validate() ;
    }
      
    public void removeGestureMarker(ScrollMarker m) {
        gestureMarkers.remove(m) ;
        remove(m) ;
        parent.paintImmediately() ;
    }
    
    public void addUndoMarker(long parId, int yOffset) {
        if (!undoMarkers.isEmpty()) {
            ScrollMarker old = (ScrollMarker)undoMarkers.lastElement() ;
            old.setVisible(false) ;
        }
        
        ScrollMarker undo = new ScrollMarker(parId, yOffset) ;
        undoMarkers.add(undo) ;
        add(undo, new Integer(1)) ;
        parent.validate() ;
    }
    
    public void invalidate() {
        Iterator i = undoMarkers.iterator() ;
        while (i.hasNext()) {
            ScrollMarker m = (ScrollMarker) i.next() ;
            m.reposition() ;
        }
        
        super.invalidate() ;
        
    }
    
    public void removeUndoMarker(ScrollMarker undoMarker) {
        remove(undoMarker) ;
        undoMarkers.remove(undoMarker) ;
        parent.paintImmediately() ;
    }
    
    public void moveTo(final int destination) {
        
        moving = true ;
        
        final int start = scrollbar.getValue() ;
        final int diff = destination - start ;
        
        //int counter = 0 ;
        
        Thread thread = new Thread() {
            
            public void run() {
                for (double x = 0 ; x < Math.PI ; x = x + 0.1) {
                    double y = (0.5 * (Math.sin(x-(Math.PI/2)))) + 0.5 ;
                    
                    int pos = (int) Math.round(start + (y*diff)) ;
                    scrollbar.setValue(pos) ;
                    parent.paintImmediately(new Rectangle(0,0, parent.getWidth(), parent.getHeight())) ;
                    //scrollbar.paintImmediately(new Rectangle(0,0,scrollbar.getWidth(),scrollbar.getHeight())) ;
                    yield() ;
                    try {
                        sleep(50) ;
                    } catch(Exception e) {
                        System.out.println("Error: couldnt sleep") ;
                        e.printStackTrace() ;
                    }
                }
            }
        } ;
        thread.run() ;
        
        moving = false ;
        lastValue = scrollbar.getValue() ;
    }
    
    public void moveToCentered(int destination) {
    	moveTo(destination - (parent.getVisibleHeight()/2));
    }
    
    public int valueToPosition(int value) {
        return ((value*(getHeight() - 32))/scrollbar.getMaximum()) + 16 ;
    }
    
    public class ScrollMarker extends JPanel {
        
    	
    	long parId ;
    	int yOffset ;
    	Color color ;
    	int opacity ;
    	java.util.Timer fadeTimer ;
    	
    	Image image ;
        JLabel arrow ;
        JLabel imageLabel ;
        Gesture gesture ;
        
        private ScrollMarker self ;
        
        public ScrollMarker(long pId, int y) {
            super() ;
            self = this ;
            setLayout(new BorderLayout()) ;
            
            parId = pId ;
            yOffset = y ;
            color = Color.LIGHT_GRAY ;
            
            arrow = new JLabel(new MyImageIcon("images/arrow.gif")) ;
            add(arrow, BorderLayout.WEST);
            
            image = getImage("images/undo.gif") ;
            imageLabel = new JLabel(new ImageIcon(image)) ;
            //imageLabel.setBackground(color) ;
            //imageLabel.setOpaque(true) ;
            imageLabel.setBorder(new LineBorder(Color.BLACK)) ;
            add(imageLabel, BorderLayout.CENTER) ;
            
            
            
            int value = parent.getVerticalPosition(parId, yOffset) ;
            setBounds(0, valueToPosition(value) - 11, 25, 22) ;
            System.out.println("Scrollmarker added at " + String.valueOf(value)) ;
            
            imageLabel.addMouseListener(new MouseAdapter() {
                
                public void mouseClicked(MouseEvent e) {
                    moveToCentered(parent.getVerticalPosition(parId, yOffset)) ;
                    removeUndoMarker(self) ;
                    
                    if(!undoMarkers.isEmpty()) {
                        ScrollMarker old = (ScrollMarker)undoMarkers.lastElement() ;
                        old.setVisible(true) ;
                    }
                    parent.revalidate() ;
                }
            }) ;
        }
        
        public ScrollMarker(Gesture g) {
            super() ;
            self = this ;
            gesture = g ;
            setLayout(new BorderLayout()) ;
            
            parId = g.getParId() ;
            yOffset = g.getStart().y ;
            color = g.getLightColor() ;
            opacity = 255 ;
            
            arrow = new JLabel(new MyImageIcon("images/arrow.gif")) ;
            add(arrow, BorderLayout.WEST);
            
            
            image = getImage("images/point.gif");
            imageLabel = new JLabel(new ImageIcon(image)) ;
            imageLabel.setBackground(color) ;
            imageLabel.setOpaque(true) ;
            imageLabel.setBorder(new LineBorder(Color.BLACK)) ;
            add(imageLabel, BorderLayout.CENTER) ;
            
            int value = parent.getVerticalPosition(parId, yOffset) ;
            
            setBounds(0, valueToPosition(value) - 11, 25, 22) ;
            System.out.println("Scrollmarker added at " + String.valueOf(value)) ;
            
            imageLabel.addMouseListener(new MouseAdapter() {
                
                public void mouseClicked(MouseEvent e) {
                	int y = parent.getVisibleCenter() ;
                	
                	Paragraph p = parent.getParFromVerticalPosition(y) ;
                	
                	
                	addUndoMarker(p.getID(), y - parent.getVerticalPosition(p.getID(), 0)) ;
                	int value = parent.getVerticalPosition(parId, yOffset) ;
                    moveToCentered(value) ;
                    playVisableGestures() ;
                    //parent.playGesture(gesture) ;
                    //removeGestureMarker(self) ;
                    //fadeTimer.cancel() ;
                }
            }) ;
            
            fadeTimer = new java.util.Timer() ;
            fadeTimer.scheduleAtFixedRate(new TimerTask() {
            	
            	public void run() {
            		fade() ;
            	} ;
            }, 1000, 1000) ;
        }
        
        public void fade() {
        	
        	opacity = opacity - 5 ;
        	Color destColor = self.getBackground() ;
        	
        	//int red = color.getRed() - (opacity * (color.getRed() - destColor.getRed()))/ * 100 ;
        	
        	//Color curr = new Color(destColor.get)
        	
        	if (opacity > 0) {
        		
        		BufferedImage b = new BufferedImage(22, 22, BufferedImage.TYPE_INT_RGB) ;
        		Graphics g = b.getGraphics() ;
        		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue())) ;
        		g.fillRect(0, 0, 22, 22) ;
        		g.drawImage(image, 0, 0, self) ;
        		g.setColor(new Color(destColor.getRed(), destColor.getGreen(), destColor.getBlue(), 255-opacity)) ;
        		g.fillRect(0, 0, 22, 22) ;
        		
        		imageLabel.setIcon(new ImageIcon(b)) ;
        		//image.setBackground(new Color(0, 0, 0, 255)) ;
        		imageLabel.setBorder(new LineBorder(new Color(0, 0, 0, opacity))) ;
        		repaint() ;
        	}
        	else {
        		fadeTimer.cancel() ;
        		removeGestureMarker(self) ;
        	}
        	
        }
        
        public void stopFading() {
            fadeTimer.cancel() ;
        }
        
        public Gesture getGesture() {
            return gesture ;
        }
        
      private Image getImage (String p){
        return 
          Toolkit.getDefaultToolkit().
          createImage(ClassLoader.getSystemResource(p)) ;
      }

        public void reposition() {
        	int value = parent.getVerticalPosition(parId, yOffset) ;
            setBounds(0, valueToPosition(value) - 11, 25, 22) ;
        }
        
        public int getVerticalPosition() {
            return parent.getVerticalPosition(parId, yOffset) ;
        }
    }
}
