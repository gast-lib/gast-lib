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
package root.gast.speech.voiceaction;

import java.util.List;

import root.gast.speech.SpeechRecognizingActivity;
import root.gast.speech.tts.TextToSpeechUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

/**
 * Helps execute {@link VoiceAction}s and say their responses and prompts.
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
@SuppressLint("NewApi")
public class VoiceActionExecutor
{
    private static final String TAG = "VoiceActionExecutor";

    private VoiceAction active;

    private SpeechRecognizingActivity speech;

    /**
     * parameter for TTS to identify utterance
     */
    private final String EXECUTE_AFTER_SPEAK = "EXECUTE_AFTER_SPEAK";

    private TextToSpeech tts;

    public VoiceActionExecutor(SpeechRecognizingActivity speech)
    {
        this.speech = speech;
        active = null;
    }
    
    /**
     * set the tts when it is ready to complete initialization
     */
    @SuppressLint("NewApi")
    public void setTts(TextToSpeech tts)
    {
        this.tts = tts;
        if (Build.VERSION.SDK_INT >= 15)
        {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onDone(String utteranceId)
                {
                    onDoneSpeaking(utteranceId);
                }

                @Override
                public void onError(String utteranceId)
                {
                }

                @Override
                public void onStart(String utteranceId)
                {
                }
            });
        }
        else
        {
            Log.d(TAG, "set utternace completed listener");
            tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
            {
                @Override
                public void onUtteranceCompleted(String utteranceId)
                {
                    onDoneSpeaking(utteranceId);
                }
            });
        }
    }

    /**
     * external handleReceiveWhatWasHeard must call this
     */
    public void handleReceiveWhatWasHeard(List<String> heard,
            float[] confidenceScores)
    {
        active.interpret(heard, confidenceScores);
    }

    private void onDoneSpeaking(String utteranceId)
    {
        if (utteranceId.equals(EXECUTE_AFTER_SPEAK))
        {
            doRecognitionOnActive();
        }
    }

    /**
     * convenient way to just reply with something spoken
     */
    public void speak(String toSay)
    {
        tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * add speech, don't flush the speaking queue
     */
    public void addSpeech(String toSay)
    {
        tts.speak(toSay, TextToSpeech.QUEUE_ADD, null);
    }

    /**
     * execute the current active {@link VoiceAction} again speaking
     * extraPrompt before
     */
    public void reExecute(String extraPrompt)
    {
        if ((extraPrompt != null) && (extraPrompt.length() > 0))
        {
            tts.speak(extraPrompt, TextToSpeech.QUEUE_FLUSH,
                    TextToSpeechUtils.makeParamsWith(EXECUTE_AFTER_SPEAK));
        }
        else
        {
            execute(getActive());
        }
    }

    /**
     * change the current voice action to this and then execute it, optionally
     * saying a prompt first
     */
    public void execute(VoiceAction voiceAction)
    {
        if (tts == null)
        {
            throw new RuntimeException("Text to speech not initialized");
        }

        setActive(voiceAction);

        if (voiceAction.hasSpokenPrompt())
        {
            Log.d(TAG, "speaking prompt: " + voiceAction.getSpokenPrompt());
            tts.speak(voiceAction.getSpokenPrompt(), TextToSpeech.QUEUE_FLUSH,
                    TextToSpeechUtils.makeParamsWith(EXECUTE_AFTER_SPEAK));
        }
        else
        {
            doRecognitionOnActive();
        }
    }

    private void doRecognitionOnActive()
    {
        Intent recognizerIntent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getActive()
                .getPrompt());
        speech.recognize(recognizerIntent);
    }

    private VoiceAction getActive()
    {
        return active;
    }

    private void setActive(VoiceAction active)
    {
        this.active = active;
    }
    
    public TextToSpeech getTts()
    {
        return tts;
    }
}
