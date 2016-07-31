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

import root.gast.playground.speech.food.db.Food;

/**
 * common methods for the two multi-part classes
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public abstract class MultiPartUnderstander
{
    public abstract Food addFreeText(String toMatch);
    public abstract Food removeExistingFood(String toMatch);
    public abstract String compareCalories(String toMatch);
    
    protected String makeComparisonResultString(Food firstMatch, Food secondMatch)
    {
        String comparison = null;
        if (firstMatch.getCalories() > secondMatch.getCalories())
        {
            comparison = firstMatch.toString() + " has more calories than " + secondMatch.toString();
        }
        else if (firstMatch.getCalories() > secondMatch.getCalories())
        {
            comparison = secondMatch.toString() + " has more calories than " + firstMatch.toString();
        }
        else
        {
            comparison = firstMatch.toString() + " has the same calories as " + secondMatch.toString();
        }
        return comparison;
    }
}
