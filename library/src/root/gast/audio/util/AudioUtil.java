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
package root.gast.audio.util;

import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/**
 * various utility methods for audio processing
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class AudioUtil
{
    private static final String TAG = "AudioUtil";
    
    /**
     * creates a media recorder, or throws a {@link IOException} if 
     * the path is not valid. 
     * @param sdCardPath should contain a .3gp extension
     */
    public static MediaRecorder prepareRecorder(String sdCardPath)
            throws IOException
    {
        if (!isStorageReady())
        {
            throw new IOException("SD card is not available");
        }

        MediaRecorder recorder = new MediaRecorder();
        //set a custom listener that just logs any messages
        RecorderErrorLoggerListener recorderListener =
                new RecorderErrorLoggerListener();
        recorder.setOnErrorListener(recorderListener);
        recorder.setOnInfoListener(recorderListener);

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        Log.d(TAG, "recording to: " + sdCardPath);
        recorder.setOutputFile(sdCardPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.prepare();
        return recorder;
    }
    
    private static boolean isStorageReady() 
    {
            String cardstatus = Environment.getExternalStorageState();
            if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            {
                return false;
            }
            else
            {
                if (cardstatus.equals(Environment.MEDIA_MOUNTED))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
    }
    
    public static boolean hasMicrophone(Context context)
    {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }
    
    public static boolean isSilence(short [] data)
    {
        boolean silence = false;
        int RMS_SILENCE_THRESHOLD = 2000;
        if (rootMeanSquared(data) < RMS_SILENCE_THRESHOLD) 
        {
            silence = true;
        }
        return silence;
    }
    
    public static double rootMeanSquared(short[] nums)
    {
        double ms = 0;
        for (int i = 0; i < nums.length; i++)
        {
            ms += nums[i] * nums[i];
        }
        ms /= nums.length;
        return Math.sqrt(ms);
    }
    
    public static int countZeros(short [] audioData)
    {
        int numZeros = 0;
        
        for (int i = 0; i < audioData.length; i++)
        {
            if (audioData[i] == 0)
            {
                numZeros++;
            }
        }
        
        return numZeros;
    }

    public static double secondsPerSample(int sampleRate)
    {
        return 1.0/(double)sampleRate;
    }
    
    public static int numSamplesInTime(int sampleRate, float seconds)
    {
        return (int)((float)sampleRate * (float)seconds);
    }
    
    public static void outputData(short [] data, PrintWriter writer)
    {
        for (int i = 0; i < data.length; i++)
        {
            writer.println(String.valueOf(data[i]));
        }
        if (writer.checkError())
        {
            Log.w(TAG, "Error writing sensor event data");
        }
    }
}
