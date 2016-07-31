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

import android.util.Log;

/**
 * Provide some common methods for any voice action
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public abstract class AbstractVoiceAction implements VoiceAction, OnNotUnderstoodListener
{
    private static final String TAG = "AbstractVoiceAction";
    
    private String prompt;

    private String spokenPrompt;

    private OnNotUnderstoodListener notUnderstood;
    
    /**
     * by default include all possible recognitions
     */
    private float minConfidenceRequired = -1.0f;

    /**
     * must be higher than this in order to report not a command
     * In general this should be a high number
     */
    private float notACommandConfidenceThreshold = 0.9f;

    /**
     * if the confidence is lower than this, assume the recognizer
     * had inaccurate recognition
     */
    private float inaccurateConfidenceThreshold = 0.3f;
    
    public AbstractVoiceAction()
    {
        //default implementation
        notUnderstood = this;
    }

    public void setMinConfidenceRequired(float minConfidenceRequired)
    {
        this.minConfidenceRequired = minConfidenceRequired;
    }
    
    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }
    
    /**
     * @see root.gast.speech.voiceaction.VoiceAction#hasSpokenPrompt()
     */
    @Override
    public boolean hasSpokenPrompt()
    {
        return spokenPrompt != null && spokenPrompt.length() > 0;
    }
    
    /**
     * @return the prompt
     */
    public String getPrompt()
    {
        return prompt;
    }

    /**
     * @param notUnderstood the notUnderstood to set
     */
    public void setNotUnderstood(OnNotUnderstoodListener notUnderstood)
    {
        this.notUnderstood = notUnderstood;
    }
    
    /**
     * @return the notUnderstood
     */
    public OnNotUnderstoodListener getNotUnderstood()
    {
        return notUnderstood;
    }
    
    /**
     * @return the minConfidenceRequired
     */
    public float getMinConfidenceRequired()
    {
        return minConfidenceRequired;
    }
    
    @Override
    public String getSpokenPrompt()
    {
        return spokenPrompt;
    }
    
    /**
     * @see root.gast.speech.voiceaction.VoiceAction#setSpokenPrompt(java.lang.String)
     */
    @Override
    public void setSpokenPrompt(String prompt)
    {
        spokenPrompt = prompt;
    }

    public float getNotACommandConfidenceThreshold()
    {
        return notACommandConfidenceThreshold;
    }

    public void setNotACommandConfidenceThreshold(
            float notACommandConfidenceThreshold)
    {
        this.notACommandConfidenceThreshold = notACommandConfidenceThreshold;
    }

    public float getInaccurateConfidenceThreshold()
    {
        return inaccurateConfidenceThreshold;
    }

    public void setInaccurateConfidenceThreshold(
            float inaccurateConfidenceThreshold)
    {
        this.inaccurateConfidenceThreshold = inaccurateConfidenceThreshold;
    }
    
    @Override
    public void notUnderstood(List<String> heard, int reason)
    {
        Log.d(TAG, "not understood because of " + reason);
    }
}
