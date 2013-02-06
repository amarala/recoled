package cowrat.recoled.client;

import java.util.* ;
import javax.swing.text.* ;
import java.awt.* ;

public class ColoredSections extends Thread {
	
    private Vector coloredSections ;
    private EditorDocument document ;
    private boolean ShowOrder;
	
    public ColoredSections(EditorDocument d) {
	super() ;
	document = d ;
	coloredSections = new Vector() ;
	setDaemon(true) ;
		
	start() ;
    }
	
    public synchronized void addColoredSection (int start, int end, Color color) {
	
	int index = 0 ;
	int s, e ;
		
	while (index < coloredSections.size()) {
	    ColoredSection cs = (ColoredSection) coloredSections.elementAt(index) ;
			
	    s = cs.getStart() ;
	    e = cs.getEnd() ;
			
	    if (e < start) {
				// completely before new section
		if (ShowOrder)
		System.out.println(cs.toString() + " is completely before " + String.valueOf(start) + " " + String.valueOf(end)) ;
				// just skip along to next one
	    }
	    else if (s < start && e <= end) {
				// partly before new section
		if (ShowOrder)
		System.out.println(cs.toString() + " is partly before " + String.valueOf(start) + " " + String.valueOf(end)) ;
		cs.setEnd(start) ;
	    }
	    else if (s < start && e > end) {
				// encloses new section
		if (ShowOrder)
		System.out.println(cs.toString() + " completely encloses " + String.valueOf(start) + " " + String.valueOf(end)) ;
		ColoredSection split = new ColoredSection(end, e, cs.getColor(), cs.getValue()) ;
		coloredSections.add(index + 1, split) ;
		cs.setEnd(start) ;
		index = index + 1 ;
		break ;
	    }
	    else if (s >= start && e <= end) {
				// enclosed by new section
		if (ShowOrder)
		System.out.println(cs.toString() + " is completely enclosed by " + String.valueOf(start) + " " + String.valueOf(end)) ;
		coloredSections.remove(index) ;
		index = index -1 ;
	    }
	    else if (s >= start && s < end && e > end) {
				// partly after new section
		if (ShowOrder)
		System.out.println(cs.toString() + " is partly after " + String.valueOf(start) + " " + String.valueOf(end)) ;
		cs.setStart(end) ;
		break ;
	    }
	    else {
				// completely after new section
		if (ShowOrder)
		System.out.println(cs.toString() + " is completely after " + String.valueOf(start) + " " + String.valueOf(end)) ;
		break ;
	    }
		
		
	    index ++ ;
	}
		
	ColoredSection cs = new ColoredSection(start, end, color) ;
	coloredSections.add(index, cs) ;
    }
	
	
    public void run() {
	while(1==1) {
	    yield() ;
			
	    if(!coloredSections.isEmpty()) 
		update() ;
			
	    try {
		sleep(250) ;
	    } catch(Exception e) { 
		System.out.println("Gestures: run: couldnt sleep") ;
	    }
	}
    }
	
    public synchronized void update() {
		
	Iterator i = coloredSections.iterator() ;
		
	while (i.hasNext()){
	    ColoredSection cs = (ColoredSection) i.next() ;
	    if(!cs.update())
		i.remove() ;
	}
		
		
    }
	
	
    private class ColoredSection {
		
	private Position start, end ;
	private Color color ;
	private int value ;
		
	public int getStart() {return start.getOffset() ; }
	public void setStart(int s) { start = document.createPosition(s) ; }
	public int getEnd() { return end.getOffset() ; }
	public void setEnd(int e) { end = document.createPosition(e) ; }
	public Color getColor() { return color ; }
	public int getValue() { return value ; } 
		
	public ColoredSection(int s, int e, Color c) {
	    start = document.createPosition(s) ;
	    end = document.createPosition(e) ;
	    color = c ;
	    value = 100 ;
	}
		
	public ColoredSection(int s, int e, Color c, int v) {
	    start = document.createPosition(s) ;
	    end = document.createPosition(e) ;
	    color = c ;
	    value = v ;
	}
		
	public String toString() { return String.valueOf(getStart()) + " " + String.valueOf(getEnd()) ; }
		
	public synchronized boolean update() {
			
	    // paint
	    int red = (color.getRed()*value)/100 ;
	    int green = (color.getGreen()*value)/100 ;
	    int blue = (color.getBlue()*value)/100 ;
			
	    Color adjColor = new Color(red, green, blue) ;
	    SimpleAttributeSet attrs = new SimpleAttributeSet();
	    StyleConstants.setForeground(attrs, adjColor) ;
	    //StyleConstants.setBold(attrs, true) ;
	    document.setCharAttributes(start.getOffset(), end.getOffset() - start.getOffset(), attrs) ;
			
	    value = value - 1 ;
			
	    if (value < 0)
		return false ;
	    else
		return true ;
	}
    }
}
