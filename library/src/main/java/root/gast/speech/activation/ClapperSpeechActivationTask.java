/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.speech.activation;

import java.io.File;
import java.io.IOException;

import root.gast.audio.interp.SingleClapDetector;
import root.gast.audio.record.MaxAmplitudeRecorder;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * utility for executing speech activators who need to run something
 * asyncronously
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class ClapperSpeechActivationTask extends AsyncTask<Void, Void, Boolean>
{
    private static final String TAG = "ClapperSpeechActivationTask";

    private SpeechActivationListener listener;

    private Context context;

    private static final String TEMP_AUDIO_DIRECTORY = "tempaudio";
    
    /**
     * time between amplitude checks
     */
    private static final int CLIP_TIME = 1000;

    public ClapperSpeechActivationTask(Context context,
            SpeechActivationListener listener)
    {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        boolean heard = detectClap();
        return heard;
    }

    /**
     * start detecting a clap, return when done
     */
    private boolean detectClap()
    {
        SingleClapDetector clapper =
                new SingleClapDetector(SingleClapDetector.AMPLITUDE_DIFF_MED);
        Log.d(TAG, "recording amplitude");
        String audioStorageDirectory =
                context.getExternalFilesDir(TEMP_AUDIO_DIRECTORY)
                        + File.separator + "audio.3gp";

        // pass in this so recording can stop if this task is canceled
        MaxAmplitudeRecorder recorder =
                new MaxAmplitudeRecorder(CLIP_TIME, audioStorageDirectory,
                        clapper, this);

        // start recording
        boolean heard = false;
        try
        {
            heard = recorder.startRecording();
        } catch (IOException io)
        {
            Log.e(TAG, "failed to record", io);
            heard = false;
        } catch (IllegalStateException se)
        {
            Log.e(TAG, "failed to record, recorder not setup properly", se);
            heard = false;
        } catch (RuntimeException se)
        {
            Log.e(TAG, "failed to record, recorder already being used", se);
            heard = false;
        }
        return heard;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        listener.activated(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled()
    {
        Log.d(TAG, "cancelled");
        super.onCancelled();
    }
}
