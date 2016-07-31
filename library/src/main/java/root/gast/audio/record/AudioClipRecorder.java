package root.gast.audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.util.Log;

/**
 * record an audio clip and pass it to the listener
 * 
 * @author gmilette
 * 
 */
public class AudioClipRecorder
{
    private static final String TAG = "AudioClipRecorder";

    private AudioRecord recorder;
    private AudioClipListener clipListener;

    /**
     * state variable to control starting and stopping recording
     */
    private boolean continueRecording;

    public static final int RECORDER_SAMPLERATE_CD = 44100;
    public static final int RECORDER_SAMPLERATE_8000 = 8000;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3;

    private AsyncTask task;

    private boolean heard;

    public AudioClipRecorder(AudioClipListener clipListener)
    {
        this.clipListener = clipListener;
        heard = false;
        task = null;
    }

    public AudioClipRecorder(AudioClipListener clipListener, AsyncTask task)
    {
        this(clipListener);
        this.task = task;
    }

    /**
     * records with some default parameters
     */
    public boolean startRecording()
    {
        return startRecording(RECORDER_SAMPLERATE_8000,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * start recording: set the parameters that correspond to a buffer that
     * contains millisecondsPerAudioClip milliseconds of samples
     */
    public boolean startRecordingForTime(int millisecondsPerAudioClip,
            int sampleRate, int encoding)
    {
        float percentOfASecond = (float) millisecondsPerAudioClip / 1000.0f;
        int numSamplesRequired = (int) ((float) sampleRate * percentOfASecond);
        int bufferSize =
                determineCalculatedBufferSize(sampleRate, encoding,
                        numSamplesRequired);

        return doRecording(sampleRate, encoding, bufferSize,
                numSamplesRequired, DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    /**
     * start recording: Use a minimum audio buffer and a read buffer of the same
     * size.
     */
    public boolean startRecording(final int sampleRate, int encoding)
    {
        int bufferSize = determineMinimumBufferSize(sampleRate, encoding);
        return doRecording(sampleRate, encoding, bufferSize, bufferSize,
                DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    private int determineMinimumBufferSize(final int sampleRate, int encoding)
    {
        int minBufferSize =
                AudioRecord.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding);
        return minBufferSize;
    }

    /**
     * Calculate audio buffer size such that it holds numSamplesInBuffer and is
     * bigger than the minimum size<br>
     * 
     * @param numSamplesInBuffer
     *            Make the audio buffer size big enough to hold this many
     *            samples
     */
    private int determineCalculatedBufferSize(final int sampleRate,
            int encoding, int numSamplesInBuffer)
    {
        int minBufferSize = determineMinimumBufferSize(sampleRate, encoding);

        int bufferSize;
        // each sample takes two bytes, need a bigger buffer
        if (encoding == AudioFormat.ENCODING_PCM_16BIT)
        {
            bufferSize = numSamplesInBuffer * 2;
        }
        else
        {
            bufferSize = numSamplesInBuffer;
        }

        if (bufferSize < minBufferSize)
        {
            Log.w(TAG, "Increasing buffer to hold enough samples "
                    + minBufferSize + " was: " + bufferSize);
            bufferSize = minBufferSize;
        }

        return bufferSize;
    }

    /**
     * Records audio until stopped the {@link #task} is canceled,
     * {@link #continueRecording} is false, or {@link #clipListener} returns
     * true <br>
     * records audio to a short [readBufferSize] and passes it to
     * {@link #clipListener} <br>
     * uses an audio buffer of size bufferSize * bufferIncreaseFactor
     * 
     * @param recordingBufferSize
     *            minimum audio buffer size
     * @param readBufferSize
     *            reads a buffer of this size
     * @param bufferIncreaseFactor
     *            to increase recording buffer size beyond the minimum needed
     */
    private boolean doRecording(final int sampleRate, int encoding,
            int recordingBufferSize, int readBufferSize, 
            int bufferIncreaseFactor)
    {
        if (recordingBufferSize == AudioRecord.ERROR_BAD_VALUE)
        {
            Log.e(TAG, "Bad encoding value, see logcat");
            return false;
        }
        else if (recordingBufferSize == AudioRecord.ERROR)
        {
            Log.e(TAG, "Error creating buffer size");
            return false;
        }

        // give it extra space to prevent overflow
        int increasedRecordingBufferSize = 
            recordingBufferSize * bufferIncreaseFactor;

        recorder =
                new AudioRecord(AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding,
                        increasedRecordingBufferSize);

        final short[] readBuffer = new short[readBufferSize];

        continueRecording = true;
        Log.d(TAG, "start recording, " + "recording bufferSize: "
                + increasedRecordingBufferSize 
                + " read buffer size: " + readBufferSize);

        //Note: possible IllegalStateException
        //if audio recording is already recording or otherwise not available
        //AudioRecord.getState() will be AudioRecord.STATE_UNINITIALIZED
        recorder.startRecording();
        
        while (continueRecording)
        {
            int bufferResult = recorder.read(readBuffer, 0, readBufferSize);
            //in case external code stopped this while read was happening
            if ((!continueRecording) || ((task != null) && task.isCancelled()))
            {
                break;
            }
            // check for error conditions
            if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION)
            {
                Log.e(TAG, "error reading: ERROR_INVALID_OPERATION");
            }
            else if (bufferResult == AudioRecord.ERROR_BAD_VALUE)
            {
                Log.e(TAG, "error reading: ERROR_BAD_VALUE");
            }
            else
            // no errors, do processing
            {
                heard = clipListener.heard(readBuffer, sampleRate);

                if (heard)
                {
                    stopRecording();
                }
            }
        }
        done();

        return heard;
    }

    public boolean isRecording()
    {
        return continueRecording;
    }

    public void stopRecording()
    {
        continueRecording = false;
    }

    /**
     * need to call this when completely done with recording
     */
    public void done()
    {
        Log.d(TAG, "shut down recorder");
        if (recorder != null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    /**
     * @param audioData
     *            will be filled when reading the audio data
     */
    private void setOnPositionUpdate(final short[] audioData,
            final int sampleRate, int numSamplesInBuffer)
    {
        // possibly do it that way
        // setOnNotification(audioData, sampleRate, numSamplesInBuffer);

        OnRecordPositionUpdateListener positionUpdater =
                new OnRecordPositionUpdateListener()
                {
                    @Override
                    public void onPeriodicNotification(AudioRecord recorder)
                    {
                        // no need to read the audioData again since it was just
                        // read
                        heard = clipListener.heard(audioData, sampleRate);
                        if (heard)
                        {
                            Log.d(TAG, "heard audio");
                            stopRecording();
                        }
                    }

                    @Override
                    public void onMarkerReached(AudioRecord recorder)
                    {
                        Log.d(TAG, "marker reached");
                    }
                };
        // get notified after so many samples collected
        recorder.setPositionNotificationPeriod(numSamplesInBuffer);
        recorder.setRecordPositionUpdateListener(positionUpdater);
    }
}