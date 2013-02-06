package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.datatransfer.Clipboard ;
import java.util.* ;
import java.awt.image.* ;
import cowrat.recoled.client.Highlights.* ;
import cowrat.recoled.client.Clients.* ;
import cowrat.recoled.shared.MyImageIcon ;

public class DocumentView extends JPanel implements EditorDebugFlags {
    
    private EditorClient parent ;
    private EditorDocument document ;
    private Clients clients ;
    private Paragraphs paragraphs ;
    private Gestures gestures ;
    private LockManager lockManager ;
    private Highlights highlights ;
    private DefaultEditorKit editorKit ;
    private boolean isPrimary ;
    
    private ParagraphPanel paragraphPanel ;
    private JTextPane textPane ;
    private DrawArea drawArea ;
    private JLayeredPane layers ;
    private JScrollPane normalScroll ;
    private SharedScrollPanel sharedScrolls ;
    private ScrollMarkers scrollMarkers ;
    private JPanel textBox ;
    
    private Highlights.HighlightType currHighlightType ;
    private Vector selectedHighlights ;
    
    private boolean drawMode ;
    
    private float lastScrollPercent ;
    private int lastScrollValue ;
    
    private Color normalSelectionColor ;
    private Cursor highlightCursor, drawCursor ;
    
    private java.util.Timer followTimer ;
    private double followX ;
    private int followStart ;
    private int followEnd ;
    private int pointX, pointY ;
    
    Client followingClient ;
    boolean followingSomebody ;
    JLabel lblFollowingClient ;
    
    public boolean ignoreScroll ;
    
    public Clients.Client getFolowingClient() {
    	return followingClient ;
    }
    
    public JScrollBar getVerticalScroll() {
        return normalScroll.getVerticalScrollBar() ;
    }
    
    public void setFollowingClient(Clients.Client client) {
    	
    	if(followingClient != null) {
    		followingClient.setFollowed(false) ;
    		followTimer.cancel() ;
    	}
    	
    	followingClient = client ;
    	
    	if (client == null) {
    		lblFollowingClient.setVisible(false) ;
    		scrollMarkers.setShowPacker(0) ;
    		sharedScrolls.setShowPacker(0) ;
    	} else {
    		lblFollowingClient.setBackground(client.getLightColor()) ;
    		lblFollowingClient.setBorder(new LineBorder(client.getMainColor()));
    		lblFollowingClient.setText("  Following " + client.getName()) ;
    		lblFollowingClient.setVisible(true) ;
    		scrollMarkers.setShowPacker(18) ;
    		sharedScrolls.setShowPacker(18) ;
    	}
    }
    
    public void followClientTo(final double scrollProportion) {
    	//System.out.println("Following client to: " + String.valueOf(scrollProportion)) ;
    	
    	
    	followX = 0 ;
    	followStart = normalScroll.getVerticalScrollBar().getValue() ;
    	followEnd = (int) (scrollProportion * normalScroll.getVerticalScrollBar().getMaximum()) ;
    	
    	if (followTimer != null)
    		followTimer.cancel() ;
    	
    	followTimer = new java.util.Timer()  ;    	
    	followTimer.scheduleAtFixedRate(new TimerTask() {
    		
    		public void run() {
    			
    			if (followX >= Math.PI)
    				followTimer.cancel() ;
    			else {
    				followingSomebody = true ;
    				followX = followX + 0.1 ;
    				double y = (0.5 * (Math.sin(followX-(Math.PI/2)))) + 0.5 ;
    				int pos = (int) Math.round(followStart + (y*(followEnd - followStart))) ;
    				normalScroll.getVerticalScrollBar().setValue(pos) ;
    				//paintImmediately(new Rectangle(0,0, getWidth(), getHeight())) ;
    				followingSomebody = false ;
    			}
    		}
    	}, 0, 50) ;
    	
    }
    
    public void grabFocus() {
        if (drawMode)
            super.grabFocus() ;
        else
            textPane.grabFocus() ;
    }
    
    public void setShowParagraphNumbers(boolean spn) {
        paragraphPanel.setShowParagraphNumbers(spn) ;
    }
    
