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
package root.gast.nfc;

import java.io.IOException;

import root.gast.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * @author Pearl Chen &#60;<a
 *         href="mailto:dotdotdotspace@gmail.com">mailto:dotdotdotspace@gmail.com</a>&#62;
 */
public class NfcUtil
{
    private static final String TAG = "NfcUtil";
    

    /**
     * retrieves an {@link NfcAdapter} if it is available,
     * sends the user to the configuration screen if it is not enabled
     * returns null if this device doesn't support nfc.
     */
    public static NfcAdapter getNfcAdapter(Activity context)
    {
        // get an instance of the context's cached NfcAdapter
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(context);

        // check if NFC is enabled
        checkNfcEnabled(context, mNfcAdapter);

        return mNfcAdapter;
    }

    private static void checkNfcEnabled(final Activity context, NfcAdapter mNfcAdapter)
    {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled)
        {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.warning_nfc_is_off))
                    .setMessage(context.getString(R.string.turn_on_nfc))
                    .setCancelable(false)
                    .setPositiveButton("Update Settings",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                        int id)
                                {
                                    context.startActivity(new Intent(
                                            android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).create().show();
        }
    }
    
    public static NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                        || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                Parcelable[] rawMsgs = intent
                                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMsgs != null) {
                        msgs = new NdefMessage[rawMsgs.length];
                        for (int i = 0; i < rawMsgs.length; i++) {
                                msgs[i] = (NdefMessage) rawMsgs[i];
                        }
                } else {
                        // Unknown tag type
                        byte[] empty = new byte[] {};
                        NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                                        empty, empty, empty);
                        NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                        msgs = new NdefMessage[] { msg };
                }
        } else {
                Log.d(TAG, "Unknown intent.");
                return null;
        }
        return msgs;
}

    /**
     * write a message to a tag, handling various error conditions by returning
     * false. If context != null, it makes Toast with the result
     */
    public static boolean writeTag(Context context, NdefMessage message, Tag tag)
    {
        int size = message.toByteArray().length;

        try
        {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null)
            {
                ndef.connect();

                if (!ndef.isWritable())
                {
                    toast(context, "Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size)
                {
                    toast(context, "Tag capacity is " + ndef.getMaxSize()
                            + " bytes, message is " + size + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast(context, "Wrote message to pre-formatted tag.");
                return true;
            }
            else
            {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null)
                {
                    try
                    {
                        format.connect();
                        format.format(message);
                        toast(context, "Formatted tag and wrote message");
                        return true;
                    } catch (IOException e)
                    {
                        toast(context, "Failed to format tag.");
                        return false;
                    }
                }
                else
                {
                    toast(context, "Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e)
        {
            toast(context, "Failed to write tag");
        }

        return false;
    }
    
    private static void toast(Context context, String text)
    {
        if (context != null)
        {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }


}
