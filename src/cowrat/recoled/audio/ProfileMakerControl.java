package cowrat.recoled.audio;

/**
 *  Interface to be implemented by everyone wishing to control
 *  ProfileMaker. (NB: not a very clean implementation: the controller
 *  must in addition get a ref to pm and setup, start and stop it as
 *  needed)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: ProfileMakerControl.java,v 1.1 2005/10/22 11:09:14 amaral Exp $</font>
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated The JMF-based classes in this package have been dropped
 * in favour of our own implementations. Use
 * cowrat.recplaj.recorder.Recorder for audio recording and
 * cowrat.rexplore.hnm.MakeHanmerProfile for activity logging
 * (post-processing) instead. THIS PACKAGE IS NO LONGER USED IN RECOLED AND WILL SOON BE REMOVED.
*/
public interface ProfileMakerControl {

  public void addParticipant(String cname);

  public void removeParticipant(String cname);

  public void hiliteActiveParticipant(String cname);

  public void unhiliteInactiveParticipant(String cname);
}
