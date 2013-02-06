// a JMF-based profile maker. 

package cowrat.recoled.audio;

import java.io.*;
import java.lang.ref.*;
import java.awt.*;
import java.net.*;
import java.net.URL;
import java.awt.event.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;

import javax.media.*;
import javax.media.Buffer;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.format.FormatChangeEvent;
import javax.media.control.BufferControl;


/**
 * Gather audio activity (RTP stream, really) stats, and optionally
 * record audio streams; 
 *
 * @author <a href="mailto:luzs@cs.tcd.ie">S. Luz</a>
 * @version 1.0
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated This class is based on JMF. JMF however never really
 * transcodes properly, so this class has been deprecated in favour of
 * recording RTP packets directly via cowrat.recplaj.recorder.Recorder
 * and post-processing them with cowrat.rexplor.hnm.MakeHanmerProfile.
  */
public class ProfileMaker 
  extends Thread 
  implements ReceiveStreamListener, SessionListener
{
  //Initialise some required variables.
  public boolean completed = false;    // has the user told us to stop recording?
  private boolean recording = false;   // are we currently recording/profiling 
  private boolean recordaudio = true;  // should we create audio files for each stream?

  public boolean quit = false;

  private int rtp_stream_count = 0;
  private DataSource[] datasources;
  private Hashtable participantdetails = new Hashtable();
  private String ip_address;
  private String file_name = null;
  private String save_dir = null;
  Transcoder[] recorders;
  long[] recorderstarttimes;
  RTPManager rtpmanager = null;
  Processor pro = null;
  public long startTime, finishTime = 0;
  Hashtable all_participants;
  int count, no_of_participants, total_no_of_participants = 0;

  boolean dataReceived = false;
  Object dataSync = new Object();

  ProfileMakerControl pmgui;                // default user interface for stand-alone use
  private int acctime = 0;                  // number of 100ms iterations sampled
  private static final int ACTHRESHOLD = 5; // number of active samples above which we consider a 1s interval as active
  FineGrainedActivityLog hundredmseclog = 
    new FineGrainedActivityLog(10); // record 100ms activity counts per participant 
  Profile profile = null;

  public ProfileMaker() {
  }

  public ProfileMaker(ProfileMakerControl pmgui){
    this.pmgui = pmgui;
  }

  //Used to setup sessions for recording. Each IP address is a new session.
  public ProfileMaker(String ip, String file) {
    setArgs(ip, file);
  }

  public void setArgs(String ip, String file){
    setIP(ip);
    setFileName(file);
  }

  public void setIP(String ip){
    this.ip_address = ip;
  }

  public void setFileName(String file){
    this.file_name = file;
    String d = (new File(file)).getParent();
    this.save_dir = d == null? System.getProperty("user.dir") : d;
  }

  public String getSaveDir (){
    return save_dir;
  }

  public Profile getProfile (){
    return profile;
  }


  public void setGUI(ProfileMakerControl pmgui){
    this.pmgui = pmgui;
  }
  public void run()
  {
    if (!initialize()) {
      System.err.println("Failed to initialize the sessions.");
      System.exit(-1);
    }
    
    // gather stats every 100ms until user sets done=true
    try {
      while (samplingRTP())
        {
          Thread.sleep(100);
        }
    } catch (Exception e) 
      {
        System.err.println("Caught exception in isDone");
        e.printStackTrace();
      }
    
    System.err.println("Exiting...");
    completed = true;
  }
  
  protected boolean initialize() {

  datasources = new DataSource[Profile.DMAXPART];
  recorders = new Transcoder[Profile.DMAXPART];
  recorderstarttimes = new long[Profile.DMAXPART];
  try {
    InetAddress ipAddr;
    SessionAddress localAddr = new SessionAddress();
    SessionAddress destAddr;
    
    all_participants = new Hashtable();
    
    SessionLabel session;
    
    // Open the RTP session.
    try {
      session = new SessionLabel(ip_address);
    } catch (IllegalArgumentException e) {
        System.err.println("Failed to parse the session address given: " 
                           + ip_address);
        return false;
      }
        
      // Create RTPManager  and add listerners
      rtpmanager = (RTPManager) RTPManager.newInstance();
      rtpmanager.addSessionListener(this);
      rtpmanager.addReceiveStreamListener(this);
      
      ipAddr = InetAddress.getByName(session.addr);
        
      if( ipAddr.isMulticastAddress()) {
        // local and remote address pairs are identical:
        localAddr= new SessionAddress( ipAddr,
                                       session.port,
                                       session.ttl);
        destAddr = new SessionAddress( ipAddr,
                                       session.port,
                                       session.ttl);
      } else {
        localAddr= new SessionAddress( InetAddress.getLocalHost(),
                                       session.port);
        destAddr = new SessionAddress( ipAddr, session.port);
      }
        
      rtpmanager.initialize(localAddr);
        
      // You can try out some other buffer size to see
      // if you can get better smoothness.
      BufferControl bc = 
        (BufferControl)rtpmanager.getControl("javax.media.control.BufferControl");
      if (bc != null)
        bc.setBufferLength(350);
      
      rtpmanager.addTarget(destAddr);
      //send_manager.addTarget();
    } catch (Exception e){
      System.err.println("Cannot create the RTP Session: " + e.getMessage());
      return false;
    }
    // Wait for data to arrive before moving on.
    
    long then = System.currentTimeMillis();
    long waitingPeriod = 90000;  // wait for a maximum of 90 secs.
    
    //Wait For data received AND for start from participant list.
    try{
      synchronized (dataSync) {
        while (!dataReceived && System.currentTimeMillis() - then < waitingPeriod) {
          if (!dataReceived)
            System.err.println("  - Waiting for RTP data to arrive...");
          dataSync.wait(1000);
        }
      }
    } catch (Exception e) {}
    
    if (!dataReceived) {
      System.err.println("No RTP data was received.");
      close();
      return false;
    }
    return true;
  }

  // Entered every 100ms. Gather Statistical data about the meeting.
  public boolean samplingRTP() {
    acctime++;
    if (profile == null && recording){
      startTime = System.currentTimeMillis();
      profile = new Profile(System.currentTimeMillis());
    }
    if (rtpmanager != null)
      {
        Vector recvs = rtpmanager.getReceiveStreams();

        //For each participant, check if RTP streams are sending data.
        for (int ab = 0; ab < recvs.size(); ab++)
          {
            ReceiveStream rs = (ReceiveStream)recvs.get(ab);
            ReceptionStats rss = rs.getSourceReceptionStats();
            Integer blah = new Integer(rss.getPDUProcessed()); // get total number of valid packets received from rs
            Participant p = rs.getParticipant();
            String cname = p.getCNAME();
            if(all_participants.containsKey(cname))
              {
                Integer packet_count = (Integer)all_participants.get(cname);
                if(blah.intValue() > packet_count.intValue())
                  {
                    all_participants.remove(cname);
                    all_participants.put(cname, blah);
                    //System.out.println("packet count="+packet_count.intValue());
                    // mark this tenth of second as active
                    hundredmseclog.incrementActive100ms(cname);
                  }
              }
          }
        // System.out.println("acctime="+acctime);
        if (acctime >= 10 && recording){
          // System.out.println("pooling 100ms");
          for (Iterator j=hundredmseclog.keySet().iterator(); j.hasNext(); ){
            String cname = (String)j.next();
            // System.out.println("logging "+cname+" pkg count="+hundredmseclog.getActivityCount(cname));
            if (hundredmseclog.getActivityCount(cname) > ACTHRESHOLD){
              profile.addDataPoint(true, cname);
              pmgui.hiliteActiveParticipant(cname);
            }
            else{
              profile.addDataPoint(false, cname);
              pmgui.unhiliteInactiveParticipant(cname);
            }
          }
          hundredmseclog = new FineGrainedActivityLog(10);
          acctime = 0;
        }
      }
    //When the meeting is finished Stop the recorders  and output profile
    if (quit)
      {
        int k = 0;
        while (recorders[k] != null)
          {
            System.out.println("STOPPING RECORDERS");
            recorders[k].fileDone = true;
            k++;
          }
        k = 0;
        while (recorders[k] != null)
          {
            while(!recorders[k].completed)
              {
                try {
                  Thread.sleep(100);
                } catch (Exception e) {}
              }
            k++;
          }
        
        finishTime = System.currentTimeMillis();
        long totalTime = finishTime - startTime;
        
        //System.out.println(profile.toString()); 
      }
    return !quit;
  }

  /**
   * Close the session managers.
   */
  protected void close() {
    
    // close the RTP session.
    if (rtpmanager != null) {
      rtpmanager.removeTargets( "Closing session");
      rtpmanager.dispose();
      rtpmanager = null;
    }
  }
  
  public void setEnableAudioRecording(boolean v){
    // apparently processors need to be configured and started in order
    // for RTP stats to be gathered (?!!!), so we need to record audio for
    // profile maker to work. (SL)
    recordaudio = true; 
    // recordaudio = v;
  }


  public void startRecording()
  {    
    /*  ****************************************
    // The code below could, notionally, be used to merge all RTP audio streams into a single file. However, 
    // since the transcoder  blocks forever  (never makes the transition out of state 180), we can't really use it. 
    // Let's hear it for JMF :-(
    int i = 0;
    while(datasources[i] != null)
      {
        System.out.println("IN LOOP: " + datasources[i]);
        i++;
      }
    System.out.println("NUMBER OF DATA SOURCES: " + i);
    DataSource[] dss = new DataSource[i];
    //System.out.println(datasources[0]);
    for (int j = 0; j < i; j++)
      {
        System.out.println("IN HERE: " + j);
        dss[j] = datasources[j];
        System.out.println(dss);
      }

    try {
    DataSource mds = Manager.createMergingDataSource(dss);
    try {
    //System.out.println("---RTP STREAM COUNT: " + recorders[0]);
    pro = Manager.createProcessor(mds);
    
    recorders[0] = new Transcoder(pro, "/tmp/test.wav");
    } catch (Exception e) {System.out.println("UNABLE TO CREATE DATASINK" + e.toString());}
    } catch (Exception c){ System.out.println("UNABLE TO CREATE MERGED DATA SOURCE: " + c.toString());}
    */
    if (recordaudio && !recording && recorders != null){
      int k = 0;
      while (recorders[k] != null)
        {
          System.out.println("STARTING RECORDERS");
          recorders[k].start();
          k++;
        }
      recording = true;
      acctime = 0;
    }
    System.out.println("Recorders started");
  }

  
  /**
   * SessionListener.
   */
  public synchronized void update(SessionEvent evt) {
    if (evt instanceof NewParticipantEvent) {
      total_no_of_participants++;
      //System.out.println(total_no_of_participants);
      Participant p = ((NewParticipantEvent)evt).getParticipant();
      System.err.println("  - A new participant has just joined: " + p.getCNAME());
      pmgui.addParticipant(p.getCNAME());
      Integer test = new Integer(0);
      all_participants.put(p.getCNAME(), test);
    }
  }
  
  /**
   * ReceiveStreamListener
   * Try activeReceiveStreamEvent
   */
  public synchronized void update(ReceiveStreamEvent evt) {
    Participant participant = evt.getParticipant();	// could be null.
    ReceiveStream stream = evt.getReceiveStream();  // could be null.
    /* RTCP source description doesn't seem to be properly implemented, so we'll have to go with CNAME. 
    if (participant != null){
      Vector sd = participant.getSourceDescription();
    
      for (Iterator j=sd.iterator(); j.hasNext(); ){
        SourceDescription d = (SourceDescription)j.next();
        System.err.println("-->"+d.getDescription());
      }
    }
    */    

    if (evt instanceof RemotePayloadChangeEvent) {      
      System.err.println("  - Received an RTP PayloadChangeEvent.");
      System.err.println("Sorry, cannot handle payload change.");
      System.exit(0);
    }
    
    else if (evt instanceof NewReceiveStreamEvent) {
      try {
        stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
        DataSource ds = stream.getDataSource();
        
        no_of_participants++;
        
        // Notify intialize() that a new stream had arrived.
        synchronized (dataSync) {
          dataReceived = true;
          dataSync.notifyAll();
        }
      } catch (Exception e) {
        System.err.println("NewReceiveStreamEvent exception ");
        e.printStackTrace();
        return;
      }
    }  
    else if (evt instanceof StreamMappedEvent) {
      
      if (stream != null && stream.getDataSource() != null) {
        try {
          DataSource ds = stream.getDataSource();
          System.out.println("Mapped media stream " + ds.getContentType());

          pro = Manager.createProcessor(ds);
          System.out.println("Creating player for new stream");
          datasources[rtp_stream_count] = ds;
          recorderstarttimes[rtp_stream_count] = System.currentTimeMillis();
          String fn = save_dir+File.separator+Profile.getUserName(participant.getCNAME())+"_"+
            recorderstarttimes[rtp_stream_count]+".wav";
          recorders[rtp_stream_count] = 
            new Transcoder(pro, fn);
          // might be used in EditorServer in conjubction with saveXML()
          participantdetails.put(participant.getCNAME(),
                                 new AudioParticipant(fn,
                                                      recorderstarttimes[rtp_stream_count],
                                                      participant.getCNAME()));
          pmgui.hiliteActiveParticipant(participant.getCNAME());
          System.out.println("Created player for new stream"+fn);
          if (recording && recordaudio){ 
            recorders[rtp_stream_count].start();
            System.out.println("Started player for new stream"+fn);
          }
          rtp_stream_count++;
        } catch (Exception e) {
          System.err.println("NewReceiveStreamEvent exception ");
          e.printStackTrace();
          return;
        }
      }
    }
    else if (evt instanceof ByeEvent) {
      System.err.println("  - Got \"bye\" from: " + participant.getCNAME());
      no_of_participants--;
      pmgui.removeParticipant(participant.getCNAME());
    } 
  }
  
  /** Keep activity log for each 100ms interval
   */
  class FineGrainedActivityLog extends Hashtable{
    
    FineGrainedActivityLog (int init){
      super(init);
    }
    
    public void reset(String cname){
        this.remove(cname);
    }
    public void incrementActive100ms(String cname){
      if (this.containsKey(cname)){
        int count = getActivityCount(cname);
        count++;
        this.remove(cname);
        this.put(cname, new Integer(count));
        // System.err.println("-->"+cname+" count="+count);
      }
      else {
        this.put(cname, new Integer(1));
        // System.err.println("-->"+cname+" initial count=1");
      }
    }

    public int getActivityCount(String cname){
      return ((Integer)this.get(cname)).intValue();
    }
  }

  /**
   * A utility class to parse the session addresses.
   */
  class SessionLabel {
    
    public String addr = null;
    public int port;
    public int ttl = 1;
    
    SessionLabel(String session) throws IllegalArgumentException {
      
      int off;
      String portStr = null, ttlStr = null;
      
      if (session != null && session.length() > 0) {
        while (session.length() > 1 && session.charAt(0) == '/')
          session = session.substring(1);
        
        // Now see if there's a addr specified.
        off = session.indexOf('/');
        if (off == -1) {
          if (!session.equals(""))
            addr = session;
        } else {
          addr = session.substring(0, off);
          session = session.substring(off + 1);
          // Now see if there's a port specified
          off = session.indexOf('/');
          if (off == -1) {
            if (!session.equals(""))
              portStr = session;
          } else {
            portStr = session.substring(0, off);
            session = session.substring(off + 1);
            // Now see if there's a ttl specified
            off = session.indexOf('/');
            if (off == -1) {
              if (!session.equals(""))
                ttlStr = session;
            } else {
              ttlStr = session.substring(0, off);
            }
          }
        }
      }
      
      if (addr == null)
        throw new IllegalArgumentException();
      
      if (portStr != null) {
        try {
          Integer integer = Integer.valueOf(portStr);
          if (integer != null)
            port = integer.intValue();
        } catch (Throwable t) {
          throw new IllegalArgumentException();
        }
      } else
        throw new IllegalArgumentException();
      
      if (ttlStr != null) {
        try {
          Integer integer = Integer.valueOf(ttlStr);
          if (integer != null)
            ttl = integer.intValue();
        } catch (Throwable t) {
          throw new IllegalArgumentException();
        }
      }
    }
  }

  // methods for stand-alone use. Use ProfileMakerControl to embed
  // ProfileMake in your application instead
  private void showGUI (ProfileMaker pm){
    pmgui = new ProfileMakerGUI(pm);
  }

  public static void main(String args[])
  {
    ProfileMaker pm = new ProfileMaker();
    pm.showGUI(pm);
  }
}
