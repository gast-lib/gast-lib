/*
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
 * Thresholds an RgbImage based on a given RGB value and another
 * RGB value that represents the masimum RGB distance from the
 * first value that a pixel can lie in to be considered within
 * the threshold.
 * The computation is analogous to a vector distance from a given
 * point. It can be described as </br>
 *  (rgb - rgbTarget) &middot rgbVec  &lt; threshold
 * @author webb
 */
public class RgbVecThresh extends PipelineStage {
    private int nR, nG, nB;
    private int nRVec, nGVec, nBVec;
    private int nThresh;
    private boolean bWithin;
    
    /**
     * Initialize class to filter based on given target rgb value
     * and vector distance from the target value measured along
     * a particular vector. The bWithin parameter allows the 
     * threshold test to be reversed -- bWithin = false causes
     * true (Byte.MAX_VALUE) to be output for rgb values &gt; than
     * the test.
     * @param rgbTarget target RGB value
     * @param rgbVec direction and distance of test
     * @param nThresh threshold to compare with
     * @param bWithin if true result is true if &lt; test; if false,
     * result is true if &gt; than the test.
     */
    public RgbVecThresh(int rgbTarget, int rgbVec, int nThresh, boolean bWithin) {
        this.nR = RgbVal.getR(rgbTarget) - Byte.MIN_VALUE;
        this.nG = RgbVal.getG(rgbTarget) - Byte.MIN_VALUE;
        this.nB = RgbVal.getB(rgbTarget) - Byte.MIN_VALUE;
        this.nRVec = RgbVal.getR(rgbVec);
        this.nGVec = RgbVal.getG(rgbVec);
        this.nBVec = RgbVal.getB(rgbVec);
        this.nThresh = nThresh;
        this.bWithin = bWithin;
    }

    /**
     * Perform thresholding operation. Output is a gray image.
     * @param imageInput input RgbImage to be thresholded.
     * @throws jjil.core.Error if input is not an RgbImage.
     */
    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof RgbImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_RGBIMAGE,
                            imageInput.toString(),
                            null,
                            null);
        }
        RgbImage rgb = (RgbImage) imageInput;
        int[] nData = rgb.getData();
        Gray8Image imageResult = new Gray8Image(rgb.getWidth(), rgb.getHeight());
        byte[] bData = imageResult.getData();
        for (int i=0; i<rgb.getWidth()*rgb.getHeight(); i++) {
            int nRCurr = RgbVal.getR(nData[i]) - Byte.MIN_VALUE;
            int nGCurr = RgbVal.getG(nData[i]) - Byte.MIN_VALUE;
            int nBCurr = RgbVal.getB(nData[i]) - Byte.MIN_VALUE;
            int nRDiff = nRCurr - this.nR;
            int nGDiff = nGCurr - this.nG;
            int nBDiff = nBCurr - this.nB;
            int nDot = Math.abs(nRDiff * this.nRVec + 
                    nGDiff * this.nGVec +
                    nBDiff * this.nBVec);
            bData[i] = ((nDot < this.nThresh) == this.bWithin)
                    ? Byte.MAX_VALUE : Byte.MIN_VALUE;
            
        }
        super.setOutput(imageResult);
    }

}
