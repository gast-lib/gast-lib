/*
 * MaskedGray32SubImgGen.java
 *
 * Given a target image size and a horizontal and vertical offset
 * generates a series of subimages within the input image,
 * each subimage offset by an integral multiple of the 
 * offset with size equal to the target size and lying 
 * entirely within the original image. The offset of the
 * subimage in the input image is given in the subimage class.
 * In this masked version only subimages whose center is not a masked
 * point (mask image value = 0) will be generated.
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
import jjil.core.Gray32MaskedImage;
import jjil.core.Gray32OffsetImage;
import jjil.core.Gray8OffsetImage;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Generates subimages from a source Gray32Image, using a mask. In a normal
 * subimage generator subimages are generated evenly spaced across the input
 * image. Here, the subimage is generated only if is center point is not masked.
 * This can increase processing speed in hierarchical detection operations since features
 * detected at coarser resolution don't have to be redetected at finer resolution.
 * @author webb
 */
public class MaskedGray32SubImgGen extends PipelineStage {
    Gray32MaskedImage imageInput;  // input image
    boolean oSubImageReady; // true if sub image position is OK
    int nHeight;            // target height
    int nHorizLimit = 0;    // number of subimages generated horizontally
    int nVertLimit = 0;     // number of subimages generated vertically
    int nHorizIndex = 0;    // current subimiage index, horizontal
    int nVertIndex = 0;     // current subimage index, vertical
    int nWidth;             // target width
    int nXOffset;           // x offset multiple for subimages
    int nYOffset;           // y offset multiple for subimages
    
    /**
     * Creates a new instance of MaskedGray32SubImgGen.
     * @param nWidth the width of the generated subimage.
     * @param nHeight the height of the generated subimage.
     * @param nXOffset the horizontal offset between subimages.
     * @param nYOffset the vertical offset between subimages.
     */
    public MaskedGray32SubImgGen(int nWidth, int nHeight, int nXOffset, int nYOffset) {
        this.nWidth = nWidth;
        this.nHeight = nHeight;
        this.nXOffset = nXOffset;
        this.nYOffset = nYOffset;
        this.oSubImageReady = false;
        // create an output image. We'll reuse this
        // image, changing the contents and offset,
        // for every Gray8OffsetImage we output.
        super.imageOutput = new Gray8OffsetImage( 
            this.nWidth, 
            this.nHeight, 
            0, 
            0);
    }
    
    /** advanceToNextSubImage advances to the next position for generating
     * a subimage. It returns true iff there is a non-masked position within
     * the image where a subimage can be generated.
     */
    private boolean advanceToNextSubImage() {
        if (oSubImageReady) {
            return this.nVertIndex <= this.nVertLimit;
        }
        // advance to next subimage position
        this.nHorizIndex ++;
        if (this.nHorizIndex > this.nHorizLimit) {
            this.nVertIndex ++;
            this.nHorizIndex = 0;
        }
        // nPos is the byte address that we will test in the mask image
        // to see if it is OK to generate a subimage. It is the midpoint
        // of the subimage.
        int nPos = (this.nHeight / 2 + 
                this.nVertIndex * this.nYOffset) * this.imageInput.getWidth() +
                this.nWidth / 2 + this.nHorizIndex * this.nXOffset;
        // starting at the current position, search forward for a position
        // that is not masked.
        while (this.nVertIndex <= this.nVertLimit) {
            while (this.nHorizIndex <= this.nHorizLimit) {
                if (this.imageInput.getMaskData()[nPos] == Byte.MIN_VALUE) {
                    // found it
                    oSubImageReady = true;
                    return true;
                }
                this.nHorizIndex ++;
                nPos += this.nWidth;
            }
            this.nHorizIndex = 0;
            this.nVertIndex ++;
            // reset nPos for next row of subimages
            nPos = (this.nHeight / 2 + 
                this.nVertIndex * this.nYOffset) * this.imageInput.getWidth() +
                this.nWidth / 2;
        }
        this.oSubImageReady = true;
        return false;
    }
    
    // We are done producing images when the advance returns no more images
    /**
     * Returns true iff there is another image available from getFront().
     * Note that the existence of another image from
     * MaskedGray32SubImgGen depends on the mask image so there's no way to guarantee
     * there will be even one subimage generated for a particular input. You must
     * always call isEmpty().
     * @return true iff there is another image available from getFront().
     */
    public boolean isEmpty() {
        return !advanceToNextSubImage();
     }
    
    // Return the next subimage and increment the indices
    /**
     * Returns the next subimage generated. The subimage will have its offset
     * set to indicate where it was generated in the input image.
     * @return a MaskedGray8SubImage that is the next subimage in the input Gray8Image to
     * be processed.
     * @throws jjil.core.Error if no subimage is available (you have to call isEmpty() to determine if
     * a subimage is available. As few as 0 subimage can be generated for a
     * given input if the entire image is masked.) Also throws if the output
     * image (stored in the superclass) has been changed in type.
     */
     public Image getFront() throws jjil.core.Error
    {
        // reuse output image
        // check to make sure nobody damaged it somehow
        if (!(super.imageOutput instanceof Gray32OffsetImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                            super.imageOutput.toString(),
                            "Gray32SubImage",
                            null);
        }
        if (!advanceToNextSubImage()) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.SUBIMAGE_NO_IMAGE_AVAILABLE,
                            this.toString(),
                            null,
                            null);
        }
        
        int[] dataIn = this.imageInput.getData();
        // offset of first pixel of the subimage within the
        // larget image.
        int nHOffset = this.nXOffset * this.nHorizIndex;
        int nVOffset = this.nYOffset * this.nVertIndex;
        Gray32OffsetImage imageResult = (Gray32OffsetImage) super.imageOutput;
        imageResult.setXOffset(nHOffset);
        imageResult.setYOffset(nVOffset);
        int[] dataOut = imageResult.getData();
        // don't access outside the image
        int nLimitY = Math.min(this.imageInput.getHeight() - nVOffset, this.nHeight);
        int nLimitX = Math.min(this.imageInput.getWidth() - nHOffset, this.nWidth);
        for (int i=0; i<nLimitY; i++) {
            int nVInLoc = i + nVOffset;
            System.arraycopy( 
                    dataIn, 
                    nVInLoc*this.imageInput.getWidth() + nHOffset, 
                    dataOut, 
                    i*this.nWidth, 
                    nLimitX);
        }

        this.oSubImageReady = false;
        return imageResult;
    }

    
    /**
     * Accepts a new MaskedGray8Image and initializes all the generator indices.
     * @param image The input MaskedGray8Image.
     * @throws jjil.core.Error if the input is not of type MaskedGray8Image or is smaller than the
     * subimages to be generated.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray32MaskedImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                            image.toString(),
                            "Gray32MaskedImage",
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
        this.imageInput = (Gray32MaskedImage) image;
        // we want to find the largest integer l such that
        // (l-1) * w + w  <= iw 
        // where l = computed limit on index
        // w = subimage width or height
        // iw = image width or height
        // or l = (iw - w) / w  + 1 (truncated)
        // Java division truncates
        this.nHorizLimit = (image.getWidth() - this.nWidth) / this.nXOffset;
        this.nVertLimit = (image.getHeight() - this.nHeight) / this.nYOffset;
        this.nHorizIndex = -1; // first time through increments
        this.nVertIndex = 0;
        this.oSubImageReady = false;
    }
    
}
