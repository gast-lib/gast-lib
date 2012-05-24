package root.gast.audio.util;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.util.Log;

/**
 * log any callbacks
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class RecorderErrorLoggerListener implements OnErrorListener, OnInfoListener
{
    private static final String D_LOG = RecorderErrorLoggerListener.class.getName();
    
    public void onError(MediaRecorder mr, int what, int extra)
    {
        Log.d(D_LOG, "error in media recorder detected: " + what + " ex: " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN)
        {
            Log.d(D_LOG, "it was a media recorder error unknown");
        }
        else 
        {
            Log.d(D_LOG, "unknown media error");
        }
    }
    
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        Log.d(D_LOG, "info in media recorder detected: " + what + " ex: " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN)
        {
            Log.d(D_LOG, "it was a MEDIA_INFO_UNKNOWN");
        } 
        else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
        {
            Log.d(D_LOG, "it was a MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
        }
        else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
        {
            Log.d(D_LOG, "it was a MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED");
        }
        else 
        {
            Log.d(D_LOG, "unknown info");
        }
    }
}
