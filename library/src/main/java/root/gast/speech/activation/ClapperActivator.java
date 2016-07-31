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

import android.content.Context;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class ClapperActivator implements SpeechActivator
{
    private static final String TAG = "ClapperActivator";

    private ClapperSpeechActivationTask activeTask;
    private SpeechActivationListener listener;
    private Context context;

    public ClapperActivator(Context context, SpeechActivationListener listener)
    {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void detectActivation()
    {
        Log.d(TAG, "started clapper activation");
        activeTask = new ClapperSpeechActivationTask(context, listener);
        activeTask.execute();
    }

    @Override
    public void stop()
    {
        if (activeTask != null)
        {
            activeTask.cancel(true);
        }
    }
}
