/*
 * Gray8MaskedImage.java
 *   Describes an 8-bit image together with its mask, in which any non-zero
 *   value is masked.
 *
 * Created on August 27, 2006, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
 *
 */

package jjil.core;

/**
 * Gray8MaskedImage is the image type used to store a signed
 * 8-bit image and its associated mask. Mask value = Byte.MIN_VALUE is considered
 * to be unmasked; all other values are masked.
 * @author webb
 */
public class Gray8MaskedImage extends Gray8Image {
    private final Gray8Image imMask;
    
    /**
     * Creates a new instance of Gray8MaskedImage
     * @param cWidth Width of the image (columns).
     * @param cHeight Height of the image (rows)
     */
    public Gray8MaskedImage(int cWidth, int cHeight) {
        super(cWidth, cHeight);
        this.imMask = new Gray8Image(cWidth, cHeight);
    }
    
    /**
     * Creates a new instance of Gray8MaskedImage from an existing
     * image and mask.
     * @param imData the data image.
     * @param imMask the mask
     * @throws jjil.core.Error If either input is not a Gray8Image or the sizes are not the same.
     */
    public Gray8MaskedImage(Gray8Image imData, Gray8Image imMask) 
        throws jjil.core.Error
    {
        super(imData.getWidth(), imData.getHeight());
        if (imData.getWidth() != imMask.getWidth() ||
            imData.getHeight() != imMask.getHeight()) {
            throw new Error(
            				Error.PACKAGE.CORE,
            				ErrorCodes.IMAGE_MASK_SIZE_MISMATCH,
            				imData.toString(),
            				imMask.toString(),
            				null);
        }
        System.arraycopy(
                imData.getData(),
                0,
                this.getData(),
                0,
                getWidth()*getHeight());
        this.imMask = imMask;
    }
    
    /**
     * Creates a new instance of Gray8MaskedImage from an existing
     * image, setting everything unmasked.
     * @param imData the data image.
     * @param imMask the mask
     * @throws jjil.core.Error If either input is not a Gray8Image or the sizes are not the same.
     */
    public Gray8MaskedImage(Gray8Image imData) 
        throws jjil.core.Error
    {
        super(imData.getWidth(), imData.getHeight());
        System.arraycopy(
                imData.getData(),
                0,
                this.getData(),
                0,
                getWidth()*getHeight());
        // initialize mask, setting everything unmasked
        this.imMask = new Gray8Image(
                imData.getWidth(), 
                imData.getHeight(), 
                Byte.MIN_VALUE);
    }

    /**
     * Copy this image
     * @return the image copy.
     */
    public Object clone()
    {
        Gray8MaskedImage image = new Gray8MaskedImage(
        		this.getWidth(), this.getHeight());
        System.arraycopy(
                this.getImage().getData(),
                0,
                image.getImage().getData(),
                0,
                getWidth()*getHeight());
        System.arraycopy(
                this.getMask().getData(),
                0,
                image.getMask().getData(),
                0,
                getWidth()*getHeight());
        return image;
    }
    
    /**
     * Returns the input Gray8Image.
     * @return the input image
     */
    public Gray8Image getImage()
    {
        return this;
    }
    
    /**
     * Get the image mask, a Gray8Image.
     * @return the input mask
     */
    public Gray8Image getMask()
    {
        return this.imMask;
    }
    
    /** Return a pointer to the mask data.
     *
     * @return the data pointer.
     */
    public byte[] getMaskData()
    {
        return this.imMask.getData();
    }
    
    /**
     * Returns true iff the given pixel is masked.
     * @param nRow row of pixel
     * @param nCol column of pixel
     * @return true iff the given pixel is masked.
     */
    public boolean isMasked(int nRow, int nCol) {
        return this.imMask.getData()[nRow*this.imMask.getWidth()+nCol] != Byte.MIN_VALUE;
    }

    /**
     * Set the mask at the given pixel.
     * @param nRow row of pixel
     * @param nCol column of pixel
     * @return modified Gray8Image (this)
     */
    public Gray8MaskedImage setMask(int nRow, int nCol) {
        this.imMask.getData()[nRow*this.imMask.getWidth()+nCol] = Byte.MAX_VALUE;
        return this;
    }
    
    /** Return a string describing the image.
     *
     * @return the string.
     */
    public String toString()
    {
        return super.toString() + " (" + this.getWidth() + "x" +  //$NON-NLS-1$ //$NON-NLS-2$
                this.getHeight() + "," + this.imMask.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Clear the mask at the given pixel.
     * @param nRow row of pixel
     * @param nCol column of pixel
     * @return modified Gray8Image (this)
     */
    public Gray8MaskedImage unsetMask(int nRow, int nCol) {
        this.imMask.getData()[nRow*this.imMask.getWidth()+nCol] = Byte.MIN_VALUE;
        return this;
    }

}
