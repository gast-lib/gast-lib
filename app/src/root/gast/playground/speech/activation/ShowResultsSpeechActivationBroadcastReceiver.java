/*
 * Copyright 2011 Greg Milette and Adam Stroud
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
package root.gast.playground.speech.activation;

import root.gast.playground.speech.SpeechRecognitionLauncher;
import root.gast.speech.activation.SpeechActivationService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShowResultsSpeechActivationBroadcastReceiver extends
        BroadcastReceiver
{
    private static final String TAG =
            "ShowResultsSpeechActivationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(
                SpeechActivationService.ACTIVATION_RESULT_BROADCAST_NAME))
        {
            if (intent
                    .getBooleanExtra(
                            SpeechActivationService.ACTIVATION_RESULT_INTENT_KEY,
                            false))
            {
                Log.d(TAG,
                        "ShowResultsSpeechActivationBroadcastReceiver taking action");
                // launch something that prompts the user...
                Intent i = new Intent(context, SpeechRecognitionLauncher.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
