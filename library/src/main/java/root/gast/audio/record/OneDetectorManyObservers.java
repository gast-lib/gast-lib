/*
 * Copyright 2012 Greg Milette and Adam Stroud
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
package root.gast.audio.record;

import java.util.List;


/**
 * allow many listeners to receive the data, but only one to act on it
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class OneDetectorManyObservers implements AudioClipListener
{
    private AudioClipListener detector;
    
    private List<AudioClipListener> observers;
    
    public OneDetectorManyObservers(AudioClipListener detector, List<AudioClipListener> observers)
    {
        this.detector = detector;
        this.observers = observers;
    }
    
    @Override
    public boolean heard(short[] audioData, int sampleRate)
    {
        for (AudioClipListener observer : observers)
        {
            observer.heard(audioData, sampleRate);
        }
        
        return detector.heard(audioData, sampleRate);
    }
}
