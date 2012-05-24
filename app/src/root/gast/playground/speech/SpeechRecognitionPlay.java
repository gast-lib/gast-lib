/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.speech;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.language.Soundex;

import root.gast.R;
import root.gast.playground.pref.PreferenceHelper;
import root.gast.playground.pref.SummarizingEditPreferences;
import root.gast.playground.speech.activation.SpeechActivatorTagWriter;
import root.gast.playground.speech.visitor.MatchesTargetVisitor;
import root.gast.playground.speech.visitor.MatchesTargetVisitorSoundex;
import root.gast.playground.speech.visitor.MatchesTargetVisitorStem;
import root.gast.playground.speech.visitor.PartialMatchTargetVisitor;
import root.gast.playground.speech.visitor.SpeechResultVisitor;
import root.gast.playground.util.DialogGenerator;
import root.gast.speech.LanguageDetailsChecker;
import root.gast.speech.OnLanguageDetailsListener;
import root.gast.speech.RecognizerIntentFactory;
import root.gast.speech.SpeechRecognizingActivity;
import root.gast.speech.activation.ClapperActivator;
import root.gast.speech.activation.MovementActivator;
import root.gast.speech.activation.SpeechActivationListener;
import root.gast.speech.activation.SpeechActivator;
import root.gast.speech.activation.SpeechActivatorFactory;
import root.gast.speech.activation.WordActivator;
import root.gast.speech.text.index.Stemmer;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * helps the user test speech recognition with all its settings and use cases interactively
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechRecognitionPlay extends SpeechRecognizingActivity implements SpeechActivationListener
{
    private static final String TAG = "SpeechRecognitionPlay";

    private ListView log;

    private EditText whatYouAreTryingToSay;
    
    private TextView resultsSummary;
    
    private Button speak;
    
    private Button stopSpeaking;
    
    private PreferenceHelper preferences;
    
    private List<String> presets;

    private List<String> activationMethod; 
    
    private SpeechActivator speechActivator;

    private ViewSwitcher startStopButton;

    private boolean isListeningForActivation = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speechrecognition);
        
        hookButtons();
        
        init();
    }

    private void init()
    {
        preferences = new PreferenceHelper(getResources().getString(R.string.pref_speech_key), 
                this.getApplicationContext());
        isListeningForActivation = false;
    }

    private void hookButtons()
    {
        final SpeechRecognitionPlay finalContext = this;
        speechActivator = null;
        
        speak = (Button)findViewById(R.id.btn_speak);
        speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivator();
            }
        });

        stopSpeaking = (Button)findViewById(R.id.btn_stop_speak);
        stopSpeaking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stopActivator();
            }
        });
        
        startStopButton = (ViewSwitcher)findViewById(R.id.vf_start_speak_flipper);
        
        log = (ListView)findViewById(R.id.lv_resultlog);
        whatYouAreTryingToSay = (EditText)findViewById(R.id.et_speech_target);

        resultsSummary = (TextView)findViewById(R.id.tv_speechResultsSummary);

        //set speech target selector. what you are trying to say
        String [] defaultTargets = getResources().getStringArray(R.array.default_speech_targets);
        presets = Arrays.asList(defaultTargets);
        ImageButton bt = (ImageButton)findViewById(R.id.bt_speechTargets);
        bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogGenerator.makeSelectListDialog(getResources().getString(R.string.speechTargetPresetHeading), 
                        SpeechRecognitionPlay.this, presets, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                whatYouAreTryingToSay.setText(presets.get(which));
                                resetSpeechResult();
                            }
                        }, DialogGenerator.DO_NOTHING).show();
            }
        });

        //set activation method selector
        String [] activationMethods = getResources().getStringArray(R.array.speech_activation_types);
        activationMethod = Arrays.asList(activationMethods);

        ImageButton btActivate = (ImageButton)findViewById(R.id.bt_speechActivation);
        btActivate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogGenerator.makeSelectListDialog(getResources().getString(R.string.speechActivationHeading), 
                        SpeechRecognitionPlay.this, activationMethod, new DialogInterface.OnClickListener()
                        {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //stop the activator before doing something new
                                stopActivator();
                                String whichMethod = activationMethod.get(which);
                                Log.d(TAG, "selected activation method: " + whichMethod);

                                speechActivator = SpeechActivatorFactory.createSpeechActivator(
                                        SpeechRecognitionPlay.this, finalContext, whichMethod);
                                resetSpeakButtonText();
                            }
                        }, DialogGenerator.DO_NOTHING).show();
            }
        });
    }
 
    //handle the various activation methods
    
    private void restartActivator()
    {
        if (isListeningForActivation)
        {
            isListeningForActivation = false;
            startActivator();
        }
    }

    private void startActivator()
    {
        if (isListeningForActivation)
        {
            //only activate once
            return;
        }
        
        if (speechActivator == null)
        {
            Log.d(TAG, "activator is null");
            startRecognition();
        }
        else
        {
            isListeningForActivation = true;
            speechActivator.detectActivation();
            startStopButton.showNext();
            resetSpeakButtonText();
        }
    }

    private void stopActivator()
    {
        if (speechActivator != null)
        {
            // first stop
            speechActivator.stop();
            // then reset button
            startStopButton.showPrevious();
        }
        isListeningForActivation = false;
    }
    
    private void resetSpeakButtonText()
    {
        setStartButton();
        setStopButton();
    }

    private void setStartButton()
    {
        String prefix = getResources().getString(R.string.speech_activation_btn_prefix);
        String label = SpeechActivatorFactory.getLabel(this, speechActivator);
        if (speechActivator == null)
        {
            stopSpeaking.setText(this.getResources().getString(R.string.btn_speak_label));
        }
        else
        {
            speak.setText(prefix + " " + label);
        }
    }
    
    private void setStopButton()
    {
        if (speechActivator == null)
        {
            stopSpeaking.setText(this.getResources().getString(R.string.btn_speak_label));
        }
        else if (speechActivator instanceof WordActivator)
        {
            stopSpeaking.setText("Stop listinging for hello");
        }
        else if ((speechActivator instanceof MovementActivator))
        {
            stopSpeaking.setText("Stop detecting movement");
        }
        else if ((speechActivator instanceof ClapperActivator))
        {
            stopSpeaking.setText("Stop listening for clap");
        }
        else
        {
            stopSpeaking.setText("Other listening happening");
        }
    }

    @Override
    public void activated(boolean success)
    {
        Log.d(TAG, "activated...");
        //app could indicated to stop, but an activation
        //could still occur. possibly ignore it
        if (!isListeningForActivation)
        {
            Log.d(TAG, "listening stopped");
            //don't do multiple activations
            return;
        }
        // first stop the activator so no activations occur 
        if (speechActivator != null)
        {
            //still more may occur, do I need a boolean
            speechActivator.stop();
        }
        
        startRecognition();
    }

    //now do some recognition based on the set preferences
    
    private void startRecognition()
    {
        Log.d(TAG, "starting recognition");
        
        Intent targetIntent = readRecognizerIntentFromPreferences();
        boolean direct = preferences.getBoolean(SpeechRecognitionPlay.this, R.string.pref_direct, R.string.pref_direct_default);
        if (direct)
        {
            //put up toast since there is not dialog
            Toast.makeText(SpeechRecognitionPlay.this,
                    "Started recognition", Toast.LENGTH_SHORT).show();
            
            recognizeDirectly(targetIntent);
        }
        else
        {
            recognize(targetIntent);
        }
        resetSpeakButtonText();
    }
    
    /**
     * display the results
     */
    @Override
    public void receiveWhatWasHeard(List<String> heard, float [] confidenceScores)
    {
        String matchPreference = getMatchIndexingType();
        
        //Note: use a visitor pattern to process the heard strings and determine
        //what to put in the final column and how to highlight the heard strings
        //in the output log
        MatchesTargetVisitor matches = null;
        if (matchPreference.equals(getResources().getString(R.string.pref_match_stem)))
        {
            matches = new MatchesTargetVisitor(whatYouAreTryingToSay.getText().toString());
        }
        else if (matchPreference.equals(getResources().getString(R.string.pref_match_phonetic)))
        {
            matches = new MatchesTargetVisitorStem(whatYouAreTryingToSay.getText().toString());
        }
        else
        {
            matches = new MatchesTargetVisitorSoundex(whatYouAreTryingToSay.getText().toString());
        }
        //depends on "indexing algorithm"
        SpeechResultVisitor partial = new PartialMatchTargetVisitor(whatYouAreTryingToSay.getText().toString(), matches);

        List<? extends SpeechResultVisitor> visitors = 
            Arrays.asList(
                    matches,
                    partial
                    );

        //need to execute this so that the speech result works
        int i = 0;
        for (String heardPossibility : heard)
        {
            matches.mark(heardPossibility, i, null);
            i++;
        }
        
        SpeechResultAdapter adapter = 
            new SpeechResultAdapter(this, R.layout.speechresult_listitem, heard, confidenceScores, visitors);
        log.setAdapter(adapter);
        setSpeechResult(matches);
        
        //possibly restart
        restartActivator();
    }

    private String getMatchIndexingType()
    {
        String matchPreference = preferences.getString(getResources().getString(R.string.pref_match), 
                getResources().getString(R.string.pref_match_default));

        return matchPreference;
    }

    //menu handling
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.speech_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.m_speechparam)
        {
            //launch preferences activity
            Intent i = new Intent(this, SummarizingEditPreferences.class);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_INTENT, R.xml.speech_preferences);
            String preferenceName = getResources().getString(R.string.pref_speech_key);
            i.putExtra(SummarizingEditPreferences.WHICH_PREFERENCES_NAME_INTENT, preferenceName);
            startActivity(i);
        } else if (item.getItemId() == R.id.m_speech_language_details)
        {
            OnLanguageDetailsListener andThen = new OnLanguageDetailsListener() {
                @Override
                public void onLanguageDetailsReceived(
                        LanguageDetailsChecker data)
                {
                    String languagesSupportedDescription = data.toString();
                    DialogGenerator.createInfoDialog(SpeechRecognitionPlay.this,
                            getResources().getString(R.string.speech_data_check_result_title),
                            languagesSupportedDescription).show();                
                }
            };
            Intent detailsIntent =  new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
            sendOrderedBroadcast(
                    detailsIntent, null, new LanguageDetailsChecker(andThen), null, Activity.RESULT_OK, null, null);
        } else if (item.getItemId() == R.id.m_setlanguage)
        {
            OnLanguageDetailsListener andThenSetLanguage = new OnLanguageDetailsListener() {
                @Override
                public void onLanguageDetailsReceived(
                        LanguageDetailsChecker data)
                {
                    final List<String> langs = data.getSupportedLanguages();
                    DialogGenerator.makeSelectListDialog(getResources().getString(R.string.d_select_language), 
                            SpeechRecognitionPlay.this, langs, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String setLanguagePreferenceTo = langs.get(which);
                                preferences.setString(getResources().getString(R.string.pref_language), setLanguagePreferenceTo);
                            }
                        }).show();
                }
            };
            Intent details =  new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
            sendOrderedBroadcast(
                    details, null, new LanguageDetailsChecker(andThenSetLanguage), null, Activity.RESULT_OK, null, null);
        } else if (item.getItemId() == R.id.m_checkmenu_languageavailable)
        {
            //show all the locales, and pic one to check
            final List<Locale> localesUsed = Arrays.asList(Locale.getAvailableLocales());
            DialogGenerator.makeSelectListDialog(getResources().getString(R.string.d_select_language), 
                    this, localesUsed, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Locale loc = localesUsed.get(which);
                        Toast.makeText(SpeechRecognitionPlay.this, "Checking language support for: " + loc, Toast.LENGTH_SHORT).show();
                        checkForLanguage(loc);
                    }
                }).show();
        }
        else if (item.getItemId() == R.id.m_speech_compute_index)
        {
            final View frameLayout = getLayoutInflater().inflate(R.layout.enterworddialog, null);
            final EditText dialogtext = (EditText)frameLayout.findViewById(R.id.et_dialog_text_input);
            String hint = whatYouAreTryingToSay.getText().toString();
            if ( (hint != null) && (hint.length() > 0) )
            {
                hint = hint.split("\\s")[0];
            }
            dialogtext.setText(hint);
            //reply with the word's index
            //show an edit box
            DialogGenerator.createFrameDialog(this, 
                    "Enter word to index", frameLayout, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String input = dialogtext.getText().toString();
                    String soundex = new Soundex().encode(input);
                    String stem = Stemmer.stem(input);
                    StringBuilder message = new StringBuilder();
                    message.append("input: ").append(input).append("\n");
                    message.append("soundex: ").append(soundex).append("\n");
                    message.append("stem: ").append(stem);
                            DialogGenerator.createInfoDialog(
                                    SpeechRecognitionPlay.this,
                                    getResources().getString(R.string.d_info),
                                    message.toString()).show();
                }
            }).show();
        } 
        else if (item.getItemId() == R.id.m_write_speech_activation_tag)
        {
            Log.d(TAG, "start write activation");
            Intent doTag = new Intent(this, SpeechActivatorTagWriter.class);
            startActivity(doTag);
        }
        else
        {
            throw new RuntimeException("unknown menu selection");
        }
        return true;
    }

    /**
     * create the {@link RecognizerIntent} based on the many preferences
     */
    private Intent readRecognizerIntentFromPreferences()
    {
        Intent intentToSend;
        
        //web search handling
        boolean isWebSearchAction = preferences.getBoolean(this, R.string.pref_websearch, R.string.pref_websearch_default);
        if (isWebSearchAction)
        {
            intentToSend = RecognizerIntentFactory.getWebSearchRecognizeIntent();
            final boolean ADD_ORIGIN = true;
            if (ADD_ORIGIN && Build.VERSION.SDK_INT >= 14)
            {
                intentToSend.putExtra(RecognizerIntent.EXTRA_ORIGIN, true);
            }
        }
        else
        {
            intentToSend = RecognizerIntentFactory.getBlankRecognizeIntent();
        }

        //language model
        boolean isFreeFormModel = preferences.getBoolean(this, R.string.pref_languagemodel, R.string.pref_languagemodel_default);
        if (isFreeFormModel)
        {
            intentToSend.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        }
        else
        {
            intentToSend.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        }

        //common extras
        String language = 
            preferences.getString(getResources().getString(R.string.pref_language), 
                    getResources().getString(R.string.pref_language_default));
        intentToSend.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);

        String prompt = getResources().getString(R.string.speech_prompt) + ": " + whatYouAreTryingToSay.getText().toString();
        intentToSend.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        intentToSend.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 
                preferences.getInt(this, R.string.pref_maxresults, R.string.pref_maxresults_default));
        intentToSend.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, 
                preferences.getBoolean(this, R.string.pref_partial, R.string.pref_partial_default));


        setIfValueSpecified(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 
                R.string.pref_complete_silence, R.string.pref_complete_silence_default, intentToSend);
        setIfValueSpecified(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 
                R.string.pref_minimum_input_length, R.string.pref_minimum_input_length_default, intentToSend);
        setIfValueSpecified(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 
                R.string.pref_possibly_complete_silence_length, R.string.pref_possibly_complete_silence_length_default, intentToSend);
        
        //pendingIntent handling
        boolean doPending = preferences.getBoolean(this, R.string.pref_withpendingintent, R.string.pref_withpendingintent);
        if (doPending)
        {
            Intent pendingIntentSource =
                    new Intent(this, SpeechRecognitionResultsActivity.class);
            PendingIntent pi =
                    PendingIntent.getActivity(this, 0, pendingIntentSource, 0);

            Bundle extraInfoBundle = new Bundle();
            // pass in what you are trying to say so the results activity can
            // show it
            extraInfoBundle
                    .putString(
                            SpeechRecognitionResultsActivity.
                                WHAT_YOU_ARE_TRYING_TO_SAY_INTENT_INPUT,
                            whatYouAreTryingToSay.getText().toString());
            // set the variables in the intent this is sending
            intentToSend.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT,
                    pi);
            intentToSend.putExtra(
                    RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE,
                    extraInfoBundle);
        }

        Log.d(TAG, "sending recognizer intent: " + intentToSend.getExtras().toString());
        return intentToSend;
    }
    

    /**
     * utility method
     */
    private void setIfValueSpecified(
            String bundlePart,
            int resource, int resourceDefault, Intent intent)
    {
        long unsetValue = Long.valueOf(getResources().getString(R.string.UNSET));
        long value = preferences.getLong(this, resource, resourceDefault);
        if (value != unsetValue)
        {
            intent.putExtra(bundlePart, value);
        }
    }

    private void resetSpeechResult()
    {
        resultsSummary.setText("");
    }

    private void setSpeechResult(MatchesTargetVisitor matches)
    {
        String result = "";
        if (matches.isMatch())
        {
            result = result + " matched at " + matches.getMatchRank() + " (" + getMatchIndexingType() + ")";
        }
        else
        {
            result = result + " no match";
        }
        Log.d(TAG, "result: " + result);
        resultsSummary.setText(result);
    }
    
    //super class methods
    
    @Override
    public void speechNotAvailable()
    {
        String message =
                getResources().getString(R.string.speechNotAvailableMessage);
        DialogGenerator.createErrorDialog(this,
                getResources().getString(R.string.d_error), message,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                }).show();
    }
    
    @Override
    protected void directSpeechNotAvailable()
    {
    }

    @Override
    protected void languageCheckResult(String languageToUse)
    {
        if (languageToUse != null)
        {
            DialogGenerator.createInfoDialog(this, getResources().getString(R.string.d_info), languageToUse + " is available").show();
        }
        else
        {
            DialogGenerator.createInfoDialog(this, getResources().getString(R.string.d_info), "language not available").show();
        }
    }
    
    @Override
    protected void recognitionFailure(int errorCode)
    {
        //do not report an error
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (speechActivator != null)
        {
            stopActivator();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (speechActivator != null)
        {
            startActivator();
        }
    }

    @Override
    protected void onDestroy()
    {
        stopActivator();
        super.onDestroy();
    }
}