/*
 * Gray8OffsetImage.java
 *
 * Created on June 29, 2007, 5:27 PM
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

package jjil.core;

/**
 * Gray8OffsetImage is used to represent a rectangular region extracted from a larger
 * Gray8Image, retaining the x and y position where the subimage was extracted.
 * @author webb
 */
public class Gray8OffsetImage extends Gray8Image {
    int cX;
    int cY;
    
    /**
     * Creates a new instance of Gray8OffsetImage
     * @param cWidth Image height.
     * @param cHeight Image width.
     * @param cX Horizontal position of top-left corner of subimage.
     * @param cY Vertical position of top-left corner of subimage.
     */
    public Gray8OffsetImage(int cWidth, int cHeight, int cX, int cY) {
        super(cWidth, cHeight);
        this.cX = cX;
        this.cY = cY;
    }
    
    /**
     * Creates a new instance of Gray8OffsetImage
     * @param cWidth Image height.
     * @param cHeight Image width.
     * @param bValue value to assign to image
     * @param cX Horizontal position of top-left corner of subimage.
     * @param cY Vertical position of top-left corner of subimage.
     */
    public Gray8OffsetImage(int cWidth, int cHeight, int cX, int cY, byte bValue) {
        super(cWidth, cHeight, bValue);
        this.cX = cX;
        this.cY = cY;
    }
    
    /**
     * Creates a new instance of Gray8OffsetImage from a Gray8Image
     * @param image Gray8Image to initialize contents to
     * @param cX Horizontal position of top-left corner of subimage.
     * @param cY Vertical position of top-left corner of subimage.
     */
    public Gray8OffsetImage(Gray8Image image, int cX, int cY) {
        super(image.getWidth(), image.getHeight());
        System.arraycopy(
                image.getData(),
                0,
                this.getData(),
                0,
                getWidth()*getHeight());
        this.cX = cX;
        this.cY = cY;
    }
    
    /** Copy this image
     *
     * @return the image copy.
     */
    public Object clone()
    {
        Gray8Image image = new Gray8OffsetImage(getWidth(),getHeight(),getXOffset(),getYOffset());
        System.arraycopy(
                this.getData(),
                0,
                image.getData(),
                0,
                getWidth()*getHeight());
        return image;
    }
    
    /**
     * Get horizontal offset of subimage.
     * @return the horizontal position of the top-left corner of the subimage.
     */
    public int getXOffset()
    {
        return this.cX;
    }
    
    /**
     * Get vertical offset of subimage.
     * @return the vertical position of the top-left corner of the subimage.
     */
    public int getYOffset()
    {
        return this.cY;
    }
    
    /**
     * Change horizontal position of subimage.
     * @param nX the new horizontal position.
     */
    public void setXOffset(int nX)
    {
        this.cX = nX;
    }
    
    /**
     * Change vertical position of subimage.
     * @param nY the new vertical position.
     */
    public void setYOffset(int nY)
    {
        this.cY = nY;
    }
    
    /** Return a string describing the image.
     *
     * @return the string.
     */
    public String toString()
    {
        return super.toString() + " (" + getWidth() + "x" + getHeight() + //$NON-NLS-1$ //$NON-NLS-2$
                "," + getXOffset() + "," + getYOffset() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


}
