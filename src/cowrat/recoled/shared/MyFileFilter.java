package cowrat.recoled.shared;

import java.io.File;

/**
 * @author David King
 *
 */
public class MyFileFilter extends javax.swing.filechooser.FileFilter {
    
	String desc, ext ;
        
    public MyFileFilter(String desc, String ext) {
        this.desc = desc ;
        this.ext = ext ;
        
    }
    
	public String getDescription() {
		return desc ;
	}
	
	public boolean accept(File file) {
		if (file.isDirectory())
			return true ;
		
		return file.getName().endsWith(ext) ;
	}
	
}
