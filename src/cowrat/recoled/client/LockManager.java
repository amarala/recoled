//Feb version

package cowrat.recoled.client;

import java.util.Vector ;
import java.util.Iterator ;
import java.util.* ;
import java.awt.Color ;

public class LockManager implements EditorDebugFlags {
    
    private EditorClient parent ;
    private Paragraphs paragraphs ;
    private EditorDocument document ;
    private Clients clients ;
    private ColoredSections coloredSections ;
    
    private HashMap locks ;
    private int nextLockId ;

    private static final int newline = 10 ;
    private static boolean initialized = false;
    
        
    public void setDocument(EditorDocument d) { 
        document = d ; 
        coloredSections = new ColoredSections(d) ;
    } 
    public void setParagraphs(Paragraphs p) { paragraphs = p ; }
    public void setClients(Clients c) { clients = c ; }
    public Clients getClients() { return clients ; }
    
       public LockManager(EditorClient p) {
        parent = p ;
        
        locks = new HashMap() ;
        nextLockId = 1 ;
    }
    
    // Methods that are only here for synchronization purposes - makes more sense to have them in document class
    
    public synchronized Paragraph getParFromOffset(int offset) {
        return paragraphs.getParFromIndex(paragraphs.getParIndexFromOffset(offset)) ;        
    }
    
    public synchronized Paragraph getParFromId(long id) {
        return paragraphs.getParFromID(id) ;
    }
    
    public synchronized void setDocumentState(String xml) {
	    //if(LockManager_Debug)
        	System.out.println("LockManager-> setDocumentState : " + xml) ;
        int startIndex = 0 ;
        int endIndex = xml.indexOf("</par>") ;
        int i, j ;
        while (endIndex >= 0) {
            String line = xml.substring(startIndex, endIndex) ;		    
            
            i = 8 ;
            j = line.indexOf(" ", i) ;
            long parId = new Long(line.substring(i, j)).longValue() ;
            
            i = j + 8 ;
            j = line.indexOf(" ", i) ;
            int offset = new Integer(line.substring(i, j)).intValue() ;
            
            i = j + 11 ;
            j = line.indexOf(">", i) ;
            int lockOwner = new Integer(line.substring(i, j)).intValue() ;
            
            String text = line.substring(j + 1, line.length()) ;
            
            document.appendText(text) ;
            
            
            Paragraph p = paragraphs.addParagraph(paragraphs.size(), parId, offset) ;
            if(lockOwner >=0)
                paragraphs.lock(p, lockOwner) ;
                
            //if(LockManager_Debug)
            	{
	            System.out.println("ParSize :" + paragraphs.size()) ;
            	System.out.println("Par:" + String.valueOf(parId)) ;
            	System.out.println("Offset:" + String.valueOf(offset)) ;
            	System.out.println("Lock:" + String.valueOf(lockOwner)) ;
            	System.out.println("Text:\"" + text + "\"") ;
        		}
            
            startIndex = endIndex + 6 ;
            endIndex = xml.indexOf("</par>", startIndex) ;
        } 
        
    }
    
    public synchronized void insertText(long parId, int offset, String text, int cID) {
        
        Paragraph p = paragraphs.getParFromID(parId) ;
        //if(LockManager_Debug)
        	System.out.println("\nLockManager--*--*--> insertText: par " + p.toString()) ;
        document.insertText(p.getOffset() + offset, text, cID) ;
    }   
    
