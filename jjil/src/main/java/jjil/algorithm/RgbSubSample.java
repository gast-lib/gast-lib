/*
 * RgbSubSample.java
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
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;

/**
 * Pipeline stage reduces an RgbImage's size by subsampling WITHOUT
 * smoothing. This results in an aliased image but can be
 * done very quickly and the artifacts resulting from the subsampling can
 * themselves be useful for detection of certain kinds of objects such
 * as barcodes. This pipeline stage should be used with caution because
 * of the artifacts introduced by subsampling without smoothing.
 *
 * @author webb
 */
public class RgbSubSample extends PipelineStage {
    private int cTargetHeight;
    private int cTargetWidth;
    
    /**
     * Creates a new instance of RgbSubSample.
     * @param cTargetWidth the new width
     * @param cTargetHeight the new height
     * @throws jjil.core.Error if the target width or height is less than or equal to zero.
     */
    public RgbSubSample(int cTargetWidth, int cTargetHeight)
    	throws jjil.core.Error {
        setTargetSize(cTargetWidth, cTargetHeight);
    }
    
    /** Reduces an RgbImage by a factor horizontally and vertically through
     * averaging. The reduction factor must be an even multiple of the image
     * size.
     *
     * @param image the input image.
     * @throws jjil.core.Error if the input image is not gray, or
     * the reduction factor does not evenly divide the image size.
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
        if (image.getWidth() < this.cTargetWidth) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.SHRINK_OUTPUT_LARGER_THAN_INPUT,
                			image.toString(),
                			this.toString(),
                			null);
        }
        if (image.getHeight() < this.cTargetHeight) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.SHRINK_OUTPUT_LARGER_THAN_INPUT,
                			image.toString(),
                			this.toString(),
                			null);
        }
        // note that Java division truncates. So 
        // e.g. cReduceWidth * this.cTargetWidth <= image.getWidth
        // This is important in the for loop below to keep from out of bounds
        // array access.
        int cReduceWidth = image.getWidth() / this.cTargetWidth;
        int cReduceHeight = image.getHeight() / this.cTargetHeight;
        RgbImage rgb = (RgbImage) image;
        int[] rnIn = rgb.getData();
        RgbImage result = new RgbImage(
        		this.cTargetWidth, 
        		this.cTargetHeight);
        int[] rnOut = result.getData();
        for (int i=0; i<this.cTargetHeight; i++) {
            for (int j=0; j<this.cTargetWidth; j++) {
                rnOut[i*this.cTargetWidth + j] = 
                	rnIn[(i*image.getWidth()*cReduceHeight) + (j*cReduceWidth)];
            }
        }
        super.setOutput(result);
    }
    
    /** Returns the target height.
     *
     * @return the target height.
     */
    public int getTargetHeight()
    {
        return this.cTargetHeight;
    }
    
    /** Returns the target width.
     *
     * @return the target width.
     */
    public int getTargetWidth()
    {
        return this.cTargetWidth;
    }
    
    /** Sets a new width, height target size.
     *
     * @param cTargetWidth the target image width.
     * @param cTargetHeight the target image height.
     * @throws jjil.core.Error if either cTargetWidth or cTargetHeight
     * is less than or equal to 0.
     */
    public void setTargetSize(int cTargetWidth, int cTargetHeight) 
        throws jjil.core.Error {
        if (cTargetWidth <= 0 || cTargetHeight <= 0) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
                			new Integer(cTargetWidth).toString(),
                			new Integer(cTargetHeight).toString(),
                			null);
        }
        this.cTargetWidth = cTargetWidth;
        this.cTargetHeight = cTargetHeight;
    }

    /** Return a string describing the reduction operation.
     *
     * @return the string describing the reduction operation.
     */
    public String toString() {
        return super.toString() + " (" + this.cTargetWidth + "," +  //$NON-NLS-1$ //$NON-NLS-2$
                this.cTargetHeight + ")"; //$NON-NLS-1$
    }
}
