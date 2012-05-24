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

import root.gast.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * Activity that records a device's current location and plots both the current location
 * and historical location on a Google map.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class TrackLocationActivity extends MapActivity
{
    private static final String TAG = "TrackLocationActivity";
    private static final int REQUEST_CODE = 0;
    private static final String TRACKING_PREFERENCE_KEY =
            "TRACKING_PREFERENCE_KEY";
    private static final String ADD_LOCATION_ACTION =
            "root.gast.playground.location.ACTION_LOCATION_CHANGED";
    
    private LocationManager locationManager;
    private MapView mapView;
    private TrackLocationOverlay trackLocationOverlay;
    private PendingIntent pendingIntent;
    private Button startTrackingButton;
    private Button stopTrackingButton;
    private boolean tracking;
    private SharedPreferences preferences;
    private PointDatabaseManager pointDatabaseManager;
    private UpdateViewBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_location);
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        mapView = (MapView) findViewById(R.id.mapView);
        startTrackingButton =
                (Button) findViewById(R.id.startTrackingLocationButton);
        stopTrackingButton =
                (Button) findViewById(R.id.stopTrackingLocationButton);
        
        mapView.setBuiltInZoomControls(true);

        Drawable starDrawable =
                getResources().getDrawable(android.R.drawable.star_on);
        trackLocationOverlay =
                new TrackLocationOverlay(starDrawable, mapView);
        
        mapView.getOverlays().add(trackLocationOverlay);
        
        preferences = getPreferences(MODE_PRIVATE);
        
        pointDatabaseManager =
                PointDatabaseManager.getInstance(getApplicationContext());
        broadcastReceiver =
                new UpdateViewBroadcastReceiver(trackLocationOverlay, mapView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        registerReceiver(broadcastReceiver,
                new IntentFilter(ADD_LOCATION_ACTION));
        
        tracking = preferences.getBoolean(TRACKING_PREFERENCE_KEY, false);
        
        startTrackingButton.setEnabled(!tracking);
        stopTrackingButton.setEnabled(tracking);
        
        if (tracking)
        {
            String[] queryString =
                    new String[]
                    {
                        PointDatabaseManager.COLUMN_LATITUDE,
                        PointDatabaseManager.COLUMN_LONGITUDE,
                        PointDatabaseManager.COLUMN_ACCURACY
                    };
            Cursor pointCursor =
                    pointDatabaseManager.getPointCursor(queryString,
                            null,
                            null,
                            "_id ASC");
            
            if (pointCursor.getCount() > 0)
            {
                for (pointCursor.moveToFirst(); !pointCursor.isAfterLast(); pointCursor.moveToNext())
                {
                    double latitude = pointCursor.getDouble(pointCursor.getColumnIndex(PointDatabaseManager.COLUMN_LATITUDE));
                    double longitude = pointCursor.getDouble(pointCursor.getColumnIndex(PointDatabaseManager.COLUMN_LONGITUDE));
                    float accuracy = pointCursor.getFloat(pointCursor.getColumnIndex(PointDatabaseManager.COLUMN_ACCURACY));
                    
                    trackLocationOverlay.addPoint(latitude, longitude, accuracy);
                }
            }
            
            pointCursor.close();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        preferences.edit().putBoolean(TRACKING_PREFERENCE_KEY, tracking).commit();
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return true;
    }

    @Override
    protected boolean isLocationDisplayed()
    {
        return true;
    }
    
    public void onStartTrackingButtonClick(View view)
    {
        Log.d(TAG, "Tracking = " + tracking);
        
        if (!tracking)
        {
            startTrackingButton.setEnabled(false);
            stopTrackingButton.setEnabled(true);
            
            pendingIntent = createPendingIntent();
            
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            
            for (String provider : locationManager.getProviders(criteria, true))
            {
                Log.d(TAG, "Enabling provider " + provider);
                locationManager.requestLocationUpdates(provider,
                        0,
                        0,
                        pendingIntent);
            }
            
            tracking = true;
        }
    }

    public void onStopTrackingButtonClick(View view)
    {
        if (tracking)
        {
            if (pendingIntent == null)
            {
                pendingIntent = createPendingIntent();
            }
            
            locationManager.removeUpdates(pendingIntent);
            
            startTrackingButton.setEnabled(true);
            stopTrackingButton.setEnabled(false);
            
            tracking = false;
            
            pointDatabaseManager.deletePoints();
            pointDatabaseManager.close();
        }
    }

    private PendingIntent createPendingIntent()
    {
        Intent intent = new Intent(ADD_LOCATION_ACTION);
        return PendingIntent.getBroadcast(getApplicationContext(),
                                          REQUEST_CODE,
                                          intent,
                                          PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static class UpdateViewBroadcastReceiver
    extends FilteringLocationBroadcastReceiver
    {
        private TrackLocationOverlay trackLocationOverlay;
        private MapView mapView;
        private boolean initialZoomSet;

        public UpdateViewBroadcastReceiver(TrackLocationOverlay overlay,
                MapView mapView)
        {
            this.trackLocationOverlay = overlay;
            this.mapView = mapView;
        }

        @Override
        public void onFilteredLocationChanged(Context context,
                Location location)
        {
            trackLocationOverlay.addPoint(location.getLatitude(),
                    location.getLongitude(),
                    location.getAccuracy());
            
            int latE6 = (int) (location.getLatitude() * 1E6);
            int longE6 = (int) (location.getLongitude() * 1E6);
            
            mapView.getController().animateTo(new GeoPoint(latE6, longE6));
            
            if (!initialZoomSet)
            {
                mapView.getController().setZoom(15);
                initialZoomSet = true;
            }
        }
    }
}