    public synchronized void deleteText(long startPar, long endPar, int startOffset,int endOffset, int cID) {
        int startPos, endPos ;
        
        Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
        Paragraph start = (Paragraph) range.firstElement() ;
        startPos = start.getOffset() + startOffset ;
        
        Paragraph end = (Paragraph) range.lastElement() ;
        endPos = end.getOffset() + endOffset ;
        
        document.deleteText(startPos, endPos - startPos, cID) ;
    }
    
    
    /*
     Called by document when it detects that text has been inserted into document
     - adds paragraphs if neccessary
     - sets status appropriately for all paragraphs involved
     - resizes indicators for all paragraphs involved

	Note : see server.LockManager-> synchronized Vector textInserted for
	a server equivalent to this function
     */
    public synchronized void insertUpdate(int offset, String text, int clientID) {
	    
	if(LockManager_Debug)
    	System.out.print("\n%%LockManager->insertUpdate: offset = "+ offset );

  // add colored section
        Color color = clients.getClient(clientID).getMainColor() ;
        coloredSections.addColoredSection(offset, offset + text.length(), color) ; 
        
        
        int parIndex = paragraphs.getParIndexFromOffset(offset) ;
        int startIndex = parIndex ;
        
        Paragraph currPar = paragraphs.getParFromIndex(parIndex) ;
        
        // identify current lock (if any) 
        Lock lock ;
        if (currPar.getLockId() > 0)
            lock = (Lock)locks.get(new Integer(currPar.getLockId())) ;
        else
            lock = null ;
        
        // calculate level of paragraph after current one
        int nextLevel ;
        if (parIndex < paragraphs.size() - 1)
            nextLevel = (paragraphs.getParFromIndex(parIndex + 1).getLevel()) ;
        else
            nextLevel = currPar.getLevel() - 1 ;
        
        // add new paragraphs if neccessary
        int index = text.indexOf(newline) ;
        while (index >= 0) {
            parIndex ++ ;   		    
            currPar = paragraphs.addParagraph(parIndex, currPar.getNextID(nextLevel), offset + index + 1) ;
            if (lock == null) {
                paragraphs.lock(currPar, clientID) ;
                currPar.setLockId(-1) ;
            }
            else {
                lock.expand(currPar.getID(), currPar.getID()) ;
                lock.parInserted(currPar.getID()) ;
            }
            
            index = text.indexOf(newline, index + 1) ;
        } 
        int endIndex = parIndex ;
        
        // resize all effected paragraphs 
        while (startIndex <= endIndex) {
            currPar = paragraphs.getParFromIndex(startIndex) ;
            paragraphs.resizeParagraph(currPar) ;
            startIndex ++ ;
        } 

}//end  insertUpdate()
    
    public synchronized void removeUpdate(int offset) {
        Vector range = paragraphs.getRangeFromOffsets(offset, offset) ;
        paragraphs.resizeParagraph((Paragraph)range.firstElement()) ;
    }
    
    // called by document when it detects that a chunk of text is about to be deleted

    public synchronized boolean remove (int offset, int length, String text, boolean notify) {
        
        if (notify && !isEditable(offset, offset + length))
            return false ;
            
        else //else1
        
        {
			if(LockManager_Debug)
            		System.out.println("\n\nclient.LockManager-> remove : Deleting from " + String.valueOf(offset) + " to " + String.valueOf(offset+length)+ " \n" ) ;
            Vector range = paragraphs.getRangeFromOffsets(offset, offset + length) ;
            
            Paragraph sP = (Paragraph) range.firstElement() ;
            int sOffset = offset - sP.getOffset() ;
            Paragraph eP = (Paragraph) range.lastElement() ;
            int eOffset = (offset+length) - eP.getOffset() ;
	     
            // broadcast event (or store it if lock not granted

            if (notify)
				{	
           		Lock lock = prepareForEdit(offset, offset + length)  ;	

                if (lock.isGranted())
						{
							if(LockManager_Debug)
								System.out.print("\n\nclient.LockManager-> remove : notifying EdClient \n... of textDeleted from offset :  sOffset : "+ sOffset + " to eOffset : "+ eOffset) ;
                    	parent.textDeleted(sP.getID(), eP.getID(), sOffset, eOffset) ;
                    	}
               	else 
						{
						if(LockManager_Debug)
							System.out.print("\n\nclient.LockManager-> remove : in notify/else textDeleted from sP :  " + sP.getID() + " to : eP " + eP.getID() + " sOffset : " + sOffset + " - eOffset : " + eOffset );
                    	lock.textDeleted(sP.getID(), eP.getID(), sOffset, eOffset, text) ;
						}
           	 }
	            
            // restructure paragraphs
	
            Iterator i = range.iterator() ;
            
            while (i.hasNext()) 
				{
                Paragraph p = (Paragraph) i.next() ; 
                if(LockManager_Debug)							
                	System.out.print("\nclient.LockManager-> remove : Checking par " + p.toString()) ;
                
                if (p.getOffset() > offset && p.getOffset() <= offset + length)
						{
                    	paragraphs.removeParagraph(p) ;
                    	if(LockManager_Debug)
                    		System.out.println(" - removed") ;
                    
                    	if(notify && p.getLockId() >= 0) 
								{
                        		Lock lock = (Lock)locks.get(new Integer(p.getLockId())) ;
                        		if(lock.isGranted()) 
                            			parent.releaseParagraphs(p.getID(), p.getID()) ;
                        		else
                            			lock.parDeleted(p.getID(), p.getOffset()) ;
                    			}
               			}

                else if (LockManager_Debug)
                	{ System.out.println(" - kept") ; }

            }//endwhile
            
	       return true ;

        }//endelse1
    }
    
