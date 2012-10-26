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
package root.gast.speech.text;


/**
 * utility class for processing recognition results
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class WordList
{
    private String [] words;
    
    private String source;
    
    public WordList(String source)
    {
        this.source = source.toLowerCase();
        words = this.source.split("\\s");
    }

    public String getStringAfter(int wordIndex)
    {
        int startAt = wordIndex + 1;
        if (startAt >= words.length)
        {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = startAt; i < words.length; i++)
        {
            sb.append(words[i]).append(" ");
        }
        return sb.toString();
    }

    public String getStringWithout(int indexToRemove)
    {
        if (indexToRemove >= words.length)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++)
        {
            if (i != indexToRemove)
            {
                sb.append(words[i]).append(" ");
            }
        }
        return sb.toString();
    }

    public String[] getWords()
    {
        return words;
    }
    
    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return source;
    }
}
