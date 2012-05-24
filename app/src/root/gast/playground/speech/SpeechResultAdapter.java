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
package root.gast.playground.speech;

import java.text.NumberFormat;
import java.util.List;

import root.gast.playground.R;
import root.gast.playground.speech.visitor.SpeechResultVisitor;
import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * help output speech results
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class SpeechResultAdapter extends ArrayAdapter<String>
{
    private static final String TAG = "SpeechResultAdapter";
    
    private List<String> items;
    
    private List<? extends SpeechResultVisitor> visitors;
    
    private float [] confidence;
    
    private Context context;

    /**
     * view only two digits of the confidence
     */
    private static final NumberFormat confidenceFormat;
    static
    {
        confidenceFormat = NumberFormat.getInstance();
        confidenceFormat.setMaximumFractionDigits(2);
    }
    
    public SpeechResultAdapter(Context context, int textViewResourceId,
            List<String> items, float [] confidence, List<? extends SpeechResultVisitor> visitors)
    {
        super(context, textViewResourceId, items);
        
        this.items = items;
        this.visitors = visitors;
        this.context = context;
        this.confidence = confidence;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.speechresult_listitem, null);
        }
        String speechResult = items.get(position);
        float conf;
        //could be null if the device doesn't support Android 4.0
        if (confidence == null)
        {
            conf = -1.0f;
        }
        else
        {
            conf = confidence[position];
        }
        if (speechResult != null)
        {
            TextView num = (TextView) v.findViewById(R.id.tv_speech_result_number);
            TextView confView = (TextView) v.findViewById(R.id.tv_speech_result_confidence);
            if (conf > 0.0)
            {
                String formattedConfidence = confidenceFormat.format(conf);
                confView.setText(formattedConfidence);
            }
            TextView text = (TextView) v.findViewById(R.id.tv_speech_result);
            text.setText("Italic, highlighted, bold.", TextView.BufferType.SPANNABLE);
            TextView flag = (TextView) v.findViewById(R.id.tv_speech_result_flag);
            num.setText(String.valueOf(position));
            text.setText(speechResult);
            String flagMark = SpeechResultVisitor.NO_MARK;
            Spannable wordToSpan = (Spannable) text.getText();
            for (SpeechResultVisitor visitor : visitors)
            {
                String possibleFlag = visitor.mark(speechResult, position, wordToSpan);
                if (!possibleFlag.equals(SpeechResultVisitor.NO_MARK))
                {
                    flagMark = flagMark + possibleFlag + " ";
                }
            }
            flag.setText(flagMark.trim());
            text.setText(wordToSpan);
        }
        return v;
    }
}
