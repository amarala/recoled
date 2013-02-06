package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.border.* ;
import java.awt.* ;
import java.util.* ;


public class SharedScrollPanel extends JPanel {

    private Vector sharedScrolls ;
    
    public void setShowPacker(int height) {
    	//System.out.println("Showing packer " + String.valueOf(height)) ;
    	setBorder(new EmptyBorder(16 + height, 2, 16, 2)) ;
    	invalidate() ;
    }

    public SharedScrollPanel() {
	super() ;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
	setMaximumSize(new Dimension(0, 10000)) ;
	setBorder(new EmptyBorder(16, 2, 16, 2)) ;

	sharedScrolls = new Vector() ;
    }

    public SharedScroll addSharedScroll(Clients.Client c) {
	SharedScroll s = new SharedScroll(c) ;
	JPanel jp = new JPanel() ;
	jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS)) ;
	jp.add(s) ;
	jp.add(Box.createHorizontalStrut(2)) ;
	
	add(jp) ;
	sharedScrolls.add(s) ;
	setMinimumSize(new Dimension((sharedScrolls.size() * 7) + 4, 5)) ;
	setMaximumSize(new Dimension((sharedScrolls.size() * 7) + 4, 10000)) ;
	setPreferredSize(new Dimension((sharedScrolls.size() * 7) + 4, 400)) ;

	return s ;
    }

    public void removeSharedScroll(int clientId) {

	SharedScroll s ;
	int index = 0 ;
	Iterator i = sharedScrolls.iterator() ;


	while (i.hasNext()){
	    s = (SharedScroll) i.next() ;
	    if (s.getClient().getIdNumber() == clientId) {
		i.remove() ;
		remove(index) ;
	    }
	    index ++ ;
	}

	setMinimumSize(new Dimension((sharedScrolls.size() * 7) + 4, 5)) ;
	setMaximumSize(new Dimension((sharedScrolls.size() * 7) + 4, 10000)) ;
	setPreferredSize(new Dimension((sharedScrolls.size() * 7) + 4, 400)) ;
    }

    public void scrollMoved(int clientID, int value, int view, int max, boolean isPrimary) {

	Iterator i = sharedScrolls.iterator() ;
	while (i.hasNext()) {
	    SharedScroll s = (SharedScroll) i.next() ;
	    if(s.getClient().getIdNumber() == clientID) {
		if (isPrimary)
		    s.updatePrimaryScroll(value, view, max) ;
		else
		    s.updateSecondaryScroll(value, view, max) ;
		break ;
	    }
	}
    }

    public class SharedScroll extends JPanel {
	
	Clients.Client client ;

	private int pValue, sValue ;
	private int pView, sView ;
	private int pMax, sMax ;

	JLabel pSlider, sSlider ;

	public Clients.Client getClient() { return client ;}
	
	public SharedScroll(Clients.Client c) {
	    super() ;
	    client = c ;

	    setLayout(null) ;
	    setBackground(Color.WHITE) ;
	    setBorder(new LineBorder(Color.BLACK)) ;
	    setMinimumSize(new Dimension(5, 5)) ;
	    setMaximumSize(new Dimension(5, 10000)) ;
		
	    pValue = 0 ;
	    pView = 100 ;
	    pMax = 100 ;

	    sValue = -1 ;
	    sView = 100 ;
	    sMax = 100 ;

	    pSlider = new JLabel() ;
	    pSlider.setBackground(c.getMainColor()) ;
	    pSlider.setOpaque(true) ;
	    pSlider.setBorder(new LineBorder(Color.BLACK)) ;
	    pSlider.setMinimumSize(new Dimension(5, 5)) ;
	    pSlider.setMaximumSize(new Dimension(5, 10000)) ;

	    sSlider = new JLabel() ;
	    sSlider.setBackground(c.getLightColor()) ;
	    sSlider.setOpaque(true) ;
	    sSlider.setBorder(new LineBorder(Color.BLACK)) ;
	    sSlider.setMinimumSize(new Dimension(5, 5)) ;
	    sSlider.setMaximumSize(new Dimension(5, 10000)) ;
	    sSlider.setVisible(false) ;
				
	    add(pSlider) ;
	    add(sSlider) ;
	}
	
	public void updatePrimaryScroll(int va, int vi, int m) {
	    pValue = va ;
	    pView = vi ;
	    pMax = m ;

	    if (pValue < 0) {
		pSlider.setVisible(false) ;
	    }
	    else {
		pSlider.setVisible(true) ;
		int height = getHeight() ;
		
		pSlider.setSize(new Dimension(5, (pView*height)/pMax)) ;
		pSlider.setLocation(new Point(0, (pValue*height)/pMax)) ;
	    }
	    repaint() ;
	}

	public void updateSecondaryScroll(int va, int vi, int m) {
	    sValue = va ;
	    sView = vi ;
	    sMax = m ;

	    if (sValue < 0) {
		sSlider.setVisible(false) ;
	    }
	    else {
		sSlider.setVisible(true) ;
		int height = getHeight() ;
		
		sSlider.setSize(new Dimension(5, (sView*height)/sMax)) ;
		sSlider.setLocation(new Point(0, (sValue*height)/sMax)) ;

	    }
	    repaint() ;
	}
	
	// gets called every time scroll is resized
	public void invalidate() {
	    super.invalidate() ;
	    //System.out.println("height: " + String.valueOf(getHeight())) ;
	    updatePrimaryScroll(pValue, pView, pMax) ;
	    updateSecondaryScroll(sValue, sView, sMax) ;
	}	
    }


}
	
	
		
