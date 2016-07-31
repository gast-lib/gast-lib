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
import java.util.List;

import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.playground.speech.food.db.MatchedFood;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstander;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstanderNoOrder;
import root.gast.playground.speech.food.multimatcher.MultiPartUnderstanderOrdered;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class TestAndroidFtsDb extends AndroidTestCase
{
    private static final String TAG = "TestAndroidDb";

    public void testMulti()
    {
        FtsIndexedFoodDatabase food = FtsIndexedFoodDatabase.getInstance(getContext());
        InputStream stream = getContext().getResources().openRawResource(root.gast.playground.R.raw.foods);
        Log.d(TAG, "loading food");
        try
        {
            food.loadFrom(stream);
        }
        catch (IOException io)
        {
            Log.d(TAG, "failed to load db");
        }

        MultiPartUnderstander noOrder = new MultiPartUnderstanderNoOrder();
        MultiPartUnderstander order = new MultiPartUnderstanderOrdered();

        food = FtsIndexedFoodDatabase.getInstance(getContext());
        
        noOrder.compareCalories("Green Bean");
        
    }

    public void estExec()
    {
        FtsIndexedFoodDatabase food = FtsIndexedFoodDatabase.getInstance(getContext());
        food.clean(getContext());
        food = FtsIndexedFoodDatabase.getInstance(getContext());
        food.insertFood("Red Kidney bean", 110);
        food.insertFood("Green string bean", 100);
        food.insertFood("white beans", 80);

        //will it find numbers or only the text fields
        List<MatchedFood> foods = food.retrieveBestMatch("100");

        if (foods.size() == 0)
        {
            Log.d(TAG, "NO FOODS!");
        }
        //add a bunch of stuff
        for (MatchedFood food2 : foods)
        {
            Log.d(TAG, "food: " + food2.getFood());
        }
        
    }
    
    public void testExecFoodList()
    {
        FtsIndexedFoodDatabase food = FtsIndexedFoodDatabase.getInstance(getContext());
        food.clean(getContext());
        food.insertFood("Red Concord Grapes", 110);
        food.insertFood("red grape", 110); //s
        food.insertFood("grape leaves", 110);
        food.insertFood("orangegrapefruit juice", 110); //will *input match it?
        food.insertFood("Grapes", 80);
        food.insertFood("Grape", 80);
        food.insertFood("Grapefruit", 90);
        food.insertFood("Red Grapefruit", 100);
      
        Log.d(TAG, "STARTING TEST QUERIES");
        log("Q3 grape prefix", food.retrieveBestMatch("grape", true, false, false));
        log("Q4 red grape no prefix", food.retrieveBestMatch("red grape", false, false, false));
        log("Q5 red grape phrase", food.retrieveBestMatch("red grape", false, false, true));
        log("Q6 red grape prefix", food.retrieveBestMatch("red grape", true, false, false));
        log("Q8 grapes or", food.retrieveBestMatch("red grape", false, true, false));
        Log.d(TAG, "DONE TEST QUERIES");
    }

    /**
     * @param input
     * @param foods
     */
    private void log(String input, List<MatchedFood> foods)
    {
        if (foods.size() == 0)
        {
            Log.d(TAG, input + ": NO FOODS!");
        }
        //Log result
        for (MatchedFood food2 : foods)
        {
            Log.d(TAG, input + ": food: " + food2.getFood());
        }
    }

    //maybe digest the matchinfo function
//    static void rankfunc(sqlite3_context *pCtx, int nVal, sqlite3_value **apVal){
//        int *aMatchinfo;                /* Return value of matchinfo() */
//        int nCol;                       /* Number of columns in the table */
//        int nPhrase;                    /* Number of phrases in the query */
//        int iPhrase;                    /* Current phrase */
//        double score = 0.0;             /* Value to return */
//
//        assert( sizeof(int)==4 );
//
//        /* Check that the number of arguments passed to this function is correct.
//        ** If not, jump to wrong_number_args. Set aMatchinfo to point to the array
//        ** of unsigned integer values returned by FTS function matchinfo. Set
//        ** nPhrase to contain the number of reportable phrases in the users full-text
//        ** query, and nCol to the number of columns in the table.
//        */
//        if( nVal<1 ) goto wrong_number_args;
//        aMatchinfo = (unsigned int *)sqlite3_value_blob(apVal[0]);
//        nPhrase = aMatchinfo[0];
//        nCol = aMatchinfo[1];
//        if( nVal!=(1+nCol) ) goto wrong_number_args;
//
//        /* Iterate through each phrase in the users query. */
//        for(iPhrase=0; iPhrase<nPhrase; iPhrase++){
//          int iCol;                     /* Current column */
//
//          /* Now iterate through each column in the users query. For each column,
//          ** increment the relevancy score by:
//          **
//          **   (<hit count> / <global hit count>) * <column weight>
//          **
//          ** aPhraseinfo[] points to the start of the data for phrase iPhrase. So
//          ** the hit count and global hit counts for each column are found in 
//          ** aPhraseinfo[iCol*3] and aPhraseinfo[iCol*3+1], respectively.
//          */
//          int *aPhraseinfo = &aMatchinfo[2 + iPhrase*nCol*3];
//          for(iCol=0; iCol<nCol; iCol++){
//            int nHitCount = aPhraseinfo[3*iCol];
//            int nGlobalHitCount = aPhraseinfo[3*iCol+1];
//            double weight = sqlite3_value_double(apVal[iCol+1]);
//            if( nHitCount>0 ){
//              score += ((double)nHitCount / (double)nGlobalHitCount) * weight;
//            }
//          }
//        }
//
//        sqlite3_result_double(pCtx, score);
//        return;
//
//        /* Jump here if the wrong number of arguments are passed to this function */
//      wrong_number_args:
//        sqlite3_result_error(pCtx, "wrong number of arguments to function rank()", -1);
//      }
}
