/*
 * Rgb3x3Average.java
 *
 * Created on August 27, 2006, 1:58 PM
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
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Pipeline stage performs a 3x3 RGB average of the input.
 * @author webb
 */
public class Rgb3x3Average extends PipelineStage {
    
    /**
     * Creates a new instance of Rgb3x3Average
     */
    public Rgb3x3Average() {
    }
       
    /**
     * Do a color 3x3 average of the input image. The red, green, and blue
     * bands are averaged independently. The code has been written to be
     * as efficient as possible. Borders are handled by duplicating the
     * first or last row, and replacing the first and last column
     * with 0, when doing the average.
     *
     * @param imageInput the input image
     * @throws jjil.core.Error if imageInput is not an RgbImage
     */
    public void push(Image imageInput) throws jjil.core.Error 
   {
        if (!(imageInput instanceof RgbImage)) 
        {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_RGBIMAGE,
                			imageInput.toString(),
                			null,
                			null);
        }
        
        int cWidth  = imageInput.getWidth();
        int cHeight = imageInput.getHeight();
        int rgbInput[] = ((RgbImage)imageInput).getData();
        
        RgbImage imageResult = new RgbImage(cWidth, cHeight);
        int[] rgbOutput = imageResult.getData();
        
        for(int i=0;i<cHeight;i++) {
            /* declare and initialize integers which will hold the r, g, and b
             * pixel values. The variables are named and numbered as if
             * they were array indices for three different 3x3 arrays.
             * They are set to -128 because this represents black in the
             * signed byte representation of a pixel.
             */
            byte r00 = -128, r01 = -128, r02 = -128;
            byte g00 = -128, g01 = -128, g02 = -128;
            byte b00 = -128, b01 = -128, b02 = -128;
            byte r10 = -128, r11 = -128, r12 = -128;
            byte g10 = -128, g11 = -128, g12 = -128;
            byte b10 = -128, b11 = -128, b12 = -128;
            byte r20 = -128, r21 = -128, r22 = -128;
            byte g20 = -128, g21 = -128, g22 = -128;
            byte b20 = -128, b21 = -128, b22 = -128;
          
            /* set column indices into this row
             * for first row use row 0 instead of row -1
             * for last row use row cHeight-1 instead of row cHeight
             */
            int pos0 = (i==0) ? 0 : (i-1) * cWidth;
            int pos1 = i * cWidth;
            int pos2 = (i==cHeight-1) ? i * cWidth : (i+1) * cWidth;
            
            /* initialize the (*,2) variables so the initial step to
             * the right does the right thing.
             */
            r02 = RgbVal.getR(rgbInput[pos0]);
            g02 = RgbVal.getG(rgbInput[pos0]);
            b02 = RgbVal.getB(rgbInput[pos0]);
            r12 = RgbVal.getR(rgbInput[pos1]);
            g12 = RgbVal.getG(rgbInput[pos1]);
            b12 = RgbVal.getB(rgbInput[pos1]);
            r22 = RgbVal.getR(rgbInput[pos2]);
            g22 = RgbVal.getG(rgbInput[pos2]);
            b22 = RgbVal.getB(rgbInput[pos2]);
            
            for(int j=0;j<cWidth;j++) {
                
                /* move one step to the right
                 */
                r00 = r01;
                r01 = r02;
                g00 = g01;
                g01 = g02;
                b00 = b01;
                b01 = b02;
                
                r10 = r11;
                r11 = r12;
                g10 = g11;
                g11 = g12;
                b10 = b11;
                b11 = b12;
                
                r20 = r21;
                r21 = r22;
                g20 = g21;
                g21 = g22;
                b20 = b21;
                b21 = b22;
                
                /* get new RGB pixel value. 
                 * In this code the r, g, or b value is treated as an
                 * unsigned value from 0 to 255, rather than as a signed
                 * value from -128 to 127, as it is in the byte image code.
                 * This is mathematically equivalent for averaging and 
                 * requires less computation than doing sign extension.
                 */
                if (j < cWidth-1) {
                    r02 = RgbVal.getR(rgbInput[pos0+1]);
                    g02 = RgbVal.getG(rgbInput[pos0+1]);
                    b02 = RgbVal.getB(rgbInput[pos0+1]);
                    r12 = RgbVal.getR(rgbInput[pos1+1]);
                    g12 = RgbVal.getG(rgbInput[pos1+1]);
                    b12 = RgbVal.getB(rgbInput[pos1+1]);
                    r22 = RgbVal.getR(rgbInput[pos2+1]);
                    g22 = RgbVal.getG(rgbInput[pos2+1]);
                    b22 = RgbVal.getB(rgbInput[pos2+1]);
                } else {
                    /* we use black (-128) as the border in the last column 
                     */
                    r02 = g02 = b02 = r12 = g12 = b12 =
                            r22 = g22 = b22 = -128;
                }
                
                /* calculate average r, g, and b values
                 */
                byte r = (byte) ((r00 + r01 + r02 + 
                                  r10 + r11 + r12 + 
                                  r20 + r21 + r22) / 9);
                byte g = (byte) ((g00 + g01 + g02 + 
                                  g10 + g11 + g12 + 
                                  g20 + g21 + g22) / 9);
                byte b = (byte) ((b00 + b01 + b02 + 
                                  b10 + b11 + b12 + 
                                  b20 + b21 + b22) / 9);
                /* note r, g, and b will always be between 0 and 255 so
                 * it is not necessary to mask etc.
                 */
                rgbOutput[pos1] = RgbVal.toRgb(r, g, b);
                /* advance column indices to next position
                 */
                pos0++;
                pos1++;
                pos2++;
            }
        }
        /* send output to PipelineStage
         */
        super.setOutput(imageResult);
    }
}
