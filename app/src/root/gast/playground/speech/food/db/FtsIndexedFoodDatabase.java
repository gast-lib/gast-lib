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
package root.gast.playground.speech.food.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Creates a db of food and helps code query it
 */
public class FtsIndexedFoodDatabase
{
    private static final String TAG = "FtsIndexedFoodDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FoodDatabaseFts";
    private static final String TABLE_FOOD = "foodlist";

    public static final String COLUMN_FOOD = "food";
    public static final String COLUMN_CALORIE = "calorie";

    private static FtsIndexedFoodDatabase instance;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    private FtsIndexedFoodDatabase(Context context)
    {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
        database = databaseHelper.getWritableDatabase();
    }

    public static synchronized FtsIndexedFoodDatabase getInstance(
            Context context)
    {
        if (instance == null)
        {
            instance =
                    new FtsIndexedFoodDatabase(context.getApplicationContext());
        }

        return instance;
    }

    public List<MatchedFood> retrieveBestMatch(String input)
    {
        return retrieveBestMatch(input, false, false, false);
    }

    /**
     * return a list of best matching Foods ordered by best match
     */
    public List<MatchedFood> retrieveBestMatch(String input, boolean prefix,
            boolean or, boolean phrase)
    {
        final String[] columns =
                { COLUMN_FOOD, COLUMN_CALORIE, "offsets(foodlist) as offsets" };

        // sort the food by a score
        TreeMap<Integer, List<MatchedFood>> scoredMatches =
                new TreeMap<Integer, List<MatchedFood>>();

        input = input.trim();
        // handle different types
        if (prefix)
        {
            // add start at end of the input words
            input = input.replaceAll("\\s", "* ");
            input = input + "*";
        }
        if (or)
        {
            input = input.replaceAll("\\s", " OR ");
        }
        if (phrase)
        {
            input = "\"" + input + "\"";
        }
        Log.d(TAG, "query: " + input);
        String query = COLUMN_FOOD + " MATCH ?";
        Cursor cursor =
                database.query(TABLE_FOOD, columns, query,
                        new String[] { input }, null, null, null);
        try
        {
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false)
                {
                    String food =
                            cursor.getString(cursor.getColumnIndex(COLUMN_FOOD));
                    float cal =
                            cursor.getFloat(cursor
                                    .getColumnIndex(COLUMN_CALORIE));
                    String offsets =
                            cursor.getString(cursor.getColumnIndex("offsets"));
                    // each matching term consists of 4 integers separated by
                    // spaces
                    // offsetTokens[0]: db column number, unused
                    // offsetTokens[1]: term number of matching term
                    // offsetTokens[2,3]: byte values, unused
                    // for more info, see: http://sqlite.org/fts3.html#offsets

                    // add 1 because the last integer has no space after it
                    // divide by 2 because each integer takes up two characters
                    // divide by 4 because each matching term has 4 integers
                    int numMatches = ((offsets.length() + 1) / 2) / 4;
                    // find which tokens matched
                    String[] offsetTokens = offsets.split("\\s");
                    int firstMatchTerm = Integer.valueOf(offsetTokens[1]);
                    int lastMatchTerm =
                            Integer.valueOf(offsetTokens[offsetTokens.length - 3]);
                    Log.d(TAG, "food found: " + food + " num matches: "
                            + numMatches + " offsets: " + offsets);
                    MatchedFood found =
                            new MatchedFood(firstMatchTerm, lastMatchTerm,
                                    new Food(food, cal));
                    List<MatchedFood> foodsAt;
                    if (!scoredMatches.containsKey(numMatches))
                    {
                        foodsAt = new ArrayList<MatchedFood>();
                        scoredMatches.put(numMatches, foodsAt);
                    }
                    else
                    {
                        foodsAt = scoredMatches.get(numMatches);
                    }
                    foodsAt.add(found);

                    cursor.moveToNext();
                }
            }
        } finally
        {
            cursor.close();
        }

        List<MatchedFood> match = new ArrayList<MatchedFood>();
        for (List<MatchedFood> foodLists : scoredMatches.descendingMap()
                .values())
        {
            match.addAll(foodLists);
        }
        Log.d(TAG, match.size() + " matches.");
        for (MatchedFood matchedFood : match)
        {
            Log.d(TAG, matchedFood.getFood().toString());
        }
        return match;
    }

    public boolean isEmpty()
    {
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_FOOD, null);
        boolean isEmpty = (cursor.getCount() == 0);
        cursor.close();
        return isEmpty;
    }

    public void loadFrom(InputStream csvFile) throws IOException
    {
        BufferedReader is =
                new BufferedReader(new InputStreamReader(csvFile, "UTF8"));
        String line;

        line = is.readLine();
        while (line != null)
        {
            String[] parts = line.split(",");
            String food = parts[0];
            float cals = Float.valueOf(parts[1]);
            insertFood(food, cals);
            Log.d(TAG, "inserted: " + food + " " + cals);
            line = is.readLine();
        }
    }

    public long insertFood(String food, float calorie)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FOOD, food);
        contentValues.put(COLUMN_CALORIE, calorie);
        return database.insert(TABLE_FOOD, null, contentValues);
    }

    public int removeFood(String food)
    {
        return database.delete(TABLE_FOOD, COLUMN_FOOD + " = ?",
                new String[] { food });
    }

    public void close()
    {
        synchronized (FtsIndexedFoodDatabase.class)
        {
            databaseHelper.close();
            instance = null;
            database = null;
        }
    }

    public Cursor getAllFood()
    {
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_FOOD, null);
        return cursor;
    }

    public void clean(Context context)
    {
        databaseHelper.dropTables(database);
        databaseHelper.createTables(database);
        instance = new FtsIndexedFoodDatabase(context.getApplicationContext());
    }

    private static final class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            createTables(db);
        }

        @Override
        public void
                onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            dropTables(db);
            createTables(db);
        }

        public void dropTables(SQLiteDatabase db)
        {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD + ";");
        }

        public void createTables(SQLiteDatabase db)
        {
            db.execSQL("CREATE VIRTUAL TABLE " + TABLE_FOOD
                    + " USING fts3(tokenize=porter," + BaseColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FOOD
                    + " TEXT, " + COLUMN_CALORIE + " REAL);");
        }
    }
}

// public Cursor getFoodCursor(String[] columns, String selection,
// String[] selectionArgs, String orderBy)
// {
// return database.query(TABLE_FOOD, columns, selection, selectionArgs,
// null, null, orderBy);
// }
// public void deleteData()
// {
// database.delete(TABLE_FOOD, null, null);
// }
// return retrieveBestMatch("apple").size() == 0;

