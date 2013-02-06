package cowrat.recoled.client;

import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import cowrat.recoled.shared.MyColors;

public class DrawArea extends JPanel {
    
    private int oldX, oldY ;
    
    private EditorClient parent ;
    private Gestures gestures ;
    private Clients clients ;
    private Paragraphs paragraphs ;
    private JTextPane textPane ;
    
    private int nextGestureId ;
    private Paragraph lastPar ;
    private int parY ;
    
    boolean allowClick ;
    
    public void setAllowClick(boolean b) {
        allowClick = b ;
    }
    
    public Point getRelativePoint(Point p) {
        Point newPoint = new Point(p.x, p.y - parY) ;
        return newPoint ;
    }
    
    public void point(int x, int y) {
        lastPar = parent.getParFromOffset(textPane.viewToModel(new Point(x, y))) ;
        try {
            parY = (int) textPane.modelToView(lastPar.getOffset()).getY() ; 
        } catch (Exception e) {
            System.out.println("DrawArea: error calculating closest par Y") ;
            e.printStackTrace() ;
        }
        nextGestureId ++ ; 
        
        Point p = getRelativePoint(new Point(x,y)) ;
        gestures.addCircle(MyColors.getMainColor(clients.getMyColorCode()), lastPar.getID(), p.x, p.y) ;
        parent.sendGestureCircle(nextGestureId, lastPar.getID(), p.x, p.y) ;
        repaint() ;
        
    }
    
    public DrawArea(EditorClient p, Gestures g, Clients c, JTextPane tp) {
        super() ;
        
        parent = p ;
        gestures = g ;
        clients = c ;
        textPane = tp ;
        
        nextGestureId++ ;
        oldX = oldY = -1 ;
        
        setOpaque(false) ;
        
        addMouseListener(new MyMouseListener()) ;
        addMouseMotionListener(new MyMouseMotionListener()) ;
    }
    
    public void paint(Graphics g) {
        gestures.paint(g) ;
    }
    
    private class MyMouseListener extends MouseAdapter {
        
        public void mousePressed(MouseEvent e) {
            if (allowClick) {
                lastPar = parent.getParFromOffset(textPane.viewToModel(e.getPoint())) ;  
                
                try {
                    parY = (int) textPane.modelToView(lastPar.getOffset()).getY() ; 
                } catch (Exception x) {
                    System.out.println("DrawArea: error calculating closest par Y") ;
                    x.printStackTrace() ;
                }
                nextGestureId ++ ; 
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            if (allowClick)
                oldX = oldY = -1 ;
            
        }
        
        public void mouseClicked(MouseEvent e) {
            if (allowClick) {
                Point p = getRelativePoint(new Point(e.getX(), e.getY())) ;
                gestures.addCircle(MyColors.getMainColor(clients.getMyColorCode()), lastPar.getID(), p.x, p.y) ;
                parent.sendGestureCircle(nextGestureId, lastPar.getID(), p.x, p.y) ;
                repaint() ;
            }
        }
    }
    
    private class MyMouseMotionListener extends MouseMotionAdapter {
        
        public void mouseDragged(MouseEvent e) {
            if (allowClick) {
	            int newX = e.getX() ;
	            int newY = e.getY() ;
	            
	            Point a = getRelativePoint(new Point(newX, newY)) ;
	            Point b = getRelativePoint(new Point(oldX, oldY)) ;
	            
	            if (a.distance(b) > 10) {
	                
	                if (oldX >= 0) {
	                    gestures.addLine(MyColors.getMainColor(clients.getMyColorCode()), lastPar.getID(), a.x, a.y, b.x, b.y) ;
	                    parent.sendGestureLine(nextGestureId, lastPar.getID(), a.x, a.y, b.x, b.y) ;
	                    repaint() ;
	                }
	                
	                oldX = newX ;
	                oldY = newY ;
	                
	            }
            }
        }
    }
}
