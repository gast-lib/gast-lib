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

import root.gast.location.LocationBroadcastReceiver;
import root.gast.playground.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver that will display a notification for a proximity alert.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class ProximityAlertBroadcastReceiver extends LocationBroadcastReceiver
{
    private static final int NOTIFICATION_ID = 9999;
    
    @Override
    public void onEnteringProximity(Context context)
    {
        displayNotification(context, "Entering Proximity");
    }

    @Override
    public void onExitingProximity(Context context)
    {
    	displayNotification(context, "Exiting Proximity");
    }

    private void displayNotification(Context context, String message)
    {
    	NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
        
        Notification notification = new Notification(R.drawable.icon, message, System.currentTimeMillis());
        notification.setLatestEventInfo(context, "GAST", "Proximity Alert", pi);
        
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
