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
package root.gast.audio.interp;

import root.gast.audio.record.AudioClipListener;
import android.util.Log;

/**
 * alternative Loud Noise detector that tracks the difference between
 * the new noise and an averagre value. It might be useful in some situations.
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class LoudNoiseDetectorAboveNormal implements AudioClipListener
{
    private static final String TAG = "MultipleClapDetector";

    private double averageVolume;

    private double lowPassAlpha = 0.5;

    private double STARTING_AVERAGE = 100.0;

    private double INCREASE_FACTOR = 100.0;

    private static final boolean DEBUG = true;

    public LoudNoiseDetectorAboveNormal()
    {
        averageVolume = STARTING_AVERAGE;
    }

    @Override
    public boolean heard(short[] data, int sampleRate)
    {
        boolean heard = false;
        // use rms to take the entire audio signal into account
        // and discount any one single high amplitude
        double currentVolume = rootMeanSquared(data);

        double volumeThreshold = averageVolume * INCREASE_FACTOR;
        if (DEBUG)
        {
            Log.d(TAG, "current: " + currentVolume + " avg: " + averageVolume
                    + " threshold: " + volumeThreshold);
        }

        if (currentVolume > volumeThreshold)
        {
            Log.d(TAG, "heard");
            heard = true;
        }
        else
        {
            // Big changes should have very little affect on
            // the average value but if the average volume does increase
            // consistently let the average increase too
            averageVolume = lowPass(currentVolume, averageVolume);
        }

        return heard;
    }

    private double lowPass(double current, double last)
    {
        return last * (1.0 - lowPassAlpha) + current * lowPassAlpha;
    }

    private double rootMeanSquared(short[] nums)
    {
        double ms = 0;
        for (int i = 0; i < nums.length; i++)
        {
            ms += nums[i] * nums[i];
        }
        ms /= nums.length;
        return Math.sqrt(ms);
    }
}
