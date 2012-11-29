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

import root.gast.playground.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * TODO
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class SensorDisplayActivity extends FragmentActivity
{
	public static final String KEY_SENSOR_TYPE = "sensorType";
	public static final String KEY_SENSOR_NAME = "sensorName";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_display);
		
		SensorDisplayFragment fragment =
				(SensorDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.sensorDisplayFragment);
		
		Intent intent = getIntent();
		fragment.displaySensor(intent.getIntExtra(KEY_SENSOR_TYPE, -1),
				               intent.getStringExtra(KEY_SENSOR_NAME));
	}
}
