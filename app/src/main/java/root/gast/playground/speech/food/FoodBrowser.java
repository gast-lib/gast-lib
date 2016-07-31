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
package root.gast.playground.speech.food;

import root.gast.playground.R;
import root.gast.playground.speech.food.db.FtsIndexedFoodDatabase;
import root.gast.playground.util.DialogGenerator;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * show all the foods in the {@link FtsIndexedFoodDatabase}
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class FoodBrowser extends ListActivity
{
    private static final String TAG = "FoodBrowser";
    
    // thank you:
    // http://thinkandroid.wordpress.com/2010/01/09/simplecursoradapters-and-listviews/
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.foodbrowselist);
        setData();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View viewClicked, int arg2,
                    long arg3)
            {
                TextView tv = (TextView)viewClicked.findViewById(R.id.foodname_entry);
                final String foodName = (String)tv.getText();
                DialogGenerator.createConfirmDialog(FoodBrowser.this, "Do you want to delete: " + foodName + "?", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FtsIndexedFoodDatabase.getInstance(FoodBrowser.this).removeFood(foodName);
                        setData();
                        Toast.makeText(FoodBrowser.this, "Deleted: " + foodName, Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
    }
    
    private void setData()
    {
        cursor = FtsIndexedFoodDatabase.getInstance(this).getAllFood();
        startManagingCursor(cursor);

        String[] columns =
                new String[] {FtsIndexedFoodDatabase.COLUMN_FOOD,
                        FtsIndexedFoodDatabase.COLUMN_CALORIE };
        int[] to = new int[] { R.id.foodname_entry, R.id.foodcalorie_entry };
        // create the adapter using the cursor pointing to the desired data as
        // well as the layout information
        SimpleCursorAdapter mAdapter =
                new SimpleCursorAdapter(this, R.layout.foodbrowselist_entry,
                        cursor, columns, to);
        setListAdapter(mAdapter);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
//        FtsIndexedFoodDatabase.getInstance(this).close();
    }
}
