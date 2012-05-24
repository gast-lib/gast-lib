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
package root.gast.playground.speech.activation.util;

import root.gast.nfc.NfcUtil;
import root.gast.nfc.TagWriter;
import root.gast.playground.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * this is an alternate method for writing the speech activation tag
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * @author Pearl Chen &#60;<a
 *         href="mailto:dotdotdotspace@gmail.com">mailto:dotdotdotspace@gmail.com</a>&#62;
 */
public class TagWriterActivity extends Activity
{
    private static final String TAG = "TagWriterActivity";

    private NfcAdapter mNfcAdapter;
    
    private TagWriter writer;

    // TODO: maybe pass in the mime type and message so this can be a utility

    protected void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speechactivation_tagwriter);
        init();
    }

    private void init()
    {
        mNfcAdapter = NfcUtil.getNfcAdapter(TagWriterActivity.this);
        if (mNfcAdapter == null)
        {
            Log.d(TAG, "no nfc adapter");
            return;
        }
        
        writer = new TagWriter(this, mNfcAdapter);

        Button write =
                (Button) findViewById(R.id.btn_speech_write_activation_tag);
        write.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "write a nfc tag");
                // create IntentFilter arrays:
                IntentFilter tagDetected =
                        new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                IntentFilter[] mWriteTagFilters =
                        new IntentFilter[] { tagDetected };
                PendingIntent mNfcPendingIntent =
                        PendingIntent.getActivity(TagWriterActivity.this, 0,
                                new Intent(TagWriterActivity.this,
                                        TagWriterExecutionActivity.class), 0);
                writer.startTagWriting(mWriteTagFilters, mNfcPendingIntent);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // stop the writing process, if was begun previously
        writer.endTagWriting();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }
}
