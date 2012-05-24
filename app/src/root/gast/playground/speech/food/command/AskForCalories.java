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

import root.gast.playground.R;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.speech.text.WordList;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.content.Context;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class AskForCalories implements VoiceActionCommand
{
    private String foodToAdd;
    private FtsIndexedFoodDatabase foodFts;
    private VoiceActionExecutor executor;
    private Context context;

    public AskForCalories(Context context, VoiceActionExecutor executor,
            FtsIndexedFoodDatabase foodFts, String foodToAdd)
    {
        this.context = context;
        this.executor = executor;
        this.foodFts = foodFts;
        this.foodToAdd = foodToAdd;
    }

    @Override
    public boolean interpret(WordList heard, float[] confidenceScores)
    {
        boolean understood = false;
        // look for a number within "heard"
        for (String word : heard.getWords())
        {
            if (isNumber(word))
            {
                String responseFormat =
                        context.getResources().getString(
                                R.string.food_add_result);
                String response =
                        String.format(responseFormat, foodToAdd, word);
                // insert food
                foodFts.insertFood(foodToAdd, Float.parseFloat(word));
                executor.speak(response);
                understood = true;
            }
        }
        return understood;
    }

    private boolean isNumber(String word)
    {
        boolean isNumber = false;
        try
        {
            Integer.parseInt(word);
            isNumber = true;
        } catch (NumberFormatException e)
        {
            isNumber = false;
        }
        return isNumber;
    }
}
