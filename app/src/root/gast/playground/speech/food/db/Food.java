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
package root.gast.playground.speech.food.db;

import java.text.NumberFormat;

/**
 * Domain class for Food
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class Food
{
    private String name;
    private float calories;
    
    public static final float UNKNOWN_CALORIES = -1f;

    private static final NumberFormat noDecimalFormat;
    static
    {
        noDecimalFormat = NumberFormat.getInstance();
        noDecimalFormat.setMaximumFractionDigits(0);
    }

    public Food(String name)
    {
        this(name, UNKNOWN_CALORIES);
    }

    public Food(String name, float calories)
    {
        this.name = name;
        this.calories = calories;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " " + getCalories();
    }
    
    /**
     * @return the calories
     */
    public float getCalories()
    {
        return calories;
    }

    public String getFormattedCalories()
    {
        return noDecimalFormat.format(calories);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
}
