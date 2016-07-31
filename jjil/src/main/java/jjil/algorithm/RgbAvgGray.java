/*
 * RgbAvgGray.java
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
 * Pipeline stage converts an ARGB color image into a Gray8Image. It does
 * this by averaging the R, G, and B values. Note that since the byte image
 * is signed, each unsigned byte in the ARGB word is converted to a signed
 * integer before doing the average. So the word 0x00FF8001 would get
 * R, G, and B values 127, 0, and -126 (and the average would be 0).
 * @author webb
 */
public class RgbAvgGray extends PipelineStage {
    
    /** Creates a new instance of RgbAvgGray */
    public RgbAvgGray() {
    }
    
    /** Implementation of push operation from PipelineStage.
     * Averages the R, G, and B values to create a gray image of
     * the same size. Note that the RGB->Gray conversion involves
     * changing the data range of each pixel from 0->255 to -128->127
     * because byte is a signed type.
     *
     * @param image the input image
     * @throws jjil.core.Error if image is not an RgbImage
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbImage)) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
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
             * ARGB word. 
             */
            byte r = RgbVal.getR(rgbData[i]);
            byte g = RgbVal.getG(rgbData[i]);
            byte b = RgbVal.getB(rgbData[i]);
            /* average the values to get the grayvalue
             */
            grayData[i] = (byte)((r + g + b) / 3);
        }
        super.setOutput(gray);
    }
}