    public synchronized String insertString(int absOffset, String string, boolean notify,boolean SecondCall) {

	if(LockManager_Debug)
		System.out.println("\n\n||client.LockManager-> insertString : @  offset : " + absOffset + "Doc length : " + document.getLength()+ " string :  \n*" +  string +"*" +"\n notify : " +  notify +"\n") ;
        
    if (notify && !isEditable(absOffset, absOffset))
    	return "false" ;

    else {

            if (notify)
				{
					
					try{

				Lock lock = prepareForEdit(absOffset, absOffset) ;

				if (!SecondCall)
					{
			
					int parIndex = paragraphs.getParIndexFromOffset(absOffset) ;

					Paragraph currPar = paragraphs.getParFromIndex(parIndex) ;
		
					String currStringId = currPar.toString();
		
					long currId = currPar.getID();

					int currOffset = currPar.getOffset();

					//the condition should be generalized to if the par !empty and caret before any text...
		
					if (absOffset==0 &&  !( lock.wasParagraphEmptyBeforeLock() ) )
						{
						if(LockManager_Debug)
							System.out.println("\n\n===client.LockManager-> insertString : Exception CASE !!!");
						//answer = lockManager.insertString(absOffset, string, notify,true);
						//skips next condition and should continue normally...
						}
				
					else if (absOffset == document.getLength() )
						{
						//carries on as normal skipping next special case...
					 	}

					else if ( !( lock.wasParagraphEmptyBeforeLock() ) && currOffset==absOffset &&  ( ( string.indexOf("\n") ) >= 0  ) )
						{
						if(LockManager_Debug)
							System.out.println("\n\n===client.LockManager-> insertString : THIS IS THE SPECIAL CASE !!!");
						return "PreInsertion";		
						}

					}//endif (!SecondCall)
                
                Vector range = paragraphs.getRangeFromOffsets(absOffset, absOffset) ;
                Paragraph sP = (Paragraph) range.firstElement() ;
                int sOffset = absOffset - sP.getOffset() ;
	                
                if (lock.isGranted()) 
					{
					if(LockManager_Debug)
						System.out.println("\n\n||||||client.LockManager : LOCK IS GRANTED !!!");

                   	 parent.textInserted(string, sP.getID(), sOffset) ;
                   	 lock.reduce(sP.getID(), sP.getID()) ;
                	}

                else
					{
					if(LockManager_Debug)
						System.out.println("\n\n||||||client.LockManager-> insertString : LOCK ELSE !!!");
                    lock.textInserted(string, sP.getID(), sOffset, string.length()) ;
					}
					
				}//endtry
				
				catch (Exception E) {System.out.println("\n\nclient.LockManager-> insertString : Insert/LOCK ERROR !!!");
													return "ignore";
												}
	
            }//endif (notify)

            return "true" ;
            
        }//endelse

    }//end insertString()
    
    
    
