/*
 * RgbAvg2Gray.java
 *
 * Created on August 27, 2006, 8:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2007 by Jon A. Webb
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the Lesser GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jjil.algorithm;
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Pipeline stage converts an RgbImage into a Gray8Image. It does
 * this by choosing at each pixel the value that will give the maximum
 * contrast in the resulting image. That is, it computes 
 * the R, G, and B values at each pixel, and chooses the most extreme
 * value -- the one largest in absolute value.
 * @author webb
 */
public class RgbMaxContrast2Gray extends PipelineStage {
    
    /** Implementation of push operation from PipelineStage.
     * Pipeline stage converts an ARGB color image into a Gray8Image. It does
     * this by choosing at each pixel the value that will give the maximum
     * contrast in the resulting image. That is, it computes 
     * the R, G, and B values at each pixel, and chooses the most extreme
     * value -- the one largest in absolute value. 
     * Note that the RGB&rarr;Gray conversion involves
     * changing the data range of each pixel from 0&rarr;255 to -128&rarr;127
     * because byte is a signed type.
     *
     * @param image the input image
     * @throws jjil.core.Error if image is not an RgbImage
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbImage)) {
            throw new Error(
        			Error.PACKAGE.CORE,
        			ErrorCodes.IMAGE_NOT_RGBIMAGE,
        			image.toString(),
        			null,
        			null);
        }
        RgbImage rgb = (RgbImage) image;
        int[] rgbData = rgb.getData();
        Gray8Image gray = new Gray8Image(image.getWidth(), image.getHeight());
        byte[] grayData = gray.getData();
        for (int i=0; i<image.getWidth() * image.getHeight(); i++) {
            /* get individual r, g, and b values, unmasking them from the
             * ARGB word. The r, g, and b values are signed values 
             * from -128 to 127.
             */
            byte r = RgbVal.getR(rgbData[i]);
            byte g = RgbVal.getG(rgbData[i]);
            byte b = RgbVal.getB(rgbData[i]);
            int nAR = Math.abs(r);
            int nAG = Math.abs(g);
            int nAB = Math.abs(b);
            if (nAR >= nAG && nAR >= nAB) {
            	grayData[i] = r;
            } else if (nAG >= nAB) {
            	grayData[i] = g;
            } else {
            	grayData[i] = b;
            }
        }
        super.setOutput(gray);
    }
}
