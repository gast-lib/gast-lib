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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import root.gast.playground.R;
import root.gast.playground.pref.PreferenceHelper;
import root.gast.playground.pref.SummarizingEditPreferences;
import root.gast.playground.speech.food.db.Food;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.playground.speech.food.db.MatchedFood;
import root.gast.playground.speech.food.lucene.FoodIndexBuilder;
import root.gast.playground.speech.food.lucene.FoodSearcher;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstander;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstanderNoOrder;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstanderOrdered;
import root.gast.speech.RecognizerIntentFactory;
import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import root.gast.speech.text.match.WordMatcher;
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
 * For testing out matchers
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class FoodDialogPlay extends SpeechRecognizingAndSpeakingActivity
{
    private static final String TAG = "FoodDialogPlay";

    private FtsIndexedFoodDatabase foodDb;

    private FoodSearcher luceneSearcher;
    
    private FoodSpeechResultUnderstander understander;
    
    private TextView log;
    
    private PreferenceHelper preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddialog);

        preferences = new PreferenceHelper(getResources().getString(R.string.food_preferences_key), 
                this.getApplicationContext());

        initDbs();
        hookButtons();
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
    
    private void hookButtons()
    {
        log = (TextView)findViewById(R.id.tv_resultlog);
        
        Button edit = (Button)findViewById(R.id.btn_food_edit);
        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                understander = new FoodSpeechResultUnderstander()
                {
                    @Override
                    public void interpret(List<String> heard, float[] confidenceScores)
                    {
                        doFoodEditAndCompare(heard, confidenceScores);
                    }
                };

                Intent recognizerIntent = 
                    RecognizerIntentFactory.getSimpleRecognizerIntent(
                            getString(R.string.food_edit_compare_prompt));
                recognize(recognizerIntent);
            }
        });

        Button food = (Button)findViewById(R.id.btn_food_lookup);
        food.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                understander = new FoodSpeechResultUnderstander()
                {
                    @Override
                    public void interpret(List<String> heard, float[] confidenceScores)
                    {
                        doFoodLookup(heard, confidenceScores);
                    }
                };

                Intent recognizerIntent = RecognizerIntentFactory.getSimpleRecognizerIntent(
                        getString(R.string.food_lookup_prompt));
                recognize(recognizerIntent);
            }
        });
    }
    
    protected void receiveWhatWasHeard(List<String> heard,
            float[] confidenceScores)
    {
        Log.d(TAG, "received " + heard.size());
        understander.interpret(heard, confidenceScores);
    }
    

    /**
     * implements food edit and compare, does ordered and 
     * not ordered matching, uses a series of if checks
     * to prioritize between the three possible commands
     */
    private void doFoodEditAndCompare(List<String> heard,
            float[] confidenceScores)
    {
        //make sure the DB is initialized
        initDbs();
        
        MultiPartUnderstander noOrder = new MultiPartUnderstanderNoOrder();
        MultiPartUnderstander order = new MultiPartUnderstanderOrdered();

        boolean ordered = 
            preferences.getBoolean(this, R.string.pref_food_ordered, R.string.pref_food_default);
        MultiPartUnderstander matcher;
        
        if (ordered)
        {
            matcher = order;
        }
        else
        {
            matcher = noOrder;
        }
        
        //add (free text)
        //remove (lookup text)
        //compare

        String toSpeak = null;
        for (String said : heard)
        {
            Log.d(TAG, "*understanding: " + said);
            Food add = matcher.addFreeText(said);
            if (add == null)
            {
                Log.d(TAG, "not add");
                Food removed = matcher.removeExistingFood(said);
                if (removed == null)
                {
                    Log.d(TAG, "not remove");
                    String comparison = matcher.compareCalories(said);
                    if (comparison == null)
                    {
                        Log.d(TAG, "not comparison");
                    }
                    else 
                    {
                        toSpeak = comparison;
                        Log.d(TAG, "MATCHED compare: " + toSpeak);
                        break;
                    }
                }
                else
                {
                    Log.d(TAG, "MATCHED remove");
                    toSpeak = "removed " + removed.getName();
                    FtsIndexedFoodDatabase.getInstance(this).removeFood(removed.getName());
                    break;
                }
            }
            else
            {
                Log.d(TAG, "MATCHED add");
                toSpeak = "added " + add.getName();
                FtsIndexedFoodDatabase.getInstance(this).insertFood(add.getName(), add.getCalories());
                break;
            }
        }

        if (toSpeak == null)
        {
            toSpeak = getResources().getString(R.string.speechRecognitionFailed);
        }
        
        getTts().speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        Log.d(TAG, "speaking.. " + toSpeak);
        appendToLog(toSpeak);
    }

    private void doFoodLookup(List<String> heard,
            float[] confidenceScores)
    {
        Log.d(TAG, "do food lookup");
        //if lucene is true the do lucene
        boolean lucene = 
            preferences.getBoolean(this, R.string.pref_food_lucene, R.string.pref_food_default);

        boolean or = 
            preferences.getBoolean(this, R.string.pref_food_or, R.string.pref_food_or_default);
        boolean prefix = 
            preferences.getBoolean(this, R.string.pref_food_prefix, R.string.pref_food_default);
        boolean phrase = 
            preferences.getBoolean(this, R.string.pref_food_phrase, R.string.pref_food_default);

        //setup lucene or fts
        FtsIndexedFoodDatabase foodFts = null;
        if (lucene)
        {
            Log.d(TAG, "do lucene");
            //This creates the searcher so this method can use it later 
            receiveWhatWasHeardLuceneFood(heard, confidenceScores);
        }
        else
        {
            Log.d(TAG, "do fts");
            foodFts = FtsIndexedFoodDatabase.getInstance(this);
        }
        
        for (String said : heard)
        {
            List<MatchedFood> foods;
            //search lucene or fts
            if (lucene)
            {
                List<Food> foodMatches = luceneSearcher.findMatching(said);
                foods = new ArrayList<MatchedFood>();
                for (Food food : foodMatches)
                {
                    foods.add(new MatchedFood(0, 0, food));
                }
            } else
            {
                foods = foodFts.retrieveBestMatch(said, prefix, or, phrase);
            }
            
            //result
            if (foods.size() > 0)
            {
                Food heardFood = foods.get(0).getFood();
                Log.d(TAG, "heard a food " + heardFood);
                String toSpeak = heardFood.getName() + " has " + heardFood.getFormattedCalories() + " calories.";
                getTts().speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                appendToLog(toSpeak);
                break;
            }
        }
    }
    

    protected void receiveWhatWasHeardAdd(List<String> heard,
            float[] confidenceScores)
    {
        WordMatcher command = new WordMatcher("add");
        for (String said : heard)
        {
            if (command.isIn(said.split("\\s")))
            {
                Log.d(TAG, "heard add");
                break;
            }
        }
    }

    protected void receiveWhatWasHeardFts(List<String> heard,
            float[] confidenceScores)
    {
        FtsIndexedFoodDatabase food = FtsIndexedFoodDatabase.getInstance(this);

        for (String said : heard)
        {
            if (food.retrieveBestMatch(said).size() > 0)
            {
                Log.d(TAG, "heard a food");
                break;
            }
        }
    }

    /**
     * handle receive using Lucene
     */
    protected void receiveWhatWasHeardLuceneFood(List<String> heard,
            float[] confidenceScores)
    {
        // create the food index only once
        if (luceneSearcher == null)
        {
            // don't overwrite, but do stemming
            FoodIndexBuilder builder =
                    new FoodIndexBuilder(getExternalFilesDir("foodindex")
                            .getAbsolutePath(), false, false, true);
            try
            {
                // read the foods file and add foods to the builder
                loadLuceneIndex(builder);
            } catch (IOException e)
            {
                Log.e(TAG, "unable to load index", e);
            }

            try
            {
                luceneSearcher = builder.get();
            } catch (IOException e)
            {
                Log.e(TAG, "error", e);
            }
        }

        for (String said : heard)
        {
            if (luceneSearcher.findMatching(said).size() > 0)
            {
                Log.d(TAG, "heard a food");
                break;
            }
        }
    }
    
    private void loadLuceneIndex(FoodIndexBuilder builder) throws IOException
    {
        Log.d(TAG, "loading foods");
        InputStream stream = getResources().openRawResource(R.raw.foods);

        BufferedReader is =
                new BufferedReader(new InputStreamReader(stream, "UTF8"));
        String line;

        line = is.readLine();
        while (line != null)
        {
            String[] parts = line.split(",");
            String food = parts[0];
            float cals = Float.valueOf(parts[1]);
            builder.addFood(food, cals);
            Log.d(TAG, "loaded: " + food);
            line = is.readLine();
        }
    }

    private void appendToLog(String appendThis)
    {
        String currentLog = log.getText().toString();
        currentLog = appendThis + "\n" + currentLog;
        log.setText(currentLog);
    }
    
    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_dialog_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.m_match_param)
        {
            //launch preferences activity
            Intent i = new Intent(this, SummarizingEditPreferences.class);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_INTENT, R.xml.food_preferences);
            String preferenceName = getResources().getString(R.string.food_preferences_key);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_NAME_INTENT, preferenceName);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.m_resetdb_param)
        {
            FtsIndexedFoodDatabase.getInstance(this).clean(this);
            //now load
            initDbs();
            Toast.makeText(this, getString(R.string.menu_resetdb_param), Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.m_showdb_param)
        {
            Intent browseIntent = new Intent(this, FoodBrowser.class);
            startActivity(browseIntent);
        }
        return true;
    }
}
