package cowrat.recoled.client;

import javax.swing.* ;
import java.awt.* ;
import javax.swing.border.* ;

public class ScrollBar extends JPanel {
	
    private int value ;
    private int view ;
    private int max ;
	
    JLabel slider ;
	
    public ScrollBar(Color back, Color front) {
	setLayout(null) ;
	setBackground(back) ;
	setBorder(new LineBorder(Color.BLACK)) ;
	setMinimumSize(new Dimension(5, 5)) ;
	setMaximumSize(new Dimension(5, 10000)) ;
		
	value = 0 ;
	view = 100 ;
	max = 100 ;
		
	slider = new JLabel() ;
	slider.setBackground(front) ;
	slider.setOpaque(true) ;
	slider.setBorder(new LineBorder(Color.BLACK)) ;
	slider.setMinimumSize(new Dimension(5, 5)) ;
	slider.setMaximumSize(new Dimension(5, 10000)) ;
		
		
	add(slider) ;
    }
	
    public void updateScroll(int va, int vi, int m) {
	value = va ;
	view = vi ;
	max = m ;
	int height = getHeight() ;
	System.out.println("Updating to (" + String.valueOf((value*height)/max) + 
			   ", " + String.valueOf((view*height)/max) + ")") ;
		
	slider.setSize(new Dimension(5, (view*height)/max)) ;
	slider.setLocation(new Point(0, (value*height)/max)) ;
		
	repaint() ;
    }
	
    // gets called every time scroll is resized
    public void invalidate() {
	super.invalidate() ;
	System.out.println("height: " + String.valueOf(getHeight())) ;
	updateScroll(value, view, max) ;
    }	
}
