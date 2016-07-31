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

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

/**
 * Helps construct an initalized {@link TextToSpeech} using the 
 * TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class TextToSpeechInitializerByAction
{
    private static final String TAG = "TextToSpeechInitializerByAction";

    private TextToSpeech tts;

    private TextToSpeechStartupListener callback;

    private Activity activity;

    private Locale targetLocale;
    
    /**
     * creates by checking {@link TextToSpeech#isLanguageAvailable(Locale)}
     */
    public TextToSpeechInitializerByAction(Activity activity, String voiceToCheck,
            TextToSpeechStartupListener callback, Locale targetLocale)
    {
        this.callback = callback;
        this.activity = activity;
        this.targetLocale = targetLocale;
        startDataCheck(activity, voiceToCheck);
    }

    /**
     * version of the constructor that converts the {@link Locale} 
     * to the proper voice to check
     */
    public TextToSpeechInitializerByAction(Activity activity,
            TextToSpeechStartupListener callback, Locale targetLocale)
    {
        this(activity, convertLocaleToVoice(targetLocale),  
                callback, targetLocale);
    }

    /**
     * voice name as defined by
     * {@link TextToSpeech.Engine#EXTRA_CHECK_VOICE_DATA_FOR}
     */
    public void startDataCheck(Activity activity, String voiceToCheck)
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        Log.d(TAG, "launching speech check");
        if (voiceToCheck != null && voiceToCheck.length() > 0)
        {
            Log.d(TAG, "adding voice check for: " + voiceToCheck);
            // needs to be in an ArrayList
            ArrayList<String> voicesToCheck = new ArrayList<String>();
            voicesToCheck.add(voiceToCheck);
            check.putStringArrayListExtra(
                    TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR,
                    voicesToCheck);
        }
        activity.startActivityForResult(check,
                CommonTtsMethods.SPEECH_DATA_CHECK_CODE);
    }

    /**
     * handle onActivityResult from call to
     * {@link #startDataCheck(Activity, String)}
     */
    public void handleOnActivityResult(Context launchFrom,
            int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CommonTtsMethods.SPEECH_DATA_CHECK_CODE)
        {
            switch (resultCode)
            {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    // success, create the TTS instance
                    Log.d(TAG, "has language data");
                    tts = new TextToSpeech(launchFrom, new OnInitListener()
                    {
                        @Override
                        public void onInit(int status)
                        {
                            if (targetLocale != null)
                            {
                                tts.setLanguage(targetLocale);
                            }
                            if (status == TextToSpeech.SUCCESS)
                            {
                                callback.onSuccessfulInit(tts);
                            } else
                            {
                                callback.onFailedToInit();
                            }
                        }
                    });
                    break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
                    Log.d(TAG, "no language data");
                    callback.onRequireLanguageData();
            }
        }
    }

    public void installLanguageData()
    {
        // waiting for the download
        LanguageDataInstallBroadcastReceiver.setWaiting(activity, true);

        // don't actually do it in test mode, just register a receiver
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        activity.startActivity(installIntent);
    }

    public static String convertLocaleToVoice(Locale loc)
    {
        // The format of each voice is: lang-COUNTRY-variant where COUNTRY and
        // variant are optional (ie, "eng" or "eng-USA" or "eng-USA-FEMALE").
        String country = loc.getISO3Country();
        String language = loc.getISO3Language();
        StringBuilder sb = new StringBuilder();
        sb.append(language);
        if (country.length() > 0)
        {
            sb.append("-");
            sb.append(country);
        }
        return sb.toString();
    }
}
