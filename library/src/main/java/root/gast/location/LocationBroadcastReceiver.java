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
package root.gast.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Receives location information.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public abstract class LocationBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "LocationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "Received Intent");
        
        if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED))
        {
            Log.d(TAG, "Received KEY_LOCATION_CHANGED");
            
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            onLocationChanged(context, location);
        }
        else if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED))
        {
            Log.d(TAG, "Received KEY_PROVIDER_ENABLED");
            
            if (intent.getExtras().getBoolean(LocationManager.KEY_PROVIDER_ENABLED))
            {
                onProviderEnabled(null);
            }
            else
            {
                onProviderDisabled(null);
            }
        }
        else if (intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING))
        {
            Log.d(TAG, "Received KEY_PROXIMITY_ENTERING");
            
            if (intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false))
            {
                onEnteringProximity(context);
            }
            else
            {
                onExitingProximity(context);
            }
        }
        else if (intent.hasExtra(LocationManager.KEY_STATUS_CHANGED))
        {
            
        }
    }
    
    public void onLocationChanged(Context context, Location location) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}
    public void onEnteringProximity(Context context) {}
    public void onExitingProximity(Context context) {}
    public void onStatusChanged() {}
}
