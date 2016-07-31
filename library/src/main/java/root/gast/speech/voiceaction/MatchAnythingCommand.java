package root.gast.speech.voiceaction;

import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;

public class MatchAnythingCommand implements VoiceActionCommand
{
    private static final String TAG = "MatchAnythingCommand";
    
    private OnUnderstoodListener onUnderstood;

    public MatchAnythingCommand(OnUnderstoodListener onUnderstood)
    {
        this.onUnderstood = onUnderstood;
    }

    @Override
    public boolean interpret(WordList heard, float[] confidence)
    {
        boolean understood = false;
        if (heard.getWords().length > 0)
        {
            understood = true;
            if (onUnderstood != null)
            {
                onUnderstood.understood();
            }
        }
        return understood;
    }

    public OnUnderstoodListener getOnUnderstood()
    {
        return onUnderstood;
    }
}
