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
package root.gast.playground.location;

import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Broadcast receiver that will receive a location and store it in a database.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class TrackLocationBroadcastReceiver extends FilteringLocationBroadcastReceiver
{
    private static final String TAG = "TrackLocationBroadcastReceiver";
    
    @Override
    public void onFilteredLocationChanged(Context context, Location location)
    {
        Log.d(TAG, "Received Location -> " + location); 
        
        if (location != null)
        {
            if (PointDatabaseManager.getInstance(context).insertPoint(location) == -1)
            {
                Log.e(TAG, "Unable to save point");
            }
        }
    }
}
