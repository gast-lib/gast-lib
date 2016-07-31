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

import java.util.Date;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * just logs what it receives from the {@link SpeechRecognizer} useful for testing
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class LoggingRecognitionListener implements RecognitionListener
{
    private static final String TAG = "LoggingRecognitionListener";
    
    private SpeechRecognizer recognizer;

    public LoggingRecognitionListener(SpeechRecognizer recognizer)
    {
        this.recognizer = recognizer;
    }
    
    @Override
    public void onRmsChanged(float rmsdB)
    {
//        Log.d(TAG, "rmsdB " + rmsdB);
    }
    
    @Override
    public void onResults(Bundle results)
    {
        logResults(results);
    }
    
    @Override
    public void onReadyForSpeech(Bundle params)
    {
        Log.d(TAG, "params " + params);
    }
    
    @Override
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "partial results");
        logResults(partialResults);
    }
    
    @Override
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "eventType " + eventType);
    }
    
    @Override
    public void onError(int error)
    {
        Log.d(TAG, "error " + error);
    }
    
    @Override
    public void onEndOfSpeech()
    {
        Log.d(TAG, "onEndOfSpeech " + new Date().toLocaleString());
        //should it stop now?
        recognizer.stopListening();
        recognizer.destroy();
    }
    
    @Override
    public void onBufferReceived(byte[] buffer)
    {
//        Log.d(TAG, "buffer " + buffer.length);
    }
    
    @Override
    public void onBeginningOfSpeech()
    {
        Log.d(TAG, "onBeginningOfSpeech " + new Date().toLocaleString());
    }
    
    private void logResults(Bundle bundle)
    {
        if (bundle != null)
        {
            Log.d(TAG, "bundle: " + bundle.toString());
        }
        if ((bundle != null)
                && bundle.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))
        {
            int i = 0;
            for (String result : bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
            {
                Log.d(TAG, i + ": " + result);
                i++;
            }
        }
    }
}