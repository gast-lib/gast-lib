package root.gast.playground.nfc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import root.gast.playground.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Peer2PeerNFCInventoryActivity extends Activity
{

    private static final String TAG = "nfcinventory_peer";

    // NFC-related variables
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadTagFilters;
    IntentFilter[] mWriteTagFilters;
    private boolean mWriteMode = false;

    // UI elements
    EditText mName;
    EditText mRAM;
    EditText mProcessor;
    AlertDialog mWriteTagDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        // set the main.xml layout of 3 editable textfields and an update
        // button:
        setContentView(R.layout.nfc_peer);
        findViewById(R.id.write_tag).setOnClickListener(mTagWriter);
        mName = ((EditText) findViewById(R.id.computer_name));
        mRAM = ((EditText) findViewById(R.id.computer_ram));
        mProcessor = ((EditText) findViewById(R.id.computer_processor));

        // add change listeners to the text field so the available peer-to-peer
        // data is kept up to date
        mName.addTextChangedListener(mTextWatcher);
        mRAM.addTextChangedListener(mTextWatcher);
        mProcessor.addTextChangedListener(mTextWatcher);

        // get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // if null is returned this demo cannot run. Use this check if the
        // "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null)
        {
            Toast.makeText(this,
                    "Your device does not support NFC. Cannot run demo.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // check if NFC is enabled
        checkNfcEnabled();

        // Handle foreground NFC scanning in this activity by creating a
        // PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
        // is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Create intent filter to handle NDEF NFC tags detected from inside our
        // application when in "read mode":
        IntentFilter ndefDetected = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        try
        {
            ndefDetected.addDataType("application/root.gast.playground.nfc");
        } catch (MalformedMimeTypeException e)
        {
            throw new RuntimeException("Could not add MIME type.", e);
        }

        // Create intent filter to detect any NFC tag when attempting to write
        // to a tag in "write mode"
        IntentFilter tagDetected = new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED);

        // create IntentFilter arrays:
        mWriteTagFilters = new IntentFilter[] { tagDetected };
        mReadTagFilters = new IntentFilter[] { ndefDetected, tagDetected };
    }

    /* Called when the activity will start interacting with the user. */
    @Override
    protected void onResume()
    {
        super.onResume();

        // Double check if NFC is enabled
        checkNfcEnabled();

        Log.d(TAG, "onResume: " + getIntent());

        if (getIntent().getAction() != null)
        {
            // tag received when app is not running and not in the foreground:
            if (getIntent().getAction().equals(
                    NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();
                setTextFieldValues(new String(payload));
            }
        }

        // Enable priority for current activity to detect scanned tags
        // enableForegroundDispatch( activity, pendingIntent,
        // intentsFiltersArray, techListsArray );
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadTagFilters, null);

        // Enable peer-to-peer push of NDEF data
        mNfcAdapter.enableForegroundNdefPush(
                Peer2PeerNFCInventoryActivity.this, createNdefFromJson());

    }

    /* Called when the system is about to start resuming a previous activity. */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause: " + getIntent());
        mNfcAdapter.disableForegroundDispatch(this);
        mNfcAdapter.disableForegroundNdefPush(this);
    }

    /*
     * This is called for activities that set launchMode to "singleTop" or
     * "singleTask" in their manifest package, or if a client used the
     * FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
     */
    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent: " + intent);

        if (!mWriteMode)
        {
            // Currently in tag READING mode
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
                confirmDisplayedContentOverwrite(msgs[0]);
            } else if (intent.getAction().equals(
                    NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                Toast.makeText(this,
                        "This NFC tag currently has no inventory NDEF data.",
                        Toast.LENGTH_LONG).show();
            }
        } else
        {
            // Currently in tag WRITING mode
            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                Tag detectedTag = intent
                        .getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeTag(createNdefFromJson(), detectedTag);
                mWriteTagDialog.cancel();
            }
        }
    }

    /*
     * **** READING MODE METHODS ****
     */
    NdefMessage[] getNdefMessagesFromIntent(Intent intent)
    {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else
            {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
        } else
        {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private void confirmDisplayedContentOverwrite(final NdefMessage msg)
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_tag_found))
                .setMessage(getString(R.string.replace_current_tag))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // use the current values in the NDEF payload
                        // to update the text fields
                        String payload = new String(msg.getRecords()[0]
                                .getPayload());
                        setTextFieldValues(payload);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                }).show();
    }

    private void setTextFieldValues(String jsonString)
    {

        JSONObject inventory = null;
        String name = "";
        String ram = "";
        String processor = "";
        try
        {
            inventory = new JSONObject(jsonString);
            name = inventory.getString("name");
            ram = inventory.getString("ram");
            processor = inventory.getString("processor");
        } catch (JSONException e)
        {
            Log.e(TAG, "Couldn't parse JSON: ", e);
        }

        Editable nameField = mName.getText();
        nameField.clear();
        nameField.append(name);

        Editable ramField = mRAM.getText();
        ramField.clear();
        ramField.append(ram);

        Editable processorField = mProcessor.getText();
        processorField.clear();
        processorField.append(processor);

    }

    private TextWatcher mTextWatcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3)
        {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3)
        {

        }

        @Override
        public void afterTextChanged(Editable arg0)
        {
            // if (mResumed)
            // {
            mNfcAdapter.enableForegroundNdefPush(
                    Peer2PeerNFCInventoryActivity.this, createNdefFromJson());
            // }
        }
    };

    /*
     * **** WRITING MODE METHODS ****
     */

    private View.OnClickListener mTagWriter = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {

            enableTagWriteMode();

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    Peer2PeerNFCInventoryActivity.this)
                    .setTitle(getString(R.string.ready_to_write))
                    .setMessage(getString(R.string.ready_to_write_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                        int id)
                                {
                                    dialog.cancel();
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            enableTagReadMode();
                        }
                    });
            mWriteTagDialog = builder.create();
            mWriteTagDialog.show();
        }
    };

    private NdefMessage createNdefFromJson()
    {

        // get the values from the form's text fields:
        Editable nameField = mName.getText();
        Editable ramField = mRAM.getText();
        Editable processorField = mProcessor.getText();

        // create a JSON object out of the values:
        JSONObject computerSpecs = new JSONObject();
        try
        {
            computerSpecs.put("name", nameField);
            computerSpecs.put("ram", ramField);
            computerSpecs.put("processor", processorField);
        } catch (JSONException e)
        {
            Log.e(TAG, "Could not create JSON: ", e);
        }

        // create a new NDEF record and containing NDEF message using the app's
        // custom MIME type:
        String mimeType = "application/root.gast.playground.nfc";
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        String data = computerSpecs.toString();
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];
        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeBytes, id, dataBytes);
        NdefMessage m = new NdefMessage(new NdefRecord[] { record });

        // return the NDEF message
        return m;
    }

    private void enableTagWriteMode()
    {
        mWriteMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mWriteTagFilters, null);
    }

    private void enableTagReadMode()
    {
        mWriteMode = false;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadTagFilters, null);
    }

    boolean writeTag(NdefMessage message, Tag tag)
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
                    Toast.makeText(this,
                            "Cannot write to this tag. This tag is read-only.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                if (ndef.getMaxSize() < size)
                {
                    Toast.makeText(
                            this,
                            "Cannot write to this tag. Message size (" + size
                                    + " bytes) exceeds this tag's capacity of "
                                    + ndef.getMaxSize() + " bytes.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                ndef.writeNdefMessage(message);
                Toast.makeText(this,
                        "A pre-formatted tag was successfully updated.",
                        Toast.LENGTH_LONG).show();
                return true;
            } else
            {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null)
                {
                    try
                    {
                        format.connect();
                        format.format(message);
                        Toast.makeText(
                                this,
                                "This tag was successfully formatted and updated.",
                                Toast.LENGTH_LONG).show();
                        return true;
                    } catch (IOException e)
                    {
                        Toast.makeText(
                                this,
                                "Cannot write to this tag due to I/O Exception.",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else
                {
                    Toast.makeText(
                            this,
                            "Cannot write to this tag. This tag does not support NDEF.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        } catch (Exception e)
        {
            Toast.makeText(this,
                    "Cannot write to this tag due to an Exception.",
                    Toast.LENGTH_LONG).show();
        }

        return false;
    }

    /*
     * **** HELPER METHODS ****
     */

    private void checkNfcEnabled()
    {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled)
        {
            new AlertDialog.Builder(Peer2PeerNFCInventoryActivity.this)
                    .setTitle(getString(R.string.warning_nfc_is_off))
                    .setMessage(getString(R.string.turn_on_nfc))
                    .setCancelable(false)
                    .setPositiveButton("Update Settings",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                        int id)
                                {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).create().show();
        }
    }

}