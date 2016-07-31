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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class TextToSpeechUtils
{
    private static final String TAG = "TextToSpeechUtils";
    
    private static final String NEW_LINE = "\n";

    public static HashMap<String, String> EMPTY_PARAMS = new HashMap<String, String>();
    
    static
    {
        EMPTY_PARAMS = makeParamsWith("dummy_id");
    }
    
    public static HashMap<String, String> makeParamsWith(String key)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, key);
        return params;
    }

    /**
     * need to get the list and then try to return a best match
     */
    private String convertToVoiceCheck(Locale loc, List<String> possibleVoices)
    {
//      // The format of each voice is: lang-COUNTRY-variant where COUNTRY and
//      // variant are optional (ie, "eng" or "eng-USA" or "eng-USA-FEMALE").
        //you can't derive the voice from the loc, the best we can do is a best match
        if (possibleVoices.size() == 0)
        {
            return null;
        }
        //try to match the country?
        String bestMatchVoice = null;

        String countryToMatch = loc.getISO3Language();
        for (String possibleVoice : possibleVoices)
        {
            if (possibleVoice.toLowerCase().contains(countryToMatch))
            {
                bestMatchVoice = possibleVoice;
            }
        }
        
        //handle english because it has a variant
        if (bestMatchVoice.contains("eng"))
        {
                //check country..
                if (loc.getCountry().equals("US"))
                {
                    bestMatchVoice = "eng-USA";
                }
                else
                {
                    bestMatchVoice = "eng-GBR";
                }
        }
        
        return bestMatchVoice;
    }

    public static List<Locale> getLocalesSupported(Context context, TextToSpeech tts)
    {
        List<Locale> supportedLocales = new ArrayList<Locale>();
        for (Locale loc : Locale.getAvailableLocales())
        {
            if (isLanguageAvailable(loc, tts))
            {
                supportedLocales.add(loc);
            }
        }
        return supportedLocales;
    }
    
    private static boolean isLanguageAvailable(Locale language, TextToSpeech tts)
    {
        boolean available = false;
        switch (tts.isLanguageAvailable(language))
        {
            case TextToSpeech.LANG_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                available = true;
                break;
            case TextToSpeech.LANG_MISSING_DATA:
            case TextToSpeech.LANG_NOT_SUPPORTED:
                available = false;
                break;
        }
        return available;
    }

    public static String logOnActivityResultDataCheck(int requestCode, int resultCode, Intent data)
    {
        StringBuilder sb = new StringBuilder();
        if (requestCode == CommonTtsMethods.SPEECH_DATA_CHECK_CODE)
        {
            sb.append("data status: ").append(NEW_LINE);
            
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                sb.append("pass").append(NEW_LINE);
                // success, create the TTS instance
                Log.d(TAG, "has it");
            } else
            {
                sb.append("fail").append(NEW_LINE);
            }
            //
            sb.append("extra info:").append(NEW_LINE);
            if (data.hasExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES))
            {
                List<String> voices = data
                        .getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
                sb.append("available voices: ").append(NEW_LINE);
                for (String voice : voices)
                {
                    sb.append(voice).append(NEW_LINE);
                }
            }

            if (data.hasExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES))
            {
                //does this come back when you try to specify a crazy voice???
                List<String> dataFiles = data
                        .getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);
                sb.append("unavailable voices: ").append(NEW_LINE);
                for (String dataFile : dataFiles)
                {
                    sb.append(dataFile).append(NEW_LINE);
                }
            }
            sb.append(NEW_LINE);

            if (data.hasExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY))
            {
                String rootDir = data
                        .getStringExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY);
                if (rootDir == null)
                {
                    sb.append("data root directory unknown ").append(NEW_LINE);
                }
                else
                {
                    sb.append("data root directory: ").append(NEW_LINE);
                    sb.append(rootDir).append(NEW_LINE);
                }
            }

            if (data.hasExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES))
            {
                String [] dataFiles = data
                        .getStringArrayExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES);
                if (dataFiles == null)
                {
                    sb.append("data files unknown").append(NEW_LINE);
                } else
                {
                    sb.append("data files: ").append(NEW_LINE);
                    for (String dataFile : dataFiles)
                    {
                        sb.append(dataFile)
                                .append(NEW_LINE);
                    }
                }
            }

            if (data.hasExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO))
            {
                String[] info = data
                        .getStringArrayExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO);
                if (info == null)
                {
                    sb.append("data files info unknown").append(NEW_LINE);
                } else
                {
                    sb.append("data files info: ").append(NEW_LINE);
                    for (String dataFile : info)
                    {
                        sb.append(dataFile)
                                .append(NEW_LINE);
                    }
                }
            }

            sb.append("Intent.toString():").append(NEW_LINE);
            Object result = data.getExtras();
            sb.append(result.toString()).append(NEW_LINE);
        }
        
        return sb.toString();
    }


}
