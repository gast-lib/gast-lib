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
package root.gast.speech.text.match;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Match incoming words to a set of words, sublasses may encode the strings first
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class WordMatcher
{
    private Set<String> words;
    public static final int NOT_IN = -1;

    public WordMatcher(String... wordsIn)
    {
        this(Arrays.asList(wordsIn));
    }

    public WordMatcher(List<String> wordsIn)
    {
        //care about order so we can execute isInAt
        words = new LinkedHashSet<String>(wordsIn);
    }

    public Set<String> getWords()
    {
        return words;
    }
    
    public boolean isIn(String word)
    {
        return words.contains(word);
    }

    public boolean isIn(String [] wordsIn)
    {
        boolean wordIn = false;
        for (String word : wordsIn)
        {
            if (isIn(word))
            {
                wordIn = true;
                break;
            }
        }
        return wordIn;
    }

    public int isInAt(String [] wordsIn)
    {
        int which = NOT_IN;
        for (String word : wordsIn)
        {
            which = isInAt(word);
            if (which != NOT_IN)
            {
                break;
            }
        }
        return which;
    }

    public int isInAt(String wordCheck)
    {
        int which = NOT_IN;
        int ct = 0;
        for (String word : words)
        {
            if (word.equals(wordCheck))
            {
                which = ct;
                break;
            }
            ct++;
        }
        return which;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (String word : getWords())
        {
            sb.append(word).append(" ");
        }
        return sb.toString().trim();
    }
}
