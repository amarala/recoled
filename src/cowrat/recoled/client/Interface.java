package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.event.* ;
import java.io.* ;
import java.util.* ;

import cowrat.recoled.shared.MyFileFilter;
import cowrat.recoled.shared.*;

import java.awt.* ;
import java.awt.datatransfer.*;
import java.awt.event.* ;

public class Interface extends JFrame {
	
	// Constants.......................................................................
	static final String newline = "\n";
	
	// Variables......................................................................
	private JTextArea messageArea ;
	
	private JPanel shortcutBar ;
	
	private Action actLoad, actSave, actPrint, actCut, actCopy, actPaste, actExit ;
	//private JSplitPane splitPane ;
	
	private EditorClient parent ;
	private Clients clients ;
	private LockManager lockManager ;
	private EditorDocument document ;
	private Paragraphs paragraphs ;
	private Gestures gestures ;
	private boolean drawing ;
	private boolean showParagraphNumbers ;
	
	private DocumentView mainView ;
	
	public DocumentView getMainView() {
		return mainView ;
	}
	/*
	public boolean canPaste() {
		if (drawing)
			return false ;
		
		try {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard() ;
			Transferable trans = clip.getContents(null) ;
			
			if (trans == null)
				return false ;
			
			return trans.isDataFlavorSupported(DataFlavor.stringFlavor) ;
		} catch (Exception e) {
			return false ;
		}
	}
	
	
	public void setCanCutCopy(boolean b) {
		actCopy.setEnabled(b) ;
		actCut.setEnabled(b) ;
	}
	*/
	
	//private DocumentView secondaryView ;
	
	
	// Constructors....................................................................................................
	public Interface(EditorClient f, EditorDocument d, Clients c, Paragraphs p, Gestures g, LockManager l, Highlights h) {
		
		super() ;

		parent = f ;
		document = d ;
		clients = c ;
		paragraphs = p ;
		gestures = g ;
		lockManager = l;
		showParagraphNumbers = true ;

		mainView = new DocumentView(parent, document, clients, paragraphs, gestures, l, h, true) ;
		clients.setDocumentView(mainView) ;

		setTitle("EditorClient: " + clients.getMyName()) ;
		//setSize(new Dimension(300, 150)) ;
		setSize(new Dimension(500, 300)) ;
		addWindowListener(new EC_WindowAdapter()) ;
		
		shortcutBar = new JPanel() ;

		shortcutBar.setLayout(new BoxLayout(shortcutBar, BoxLayout.X_AXIS)) ;
		shortcutBar.setBorder(new EmptyBorder(5,5,5,5)) ;
				
		actLoad = new LoadAction();
		actSave = new SaveAction() ;
		actPrint = new PrintAction() ;		
		actCut = mainView.getCutAction() ;
		actCopy = mainView.getCopyAction() ;
		actPaste = mainView.getPasteAction() ;
		actExit = new ExitAction() ;
		//setCanCutCopy(false) ;
		
		shortcutBar.add(Box.createHorizontalStrut(5)) ;
		shortcutBar.add(new ShortcutButton(actLoad)) ;
		shortcutBar.add(new ShortcutButton(actSave)) ;
		shortcutBar.add(new ShortcutButton(actPrint)) ;
		

		shortcutBar.add(Box.createHorizontalStrut(20)) ;
		
		shortcutBar.add(new ShortcutButton(actCut, getImageIcon("images/cut.gif"))) ;
		shortcutBar.add(new ShortcutButton(actCopy, getImageIcon("images/copy.gif"))) ;
		shortcutBar.add(new ShortcutButton(actPaste, getImageIcon("images/paste.gif"))) ;
		
		shortcutBar.add(Box.createHorizontalStrut(20)) ;

		shortcutBar.add(new GestureButton()) ;
		shortcutBar.add(Box.createGlue()) ;
		
		Box shortcutBox = new Box(BoxLayout.Y_AXIS) ;
		shortcutBox.add(shortcutBar) ;
		shortcutBox.add(new JSeparator()) ;
		
		setJMenuBar(new MyMenuBar()) ;
		
		getContentPane().add(shortcutBox, BorderLayout.NORTH) ;
		getContentPane().add(mainView, BorderLayout.CENTER) ;
		getContentPane().add(clients, BorderLayout.EAST) ;
	}
	
	public void show() {
	    super.show() ;
	    /*
	    Thread checkPasteThread = new Thread() {
        	public void run() {
        		while (true) {
        			yield() ;
        			actPaste.setEnabled(canPaste()) ;
        			try {
        			    sleep(1000) ;
        			} catch (Exception e) {
        				System.out.println("Interface: checkPaste. error") ;
        				e.printStackTrace() ;
        			}
        		}
        	}
        } ;
        checkPasteThread.setDaemon(true) ;
        checkPasteThread.start() ;
        */
	    
	}
	
