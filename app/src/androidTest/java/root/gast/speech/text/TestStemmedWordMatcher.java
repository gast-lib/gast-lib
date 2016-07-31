package root.gast.speech.text;

import junit.framework.TestCase;
import root.gast.speech.text.match.StemmedWordMatcher;

public class TestStemmedWordMatcher extends TestCase
{
    public void testDict()
    {
        StemmedWordMatcher wd = new StemmedWordMatcher("tree", "car", "walk");
        assertTrue(wd.isIn("tree"));
        assertTrue(wd.isIn("trees"));
        assertTrue(wd.isIn("cars"));
        assertTrue(wd.isIn("walk"));
        assertTrue(wd.isIn("walking"));
        assertTrue(wd.isIn("walks"));
        assertTrue(wd.isIn("walked"));
    }

    public void testDictOther()
    {
        StemmedWordMatcher wd =
                new StemmedWordMatcher("tree", "car", "walk");
        assertFalse(wd.isIn("NOTHING"));
        assertTrue(wd.isIn(new String[] { "one", "two", "walks" }));
    }

}
