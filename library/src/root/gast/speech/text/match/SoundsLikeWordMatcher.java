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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.language.Soundex;

/**
 * encode strings using soundex
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SoundsLikeWordMatcher extends WordMatcher
{
    protected static Soundex soundex;
    
    static
    {
        soundex = new Soundex();
    }
    
    public SoundsLikeWordMatcher(String... wordsIn)
    {
        this(Arrays.asList(wordsIn));
    }

    public SoundsLikeWordMatcher(List<String> wordsIn)
    {
        super(encode(wordsIn));
    }
    
    @Override
    public boolean isIn(String word)
    {
        return super.isIn(encode(word));
    } 
    
    protected static List<String> encode(List<String> input)
    {
        List<String> encoded = new ArrayList<String>();
        for (String in : input)
        {
            encoded.add(encode(in));
        }
        return encoded;
    }

    private static String encode(String in)
    {
        return soundex.encode(in);
    }
}

    