    //Accessable Methods (all synchronized)...............................................................................
    
    
    public synchronized void releaseEverything() {
        Iterator i = locks.values().iterator() ;
        while (i.hasNext()) {
            Lock l = (Lock) i.next() ;
            l.release() ;
            i.remove() ;
        }
    }
    
    /*
     lock is made by someone else
     - if we have an ungrantedLock that overlaps this range, deny it
     (often get the lockmade message before our lock is denied)
     - lock all paragraphs in this range.
     */
    public synchronized void lockMade(int clientID, long sPar, long ePar) {
        
        // if there are any ungranted locks that overlap this, deny them
        
        
        // update status for all paragraphs involved
        Vector range = paragraphs.getRangeFromIds(sPar, ePar) ;
        Iterator i = range.iterator() ;
        while (i.hasNext())
            paragraphs.lock((Paragraph)i.next(), clientID) ;
    }
    
    public synchronized void lockReleased(long sPar, long ePar) {
        Vector range = paragraphs.getRangeFromIds(sPar, ePar) ;
        Iterator i = range.iterator() ;
        while (i.hasNext()) 
            paragraphs.unlock((Paragraph)i.next()) ;
    }
    
    /* 
     grants a lock - updates status of all paragraphs in lock
     - broadcasts and clears all actions made while lock was ungranted
     */
    public synchronized void grantLock(int lockId) {
        Lock lock = (Lock)locks.get(new Integer(lockId)) ;
        lock.grant() ;
    }
    
    /*
     denies a lock - updates status of all paragraphs in lock
     - undoes and clears all actions made while lock was ungranted
     - removes lock from list
     */
    public synchronized void denyLock(int lockId) {
        Lock lock = (Lock)locks.get(new Integer(lockId)) ;
        lock.deny() ;
        locks.remove(new Integer(lockId)) ;
    }
    
    /* 
     releases a lock - updates status of all paragraphs in lock
     - removes lock from list
     */
    public synchronized void releaseLock(int lockId) {
        Lock lock = (Lock)locks.get(new Integer(lockId)) ;
        lock.release() ;
        locks.remove(new Integer(lockId)) ;
    }
    
    
    // inaccessable methods............................................................................................
    
    /*
     creates a new lock for the given region - assumes that lock has not been granted yet
     returns the id of the lock.
     */
    private Lock createNewLock(long startPar, long endPar) {
        Lock lock = new Lock(nextLockId, startPar, endPar) ;
        locks.put(new Integer(nextLockId), lock) ;
        parent.requestLock(startPar, endPar, nextLockId) ;
        nextLockId ++ ;
        return lock ;
    }
    
    /* 
     expands a lock to include the given region
     sets the status of the paragraphs in this region accordingly
     */
    private void expandLock(int lockId, long startPar, long endPar) {
        Lock lock = (Lock)locks.get(new Integer(lockId)) ;
        lock.expand(startPar, endPar) ;
    }
    
    /*
     reduces a lock to include only the given region
     called on a granted lock to reduce it to the region currently edited
     */
    private void reduceLock(int lockId, long startPar, long endPar) {
        Lock lock = (Lock)locks.get(new Integer(lockId)) ;
        lock.reduce(startPar, endPar) ;
    }
    
    /*
     Indicates weither a range of text can be edited (whether it is possible to obtain a lock for this region)
     */
    public boolean isEditable(int startOffset, int endOffset) {
        
        Vector range = paragraphs.getRangeFromOffsets(startOffset, endOffset) ;
        Iterator i = range.iterator() ;
        
        while (i.hasNext()) {
            Paragraph p = (Paragraph) i.next() ;
            if(!p.isEditableBy(clients.getMyId()))
                return false ;
        }
        return true ;
    }
    
