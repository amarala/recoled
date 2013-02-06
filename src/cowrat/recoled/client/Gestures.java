package cowrat.recoled.client;

import java.util.* ;
import java.awt.* ;

import javax.swing.JTextPane ;

public class Gestures extends Thread{
    
    private Vector lines ;
    private Vector circles ;
    private Vector drawAreas ;
    private int nextGestureId ;
    private LockManager lockManager ;
    
    private Paragraph lastPar ;
    private JTextPane textPane ;
    private int parY ;
    
    
    
    public void setTextPane(JTextPane t) {
        textPane = t ;
    }
    
    public Point getRelativePoint(Point p) {
        Point newPoint = new Point(p.x, p.y + parY) ;
        return newPoint ;
    }
    
    public Gestures(LockManager l) {
        super() ;
        lockManager = l ;
        lines = new Vector() ;
        circles = new Vector() ;
        drawAreas = new Vector() ;
        
        setDaemon(true) ;
        start() ;
    }
    
    public boolean hasShapes() {
        if (lines.isEmpty() && circles.isEmpty())
            return false ;
        else
            return true ;
    }
    
    public void run() {
        
        while (true) {
            yield() ;
            
            if (hasShapes()) {
                updateShapes() ;
                
                Iterator i = drawAreas.iterator() ; 
                while (i.hasNext()) 
                    ((DrawArea) i.next()).repaint() ;
            }
            
            try {
                sleep(100) ;
            } catch(Exception e) { 
                System.out.println("Gestures: run: couldnt sleep") ;
            }
        }			
    }
    
    public void addDrawArea(DrawArea d) {
        drawAreas.add(d) ;
    }
    
    public synchronized void addCircle(Color c, long parId, int x, int y) {
        if (lastPar == null || lastPar.getID() != parId) {
            lastPar = lockManager.getParFromId(parId) ;
            try {
                parY = (int) textPane.modelToView(lastPar.getOffset()).getY() ;
            } catch(Exception e) {
                System.out.println("Gestures: error calculating par Y") ;
                e.printStackTrace() ;
            }
        }
        
        Point p = getRelativePoint(new Point(x, y)) ;
        
        DrawnCircle circle = new DrawnCircle(c, p.x, p.y) ;
        circles.add(circle) ;
    }
    
    public synchronized void addLine(Color c, long parId, int aX, int aY, int bX, int bY) {
        
        if (lastPar == null || lastPar.getID() != parId) {
            lastPar = lockManager.getParFromId(parId) ;
            try {
                parY = (int) textPane.modelToView(lastPar.getOffset()).getY() ;
            } catch(Exception e) {
                System.out.println("Gestures: error calculating par Y") ;
                e.printStackTrace() ;
            }
        }
        
        Point a = getRelativePoint(new Point(aX, aY)) ;
        Point b = getRelativePoint(new Point(bX, bY)) ;
        
        DrawnLine line = new DrawnLine(c, a.x, a.y, b.x, b.y) ;
        lines.add(line) ;
    }
    
    public synchronized void paint(Graphics g) {
        
        if (hasShapes()) {
            Iterator i = lines.iterator() ;
            while (i.hasNext()) {
                DrawnLine line = (DrawnLine) i.next() ;
                line.draw((Graphics2D) g) ;
            }
            i = circles.iterator() ;
            while (i.hasNext()) {
                DrawnCircle circle = (DrawnCircle) i.next() ;
                circle.draw((Graphics2D) g) ;
            }
        }
    }
    
    public synchronized void updateShapes() {
        DrawnLine line ;
        DrawnCircle circle ;
        
        Iterator i = lines.iterator() ;
        while (i.hasNext()) {
            line = (DrawnLine) i.next() ;
            if (!line.update())
                i.remove() ;
        }
        
        i = circles.iterator() ;
        while (i.hasNext()) {
            circle = (DrawnCircle) i.next() ;
            if (!circle.update()){
                i.remove() ;
            }
        }
    }
    
    public void playGesture(final Gesture g) {
        System.out.println("Playing gesture") ;
        if (g.isCircle()) {
            addCircle(g.getMainColor(), g.getParId(), g.getStart().x, g.getStart().y) ;
        }
        else {
            Thread thread = new Thread() {
                public void run() {
                    Point last = g.getStart() ;
                    Iterator i = g.getPoints().iterator() ;
                    
                    while (i.hasNext()) {
                        try {
                            sleep(50) ;
                        } catch (Exception e) {
                            System.out.println("Gestures: playGesture. error") ;
                            e.printStackTrace() ;
                        }
                        
                        Point curr = (Point) i.next() ;
                        addLine(g.getMainColor(), g.getParId(), last.x, last.y, curr.x, curr.y) ;
                        last = curr ;
                        
                        yield() ;
                    }
                }
            } ;
            thread.start() ;
        }
    }
    
    private class DrawnCircle{
        
        private Color color ;
        private int opacity ;
        private int x, y, size ;
        
        public DrawnCircle(Color c, int x, int y) {
            color = c ;
            this.x = x ;
            this.y = y ;
            opacity = 150 ;
            size = 3 ;
        }
        
        public void draw(Graphics2D g) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity)) ;
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)) ;
            g.drawOval(x-2, y-2, 4, 4) ;
            g.drawOval(x-size, y-size, 2*size, 2*size) ;
        }
        
        public boolean update() {
            size = size + 5 ;
            opacity = opacity - 5 ;
            if (opacity > 0) 
                return true ;
            else
                return false ;
        }
    }
    
    private class DrawnLine{
        
        private Color color ;
        private int opacity ;
        private int aX, aY, bX, bY ;
        
        public DrawnLine(Color c, int aX, int aY, int bX, int bY){
            color = c ;
            opacity = 150 ;
            
            this.aX = aX ;
            this.aY = aY ;
            this.bX = bX ;
            this.bY = bY ;
        }
        
        public void draw(Graphics2D g) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity)) ;
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)) ;
            g.drawLine(aX, aY, bX, bY) ;
        }
        
        public boolean update() {
            opacity = opacity - 2 ;
            if (opacity > 0)
                return true ;
            else
                return false ;
        }	    
        
    }
    
}
