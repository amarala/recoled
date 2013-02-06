package cowrat.recoled.server;

import java.awt.Color;
import java.io.Serializable;

/**
 * @author David King
 *
 */

public class HighlightType implements Serializable{
    private int id ;
    private String name ;
    private Color color ;

    public int getId() {return id ; }
    public String getName() { return name ; }
    public Color getColor() { return color ; }
    
    public HighlightType(int id, String n, Color c) {
        this.id = id ;
        name = n ;
        color = c ;
    }
    
    public String getXML() {
        String s = "    <keywordType" ;
        s = s + " id=\"" + id + "\"" ;
        s = s + " name=\"" + SavedDocument.replaceReservedCharacters(name) + "\"" ;
        s = s + "/>\n" ;
        return s ;        
    }
}
