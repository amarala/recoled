package cowrat.recoled.client;

import java.awt.Color ;
import java.util.Vector ;
import java.awt.Point ;

public class Gesture {
    
    private long parId ;
    private Vector points ;
    private Point start ;
    private Color mainColor, lightColor ;
    ScrollMarkers.ScrollMarker marker ;
    
    public void setMarker(ScrollMarkers.ScrollMarker m) {
        marker = m ;
        
    }
    
    public boolean isCircle() {
        return points.isEmpty() ;
    }
    
    public Color getMainColor() {
        return mainColor ;        
    }
    
    public Color getLightColor() {
        return lightColor ;        
    }
    
    public long getParId() {
        return parId ;
    }
    
    public Vector getPoints() {
        return points ;
    }
    
    public Point getStart() {
        return start ;
    }
    
    public Gesture(Color main, Color light, long p, int x, int y) {

        mainColor = main ;
        lightColor = light ;
        parId = p ;
        start = new Point(x, y) ;
        points = new Vector() ;
    }
    
    public Gesture(Color main, Color light, long p, int aX, int aY, int bX, int bY) {
        
        mainColor = main ;
        lightColor = light ;
        parId = p ;
        start = new Point(aX, aY) ;
        
        Point point = new Point(bX, bY) ;
        points = new Vector() ;
        points.add(point) ;
    }
    
    public void addPoint(int x, int y) {
        
        points.add(new Point(x, y)) ;
        
    }
    
}
