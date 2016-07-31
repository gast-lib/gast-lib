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
package root.gast.speech;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class ActionLanguageDetailsLogger extends BroadcastReceiver
{
    private static final String TAG = "ActionLanguageDetailsLogger";
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle results = getResultExtras(true);
        Log.d(TAG,
                "RECIEVED! get language details broadcast "
                        + results);
    }
    
    public String getDescription()
    {
        Bundle results = getResultExtras(true);
        StringBuilder sb = new StringBuilder();
        if (results
                .containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE))
        {
            String lang = results
                    .getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
            sb.append("Language Preference: ")
                    .append(lang).append("\n");
        }
        if (results
                .containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES))
        {
            List<String> langs = results
                    .getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
            sb.append("languages supported: ").append(
                    "\n");
            for (String lang : langs)
            {
                sb.append(" ").append(lang)
                        .append("\n");
            }
        }
        Log.d(TAG, sb.toString());
        return sb.toString();
    }
}
