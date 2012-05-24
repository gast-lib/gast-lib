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
package root.gast.playground.sensor.aoa;

import java.text.DecimalFormat;

import root.gast.playground.R;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

/**
 * TODO
 * 
 * @author David Hutchison &#60;<a href="mailto:david.n.hutch@gmail.com">david.n.hutch@gmail.com</a>&#62;
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class AccessoryController
{
    private static final DecimalFormat TEMP_FORMATTER =
            new DecimalFormat("Temperature: " + "### " + (char) 0x00B0 + "C");
    
    protected BaseActivity mHostActivity;
    private TextView mTemperature;

    /**
     * TODO
     * 
     * @param activity
     */
    public AccessoryController(BaseActivity activity)
    {
        mHostActivity = activity;
        mTemperature = (TextView) findViewById(R.id.tempLabel);
    }

    /**
     * TODO
     * 
     * @param id
     * @return
     */
    protected View findViewById(int id)
    {
        return mHostActivity.findViewById(id);
    }

    /**
     * TODO
     * 
     * @return
     */
    protected Resources getResources()
    {
        return mHostActivity.getResources();
    }

    /**
     * TODO
     * @deprecated empty method
     */
    protected void onAccessoryAttached()
    {
        // FIXME: Remove empty method
    }

    /**
     * TODO
     * 
     * @param temperatureFromArduino
     */
    public void setTemperature(int temperatureFromArduino)
    {
        double voltagemv = temperatureFromArduino * 4.9;
        double kVoltageAtZeroCmv = 400;
        double kTemperatureCoefficientmvperC = 19.5;
        double temperatureC = ((double) voltagemv - kVoltageAtZeroCmv)
                / kTemperatureCoefficientmvperC;
        mTemperature.setText(TEMP_FORMATTER.format(temperatureC));
    }
}

