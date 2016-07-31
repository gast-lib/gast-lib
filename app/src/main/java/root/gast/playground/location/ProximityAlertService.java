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

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Service that provides an alternative implementation for a proximity alert.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class ProximityAlertService extends Service implements LocationListener
{
    public static final String LATITUDE_INTENT_KEY = "LATITUDE_INTENT_KEY";
    public static final String LONGITUDE_INTENT_KEY = "LONGITUDE_INTENT_KEY";
    public static final String RADIUS_INTENT_KEY = "RADIUS_INTENT_KEY";
    private static final String TAG = "ProximityAlertService";
    
    private double latitude;
    private double longitude;
    private float radius;
    private LocationManager locationManager;
    private boolean inProximity;

    @Override
    public IBinder onBind(Intent intent)
    {
        // no-op
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();    
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Location bestLocation = null;
        
        latitude = intent.getDoubleExtra(LATITUDE_INTENT_KEY, Double.MIN_VALUE);
        longitude = intent.getDoubleExtra(LONGITUDE_INTENT_KEY, Double.MIN_VALUE);
        radius = intent.getFloatExtra(RADIUS_INTENT_KEY, Float.MIN_VALUE);
        
        for (String provider : locationManager.getProviders(false))
        {
            Location location = locationManager.getLastKnownLocation(provider);
            
            if (bestLocation == null)
            {
                bestLocation = location;
            }
            else
            {
                if (location.getAccuracy() < bestLocation.getAccuracy())
                {
                    bestLocation = location;
                }
            }
        }
        
        if (bestLocation != null)
        {
            if (getDistance(bestLocation) <= radius)
            {
                inProximity = true;
            }
            else
            {
                inProximity = false;
            }
        }
        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0,
                0,
                this);
        
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        float distance = getDistance(location);
        
        if (distance <= radius && !inProximity)
        {
            inProximity = true;
            Log.i(TAG, "Entering Proximity");
            
            Intent intent =
                    new Intent(ProximityPendingIntentFactory.PROXIMITY_ACTION);
            intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
            sendBroadcast(intent);
        }
        else if (distance > radius && inProximity)
        {
            inProximity = false;
            Log.i(TAG, "Exiting Proximity");
            
            Intent intent =
                    new Intent(ProximityPendingIntentFactory.PROXIMITY_ACTION);
            intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
            sendBroadcast(intent);
        }
        else
        {
            float distanceFromRadius = Math.abs(distance - radius);
            
            // Calculate the distance to the edge of the user-defined radius
            // around the target location
            float locationEvaluationDistance =
                    (distanceFromRadius - location.getAccuracy()) / 2;
            
            locationManager.removeUpdates(this);
            float updateDistance = Math.max(1, locationEvaluationDistance);
            
            String provider;
            if (distanceFromRadius <= location.getAccuracy()
                    || LocationManager.GPS_PROVIDER.equals(location.getProvider()))
            {
                provider = LocationManager.GPS_PROVIDER;
            }
            else
            {
                provider = LocationManager.NETWORK_PROVIDER;
            }
            
            locationManager.requestLocationUpdates(provider, 0, updateDistance, this);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // no-op
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        // no-op
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // no-op
    }

    private float getDistance(Location location)
    {
        float[] results = new float[1];
        
        Location.distanceBetween(latitude,
                longitude,
                location.getLatitude(),
                location.getLongitude(),
                results);
        
        return results[0];
    }
}
