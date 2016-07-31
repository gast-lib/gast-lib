/*
 * Gray8Image.java
 *
 * Created on August 27, 2006, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2006 by Jon A. Webb
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
 * Gray8Image is the image type used to store a signed
 * 8-bit image. Note that Java limitations (no unsigned byte) make it necessary
 * to treat alll 8-bit images as signed.
 *
 * @author webb
 */
public class Gray8Image extends Image {
    private final byte bImage[];
    
    /**
     * Creates a new instance of Gray8Image
     * @param cWidth Width of the image (columns).
     * @param cHeight Height of the image (rows)
     */
    public Gray8Image(int cWidth, int cHeight) {
        super(cWidth, cHeight);
        this.bImage = new byte[getWidth()*getHeight()];
    }
    
    public Gray8Image(int cWidth, int cHeight, byte[] rbData) {
        super(cWidth, cHeight);
        this.bImage = rbData;
    }
    
    /**
     * Creates a new instance of Gray8Image
     * @param cWidth Width of the image (columns).
     * @param cHeight Height of the image (rows)
     * @param bValue constant value to be assigned to the image
     */
    public Gray8Image(int cWidth, int cHeight, byte bValue) {
        super(cWidth, cHeight);
        this.bImage = new byte[getWidth()*getHeight()];
        for (int i=0; i<this.getWidth()*this.getHeight();i++) {
            this.bImage[i] = bValue;
        }
    }
    
    /**
     * Copy this image
     * @return the image copy.
     */
    public Object clone()
    {
        Gray8Image image = new Gray8Image(getWidth(),getHeight());
        System.arraycopy(
                this.getData(),
                0,
                image.getData(),
                0,
                getWidth()*getHeight());
        return image;
    }
    
    /**
     * Fill a rectangle in a Gray8Image with a specific value. Rect is filled
     * up to but not including bottom and right edge
     * @param r the Rect to fill
     * @param bVal the value to assign
     * @return modified Gray8Image (this)
     */
    public Gray8Image fill(Rect r, byte bVal) {
        for (int i=r.getTop(); i<r.getBottom(); i++) {
            for (int j=r.getLeft(); j<r.getRight(); j++) {
                this.bImage[i*this.getWidth()+j] = bVal;
            }
        }
        return this;
    }

    /** Return a pointer to the byte image data.
     *
     * @return the data pointer.
     */
    public byte[] getData()
    {
        return this.bImage;
    }

    
    /** Return a string describing the image.
     *
     * @return the string.
     */
    public String toString()
    {
        return super.toString() + " (" + getWidth() + "x" + getHeight() + //$NON-NLS-1$ //$NON-NLS-2$
                ")"; //$NON-NLS-1$
    }

}
