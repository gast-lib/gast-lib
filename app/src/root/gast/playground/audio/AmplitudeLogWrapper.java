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

import android.app.Activity;
import android.widget.TextView;
import root.gast.audio.record.AmplitudeClipListener;

/**
 * Helps log information during {@link ClapperPlay}
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class AmplitudeLogWrapper implements AmplitudeClipListener
{
    private TextView log;
    
    private Activity context;
    
    public AmplitudeLogWrapper(TextView log, Activity context)
    {
        this.log = log;
        this.context = context;
    }

    public boolean heard(final int maxAmplitude) 
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AudioTaskUtil.appendToStartOfLog(log, "amplitude: " + maxAmplitude);
            }
        });

        return false;
    }
}
