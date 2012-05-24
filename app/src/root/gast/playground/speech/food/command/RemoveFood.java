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
import root.gast.speech.text.match.SoundsLikeThresholdWordMatcher;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.OnNotUnderstoodListener;
import root.gast.speech.voiceaction.OnUnderstoodListener;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.VoiceAlertDialog;
import android.content.Context;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class RemoveFood implements VoiceActionCommand
{
    private static final String TAG = "RemoveFood";

    private WordMatcher match;

    private Context context;
    private VoiceActionExecutor executor;
    private FtsIndexedFoodDatabase foodFts;
    private boolean relaxed;

    public RemoveFood(Context context, VoiceActionExecutor executor,
            FtsIndexedFoodDatabase foodFts, boolean relaxed)
    {
        String[] commandWords =
                context.getResources().getStringArray(
                        R.array.food_remove_command);
        if (relaxed)
        {
            // match "remove" if 3 of the 4 soundex characters match
            match = new SoundsLikeThresholdWordMatcher(3, commandWords);
        }
        else
        {
            //exact match
            match = new WordMatcher(commandWords);
        }
        this.context = context;
        this.executor = executor;
        this.foodFts = foodFts;
        this.relaxed = relaxed;
    }

    public boolean interpret(WordList heard, float[] confidence)
    {
        Food toRemove = null;

        //match "remove"
        int matchIndex = match.isInAt(heard.getWords());
        
        //match the food to remove
        if (matchIndex >= 0)
        {
            String freeText = heard.getStringAfter(matchIndex);
            List<MatchedFood> match;
            if (relaxed)
            {
                // for relaxed add prefix matching
               match = foodFts.retrieveBestMatch(freeText, true, true, false);
            }
            else
            {
               match = foodFts.retrieveBestMatch(freeText, false, true, false);
            }
            if (match.size() > 0)
            {
               toRemove = match.get(0).getFood();
            }
        }

        //start another VoiceAction
        //to confirm before removing
        if (toRemove != null)
        {
            final Food foodToRemove = toRemove;
            final VoiceAlertDialog confirmDialog = new VoiceAlertDialog();
            // add listener for positive response
            // use relaxed matching to increase chance of understanding user
            confirmDialog.addRelaxedPositive(new OnUnderstoodListener()
            {
                @Override
                public void understood()
                {
                    Log.d(TAG, "REMOVE!: " + foodToRemove);
                    FtsIndexedFoodDatabase.getInstance(context).removeFood(
                            foodToRemove.getName());
                    String toSayRemoved =
                            String.format(
                                    context.getResources().getString(
                                            R.string.food_remove_complete),
                                    foodToRemove.getName());
                    executor.speak(toSayRemoved);
                }
            });

            //prompt for the confirm VoiceAction
            String toSay =
                    String.format(
                            context.getResources().getString(
                                    R.string.food_remove_confirm_prompt),
                            foodToRemove.getName());
            confirmDialog.setPrompt(toSay);
            confirmDialog.setSpokenPrompt(toSay);

            // if the user says anything else besides the yes words cancel
            confirmDialog.setNotUnderstood(new OnNotUnderstoodListener()
            {
                @Override
                public void notUnderstood(List<String> heard, int reason)
                {
                    String toSayCancelled = context.getResources().getString(
                            R.string.voiceaction_cancelled_response);
                    executor.speak(toSayCancelled);
                }
            });
            
            executor.execute(confirmDialog);
        }
        return (toRemove != null);
    }
}
