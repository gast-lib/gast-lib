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
package root.gast.playground.speech.food.multimatcher;

import java.util.List;

import root.gast.playground.speech.food.db.Food;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.playground.speech.food.db.MatchedFood;
import root.gast.speech.text.match.WordMatcher;
import android.util.Log;

/**
 * Implements unordered matching of three food commands
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class MultiPartUnderstanderNoOrder extends MultiPartUnderstander
{
    private static final String TAG = "MultiPartUnderstanderNoOrder";

    public Food addFreeText(String toMatch)
    {
        Food toAdd = null;
        WordMatcher dc = new WordMatcher("add");
        String[] tokens = toMatch.split("\\s");
        if (dc.isIn(tokens) && tokens.length > 1)
        {
            // after the first space
            String freeText = toMatch.substring(toMatch.indexOf(" "));
            Log.d(TAG, "matched add " + freeText);
            toAdd = new Food(freeText);
        }
        return toAdd;
    }

    public Food removeExistingFood(String toMatch)
    {
        Food removed = null;
        WordMatcher dc = new WordMatcher("remove");
        String[] tokens = toMatch.split("\\s");
        if (dc.isIn(tokens) && tokens.length > 1)
        {
            Log.d(TAG, "remove word");
            FtsIndexedFoodDatabase food =
                    FtsIndexedFoodDatabase.getInstance(null);
            List<MatchedFood> match =
                    food.retrieveBestMatch(toMatch, false, true, false);
            if (match.size() > 0)
            {
                Food toRemove = match.get(0).getFood();
                Log.d(TAG, "matched remove " + toRemove);
                removed = toRemove;
            }
        }
        else
        {
            Log.d(TAG, "No remove word");
        }
        return removed;
    }

    public String compareCalories(String toMatch)
    {
        String comparison = null;
        FtsIndexedFoodDatabase food = FtsIndexedFoodDatabase.getInstance(null);
        // do or match
        List<MatchedFood> match =
                food.retrieveBestMatch(toMatch, false, true, false);
        if (match.size() > 1)
        {
            Food firstMatch = match.get(0).getFood();
            Food secondMatch = match.get(1).getFood();
            Log.d(TAG, "matched compare: " + firstMatch + " with "
                    + secondMatch);
            comparison = makeComparisonResultString(firstMatch, secondMatch);
        } else
        {
            Log.d(TAG, "did not match enough to compare");
        }
        return comparison;
    }
}
