/*
 * Gray8Rect.java
 *
 * Created on September 9, 2006, 2:52 PM
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

/**
 * Pipeline stage assigns a constant value to a rectangle in an input Gray8Image
 * to produce an output Gray8Image.
 * @author webb
 */
public class Gray8Rect extends PipelineStage {
    private int cX, cY, nWidth, nHeight;
    private byte bValue;
    
    /**
     * Creates a new instance of Gray8Rect.
     * @param cX The horizontal offset of the rectangle.
     * @param cY the vertical offset of the rectangle.
     * @param nWidth the width of the rectangle.
     * @param nHeight the height of the rectangle.
     * @param bValue the value to be assigned to the rectangle.
     * @throws jjil.core.Error if the height or width of the rectange is negative or zero.
     */
    public Gray8Rect(int cX, int cY, int nWidth, int nHeight, byte bValue) 
    	throws jjil.core.Error {
    	setWindow(cX, cY, nWidth, nHeight);
        this.bValue = bValue;
    }
        
    /**
     * Assigns a constant rectangle to the input Gray8Image, replacing values in the image.
     * @param image the input image (output replaces input).
     * @throws jjil.core.Error if the input is not a Gray8Image.
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
        Gray8Image input = (Gray8Image) image;
        byte[] data = input.getData();
        int nLimitY = Math.min(input.getHeight(), this.cY + this.nHeight);
        int nLimitX = Math.min(input.getWidth(), this.cX + this.nWidth);
        for (int i=this.cY; i<nLimitY; i++) {
            int nStart = i * image.getWidth();
            for (int j=this.cX; j<nLimitX; j++) {
                data[nStart+j] = this.bValue;
            }
        }
         super.setOutput(input);
    }
    
    /**
     * 
     * @param cX Top-left horizontal coordinate of the rectangle.
     * @param cY top-left vertical coordinate of the rectangle.
     * @param nWidth Width of the rectangle.
     * @param nHeight Height of the rectangle.
     * @throws jjil.core.Error if the width or height is negative or zero.
     */
    public void setWindow(int cX, int cY, int nWidth, int nHeight) 
    	throws jjil.core.Error {
    	if (nWidth <= 0 || nHeight <= 0) {
            throw new Error(
            		Error.PACKAGE.ALGORITHM,
            		ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
            		new Integer(nWidth).toString(),
            		new Integer(nHeight).toString(),
            		null);
    	}
        this.cX = cX;
        this.cY = cY;
        this.nWidth = nWidth;
        this.nHeight = nHeight;
    }
}
