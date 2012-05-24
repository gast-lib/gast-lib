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

import android.content.Intent;
import android.speech.RecognizerIntent;

/**
 * helps create {@link RecognizerIntent}s
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class RecognizerIntentFactory
{
    public static final int ACTION_GET_LANGUAGE_DETAILS_REQUEST_CODE = 88811;

    private static final int DEFAUT_MAX_RESULTS = 100;
    
    public static Intent getSimpleRecognizerIntent(String prompt)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        return intent;
    }

    public static Intent getBlankRecognizeIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        return intent;
    }

    public static Intent getWebSearchRecognizeIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        return intent;
    }

    public static Intent getPossilbeWebSearchRecognizeIntent(String prompt)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, DEFAUT_MAX_RESULTS);
        intent.putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY, false);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        intent.putExtra(RecognizerIntent.ORIGIN, "http://www.github.com/gast-lib");
        return intent;
    }

    public static Intent getLanguageDetailsIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        return intent;
    }

}
