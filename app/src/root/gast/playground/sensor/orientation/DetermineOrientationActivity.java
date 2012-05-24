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
package root.gast.playground.sensor.orientation;

import java.util.HashMap;
import java.util.List;

import root.gast.playground.R;
import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Determines whether the device is face up or face down and gives a audio
 * notification (via TTS) when the face-up/face-down orientation changes.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class DetermineOrientationActivity extends SpeechRecognizingAndSpeakingActivity implements SensorEventListener
{
    private static final String TAG = "DetermineOrientationActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private static final int TTS_STREAM = AudioManager.STREAM_NOTIFICATION;
    private static final String TTS_NOTIFICATION_PREFERENCES_KEY =
            "TTS_NOTIFICATION_PREFERENCES_KEY";
    private static final double GRAVITY_THRESHOLD =
            SensorManager.STANDARD_GRAVITY / 2;
    
    private SensorManager sensorManager;
    private float[] accelerationValues;
    private float[] magneticValues;
    private TextToSpeech tts;
    private boolean isFaceUp;
    private RadioGroup sensorSelector;
    private TextView selectedSensorValue;
    private TextView orientationValue;
    private TextView sensorXLabel;
    private TextView sensorXValue;
    private TextView sensorYLabel;
    private TextView sensorYValue;
    private TextView sensorZLabel;
    private TextView sensorZValue;
    private HashMap<String, String> ttsParams;
    private ToggleButton ttsNotificationsToggleButton;
    private SharedPreferences preferences;
    private boolean ttsNotifications;
    private int selectedSensorId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.determine_orientation);
        
        // Keep the screen on so that changes in orientation can be easily
        // observed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Set up stream to use for Text-To-Speech
        ttsParams = new HashMap<String, String>();
        ttsParams.put(Engine.KEY_PARAM_STREAM, String.valueOf(TTS_STREAM));
        
        // Set the volume control to use the same stream as TTS which allows
        // the user to easily adjust the TTS volume
        this.setVolumeControlStream(TTS_STREAM);
        
        // Get a reference to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        // Initialize references to the UI views that will be updated in the
        // code
        sensorSelector = (RadioGroup) findViewById(R.id.sensorSelector);
        selectedSensorValue = (TextView) findViewById(R.id.selectedSensorValue);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXLabel = (TextView) findViewById(R.id.sensorXLabel);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYLabel = (TextView) findViewById(R.id.sensorYLabel);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZLabel = (TextView) findViewById(R.id.sensorZLabel);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);
        ttsNotificationsToggleButton =
                (ToggleButton) findViewById(R.id.ttsNotificationsToggleButton);
        
        // Retrieve stored preferences
        preferences = getPreferences(MODE_PRIVATE);
        ttsNotifications =
                preferences.getBoolean(TTS_NOTIFICATION_PREFERENCES_KEY, true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        ttsNotificationsToggleButton.setChecked(ttsNotifications);
        updateSelectedSensor();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        
        // Unregister updates from sensors
        sensorManager.unregisterListener(this);
        
        // Shutdown TTS facility
        if (tts != null)
        {
            tts.shutdown();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] rotationMatrix;
        
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_GRAVITY:
                sensorXLabel.setText(R.string.xAxisLabel);
                sensorXValue.setText(String.valueOf(event.values[0]));
                
                sensorYLabel.setText(R.string.yAxisLabel);
                sensorYValue.setText(String.valueOf(event.values[1]));
                
                sensorZLabel.setText(R.string.zAxisLabel);
                sensorZValue.setText(String.valueOf(event.values[2]));
                
                sensorYLabel.setVisibility(View.VISIBLE);
                sensorYValue.setVisibility(View.VISIBLE);
                sensorZLabel.setVisibility(View.VISIBLE);
                sensorZValue.setVisibility(View.VISIBLE);
                
                if (selectedSensorId == R.id.gravitySensor)
                {
                    if (event.values[2] >= GRAVITY_THRESHOLD)
                    {
                        onFaceUp();
                    }
                    else if (event.values[2] <= (GRAVITY_THRESHOLD * -1)) 
                    {
                        onFaceDown();
                    }
                }
                else
                {
                    accelerationValues = event.values.clone();
                    rotationMatrix = generateRotationMatrix();
                    
                    if (rotationMatrix != null)
                    {
                        determineOrientation(rotationMatrix);
                    }
                }
                
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerationValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                
                if (rotationMatrix != null)
                {
                    determineOrientation(rotationMatrix);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                
                if (rotationMatrix != null)
                {
                    determineOrientation(rotationMatrix);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                
                rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix,
                        event.values);
                determineOrientation(rotationMatrix);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        Log.d(TAG,
                String.format("Accuracy for sensor %s = %d",
                sensor.getName(), accuracy));
    }
    
    /**
     * Generates a rotation matrix using the member data stored in
     * accelerationValues and magneticValues.
     * 
     * @return The rotation matrix returned from
     * {@link android.hardware.SensorManager#getRotationMatrix(float[], float[], float[], float[])}
     * or <code>null</code> if either <code>accelerationValues</code> or
     * <code>magneticValues</code> is null.
     */
    private float[] generateRotationMatrix()
    {
        float[] rotationMatrix = null;
        
        if (accelerationValues != null && magneticValues != null)
        {
            rotationMatrix = new float[16];
            boolean rotationMatrixGenerated;
            rotationMatrixGenerated =
                    SensorManager.getRotationMatrix(rotationMatrix,
                    null,
                    accelerationValues,
                    magneticValues);
            
            if (!rotationMatrixGenerated)
            {
                Log.w(TAG, getString(R.string.rotationMatrixGenFailureMessage));

                rotationMatrix = null;
            }
        }
            
        return rotationMatrix;
    }
    
    /**
     * Uses the last read accelerometer and gravity values to determine if the
     * device is face up or face down.
     * 
     * @param rotationMatrix The rotation matrix to use if the orientation 
     * calculation
     */
    private void determineOrientation(float[] rotationMatrix)
    {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);
        
        double azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);
        
        sensorXLabel.setText(R.string.azimuthLabel);
        sensorXValue.setText(String.valueOf(azimuth));
        
        sensorYLabel.setText(R.string.pitchLabel);
        sensorYValue.setText(String.valueOf(pitch));
        
        sensorZLabel.setText(R.string.rollLabel);
        sensorZValue.setText(String.valueOf(roll));
        
        sensorYLabel.setVisibility(View.VISIBLE);
        sensorYValue.setVisibility(View.VISIBLE);
        sensorZLabel.setVisibility(View.VISIBLE);
        sensorZValue.setVisibility(View.VISIBLE);
        
        if (pitch <= 10)
        {   
            if (Math.abs(roll) >= 170)
            {
                onFaceDown();
            }
            else if (Math.abs(roll) <= 10)
            {
                onFaceUp();
            }
        }
    }
    
    /**
     * Handler for device being face up.
     */
    private void onFaceUp()
    {
        if (!isFaceUp)
        {
            if (tts != null && ttsNotificationsToggleButton.isChecked())
            {
                tts.speak(getString(R.string.faceUpText),
                        TextToSpeech.QUEUE_FLUSH,
                        ttsParams);
            }
            
            orientationValue.setText(R.string.faceUpText);
            isFaceUp = true;
        }
    }

    /**
     * Handler for device being face down.
     */
    private void onFaceDown()
    {
        if (isFaceUp)
        {
            if (tts != null && ttsNotificationsToggleButton.isChecked())
            {
                tts.speak(getString(R.string.faceDownText),
                        TextToSpeech.QUEUE_FLUSH,
                        ttsParams);
            }
            
            orientationValue.setText(R.string.faceDownText);
            isFaceUp = false;
        }
    }
    
    /**
     * Updates the views for when the selected sensor is changed
     */
    private void updateSelectedSensor()
    {
        // Clear any current registrations
        sensorManager.unregisterListener(this);
        
        // Determine which radio button is currently selected and enable the
        // appropriate sensors
        selectedSensorId = sensorSelector.getCheckedRadioButtonId(); 
        if (selectedSensorId == R.id.accelerometerMagnetometer)
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    RATE);
            
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    RATE);
        }
        else if (selectedSensorId == R.id.gravityMagnetometer)
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                    RATE);
            
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    RATE);
        }
        else if ((selectedSensorId == R.id.gravitySensor))
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                    RATE);
        }
        else
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    RATE);
        }
        
        // Update the label with the currently selected sensor
        RadioButton selectedSensorRadioButton =
                (RadioButton) findViewById(selectedSensorId);
        selectedSensorValue.setText(selectedSensorRadioButton.getText());
    }
    
    /**
     * Handles click event for the sensor selector.
     * 
     * @param view The view that was clicked
     */
    public void onSensorSelectorClick(View view)
    {
        updateSelectedSensor();
    }
    
    /**
     * Handles click event for the TTS toggle button.
     * 
     * @param view The view for the toggle button
     */
    public void onTtsNotificationsToggleButtonClicked(View view)
    {
        ttsNotifications = ((ToggleButton) view).isChecked();
        preferences.edit()
            .putBoolean(TTS_NOTIFICATION_PREFERENCES_KEY, ttsNotifications)
            .commit();
    }

    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        super.onSuccessfulInit(tts);
        this.tts = tts;
    }

    @Override
    protected void receiveWhatWasHeard(List<String> heard, float[] confidenceScores)
    {
        // no-op
    }
}