	public class MyMenuBar extends JMenuBar {
		
		private JMenu mnuFile, mnuEdit, mnuView, mnuKeywords, mnuHelp ;
		
		public MyMenuBar() {
			super() ;
			mnuFile = new JMenu("File") ;
			add(mnuFile) ;
			mnuEdit = new JMenu("Edit") ;
			add(mnuEdit) ;
			mnuView = new JMenu("View") ;
			add(mnuView) ;
			mnuKeywords = new JMenu("Keywords") ;
			add(mnuKeywords) ;
			mnuHelp = new JMenu("Help") ;
			add(mnuHelp) ;
			
			mnuFile.add(new MyMenuItem(actLoad)) ;
			mnuFile.add(new MyMenuItem(actSave)) ;
			mnuFile.add(new MyMenuItem(actPrint)) ;
			mnuFile.addSeparator() ;
			mnuFile.add(new MyMenuItem(actExit)) ;
			
			mnuEdit.add(new MyMenuItem(actCut, "Cut")) ;
			mnuEdit.add(new MyMenuItem(actCopy, "Copy")) ;
			mnuEdit.add(new MyMenuItem(actPaste, "Paste")) ;

			mnuView.addMenuListener(new MenuListener() {
			    public void menuSelected(MenuEvent e) {
			        mnuView.removeAll() ;
			        mnuView.add(new ParagraphMenuItem()) ;
			        
			        Vector followItems = mainView.getFollowMenuItems() ;
			        
			        if(!followItems.isEmpty()){
			            mnuView.addSeparator() ;
			            Iterator i = mainView.getFollowMenuItems().iterator() ;
			            while (i.hasNext())
			                mnuView.add((JMenuItem)i.next()) ;
			        }
			    }
			    public void menuDeselected(MenuEvent e) { }
			    public void menuCanceled(MenuEvent e) { }
			});
			
			
			mnuKeywords.addMenuListener(new MenuListener() {
			    JPopupMenu menu ;
			    
			    public void menuSelected(MenuEvent e) {
			        mnuKeywords.removeAll() ;
			        
			        Component c[] = mainView.getKeywordMenu().getComponents() ;
			        for (int i = 0 ; i < c.length ; i++){
			            mnuKeywords.add(c[i]) ;
			        }
			    }
			    
			    public void menuDeselected(MenuEvent e) { }
			    
			    public void menuCanceled(MenuEvent e) { }
			}) ;
		}
	}
	
	public ImageIcon getImageIcon(String path){
    java.net.URL i = ClassLoader.getSystemResource(path);  
		return new ImageIcon(i) ;
	}   

	
	public class GestureButton extends JToggleButton {
		
		public GestureButton() {
			super(getImageIcon("images/point.gif")) ;
			
			final Border normalBorder = new CompoundBorder(new EtchedBorder(), new EmptyBorder(3, 3, 3, 3)) ;
			final Border noBorder = new EmptyBorder(5, 5, 5, 5) ;
			
			setBorder(noBorder) ;
			
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					mainView.grabFocus() ;
				}
				public void mouseEntered(MouseEvent e) {
					if(isEnabled())
						setBorder(normalBorder) ;
				}
				public void mouseExited(MouseEvent e) {
					if (!isSelected())
						setBorder(noBorder) ;
				}
			}) ;
			
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//setCanCutCopy(false) ;
					if (isSelected()) {
						drawing = true ;
						mainView.setDrawMode(true) ;
					}
					else {
						drawing = false ;	
						mainView.setDrawMode(false) ;
					}
				}
			}) ;
		}		
	}
	
	protected class ParagraphMenuItem extends JMenuItem {
	    
	    
	    public ParagraphMenuItem() {
	        if (showParagraphNumbers)
	            setText("Hide paragraph numbers") ;
	        else
	            setText("Show paragraph numbers") ;
	        
	        addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                showParagraphNumbers = !showParagraphNumbers ;
	                
	                if (showParagraphNumbers) {
	                    setText("Hide paragraph numbers") ;
	                    mainView.setShowParagraphNumbers(true) ;
	                } else {
	                    setText("Show paragraph numbers") ;
	                    mainView.setShowParagraphNumbers(false) ;
	                }
	                    
	            }
	        }) ;
	    }
	    
	}
	
	protected class MyMenuItem extends JMenuItem {
	    
	    public MyMenuItem(Action a) {
	        super(a) ;
	        setIcon(null) ;
	    }
	    
	    public MyMenuItem(Action a, String name) {
	        super(a) ;
	        setText(name) ;
	    }
	    
	}
	protected class ShortcutButton extends JButton {
		
		public ShortcutButton(Action action) {
			super(action) ;
			setText(null) ;
			
			final Border normalBorder = new CompoundBorder(new EtchedBorder(), new EmptyBorder(3, 3, 3, 3)) ;
			final Border noBorder = new EmptyBorder(5, 5, 5, 5) ;
			
			setBorder(noBorder) ;
			
			
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					mainView.grabFocus() ;
				}
				
				public void mouseEntered(MouseEvent e) {
					if(isEnabled())
						setBorder(normalBorder) ;
				}
				
				public void mouseExited(MouseEvent e) {
					setBorder(noBorder) ;
				}
				
			}) ;
		}
		
		public ShortcutButton(Action action, ImageIcon icon) {
		    super(action) ;
			setText(null) ;
			setIcon(icon) ;
			
			final Border normalBorder = new CompoundBorder(new EtchedBorder(), new EmptyBorder(3, 3, 3, 3)) ;
			final Border noBorder = new EmptyBorder(5, 5, 5, 5) ;
			
			setBorder(noBorder) ;

			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					mainView.grabFocus() ;
				}
				
				public void mouseEntered(MouseEvent e) {
					if(isEnabled())
						setBorder(normalBorder) ;
				}
				
				public void mouseExited(MouseEvent e) {
					setBorder(noBorder) ;
				}
				
			}) ;
		}
	}
	
