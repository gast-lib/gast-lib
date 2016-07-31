/*
 * Copyright 2011 Jon A. Webb
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.image.detectlogobetter;

import jjil.core.RgbImage;
import jjil.core.RgbVal;

public class FindBrightestPoint
{
    public FindBrightestPoint(int nSkipHoriz, int nSkipVert)
    {
        mnSkipHoriz = nSkipHoriz;
        mnSkipVert = nSkipVert;
    }

    public int getBrightestColor()
    {
        return mcBrightestColor;
    }

    public void push(RgbImage rgb)
    {
        int nLuminance = Integer.MIN_VALUE;
        for (int i = 0; i < rgb.getHeight(); i += mnSkipVert)
        {
            for (int j = 0; j < rgb.getWidth(); j += mnSkipHoriz)
            {
                int cColor = rgb.getData()[i * rgb.getHeight() + j];
                int nThisLuminance = RgbVal.getR(cColor) + RgbVal.getG(cColor)
                        + RgbVal.getB(cColor);
                if (nThisLuminance > nLuminance)
                {
                    nLuminance = nThisLuminance;
                    mcBrightestColor = cColor;
                }
            }
        }
    }

    int mcBrightestColor;
    int mnSkipHoriz, mnSkipVert;
}
