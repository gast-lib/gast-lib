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
package root.gast.playground.speech.tts;

import java.util.List;
import java.util.Locale;

import root.gast.R;
import root.gast.playground.util.DialogGenerator;
import root.gast.speech.tts.CommonTtsMethods;
import root.gast.speech.tts.TextToSpeechInitializer;
import root.gast.speech.tts.TextToSpeechStartupListener;
import root.gast.speech.tts.TextToSpeechUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class TextToSpeechInfoActivity extends Activity implements TextToSpeechStartupListener
{
    private static final String TAG = "TextToSpeechInfoActivity";

    private TextToSpeech tts;
    
    /**
     * Performs the one time initialization for the activity.
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ttsinfo);
        init();
    }

    private void init()
    {
        //start the init procedure
        new TextToSpeechInitializer(this, Locale.getDefault(), this);
    }

    @Override
    public void onFailedToInit()
    {
    }
    @Override
    public void onRequireLanguageData()
    {
    }
    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        this.tts = tts;
    }
    @Override
    public void onWaitingForLanguageData()
    {
    }

    /**
     * show which locales are supported and not supported on this device
     */
    public void onSeeLocales(View view) 
    {
        String whatKindOfLocales;
        List<Locale> locales = TextToSpeechUtils.getLocalesSupported(this, tts);
        StringBuilder sb = new StringBuilder();
        for (Locale locale : locales)
        {
            sb.append(locale.toString());
            sb.append("\n");
        }
        DialogGenerator.createInfoDialog(this, "Supported Locales", sb.toString()).show();
    }

    public void onDataCheck(View view) 
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check,
                CommonTtsMethods.SPEECH_DATA_CHECK_CODE);
    }
    
    
    public void onSeeNoDataLocales(View view)
    {
        DialogGenerator.createInfoDialog(this, "Locales Status", 
                CommonTtsMethods.getLanguageAvailableDescription(tts)).show();
    }
    
    public void launchInstallTtsData()
    {
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(installIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String info = TextToSpeechUtils.logOnActivityResultDataCheck(requestCode, resultCode, data);
        DialogGenerator.createInfoDialog(this, "", info).show();
    }
}
