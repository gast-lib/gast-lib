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
package root.gast.playground.speech.food;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import root.gast.playground.R;
import root.gast.playground.speech.food.command.AddFood;
import root.gast.playground.speech.food.command.CancelCommand;
import root.gast.playground.speech.food.command.FoodLookup;
import root.gast.playground.speech.food.command.RemoveFood;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Uses {@link VoiceAction}s and {@link FtsIndexedFoodDatabase} to implement the food dialogue
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class MultiTurnFoodDialogActivity extends SpeechRecognizingAndSpeakingActivity
{
    private static final String TAG = "MultiTurnFoodDialogActivity";
    
    private FtsIndexedFoodDatabase foodDb;
    
    private TextView log;
    
    private VoiceActionExecutor executor;

    private VoiceAction lookupVoiceAction;
    private VoiceAction editVoiceAction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddialogmulti);

        hookButtons();
        initDbs();
        initDialog();
    }
    
    private void hookButtons()
    {
        log = (TextView)findViewById(R.id.tv_resultlog);
        
        Button edit = (Button)findViewById(R.id.btn_food_edit);
        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                executor.execute(editVoiceAction);
            }
        });

        Button food = (Button)findViewById(R.id.btn_food_lookup);
        food.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                executor.execute(lookupVoiceAction);
            }
        });
    }

    private void initDialog()
    {
        if (executor == null)
        {
            executor = new VoiceActionExecutor(this);
        }
        editVoiceAction = makeFoodEdit();
        lookupVoiceAction = makeFoodLookup();
    }
    

    /**
     * make sure the databases have data
     */
    private void initDbs()
    {
        foodDb = FtsIndexedFoodDatabase.getInstance(this);
        
        if (foodDb.isEmpty())
        {
            Log.d(TAG, "loading foods");
            InputStream stream = getResources().openRawResource(R.raw.foods);
            try
            {
                foodDb.loadFrom(stream);
            }
            catch (IOException io)
            {
                Log.d(TAG, "failed to load db");
            }
        }
    }
    
    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        super.onSuccessfulInit(tts);
        Log.d(TAG, "activate ui, set tts");
        executor.setTts(getTts());
    }
    
    protected void receiveWhatWasHeard(List<String> heard,
            float[] confidenceScores)
    {
        Log.d(TAG, "received " + heard.size());
        clearLog();
        for (int i = 0; i < heard.size(); i++)
        {
            appendToLog(heard.get(i) + " " + confidenceScores[i]);
        }
        executor.handleReceiveWhatWasHeard(heard, confidenceScores);
    }
    
    protected void recognitionFailure()
    {
        //don't do anything here
        Log.d(TAG, "cancelled with button");
    }

    private VoiceAction makeFoodEdit()
    {
        FtsIndexedFoodDatabase foodDb =
                FtsIndexedFoodDatabase
                        .getInstance(MultiTurnFoodDialogActivity.this);

        // match it with two levels of strictness
        boolean relaxed = false;

        VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
        VoiceActionCommand removeCommand =
                new RemoveFood(this, executor, foodDb, relaxed);
        VoiceActionCommand addCommand =
                new AddFood(this, executor, foodDb, relaxed);

        relaxed = true;
        VoiceActionCommand removeCommandRelaxed =
                new RemoveFood(this, executor, foodDb, relaxed);
        VoiceActionCommand addCommandRelaxed =
                new AddFood(this, executor, foodDb, relaxed);

        VoiceAction voiceAction =
                new MultiCommandVoiceAction(Arrays.asList(cancelCommand,
                        addCommand, removeCommand, addCommandRelaxed,
                        removeCommandRelaxed));
        // don't retry
        voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this,
                executor, false));
        final String EDIT_PROMPT =
                getResources().getString(R.string.food_edit_prompt);
        // no spoken prompt
        voiceAction.setPrompt(EDIT_PROMPT);

        return voiceAction;
    }

    private VoiceAction makeFoodLookup()
    {
        final String LOOKUP_PROMPT = getResources().getString(R.string.food_lookup_prompt);
        FtsIndexedFoodDatabase foodDb = 
            FtsIndexedFoodDatabase.getInstance(
                    MultiTurnFoodDialogActivity.this);
        VoiceActionCommand lookup = new FoodLookup(this, executor, foodDb);
        VoiceActionCommand cancel = new CancelCommand(this, executor);
        VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(cancel, lookup));
        voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this, executor, false));
        voiceAction.setPrompt(LOOKUP_PROMPT);
        
        return voiceAction;
    }
    
    private void clearLog()
    {
        log.setText("");
    }
    
    private void appendToLog(String appendThis)
    {
        String currentLog = log.getText().toString();
        currentLog = currentLog  + "\n" + appendThis;
        log.setText(currentLog);
    }
    
    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_multi_dialog_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.m_resetdb_param)
        {
            final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Updating Food Database");
            dialog.setIndeterminate(true);
            Runnable updateDbRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    FtsIndexedFoodDatabase.getInstance(MultiTurnFoodDialogActivity.this).clean(MultiTurnFoodDialogActivity.this);
                    //now load
                    initDbs();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(MultiTurnFoodDialogActivity.this, "Reset database", Toast.LENGTH_SHORT).show();
                            dialog.hide();
                        }
                    });
                }
            };
            Thread updateDbThread = new Thread(updateDbRunnable);
            updateDbThread.start();
        }
        else if (item.getItemId() == R.id.m_showdb_param)
        {
            Intent browseIntent = new Intent(this, FoodBrowser.class);
            startActivity(browseIntent);
        }
        return true;
    }
}
