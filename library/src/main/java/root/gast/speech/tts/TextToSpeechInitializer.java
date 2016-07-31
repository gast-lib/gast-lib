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

import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

/**
 * Helps construct an initalized {@link TextToSpeech}
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class TextToSpeechInitializer
{
    private static final String TAG = "TextToSpeechInitializer";

    private TextToSpeech tts;

    private TextToSpeechStartupListener callback;

    private Context context;

    /**
     * creates by checking {@link TextToSpeech#isLanguageAvailable(Locale)}
     */
    public TextToSpeechInitializer(Context context, final Locale loc,
            TextToSpeechStartupListener callback)
    {
        this.callback = callback;
        this.context = context;
        createTextToSpeech(loc);
    }

    /**
     * creating a TTS
     * @param locale {@link Locale} for the desired language
     */
    private void createTextToSpeech(final Locale locale)
    {
        tts = new TextToSpeech(context, new OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    setTextToSpeechSettings(locale);
                } else
                {
                    Log.e(TAG, "error creating text to speech");
                    callback.onFailedToInit();
                }
            }
        });
    }
    
    /**
     * perform the initialization checks after the 
     * TextToSpeech is done
     */
    private void setTextToSpeechSettings(final Locale locale)
    {
        Locale defaultOrPassedIn = locale;
        if (locale == null)
        {
            defaultOrPassedIn = Locale.getDefault();
        }
        // check if language is available
        switch (tts.isLanguageAvailable(defaultOrPassedIn))
        {
            case TextToSpeech.LANG_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                Log.d(TAG, "SUPPORTED");
                tts.setLanguage(locale);
                callback.onSuccessfulInit(tts);
                break;
            case TextToSpeech.LANG_MISSING_DATA:
                Log.d(TAG, "MISSING_DATA");
                // check if waiting, by checking
                // a shared preference
                if (LanguageDataInstallBroadcastReceiver
                        .isWaiting(context))
                {
                    Log.d(TAG, "waiting for data...");
                    callback.onWaitingForLanguageData();
                } else
                {
                    Log.d(TAG, "require data...");
                    callback.onRequireLanguageData();
                }
                break;
            case TextToSpeech.LANG_NOT_SUPPORTED:
                Log.d(TAG, "NOT SUPPORTED");
                callback.onFailedToInit();
                break;
        }
    }

    public void setOnUtteranceCompleted(OnUtteranceCompletedListener whenDoneSpeaking)
    {
        int result = tts.setOnUtteranceCompletedListener(whenDoneSpeaking);
        if (result == TextToSpeech.ERROR)
        {
            Log.e(TAG, "failed to add utterance listener");
        }
    }

    /**
     * helper method to display a dialog if waiting for a download
     */
    public void installLanguageData(Dialog ifAlreadyWaiting)
    {
        boolean alreadyDidThis = LanguageDataInstallBroadcastReceiver
                .isWaiting(context);
        if (alreadyDidThis)
        {
            // give the user the ability to try again..
            // throw exception here?
            ifAlreadyWaiting.show();
        } else
        {
            installLanguageData();
        }
    }

    public void installLanguageData()
    {
        // set waiting for the download
        LanguageDataInstallBroadcastReceiver.setWaiting(context, true);

        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        context.startActivity(installIntent);
    }
}
