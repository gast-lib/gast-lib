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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * Overlay that will draw locations on a map with lines between each location.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class TrackLocationOverlay extends ItemizedOverlay<OverlayItem>
{
    private List<PointOverlayItem> pointOverlayList =
            new ArrayList<PointOverlayItem>();
    private Paint trackingPaint;
    private Paint strokePaint;
    private Paint fillPaint;
    private MapView mapView;

    public TrackLocationOverlay(Drawable defaultMarker, MapView mapView)
    {
        super(boundCenterBottom(defaultMarker));
        
        trackingPaint = new Paint();
        trackingPaint.setColor(Color.RED);
        trackingPaint.setStrokeWidth(7);
        
        strokePaint = new Paint();
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStrokeWidth(2);
        strokePaint.setStyle(Paint.Style.STROKE);
        
        fillPaint = new Paint();
        fillPaint.setColor(Color.BLUE);
        fillPaint.setStyle(Style.FILL);
        fillPaint.setAlpha(32);
        
        this.mapView = mapView;
    }

    @Override
    protected OverlayItem createItem(int i)
    {
        return pointOverlayList.get(i);
    }

    @Override
    public int size()
    {
        return pointOverlayList.size();
    }
    
    public void addPoint(double latitude, double longitude, float accuracy)
    {
        pointOverlayList.add(new PointOverlayItem(latitude,
                longitude, accuracy));
        populate();
        
        mapView.invalidate();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        super.draw(canvas, mapView, shadow);
        
        // If list is empty, then there is nothing to draw
        if (!pointOverlayList.isEmpty())
        {
            PointOverlayItem previous = null;
            
            for (PointOverlayItem pointOverlayItem : pointOverlayList)
            {
                if (previous != null)
                {
                    Projection projection = mapView.getProjection();
                    
                    android.graphics.Point previousPoint =
                            projection.toPixels(previous.getPoint(), null);
                    
                    android.graphics.Point currentPoint =
                            projection.toPixels(pointOverlayItem.getPoint(), null);
                    
                    canvas.drawLine(previousPoint.x,
                                    previousPoint.y,
                                    currentPoint.x,
                                    currentPoint.y,
                                    trackingPaint);
                }
                
                previous = pointOverlayItem;
            }
            
            PointOverlayItem last =
                    pointOverlayList.get(pointOverlayList.size() - 1);
            android.graphics.Point lastPoint =
                    mapView.getProjection().toPixels(last.getPoint(), null);
            
            // Draw circle(s) for accuracy. The inner circle will be translucent
            // so it does not cover up the point marker.
            canvas.drawCircle(lastPoint.x,
                              lastPoint.y,
                              last.getAccuracy(),
                              strokePaint);
            
            canvas.drawCircle(lastPoint.x,
                              lastPoint.y,
                              last.getAccuracy(),
                              fillPaint);
        }
    }
}
