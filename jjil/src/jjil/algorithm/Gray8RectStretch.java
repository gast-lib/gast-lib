/*
 * Gray8RectStretch.java
 *
 * Created on September 9, 2006, 8:08 AM
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

/** Pipeline stage stretches an image to a larger rectangular size with 
 * bilinear interpolation. For more information on this and other image
 * warps, see George Wolberg's excellent book, "Digital Image Warping",
 * Wiley-IEEE Computer Society Press, 1990.
 * @author webb
 */
public class Gray8RectStretch extends PipelineStage {
    private int cHeight;
    private int cWidth;
    
    /** Creates a new instance of Gray8RectStretch. 
     *
     * @param cWidth new image width
     * @param cHeight new image height
     * @throws jjil.core.Error if either is less than or equal to zero.
     */
    public Gray8RectStretch(int cWidth, int cHeight) 
        throws jjil.core.Error {
        setWidth(cWidth);
        setHeight(cHeight);
    }
    
    /** Gets current target height 
     *
     * @return current height
     */
    public int getHeight() {
        return this.cHeight;
    }
    
    /** Gets current target width
     *
     * @return current width
     */
    public int getWidth() {
        return this.cWidth;
    }
    
    /** Bilinear interpolation to stretch image to (cWidth, cHeight).
     * Does this in two passes, for more efficient computation.
     * 
     * @param image the input image
     * @throws jjil.core.Error if input image is not gray 8 bits,
     * or the input image size is larger than the target size. This class
     * does not do subsampling, only interpolation.
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
        if (image.getWidth() > this.cWidth || image.getHeight() > this.cHeight) {
            throw new Error(
			        		Error.PACKAGE.ALGORITHM,
			        		ErrorCodes.STRETCH_OUTPUT_SMALLER_THAN_INPUT,
			        		image.toString(),
			        		new Integer(this.cWidth).toString(),
			        		new Integer(this.cHeight).toString());
        }
        Gray8Image input = (Gray8Image) image;
        /** we do the stretch in to passes, one horizontal and the other
         * vertical. This leads to less computation than doing it in one
         * pass, because the calculation of the interpolation weights
         * has to happen only once. 
         */
        /* horizontal stretch */
        Gray8Image horiz = stretchHoriz(input);
        Gray8Image result = stretchVert(horiz);
        super.setOutput(result);
    }
    
    /** Changes target height
     * 
     * @param cHeight the new target height.
     * @throws jjil.core.Error if height is not positive
     */
    public void setHeight(int cHeight) throws jjil.core.Error {
        if (cHeight <= 0) {
            throw new Error(
			        Error.PACKAGE.ALGORITHM,
			        ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
			        new Integer(cHeight).toString(),
			        null,
			        null);
        }
        this.cHeight = cHeight;
    }
    
    /** Changes target width
     * 
     * @param cWidth the new target width.
     * @throws jjil.core.Error if height is not positive
     */
    public void setWidth(int cWidth) throws jjil.core.Error {
        if (cWidth <= 0) {
            throw new Error(
    				        Error.PACKAGE.ALGORITHM,
    				        ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
    				        new Integer(cWidth).toString(),
    				        null,
    				        null);
        }
        this.cWidth = cWidth;
    }
    
    /** Horizontal stretch. Stretches the input from
     * (input.getWidth(),input.getHeight()) to (this.cWidth,input.getHeight())
     *
     * @param input the input image
     * @return the stretched image
     */
    private Gray8Image stretchHoriz(Gray8Image input) {
        /* horizontal stretch */
        Gray8Image horiz = new Gray8Image(this.cWidth, input.getHeight());
        byte[] inData = input.getData();
        byte[] outData = horiz.getData();
        for (int j=0; j<this.cWidth; j++) {
            /* the interpolated position is
             * j*input.getWidth()/this.cWidth.
             * Compute the remainder of this division.
             */
            int cMod = (j*input.getWidth()) % this.cWidth;
            /* compute the left integral pixel position */
            int cX = (j*input.getWidth()-cMod) / this.cWidth;
            if (cX < input.getWidth()-1) {
                /* compute the fractional position in that pixel (* 256 so
                 * the computation can be done in integer */
                int cFrac = (cMod<<8) / this.cWidth;
                for (int i=0; i<input.getHeight(); i++) {
                    /* interpolate */
                    outData[i*this.cWidth+j] =
                     (byte) ((inData[i*input.getWidth()+cX] * (256-cFrac) +
                              inData[i*input.getWidth()+cX+1] * cFrac) >> 8);
                }
            } else {
                /* adjust cX so we will use the last column of the input
                 * image for interpolating beyond its right edge, without
                 * accessing a value outside the array bounds. In other
                 * words, for fractional input positions beyond input.getWidth()-1
                 * we use input.getWidth()-1 as the column.
                 */
                cX = input.getWidth() - 2;
                for (int i=0; i<input.getHeight(); i++) {
                    /* interpolate */
                    outData[i*this.cWidth+j] =
                     inData[i*input.getWidth()+cX+1];
                }
            }
        }
        return horiz;
    }
    
    /** Vertical stretch. Stretches an image from
     * (this.cWidth,input.getHeight()) to (this.cWidth,this.cHeight)
     *
     * @param input the input image.
     * @returns the stretched image.
     */
    private Gray8Image stretchVert(Gray8Image input) {
        byte[] inData = input.getData();
        Gray8Image vert = new Gray8Image(this.cWidth, this.cHeight);
        byte[] outData = vert.getData();
        for (int i=0; i<this.cHeight; i++) {
            /* remainder */
            int cMod = (i*input.getHeight()) % this.cHeight;
            /* top integral position */
            int cY = (i*input.getHeight()-cMod) / this.cHeight;
            if (cY < input.getHeight() - 1) {
                /* fractional position, times 256 */
                int cFrac = (cMod<<8) / this.cHeight;
                for (int j=0; j<input.getWidth(); j++) {
                    /* interpolate */
                    outData[i*this.cWidth+j] =
                     (byte) ((inData[cY*this.cWidth+j] * (256-cFrac) +
                              inData[(cY+1)*this.cWidth+j] * cFrac) >> 8);
                }
            } else {
                /* adjust cX so we will use the last row of the input
                 * image for interpolating beyond its bottom edge, without
                 * accessing a value outside the array bounds. In other
                 * words, for fractional input positions beyond input.getHeight()-1
                 * we use input.getHeight()-1 as the row.
                 */
                cY = input.getHeight() - 2;
                for (int j=0; j<input.getWidth(); j++) {
                    /* interpolate */
                    outData[i*this.cWidth+j] =
                     (inData[(cY+1)*this.cWidth+j]);
                }
            }
        }
        return vert;
    }
    
    /** Return a string describing the stretching operation.
     *
     * @return the string describing the stretching operation.
     */
    public String toString() {
        return super.toString() + " (" + this.cWidth + "," + this.cHeight + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
