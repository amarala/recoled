package cowrat.recoled.audio;
/**
 *  Summary of attributes of an audio file (location, start time, etc)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: AudioParticipant.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated These JMF-based classes have been dropped in favour of
 * our own implementations. Use cowrat.recplaj.recorder.Recorder for
 * audio recording and cowrat.rexplore.hnm.MakeHanmerProfile for
 * activity logging (post-processing) instead. THIS PACKAGE IS NO LONGER USED IN RECOLED AND WILL SOON BE REMOVED.  
*/
public class AudioParticipant {

  String url;
  long timestamp;
  String cname;
  
  public AudioParticipant (String url, long timestamp, String cname){
    this.url = url;
    this.timestamp = timestamp;
    this.cname = cname;
  }

  /**
   * Get the value of url.
   * @return value of url.
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Set the value of url.
   * @param v  Value to assign to url.
   */
  public void setUrl(String  v) {
    this.url = v;
  }
    
  /**
   * Get the value of timestamp.
   * @return value of timestamp.
   */
  public long getTimestamp() {
    return timestamp;
  }
  
  /**
   * Set the value of timestamp.
   * @param v  Value to assign to timestamp.
   */
  public void setTimestamp(long  v) {
    this.timestamp = v;
  }
  
  /**
   * Get the value of cname.
   * @return value of cname.
   */
  public String getCname() {
    return cname;
  }
  
  /**
   * Set the value of cname.
   * @param v  Value to assign to cname.
   */
  public void setCname(String  v) {
    this.cname = v;
  }

  

}
