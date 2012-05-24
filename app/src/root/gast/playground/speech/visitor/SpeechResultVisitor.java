package root.gast.playground.speech.visitor;

import android.text.Spannable;

/**
 * something that can review the results and
 * possibly highlight them
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public interface SpeechResultVisitor
{
    public String NO_MARK = "";
    
    public String mark(String result, int rank, Spannable wordToSpan);
}