    public Action getCutAction() {
        String name = DefaultEditorKit.cutAction ;
        
        Action actions[] = editorKit.getActions() ;
        for (int i=0 ; i<actions.length ; i++) {
            //System.out.println(name + " ---- " + actions[i].getValue(Action.NAME)) ;
            if (name.equals(actions[i].getValue(Action.NAME)))
                return actions[i] ;
        }
        
        return null ;
    }
    
    public Action getCopyAction() {
        String name = DefaultEditorKit.copyAction ;
        
        Action actions[] = editorKit.getActions() ;
        for (int i=0 ; i<actions.length ; i++)
            if (name.equals(actions[i].getValue(Action.NAME)))
                return actions[i] ;
        
        return null ;
    }
    
    public Action getPasteAction() {
        String name = DefaultEditorKit.pasteAction ;
        
        Action actions[] = editorKit.getActions() ;
        for (int i=0 ; i<actions.length ; i++)
            if (name.equals(actions[i].getValue(Action.NAME)))
                return actions[i] ;
        
        return null ;
    }
    
    public Interface getFrame() {
        return parent.getGUI() ;
    }
    
    
    public int getVisibleHeight() {
        return normalScroll.getViewport().getHeight() ;
    }
    
    public int getVisibleCenter() {
        return normalScroll.getViewport().getViewPosition().y + (normalScroll.getViewport().getHeight()/2) ;
    }
    
    public void paintImmediately() {
        
        paintImmediately(scrollMarkers.getBounds()) ;
    }
    
    public boolean isVisible(int yValue) {
        int y1 = normalScroll.getViewport().getViewPosition().y ;
        int y2 = y1 + normalScroll.getViewport().getHeight() ;
        return (yValue >= y1 && yValue <= y2) ;
    }
    
    public int getVerticalPosition(long parId, int y){
        
        try {
			//System.out.println("\nDocumentView-->getVerticalPosition : parId : "+ parId) ;
            Paragraph par = lockManager.getParFromId(parId) ;
            return (int) textPane.modelToView(par.getOffset()).getY() + y ;  
        } catch(Exception e) {
            System.out.println("DocumentView: getVerticalPosition. error") ;
            return -1 ;
        }
    }
    
    public Paragraph getParFromVerticalPosition(int y) {
		//System.out.println("\nDocumentView-->getVerticalPosition : y : "+ y) ;
        int offset = textPane.viewToModel(new Point(0, y)) ;
        
        return lockManager.getParFromOffset(offset) ;
    }
    
    public void setDrawMode(boolean drawMode) {
        this.drawMode = drawMode ;
        
        if (!drawMode) {
            layers.moveToFront(textBox) ;
            layers.setCursor(new Cursor(Cursor.TEXT_CURSOR)) ;
            textBox.setCursor(new Cursor(Cursor.TEXT_CURSOR)) ;
            textPane.setCursor(new Cursor(Cursor.TEXT_CURSOR)) ;
            drawArea.setAllowClick(false) ;
        }
        else {
            layers.moveToFront(drawArea) ;
            layers.setCursor(drawCursor) ;
            drawArea.setAllowClick(true) ;
        }
    }
    
    public void addGestureCircle(int clientId, long parId, int id, int x, int y) {
        try {
            Clients.Client c = clients.getClient(clientId) ;
            Paragraph par = lockManager.getParFromId(parId) ;
            c.lastGestureId = id ;
            c.lastGesturePar = parId ;
            c.lastGestureParY = (int) textPane.modelToView(par.getOffset()).getY() ;
            c.lastGestureY = y ;
            c.lastGestureVisible = isVisible(c.lastGestureParY + y) ;
            
            if (c.lastGestureVisible) {
                gestures.addCircle(c.getMainColor(), parId, x, y) ;
            } else {
                addUnplayedGesture(new Gesture(c.getMainColor(), c.getLightColor(), parId, x, y)) ;
                System.out.println("Gesture Hidden " + String.valueOf(id)) ;
            }
        } catch(Exception e) {
            System.out.println("DocumentView: addGestureCircle. error") ;
            e.printStackTrace() ;
        }   
    }
    
