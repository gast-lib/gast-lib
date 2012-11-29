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
package root.gast.playground.sensor;

import android.app.Activity;

/**
 * TODO
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class InvalidFragmentActivityException extends RuntimeException
{
    /**
     * Thrown when an activity does not implement the correct interface to use a fragment.
     *
     * @param activity The activity attempting to attach the fragment
     * @param clazz The class of the interface that the activiyt needs to implement
     */
    public InvalidFragmentActivityException(Activity activity, Class<?> clazz)
    {
        super(activity.getClass().getName() + " must implement " + clazz.getName());
    }
}
