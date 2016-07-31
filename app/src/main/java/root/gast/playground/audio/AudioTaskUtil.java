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
package root.gast.playground.audio;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.widget.TextView;

/**
 * A small helper class to help the various classes in this package
 * update the UI
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class AudioTaskUtil
{
    private static DateFormat WHEN_FORMATTER = new SimpleDateFormat("hh:mm:ss");

    public static String getNow()
    {
        Calendar now = Calendar.getInstance();
        String when = WHEN_FORMATTER.format(now.getTime());
        return when;
    }
    
    public static void appendToStartOfLog(TextView log, String appendThis)
    {
        String currentLog = log.getText().toString();
        currentLog = appendThis + "\n" + currentLog;
        log.setText(currentLog);
    }

}
