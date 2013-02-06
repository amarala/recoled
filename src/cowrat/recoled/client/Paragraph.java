package cowrat.recoled.client;

import javax.swing.text.Position ;
import java.util.StringTokenizer ;

public class Paragraph {
	
    // Constants.............................................................................................
	
    public static final int UNLOCKED = 0 ;
    public static final int TEMPLOCK = 1 ;
    public static final int LOCKED   = 2 ;
	
    public static final int timerRelease = 3000 ; // millisecconds of inactivity before lock is released
	
    // Variables.............................................................................................
	
    private long id ;	         //Unique identifier of paragraph. replaces "." with 9 and a 0 between two 9s is implicit
    private String StringId ; //such as "2.1.2" etc...
    private int level ; 	 //Number of "." in the id (used frequently so calculate at start)
    private Position position ;  
//Position of start of paragraph (is actually 1 char behind start unless first paragraph)
// This field seems unecessary as the position is only recorded
// at paragraph creation and does not record subsequent changes
    private EditorDocument document ;
	
    private int lockStatus ;              //Status of lock (0, 1, 2)	
    private int lockOwner ;               //Id number of client
    private int lockId ;
	
    // Variable Access Methods................................................................................
	
    public int getLockStatus() {return lockStatus ;}
    public int getLockOwner() {return lockOwner ;}
	
    public int getLockId() {return lockId ;}
    public void setLockId(int l) {lockId = l ;} 
	
    public long getID() { return id ; } 

    //public void pushPosition{ 

    public static String getIdAsString(long idNum) {
		
	long num = idNum ;
	long prevNum = 0 ;
	long num2 ;
	String str = "" ;
		
	while (num > 0){
	    num2 = num % 10 ;
	    Double d = new Double(Math.floor(num/10)) ;
	    num = d.intValue() ;
			
	    if (num2 == 9) 
		if (prevNum == 9)
		    str = ".0" + str ;
		else
		    str = "." + str ;
	    else
		str = String.valueOf(num2) + str ;
			
	    prevNum = num2 ;
	}
	return str ;
    }

    public int getLevel() { return level ; }
	
    public int getOffset() { 
	int offset = position.getOffset() ;
	if (offset == 0)
	    return 0 ;
	else
	    return offset + 1 ;
    }
	
    //public String toString() { return getIdAsString(id) ; }
    public String toString() { return StringId ; }
	
    // constructors..................................................................................................
	
    public Paragraph(long i, int offset, EditorDocument d) {
		
	id = i ;
	document = d ;
	lockId = -1 ;
	StringId = getIdAsString(id) ;
	
		
	try {
	    if (offset == 0)
		position = document.createPosition(offset) ;
	    else
		position = document.createPosition(offset - 1) ;
		
	 		//System.out.println("\n\nPARAGRAPH->created position for par : " + StringId + " at position : " + position) ;
	} catch (Exception e) {
	    System.out.println("PARAGRAPH->Constructor ... Could not create position") ;
	}
		
	level = calculateLevel() ;

	}
	
    // Methods.................................................................................................
	
    public boolean isEditableBy(int clientId) {
	if (lockStatus != LOCKED) 
	    return true ;
		
	if (lockOwner == -1)
	    return true ;
		
	if (lockOwner == clientId) 
	    return true ;
		
	return false ;
    }
	
    public void tempLock() {
	lockOwner = -1 ;
	lockStatus = TEMPLOCK ;
    }
	
    public void lock(int clientID) {
	lockStatus = LOCKED ;
	lockOwner = clientID ;
		
    }
	
    public void unlock() {
	lockStatus = UNLOCKED ;
    }
	
    /*
      Calculates the level of the paragraph based on it's id. 
      The level is basically the number of "." in the id number.
    */
    public int calculateLevel() {

	int count =0;

	StringTokenizer tokenizer = new StringTokenizer(StringId,".",true);
	

	while ( tokenizer.hasMoreTokens() )
		{		
		if (  (tokenizer.nextToken()).equals(".") )
			count++;
		}

	return count;
	/*long num = id, num2 ;
	int level = 0 ;
		
	while (num > 0){
	    num2 = num % 10 ;
	    Double d = new Double(Math.floor(num/10)) ;
	    num = d.longValue() ;
			
	    if (num2 == 9) 
		level = level + 1 ;
	}			
	return level ;*/
    }


    public long incBase9Number(long num) {

	long head = num ;
	long tail = 0 ;
	long pos = 1 ;

	while (head > 0) {
	    long x = head % 10 ;
	    head = (long) Math.floor(head/10) ;

	    if (x < 8) 
		return (head * (pos*10)) + ((x+1) * pos) + tail ; 
	    else {
		tail = 0 ;
		pos = pos * 10 ;
	    }
	}
	return pos ;
    }
	
