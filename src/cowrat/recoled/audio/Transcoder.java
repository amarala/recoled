package cowrat.recoled.audio;

import java.awt.*;
import java.util.Vector;
import java.io.File;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.Format;
import javax.media.format.*;
import javax.media.datasink.*;
import javax.media.protocol.*;
import java.io.IOException;
import com.sun.media.format.WavAudioFormat;


/**
 * A sample program to receive RTP transmission, transcode the stream to an output
 * location with a default data format.
 * @see cowrat.recplaj.recorder.Recorder
 * @see cowrat.rexplor.hnm.MakeHanmerProfile
 * @deprecated The JMF-based classes in this package have been dropped in favour of
 * our own implementations. Use cowrat.recplaj.recorder.Recorder for
 * audio recording and cowrat.rexplore.hnm.MakeHanmerProfile for
 * activity logging (post-processing) instead. THIS PACKAGE IS NO LONGER USED IN RECOLED AND WILL SOON BE REMOVED.
 */
public class Transcoder extends Thread implements ControllerListener, DataSinkListener {

  public Processor thisProcessor;
  private DataSink dsink;
  private boolean ready_to_start = true;
  private String info = null;
  public boolean fileDone = false;
  public boolean completed = false;
  
  public Transcoder(Processor p)
  {
    thisProcessor = p;
  }
  
  public Transcoder (Processor p, String url)
  {
    thisProcessor = p;
    info = url;
    
    MediaLocator outML = createMediaLocator(info);
    
    thisProcessor.addControllerListener(this);
    
    System.out.println("ADDED Control listerener");
    
    // Put the Processor into configured state.
    thisProcessor.configure();
    if (!waitForState(thisProcessor, thisProcessor.Configured)) {
      System.err.println("Failed to configure the processor.");
      ready_to_start = false;
    }
    
    System.out.println("processor configured");

    // Set the output content descriptor based on the media locator.
    setContentDescriptor(thisProcessor, outML);
    
    // Program the tracks.
    if (!setTrackFormats(thisProcessor))
      ready_to_start = false;
    
    // We are done with programming the processor.  Let's just
    // realize it.
    thisProcessor.realize();
    if (!waitForState(thisProcessor, thisProcessor.Realized)) {
      System.err.println("Failed to realize the processor.");
      ready_to_start = false;
    }

    System.out.println("processor realised");

    // Now, we'll need to create a DataSink.
    if ((dsink = createDataSink(p, outML)) == null) {
      System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
      ready_to_start = false;
    }

    System.out.println("Created Data Sink");

    dsink.addDataSinkListener(this);
  }

  public void run()
  {
    doIt();
  }

  /**
   * Given a source media locator, destination media locator and a duration
   * this method will receive RTP transmission, transcode the source to the
   * destination into a default format.
   */
  public boolean doIt() {

    System.err.println("start transcoding ...");

    // OK, we can now start the actual transcoding.
    try {
      thisProcessor.start();
      dsink.start();
    } catch (IOException e) {
      System.err.println("IO error during transcoding");
      return false;
    }

    // Wait for EndOfStream event.
    waitForFileDone();

    // Cleanup.
    try {
      thisProcessor.stop();
      dsink.close();
    } catch (Exception e) {}
    thisProcessor.removeControllerListener(this);

    System.err.println("...done RTPExporting.");
    completed = true;

    return true;
  }


