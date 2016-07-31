/*
 * Gray8Reduce.java
 *
 * Created on September 2, 2006, 3:59 PM
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

/**
 * Pipeline stage reduces an image's size by rectangular averaging. The
 * reduction factor must evenly divide the image size. No
 * smoothing is done.
 *
 * @author webb
 */
public class Gray8Reduce extends PipelineStage {
    private int cReduceHeight;
    private int cReduceWidth;
    
    /**
     * Creates a new instance of Gray8Reduce.
     * @param cReduceWidth amount to reduce the width by
     * @param cReduceHeight amount to reduce the height by
     * @throws jjil.core.Error if the reduce width or height is less than or equal to zero.
     */
    public Gray8Reduce(int cReduceWidth, int cReduceHeight) 
    	throws jjil.core.Error {
        setReductionFactor(cReduceWidth, cReduceHeight);
    }
    
    /** Reduces a gray image by a factor horizontally and vertically through
     * averaging. The reduction factor must be an even multiple of the image
     * size.
     *
     * @param image the input image.
     * @throws jjil.core.Error if the input image is not gray, or
     * the reduction factor does not evenly divide the image size.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray8Image)) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
            				image.toString(),
            				null,
            				null);
        }
        if (image.getWidth() % this.cReduceWidth != 0) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
            				image.toString(),
            				null,
            				null);
        }
        if (image.getHeight() % this.cReduceHeight != 0) {
            throw new Error(
            		Error.PACKAGE.ALGORITHM,
            		ErrorCodes.REDUCE_INPUT_IMAGE_NOT_MULTIPLE_OF_OUTPUT_SIZE,
            		image.toString(),
            		this.toString(),
            		null);
        }
        Gray8Image gray = (Gray8Image) image;
        byte[] bIn = gray.getData();
        int cReducedHeight = image.getHeight() / this.cReduceHeight;
        int cReducedWidth = image.getWidth() / this.cReduceWidth;
        Gray8Image result = new Gray8Image(cReducedWidth, cReducedHeight);
        byte[] bOut = result.getData();
        for (int i=0; i<cReducedHeight; i++) {
            for (int j=0; j<cReducedWidth; j++) {
                int sum = 0;
                for (int k=0; k<this.cReduceHeight; k++) {
                    for (int l=0; l<this.cReduceWidth; l++) {
                        sum += bIn[(i*this.cReduceHeight+k) * image.getWidth() +
                                (j*this.cReduceWidth + l)];
                    }
                }
                bOut[i*cReducedWidth + j] = 
                        (byte) (sum / (this.cReduceHeight * this.cReduceWidth));
            }
        }
        super.setOutput(result);
    }
    
    /** Returns the height reduction factor.
     *
     * @return the height reduction factor.
     */
    public int getHeightReduction()
    {
        return this.cReduceHeight;
    }
    
    /** Returns the width reduction factor.
     *
     * @return the width reduction factor.
     */
    public int getWidthReduction()
    {
        return this.cReduceWidth;
    }
    
    /** Sets a new width, height reduction factor.
     *
     * @param cReduceWidth the amount by which to reduce the image width.
     * @param cReduceHeight the amount by which to reduce the image height.
     * @throws jjil.core.Error if either cReduceWidth or cReduceHeight
     * is less than or equal to 0.
     */
    public void setReductionFactor(int cReduceWidth, int cReduceHeight) 
        throws jjil.core.Error {
        if (cReduceWidth <= 0 || cReduceHeight <= 0) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
            				new Integer(cReduceWidth).toString(),
            				new Integer(cReduceHeight).toString(),
            				null);
        }
        this.cReduceWidth = cReduceWidth;
        this.cReduceHeight = cReduceHeight;
    }

    /** Return a string describing the reduction operation.
     *
     * @return the string describing the reduction operation.
     */
    public String toString() {
        return super.toString() + " (" + this.cReduceWidth + "," +  //$NON-NLS-1$ //$NON-NLS-2$
                this.cReduceHeight + ")"; //$NON-NLS-1$
    }
}
