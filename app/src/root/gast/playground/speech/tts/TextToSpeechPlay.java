package root.gast.playground.speech.tts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import root.gast.R;
import root.gast.playground.pref.PreferenceHelper;
import root.gast.playground.pref.SummarizingEditPreferences;
import root.gast.playground.util.DialogGenerator;
import root.gast.playground.util.FileUtil;
import root.gast.speech.tts.CommonTtsMethods;
import root.gast.speech.tts.TextToSpeechInitializer;
import root.gast.speech.tts.TextToSpeechStartupListener;
import root.gast.speech.tts.TextToSpeechUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Demonstrates executing tts under various parameter settings
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class TextToSpeechPlay extends Activity implements TextToSpeechStartupListener
{
    private static final String TAG = "TextToSpeechPlay";

    private static final String SILENCE_ROOT = "silence_";
    private static final String SPEAK_ROOT = "speak_";
    private static final String EARCON_ROOT = "earcon_";

    private static final String OUTPUT_DIR = "textoutput";

    private static String EXTERNAL_STORAGE_DIR;
    
    /**
     * how many characters to output in the log when
     * outputting the text spoken
     */
    private static final int CHARACTERS_IN_LOG = 30;
    
    private TextView log;
    private EditText whatToSay;
    private Button speak;
    private Button silence;
    private Button earcon;
    private Button stopSpeak;
    
    private PreferenceHelper preferences;
    private List<String> presets;
    
    private TextToSpeechInitializer ttsInit;
    private TextToSpeech tts;
    
    private int queueMode;

    /**
     * keep track of the number of the spoken utterance
     */
    private int utteranceCounter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texttospeech);
        
        hookButtons();
        
        init();
    }
    
    private void init()
    {
        try
        {
            EXTERNAL_STORAGE_DIR = getExternalFilesDir(OUTPUT_DIR).getAbsolutePath();
        } catch (Exception e)
        {
            Log.e(TAG, "the external storate isn't available, continue anyway",
                    e);
        }
        preferences =
                new PreferenceHelper(getResources().getString(
                        R.string.pref_tts_key), this.getApplicationContext());
        deactivateUi();
        ttsInit =
                new TextToSpeechInitializer(this, Locale.getDefault(), this);
        utteranceCounter = 0;
        queueMode = TextToSpeech.QUEUE_FLUSH;
    }

    @Override
    public void onFailedToInit()
    {
        DialogGenerator.createInfoDialog(this, getResources().getString(R.string.d_error), 
                getResources().getString(R.string.tts_init_failed), makeOnFailedToInitHandler()).show();
    }
    
    
    /**
     * @see root.gast.speech.tts.TtsStartupListener#onRequireLanguageData()
     */
    @Override
    public void onRequireLanguageData()
    {
        Log.d(TAG, "REQUIRE LANGUAGE DATA");
        DialogInterface.OnClickListener onClickInstall = makeOnClickInstallDialogListener();
        DialogInterface.OnClickListener onClickCancel = makeOnFailedToInitHandler();

        DialogGenerator.createConfirmDialog(this, 
                getResources().getString(R.string.tts_install_tts_data), 
                onClickCancel,
                onClickInstall).show();
    }
    
    /**
     * @see root.gast.speech.tts.TtsStartupListener#onWaitingForLanguageData()
     */
    @Override
    public void onWaitingForLanguageData()
    {
        Log.d(TAG, "waiting for language data");
        //either wait for install
        DialogInterface.OnClickListener onClickWait = makeOnFailedToInitHandler();
        DialogInterface.OnClickListener onClickInstall = makeOnClickInstallDialogListener();
        //or just do it again
        AlertDialog d = DialogGenerator.createConfirmDialog(TextToSpeechPlay.this, 
                getResources().getString(R.string.d_info),
                getResources().getString(R.string.tts_d_stilldownloading_message), 
                onClickWait, 
                getResources().getString(R.string.tts_d_stilldownloading_buttonwait), 
                onClickInstall, 
                getResources().getString(R.string.tts_d_stilldownloading_buttonretry));
        d.show();
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

    @Override
    public void onSuccessfulInit(TextToSpeech tts)
    {
        Log.d(TAG, "successful init");
        this.tts = tts;
        activateUi();
        setTtsListener();
    }
    
    /**
     * set the TTS listener to call {@link #onDone(String)} depending
     * on the Build.Version
     */
    private void setTtsListener()
    {
        final TextToSpeechPlay callWithResult = this;
        if (Build.VERSION.SDK_INT >= 15)
        {
            int listenerResult = tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onDone(String utteranceId)
                {
                    callWithResult.onDone(utteranceId);
                }
                @Override
                public void onError(String utteranceId)
                {
                    callWithResult.onError(utteranceId);
                }
                @Override
                public void onStart(String utteranceId)
                {
                    callWithResult.onStart(utteranceId);
                }
            });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        }
        else
        {
            int listenerResult = tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
            {
                @Override
                public void onUtteranceCompleted(String utteranceId)
                {
                    callWithResult.onDone(utteranceId);
                }
            });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "failed to add utterance completed listener");
            }
        }
    }

    public void onDone(final String utteranceId)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                Log.d(TAG, "utterance completed");
                stopSpeak.setEnabled(false);
                //add to the "log"
                appendToLog("completed: " + utteranceId);
            }
        });
    }

    public void onStart(final String utteranceId)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                Log.d(TAG, "TTS start");
                appendToLog("started: " + utteranceId);
            }
        });
    }

    public void onError(final String utteranceId)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                Log.e(TAG, "TTS error");
                appendToLog("error: " + utteranceId);
            }
        });
    }

    private void deactivateUi()
    {
        Log.d(TAG, "deactivate ui");
        //don't enable until the initialization is complete
        speak.setEnabled(false);
        earcon.setEnabled(false);
        silence.setEnabled(false);
    }
    
    private void activateUi()
    {
        Log.d(TAG, "activate ui");
        speak.setEnabled(true);
        earcon.setEnabled(true);
        silence.setEnabled(true);
    }
    
    private void hookButtons()
    {
        speak = (Button)findViewById(R.id.btn_speak);
        //speak something according to the preferences
        speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String speaking = getTextToSpeak();
                String utteranceId = makeUtteranceId(SPEAK_ROOT);
                startSpeaking(utteranceId, speaking);

                setTtsSettingsFromPreferences();
                boolean toFile = preferences.getBoolean(TextToSpeechPlay.this, R.string.pref_tts_tofile, R.string.pref_tts_tofile_default);
                if (toFile)
                {
                    String outputFile = getOutputFilePath(speaking);
                    HashMap<String, String> params = TextToSpeechUtils.makeParamsWith(utteranceId);
                    addFromPreferences(params);
                    tts.synthesizeToFile(speaking, params, outputFile);
                }
                else
                {
                    HashMap<String, String> params = TextToSpeechUtils.makeParamsWith(utteranceId);
                    addFromPreferences(params);
                    tts.speak(speaking, queueMode, params);
                }
            }
        });
        
        stopSpeak = (Button)findViewById(R.id.btn_stop_speak);
        stopSpeak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "before stop is speaking: " + tts.isSpeaking());
                tts.stop();
            }
        }); 

        //play silence
        silence = (Button)findViewById(R.id.btn_play_silence);
        silence.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String utteranceId = makeUtteranceId(SILENCE_ROOT);
                startSpeaking(utteranceId, "");
                setTtsSettingsFromPreferences();
                int silenceLength = preferences.getInt(TextToSpeechPlay.this, R.string.pref_tts_silencelength, R.string.pref_tts_silencelength_default);

                HashMap<String, String> params = TextToSpeechUtils.makeParamsWith(utteranceId);
                addFromPreferences(params);
                tts.playSilence(silenceLength, queueMode, params);
            }
        }); 

        //play an earcon
        earcon = (Button)findViewById(R.id.btn_play_earcon);
        earcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String earconName = "android";
                String utteranceId = makeUtteranceId(EARCON_ROOT);
                startSpeaking(utteranceId, "");
                setTtsSettingsFromPreferences();
                tts.addEarcon("android", "root.gast.playground", R.raw.tone);
                
                HashMap<String, String> params = TextToSpeechUtils.makeParamsWith(utteranceId);
                addFromPreferences(params);
                tts.playEarcon(earconName, queueMode,params);
            }
        }); 

        deactivateUi();
        
        log = (TextView)findViewById(R.id.tv_resultlog);
        whatToSay = (EditText)findViewById(R.id.et_tts_target);

        //default things to say
        String [] defaultTargets = getResources().getStringArray(R.array.default_tts_targets);
        presets = new ArrayList<String>();
        for (String target : defaultTargets)
        {
            presets.add(target);
        }
        
        //also add the files within the "tts" dir as possible
        //tts things to say.
        try
        {
            String[] files = getAssets().list("tts");
            Log.d(TAG, "num long files: " + files);
            for (String fileName : files)
            {
                presets.add("(" + fileName + ")");
            }
        } catch (IOException e)
        {
            Log.e(TAG, "can't file tts ", e);
        }

        //selecting a preset
        ImageButton bt = (ImageButton) findViewById(R.id.bt_ttsTargets);
        bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogGenerator.makeSelectListDialog(
                        getResources().getString(
                                R.string.tts_textToSpeakHeading),
                        TextToSpeechPlay.this, presets,
                        new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which)
                            {
                                String whichPreset = presets.get(which);
                                whatToSay.setText(whichPreset);
                            }
                        }, DialogGenerator.DO_NOTHING).show();
            }
        });
    }
    
    /**
     * get the filename to output when the user wants to write to file
     * @param speaking what the user is speaking
     */
    protected String getOutputFilePath(String speaking)
    {
        String appPath = EXTERNAL_STORAGE_DIR + File.pathSeparator + speaking.replaceAll("\\s", "_") + ".wav";
        return appPath;
    }

    /**
     * attach the utterance counter
     */
    private String makeUtteranceId(String root)
    {
        String id = root + utteranceCounter;
        utteranceCounter++;
        return id;
    }

    /**
     * either just get the text from the {@link TextView}
     * or if it is a file, read the file in
     */
    private String getTextToSpeak()
    {
        String toSay = whatToSay.getText().toString();
        if (toSay.contains("("))
        {
            //load the whole file to and play that
            String fileName = toSay.substring(1, toSay.length()-1);
            try
            {
                InputStream in = getAssets().open("tts" + File.separator + fileName);
                String contents = FileUtil.getContentsOfReader(new BufferedReader(new InputStreamReader(in)));
                toSay = contents;
            } catch (IOException e)
            {
                Log.e(TAG, "error reading file: " + fileName, e);
            }
        }
        return toSay;
    }

    /**
     * add parameters based on preferences
     */
    private void addFromPreferences(Map<String,String> parameters)
    {
        float volume = preferences.getFloat(this, R.string.pref_tts_speechrate, R.string.pref_tts_speechrate_default);
        float pan = preferences.getFloat(this, R.string.pref_tts_pan, R.string.pref_tts_pan_default);
        if (pan > 1)
        {
            pan = 1;
        }
        if (pan < -1)
        {
            pan = -1;
        }
        if (volume > 1)
        {
            volume = 1;
        }
        if (volume < 0)
        {
            volume = 0;
        }
        parameters.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(volume));
        parameters.put(TextToSpeech.Engine.KEY_PARAM_PAN, String.valueOf(pan));

        int audioStream = preferences.getInteger(this, R.string.pref_tts_stream, R.integer.pref_tts_stream_default);
        parameters.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audioStream));
    }

    /**
     * set the tts parameters based on preferences
     */
    private void setTtsSettingsFromPreferences()
    {
        float speechRate = preferences.getFloat(this, R.string.pref_tts_speechrate, R.string.pref_tts_speechrate_default);
        Log.d(TAG, "set speech rate: " + speechRate);
        tts.setSpeechRate(speechRate);

        float pitch = preferences.getFloat(this, R.string.pref_tts_pitch, R.string.pref_tts_pitch_default);
        Log.d(TAG, "set pitch rate: " + pitch);
        tts.setPitch(pitch);

        boolean queueFlush = preferences.getBoolean(this, R.string.pref_tts_queueaction, R.string.pref_tts_queueaction_default);
        Log.d(TAG, "set queueFlush: " + queueFlush);
        if (queueFlush)
        {
            queueMode = TextToSpeech.QUEUE_FLUSH;
        }
        else
        {
            queueMode = TextToSpeech.QUEUE_ADD;
        }

        boolean customSpeech = preferences.getBoolean(this, R.string.pref_tts_customaudio, R.string.pref_tts_customaudio_default);
        Log.d(TAG, "set customSpeech: " + customSpeech);
        if (customSpeech)
        {
            tts.addSpeech("android", "root.gast.playground", R.raw.androidcalm);
        }
        else
        {
            //turn it off
            tts.addSpeech("android", null, R.raw.androidcalm);
        }
    }

    private void startSpeaking(String utteranceId, String text)
    {
        stopSpeak.setEnabled(true);
        appendToLog("queued " + utteranceId + " " + text.substring(0, Math.min(CHARACTERS_IN_LOG, text.length())));
    }
    
    private void appendToLog(String appendThis)
    {
        String currentLog = log.getText().toString();
        currentLog = appendThis + "\n" + currentLog;
        log.setText(currentLog);
    }

    /**
     * shutdown tts on destroy
     */
    @Override
    protected void onDestroy()
    {
        if (tts != null)
        {
            tts.shutdown();
        }
        super.onDestroy();
    }
    
    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tts_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.m_ttsparam:
                //launch preferences activity
                Intent i = new Intent(this, SummarizingEditPreferences.class);
                i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_INTENT, R.xml.tts_preferences);
                String preferenceName = getResources().getString(R.string.pref_tts_key);
                i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_NAME_INTENT, preferenceName);
                startActivity(i);
                break;
            case R.id.m_setlanguage:
                final List<Locale> localesUsed = TextToSpeechUtils.getLocalesSupported(this, tts);
                DialogGenerator.makeSelectListDialog(getResources().getString(R.string.d_select_language), 
                        this, localesUsed, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Locale loc = localesUsed.get(which);
                            //Note: only supported locales appear in the
                            //list so this is safe
                            tts.setLanguage(loc);
                            //also set a preference
                            preferences.setLanguage(TextToSpeechPlay.this, loc);
                        }
                    }).show();
                break;
            case R.id.m_setaudiostream:
                final Map<String, Integer> streamIdToName = new LinkedHashMap<String, Integer>();
                streamIdToName.put("STREAM_ALARM", AudioManager.STREAM_ALARM);
                streamIdToName.put("STREAM_DTMF", AudioManager.STREAM_DTMF);
                streamIdToName.put("STREAM_MUSIC", AudioManager.STREAM_MUSIC);
                streamIdToName.put("STREAM_NOTIFICATION", AudioManager.STREAM_NOTIFICATION);
                streamIdToName.put("STREAM_RING", AudioManager.STREAM_RING);
                streamIdToName.put("STREAM_SYSTEM", AudioManager.STREAM_SYSTEM);
                streamIdToName.put("STREAM_VOICE_CALL", AudioManager.STREAM_VOICE_CALL);

                final List<String> streamNames = new ArrayList<String>(streamIdToName.keySet());
                DialogGenerator.makeSelectListDialog(getResources().getString(R.string.d_select_audio_stream), 
                        this, streamNames, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            int audioStream = streamIdToName.get(streamNames.get(which));
                            preferences.setInt(getString(R.string.pref_tts_stream), audioStream);
                        }
                    }).show();
                break;
            default:
                throw new RuntimeException("unknown menu selection");
        }
        return true;
    }
}