    public void addGestureLine(int clientId, long parId, int id, int ax, int ay, int bx, int by){
        
        boolean newGesture = false ;
        
        try {
            Clients.Client c = clients.getClient(clientId) ;
            if(c.lastGestureId != id) {
                newGesture = true ;
                //System.out.println("resetting par records") ;
                Paragraph par = lockManager.getParFromId(parId) ;
                c.lastGestureId = id ;
                c.lastGesturePar = parId ;
                c.lastGestureParY = (int) textPane.modelToView(par.getOffset()).getY() ;
                c.lastGestureY = ay ;
                c.lastGestureVisible = isVisible(c.lastGestureParY + ay) ;
            }
            
            if (c.lastGestureVisible) {
                gestures.addLine(c.getMainColor(), parId, ax, ay, bx, by) ;
            } else {
                if(newGesture) {
                    Gesture g = new Gesture(c.getMainColor(), c.getLightColor(), parId, ax, ay) ;
                    g.addPoint(bx, by) ;
                    addUnplayedGesture(g) ;
                    c.lastGesture = g ;
                }
                else
                    c.lastGesture.addPoint(bx, by) ;
                
                //System.out.println("Gesture Hidden") ;
                //System.out.println("Viewport starts at: " + String.valueOf(normalScroll.getViewportBorderBounds().getY())) ;
            }
        } catch(Exception e) {
            System.out.println("DocumentView: addGestureLine. error") ;
            e.printStackTrace() ;
        }   
    }
    
    public void addUnplayedGesture(Gesture g) {
        scrollMarkers.addGestureMarker(g) ;
    }
    
    
    public void playGesture(Gesture g) {
        gestures.playGesture(g) ;
    }
    
    public void sendScrollUpdate() {
        JScrollBar s = normalScroll.getVerticalScrollBar() ;
        parent.scrollMoved(s.getValue(), s.getVisibleAmount(), s.getMaximum(), true) ;
    }
    
