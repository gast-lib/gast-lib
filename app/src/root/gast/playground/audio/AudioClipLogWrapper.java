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
package root.gast.playground.audio;

import root.gast.audio.interp.LoudNoiseDetector;
import root.gast.audio.processing.ZeroCrossing;
import root.gast.audio.record.AudioClipListener;
import root.gast.audio.util.AudioUtil;
import android.app.Activity;
import android.widget.TextView;

/**
 * Helps log information during {@link ClapperPlay}
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class AudioClipLogWrapper implements AudioClipListener
{
    private TextView log;

    private Activity context;
    
    private double previousFrequency = -1;

    public AudioClipLogWrapper(TextView log, Activity context)
    {
        this.log = log;
        this.context = context;
    }

    @Override
    public boolean heard(short[] audioData, int sampleRate)
    {
        final double zero = ZeroCrossing.calculate(sampleRate, audioData);
        final double volume = AudioUtil.rootMeanSquared(audioData);
        
        final boolean isLoudEnough = volume > LoudNoiseDetector.DEFAULT_LOUDNESS_THRESHOLD;
        //range threshold of 100
        final boolean isDifferentFromLast = Math.abs(zero - previousFrequency) > 100; 

        final StringBuilder message = new StringBuilder();
        message.append("volume: ").append((int)volume);
        if (!isLoudEnough)
        {
            message.append(" (silence) ");
        }
        message.append(" freqency: ").append((int)zero);
        if (isDifferentFromLast)
        {
            message.append(" (diff)");
        }
        
        previousFrequency = zero;

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AudioTaskUtil.appendToStartOfLog(log, message.toString());
            }
        });
        return false;
    }
}
