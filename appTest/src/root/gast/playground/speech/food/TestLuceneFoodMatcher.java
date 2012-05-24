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

import root.gast.playground.speech.food.lucene.FoodIndexBuilder;
import root.gast.playground.speech.food.lucene.FoodSearcher;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class TestLuceneFoodMatcher extends AndroidTestCase
{
    private static final String TAG = "TestLuceneFoodMatcher";
    
    public void testLucene()
    {
        FoodIndexBuilder builder = new FoodIndexBuilder(false, true);
        builder.addFood("Apple", 100.0f);
        builder.addFood("Pear", 110.0f);
        builder.addFood("Chocolate", 200.0f);
        FoodSearcher searcher = null;
        try
        {
            searcher = builder.get();
        } catch (IOException e)
        {
            Log.e(TAG, "error",e);
        }
        searcher.findMatching("Apple").size();
        searcher.findMatching("Apples");
        searcher.findMatching("Appeal").size();
    }

}
