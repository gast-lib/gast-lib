package root.gast.playground.speech.activation;

import java.io.IOException;
import java.nio.charset.Charset;

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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Used to create the speech activator.
 * (COPIED NFCInventoryActivity, changed the MIME TYPE)
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechActivatorTagWriter extends Activity {

	private static final String TAG = "SpeechActivatorTagWriter";

	public static final String SPEECH_ACTIVATION_TYPE =
	            "application/root.gast.speech.activation";

	// NFC-related variables
	NfcAdapter mNfcAdapter;
	PendingIntent mNfcPendingIntent;
	IntentFilter[] mReadTagFilters;
	IntentFilter[] mWriteTagFilters;
	private boolean mWriteMode = false;

	// UI elements
	AlertDialog mWriteTagDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// set the main.xml layout of 3 editable textfields and an update
		// button:
		setContentView(R.layout.speechactivation_tagwriter);
		findViewById(R.id.btn_speech_write_activation_tag).setOnClickListener(mTagWriter);

		// get an instance of the context's cached NfcAdapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// if null is returned this demo cannot run. Use this check if the
		// "required" parameter of <uses-feature> in the manifest is not set
		if (mNfcAdapter == null) {
			toast("Your device does not support NFC. Cannot run demo.");
			finish();
			return;
		}

		// check if NFC is enabled
		checkNfcEnabled();

		// Handle foreground NFC scanning in this activity by creating a
		// PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Create intent filter to handle NDEF NFC tags detected from inside our
		// application when in "read mode":
		IntentFilter ndefDetected = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType(SPEECH_ACTIVATION_TYPE);
		} catch (MalformedMimeTypeException e) {
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
	protected void onResume() {
		super.onResume();

		// doulble check if NFC is enabled
		checkNfcEnabled();

		// tag received from outside application:
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
			NdefRecord record = msgs[0].getRecords()[0];
			byte[] payload = record.getPayload();
			//get the type:
			setTextFieldValues(new String(payload));
			setIntent(new Intent()); // Consume this intent.
		}

		// Enable priority for current activity to detect scanned tags
		// enableForegroundDispatch( activity, pendingIntent,
		// intentsFiltersArray, techListsArray );
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mReadTagFilters, null);

	}

	/* Called when the system is about to start resuming a previous activity. */
	@Override
	protected void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundNdefPush(this);
	}

	/*
	 * This is called for activities that set launchMode to "singleTop" in their
	 * package, or if a client used the FLAG_ACTIVITY_SINGLE_TOP flag when
	 * calling startActivity(Intent).
	 */
	@Override
	protected void onNewIntent(Intent intent) {

		if (!mWriteMode) {
			// Currently in tag READING mode
			if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
				NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
				confirmDisplayedContentOverwrite(msgs[0]);
			} else if (intent.getAction().equals(
					NfcAdapter.ACTION_TAG_DISCOVERED)) {
				toast("This NFC tag currently has no inventory NDEF data.");
			}
		} else {
			// Currently in tag WRITING mode
			if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
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
	NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
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
			finish();
		}
		return msgs;
	}

	private void confirmDisplayedContentOverwrite(final NdefMessage msg) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.new_tag_found))
				.setMessage(getString(R.string.replace_current_tag))
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// use the current values in the NDEF payload
								// to update the text fields
								String payload = new String(msg.getRecords()[0]
										.getPayload());
								setTextFieldValues(payload);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).show();
	}

	private void setTextFieldValues(String jsonString) {
	    //No need to set any values
	}

	/*
	 * **** WRITING MODE METHODS ****
	 */

	private View.OnClickListener mTagWriter = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {

			enableTagWriteMode();

			AlertDialog.Builder builder = new AlertDialog.Builder(
					SpeechActivatorTagWriter.this)
					.setTitle(getString(R.string.ready_to_write))
					.setMessage(getString(R.string.ready_to_write_instructions_generic))
					.setCancelable(true)
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									disableTagWriteMode();
									dialog.cancel();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									disableTagWriteMode();
								}
							});
			mWriteTagDialog = builder.create();
			mWriteTagDialog.show();
		}
	};

    private NdefMessage createNdefFromJson()
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

	private void enableTagWriteMode() {
		mWriteMode = true;
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
		mWriteMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}

	boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					toast("Tag is read-only.");
					return false;
				}
				if (ndef.getMaxSize() < size) {
					toast("Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.");
					return false;
				}

				ndef.writeNdefMessage(message);
				toast("Wrote message to pre-formatted tag.");
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						toast("Formatted tag and wrote message");
						return true;
					} catch (IOException e) {
						toast("Failed to format tag.");
						return false;
					}
				} else {
					toast("Tag doesn't support NDEF.");
					return false;
				}
			}
		} catch (Exception e) {
			toast("Failed to write tag");
		}

		return false;
	}

	/*
	 * **** HELPER METHODS ****
	 */

	private void checkNfcEnabled() {
		Boolean nfcEnabled = mNfcAdapter.isEnabled();
		if (!nfcEnabled) {
			new AlertDialog.Builder(SpeechActivatorTagWriter.this)
					.setTitle(getString(R.string.warning_nfc_is_off))
					.setMessage(getString(R.string.turn_on_nfc))
					.setCancelable(false)
					.setPositiveButton("Update Settings",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startActivity(new Intent(
											android.provider.Settings.ACTION_WIRELESS_SETTINGS));
								}
							}).create().show();
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
}