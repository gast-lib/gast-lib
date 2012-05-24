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
package root.gast.playground.speech;

import java.util.List;

import root.gast.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * For displaying the results of the Speech pending intent
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechRecognitionResultsActivity extends Activity
{
    private static final String TAG = "SpeechRecognitionResultsActivity";

    /**
     * for passing in the input
     */
    public static String WHAT_YOU_ARE_TRYING_TO_SAY_INTENT_INPUT =
            "WHAT_YOU_ARE_TRYING_TO_SAY_INPUT";

    private ListView log;

    private TextView resultsSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speechrecognition_result);
        Log.d(TAG, "SpeechRecognition Pending intent received");
        hookButtons();
        init();
    }

    private void hookButtons()
    {
        log = (ListView) findViewById(R.id.lv_resultlog);
        resultsSummary = (TextView) findViewById(R.id.tv_speechResultsSummary);
    }

    private void init()
    {
        if (getIntent() != null)
        {
            if (getIntent().hasExtra(WHAT_YOU_ARE_TRYING_TO_SAY_INTENT_INPUT))
            {
                String whatSayFromIntent =
                        getIntent().getStringExtra(
                                WHAT_YOU_ARE_TRYING_TO_SAY_INTENT_INPUT);
                resultsSummary.setText(whatSayFromIntent);
            }

            String whatSayFromIntent =
                    getIntent().getStringExtra(
                            WHAT_YOU_ARE_TRYING_TO_SAY_INTENT_INPUT);
            resultsSummary.setText(whatSayFromIntent);

            if (getIntent().hasExtra(RecognizerIntent.EXTRA_RESULTS))
            {
                List<String> results =
                        getIntent().getStringArrayListExtra(
                                RecognizerIntent.EXTRA_RESULTS);
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(this,
                                R.layout.speechresultactivity_listitem,
                                R.id.tv_speech_activity_result, results);
                log.setAdapter(adapter);
            }
            else
            {
                // if RESULT_EXTRA is not present, the recognition had an
                // error
                DialogInterface.OnClickListener onClickFinish =
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which)
                            {
                                finish();
                            }
                        };
                AlertDialog a =
                        new AlertDialog.Builder(this)
                                .setTitle(
                                        getResources().getString(
                                                R.string.d_info))
                                .setMessage(
                                        getResources()
                                                .getString(
                                                 R.string.
                                                 speechRecognitionFailed))
                                .setPositiveButton(
                                        getResources().getString(R.string.d_ok),
                                        onClickFinish).create();
                a.show();
            }
        }
    }
}
