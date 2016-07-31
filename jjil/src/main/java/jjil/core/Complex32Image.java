/*
 * Complex32Image.java
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
 * Complex32Image is the image type used to store a complex 32-bit image.
 *
 * @author webb
 */
public class Complex32Image extends Image {
    private final Complex cxImage[];
    
    /**
     * Creates a new instance of Complex32Image
     * @param cWidth Width of the image (columns).
     * @param cHeight Height of the image (rows)
     */
    public Complex32Image(int cWidth, int cHeight) {
        super(cWidth, cHeight);
        this.cxImage = new Complex[getWidth()*getHeight()];
    }
        
    /** Copy this image
     *
     * @return the image copy.
     */
    public Object clone()
    {
        Complex32Image image = new Complex32Image(getWidth(),getHeight());
        System.arraycopy(
                this.getData(),
                0,
                image.getData(),
                0,
                getWidth()*getHeight());
        return image;
    }
    
    /** Return a pointer to the image data.
     *
     * @return the data pointer.
     */
    public Complex[] getData()
    {
        return this.cxImage;
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
