/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Copyright 2008 by Jon pA. Webb
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR pA PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the Lesser GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbMaskedImage;
import jjil.core.RgbVal;

/**
 * Compute the Gray8Image that is the sum of absolute differences between
 * the background RgbMaskedImage specified in the constructor and the 
 * input RgbImage, in the unmasked areas only. The output in the unmasked areas
 * is Byte.MIN_VALUE.
 * @author webb
 */
public class RgbMaskedAbsDiff extends PipelineStage {
    private RgbMaskedImage rgbBack;

    /**
     * Set background image.
     * @param rgbBack background RgbImage.
     */
    public RgbMaskedAbsDiff(RgbMaskedImage rgbBack) {
        this.rgbBack = rgbBack;
    }
    
    /**
     * Process a foreground RgbMaskedImage and produce a Gray8Image in which each
     * pixel is the sum of absolute differences between the foreground and
     * background, in the masked areas. Outside the masked areas the output
     * is Byte.MIN_VALUE.
     * @param imInput input RgbImage
     * @throws jjil.core.Error if imInput is not an RgbImage or is not the same
     * size as the background image set in the constructor.
     */
    public void push(Image imInput) throws Error {
        {
        if (!(imInput instanceof RgbMaskedImage)) 
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                			imInput.toString(),
                			"RgbMaskedImage",
                			null);
        }
        if (imInput.getWidth() != this.rgbBack.getWidth() ||
        	imInput.getHeight() != this.rgbBack.getHeight()) {
        	throw new Error(
        				Error.PACKAGE.ALGORITHM,
        				ErrorCodes.IMAGE_SIZES_DIFFER,
        				imInput.toString(),
        				this.rgbBack.toString(),
        				null);
        
        }
        RgbMaskedImage rgbInput = (RgbMaskedImage)imInput;
        int wInput[] = rgbInput.getData();
        int wBack[] = this.rgbBack.getData();
        Gray8Image grayOut = new Gray8Image(
                this.rgbBack.getWidth(), 
                this.rgbBack.getHeight());        
        byte bGray[] = grayOut.getData();
        for (int i=0; i<imInput.getHeight(); i++) {
            for (int j=0; j<imInput.getWidth(); j++) {
                if (!rgbInput.isMasked(i, j) && !this.rgbBack.isMasked(i,j)) {
                    int rIn = RgbVal.getR(wInput[i*grayOut.getWidth()+j]);
                    int gIn = RgbVal.getG(wInput[i*grayOut.getWidth()+j]);
                    int bIn = RgbVal.getB(wInput[i*grayOut.getWidth()+j]);
                    int rBack = RgbVal.getR(wBack[i*grayOut.getWidth()+j]);
                    int gBack = RgbVal.getG(wBack[i*grayOut.getWidth()+j]);
                    int bBack = RgbVal.getB(wBack[i*grayOut.getWidth()+j]);
                    int gRes = Math.abs(rIn-rBack) +
                            Math.abs(gIn-gBack) +
                            Math.abs(bIn-bBack);
                    bGray[i*grayOut.getWidth()+j] = 
                            (byte) Math.min(gRes, Byte.MAX_VALUE);
                } else {
                    bGray[i] = Byte.MIN_VALUE;
                }
            }
       }
       super.setOutput(grayOut);
    }

    /**
     * Implement toString, providing the background image information.
     * @return a string consisting of this class name followed by the
     * background image description.
     */
    public String toString() {
        return super.toString() + "(" + this.rgbBack.toString() + ")";
    }
}
