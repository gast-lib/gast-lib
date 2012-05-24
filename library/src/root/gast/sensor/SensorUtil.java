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
package root.gast.sensor;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class SensorUtil
{
    public static boolean isAccelerometerSupported(Context context)
    {
        SensorManager sm =
                (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        return sensors.size() > 0;
    }

    public static boolean isSupported(Context context, int sensorType)
    {
        SensorManager sm =
                (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(sensorType);
        return sensors.size() > 0;
    }
}