    /*
     Prepares a range of text for editing - either renewing existing lock or requesting new one
     */
    public Lock prepareForEdit(int startOffset, int endOffset) {
	    
	    if(LockManager_Debug)
        	System.out.println("Preparing for edit: (" + String.valueOf(startOffset) + ", " + String.valueOf(endOffset) + ")") ;
        
        boolean allTempLocked = true ;
        boolean allLocked = true ;
        
        Vector range = paragraphs.getRangeFromOffsets(startOffset, endOffset) ;
        Iterator i = range.iterator() ;
        while (i.hasNext()) {
            Paragraph p = (Paragraph) i.next() ;
            if(LockManager_Debug)
            	System.out.println ("\nChecking " + p.toString()) ;
            
            if (p.getLockStatus() != Paragraph.TEMPLOCK)
                allTempLocked = false ;
            
            if (p.getLockStatus() != Paragraph.LOCKED)
                allLocked = false ;
        }
        
        if (allTempLocked || allLocked){
	        if(LockManager_Debug)
            	System.out.println("Area Prepared Allready") ;
            Paragraph p = (Paragraph) range.firstElement() ;
            return (Lock) locks.get(new Integer(p.getLockId())) ;
        }
        else {
	        if(LockManager_Debug)
            	System.out.println("Area must be prepared") ;
            // remove all granted locks
            Collection lockC =  locks.values() ;
            i = lockC.iterator() ;
            
             try{
            	while (i.hasNext())
            		 {	           
                	Lock l = (Lock) i.next() ;
                	if (l.isGranted())
                    	releaseLock(l.getIdNumber()) ;
               		}                      
            	}
            
            catch(Exception E) { System.out.println("LockManager->prepareForEdit : Lock Iteration Error...") ;}
            
            
            
            // have to make a new lock
            long startId = ((Paragraph) range.firstElement()).getID() ;
            long endId = ((Paragraph) range.lastElement()).getID() ;
            
            Lock lock = createNewLock(startId, endId) ;
            return lock ;
        }
    }
    
    
    // Lock class.........................................................................................................
    
    private class Lock {
        private int idNumber ;
        private long startPar ;
        private long endPar ;
        
        private boolean granted ;
        private Vector actions ;
        private Timer timer ;
        
        private final int delay = 5000 ;
        private boolean ParagraphEmptyBeforeLock=true;

        
        public int getIdNumber() { return idNumber ;}
        public boolean isGranted() {return granted ;}
        public boolean wasParagraphEmptyBeforeLock()
		{ return ParagraphEmptyBeforeLock;}

        
        
        public Lock(int id, long sPar, long ePar) {
           
            idNumber = id ;
            startPar = sPar ;
            endPar = ePar ;
            
            granted = false ;
            actions = new Vector() ;

			ParagraphEmptyBeforeLock = paragraphs.isCaretInEmptyParagraph();
			
			if(LockManager_Debug)
			 System.out.println("\n\n+++LOCK->CONSTRUCTOR : wasParagraphEmptyBeforeLock : " + ParagraphEmptyBeforeLock ) ;
            
            Vector range = paragraphs.getRangeFromIds(sPar, ePar) ;
            Iterator i = range.iterator() ;
            while (i.hasNext()) {
                Paragraph p = (Paragraph) i.next() ;
                paragraphs.tempLock(p) ;
                p.setLockId(idNumber) ;
            }
        }
        
        public void grant() {
	        if(LockManager_Debug)
            	System.out.println("Lock--> GRANT.") ;
            granted = true ;
            
            // update paragraph status
            Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
            Iterator i = range.iterator() ;
            while (i.hasNext()) {
                Paragraph p = (Paragraph) i.next() ;
                paragraphs.lock(p, -1) ;
            }
            
            // broadcast actions
            int x = actions.size() ;
            while (x > 0) {
                x = x - 1;
                	Action lastAction = (Action) actions.elementAt(x) ;               
			if(LockManager_Debug)
				System.out.println("Lock->grant : BROADCASTING :... " + lastAction.toString()) ;
	            		
			lastAction.broadcast() ;
            }
            actions.clear() ;            
            
            // start timer for releasing lock
            timer = new Timer() ;
            timer.schedule(new TimerTask() { 
                public void run() {
                    releaseLock(idNumber) ;
                }
            }, delay) ;
        }
        
