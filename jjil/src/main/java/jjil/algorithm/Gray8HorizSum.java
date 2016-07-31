/*
 * 
 * Copyright 2008 by Jon A. Webb
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
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Sum a Gray8Image in a horizontal window, with the width controllable, creating
 * a Gray32Image.
 * @author webb
 */
public class Gray8HorizSum extends PipelineStage {
    int nSumWidth;
    
    /**
     * Initialize Gray8HorizSum. The width is set here.
     * @param nWidth width of the sum.
     */
    public Gray8HorizSum(int nWidth) {
        this.nSumWidth = nWidth;
    }

    /**
     * Sum a Gray8Image horizontally, creating a Gray32Image. 
     * The edges of the image (closer than width)
     * are set to 0. The summing is done efficiently so that each pixel computation
     * takes only 2 additions on average.<p>
     * This code uses Gray8QmSum to form a cumulative sum of the whole image.
     * Since the entire image is summed to a Gray32Image by Gray8QmSum overflow
     * may occur if
     * more thant 2**24 pixels are in the image (e.g., larger than 2**12x2**12 =
     * 4096x4096).
     * @param imageInput input Gray8Image.
     * @throws jjil.core.Error if the input is not a Gray8Image.
     */
    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof Gray8Image)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                            imageInput.toString(),
                            null,
                            null);
        }
        Gray8QmSum gqs = new Gray8QmSum();
        gqs.push(imageInput);
        Gray32Image gSum = (Gray32Image) gqs.getFront();
        int[] sData = gSum.getData();
        Gray32Image gResult = new Gray32Image(
                imageInput.getWidth(), 
                imageInput.getHeight());
        int[] gData = gResult.getData();
        for (int i=1; i<imageInput.getHeight(); i++) {
            for (int j=0; j<this.nSumWidth; j++) {
                gData[i*imageInput.getWidth()+j] = 0;
            }
            for (int j=nSumWidth; j<imageInput.getWidth(); j++) {
                gData[i*imageInput.getWidth()+j] =
                    sData[i*imageInput.getWidth()+j] -
                        sData[i*imageInput.getWidth()+j-this.nSumWidth];
            }
        }
        for (int i=1; i<imageInput.getHeight(); i++) {
            for (int j=0; j<this.nSumWidth; j++) {
                gData[i*imageInput.getWidth()+j] = 0;
            }
            for (int j=nSumWidth; j<imageInput.getWidth(); j++) {
                gData[i*imageInput.getWidth()+j] =
                    sData[i*imageInput.getWidth()+j] -
                        sData[i*imageInput.getWidth()+j-this.nSumWidth] -
                    sData[(i-1)*imageInput.getWidth()+j] +
                        sData[(i-1)*imageInput.getWidth()+j-this.nSumWidth];
            }
        }
        super.setOutput(gResult);
    }

}
