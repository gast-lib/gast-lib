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

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Pipeline stage converts an ARGB color image into a Gray8Image by measuring
 * difference from a defined color. Pixels equal to the defined color get value
 * Byte.MIN_VALUE. Other pixels get sum of absolute difference between their
 * color and the defined color. We adjust the pixel color to compensate for
 * white balance.
 * <p>
 * For use when looking for objects of a given color. Thresholding or edge
 * detection should isolate the object.
 * 
 * @author webb
 */
public class RgbAbsDiffGrayWb extends PipelineStage
{
    int nR, nG, nB;

    /** Creates a new instance of RgbAvgGray */
    public RgbAbsDiffGrayWb(int nRGB)
    {
        this.nR = RgbVal.getR(nRGB);
        this.nG = RgbVal.getG(nRGB);
        this.nB = RgbVal.getB(nRGB);
    }

    /**
     * Implementation of push operation from PipelineStage. Averages the R, G,
     * and B values to create a gray image of the same size. Note that the
     * RGB->Gray conversion involves changing the data range of each pixel from
     * 0->255 to -128->127 because byte is a signed type.
     * 
     * @param image
     *            the input image
     * @throws jjil.core.Error
     *             if image is not an RgbImage
     */
    public void push(Image image) throws jjil.core.Error
    {
        if (!(image instanceof RgbImage))
        {
            throw new Error(Error.PACKAGE.ALGORITHM,
                    jjil.algorithm.ErrorCodes.IMAGE_NOT_RGBIMAGE,
                    image.toString(), null, null);
        }
        RgbImage rgb = (RgbImage) image;
        int[] rgbData = rgb.getData();
        Gray8Image gray = new Gray8Image(image.getWidth(), image.getHeight());
        byte[] grayData = gray.getData();
        for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
        {
            /*
             * get individual r, g, and b values, unmasking them from the ARGB
             * word.
             */
            byte r = RgbVal.getR(rgbData[i]);
            byte g = RgbVal.getG(rgbData[i]);
            byte b = RgbVal.getB(rgbData[i]);
            // adjust for white balance
            int nR = (r * Byte.MAX_VALUE) / mBrightestR;
            int nG = (g * Byte.MAX_VALUE) / mBrightestG;
            int nB = (b * Byte.MAX_VALUE) / mBrightestB;
            /*
             * average the values to get the grayvalue
             */
            grayData[i] = (byte) (Math.min(
                    Byte.MAX_VALUE,
                    (Math.abs(nR - this.nR) + Math.abs(nG - this.nG) + Math
                            .abs(nB - this.nB)) / 3 + Byte.MIN_VALUE));
        }
        super.setOutput(gray);
    }

    /**
     * Sets the color we are treating as the true white.
     * We set the minimum value to 1 in each channel to avoid a
     * division by zero in the adjustment.
     * @param nWbColor: "true" white color
     */
    public void setWhiteBalance(int nWbColor)
    {
        mBrightestR = Math.max(1, RgbVal.getR(nWbColor));
        mBrightestG = Math.max(1, RgbVal.getG(nWbColor));
        mBrightestB = Math.max(1, RgbVal.getB(nWbColor));
    }

    int mBrightestR, mBrightestG, mBrightestB;
}
