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
package root.gast.playground.speech.food.command;

import java.util.List;

import root.gast.playground.R;
import root.gast.playground.speech.food.db.Food;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.playground.speech.food.db.MatchedFood;
import root.gast.speech.text.WordList;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.content.Context;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class FoodLookup implements VoiceActionCommand
{
    private static final String TAG = "FoodLookup";

    private VoiceActionExecutor executor;
    private FtsIndexedFoodDatabase foodFts;
    private Context context;

    public FoodLookup(Context context, VoiceActionExecutor executor,
            FtsIndexedFoodDatabase foodFts)
    {
        this.context = context;
        this.executor = executor;
        this.foodFts = foodFts;
    }

    public boolean interpret(WordList heard, float[] confidence)
    {
        boolean success = false;

        boolean or = false;
        boolean prefix = false;
        boolean phrase = true;

        String said = heard.getSource();
        List<MatchedFood> foods =
                foodFts.retrieveBestMatch(said, prefix, or, phrase);

        // phrase query
        if (foods.size() == 0)
        {
            or = false;
            prefix = false;
            phrase = true;
            foods = foodFts.retrieveBestMatch(said, prefix, or, phrase);
        }

        // word query
        if (foods.size() == 0)
        {
            or = false;
            prefix = false;
            phrase = false;
            foods = foodFts.retrieveBestMatch(said, prefix, or, phrase);
        }

        // word or query
        if (foods.size() == 0)
        {
            or = true;
            prefix = false;
            phrase = false;
            foods = foodFts.retrieveBestMatch(said, prefix, or, phrase);
        }

        // word, prefix, or query
        if (foods.size() == 0)
        {
            or = true;
            prefix = true;
            phrase = false;
            foods = foodFts.retrieveBestMatch(said, prefix, or, phrase);
        }

        if (foods.size() > 0)
        {
            Food heardFood = foods.get(0).getFood();
            String resultFormat =
                    context.getResources().getString(
                            R.string.food_lookup_result);
            String toSay =
                    String.format(resultFormat, heardFood.getName(),
                            heardFood.getFormattedCalories());
            Log.d(TAG, "heard a food " + heardFood);
            success = true;
            executor.speak(toSay);
        }
        return success;
    }
}
