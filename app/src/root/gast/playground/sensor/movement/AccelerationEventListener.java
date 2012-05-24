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
package root.gast.playground.sensor.movement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

/**
 * Receives accelerometer events and writes them to CSV files and plots them on a graph.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class AccelerationEventListener implements SensorEventListener
{
    private static final String TAG = "AccelerationEventListener";
    private static final char CSV_DELIM = ',';
    private static final int THRESHHOLD = 2;
    private static final String CSV_HEADER =
            "X Axis,Y Axis,Z Axis,Acceleration,Time";
    private static final float ALPHA = 0.8f;
    private static final int HIGH_PASS_MINIMUM = 10;
    private static final int MAX_SERIES_SIZE = 30;
    private static final int CHART_REFRESH = 125;
    
    private PrintWriter printWriter;
    private long startTime;
    private float[] gravity;
    private int highPassCount;
    private SimpleXYSeries xAxisSeries;
    private SimpleXYSeries yAxisSeries;
    private SimpleXYSeries zAxisSeries;
    private SimpleXYSeries accelerationSeries;
    private XYPlot xyPlot;
    private long lastChartRefresh;
    private boolean useHighPassFilter;
    private TextToSpeech tts;
    private HashMap<String, String> ttsParams;
    private String movementText;

    public AccelerationEventListener(XYPlot xyPlot,
                                     boolean useHighPassFilter,
                                     File dataFile,
                                     TextToSpeech tts,
                                     HashMap<String, String> ttsParams,
                                     String movementText)
    {
        this.xyPlot = xyPlot;
        this.useHighPassFilter = useHighPassFilter;
        this.tts = tts;
        this.ttsParams = ttsParams;
        this.movementText = movementText;
        
        xAxisSeries = new SimpleXYSeries("X Axis");
        yAxisSeries = new SimpleXYSeries("Y Axis");
        zAxisSeries = new SimpleXYSeries("Z Axis");
        accelerationSeries = new SimpleXYSeries("Acceleration");
        
        gravity = new float[3];
        startTime = SystemClock.uptimeMillis();
        highPassCount = 0;
        
        try
        {
            printWriter = 
                    new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
            
            printWriter.println(CSV_HEADER);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not open CSV file(s)", e);
        }
        
        if (xyPlot != null)
        {
            xyPlot.addSeries(xAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.RED, null, null));
            xyPlot.addSeries(yAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.GREEN, null, null));
            xyPlot.addSeries(zAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.BLUE, null, null));
            xyPlot.addSeries(accelerationSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.CYAN, null, null));
        }
    }

    private void writeSensorEvent(PrintWriter printWriter,
                                  float x,
                                  float y,
                                  float z,
                                  double acceleration,
                                  long eventTime)
    {
        if (printWriter != null)
        {
            StringBuffer sb = new StringBuffer()
                .append(x).append(CSV_DELIM)
                .append(y).append(CSV_DELIM)
                .append(z).append(CSV_DELIM)
                .append(acceleration).append(CSV_DELIM)
                .append((eventTime / 1000000) - startTime);
            
            printWriter.println(sb.toString());
            if (printWriter.checkError())
            {
                Log.w(TAG, "Error writing sensor event data");
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] values = event.values.clone();
        
        // Pass values through high-pass filter if enabled
        if (useHighPassFilter)
        {
            values = highPass(values[0],
                              values[1],
                              values[2]);
        }
        
        // Ignore data if the high-pass filter is enabled, has not yet received
        // some data to set it
        if (!useHighPassFilter || (++highPassCount >= HIGH_PASS_MINIMUM))
        {
            double sumOfSquares = (values[0] * values[0])
                    + (values[1] * values[1])
                    + (values[2] * values[2]);
            double acceleration = Math.sqrt(sumOfSquares);
            
            // Write to data file
            writeSensorEvent(printWriter,
                             values[0],
                             values[1],
                             values[2],
                             acceleration,
                             event.timestamp);
            
            // If the plot is null, the sensor is not active. Do not plot the
            // data or used the data to determine if the device is moving
            if (xyPlot != null)
            {
                long current = SystemClock.uptimeMillis();
                
                // Limit how much the chart gets updated
                if ((current - lastChartRefresh) >= CHART_REFRESH)
                {
                    long timestamp = (event.timestamp / 1000000) - startTime;
                    
                    // Plot data
                    addDataPoint(xAxisSeries, timestamp, values[0]);
                    addDataPoint(yAxisSeries, timestamp, values[1]);
                    addDataPoint(zAxisSeries, timestamp, values[2]);
                    addDataPoint(accelerationSeries, timestamp, acceleration);
                    
                    xyPlot.redraw();
                    
                    lastChartRefresh = current;
                }
                
                // A "movement" is only triggered of the total acceleration is
                // above a threshold
                if (acceleration > THRESHHOLD)
                {
                    Log.i(TAG, "Movement detected");
                    
                    if (tts != null)
                    {
                        tts.speak(movementText,
                                  TextToSpeech.QUEUE_FLUSH,
                                  ttsParams);
                    }
                }
            }
        }
    }

    private void addDataPoint(SimpleXYSeries series,
                              Number timestamp,
                              Number value)
    {
        if (series.size() == MAX_SERIES_SIZE)
        {
            series.removeFirst();
        }
        
        series.addLast(timestamp, value);
    }
    
    /**
     * This method derived from the Android documentation and is available under
     * the Apache 2.0 license.
     * 
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html
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

    public void stop()
    {
        if (printWriter != null)
        {
            printWriter.close();
        }
        
        if (printWriter.checkError())
        {
            Log.e(TAG, "Error closing writer");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }
}