  /**
   * Set the content descriptor based on the given output MediaLocator.
   */
  void setContentDescriptor(Processor p, MediaLocator outML) {

    ContentDescriptor cd;

    // If the output file maps to a content type,
    // we'll try to set it on the processor.

    System.out.println("SETTING CONTENT DESCRIPTOR");
    if ((cd = fileExtToCD(outML.getRemainder())) != null) {

      System.err.println("- set content descriptor to: " + cd);

      if ((p.setContentDescriptor(cd)) == null) {

        // The processor does not support the output content
        // type.  But we can set the content type to RAW and
        // see if any DataSink supports it.

        p.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW));
      }
    }
  }


  /**
   * Set the target transcode format on the processor.
   */
  boolean setTrackFormats(Processor p) {

    Format supported[];

    TrackControl [] tracks = p.getTrackControls();

    // Do we have at least one track?
    if (tracks == null || tracks.length < 1) {
      System.err.println("Couldn't find tracks in processor");
      return false;
    }

    for (int i = 0; i < tracks.length; i++) {
      if (tracks[i].isEnabled()) {
        supported = tracks[i].getSupportedFormats();
        if (supported.length > 0) {
          tracks[i].setFormat(supported[0]);
        } else {
          System.err.println("Cannot transcode track [" + i + "]");
          tracks[i].setEnabled(false);
          return false;
        }
      } else {
        tracks[i].setEnabled(false);
        return false;
      }
    }
    return true;
  }

  /**
   * Create the DataSink.
   */
  DataSink createDataSink(Processor p, MediaLocator outML) {

    DataSource ds;

    if ((ds = p.getDataOutput()) == null) {
      System.err.println("Something is really wrong: the processor does not have an output DataSource");
      return null;
    }

    DataSink dsink;

    try {
      System.err.println("- create DataSink fo: " + outML);
      dsink = Manager.createDataSink(ds, outML);
      dsink.open();
    } catch (Exception e) {
      System.err.println("Cannot create the DataSink: " + e);
      return null;
    }

    return dsink;
  }


 
  
  
  Object waitSync = new Object();
  boolean stateTransitionOK = true;
  
  /**
   * Block until the processor has transitioned to the given state.
   * Return false if the transition failed.
   */
  boolean waitForState(Processor p, int state) {
    synchronized (waitSync) {
      try {
        while (p.getState() < state && stateTransitionOK)
          {
            System.out.println("p.getState: " + 
                               p.getState() + 
                               " Input state " + 
                               state + " STOK " + 
                               stateTransitionOK);
            Thread.sleep(200);
          }
        System.out.println("p.getState: " + 
                           p.getState() + 
                           " Imput state " + 
                           state + " STOK " + 
                           stateTransitionOK);
      } catch (Exception e) {}
    }
    System.out.println("GET STATE got");
    return stateTransitionOK;
  } 


  /**
   * Controller Listener.
   */
  public void controllerUpdate(ControllerEvent evt) {

    if (evt instanceof ConfigureCompleteEvent ||
        evt instanceof RealizeCompleteEvent ||
        evt instanceof PrefetchCompleteEvent) {
      synchronized (waitSync) {
        stateTransitionOK = true;
        waitSync.notifyAll();
      }
    } else if (evt instanceof ResourceUnavailableEvent) {
      synchronized (waitSync) {
        stateTransitionOK = false;
        waitSync.notifyAll();
      }
    } else if (evt instanceof MediaTimeSetEvent) {
      System.err.println("- mediaTime set: " +
                         ((MediaTimeSetEvent)evt).getMediaTime().getSeconds());
    } else if (evt instanceof StopAtTimeEvent) {
      System.err.println("- stop at time: " +
                         ((StopAtTimeEvent)evt).getMediaTime().getSeconds());
      evt.getSourceController().close();
    } else if (evt instanceof DurationUpdateEvent) {
      System.out.println("DURATION UPDATE");
    } else if (evt instanceof CachingControlEvent) {
      System.out.println("Caching Event");
    }
  }


  Object waitFileSync = new Object();
  boolean fileSuccess = true;

  /**
   * Block until file writing is done.
   */
  boolean waitForFileDone() {
    System.err.print("  ");
    synchronized (waitFileSync) {
      try {
        int i = 0;
        while (!fileDone) {
          waitFileSync.wait(1000);
          //System.err.print(i);
          i++;
          if(i > 10)
            i = 0;
        }
        thisProcessor.close();
      } catch (Exception e) {}
    }
    System.err.println("");
    return fileSuccess;
  }

  /**
   * Event handler for the file writer.
   */
  public void dataSinkUpdate(DataSinkEvent evt) {

    if (evt instanceof EndOfStreamEvent) {
      synchronized (waitFileSync) {
        System.out.println("IN HERE");
        fileDone = true;
        waitFileSync.notifyAll();
      }
    } else if (evt instanceof DataSinkErrorEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        fileSuccess = false;
        waitFileSync.notifyAll();
      }
    }
  }


  /**
   * Convert a file name to a content type.  The extension is parsed
   * to determine the content type.
   */
  ContentDescriptor fileExtToCD(String name) {

    String ext;
    int p;

    // Extract the file extension.
    if ((p = name.lastIndexOf('.')) < 0)
      return null;

    ext = (name.substring(p + 1)).toLowerCase();

    String type;

    // Use the MimeManager to get the mime type from the file extension.
    if ( ext.equals("mp3")) {
      type = FileTypeDescriptor.MPEG_AUDIO;
    } else {
      if ((type = com.sun.media.MimeManager.getMimeType(ext)) == null)
        return null;
      type = ContentDescriptor.mimeTypeToPackageName(type);
    }

    return new FileTypeDescriptor(type);
  }

  /**
   * Create a media locator from the given string.
   */
  static MediaLocator createMediaLocator(String url) {

    MediaLocator ml;

    if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
      return ml;

    if (url.startsWith(File.separator)) {
      if ((ml = new MediaLocator("file:" + url)) != null)
        return ml;
    } else {
      String file = "file:" + System.getProperty("user.dir") + File.separator + url;
      if ((ml = new MediaLocator(file)) != null)
        return ml;
    }

    return null;
  }
}
