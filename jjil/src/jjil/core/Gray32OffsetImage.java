/*
 * Gray32OffsetImage.java
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
 * Gray32OffsetImage is used to represent a rectangular region extracted from
 * a larger Gray32Image, retaining the x and y position where the image was
 * extracted.
 * @author webb
 */
public class Gray32OffsetImage extends Gray32Image {
    int cX;
    int cY;
    
    /**
     * Creates a new instance of Gray32OffsetImage
     * @param cWidth Image width.
     * @param cHeight Image height.
     * @param cX Horizontal position of top-left corner of subimage.
     * @param cY Vertical position of top-left corner of subimage.
     */
    public Gray32OffsetImage(int cWidth, int cHeight, int cX, int cY) {
        super(cWidth, cHeight);
        this.cX = cX;
        this.cY = cY;
    }
    
    /** Copy this image
     *
     * @return the image copy.
     */
    public Object clone()
    {
        Gray32Image image = new Gray32OffsetImage(getWidth(),getHeight(),getXOffset(),getYOffset());
        System.arraycopy(
                this.getData(),
                0,
                image.getData(),
                0,
                getWidth()*getHeight());
        return image;
    }
    
    /**
     * Return horizontal position of top-left corner of subimage.
     * @return the horizontal position of the top-left corner of the subimage.
     */
    public int getXOffset()
    {
        return this.cX;
    }
    
    /**
     * Return vertical position of top-left corner of subimage.
     * @return vertical position of top-left corner of subimage.
     */
    public int getYOffset()
    {
        return this.cY;
    }
    
    /**
     * Change horizontal position of subimage.
     * @param nX New horizontal position of top-left corner of subimage.
     */
    public void setXOffset(int nX)
    {
        this.cX = nX;
    }
    
    /**
     * Change vertical psotion of subimage.
     * @param nY New vertical position of top-left corner of subimage.
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
