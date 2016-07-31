package root.gast.speech.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

import com.tangentum.phonetix.PhoneticEncoder;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class PhoneticFilter31 extends TokenFilter
{
    private static final String TAG = "PhoneticFilter31";
    
    private final PhoneticEncoder phoneticEncoder;
    
    private final CharTermAttribute termAtt;
    
    private final KeywordAttribute keywordAttr;
    
    public PhoneticFilter31(TokenStream input, PhoneticEncoder phoneticEncoder)
    {
        super(input);
        termAtt = addAttribute(CharTermAttribute.class);
        keywordAttr = addAttribute(KeywordAttribute.class);
        this.phoneticEncoder = phoneticEncoder;
    }

    @Override
    public boolean incrementToken() throws IOException
    {
        if (input.incrementToken())
        {
            //don't modify this if it is a keyword
            if (!keywordAttr.isKeyword())
            {
                //calculate the key and
                //replace the buffer with it
                String term = termAtt.subSequence(0, termAtt.length()).toString();
                String code = phoneticEncoder.generateKey(term);
                termAtt.resizeBuffer(code.length());
                termAtt.copyBuffer(code.toCharArray(), 0, code.length());
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}
