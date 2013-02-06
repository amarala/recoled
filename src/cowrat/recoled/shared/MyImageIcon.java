package cowrat.recoled.shared;
import javax.swing.ImageIcon;
/**
 *  URL-loadable image icons
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: MyImageIcon.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see  
*/
public class MyImageIcon extends ImageIcon {

  public MyImageIcon (String path) {
    super(ClassLoader.getSystemResource(path));
  }


}
