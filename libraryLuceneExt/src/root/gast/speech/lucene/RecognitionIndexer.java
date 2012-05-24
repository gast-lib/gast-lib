package root.gast.speech.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;

import android.util.Log;

/**
 * indexers for recognizing speech results
 */
public class RecognitionIndexer extends Analyzer
{
    private static final String TAG = "RecognitionIndexer";

    private boolean phonetic;

    private boolean doStem;

    /**
     * either do stemming or just phonetic, set one to true
     */
    public RecognitionIndexer(boolean phonetic, boolean doStem)
    {
        this.phonetic = phonetic;
        this.doStem = doStem;
    }

    @Override
    public TokenStream tokenStream(String tokens, Reader reader)
    {
        WhitespaceTokenizer w =
                new WhitespaceTokenizer(LuceneParameters.VERSION, reader);
        LowerCaseFilter lower =
                new LowerCaseFilter(LuceneParameters.VERSION, w);
        TokenFilter filter = null;
        EnglishMinimalStemFilter stem = null;
        if (doStem)
        {
            stem = new EnglishMinimalStemFilter(lower);
            filter = stem;
            Log.d(TAG, "do stem");
        }

        if (phonetic)
        {
            if (doStem)
            {
                // Note: PhoneticFilter31 is our custom Lucene class that 
                // works with Lucene 3.1.0 and uses any of phonetix algorithms
                PhoneticFilter31 phoneticWithStem =
                        new PhoneticFilter31(stem,
                                new com.tangentum.phonetix.Soundex());
                filter = phoneticWithStem;
                Log.d(TAG, "do stem and phonetic!");
            }
            else
            {
                PhoneticFilter31 phonetic =
                        new PhoneticFilter31(w,
                                new com.tangentum.phonetix.Soundex());
                filter = phonetic;
                Log.d(TAG, "do just phonetic");
            }
        }

        return filter;
    }
}
