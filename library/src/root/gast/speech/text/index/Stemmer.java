package root.gast.speech.text.index;


/**
 * Class to abstract away the actual stemmer implementation
 * @author <a href="mailto:gregorym@gmail.com">Greg Milette</a>
 */
public class Stemmer
{
    public static String stem(String word)
    {
        return StemmerLucene.stem(word);
    }

}