//code for LoadAction has yet to be implemented...

	protected class LoadAction extends AbstractAction {
		
		public LoadAction() {
			super("Load");
			
		}
		
		public void actionPerformed(ActionEvent e) {

		JFileChooser fchooser = new JFileChooser();

		if (fchooser.showOpenDialog(Interface.this) != JFileChooser.APPROVE_OPTION)
				return;

		File selectedFile = fchooser.getSelectedFile();

		if(selectedFile == null)
			{
			System.out.print("No File selected..." );
			return;
			}
			
		else
			{
			System.out.print("Loading File : " + selectedFile );				
			}
			
			FileReader myFileReader  = null;
			try {
				myFileReader = new FileReader(selectedFile);
			
				BufferedReader textBuffer = new BufferedReader( myFileReader ) ;
				
				StringBuffer myStringBuffer = new StringBuffer ();
				
				String loadedLine="";
				
				long parId=2;
				
				
				int offset = 11;
				
				
				while ( ( loadedLine = textBuffer.readLine() ) != null )
					{						
						//System.out.print("\nLoadedLine is : \n***" + loadedLine+"***" );
						
						System.out.println("\nEdDoc->appending text @ offset : " + offset + " for par : "+ parId + " \n*" + loadedLine + "*\n" ) ;
						
						document.appendText(loadedLine+"\n");
						
						if (!(offset == 11) )						
							{
								Paragraph p= paragraphs.addParagraph(paragraphs.size(),parId,offset);
								
								//paragraphs.resizeParagraph(p);
							}
							
							offset = offset+ loadedLine.length()+1;
						
						parId++;
						
										
						
					}
						/*
					myStringBuffer.append( loadedText + "\n" );
					
					loadedText =  "·-·+·*=·x·"+myStringBuffer.toString();		// the firstLine !!!			
					
				System.out.print("Loaded File is : \n***" + loadedText+"***" );	
				
				//insert file in document through EditorClient...
				
				parent.appendText(loadedText);
				//parent.loadText(loadedText);		
				
				*/	
				}//endTry
				
			catch (IOException ioex)
				{System.out.print("\nInterface->LoadAction : IOException  Loading File... "  ); }			
			
			////			
			
			
		}//endof actionPerformed

	}//endof LoadAction

	protected class SaveAction extends AbstractAction {
		
		public SaveAction() {
			super("Save", getImageIcon("images/save.gif")) ;
		}
		
		public void actionPerformed(ActionEvent e) {
		    JFileChooser dialog = new JFileChooser() ;
    		dialog.addChoosableFileFilter(new MyFileFilter("Text File (*.txt)", ".txt")) ;
			dialog.setAcceptAllFileFilterUsed(false) ;
			
			if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = dialog.getSelectedFile() ;
			    try {
			        file.createNewFile() ;
			    
			        BufferedWriter out = new BufferedWriter(new FileWriter(file)) ;
			        out.write(document.getText(0, document.getLength())) ;
			        out.close() ;
			    } catch (Exception x) {
			        System.out.println("Interface: SaveAction. error") ;
			        x.printStackTrace() ;
			    }
			}
		}
	}
	
	protected class PrintAction extends AbstractAction {
		
		public PrintAction() {
			super("Print", getImageIcon("images/print.gif")) ;
		}
		
		public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	protected class ExitAction extends AbstractAction {
		
		public ExitAction() {
			super("Exit", getImageIcon("images/print.gif")) ;
		}
		
		public void actionPerformed(ActionEvent e) {
		    parent.leaveSession() ;
			dispose() ;
		}
	}

	protected class EC_WindowAdapter extends WindowAdapter {
		
		public void windowClosing(WindowEvent e) {
			parent.leaveSession() ;
		}
	} 
	
}
