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

import java.util.ArrayList;
import java.util.List;

import root.gast.playground.R;
import root.gast.playground.speech.activation.SpeechActivatorTagWriter;
import root.gast.speech.activation.SpeechActivationService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * test different ways to activate speech recognition as a service
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechActivationServicePlay extends Activity
{
    private static final String TAG = "SpeechActivationServicePlay";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speechactivationservice);
        
        hookButtons();
    }
    
    private void hookButtons()
    {
        List<Button> startButtons = new ArrayList<Button>();
        startButtons.add(makeStartButton(R.id.btn_speech_activation_movement, R.string.speech_activation_movement));
        startButtons.add(makeStartButton(R.id.btn_speech_activation_clap, R.string.speech_activation_clap));
        startButtons.add(makeStartButton(R.id.btn_speech_activation_camera, R.string.speech_activation_camera));
        startButtons.add(makeStartButton(R.id.btn_speech_activation_speak, R.string.speech_activation_speak));

        Button stop = (Button)findViewById(R.id.btn_speech_stop_service);
        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "stop service");
                Intent i = SpeechActivationService.makeServiceStopIntent(SpeechActivationServicePlay.this);
                SpeechActivationServicePlay.this.startService(i);
            }
        });
        
        Button writeNfc = (Button)findViewById(R.id.btn_speech_activation_write_nfc);
        writeNfc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "start write nfc activity");
                Intent doTag = new Intent(SpeechActivationServicePlay.this, SpeechActivatorTagWriter.class);
                startActivity(doTag);
            }
        });
    }
    
    private Button makeStartButton(int button, final int name)
    {
        Button start = (Button)findViewById(button);
        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String activationTypeName = SpeechActivationServicePlay.this.getString(name);
                Intent i = SpeechActivationService.makeStartServiceIntent(SpeechActivationServicePlay.this,
                        activationTypeName);
                SpeechActivationServicePlay.this.startService(i);
                Log.d(TAG, "started service for " + activationTypeName);
            }
        });
        
        return start;
    }
}
