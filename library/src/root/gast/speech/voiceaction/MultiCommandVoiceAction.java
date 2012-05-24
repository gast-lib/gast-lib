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

import java.util.List;

import root.gast.speech.text.WordList;
import android.util.Log;

/**
 * a group of {@link VoiceAction}s
 * 
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class MultiCommandVoiceAction extends AbstractVoiceAction
{
    private static final String TAG = "MultiCommandVoiceAction";

    private List<VoiceActionCommand> commands;

    public MultiCommandVoiceAction(List<VoiceActionCommand> commands)
    {
        this.commands = commands;
    }

    @Override
    public boolean interpret(List<String> heard, float[] confidenceScores)
    {
        boolean understood = false;

        //Android version 4.0 and less devices will have null
        boolean hasConfidenceScores = (confidenceScores != null);
        
        // halt after understood something
        for (int i = 0; i < heard.size() && !understood; i++)
        {
            String said = heard.get(i);
            
            //only check confidence if the app supports it
            boolean exceedsMinConfidence = true;
            if (hasConfidenceScores)
            {
                exceedsMinConfidence = 
                    (confidenceScores[i] > getMinConfidenceRequired());
            }
            
            if (exceedsMinConfidence)
            {
                WordList saidWords = new WordList(said);
                for (VoiceActionCommand command : commands)
                {
                    understood = command.interpret(
                            saidWords, confidenceScores);
                    if (understood)
                    {
                        Log.d(TAG, "Command successful: "
                                + command.getClass().getSimpleName());
                        break;
                    }
                }
            }
        }

        if (!understood)
        {
            if (hasConfidenceScores)
            {
                Log.d(TAG, "VoiceAction unsuccessful: " + getPrompt());
                // interpret confidence to provide a reason to
                // notUnderstood

                // check only the highest confidence score, which should be the
                // first
                float highestConfidenceScore = confidenceScores[0];
                if (highestConfidenceScore < 0.0)
                {
                    getNotUnderstood().notUnderstood(heard,
                        OnNotUnderstoodListener.REASON_UNKNOWN);
                }
                else
                {
                    if (highestConfidenceScore < 
                                    getInaccurateConfidenceThreshold())
                    {
                        getNotUnderstood()
                                        .notUnderstood(
                                            heard,
                                            OnNotUnderstoodListener.
                                                REASON_INACCURATE_RECOGNITION);
                    }
                    else if (highestConfidenceScore >= 
                        getNotACommandConfidenceThreshold())
                    {
                        getNotUnderstood().notUnderstood(heard,
                            OnNotUnderstoodListener.REASON_NOT_A_COMMAND);
                    }
                    else
                    {
                        getNotUnderstood().notUnderstood(heard,
                            OnNotUnderstoodListener.REASON_UNKNOWN);
                    }
                }
            }
            else
            {
                getNotUnderstood().notUnderstood(heard,
                    OnNotUnderstoodListener.REASON_UNKNOWN);
            }
        }

        return understood;
    }

    protected void add(VoiceActionCommand command)
    {
        commands.add(command);
    }
}
