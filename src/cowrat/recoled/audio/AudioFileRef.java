package cowrat.recoled.audio;
/**
 *  Summary of attributes of an audio file (location, start time, etc)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: AudioFileRef.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated These JMF-based classes have been dropped in favour of
 * our own implementations. Use cowrat.recplaj.recorder.Recorder for
 * audio recording and cowrat.rexplore.hnm.MakeHanmerProfile for
 * activity logging (post-processing) instead. THIS PACKAGE IS NO LONGER USED IN RECOLED AND WILL SOON BE REMOVED.
*/
public class AudioFileRef {

  String url;
  long timestamp;
  String uname;
  
  public AudioFileRef (String url, long timestamp, String uname){
    this.url = url;
    this.timestamp = timestamp;
    this.uname = uname;
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
   * Get the value of uname.
   * @return value of uname.
   */
  public String getUname() {
    return uname;
  }
  
  /**
   * Set the value of uname.
   * @param v  Value to assign to uname.
   */
  public void setUname(String  v) {
    this.uname = v;
  }

  

}
