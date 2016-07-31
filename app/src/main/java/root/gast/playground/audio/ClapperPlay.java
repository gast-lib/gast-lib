/*
 * Copyright 2012 Greg Milette and Adam Stroud
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
package root.gast.playground.audio;

import java.util.ArrayList;
import java.util.List;

import root.gast.audio.interp.ConsistentFrequencyDetector;
import root.gast.audio.interp.LoudNoiseDetector;
import root.gast.audio.interp.SingleClapDetector;
import root.gast.audio.record.AmplitudeClipListener;
import root.gast.audio.record.AudioClipListener;
import root.gast.audio.record.OneDetectorManyAmplitudeObservers;
import root.gast.audio.record.OneDetectorManyObservers;
import root.gast.playground.R;
import root.gast.playground.pref.SummarizingEditPreferences;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Experiment with three different clappers and a logging one
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class ClapperPlay extends Activity
{
    private static final String TAG = "ClapperPlay";
    
    private TextView log;
    private TextView status;
    
    private RecordAudioTask recordAudioTask; 
    private RecordAmplitudeTask recordAmplitudeTask;
    
    private SoundPool testSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clapperplay);
        
        hookButtons();
    }
    
    private void hookButtons()
    {
        log = (TextView)findViewById(R.id.tv_resultlog);
        
        status = (TextView)findViewById(R.id.tv_recording_status);
        
        Button edit = (Button)findViewById(R.id.btn_audio_clapper_startstop);
        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startAmplitudeTask();
            }
        });
        
        Button pitch = (Button)findViewById(R.id.btn_audio_pitch_startstop);
        pitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startTask(createAudioLogger(), "Audio Logger");
            }
        });

        findViewById(R.id.btn_audio_clapper_audio_startstop).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startTask(new LoudNoiseDetector(), "Audio Clapper");
            }
        });

        findViewById(R.id.btn_audio_singing_startstop).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final int HISTORY_SIZE = 3;
                final int RANGE_THRESHOLD = 100;
                startTask(new ConsistentFrequencyDetector(
                        HISTORY_SIZE, RANGE_THRESHOLD, 
                        ConsistentFrequencyDetector.DEFAULT_SILENCE_THRESHOLD), "Singing Clapper");
            }
        });
        
        findViewById(R.id.btn_audio_stopall).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stopAll();
            }
        });

        //set up a sound pool with all the sounds so they playback faster
        testSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        hookSoundButton(R.id.btn_audio_low, R.raw.lowhz);
        hookSoundButton(R.id.btn_audio_mid, R.raw.midhz);
        hookSoundButton(R.id.btn_audio_high, R.raw.highhz);
        hookSoundButton(R.id.btn_audio_all, R.raw.lowhigh);
    }

    private void hookSoundButton(int button, final int audioResource)
    {
        final int soundId = testSounds.load(this, audioResource, 1);
        Button soundButton = (Button)findViewById(button);
        
        soundButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                testSounds.play(soundId, 1f, 1f, 1, 0, 1.0f);
                testSounds.play(soundId, 1f, 1f, 1, 0, 1.0f);
            }
        });
    }
    
    private void stopAll()
    {
        Log.d(TAG, "stop record amplitude");
        shutDownTaskIfNecessary(recordAmplitudeTask);
        Log.d(TAG, "stop record audio");
        shutDownTaskIfNecessary(recordAudioTask);
    }
    
    private void shutDownTaskIfNecessary(final AsyncTask task)
    {
        if ( (task != null) && (!task.isCancelled()))
        {
            if ((task.getStatus().equals(AsyncTask.Status.RUNNING))
                    || (task.getStatus()
                            .equals(AsyncTask.Status.PENDING)))
            {
                Log.d(TAG, "CANCEL " + task.getClass().getSimpleName());
                task.cancel(true);
            }
            else
            {
                Log.d(TAG, "task not running");
            }
        }
    }

    private void startTask(AudioClipListener detector, String name)
    {
        stopAll();
        
        recordAudioTask = new RecordAudioTask(ClapperPlay.this, status, log, name);
        //wrap the detector to show some output
        List<AudioClipListener> observers = new ArrayList<AudioClipListener>();
        observers.add(new AudioClipLogWrapper(log, this));
        OneDetectorManyObservers wrapped = 
            new OneDetectorManyObservers(detector, observers);
        recordAudioTask.execute(wrapped);
    }

    private void startAmplitudeTask()
    {
        stopAll();
        recordAmplitudeTask = new RecordAmplitudeTask(ClapperPlay.this, status, log, "Clapper");
        SingleClapDetector detector = new SingleClapDetector();
        //wrap the detector to show some output
        List<AmplitudeClipListener> observers = new ArrayList<AmplitudeClipListener>();
        observers.add(new AmplitudeLogWrapper(log, this));
        OneDetectorManyAmplitudeObservers wrapped = 
            new OneDetectorManyAmplitudeObservers(detector, observers);
        recordAmplitudeTask.execute(wrapped);
    }

    private AudioClipListener createAudioLogger()
    {
        AudioClipListener audioLogger = new AudioClipListener()
        {
            @Override
            public boolean heard(short[] audioData, int sampleRate)
            {
                if (audioData == null || audioData.length == 0)
                {
                    return true;
                }
                
                // returning false means the recording won't be stopped
                // users have to manually stop it via the stop button
                return false;
            }
        };
        
        return audioLogger;
    }

    @Override
    protected void onPause()
    {
        stopAll();
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clapper_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.m_match_param)
        {
            //launch preferences activity
            Intent i = new Intent(this, SummarizingEditPreferences.class);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_INTENT, R.xml.audio_clapper_preferences);
            String preferenceName = getResources().getString(R.string.audio_clapper_preferences_key);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_NAME_INTENT, preferenceName);
            startActivity(i);
        }
        return true;
    }
}
