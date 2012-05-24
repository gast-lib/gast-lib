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

import java.util.List;

import root.gast.R;
import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

/**
 * Uses both the NETWORK_PROVIDER and the GPS_PROVIDER to find the current
 * location. A single location from each provider is considered, and the
 * location with the best accuracy is displayed.
 * 
 * @author Adam Stroud &#60;<a
 *         href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class CurrentLocationActivity extends Activity implements
        LocationListener
{

    private LocationManager locationManager;
    private TextView latitudeValue;
    private TextView longitudeValue;
    private TextView providerValue;
    private TextView accuracyValue;
    private TextView timeToFixValue;
    private TextView enabledProvidersValue;
    private long uptimeAtResume;
    private List<String> enabledProviders;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_location);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        latitudeValue = (TextView) findViewById(R.id.latitudeValue);
        longitudeValue = (TextView) findViewById(R.id.longitudeValue);
        providerValue = (TextView) findViewById(R.id.providerValue);
        accuracyValue = (TextView) findViewById(R.id.accuracyValue);
        timeToFixValue = (TextView) findViewById(R.id.timeToFixValue);
        enabledProvidersValue = (TextView) findViewById(R.id.enabledProvidersValue);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        StringBuffer stringBuffer = new StringBuffer();
        
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        
        enabledProviders = locationManager.getProviders(criteria, true);
        
        if (enabledProviders.isEmpty())
        {
            enabledProvidersValue.setText("");
        }
        else
        {
            for (String enabledProvider : enabledProviders)
            {
                stringBuffer.append(enabledProvider).append(" ");
                
                locationManager.requestSingleUpdate(enabledProvider,
                        this,
                        null);
            }
            
            enabledProvidersValue.setText(stringBuffer);
        }

        uptimeAtResume = SystemClock.uptimeMillis();
        
        latitudeValue.setText("");
        longitudeValue.setText("");
        providerValue.setText("");
        accuracyValue.setText("");
        timeToFixValue.setText("");
        
        findViewById(R.id.timeToFixUnits).setVisibility(View.GONE);
        findViewById(R.id.accuracyUnits).setVisibility(View.GONE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    /**
     * Updates the display with the  new location information if new location
     * information is more accurate than the current location information.
     * 
     * @param location The new location information
     * 
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged(Location location)
    {
        latitudeValue.setText(String.valueOf(location.getLatitude()));
        longitudeValue.setText(String.valueOf(location.getLongitude()));
        providerValue.setText(String.valueOf(location.getProvider()));
        accuracyValue.setText(String.valueOf(location.getAccuracy()));

        long timeToFix = SystemClock.uptimeMillis() - uptimeAtResume;

        timeToFixValue.setText(String.valueOf(timeToFix / 1000));
        
        findViewById(R.id.timeToFixUnits).setVisibility(View.VISIBLE);
        findViewById(R.id.accuracyUnits).setVisibility(View.VISIBLE);
    }

    /**
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider)
    {
        // no-op
    }

    /**
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider)
    {
        // no-op
    }

    /**
     * @see android.location.LocationListener#onStatusChanged(java.lang.String,
     *      int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // no-op
    }

    public void onChangeLocationProvidersSettingsClick(View view)
    {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
