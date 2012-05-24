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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Overlay item that is used to draw point in a Google map.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class PointOverlayItem extends OverlayItem
{
    private float accuracy;
    
    public PointOverlayItem(double latitude, double longitude, float accuracy)
    { 
        super(createGeoPoint(latitude, longitude),
              String.format("(%f, %f)", latitude, longitude),
              "");
        this.accuracy = accuracy;
    }
 
    private static GeoPoint createGeoPoint(double latitude, double longitude)
    {
        int e6Latitude = (int) (latitude * 1E6);
        int e6Longitude = (int) (longitude * 1E6);
        
        return new GeoPoint(e6Latitude, e6Longitude);
    }

    public float getAccuracy()
    {
        return accuracy;
    }
}
