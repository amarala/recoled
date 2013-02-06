package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.border.* ;
import java.awt.* ;
import java.awt.event.* ;


public class ParagraphPanel extends JPanel  implements EditorDebugFlags  {
	
	JTextPane textPane ;
	Clients clients ;
	
	Box labels ;
	int lockWidth = 10 ;
	int paragraphCount ;
	boolean showNumbers ;
	
	public boolean getShowNumbers() {
	    return showNumbers ;	    
	}
	
	public ParagraphPanel(JTextPane t, Clients c) {
		super() ;
		textPane = t ;
		clients = c ;
		paragraphCount = 0 ;
		showNumbers = true ;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
		setBackground(Color.WHITE) ;
		setBorder(new EmptyBorder(5,5,5,5)) ;
		
		labels = new Box(BoxLayout.Y_AXIS) ;
		labels.add(Box.createGlue()) ;
		
		add(labels) ;
		add(Box.createGlue()) ;
		textPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				resizeAll() ;
			}
		}) ;
	}
	
	public void setShowParagraphNumbers(boolean spn) {
	    showNumbers = spn ;
	    for(int i = 0 ; i < paragraphCount ; i++) {
			ParagraphBox p = (ParagraphBox) labels.getComponent(i) ;
			p.setShowNumber(spn) ;
		}
    }
	
	public void setSize(Dimension d) {
		super.setSize(d) ;
		resizeAll() ;
	}
	
	public void setSize(int x, int y) {
		super.setSize(x, y) ;
		resizeAll() ;
	}
	
	public void addParagraphBox(Paragraph p, int index) {

		if (ParagraphPanel_Debug)
			System.out.print("\n\nParagraphPanel-->addParagraphBox : index = "+  index +"\n" ) ;


		ParagraphBox parBox = new ParagraphBox(p) ;
		labels.add(parBox, index) ;
		paragraphCount ++ ;
		resizeParagraph(index) ;
		repaint() ;
	}
	
	public void removeParagraphBox(int index) {
		labels.remove(index) ;
		paragraphCount -- ;
	}
	
	public void lock(int index, int clientId) {
		ParagraphBox parBox = (ParagraphBox) labels.getComponent(index) ;
		parBox.setColor(clients.getClient(clientId).getLightColor()) ;
	}
	
	public void tempLock(int index) {
		ParagraphBox parBox = (ParagraphBox) labels.getComponent(index) ;
		parBox.setColor(Color.GRAY) ;
	}
	
	public void unlock(int index) {
		ParagraphBox parBox = (ParagraphBox) labels.getComponent(index) ;
		parBox.setColor(Color.WHITE) ;
	}
	
	public void resizeAll() {
		
		int endOffset = textPane.getDocument().getEndPosition().getOffset() - 1 ;
		
		for(int i = paragraphCount - 1; i >= 0 ; i--) {
			ParagraphBox p = (ParagraphBox) labels.getComponent(i) ;
			p.updateSize(endOffset) ;
			endOffset = p.getParagraph().getOffset() - 1 ;
		}
	}
	
	public void resizeParagraph(int index) {

		if (ParagraphPanel_Debug)
			System.out.print("\n\nParagraphPanel-->resizeParagraph : index = "+  index +"\n" ) ;

		ParagraphBox p = (ParagraphBox) labels.getComponent(index) ;
		int endOffset ;
		
		if (index == paragraphCount - 1) 
			endOffset = textPane.getDocument().getEndPosition().getOffset() - 1 ;
		else {
			ParagraphBox p2 = (ParagraphBox) labels.getComponent(index + 1) ;
			endOffset = p2.getParagraph().getOffset() - 1 ;
		}
		
		p.updateSize(endOffset) ;
	}
	
	public void showLockIndicator(int index, Color c) {
		ParagraphBox p = (ParagraphBox) labels.getComponent(index) ;
		p.setColor(c) ;
	}
	
	public void hideLockIndicator(int index) {
		ParagraphBox p = (ParagraphBox) labels.getComponent(index) ;
		p.setColor(Color.WHITE) ;
	}
	
	private class ParagraphBox extends Box {
		
		JLabel name ;
		JLabel lock ;
		Paragraph paragraph ;
		
		public Paragraph getParagraph() { return paragraph ; }
		
		public ParagraphBox(Paragraph p) {
			super(BoxLayout.Y_AXIS) ;
			paragraph = p ;
			
			String parID =  p.toString() ;
			
			if ( parID.equals("1") )
				name= new JLabel(" ");
			else			
				name = new JLabel("p" + parID) ;
			
			if (showNumbers)
			    name.setForeground(new Color(150, 150, 150)) ;
			else
			    name.setForeground(Color.WHITE) ;
			
			name.setFont(new Font("Times", Font.ITALIC, 8)) ;
			name.setMaximumSize(new Dimension(10000, 10)) ;
			
			lock = new JLabel() ;
			lock.setBackground(Color.WHITE) ;
			lock.setOpaque(true) ;
			
			add(name) ;
			add(lock) ;
		}
		
		public void updateSize(int endOffset) {
			int height = 15 ; // default value
			
			try {
				Rectangle start = textPane.modelToView(paragraph.getOffset()) ;
				Rectangle end = textPane.modelToView(endOffset) ;
				
				height = new Double(end.getY() - start.getY() + end.getHeight()).intValue() ;

			} catch(Exception e) {
				if (ParagraphPanel_Debug)
					System.out.println("Paragraph: updateSize: error calculating height") ;
			}
			lock.setSize(new Dimension(lockWidth, height)) ;
			lock.setPreferredSize(new Dimension(lockWidth, height)) ;
			lock.setMinimumSize(new Dimension(lockWidth, height)) ;
			lock.setMaximumSize(new Dimension(lockWidth, height)) ;
		}
		
		public void setColor(Color c) {
			lock.setBackground(c) ;
		}
		
		public void setShowNumber(boolean showNumber) {
		    if (showNumber)
		        name.setForeground(new Color(150, 150, 150)) ;
		    else
		        name.setForeground(Color.WHITE) ;
		}
		
	}
}