        public void deny() {
	        if(LockManager_Debug)
            	System.out.println("Lock: deny.") ;
            
            // update paragraph status
            Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
            Iterator i = range.iterator() ;
            while (i.hasNext()) {
                Paragraph p = (Paragraph) i.next() ;
                paragraphs.lock(p, -1) ;
            }
            
            // undo actions
            i = actions.iterator() ;
            while (i.hasNext()) {
                Action a = (Action) i.next() ;
                a.undo() ;
            }
            actions.clear() ;
        }
        
        public void expand(long sPar, long ePar) {
            
            boolean changed = false ;
            
            // calculate new extents of lock ;
            if (paragraphs.compareIds(sPar, startPar) < 0) {
                startPar = sPar ;
                changed = true ;
            }
            
            if (paragraphs.compareIds(ePar, endPar) > 0) {
                endPar = ePar ;
                changed = true ;
            }
            
            if (changed) {
                // update status of paragraphs
                
                Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
                Iterator i = range.iterator() ;
                while (i.hasNext()) {
                    Paragraph p = (Paragraph) i.next() ;
                    
                    if (granted) 
                        paragraphs.lock(p, -1) ;
                    else
                        paragraphs.tempLock(p) ;
                    
                    p.setLockId(idNumber) ;
                }
            }
            if(LockManager_Debug)
           		System.out.println("Lock: expand. new lock from " + String.valueOf(startPar) + " to " + String.valueOf(endPar)) ;
        }
        
        public void reduce(long sPar, long ePar) {
            
            Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
            Vector before = new Vector() ;
            Vector after = new Vector() ;
            
            // identify regions to be discarded, update paragraph statuses
            Iterator i = range.iterator() ;
            while (i.hasNext()) {
                Paragraph p = (Paragraph) i.next() ;
                if(p.compare(sPar) < 0) {
                    before.add(p) ;
                    paragraphs.unlock(p) ;
                    p.setLockId(-1) ;
                }
                
                if(p.compare(ePar) > 0) {
                    after.add(p) ;
                    paragraphs.unlock(p) ;
                    p.setLockId(-1) ;
                }
            }
            
            // update size of lock
            startPar = sPar ;
            endPar = ePar ;
            
            // send releases for regions that have been discarded from this lock
            if (!before.isEmpty())
                parent.releaseParagraphs(((Paragraph)before.firstElement()).getID(), 
                        ((Paragraph)before.lastElement()).getID()) ;
            if (!after.isEmpty())
                parent.releaseParagraphs(((Paragraph)after.firstElement()).getID(), 
                        ((Paragraph)after.lastElement()).getID()) ;
            
            // reset timer for releasing this lock
            timer.cancel() ;
            timer = new Timer() ;
            timer.schedule(new TimerTask() { 
                public void run() {
                    releaseLock(idNumber) ;
                }
            }, delay) ;
            if(LockManager_Debug)
            	System.out.println("Lock: reduce. new lock from " + String.valueOf(startPar) + " to " + String.valueOf(endPar)) ;
        }
        
        public void release() {
	        try{
            timer.cancel() ;
        	}
        	catch (Exception e){System.out.println("Lock->release. Timer not found... ");} 
            timer = null ;
            Vector range = paragraphs.getRangeFromIds(startPar, endPar) ;
            Iterator i = range.iterator() ;
            
            // update status of paragraphs
            while (i.hasNext()) {
                Paragraph p = (Paragraph) i.next() ;
                paragraphs.unlock(p) ;
                p.setLockId(-1) ;
            }
            
            // send release
			try{
				parent.releaseParagraphs(((Paragraph)range.firstElement()).getID(),
                    ((Paragraph)range.lastElement()).getID()) ;}
                    
             catch(Exception e) {System.out.println("Lock->release. Could not release paragraph... ");}
        }
        
        public void textInserted(String text, long par, int offset, int length) {
		if(LockManager_Debug)
			System.out.println("\nLock->textInserted : TEXT_INSERT ACTION LOADED : \n Text : \n*"+ text+ "*\n"+ " par : "+ par +" offset : "+ offset ); 

            Action a = new Action(Action.TEXT_INSERT, par, 0, offset, 0, length, text) ;
            actions.add(0, a) ; // add to top of stack
        }
        
