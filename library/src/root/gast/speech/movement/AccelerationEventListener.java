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
package root.gast.speech.movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * 
 * @author Adam Stroud &#60;<a
 *         href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class AccelerationEventListener implements SensorEventListener
{
    private static final String TAG = "AccelerationEventListener";
    private static final int THRESHHOLD = 2;
    private static final float ALPHA = 0.8f;
    private static final int HIGH_PASS_MINIMUM = 10;

    private float[] gravity;
    private int highPassCount;
    private boolean useHighPassFilter;

    private MovementDetectionListener callback;
    
    public AccelerationEventListener(boolean useHighPassFilter, 
            MovementDetectionListener callback)
    {
        this.useHighPassFilter = useHighPassFilter;
        gravity = new float[3];
        highPassCount = 0;
        this.callback = callback;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] values = event.values;

        // Pass values through high-pass filter if enabled
        if (useHighPassFilter)
        {
            values =
                    highPass(event.values[0], event.values[1], event.values[2]);
        }

        // Ignore data if the high-pass filter is enabled, has not yet received
        // some data to set it
        if (!useHighPassFilter || (++highPassCount >= HIGH_PASS_MINIMUM))
        {
            double sumOfSquares =
                    (values[0] * values[0]) + (values[1] * values[1])
                            + (values[2] * values[2]);
            double acceleration = Math.sqrt(sumOfSquares);

            // A "movement" is only triggered of the total acceleration is
            // above a threshold
            if (acceleration > THRESHHOLD)
            {
                Log.i(TAG, "Movement detected");
                callback.movementDetected(true);
            }
        }
    }

    /**
     * TODO
     * 
     * alpha is calculated as t / (t + dT) with t, the low-pass filter's
     * time-constant and dT, the event delivery rate
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    private float[] highPass(float x, float y, float z)
    {
        float[] filteredValues = new float[3];

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];

        return filteredValues;
    }

    /**
     * TODO
     */
    public void stop()
    {
    }

    /**
     * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor,
     *      int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }
}
