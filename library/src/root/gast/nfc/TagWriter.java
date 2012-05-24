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
package root.gast.nfc;

import root.gast.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

/**
 * shows a dialog while writing and also managed foreground dispatch
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * @author Pearl Chen &#60;<a
 *         href="mailto:dotdotdotspace@gmail.com">mailto:dotdotdotspace@gmail.com</a>&#62;
 */
public class TagWriter
{
    private static final String TAG = "TagWriter";
    
    private Activity context;

    private AlertDialog mWriteTagDialog;

    private NfcAdapter mNfcAdapter;
    
    public TagWriter(Activity context, NfcAdapter mNfcAdapter)
    {
        this.context = context;
        this.mNfcAdapter = mNfcAdapter;
    }

    public void startTagWriting(
            IntentFilter[] writeTagFilters, PendingIntent pendingIntent)
    {
        enableTagWriteMode(writeTagFilters, pendingIntent);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.ready_to_write))
                        .setMessage(
                                context.getString(R.string.ready_to_write_instructions))
                        .setCancelable(true)
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog,
                                            int id)
                                    {
                                        disableTagWriteMode();
                                        dialog.cancel();
                                    }
                                })
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener()
                                {
                                    @Override
                                    public void
                                            onCancel(DialogInterface dialog)
                                    {
                                        disableTagWriteMode();
                                    }
                                });
        mWriteTagDialog = builder.create();
        mWriteTagDialog.show();
    }
    
    public void endTagWriting()
    {
        if (mWriteTagDialog != null)
        {
            mWriteTagDialog.cancel();
        }
        disableTagWriteMode();
    }

    private void enableTagWriteMode(IntentFilter[] writeTagFilters,
            PendingIntent pendingIntent)
    {
        Log.d(TAG, "enable write mode");
        mNfcAdapter.enableForegroundDispatch(context, pendingIntent,
                writeTagFilters, null);
    }

    private void disableTagWriteMode()
    {
        Log.d(TAG, "disable write mode");
        if (mNfcAdapter != null)
        {
            mNfcAdapter.disableForegroundDispatch(context);
        }
    }

}
