package root.gast.audio.record;

import java.io.IOException;

import root.gast.audio.util.AudioUtil;
import root.gast.audio.util.RecorderErrorLoggerListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Records {@link MediaRecorder#getMaxAmplitude()}
 * @author gmilette
 * 
 */
public class MaxAmplitudeRecorder
{
    private static final String TAG = "MaxAmplitudeRecorder";

    private static final long DEFAULT_CLIP_TIME = 1000;
    private long clipTime = DEFAULT_CLIP_TIME;

    private AmplitudeClipListener clipListener;

    private boolean continueRecording;

    private MediaRecorder recorder;

    private String tmpAudioFile;

    private AsyncTask task;

    /**
     * 
     * @param clipTime
     *            time to wait in between maxAmplitude checks
     * @param tmpAudioFile
     *            should be a file where the MediaRecorder class can write 
     *            temporary audio data
     * @param clipListener
     *            called periodically to analyze the max amplitude
     * @param task
     *            stop recording if this task is canceled
     */
    public MaxAmplitudeRecorder(long clipTime, String tmpAudioFile,
            AmplitudeClipListener clipListener, AsyncTask task)
    {
        this.clipTime = clipTime;
        this.clipListener = clipListener;
        this.tmpAudioFile = tmpAudioFile;
        this.task = task;
    }

    /**
     * start recording maximum amplitude and passing it to the
     * {@link #clipListener} <br>
     * @throws {@link IllegalStateException} if there is trouble creating
     * the recorder
     * @throws {@link IOException} if the SD card is not available
     * @throws {@link RuntimeException} if audio recording channel is occupied
     * @return true if {@link #clipListener} succeeded in detecting something
     * false if it failed or the recording stopped for some other reason
     */
    public boolean startRecording() throws IOException
    {
        Log.d(TAG, "recording maxAmplitude");

        recorder = AudioUtil.prepareRecorder(tmpAudioFile);

        // when an error occurs just stop recording
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener()
        {
            @Override
            public void onError(MediaRecorder mr, int what, int extra)
            {
                // log it
                new RecorderErrorLoggerListener().onError(mr, what, extra);
                // stop recording
                stopRecording();
            }
        });

        //possible RuntimeException if Audio recording channel is occupied
        recorder.start();
        
        continueRecording = true;
        boolean heard = false;
        recorder.getMaxAmplitude();
        while (continueRecording)
        {
            Log.d(TAG, "waiting while recording...");
            waitClipTime();
            if (task != null)
            {
                Log.d(TAG, "continue recording: " + continueRecording + " cancelled after waiting? " + task.isCancelled());
            }
            //in case external code stopped this while read was happening
            if ((!continueRecording) || ((task != null) && task.isCancelled()))
            {
                break;
            }

            int maxAmplitude = recorder.getMaxAmplitude();
            Log.d(TAG, "current max amplitude: " + maxAmplitude);

            heard = clipListener.heard(maxAmplitude);
            if (heard)
            {
                stopRecording();
            }
        }

        Log.d(TAG, "stopped recording max amplitude");
        done();

        return heard;
    }

    private void waitClipTime()
    {
        try
        {
            Thread.sleep(clipTime);
        } catch (InterruptedException e)
        {
            Log.d(TAG, "interrupted");
        }
    }

    /**
     * stop recorder and clean up resources
     */
    public void done()
    {
        Log.d(TAG, "stop recording on done");
        if (recorder != null)
        {
            try
            {
                recorder.stop();
            } catch (Exception e)
            {
                Log.d(TAG, "failed to stop");
                return;
            }
            recorder.release();
        }
    }

    public boolean isRecording()
    {
        return continueRecording;
    }

    public void stopRecording()
    {
        continueRecording = false;
    }
}
