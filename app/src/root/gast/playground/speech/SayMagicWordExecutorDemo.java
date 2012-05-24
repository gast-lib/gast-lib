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
package root.gast.playground.speech;

import java.util.List;

import root.gast.R;
import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import root.gast.speech.tts.TextToSpeechStartupListener;
import root.gast.speech.voiceaction.AbstractVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * A simple app to demonstrate a barebones usecase for playing a simple text to
 * speech sound using {@link VoiceActionExecutor}
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SayMagicWordExecutorDemo extends SpeechRecognizingAndSpeakingActivity implements
        TextToSpeechStartupListener
{
    private static final String TAG = "SayMagicWordExecutorDemo";
    private Button speak;

    private VoiceActionExecutor executor;
    private VoiceAction magicWordAction;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magicworddemo);
        hookButtons();
    }
    
    private void hookButtons()
    {
        speak = (Button) findViewById(R.id.btn_speak);
        speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                executor.execute(magicWordAction);
            }
        });
    }
    
    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        Log.d(TAG, "successful init");
        super.onSuccessfulInit(tts);
        executor = new VoiceActionExecutor(this);
        magicWordAction = new MagicWordCommand();
        executor.setTts(getTts());
    }

    /**
     * determine if the user said the magic word and speak the result
     */
    protected void receiveWhatWasHeard(List<String> heard,
            float[] confidenceScores)
    {
        executor.handleReceiveWhatWasHeard(heard, confidenceScores);

    }

    /**
     * {@link VoiceAction} that handles the magic word command
     * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
     *
     */
    class MagicWordCommand extends AbstractVoiceAction
    {
        public MagicWordCommand()
        {
            setPrompt("What is the magic word?");
            setSpokenPrompt("What is the magic word?");
        }
        
        @Override
        public boolean interpret(List<String> heard, float[] confidenceScores)
        {
            String magicWord = "tree";
            String mostLikelyThingHeard = heard.get(0);
            String message = "";
            if (mostLikelyThingHeard.equals(magicWord))
            {
                message =
                       "Correct! You said the magic word: " + mostLikelyThingHeard;
            }
            else
            {
                message = "Wrong! The magic word is not: " + mostLikelyThingHeard;
            }

            AlertDialog a =
                    new AlertDialog.Builder(SayMagicWordExecutorDemo.this).setTitle("Result")
                            .setMessage(message).setPositiveButton("Ok", null)
                            .create();
            a.show();
            deactivateUi();
            executor.speak(message);
            return false;
        }
    }
}