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
package root.gast.playground.speech.food.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import root.gast.playground.speech.food.db.Food;

/**
 * code used in the book to show how to use domain knowledge to make best guesses
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class WhichFoodGuesser
{
    private static final Set<String> foodsDislike;

    static
    {
        foodsDislike = new HashSet<String>();
        foodsDislike.add("Green Onion");
        foodsDislike.add("Green Beans");
    }

    public static Food pickMostLikelyFood(List<Food> possibleFoods)
    {
        if (possibleFoods.size() == 0)
        {
            return null;
        }
        
        Food mostLikely = possibleFoods.get(0);
        for (Food food : possibleFoods)
        {
            if (!foodsDislike.contains(food.getName()))
            {
                mostLikely = food;
                break;
            }
        }
        return mostLikely;
    }
}
