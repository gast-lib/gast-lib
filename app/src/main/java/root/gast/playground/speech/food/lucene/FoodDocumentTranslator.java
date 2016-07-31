/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.speech.food.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;

import root.gast.playground.speech.food.db.Food;

/**
 * conversion to and from lucene {@link Document}s
 */
public class FoodDocumentTranslator
{
    public static final String FOOD_NAME = "food";
    public static final String CALORIE = "cal";
    
    public static Food getFood(Document doc)
    {
        return new Food(doc.get(FOOD_NAME), Float.parseFloat(doc.get(CALORIE)));
    }

    public static Document toDocument(Food food)
    {
        Document doc = new Document();
        doc.add(new Field(FOOD_NAME, food.getName(), Field.Store.YES,
                Field.Index.ANALYZED));
        NumericField numberField =
                new NumericField(CALORIE, Field.Store.YES, true);
        numberField.setFloatValue(food.getCalories());
        doc.add(numberField);
        return doc;
    }
}
