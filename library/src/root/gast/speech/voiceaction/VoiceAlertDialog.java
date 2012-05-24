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
package root.gast.speech.voiceaction;

import java.util.ArrayList;

import root.gast.R;
import root.gast.speech.text.match.SoundsLikeThresholdWordMatcher;
import root.gast.speech.text.match.SoundsLikeWordMatcher;
import root.gast.speech.text.match.StemmedWordMatcher;
import root.gast.speech.text.match.WordMatcher;
import android.app.AlertDialog;
import android.content.Context;

/**
 * A specialized kind of {@link VoiceAction} for common 
 * "yes/no/cancel" type voice actions. Similar to the
 * {@link AlertDialog} class, but made for voice.
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class VoiceAlertDialog extends MultiCommandVoiceAction
{
    // use match levels to indicate when you want less
    // to allow less strict matches
    public static final int MATCH_LEVEL_STRICT = 0;
    public static final int MATCH_LEVEL_STEM = 1;
    public static final int MATCH_LEVEL_PHONETIC = 2;
    public static final int MATCH_LEVEL_PHONETIC_LESS_STRICT = 3;

    private String[] yesWords = new String[] { "yes", "ok" };
    private String[] noWords = new String[] { "no" };
    private String[] neutralWords = new String[] { "cancel", "done" };

    public VoiceAlertDialog()
    {
        super(new ArrayList<VoiceActionCommand>());
    }

    /**
     * get the command words from resources
     */
    public VoiceAlertDialog(Context context)
    {
        super(new ArrayList<VoiceActionCommand>());
        yesWords =
                context.getResources().getStringArray(
                        R.array.voiceaction_yeswords);
        noWords =
                context.getResources().getStringArray(
                        R.array.voiceaction_nowords);
        neutralWords =
                context.getResources().getStringArray(
                        R.array.voiceaction_neutralwords);
    }

    /**
     * add your own command to the dialog here if it consists of words
     */
    public void add(OnUnderstoodListener listener, String... words)
    {
        add(new MatcherCommand(new WordMatcher(words), listener));
    }

    public void addPositive(OnUnderstoodListener listener)
    {
        add(listener, yesWords);
    }

    public void addNegative(OnUnderstoodListener listener)
    {
        add(listener, noWords);
    }

    public void addNeutral(OnUnderstoodListener listener)
    {
        add(listener, neutralWords);
    }

    public void addRelaxedPositive(OnUnderstoodListener listener)
    {
        addRelaxedAll(listener, yesWords);
    }

    public void addRelaxedNegative(OnUnderstoodListener listener)
    {
        addRelaxedAll(listener, noWords);
    }

    public void addRelaxedNeutral(OnUnderstoodListener listener)
    {
        addRelaxedAll(listener, neutralWords);
    }

    /**
     * add some command words, but allow for less strict matching
     */
    public void addRelaxedAll(OnUnderstoodListener listener, String... words)
    {
        add(listener, MATCH_LEVEL_STRICT, words);
        add(listener, MATCH_LEVEL_STEM, words);
        add(listener, MATCH_LEVEL_PHONETIC, words);
        add(listener, MATCH_LEVEL_PHONETIC_LESS_STRICT, words);
    }

    /**
     * allow matching at different levels of confidence
     */
    private void add(OnUnderstoodListener listener, int matchType,
            String... words)
    {
        WordMatcher matcher;
        switch (matchType)
        {
            case MATCH_LEVEL_STEM:
                matcher = new StemmedWordMatcher(words);
                break;
            case MATCH_LEVEL_PHONETIC:
                matcher = new SoundsLikeWordMatcher(words);
                break;
            case MATCH_LEVEL_PHONETIC_LESS_STRICT:
                matcher = new SoundsLikeThresholdWordMatcher(3, words);
                break;
            case MATCH_LEVEL_STRICT:
            default:
                matcher = new WordMatcher(words);
                break;
        }
        add(new MatcherCommand(matcher, listener));
    }
}