    /*
      Calculates the next logical id with the same level as this one
    */
    public long getNextID() {
		
	String str = String.valueOf(id) ;
	int index = str.lastIndexOf('9') ;
		
	if (index < 0) {
	    return incBase9Number(id) ;
	}
	else {
	    String head = str.substring(0, index + 1) ;
	    long tail = Long.valueOf(str.substring(index + 1)).longValue() ;
	    return Integer.valueOf(head + String.valueOf(incBase9Number(tail))).longValue() ;
	}
    }
	
    /*
      Calculates the next logical id at such a level that it cannot interfere with the following paragraphs
    */		
    public long getNextID(int nextLevel){
		
	int diff = level - nextLevel ;
	long newId ;
		
	if (diff > 0)
	    newId = getNextID() ; 	// stays at same level
	else {
				// need to go to a higher level
	    newId = id ;
	    while (diff < 0){
		newId = (newId * 10) + 9 ;  // add #.
		diff ++ ;
	    }
	    newId = (newId * 100) + 91 ;      // add #.1
	}
	return newId ;	    
    }
	
    public int compareId(long newID) {
	int value = compare(newID) ;
		
	//System.out.println("Comparing " + toString() + " with " + getIdAsString(newID) + " -> " + String.valueOf(value)) ;
	return value ;
    }
	
    /*
      Compares an id to the id of this paragraph
      Returns 0 if ids are the same
      -1 if id of this paragraph is greater
      1 if id of this paragraph is smaller
    */
    public int compare(long newID) {
		
	if (newID == id)
	    return 0 ;
		
	String s1[] = String.valueOf(id).split("9") ;
	String s2[] = String.valueOf(newID).split("9") ;
	int index = 0 ;
	int i1, i2 ;
		
	while (index < s1.length && index < s2.length){
			
	    if (s1[index].length() == 0) 
		i1 = 0 ;
	    else
		i1 = Integer.valueOf(s1[index]).intValue() ;
			
	    if (s2[index].length() == 0)
		i2 = 0 ;
	    else
		i2 = Integer.valueOf(s2[index]).intValue() ;
			
	    if ( i1 > i2) return -1 ; 
	    if ( i2 > i1) return 1 ;
			
	    index ++ ;
	}
		
	if (index < s1.length) return -1 ;
	if (index < s2.length) return 1 ;
		
	return 0 ;
    }

// t o compare String Ids of paragraph such as "2.0.2" and "2.1"
// returns 1 if num1>num2 , and -1 if num1<num2, 0 otherwise

	public static int CompareStringId (String num1, String num2)

{
	int digit1= -1;
	int digit2= -1;
	String number1=num1;
	String number2=num2;

	StringTokenizer tokenizer1 = new StringTokenizer(num1,".");
	StringTokenizer tokenizer2 = new StringTokenizer(num2,".");

	if ( tokenizer1.hasMoreTokens() )
		{		
		digit1 = Integer.parseInt ( tokenizer1.nextToken() );
		//System.out.println("\ndigit 1 =" +digit1  ) ;
		}

	if ( tokenizer2.hasMoreTokens() )
		{
		digit2 = Integer.parseInt ( tokenizer2.nextToken() );
		//System.out.println("\ndigit 2 =" +digit2  ) ;
		}

	if (digit1>digit2)
		{
		//System.out.println("\n" + num1 +" > "+ num2 ) ;
		return 1;
		}

	else if (digit1<digit2)
		{
		//System.out.println("\n" + num1 +" < "+ num2 ) ;
		return -1;
		}

	//if we 're here it means that digits for this level are equal...
	// now get "0.2" from 2.0.2 and ".1" from 2.1

	boolean num1HasMoreDigits=true;
	boolean num2HasMoreDigits=true;

	try{ num1 = number1.substring(2);}

	//if exception, means no more digits 

	catch( StringIndexOutOfBoundsException siobe) { 
	num1HasMoreDigits=false;}
	
	try{ num2 = number2.substring(2);}

	//if exception, means no more digits 

	catch( StringIndexOutOfBoundsException siobe) { 
	num2HasMoreDigits=false;}
	
	if ( num1HasMoreDigits && num2HasMoreDigits )	
		CompareStringId (num1,num2);

	//such as num1=0.1 & num2 = null

	else if ( num1HasMoreDigits && !num2HasMoreDigits )
		{
		//System.out.println("\n" + number1 +" > "+ number2 ) ;
		return 1;
		}

	else if ( !num1HasMoreDigits && num2HasMoreDigits )
		{
		//System.out.println("\n" + number1 +" < "+ number2 ) ;
		return -1;
		}
	// Ids are equal
	
		return 0;
	
}//endof CompareStringId

   public String toString2()

	{
	String tempString ="\nPar id : "+id +" StringId : "+StringId ;
	
	return (tempString );
	}
	
    public int getHeight() {
		
	return 16 ;
		
    }

}//end class Paragraph
