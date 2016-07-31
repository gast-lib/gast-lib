package root.gast.speech.text;

import junit.framework.TestCase;
import root.gast.speech.text.match.WordMatcher;

public class TestWordMatcher extends TestCase
{
    public void testDict()
    {
        WordMatcher wd = new WordMatcher("one", "two");
        assertTrue(wd.isIn("one"));
        assertTrue(wd.isIn("two"));
        assertTrue(wd.isIn(new String [] {"one", "two"}));
        assertFalse(wd.isIn("NOTHING"));
        assertFalse(wd.isIn("onez"));
    }

}
