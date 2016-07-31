/*
 * Gray8Shrink.java.
 *    Reduces an image to a new size by averaging the pixels nearest each
 * target pixel's pre-image. This is done in two passes, first averaging
 * horizontally and then vertically. The horizontal average is done with the
 * pixel values shifted 8 bits for accuracy.
 *
 * Created on October 13, 2007, 2:21 PM
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
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
/**
 * Shrinks an input Gray8Image to a given new size. The shrinking is done with
 * proper averaging so each output pixel is the average of the corresponding
 * rectangular region in the input.
 * @author webb
 */
public class Gray8Shrink extends PipelineStage {
    private int cHeight;
    private int cWidth;
    
    /** Creates a new instance of GrayRectStretch. 
     *
     * @param cWidth new image width
     * @param cHeight new image height
     * @throws jjil.core.Error if either is less than or equal to zero.
     */
    public Gray8Shrink(int cWidth, int cHeight) 
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
    
    /**
     * Process an input Gray8Image, producing a new shrunk output image.
     * @param image The input Gray8Image.
     * @throws jjil.core.Error if input is not a Gray8Image, or the input image size is smaller (either 
     * horizontally or vertically) than the desired size.
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
        if (image.getWidth() < this.cWidth || image.getHeight() < this.cHeight) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.SHRINK_OUTPUT_LARGER_THAN_INPUT,
                			image.toString(),
                			this.toString(),
                			null);
        }
        Gray8Image input = (Gray8Image) image;
        /* horizontal shrink */
        Gray32Image horiz = shrinkHoriz(input);
        /* vertical shrink */
        Gray8Image result = shrinkVert(horiz);
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
                			this.toString(),
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
                			this.toString(),
                			null);
        }
        this.cWidth = cWidth;
    }
    
    /** Horizontal shrink. Shrinks the input from
     * (input.getWidth(),input.getHeight()) to (this.cWidth,input.getHeight())
     * by averaging the pixels that are nearest each target pixel's pre-image.
     * The mapping is x -> x * this.cWidth / input.getWidth() or in inverse
     *                y -> y * input.getWidth() / this.cWidth
     *
     * @param input the input image
     * @return the shrunk image
     */
    private Gray32Image shrinkHoriz(Gray8Image input) {
        /* horizontal shrink */
        Gray32Image horiz = new Gray32Image(this.cWidth, input.getHeight());
        byte[] inData = input.getData();
        int[] outData = horiz.getData();
        int[] nPixelSum = new int[input.getHeight()];
        for (int i=0; i<input.getHeight(); i++) {
            nPixelSum[i] = 0;
        }
        int nPos = 0;
        int nNextPos = input.getWidth() * 256 / this.cWidth;
        int nCount = 0;
        for (int j=0; j<input.getWidth() && nPos < this.cWidth; j++) {
            for (int i=0; i<input.getHeight(); i++) {
                nPixelSum[i] += inData[i*input.getWidth() + j] - Byte.MIN_VALUE;
            }
            nCount += 1;
            if ((j+1) * 256 >= nNextPos) {
                for (int i=0; i<input.getHeight(); i++) {
                    outData[i*this.cWidth + nPos] = nPixelSum[i] * 256 / nCount;
                    nPixelSum[i] = 0;
               }
               nPos += 1;
               nNextPos += input.getWidth() * 256 / this.cWidth;
               nCount = 0;
            }
        }
        return horiz;
    }
    
    /** Vertical shrink. Shrinks an image from
     * (this.cWidth,input.getHeight()) to (this.cWidth,this.cHeight)
     * by averaging the pixels that are nearest each target pixel's pre-image.
     * The mapping is x -> x * this.cHeight / input.getHeight() or in inverse
     *                y -> y * input.getHeight() / this.cHeight
     *
     * @param input the input image.
     * @returns the shrunk image.
     */
    private Gray8Image shrinkVert(Gray32Image input) {
        /* vertical shrink */
        Gray8Image vert = new Gray8Image(input.getWidth(), this.cHeight);
        int[] inData = input.getData();
        byte[] outData = vert.getData();
        int[] nPixelSum = new int[input.getWidth()];
        for (int i=0; i<input.getWidth(); i++) {
            nPixelSum[i] = 0;
        }
        int nPos = 0;
        int nNextPos = input.getHeight() * 256 / this.cHeight;
        int nCount = 0;
        for (int j=0; j<input.getHeight() && nPos < input.getHeight(); j++) {
            for (int i=0; i<input.getWidth(); i++) {
                nPixelSum[i] += inData[j*input.getWidth() + i];
            }
            nCount += 1;
            if ((j+1) * 256 >= nNextPos) {
                for (int i=0; i<input.getWidth(); i++) {
                    outData[nPos*input.getWidth() + i] = 
                        (byte) (nPixelSum[i] / nCount / 256 + Byte.MIN_VALUE);
                    nPixelSum[i] = 0;
               }
               nPos += 1;
               if (nPos >= this.cHeight) {
                   break;
               }
               nNextPos += input.getHeight() * 256 / this.cHeight;
               nCount = 0;
            }
        }
        return vert;
    }
     
    
        
    /** Return a string describing the shrinking operation.
     *
     * @return the string describing the shrinking operation.
     */
    public String toString() {
        return super.toString() + " (" + this.cWidth + "," + this.cHeight + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
