package root.gast.speech.text;

import junit.framework.TestCase;
import root.gast.speech.text.match.SoundsLikeThresholdWordMatcher;
import root.gast.speech.text.match.SoundsLikeWordMatcher;

public class TestSoundsLikeWordMatcher extends TestCase
{
    public void testSoundsLikeMatcher()
    {
        SoundsLikeWordMatcher wd =
                new SoundsLikeWordMatcher("beat", "faint", "thyme");
        assertTrue(wd.isIn("beat"));
        assertTrue(wd.isIn("faint"));
        assertTrue(wd.isIn("thyme"));
        assertTrue(wd.isIn("beet"));
        assertTrue(wd.isIn("feint"));
        assertTrue(wd.isIn("time"));
        assertFalse(wd.isIn("thy"));
        assertFalse(wd.isIn("trine"));
        assertTrue(wd.isIn(new String[] { "beat", "none", "blah" }));
    }

    public void testSoundsLikeThresholdMatcher()
    {
        SoundsLikeThresholdWordMatcher wd =
            new SoundsLikeThresholdWordMatcher(3, "cumin");
        assertTrue(wd.isIn("human"));
    }
}
