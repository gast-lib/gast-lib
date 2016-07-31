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
package root.gast.playground.sensor;

import java.util.List;

import root.gast.playground.BuildConfig;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class SensorSelectorFragment extends ListFragment
{
    private static final String TAG = "SensorSelectorFragment";

    private SensorDisplayFragment sensorDisplay;

    /**
     * connect with a display fragment to call later when user clicks a sensor
     * name, also setup the ListAdapter to show all the Sensors
     */
    public void setSensorDisplay(SensorDisplayFragment sensorDisplay)
    {
        this.sensorDisplay = sensorDisplay;

        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Activity.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        this.setListAdapter(new SensorListAdapter(getActivity()
                .getApplicationContext(), android.R.layout.simple_list_item_1,
                sensors));
    }

    /**
     * hide the list of sensors and show the sensor display fragment
     * add these changes to the backstack
     */
    private void showSensorFragment(Sensor sensor)
    {
        sensorDisplay.displaySensor(sensor);
        FragmentTransaction ft =
                getActivity().getSupportFragmentManager().beginTransaction();
        ft.hide(this);
        ft.show(sensorDisplay);
        ft.addToBackStack("Showing sensor: " + sensor.getName());
        ft.commit();
    }

    /**
     * list view adapter to show sensor names and respond to clicks.
     */
    private class SensorListAdapter extends ArrayAdapter<Sensor>
    {
        public SensorListAdapter(Context context, int textViewResourceId,
                List<Sensor> sensors)
        {
            super(context, textViewResourceId, sensors);
        }

        /**
         * create a text view containing the sensor name
         */
        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent)
        {
            final Sensor selectedSensor = getItem(position);
            if (convertView == null)
            {
                convertView =
                        LayoutInflater.from(getContext()).inflate(
                                android.R.layout.simple_list_item_1, null);
            }

            ((TextView) convertView).setText(selectedSensor.getName());

            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (BuildConfig.DEBUG)
                    {
                        Log.d(TAG,
                                "display sensor! " + selectedSensor.getName());
                    }

                    showSensorFragment(selectedSensor);
                }
            });
            return convertView;
        }
    }
}
