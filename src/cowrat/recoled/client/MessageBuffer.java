package cowrat.recoled.client;

import com.sun.media.jsdt.*;
import java.util.Vector;
import cowrat.recoled.shared.*;

public class MessageBuffer extends Thread{

    private Channel channel ;
    private Client client ;
    private Vector messages ;
    private int delay ;

    public MessageBuffer(Channel ch, Client cl, int d){
	super() ;
	channel = ch ;
	client = cl ;
	delay = d ;
	messages = new Vector() ;
	setPriority(Thread.MIN_PRIORITY) ;

	setDaemon(true) ;
    }

    public void addMessage(Message msg) {
	messages.add(msg) ;
    }

    public void run() {
	while(true) {
	    //System.out.print(".") ;
	    if(!messages.isEmpty()) {
		try {
		    //System.out.print(".") ;
		    Message msg = (Message)messages.elementAt(0) ;
		    messages.remove(0) ;
		    channel.sendToClient(client, "Server", new Data(msg)) ;
		    if(msg.getType() == Message.CLIENT_LEAVE)
			System.exit(0) ;
		    
		    sleep(delay) ;
		} catch(Exception e) {
		    System.out.println("MessageBuffer: run: error sending message") ;
		    e.printStackTrace() ;
		}
	    }
	    yield() ; 
	}
    }
}