        public void textDeleted(long sPar, long ePar, int sOffset, int eOffset, String text){
            Action a = new Action(Action.TEXT_DELETE, sPar, ePar, sOffset, eOffset, 0, text) ;
            actions.add(0, a) ; // add to top of stack
        }
        
        public void parInserted(long par) {
            Action a = new Action(Action.PAR_INSERT, par, 0, 0, 0, 0, "") ;
            actions.add(0, a) ; // add after text edit that caused this
        }
        
        public void parDeleted(long par, int offset) {
            Action a = new Action(Action.PAR_DELETE, par, 0, offset, 0, 0, "") ;
            actions.add(0, a) ; // add after text edit that caused this
        }
                
    }
    
    // Lock access methods.................................................................................................
        
    
    private class Action {
        
        public static final int TEXT_INSERT = 0 ;
        public static final int TEXT_DELETE = 1 ;
        public static final int PAR_INSERT = 2 ;
        public static final int PAR_DELETE = 3 ;
        
        private int type ;
        private long sPar ;
        private long ePar ;
        private int sOffset ;
        private int eOffset ;
        private int length ;
        private String text ;
        
        public Action(int t, long sp, long ep, int so, int eo, int l, String x) {
            type = t ;
            sPar = sp ;
            ePar = ep ;
            sOffset = so ;
            eOffset = eo ;
            length = l ;
            text = x ;
        }
        
        public void undo() {
            switch(type) {
            case 0: undoInsertText() ; break ;
            case 1: undoDeleteText() ; break ;
            case 2: undoInsertPar() ; break ;
            case 3: undoDeletePar() ; break ;
            }
        }
        
        public void undoInsertText() {
            Paragraph p = paragraphs.getParFromID(sPar) ;
            document.undoInsertText(p.getOffset() + sOffset, length) ;
        }
        
        public void undoDeleteText() {
            Paragraph p = paragraphs.getParFromID(sPar) ;
            document.undoDeleteText(p.getOffset() + sOffset, text) ;
            
        }
        
        public void undoInsertPar() {
            Vector range = paragraphs.getRangeFromIds(sPar, sPar) ;
            paragraphs.removeParagraph(paragraphs.indexOf(range.firstElement())) ;
        }
        
        public void undoDeletePar() {
            paragraphs.addParagraph(sPar, sOffset) ;
        }
        
        public void broadcast() {
            // broadcast this event, if necessary
            switch(type) {
            case 0: parent.textInserted(text, sPar, sOffset) ; break ;
            case 1: parent.textDeleted(sPar, ePar, sOffset, eOffset) ; break ;
            case 3: parent.releaseParagraphs(sPar, sPar) ; break ;
            }
        }
        
        public String toString() {
            switch(type) {
            case 0: return "\n\nLockManager->toString : Action - ADDING @ :  " + Paragraph.getIdAsString(sPar) + 
            "-sOFFSET : " + String.valueOf(sOffset) + " Endpar : "+  Paragraph.getIdAsString(ePar) + "-eOFFSET : " + String.valueOf(eOffset)   ;
            case 1: return "\n\nLockManager->toString : Action - deleting : \n\n*" + text + "*\n at (" + Paragraph.getIdAsString(sPar) + 
            "," + String.valueOf(sOffset) + ")" ;
            case 2: return "LockManager->toString : Action - add par " + Paragraph.getIdAsString(sPar) ;
            case 3: return "LockManager->toString : Action - del par " + Paragraph.getIdAsString(sPar) ;
            }
            
            return "Invalid Action" ;	
        }

public long getStartParagraph()
	{return sPar;}

public long getEndParagraph()
	{return ePar;}

public int getStartOffset()
	{return sOffset;}

public int  getEndOffset()
	{return eOffset;}

public String getText()
	{return text;}

    }//end inner class Action
    
}//end LockManager

