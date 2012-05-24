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
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class ClapperActivationExecutor implements SpeechActivator
{
    private static final String TAG = "ClapperActivation";
    
    /**
     * time between amplitude checks
     */
    private static final int CLIP_TIME = 1000;

    private MaxAmplitudeRecorder recorder;

    private Context context;
    
    public ClapperActivationExecutor(Context context)
    {
        this.context = context;
    }
    
    @Override
    public void detectActivation()
    {
        detectClap();
    }
    
    public boolean detectClap()
    {
        //start the task
        SingleClapDetector clapper =
                new SingleClapDetector(SingleClapDetector.AMPLITUDE_DIFF_MED);
        Log.d(TAG, "recording amplitude");
        String appStorageLocation =
                context.getExternalFilesDir("type") + File.separator
                        + "audio.3gp";

        MaxAmplitudeRecorder recorder =
                new MaxAmplitudeRecorder(CLIP_TIME, appStorageLocation,
                        clapper, null);

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
        }
        return heard;
    }
    
    public void stop()
    {
        if (recorder != null)
        {
            recorder.stopRecording();
        }
    }
}
