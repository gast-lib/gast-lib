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

import java.util.Locale;

import root.gast.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * help deal with all the preferences
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class PreferenceHelper
{
    private static final String TAG = "PreferenceHelper";
    
    public final static double NOT_SET = Double.MAX_VALUE;

    private SharedPreferences preferences;

    /**
     * 
     */
    public PreferenceHelper(String preferencesName, Context context)
    {
        preferences = context.getSharedPreferences(preferencesName,
                Context.MODE_WORLD_WRITEABLE);
    }

    /**
     * @return the preferences
     */
    public SharedPreferences getPrefs()
    {
        return preferences;
    }

    public int getInteger(Context context, int prefNameResource, int defaultValResource)
    {
        String prefName = context.getResources().getString(prefNameResource);
        int defaultVal = context.getResources().getInteger(defaultValResource);
        Log.d(TAG, prefName + " with default: " + defaultVal);
        int pref = preferences.getInt(prefName, defaultVal); 
        return pref;
    }

//    public int getInt(String prefName, String defaultVal)
//    {
//        SharedPreferences preferences = getPrefs();
//        int pref = Integer.parseInt(preferences.getString(prefName, defaultVal));
//        return pref;
//    }

    public int getInt(Context context, int prefNameResource, int defaultValResource)
    {
        String prefName = context.getResources().getString(prefNameResource);
        String defaultVal = context.getResources().getString(defaultValResource);
        Log.d(TAG, prefName + " with default: " + defaultVal);
        int pref = Integer.valueOf(preferences.getString(prefName, defaultVal)); 
        return pref;
    }

    public long getLong(Context context, int prefNameResource, int defaultValResource)
    {
        String prefName = context.getResources().getString(prefNameResource);
        String defaultVal = context.getResources().getString(defaultValResource);
        return getLong(prefName, defaultVal);
    }

    public long getLong(String prefName, String defaultVal)
    {
        SharedPreferences preferences = getPrefs();
        long pref = Long.valueOf(preferences.getString(prefName, defaultVal));
        return pref;
    }

//    public double getDouble(String prefName, double defaultVal)
//    {
//        SharedPreferences preferences = getPrefs();
//        double pref = Double.parseDouble(preferences.getString(prefName,
//                String.valueOf(defaultVal)));
//        return pref;
//    }
//
    public float getFloat(Context context, int prefNameResource, int defaultValResource)
    {
        SharedPreferences preferences = getPrefs();
        String prefName = context.getResources().getString(prefNameResource);
        String defaultVal = context.getResources().getString(defaultValResource);
        float pref = Float.valueOf(preferences.getString(prefName, defaultVal));
        return pref;
    }

    public String getString(Context context, int prefNameResource, int defaultValResource)
    {
        SharedPreferences preferences = getPrefs();
        // String prefName = activity.getResources().getString(R.id)
        String prefName = context.getResources().getString(prefNameResource);
        String defaultVal = context.getResources().getString(defaultValResource);
        String pref = preferences.getString(prefName, defaultVal);
        Log.d(TAG, "prefname " + prefName + " val " + defaultVal + " pref " + pref);
        return pref;
    }

    public String getString(String prefName, String defaultVal)
    {
        SharedPreferences preferences = getPrefs();
        String pref = preferences.getString(prefName, defaultVal);
        return pref;
    }

    public void setLanguage(Context context, Locale locale)
    {
        SharedPreferences preferences = getPrefs();
        Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.pref_language), locale.toString());
        editor.commit();
    }

    public void setInt(String preferenceName, int value)
    {
        SharedPreferences preferences = getPrefs();
        Editor editor = preferences.edit();
        editor.putInt(preferenceName, value);
        editor.commit();
    }

    public void setString(String preferenceName, String value)
    {
        SharedPreferences preferences = getPrefs();
        Editor editor = preferences.edit();
        editor.putString(preferenceName, value);
        editor.commit();
    }

    public boolean isLanguageSet(Context context)
    {
        SharedPreferences preferences = getPrefs();
        return preferences.getAll().containsKey(context.getString(R.string.pref_language));
    }

    
    public boolean getBoolean(Context context, int prefNameResource, int defaultValResource)
    {
        String prefName = context.getResources().getString(prefNameResource);
        String defaultVal = context.getResources().getString(defaultValResource);
        return getBoolean(prefName, defaultVal);
    }

    public  boolean getBoolean(String prefName, String defaultVal)
    {
        SharedPreferences preferences = getPrefs();
//        boolean pref = 
//            Boolean.valueOf(preferences.getString(prefName, defaultVal));
        boolean pref = preferences.getBoolean(prefName, Boolean.valueOf(defaultVal));
        return pref;
    }
}
