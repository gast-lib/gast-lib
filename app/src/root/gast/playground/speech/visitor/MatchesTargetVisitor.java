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
package root.gast.playground.speech.visitor;

import android.text.Spannable;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class MatchesTargetVisitor implements SpeechResultVisitor
{
    private String target;

    private boolean match;
    
    private int matchRank;
    
    public MatchesTargetVisitor(String target)
    {
        this.target = encode(target);
        match = false;
        matchRank = -1;
    }
     
    @Override
    public String mark(String result, int rank, Spannable wordToSpan)
    {
        if (encode(result).equalsIgnoreCase(target))
        {
            if (!match)
            {
                matchRank = rank;
                match = true;
            }
            return getMark();
        }
        else
        {
            return NO_MARK;
        }
    }
    
    public boolean isSame(String s1, String s2)
    {
        return encode(s1).equals(encode(s2));
    }
    
    /**
     * subclasses change what the visitor means by "encoding"
     */
    protected String encode(String toEncode)
    {
        return toEncode;
    }

    /**
     * @return the match
     */
    public boolean isMatch()
    {
        return match;
    }
    
    /**
     * @return the matchRank
     */
    public int getMatchRank()
    {
        return matchRank;
    }

    /**
     * subclass override
     * @return
     */
    protected String getMark()
    {
        return "*";
    }
    
}
