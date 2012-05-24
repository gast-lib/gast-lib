/*
 * Gray8SubImageGenerator.java
 *
 * Given a target image size and a horizontal and vertical offset
 * generates a series of subimages within the input image,
 * each subimage offset by an integral multiple of the 
 * offset with size equal to the target size and lying 
 * entirely within the original image. The offset of the
 * subimage in the input image is given in the subimage class.
 *
 * Created on July 1, 2007, 1:51 PM
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
import jjil.core.Gray8OffsetImage;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Generates sub images (cropped images positioned regularly across the input image) 
 * from an input Gray8Image. The subimages are of type Gray8OffsetImage which makes it
 * possible to determine their location in the original input image.
 * @author webb
 */
public class Gray8SubImageGenerator extends PipelineStage {
    Gray8Image imageInput;  // input image
    int nHeight;            // target height
    int nHorizLimit = 0;    // number of subimages generated horizontally
    int nVertLimit = 0;     // number of subimages generated vertically
    int nHorizIndex = 0;    // current subimiage index, horizontal
    int nVertIndex = 0;     // current subimage index, vertical
    int nWidth;             // target width
    int nXOffset;           // x offset multiple for subimages
    int nYOffset;           // y offset multiple for subimages
    
    /**
     * Creates a new instance of Gray8SubImageGenerator. The parameters specify the 
     * size and spacing of the subimages. For example (20,30,10,15) generates
     * 20x30 subimages, spaced every 10 pixels horizontally and 15 pixels vertically.
     * @param nWidth The width of the generated subimage.
     * @param nHeight The height of the generated subimage.
     * @param nXOffset The horizontal offset from one subimage to the next.
     * @param nYOffset The vertical offset from one subimage to the next.
     */
    public Gray8SubImageGenerator(int nWidth, int nHeight, int nXOffset, int nYOffset) {
        this.nWidth = nWidth;
        this.nHeight = nHeight;
        this.nXOffset = nXOffset;
        this.nYOffset = nYOffset;
        // create an output image. We'll reuse this
        // image, changing the contents and offset,
        // for every Gray8OffsetImage we output.
        super.imageOutput = new Gray8OffsetImage( 
            this.nWidth, 
            this.nHeight, 
            0, 
            0);
    }
    
    // We are done producing images when the last row is done
    /**
     * Returns true when no more subimages are available from the input image.
     * isEmpty() should be called before getFront() to verify that subimages are available
     * since it is an error to call getFront() when isEmpty() is true.
     * @return true when no more subimages are available. 
     */
    public boolean isEmpty() {
        return this.nVertIndex == this.nVertLimit;
    }
    
    // Return the next subimage and increment the indices
    /**
     * Returns the next subimage.
     * @return a subimage within the input image, of type Gray8OffsetImage.
     * @throws jjil.core.Error when there are no more subimages available (isEmpty() would return
     * true.)
     */
     public Image getFront() throws jjil.core.Error
    {
        // offset of first pixel of the subimage within the
        // larget image.
        int nHOffset = this.nXOffset * this.nHorizIndex;
        int nVOffset = this.nYOffset * this.nVertIndex;
        byte[] dataIn = this.imageInput.getData();
        // reuse output image
        // check to make sure nobody damaged it somehow
        if (!(super.imageOutput instanceof Gray8OffsetImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                            imageOutput.toString(),
                            null,
                            null);
        }
        Gray8OffsetImage imageResult = (Gray8OffsetImage) super.imageOutput;
        imageResult.setXOffset(nHOffset);
        imageResult.setYOffset(nVOffset);
        byte[] dataOut = imageResult.getData();
        for (int i=0; i<this.nHeight; i++) {
            int nVInLoc = i + nVOffset;
            System.arraycopy( 
                    dataIn, 
                    nVInLoc*this.imageInput.getWidth() + nHOffset, 
                    dataOut, 
                    i*this.nWidth, 
                    this.nWidth);
        }
        this.nHorizIndex ++;
        if (this.nHorizIndex == this.nHorizLimit) {
            this.nVertIndex ++;
            this.nHorizIndex = 0;
        }
        return imageResult;
    }

    
    /**
     * Reinitializes the subimage generator and prepares it to generate the first
     * Gray8OffsetImage for the new input.
     * @param image The new input image (which must be of type Gray8Image).
     * @throws jjil.core.Error if image is not of type Gray8Image, or is too small
     * (less than the size of the subimages we're supposed to
     * be generating).
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
        if (image.getWidth() < this.nWidth || image.getHeight() < this.nHeight) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_TOO_SMALL,
            				image.toString(),
            				new Integer(this.nWidth).toString(),
            				new Integer(this.nHeight).toString());
        }
        this.imageInput = (Gray8Image) image;
        // we want to find the largest integer l such that
        // (l-1) * w + w  < iw 
        // where l = computed limit on index
        // w = subimage width or height
        // iw = image width or height
        // or l = iw / w (truncated)
        // Java division truncates
        this.nHorizLimit = (image.getWidth() - this.nWidth) / this.nXOffset;
        this.nVertLimit = (image.getHeight() - this.nHeight) / this.nYOffset;
        this.nHorizIndex = 0; 
        this.nVertIndex = 0;
    }
    
}
