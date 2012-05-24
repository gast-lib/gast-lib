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

import root.gast.speech.text.WordList;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;

/**
 * uses a matcher to highlight which words match
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class PartialMatchTargetVisitor implements SpeechResultVisitor
{
    private WordList tokens;
    
    private MatchesTargetVisitor matcher;

    public PartialMatchTargetVisitor(String target, MatchesTargetVisitor matcher)
    {
        tokens = new WordList(target);
        this.matcher = matcher;
    }
    
    /**
     * highlight any parts of wordToSpan that match,
     * also if there is any match mark the result as P plus
     * the number of overlapping tokens
     */
    @Override
    public String mark(String result, int rank, Spannable wordToSpan)
    {
        int numOverlap = 0;

        WordList resultTokens = new WordList(result);
        int startIndexCount = 0;
        for (int i = 0; i < tokens.getWords().length && i < tokens.getWords().length; i++)
        {
            String targetToken = tokens.getWords()[i];
            boolean same = matcher.isSame(targetToken, resultTokens.getWords()[i]);
            
            if (same)
            {
                numOverlap++;
                int start = startIndexCount;
                if (start >= 0)
                {
                    int end = start + targetToken.length();
                    if (end > result.length())
                    {
                        end = result.length();
                    }
                    if (start <= end)
                    {
                        wordToSpan.setSpan(
                                new BackgroundColorSpan(root.gast.playground.R.color.lightgrey), 
                                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            startIndexCount = startIndexCount + targetToken.length() + 1;
        }

        if (numOverlap > 0)
        {
            return "P"+numOverlap;
        }
        else
        {
            return NO_MARK;
        }
    }
}
