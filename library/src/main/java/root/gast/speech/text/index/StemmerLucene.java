package root.gast.speech.text.index;

import org.tartarus.snowball.ext.EnglishStemmer;

/**
 *
 *
 * @author <a href="mailto:gregorym@gmail.com">Greg Milette</a>
 */
public class StemmerLucene
{
    private static EnglishStemmer stemmer;
    
    /**
     * run the stemmer from Lucene
     */
    public static String stem(String word)
    {
        stemmer = new EnglishStemmer();
        stemmer.setCurrent(word);
        boolean result = stemmer.stem();
        if (!result) 
        {
            return word;
        }
        return stemmer.getCurrent(); 
    }
}