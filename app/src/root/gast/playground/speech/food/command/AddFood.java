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

import java.util.Arrays;

import root.gast.playground.R;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.SoundsLikeThresholdWordMatcher;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.content.Context;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class AddFood implements VoiceActionCommand
{
    private static final String TAG = "AddFood";

    private WordMatcher match;
    private VoiceActionExecutor executor;

    private FtsIndexedFoodDatabase foodFts;
    private Context context;

    public AddFood(Context context, VoiceActionExecutor executor,
            FtsIndexedFoodDatabase foodFts, boolean relaxed)
    {
        String[] commandWords =
                context.getResources().getStringArray(R.array.food_add_command);
        Log.d(TAG, "add with words: " + Arrays.toString(commandWords));

        if (relaxed)
        {
            // match "add" if 3 of the 4 soundex characters match
            // allows it to match add (code: A3OO) with bad (code: B300)
            match = new SoundsLikeThresholdWordMatcher(3, commandWords);
        }
        else
        {
            // match only if the use says "add" exactly
            match = new WordMatcher(commandWords);
        }
        this.context = context;
        this.executor = executor;
        this.foodFts = foodFts;
    }

    @Override
    public boolean interpret(WordList heard, float[] confidenceScores)
    {
        boolean understood = false;

        //match first part: "add"
        int matchIndex = match.isInAt(heard.getWords());
        if (matchIndex >= 0)
        {
            //match second part: the food name
            String freeText = heard.getStringAfter(matchIndex);
            if (freeText.length() > 0)
            {
                String foodToAdd = freeText;

                // first command
                VoiceActionCommand askForCalories =
                        new AskForCalories(context, executor, foodFts,
                                foodToAdd);
                String calPromptFormat =
                        context.getString(R.string.food_add_calories_prompt);
                String calPrompt = String.format(calPromptFormat, foodToAdd);

                // second command
                CancelCommand cancel = new CancelCommand(context, executor);

                // match either command, cancel first
                MultiCommandVoiceAction responseAction =
                        new MultiCommandVoiceAction(Arrays.asList(cancel,
                                askForCalories));

                // speak and display the same prompt when executing
                responseAction.setPrompt(calPrompt);
                responseAction.setSpokenPrompt(calPrompt);

                // retry if did not understood
                responseAction.setNotUnderstood(new WhyNotUnderstoodListener(
                        context, executor, true));

                understood = true;
                executor.execute(responseAction);
            }
        }

        return understood;
    }
}
