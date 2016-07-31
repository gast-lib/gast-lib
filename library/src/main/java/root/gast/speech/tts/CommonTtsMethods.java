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
package root.gast.speech.tts;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class CommonTtsMethods
{
    private static final String TAG = "CommonTtsMethods";

    private static final String NEW_LINE = "\n";
    
    public static final int SPEECH_DATA_CHECK_CODE = 19327;

    /**
     * start the language data install
     */
    public static void installLanguageData(final Context context)
    {
        //waiting for the download
        LanguageDataInstallBroadcastReceiver.setWaiting(context, true);

        //don't actually do it in test mode, just register a receiver
        Intent installIntent = new Intent();
        installIntent.setAction(
            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        context.startActivity(installIntent);
        
    }
    
    /**
     * get a descriptions of all the languages available as determined by
     * {@link TextToSpeech#isLanguageAvailable(Locale)}
     */
    public static String getLanguageAvailableDescription(TextToSpeech tts)
    {
        StringBuilder sb = new StringBuilder();
        for (Locale loc : Locale.getAvailableLocales())
        {
            int availableCheck = tts.isLanguageAvailable(loc);
            sb.append(loc.toString()).append(" ");
            switch (availableCheck)
            {
                case TextToSpeech.LANG_AVAILABLE:
                    break;
                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                    sb.append("COUNTRY_AVAILABLE");
                    break;
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                    sb.append("COUNTRY_VAR_AVAILABLE");
                    break;
                case TextToSpeech.LANG_MISSING_DATA:
                    sb.append("MISSING_DATA");
                    break;
                case TextToSpeech.LANG_NOT_SUPPORTED:
                    sb.append("NOT_SUPPORTED");
                    break;
            }
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    public static void startDataCheck(Activity callingActivity)
    {
        Log.d(TAG, "launching speech check");        
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        callingActivity.startActivityForResult(checkIntent,
                SPEECH_DATA_CHECK_CODE);
    }
}
