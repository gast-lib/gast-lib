package root.gast.playground.speech.tts;

import java.util.HashMap;
import java.util.Locale;

import root.gast.R;
import root.gast.speech.tts.TextToSpeechInitializer;
import root.gast.speech.tts.TextToSpeechStartupListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * A simple app to demonstrate a barebones usecase for playing a simple text to
 * speech sound
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class TextToSpeechDemo extends Activity implements
        TextToSpeechStartupListener
{
    private static final String TAG = "TextToSpeechDemo";
    private Button speak;
    private Button stopSpeak;

    private static final String LAST_SPOKEN = "lastSpoken";

    private TextToSpeechInitializer ttsInit;
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ttsdemo);
        hookButtons();
        init();
    }

    private void hookButtons()
    {
        speak = (Button) findViewById(R.id.btn_speak);
        speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setViewToWhileSpeaking();
                playScript();
            }
        });

        stopSpeak = (Button) findViewById(R.id.btn_stop_speak);
        stopSpeak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setViewToDoneSpeaking();
                tts.stop();
            }
        });
    }

    private void init()
    {
        deactivateUi();
        ttsInit = new TextToSpeechInitializer(this, Locale.getDefault(), this);
    }

    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        Log.d(TAG, "successful init");
        this.tts = tts;
        activateUi();
        setTtsListener();
    }

    /**
     * set the TTS listener to call {@link #onDone(String)} depending on the
     * Build.Version.SDK_INT
     */
    private void setTtsListener()
    {
        if (Build.VERSION.SDK_INT >= 15)
        {
            int listenerResult =
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
                    {
                        @Override
                        public void onDone(String utteranceId)
                        {
                            TextToSpeechDemo.this.onDone(utteranceId);
                        }

                        @Override
                        public void onError(String utteranceId)
                        {
                            Log.e(TAG, "TTS error");
                        }

                        @Override
                        public void onStart(String utteranceId)
                        {
                            Log.d(TAG, "TTS start");
                        }
                    });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        }
        else
        {
            int listenerResult =
                    tts.setOnUtteranceCompletedListener(
                            new OnUtteranceCompletedListener()
                    {
                        @Override
                        public void onUtteranceCompleted(String utteranceId)
                        {
                            TextToSpeechDemo.this.onDone(utteranceId);
                        }
                    });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "failed to add utterance completed listener");
            }
        }
    }

    private void onDone(final String utteranceId)
    {
        Log.d(TAG, "utterance completed: " + utteranceId);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (utteranceId.equals(LAST_SPOKEN))
                {
                    setViewToDoneSpeaking();
                }
            }
        });
    }

    @Override
    public void onFailedToInit()
    {
        DialogInterface.OnClickListener onClickOk = makeOnFailedToInitHandler();
        AlertDialog a =
                new AlertDialog.Builder(this).setTitle("Error")
                        .setMessage("Unable to create text to speech")
                        .setNeutralButton("Ok", onClickOk).create();
        a.show();
    }

    @Override
    public void onRequireLanguageData()
    {
        DialogInterface.OnClickListener onClickOk =
                makeOnClickInstallDialogListener();
        DialogInterface.OnClickListener onClickCancel =
                makeOnFailedToInitHandler();
        AlertDialog a =
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(
                                "Requires Language data to proceed, " +
                                "would you like to install?")
                        .setPositiveButton("Ok", onClickOk)
                        .setNegativeButton("Cancel", onClickCancel).create();
        a.show();
    }

    @Override
    public void onWaitingForLanguageData()
    {
        // either wait for install
        DialogInterface.OnClickListener onClickWait =
                makeOnFailedToInitHandler();
        DialogInterface.OnClickListener onClickInstall =
                makeOnClickInstallDialogListener();

        AlertDialog a =
                new AlertDialog.Builder(this)
                        .setTitle("Info")
                        .setMessage(
                                "Please wait for the language data to finish" +
                                " installing and try again.")
                        .setNegativeButton("Wait", onClickWait)
                        .setPositiveButton("Retry", onClickInstall).create();
        a.show();
    }

    private DialogInterface.OnClickListener makeOnClickInstallDialogListener()
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ttsInit.installLanguageData();
            }
        };
    }

    private DialogInterface.OnClickListener makeOnFailedToInitHandler()
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        };
    }

    private void playScript()
    {
        Log.d(TAG, "started script");
        // setup

        // id to send back when saying the last phrase
        // so the app can re-enable the "speak" button
        HashMap<String, String> lastSpokenWord = new HashMap<String, String>();
        lastSpokenWord.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                LAST_SPOKEN);

        // add earcon
        final String EARCON_NAME = "[tone]";
        tts.addEarcon(EARCON_NAME, "root.gast.playground", R.raw.tone);

        // add prerecorded speech
        final String CLOSING = "[Thank you]";
        tts.addSpeech(CLOSING, "root.gast.playground",
                R.raw.enjoytestapplication);

        // pass in null to most of these because we do not want a callback to
        // onDone
        tts.playEarcon(EARCON_NAME, TextToSpeech.QUEUE_ADD, null);
        tts.playSilence(1000, TextToSpeech.QUEUE_ADD, null);
        tts.speak("Attention readers: Use the try button to experiment with"
                + " Text to Speech. Use the diagnostics button to see "
                + "detailed Text to Speech engine information.",
                TextToSpeech.QUEUE_ADD, null);
        tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
        tts.speak(CLOSING, TextToSpeech.QUEUE_ADD, lastSpokenWord);

    }

    // activate and deactivate the UI based on various states

    private void deactivateUi()
    {
        Log.d(TAG, "deactivate ui");
        // don't enable until the initialization is complete
        speak.setEnabled(false);
    }

    private void activateUi()
    {
        Log.d(TAG, "activate ui");
        speak.setEnabled(true);
    }

    public void setViewToWhileSpeaking()
    {
        stopSpeak.setVisibility(View.VISIBLE);
        speak.setVisibility(View.GONE);
    }

    public void setViewToDoneSpeaking()
    {
        stopSpeak.setVisibility(View.GONE);
        speak.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        if (tts != null)
        {
            tts.shutdown();
        }
        super.onDestroy();
    }

}