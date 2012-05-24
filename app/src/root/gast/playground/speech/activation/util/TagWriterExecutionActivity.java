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

import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import root.gast.nfc.NfcUtil;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

/**
 * performs the writing of the tag
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class TagWriterExecutionActivity extends Activity
{
    private static final String TAG = "TagWriterExecutionActivity";

    public static final String SPEECH_ACTIVATION_TYPE =
            "application/root.gast.speech.activation";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "launched tag writer");
        super.onCreate(savedInstanceState);
        doWriting();
    }

    private void doWriting()
    {
        Intent intent = getIntent();
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
        {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcUtil.writeTag(this, createMimeTypeMessage(), detectedTag);
        }
        finish();
    }

    /**
     * create a new NDEF record and containing NDEF message using the app's
     * custom MIME type but that is otherwise empty.
     * 
     * @return
     */
    private NdefMessage createMimeTypeMessage()
    {
        String mimeType = SPEECH_ACTIVATION_TYPE;
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];
        byte[] data = new byte[0];
        NdefRecord record =
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, data);
        NdefMessage m = new NdefMessage(new NdefRecord[] { record });
        return m;
    }

    private NdefMessage createNdefWithJson()
    {

        // create a JSON object out of the values:
        JSONObject activationSpecs = new JSONObject();
        try
        {
            activationSpecs.put("target", "placeholder");
        } catch (JSONException e)
        {
            Log.d(TAG, "Could not create JSON");
        }

        // create a new NDEF record and containing NDEF message using the app's
        // custom MIME type:
        String mimeType = SPEECH_ACTIVATION_TYPE;
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        String data = activationSpecs.toString();
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];
        NdefRecord record =
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id,
                        dataBytes);
        NdefMessage m = new NdefMessage(new NdefRecord[] { record });

        // return the NDEF message
        return m;
    }

}
