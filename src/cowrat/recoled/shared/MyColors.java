package cowrat.recoled.shared;

import java.awt.Color ;

public class MyColors {
	
    public static final int BLUE = 1 ;
    public static final int RED = 2 ;
    public static final int GREEN = 3 ;
    public static final int PURPLE = 4 ;
    public static final int PINK = 5 ;
    public static final int YELLOW = 6 ;
	
    public static String getName(int code) {

	switch(code) {
	case BLUE:   return "Blue" ;
	case RED:    return "Red" ;
	case GREEN:  return "Green" ;
	case PURPLE: return "Purple" ;
	case PINK:   return "Pink" ;
	case YELLOW: return "Yellow" ;
	}
		
	return "Invalid" ;
    }
	
    public static Color getMainColor(int code) {
		
	switch(code) {
	case BLUE:   return new Color(5,30,205) ;
	case RED:    return new Color(210,40,40) ;
	case GREEN:  return new Color(35,120,15) ;
	case PURPLE: return new Color(120,25,170) ;
	case PINK:   return new Color(200,35,130) ;
	case YELLOW: return new Color(200,135,30) ;
	}
		
	return new Color(125, 125, 125) ;
    }
	
    public static Color getLightColor(int code) {
		
	switch(code) {
	case BLUE:   return new Color(125,135,215) ;
	case RED:    return new Color(230,130,130) ;
	case GREEN:  return new Color(120,155,145) ;
	case PURPLE: return new Color(175,125,205) ;
	case PINK:   return new Color(220,130,180) ;
	case YELLOW: return new Color(235,190,115) ;
	}	
		
	return new Color(180,180,180) ;
    }
	
    public static Color getTempLockColor() {
		
	return Color.GRAY ;
    }
	
	
}