    public DocumentView(EditorClient f, EditorDocument d, Clients c, Paragraphs p, Gestures g, LockManager l, Highlights h, boolean i) {
        super() ;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
        setBorder(new EtchedBorder()) ;
        
        parent = f ;
        document = d ;
        clients = c ;
        paragraphs = p ;
        gestures = g ;
        lockManager = l ;
        highlights = h ;      
        
        isPrimary = i ;
        drawMode = false ;
        selectedHighlights = new Vector() ;
        
        layers = new JLayeredPane() ;
        layers.setLayout(new OverlayLayout(layers)) ;
        
        lblFollowingClient = new JLabel("Following nobody") ;
        
        lblFollowingClient.setOpaque(true) ;
        lblFollowingClient.setVisible(false) ;
        
        textPane = new JTextPane(document) {
            public Dimension getMaximumSize() {
                return new Dimension(normalScroll.getWidth() - 16, 1000000);
            }
            public Dimension getMinimumSize() {
                return new Dimension(normalScroll.getWidth() - 16, 5);
            }
            
        } ;
        textPane.setMargin(new Insets(5,20,5,5));
        textPane.setOpaque(false) ;
        textPane.setPreferredSize(new Dimension(364,400)) ;
        textPane.setMinimumSize(new Dimension(364, 10)) ;
        textPane.setMaximumSize(new Dimension(364, 1000000000)) ;

        textPane.addMouseListener(new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3){
                    int pos = textPane.viewToModel(new Point(e.getX(), e.getY())) ;
                    
                    int dot = textPane.getCaret().getDot() ;
                    int mark = textPane.getCaret().getMark() ;
                    
                    int a = Math.min(dot, mark) ;
                    int b = Math.max(dot, mark) ;
                    
                    if (pos < a || pos > b) {
                        textPane.setCaretPosition(pos) ;
                        a = pos ;
                        b = pos ;
                    }
                    
                    selectedHighlights = highlights.getHighlights(a, b) ;
                    JPopupMenu popupMenu = getPopupMenu() ;
                    pointX = e.getX() ;
                    pointY = e.getY() ;
                    popupMenu.show(textPane, e.getX(), e.getY()) ;
                }
            }
        }) ;
        
        textPane.addCaretListener(new CaretListener() {
        	public void caretUpdate(CaretEvent caretEvent)
			{
        	int location = caretEvent.getDot();
        	
			if ( DocumentView_Debug )
				System.out.print("\n>>>>DocView : CARET listening @ : " + location );

			int parIndex = paragraphs.getParIndexFromOffset(location);
			
			if ( DocumentView_Debug )
			System.out.print("\n>>>>DocView : index of Par is : " + parIndex );

			Paragraph currPar = paragraphs.getParFromIndex( parIndex );
			Paragraph nextParagraph = null ;

			if (parIndex < paragraphs.size() - 1)
				{nextParagraph = paragraphs.getParFromIndex (parIndex + 1) ;}

			try{
   				String DocumentContent = document.getText( 0, document.getLength() );
   				String ParContent="";

				int currParOffs = currPar.getOffset();
				int nextParOffs = -1;
	
				//currPar is not the last Paragraph

				if ( parIndex < paragraphs.size() - 1)
					{
					nextParOffs = nextParagraph.getOffset();		
					}

			else
         		{  nextParOffs = document.getLength(); }
	           	
			ParContent = ( DocumentContent.substring ( currParOffs , nextParOffs  ) ).trim();
			//System.out.print("\n>>>>DocViewContent of THIS paragraph is : \n\n***" +  ParContent+"***\n\n" ) ;
	
			if ( ParContent .equals("") )
				{
				paragraphs.setCaretInEmptyParagraph(true);
				if ( DocumentView_Debug )
					System.out.print("\n>>>>DocViewContent  THIS paragraph empty : TRUE"  ) ;
				}
		else
   				{
				paragraphs.setCaretInEmptyParagraph(false);
				if ( DocumentView_Debug )
					System.out.print("\n>>>>DocViewContent  THIS paragraph empty : FALSE"  ) ;

				if (location>1)
					{
					String textBeforeCaret = ( DocumentContent.substring (currPar.getOffset() ,  location ) ).trim();
					String textAfterCaret = ( DocumentContent.substring (location , nextParOffs) ).trim();
					
					if ( DocumentView_Debug )
						{
						System.out.print("\n>>>>DocViewContent  text BEFORE CARET :  \n\n**"+ textBeforeCaret + "**\n\n"  ) ;
						System.out.print("\n>>>>DocViewContent  text AFTER CARET :  \n\n**"+ textAfterCaret + "**\n\n" ) ;
						}
					}
				}//endelse
     	 	}//endtry

   	catch  ( Exception bde ) {System.out.print("\n>>>>DocView->caretUpdate : EXCEPTION");}

        
        	}
        }) ;
        
        g.setTextPane(textPane) ;
        editorKit = (DefaultEditorKit)textPane.getEditorKit() ;
        
        textBox = new JPanel() ;
        textBox.setLayout(new BorderLayout()) ;
        textBox.setBackground(new Color(0, 0, 0, 0)) ;
        textBox.setOpaque(false) ;
        textBox.add(textPane, BorderLayout.WEST) ;
        textBox.add(Box.createGlue(), BorderLayout.CENTER) ;
        //textBox.add(lblFollowingClient, BorderLayout.NORTH) ;
        textBox.setCursor(textPane.getCursor()) ;
        
        paragraphPanel = new ParagraphPanel(textPane, clients) ;
        paragraphs.addParagraphPanel(paragraphPanel) ;
        
        drawArea = new DrawArea(parent, gestures, clients, textPane) ;
        gestures.addDrawArea(drawArea) ;
        
        layers.add(paragraphPanel, new Integer(0)) ;
        layers.add(textBox, new Integer(1)) ;
        layers.add(drawArea, new Integer(1)) ;
        
        
        normalScroll = new JScrollPane(layers) ;
        //normalScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) ;
        normalScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS) ;
        normalScroll.setBorder(null) ;
        
        normalScroll.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		setFollowingClient(null) ;
        	}
        });
        
        normalScroll.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
            	setFollowingClient(null) ;
                int value = normalScroll.getVerticalScrollBar().getValue() ;
                normalScroll.getVerticalScrollBar().setValue(value + (10 * e.getUnitsToScroll())) ;
                
            }
        }) ;
        
        
        JPanel textPanel = new JPanel() ;
        textPanel.setLayout(new BorderLayout()) ;
        textPanel.setOpaque(false) ;
        textPanel.add(lblFollowingClient, BorderLayout.NORTH) ;
        textPanel.add(normalScroll, BorderLayout.CENTER) ;
        
        scrollMarkers = new ScrollMarkers(normalScroll.getVerticalScrollBar(), this) ;
        
        sharedScrolls = new SharedScrollPanel()  ;
        clients.addSharedScrollPanel(sharedScrolls) ; 
        
        add(textPanel) ;
        add(scrollMarkers) ;
        add(sharedScrolls) ;
        
        normalScroll.getVerticalScrollBar().addAdjustmentListener( new AdjustmentListener() {
        	private int lastValue = 0 ;
        	
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(ignoreScroll) {
                    ignoreScroll = false ;
                    normalScroll.getVerticalScrollBar().setValue(lastValue) ;
                } else {
                    int value = e.getValue() ;
	                int view = normalScroll.getVerticalScrollBar().getVisibleAmount() ;
	                int max = normalScroll.getVerticalScrollBar().getMaximum() ;
	                
	                float diff = lastScrollPercent - (value*100/max) ;
	                //System.out.println("Old: " + String.valueOf(lastScrollPercent) + " New: " + String.valueOf(value*100/max) + " Diff" + String.valueOf(diff)) ;
	                if (diff < -1 || diff > 1) {
	                    lastScrollPercent = (value*100/max) ;
	                    parent.scrollMoved(value, view, max, isPrimary) ;
	                }
	                
	                //parScroll.getVerticalScrollBar().setValue(e.getValue()) ;                
	                lastValue = value ;
                }
            }
        }) ;
        
        highlights.setHighlighter((DefaultHighlighter)textPane.getHighlighter()) ;
        normalSelectionColor = textPane.getSelectionColor() ;
        highlightCursor = getCustomCursor(new MyImageIcon("images/highlightCursor.gif").getImage(), "highlight cursor", new Point(0, 0)) ;
        drawCursor = getCustomCursor(new MyImageIcon("images/point.gif").getImage(), "gesture cursor", new Point(0, 0)) ;
        
        
    }
    
    public Cursor getCustomCursor(Image image, String name, Point offset) {
        Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(20, 20) ;
        BufferedImage bi = new BufferedImage((int)d.getWidth(), (int)d.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = bi.getGraphics();
        g.drawImage(image, 0, 0, null);
        return Toolkit.getDefaultToolkit().createCustomCursor(bi, offset, name); 
    }
    
    public JPopupMenu getPopupMenu() {
        
        JPopupMenu popupMenu = new JPopupMenu() ;
        
        Iterator i = getEditMenuItems().iterator() ;
        while (i.hasNext()) {
            popupMenu.add((JMenuItem)i.next()) ;
        }
        popupMenu.addSeparator() ;
        
        JMenuItem mnuPoint = new JMenuItem("Point") ;
        mnuPoint.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
	           if ( DocumentView_Debug )
               		System.out.println("Point:(" + pointX + ", " + pointY + ")" + drawArea.toString()) ;
               drawArea.point(pointX, pointY) ;
           }
        });
        popupMenu.add(mnuPoint) ;
        popupMenu.addSeparator() ;
        
        i = getKeywordMenuItems(false).iterator() ;
        while (i.hasNext()) {
            popupMenu.add((JMenuItem)i.next()) ;
        }
        return popupMenu ;
    }
    
    public class HighlightMenuItem extends JMenuItem {
        private Highlights.HighlightType type ;
        
        public HighlightMenuItem(Highlights.HighlightType t){
            super(t.getName()) ;
            type = t ;
            
            BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB) ;
            Graphics g = bi.getGraphics() ;
            g.setColor(Color.WHITE) ;
            g.fillRect(0, 0, 15, 15) ;
            g.setColor(type.getColor()) ;
            g.fillRect(0, 0, 15, 15) ;
            g.setColor(Color.BLACK) ;
            g.drawRect(0, 0, 14, 14) ;
            setIcon(new ImageIcon(bi)) ;
            
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addHighlight(type) ;
                }
            }) ;
        }
    }
    
    public void addHighlight(Highlights.HighlightType t) {
        try {
            int dot = textPane.getCaret().getDot() ;
            int mark = textPane.getCaret().getMark() ;
            
            int a = Math.min(dot, mark) ;
            int b = Math.max(dot, mark) ;
            
            while (a > 0 && Character.isLetterOrDigit((document.getText(a-1, 1).charAt(0))))
                a -- ;
            
            while (b < document.getLength() && Character.isLetterOrDigit((document.getText(b, 1).charAt(0))))
                b ++ ;
            
            if(a != b)
                highlights.addHighlight(a, b, t) ;
            
        } catch (Exception e) {
            System.out.println("DocumentView: addHighlight. error") ;
            e.printStackTrace() ;
        }
    }
    
    public JPopupMenu getKeywordMenu() {
        int dot = textPane.getCaret().getDot() ;
        int mark = textPane.getCaret().getMark() ;
        
        int a = Math.min(dot, mark) ;
        int b = Math.max(dot, mark) ;
        
        selectedHighlights = highlights.getHighlights(a, b) ;
        JPopupMenu menu = new JPopupMenu() ;
        Iterator i = getKeywordMenuItems(true).iterator() ; ;
        while (i.hasNext()) {
            menu.add((JMenuItem) i.next()) ;
        }
        
        
        return menu ;        
    }
    
    public Vector getEditMenuItems() {
        Vector v = new Vector() ;
        
        JMenuItem mnuCut = new JMenuItem(getCutAction()) ;
        mnuCut.setText("Cut") ;
        v.add(mnuCut) ;
        JMenuItem mnuCopy = new JMenuItem(getCopyAction()) ;
        mnuCopy.setText("Copy") ;
        v.add(mnuCopy) ;
        JMenuItem mnuPaste = new JMenuItem(getPasteAction()) ;
        mnuPaste.setText("Paste") ;
        v.add(mnuPaste) ;
        
        return v ;
    }
    
    private class FollowMenuItem extends JMenuItem {
        private Client client ;
        
        public FollowMenuItem(Client c) {
            super() ;
            this.client = c ;
            BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB) ;
            Graphics g = bi.getGraphics() ;
            g.setColor(client.getLightColor()) ;
            g.fillRect(0, 0, 15, 15) ;
            g.setColor(Color.BLACK) ;
            g.drawRect(0, 0, 14, 14) ;
            
            setIcon(new ImageIcon(bi)) ;
            if(client.equals(followingClient)) 
                setText("Stop following " + client.getName()) ;
            else
                setText("Start following " + client.getName()) ;
            
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (client.equals(followingClient)) {
                        setFollowingClient(null) ;
                        client.setFollowed(false) ;
                    } else {
                        setFollowingClient(client) ;
                        client.setFollowed(true) ;
                        followClientTo(client.getScrollProportion()) ;
                    }
                }                    
            }) ;
        }
    }
    
    public Vector getFollowMenuItems() {
        Vector v = new Vector() ;
        
        Iterator i = clients.getOthers().iterator() ;
        while (i.hasNext()) {
            Client c = (Client)i.next() ;
            if (c.isPresent())
                v.add(new FollowMenuItem(c)) ;                       
        }
        return v ;
    }
    
    public Vector getKeywordMenuItems(boolean withDefine) {
        
        Vector v = new Vector() ;
        
        JMenu mnuAdd = new JMenu("Add Keyword") ;
        v.add(mnuAdd) ;
        
        JMenuItem mnuRemove = new JMenuItem("Remove Keyword") ;
        if (selectedHighlights.isEmpty())
            mnuRemove.setEnabled(false) ;
        if (selectedHighlights.size() > 1)
            mnuRemove.setText(mnuRemove.getText() + "s") ;
        
        mnuRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Iterator i = selectedHighlights.iterator() ;
                while (i.hasNext()) {
                    Highlights.Highlight h = (Highlights.Highlight)i.next() ;
                    highlights.deleteHighlight(h) ;
                }
            }
            
        }) ;
        v.add(mnuRemove) ;
        
        if(withDefine) {
            JMenuItem mnuDefine = new JMenuItem("Define Keyword...") ;
            mnuDefine.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    KeywordTypeDialog dialog = new KeywordTypeDialog() ;
                    dialog.pack() ; 
                    dialog.setResizable(false) ;
                    dialog.show() ;
                    
                    HighlightType type = dialog.getKeywordType() ;
                    if (type != null){
                        parent.sendHighlightTypeAdded(type.getId(), type.getName(), type.getColor()) ;
                    }
                }
            }) ;
            v.add(mnuDefine) ;
        }
        
        Iterator i = highlights.getHighlightTypes().iterator() ;
        while (i.hasNext()) {
            Highlights.HighlightType t = (Highlights.HighlightType) i.next();
            HighlightMenuItem hmi = new HighlightMenuItem(t) ;
            mnuAdd.add(hmi) ;
        }
        
        mnuAdd.addSeparator() ;
        
        JMenuItem newType = new JMenuItem("New Keyword..") ;
        BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_RGB) ;
        Graphics g = bi.getGraphics() ;
        g.setColor(Color.WHITE) ;
        g.fillRect(0, 0, 15, 15) ;
        g.setColor(Color.BLACK) ;
        g.drawRect(0, 0, 14, 14) ;
        newType.setIcon(new ImageIcon(bi)) ;
        newType.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               KeywordTypeDialog dialog = new KeywordTypeDialog() ;
               
               dialog.pack() ; 
               dialog.setResizable(false) ;
               dialog.show() ;
               
               HighlightType type = dialog.getKeywordType() ;
               if (type != null){
                   HighlightMenuItem hmi = new HighlightMenuItem(type) ;
                   parent.sendHighlightTypeAdded(type.getId(), type.getName(), type.getColor()) ;
                   addHighlight(type) ;
               }
           }
        });
        
        mnuAdd.add(newType) ;
        
        return v ;        
    }
    
    public class KeywordTypeDialog extends JDialog{
        
        private String name ;
        private Color color ;
        
        JButton cmdOk, cmdCancel ;
        
        private JTextField txtName ;
        private Vector colorButtons ;
        private ColorButton selectedColorButton ;
        
        private boolean confirmed ;
        
        public HighlightType getKeywordType() {
            if (confirmed) {
                String name = txtName.getText() ;
                Color color = selectedColorButton.getColor() ;
                return highlights.addHighlightType(name, color) ;
            } else
                return null ;
        }
        
        public KeywordTypeDialog() {
            super(getFrame(), true) ;
            //center
  
            
            setTitle("New Keyword Type") ;
            
            Box boxName = new Box(BoxLayout.X_AXIS) ;
            JLabel lblName = new JLabel("Name:") ;
            txtName = new JTextField() ;
            txtName.setMaximumSize(new Dimension(1000000, 20)) ;
            txtName.addCaretListener(new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    if(txtName.getText().length() > 0)
                        cmdOk.setEnabled(true) ;
                    else
                        cmdOk.setEnabled(false) ;
                }
            }) ;
            boxName.add(lblName) ;
            boxName.add(Box.createHorizontalStrut(5)) ;
            boxName.add(txtName) ;
            
            colorButtons = new Vector() ;
            colorButtons.add(new ColorButton(new Color(240,30,35, 100))) ;
            colorButtons.add(new ColorButton(new Color(240,100,35, 100))) ;
            colorButtons.add(new ColorButton(new Color(245,150,30, 100))) ;
            colorButtons.add(new ColorButton(new Color(255,240,0, 100))) ;
            colorButtons.add(new ColorButton(new Color(140,200,60, 100))) ;
            colorButtons.add(new ColorButton(new Color(55,180,75, 100))) ;
            colorButtons.add(new ColorButton(new Color(0,165,80, 100))) ;
            colorButtons.add(new ColorButton(new Color(0,170,160, 100))) ;
            colorButtons.add(new ColorButton(new Color(0,175,240, 100))) ;
            colorButtons.add(new ColorButton(new Color(0,115,190, 100))) ;
            colorButtons.add(new ColorButton(new Color(0,85,165, 100))) ;
            colorButtons.add(new ColorButton(new Color(50,50,145, 100))) ;
            colorButtons.add(new ColorButton(new Color(100,45,145, 100))) ;
            colorButtons.add(new ColorButton(new Color(145,40,145, 100))) ;
            colorButtons.add(new ColorButton(new Color(240,0,140, 100))) ;
            colorButtons.add(new ColorButton(new Color(245,15,90, 100))) ;
            
            Iterator iter = colorButtons.iterator() ;
            while (iter.hasNext()) {
                ColorButton c = (ColorButton) iter.next() ;
                if (c.enabled) {
                    c.setSelected(true) ;
                    break ;
                }
            }
            
            Box boxColors1 = new Box(BoxLayout.X_AXIS) ;
            for (int i = 0 ; i < 8 ; i ++ ){
                boxColors1.add((ColorButton)colorButtons.elementAt(i)) ;
            }
            
            
            Box boxColors2 = new Box(BoxLayout.X_AXIS) ;
            for (int i = 8 ; i < 16 ; i ++ ){
                boxColors2.add((ColorButton)colorButtons.elementAt(i)) ;
            }
            
            cmdOk = new JButton("OK") ;
            cmdOk.setEnabled(false) ;
            cmdOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    confirmed = true ;
                    dispose() ;
                }
            }) ;
            cmdCancel = new JButton("Cancel") ;
            cmdCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    confirmed = false ;
                    dispose() ;
                }
            }) ;
            
            Box boxButtons = new Box(BoxLayout.X_AXIS) ;
            boxButtons.add(cmdOk) ;
            boxButtons.add(Box.createHorizontalStrut(5)) ;
            boxButtons.add(cmdCancel) ;
            
            Box boxAll = new Box(BoxLayout.Y_AXIS) ;
            boxAll.setBorder(new EmptyBorder(5,5,5,5)) ;
            
            boxAll.add(boxName) ;
            boxAll.add(Box.createVerticalStrut(10)) ;
            boxAll.add(boxColors1) ;
            boxAll.add(boxColors2) ;
            boxAll.add(Box.createVerticalStrut(10)) ;
            boxAll.add(boxButtons) ;
            
            getContentPane().add(boxAll, BorderLayout.CENTER) ;
        }
        
        public class ColorButton extends JPanel {
            
            private Color color ;
            private boolean selected ;
            private boolean enabled ;
            
            public Color getColor() {
                return color ;
            }
            
            public ColorButton(Color c) {
                color = c ;
                enabled = true ;
                
                Iterator i = highlights.getUsedColors().iterator() ;
                while (i.hasNext())
                    if (c.equals(i.next()))
                        enabled = false ;
                    
                setPreferredSize(new Dimension(25, 25)) ;
                setMaximumSize(new Dimension(25, 25)) ;
                setMinimumSize(new Dimension(25, 25)) ;
                
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (enabled & !selected) {
                            setSelected(true) ;
                        }
                    }
                }) ;
            }
            
            public void paint(Graphics g) {
                if (selected) {
                    g.setColor(Color.WHITE) ;
                    g.fillRect(0, 0, 25, 25) ;
                    g.setColor(color) ;
                    g.fillRect(0, 0, 25, 25) ;
                    
                    g.setColor(Color.BLACK) ;
                    g.drawRect(0, 0, 24, 24) ;
                } else {
                    g.setColor(getBackground()) ;
                    g.fillRect(0, 0, 25, 25) ;
                    g.setColor(Color.WHITE) ;
                    g.fillRect(5, 5, 15, 15) ;
                    g.setColor(color) ;
                    g.fillRect(5, 5, 15, 15) ;
                    
                    if (enabled) {
                        g.setColor(Color.BLACK) ;
                        g.drawRect(5, 5, 14, 14) ;
                    } else {
                        g.setColor(new Color(200, 200, 200, 100)) ;
                        g.fillRect(5, 5, 14, 14) ;
                        g.setColor(Color.GRAY) ;
                        g.drawRect(5, 5, 14, 14) ;
                    }
                }
                
            }
            
            public void setSelected(boolean b) {
                selected = b ;
                
                if (selected) {
                    if (selectedColorButton != null)
                        selectedColorButton.setSelected(false) ;
                    
                    selectedColorButton = this ;
                }
                
                repaint() ;
                
            }
            
        }
        
    }
}





























