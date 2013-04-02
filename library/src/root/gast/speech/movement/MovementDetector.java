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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class MovementDetector
{
    private static final String TAG = "MovementDetector";

    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;

    private SensorManager sensorManager;
    private boolean readingAccelerationData;
    private AccelerationEventListener sensorListener;
    private boolean useHighPassFilter;
    private boolean useAccelerometer;
    private int threshold;
    private long minTimeBetweenMovementDetections;

    public MovementDetector(Context context)
    {
        this(context, false);
    }

    public MovementDetector(Context context, boolean useAccelerometer)
    {
        this(context, false, useAccelerometer, AccelerationEventListener.THRESHOLD_LOW);
    }
    
    public void setMinTimeBetweenMovementDetections(
        long minTimeBetweenMovementDetections)
    {
        this.minTimeBetweenMovementDetections = minTimeBetweenMovementDetections;
    }

    /**
     * @param useAccelerometer otherwise use linear acceleration
     * @param threshold from {@link AccelerationEventListener#THRESHOLD_HIGH}
     */
    public MovementDetector(Context context, boolean useHighPassFilter, boolean useAccelerometer, int threshold)
    {
        sensorManager =
                (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
        this.useAccelerometer = useAccelerometer;
        this.useHighPassFilter = useHighPassFilter;
        this.threshold = threshold;
        this.minTimeBetweenMovementDetections = AccelerationEventListener.DEFAULT_MIN_TIME_BETWEEN_MOVEMENT_DISABLED;
    }

    public void startReadingAccelerationData(MovementDetectionListener resultCallback)
    {
        //stop anything that is currently happening
        stopReadingAccelerationData();
        
        if (!readingAccelerationData)
        {
            // Data files are stored on the external cache directory so they can
            // be pulled off of the device by the user
            if (useAccelerometer)
            {
                sensorListener =
                        new AccelerationEventListener(
                            useHighPassFilter, resultCallback, threshold, minTimeBetweenMovementDetections);
                sensorManager.registerListener(sensorListener,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        RATE);
            }
            else
            {
                sensorListener =
                        new AccelerationEventListener(useHighPassFilter, resultCallback, threshold, minTimeBetweenMovementDetections);
                sensorManager.registerListener(sensorListener,
                        sensorManager
                                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        RATE);
            }

            readingAccelerationData = true;

            Log.d(TAG, "Started reading acceleration data");
        }
    }

    public void stopReadingAccelerationData()
    {
        if (readingAccelerationData && sensorListener != null)
        {
            sensorManager.unregisterListener(sensorListener);

            // Tell listeners to clean up after themselves
            sensorListener.stop();

            readingAccelerationData = false;

            Log.d(TAG, "Stopped reading acceleration data");
        }
    }
}
