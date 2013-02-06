package cowrat.recoled.server;

import javax.swing.text.Position ;

public class Paragraph {
	
	// Constants.............................................................................................
	
	public static final int UNLOCKED = 0 ;
	public static final int TEMPLOCK = 1 ;
	public static final int LOCKED   = 2 ;
	
	public static final int timerRelease = 3000 ; // millisecconds of inactivity before lock is released
	
	// Variables.............................................................................................
	
	private long id ;	         //Unique identifier of paragraph. replaces "." with 9 and a 0 between two 9s is implicit
	private int level ; 	 //Number of "." in the id (used frequently so calculate at start)
	private Position position ;  //Position of start of paragraph (is actually 1 char behind start unless first paragraph)
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
		if(position.getOffset() > document.getLength())
			System.out.println("FUUUUUUUUCK!!!!!!!") ;
		
		int offset = position.getOffset() ;
		if (offset == 0)
			return 0 ;
		else
			return offset + 1 ;
	}
	
	public String toString() { return getIdAsString(id) ; }
	
	// constructors..................................................................................................
	
	public Paragraph(long i, int offset, EditorDocument d) {
		
		id = i ;
		document = d ;
		lockId = -1 ;
		lockOwner = -1 ;
		
		try {
			if (offset == 0)
				position = document.createPosition(offset) ;
			else
				position = document.createPosition(offset - 1) ;
		} catch (Exception e) {
			System.out.println("Could not create position") ;
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
		lockOwner = -1 ;
	}
	
	/*
	 Calculates the level of the paragraph based on it's id. 
	 The level is basically the number of "." in the id number.
	 */
	public int calculateLevel() {
		long num = id, num2 ;
		int level = 0 ;
		
		while (num > 0){
			num2 = num % 10 ;
			Double d = new Double(Math.floor(num/10)) ;
			num = d.longValue() ;
			
			if (num2 == 9) 
				level = level + 1 ;
		}			
		return level ;
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
	
	
	public int getHeight() {
		
		return 16 ;
		
	}
}
