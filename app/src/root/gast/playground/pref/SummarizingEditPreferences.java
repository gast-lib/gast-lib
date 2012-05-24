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
package root.gast.playground.pref;

import root.gast.playground.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * handy preferences activity that shows the current value
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SummarizingEditPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    public static final String WHICH_PREFERENCES_INTENT = "whichprefs";
    public static final String WHICH_PREFERENCES_NAME_INTENT = "whichprefsname";
    
    private int preferencesToEdit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesToEdit = R.xml.speech_preferences;
        String preferenceName = getResources().getString(R.string.pref_speech_key);
        //maybe get preferences from an intent...
        if (getIntent() != null)
        {
            preferencesToEdit = getIntent().getIntExtra(WHICH_PREFERENCES_INTENT, R.xml.speech_preferences);
            preferenceName = getIntent().getStringExtra(WHICH_PREFERENCES_NAME_INTENT);
        }
        
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(preferenceName);
        preferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_WRITEABLE);
        
        addPreferencesFromResource(preferencesToEdit);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
         initSummary(getPreferenceScreen().getPreference(i));
        }
    }

    @Override 
    protected void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override 
    protected void onPause() { 
        super.onPause();
        // Unregister the listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);     
    } 

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
    {
        //if it changed a map preference, need to flag the map as changed.. maybe in the return
        //value from this activity
        updatePrefSummary(findPreference(key));
    }
    
    private void initSummary(Preference p){
       if (p instanceof PreferenceCategory){
            PreferenceCategory pCat = (PreferenceCategory)p;
            for(int i=0;i<pCat.getPreferenceCount();i++){
                initSummary(pCat.getPreference(i));
            }
        }else{
            updatePrefSummary(p);
        }

    }

    private void updatePrefSummary(Preference p){
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p; 
            p.setSummary(listPref.getEntry()); 
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p; 
            p.setSummary(editTextPref.getText()); 
        }
    }
    
    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.m_resetprefs:
                //will this do it?
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                
                PreferenceManager.setDefaultValues(SummarizingEditPreferences.this, preferencesToEdit, true);
                break;
            default:
                throw new RuntimeException("unknown menu selection");
        }
        return true;
    }

}
