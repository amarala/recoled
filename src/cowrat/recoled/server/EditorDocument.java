package cowrat.recoled.server;

import java.awt.Color;

import javax.swing.text.* ;
import javax.swing.ImageIcon;

public class EditorDocument extends DefaultStyledDocument {
	
	private String title ;
	private String description ;
	private long startTime ;
	
	// Constants.......................................................................................................
	private static final int newline = 10 ;
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	
	public long getStartTime() {
	    return startTime ;
	}
	
	public void resetStartTime(long newTime) {
	    startTime =  newTime;
	}

	// Constructors.....................................................................................................	
	public EditorDocument(String title, String description, String text, long startTime) {
		super();
		this.title = title;
		this.description = description;
		this.startTime = startTime;
		
		ImageIcon lineIcon = new ImageIcon ("images/blueline1.gif");
		
		Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
		Style line = this.addStyle("line", def);
		
		StyleConstants.setAlignment(line, StyleConstants.ALIGN_CENTER );
		if ( lineIcon != null)
			{ StyleConstants.setIcon(line, lineIcon);}
		
		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setSpaceAbove(standard, 10);
		StyleConstants.setFontFamily(standard, "Arial");
		StyleConstants.setFontSize(standard, 14);
		StyleConstants.setForeground(standard, Color.BLACK);
		
		
		
		setParagraphAttributes(0, 0, standard, true);
		
		insertText(0, text) ;
	}
	
	// Methods..........................................................................................................
	
	/*
	 Inserts a string of text into a position in a particular paragraph.
	 Called when an insertion is made by someone other than this client
	 */
	public void insertText(final int offset, final String text) {
		
		try {
			insertString(offset, text, getDefaultRootElement().getAttributes()) ;
		} catch (Exception exception) {
			System.out.println("insert failed: " +
					" offset=" + String.valueOf(offset) + 
					" text=\"" + text + "\"" ) ;
			System.out.println(exception.getMessage()) ;
		}
		
	}
	
	/*
	 Removes all text from a position in one paragraph to a position a certian 
	 distance before the start of another paragraph.
	 Called when a deletion is made by someone other than this client
	 */   
	public void deleteText(final int offset, final int length) {
		try {
			remove(offset, length) ;				
		} catch (Exception exception) {
			System.out.println(exception.getMessage()) ;
		}
		
	}
	

	/*
	 Inserts a string of text into a position in a particular paragraph.
	 Called when an insertion is made by someone other than this client
	 */
	/*
	 public void insertText(final int offset, final String text) {
	 
	 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	 
	 public void run() {
	 try {
	 insertString(offset, text, getDefaultRootElement().getAttributes()) ;
	 } catch (Exception exception) {
	 System.out.println("insert failed: " +
	 " offset=" + String.valueOf(offset) + 
	 " text=\"" + text + "\"" ) ;
	 System.out.println(exception.getMessage()) ;
	 }
	 }
	 
	 }) ;
	 }
	 */
	/*
	 Removes all text from a position in one paragraph to a position a certian 
	 distance before the start of another paragraph.
	 Called when a deletion is made by someone other than this client
	 */
	/*
	 public void deleteText(final int offset, final int length) {
	 
	 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	 public void run() {
	 
	 try {
	 remove(offset, length) ;				
	 } catch (Exception exception) {
	 System.out.println(exception.getMessage()) ;
	 }
	 }
	 
	 }) ;
	 }
	 */
}


