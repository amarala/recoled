package cowrat.recoled.audio;
import java.util.Vector;
/**
 *  Store an array of active profiles, one for each participant:
 *  'Activity Profiles' are vectors of booleans representing whether a
 *  stream was active or not over a period of time. 
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: Profile.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated These JMF-based classes have been dropped in favour of
 * our own implementations. Use cowrat.recplaj.recorder.Recorder for
 * audio recording and cowrat.rexplore.hnm.MakeHanmerProfile for
 * activity logging (post-processing) instead. THIS PACKAGE IS NO
 * LONGER USED IN RECOLED AND WILL SOON BE REMOVED.
*/
public class Profile {

  public static final int DMAXPART = 10;       // default maximum number of participants
  public static final String PEXT =  ".hnm";   // default filename extension
  public static final Boolean T = new Boolean(true); // saving some memory and screen space...
  public static final Boolean F = new Boolean(false);
  private Vector[] participant_profiles;   // and array of '  profiles' (vectors with F = silence, T = audio / sec)
  private int current_no_profiles = 0;     // number of profiles currently kept 
  private String participant_names[];
  private long start_time;                 // time in millisecs since Jan 1 1970   
  private long end_time;
  
  public Profile  (){
    initialise(System.currentTimeMillis(), DMAXPART);
  }  
  public Profile  (long stime){
    initialise(stime, DMAXPART);
  }
  public Profile  (long stime, int nofp){
    initialise(stime, DMAXPART);
  }

  public void initialise(long stime, int nofp){
    participant_profiles = new Vector[nofp];
    participant_names = new String[nofp];
    for (int i = 0; i < nofp; i++){
      participant_profiles[i] = new Vector(3000,60);
    }
    setStartTime(stime);
  }

  public void addDataPoint(boolean v, String cname){
    int part = getParticipantIndex(cname);
    //System.err.println("Addin dp="+v+"for"+cname);
    if (part < 0)   // new participant, add it.  
      part = addParticipant(cname);
    participant_profiles[part].add(v?T:F);
  }

  public void addDataPoints(boolean v, String cname, int rep){
    for (int i = 0; i < rep; i++){
      addDataPoint(v, cname);
    }
  }

  public int addParticipant(String cname){
    if (current_no_profiles == participant_profiles.length){
      System.err.println("Profile.java: Error!! Maximum number of profiles exceeded.");
      System.exit(0);
    }
    participant_names[current_no_profiles] = cname;
    // initialise vector (fill positions up to current offset with silence)
    addDataPoints(false, cname, getCurrentOffset());
    //System.err.println("Addin cname="+cname);
    return current_no_profiles++;
  }

  public int getParticipantIndex(String cname){
    for (int i = 0; i < participant_names.length; i++){
      if (participant_names[i] == null)
        return -1;
      if (participant_names[i].equals(cname))
        return i;
    }
    return -1;
  }

  /** Get number of seconds past since start_time (= length of largest profile)
   */
  public int getCurrentOffset(){
    int co = 0;
    int no = 0;
    for (int i = 0; i < current_no_profiles; i++){
      no = participant_profiles[i].size();
      if (no > co)
	co = no;
    }
    return co;
  }

  /** Get array of profiles containing vectors of the same size
   * (vectors shorter than current_offset will be topped up with
   * silences). participant_profiles size is trimmed to current_no_profiles
   */
  public Vector[] getProfiles () {
    int co = getCurrentOffset();
    Vector[] out = new Vector[current_no_profiles];
    for (int i = 0; i < current_no_profiles; i++){
      addDataPoints(false,
		    participant_names[i],
		    co-participant_profiles[i].size());
      out[i] = participant_profiles[i];
    }
    return out;
  }

  public Vector getProfile (String cname) {
    int co = getCurrentOffset();
    int i = getParticipantIndex(cname);
    addDataPoints(false,
		  participant_names[i],
		  co-participant_profiles[i].size());
    return participant_profiles[i];
  }

  /** Return array of participant names, appropriately trimmed. 
   */
  public String[] getParticipantNames () {
    String[] out = new String[current_no_profiles];
    for (int i = 0; i < current_no_profiles; i++){
      out[i] = participant_names[i];
    }
    return out;
  }

  /** return profile in CSV format (for debugging purposes) 
   */
  public String toString() {
    Vector[] profs = getProfiles();
    String[] names = getParticipantNames();
    StringBuffer out = new StringBuffer();
    int stop = names.length-1;
    //System.err.println("Stop="+current_no_profiles);
    for (int i = 0; i < stop; i++){
      out.append(getUserName(names[i])+",");
    }
    out.append(getUserName(names[stop])+"\n");
    int rstop = profs[0].size();
    for (int i = 0; i < rstop; i++){
      out.append(i+1+",");
      for (int j = 0; j < stop; j++){
        out.append(toBinary(profs[j].get(i))+",");
      }
      out.append(toBinary(profs[stop].get(i))+"\n");
    }
    return out.toString();
  }

  // return the username part of a cname: e.g.: return 'amaral' for cname 'amaral@127.0.0.1' 
  public static String getUserName(String cname){
    int i = cname.indexOf('@');
    if (i < 0)
      return "anonymous";
    else
      return cname.substring(0,i);
  }

  public static String makeProfileName(String fn){
    int cut = fn.lastIndexOf('.');
    if (cut < 0) cut = fn.length();
    return fn.substring(0,cut)+PEXT;
  }

  private byte toBinary(Object v) {
    return ((Boolean )v).booleanValue() ? (byte) 1 : (byte) 0;
  }
  /**
   * Get the value of start_time.
   * @return value of start_time.
   */
  public long getStartTime() {
    return start_time;
  }
  
  /**
   * Set the value of start_time.
   * @param v  Value to assign to start_time.
   */
  public void setStartTime(long v) {
    this.start_time = v;
  }

  /**
   * Get the value of end_time.
   * @return value of end_time.
   */
  public long getEndTime() {
    return end_time;
  }
  
  /**
   * Set the value of end_time.
   * @param v  Value to assign to end_time.
   */
  public void setEndTime(long  v) {
    this.end_time = v;
  }
  
  /**
   * Get the value of current_no_profiles.
   * @return value of current_no_profiles.
   */
  public int getCurrentNoProfiles() {
    return current_no_profiles;
  }

}
