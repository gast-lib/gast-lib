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
package root.gast.playground.speech.food.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import root.gast.playground.speech.food.db.Food;
import root.gast.speech.lucene.LuceneIndexBuilder;
import root.gast.speech.lucene.RecognitionIndexer;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class FoodIndexBuilder
{
    private static final String TAG = "FoodIndexBuilder";

    private LuceneIndexBuilder builder;

    private Analyzer analyzer;

    public FoodIndexBuilder(boolean phonetic, boolean doStem)
    {
        analyzer = new RecognitionIndexer(phonetic, doStem);
        builder =
                new LuceneIndexBuilder(new RecognitionIndexer(phonetic, doStem));
    }

    public FoodIndexBuilder(String outputDir, boolean overwrite,
            boolean phonetic, boolean doStem)
    {
        analyzer = new RecognitionIndexer(phonetic, doStem);
        builder =
                new LuceneIndexBuilder(outputDir, overwrite,
                        new RecognitionIndexer(phonetic, doStem));
    }

    public void addFood(String name, float calories)
    {
        Document doc =
                FoodDocumentTranslator.toDocument(new Food(name, calories));
        builder.addDocument(doc);
        Log.d(TAG, "added: " + doc);
    }

    public FoodSearcher get() throws IOException
    {
        builder.doneWriting();
        return new FoodSearcher(builder.getDirectory(), analyzer);
    }
}
