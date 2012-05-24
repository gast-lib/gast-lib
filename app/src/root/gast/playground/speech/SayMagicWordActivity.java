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
package root.gast.playground.speech;

import java.util.List;
import java.util.Locale;

import root.gast.playground.R;
import root.gast.playground.util.DialogGenerator;
import root.gast.speech.RecognizerIntentFactory;
import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.SpeechRecognizingActivity;
import root.gast.speech.tts.CommonTtsMethods;
import root.gast.speech.tts.TextToSpeechInitializerByAction;
import root.gast.speech.tts.TextToSpeechStartupListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Implements a button trigger, with a text-only input prompt. The
 * user must say a specific word. After each try, this displays
 * the resulting interpretation and also speaks it.
 * 
 * <br>This class produces a nicer looking activity 
 * than {@link SayMagicWordDemo} but has the same features
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class SayMagicWordActivity extends SpeechRecognizingActivity 
    implements TextToSpeechStartupListener
{
    private static final String TAG = "SayMagicWordActivity";
    private TextToSpeech speaker;
    
    private TextView result;
    private TextToSpeechInitializerByAction ttsInit;

    private Button speak;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magicword);
        
        ttsInit =
            new TextToSpeechInitializerByAction(this, this, Locale.getDefault());

        speak = (Button)findViewById(R.id.bt_speak);
        speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "speaking");
               recognize(
                       RecognizerIntentFactory.getSimpleRecognizerIntent(
                               getResources().getString(R.string.speech_magic_word_prompt)));
            }
        });
        speak.setEnabled(false);
        
        result = (TextView)findViewById(R.id.tv_result);
    }
    
    @Override
    protected void
            onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        ttsInit.handleOnActivityResult(this, requestCode, resultCode, data);
    }
    
    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        speak.setEnabled(true);
        speaker = tts;
    }
    
    @Override
    public void onFailedToInit()
    {
        DialogGenerator.createInfoDialog(this, getResources().getString(R.string.d_error), getResources().getString(R.string.tts_init_failed), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        }).show();
    }

    @Override
    public void onWaitingForLanguageData()
    {
    }

    @Override
    public void onRequireLanguageData()
    {
        DialogGenerator.createInfoDialog(this, getResources().getString(R.string.d_error), getResources().getString(R.string.tts_install_tts_data), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //after this, the code will restart this activity and this process will repeat
                //maybe showing this multiple times until the data is downloaded and 
                //installed
                CommonTtsMethods.installLanguageData(SayMagicWordActivity.this);
            }
        }).show();
    }


    /**
     * check if the user said the magic word, if so display 
     * it with nicely highlighted text and say it
     */
    public void receiveWhatWasHeard(List<String> lastThingsHeard, float [] confidenceScores)
    {
        String reply = "";
        if (lastThingsHeard.size() == 0)
        {
            reply = getResources().getString(R.string.speech_magic_word_heard_nothing);
            result.setText(reply);
        }
        else
        {
            String magicWord = getResources().getString(R.string.speech_magic_word_secret);
            String mostLikelyThingHeard = lastThingsHeard.get(0);
            String prompt;
            if (mostLikelyThingHeard.equals(magicWord))
            {
                prompt = getResources().getString(R.string.speech_magic_word_correct_prompt);
            }
            else
            {
                prompt = getResources().getString(R.string.speech_magic_word_fail_prompt);
            }
            reply = prompt + " " + mostLikelyThingHeard;
            //do it as a span...
            result.setText(reply, BufferType.SPANNABLE);
            Spannable wordToSpan = (Spannable) result.getText();
            int endOfPromptAndSpace = prompt.length() + 1;
            wordToSpan.setSpan(new UnderlineSpan(), endOfPromptAndSpace,
                    reply.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            result.setText(wordToSpan);
        }
        speaker.speak(reply, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void speechNotAvailable()
    {
    }
    
    @Override
    protected void directSpeechNotAvailable()
    {
    }
    
    @Override
    protected void languageCheckResult(String languageToUse)
    {
    }
    
    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.magic_word_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.m_magicword_hint:
                DialogGenerator.createInfoDialog(
                        this,
                        getResources().getString(R.string.d_info),
                        getResources().getString(
                                R.string.speech_magic_word_secret));
                break;
            default:
                throw new RuntimeException("unknown menu selection");
        }
        return true;
    }

    @Override
    protected void recognitionFailure(int errorCode)
    {
        AlertDialog a =
            new AlertDialog.Builder(this).setTitle("Error")
                    .setMessage(SpeechRecognitionUtil.diagnoseErrorCode(errorCode))
                    .setPositiveButton("Ok", null)
                    .create();
        a.show();
    }
}
