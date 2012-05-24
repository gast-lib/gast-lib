/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.speech;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * abstract class for getting speech, handles some boiler plate code. Any
 * Activity that uses speech should extend this, or utilize the methods in
 * {@link SpeechRecognitionUtil}
 * 
 * @author gmilette
 */
public abstract class SpeechRecognizingActivity extends Activity implements
        RecognitionListener
{
    private static final String TAG = "SpeechRecognizingActivity";

    /**
     * code to identify return recognition results
     */
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    public static final int UNKNOWN_ERROR = -1;

    private SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        boolean recognizerIntent =
                SpeechRecognitionUtil.isSpeechAvailable(this);
        if (!recognizerIntent)
        {
            speechNotAvailable();
        }
        boolean direct = SpeechRecognizer.isRecognitionAvailable(this);
        if (!direct)
        {
            directSpeechNotAvailable();
        }
    }

    protected void checkForLanguage(final Locale language)
    {
        OnLanguageDetailsListener andThen = new OnLanguageDetailsListener()
        {
            @Override
            public void onLanguageDetailsReceived(LanguageDetailsChecker data)
            {
                // do a best match
                String languageToUse = data.matchLanguage(language);
                languageCheckResult(languageToUse);
            }
        };
        SpeechRecognitionUtil.getLanguageDetails(this, andThen);
    }

    /**
     * execute the RecognizerIntent, then call
     * {@link #receiveWhatWasHeard(List, List)} when done
     */
    public void recognize(Intent recognizerIntent)
    {
        startActivityForResult(recognizerIntent, 
                VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the RecognizerIntent.
     */
    @Override
    protected void
            onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                List<String> heard =
                        data.
                        getStringArrayListExtra
                                (RecognizerIntent.EXTRA_RESULTS);
                float[] scores =
                        data.
                        getFloatArrayExtra
                                (RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
                if (scores == null)
                {
                    for (int i = 0; i < heard.size(); i++)
                    {
                        Log.d(TAG, i + ": " + heard.get(i));
                    }
                }
                else
                {
                    for (int i = 0; i < heard.size(); i++)
                    {
                        Log.d(TAG, i + ": " + heard.get(i) + " score: "
                                + scores[i]);
                    }
                }

                receiveWhatWasHeard(heard, scores);
            }
            else
            {
                Log.d(TAG, "error code: " + resultCode);
                recognitionFailure(UNKNOWN_ERROR);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * called when speech is not available on this device, and when
     * {@link #recognize(Intent)} will not work
     */
    abstract protected void speechNotAvailable();

    /**
     * called when {@link SpeechRecognizer} cannot be used on this device and
     * {@link #recognizeDirectly(Intent)} will not work
     */
    abstract protected void directSpeechNotAvailable();

    /**
     * call back the result from {@link #checkForLanguage(Locale)}
     * 
     * @param languageToUse
     *            the language string to use or null if failure
     */
    abstract protected void languageCheckResult(String languageToUse);

    /**
     * result of speech recognition
     * 
     * @param heard
     *            possible speech to text conversions
     * @param confidenceScores
     *            the confidence for the strings in heard
     */
    abstract protected void receiveWhatWasHeard(List<String> heard,
            float[] confidenceScores);

    /**
     * @param code
     *            If using {@link #recognizeDirectly(Intent) it will be
     *            the error code from {@link SpeechRecognizer} 
     *            if using {@link #recognize(Intent)} 
     *            it will be {@link #UNKNOWN_ERROR}.
     */
    abstract protected void recognitionFailure(int errorCode);

    //direct speech recognition methods follow
    
    /**
     * Uses {@link SpeechRecognizer} to perform recognition and then calls
     * {@link #receiveWhatWasHeard(List, float[])} with the results <br>
     * check {@link SpeechRecognizer.isRecognitionAvailable(context)} before
     * calling this method otherwise if it isn't available the code will report
     * an error
     */
    public void recognizeDirectly(Intent recognizerIntent)
    {
        // SpeechRecognizer requires EXTRA_CALLING_PACKAGE, so add if it's not
        // here
        if (!recognizerIntent.hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE))
        {
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "com.dummy");
        }
        SpeechRecognizer recognizer = getSpeechRecognizer();
        recognizer.startListening(recognizerIntent);
    }

    @Override
    public void onResults(Bundle results)
    {
        Log.d(TAG, "full results");
        receiveResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "partial results");
        receiveResults(partialResults);
    }

    /**
     * common method to process any results bundle from {@link SpeechRecognizer}
     */
    private void receiveResults(Bundle results)
    {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))
        {
            List<String> heard =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores =
                    results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            receiveWhatWasHeard(heard, scores);
        }
    }

    @Override
    public void onError(int errorCode)
    {
        recognitionFailure(errorCode);
    }

    /**
     * stop the speech recognizer
     */
    @Override
    protected void onPause()
    {
        if (getSpeechRecognizer() != null)
        {
            getSpeechRecognizer().stopListening();
            getSpeechRecognizer().cancel();
            getSpeechRecognizer().destroy();
        }
        super.onPause();
    }

    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer()
    {
        if (recognizer == null)
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
        }
        return recognizer;
    }

    // other unused methods from RecognitionListener...

    @Override
    public void onReadyForSpeech(Bundle params)
    {
        Log.d(TAG, "ready for speech " + params);
    }

    @Override
    public void onEndOfSpeech()
    {
    }

    /**
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
    @Override
    public void onBeginningOfSpeech()
    {
    }

    @Override
    public void onBufferReceived(byte[] buffer)
    {
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
    }

    @Override
    public void onEvent(int eventType, Bundle params)
    {
    }

    public void onPartialResultsUnsupported(Bundle partialResults)
    {
        Log.d(TAG, "partial results");
        if (partialResults
                .containsKey(SpeechRecognitionUtil.UNSUPPORTED_GOOGLE_RESULTS))
        {
            String[] heard =
                    partialResults
                            .getStringArray(SpeechRecognitionUtil.UNSUPPORTED_GOOGLE_RESULTS);
            float[] scores =
                    partialResults
                            .getFloatArray(SpeechRecognitionUtil.UNSUPPORTED_GOOGLE_RESULTS_CONFIDENCE);
            receiveWhatWasHeard(Arrays.asList(heard), scores);
        }
        else
        {
            receiveResults(partialResults);
        }
    }
}