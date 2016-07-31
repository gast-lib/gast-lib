/*
 * Gray8TrapWarp.java
 *
 * Created on September 9, 2006, 3:17 PM
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
 * This PipelineStage warps a trapezoid in the input gray image into a 
 * rectangular output image.
 * @author webb
 */
public class Gray8TrapWarp extends PipelineStage {
   Gray8Image imageOutput;
   private int nColLeftEnd;
   private int nColLeftStart;
   private int nColRightEnd;
   private int nColRightStart;
   private int nRowEnd;
   private int nRowStart;
   
    
    /** Creates a new instance of Gray8TrapWarp. Gray8TrapWarp warps a trapezoidal
     * region in an input gray image into a rectangular output image. The
     * top and bottom of the trapezoid are aligned with the rows of the image.
     * <p>
     * The bounds are specified here in a manner consistent with the way they
     * are specified as array bounds -- that is, the starting bound is closed
     * (&le;) and the ending bound is open (<).
     *
     * @param nRowStart starting row of trapezoid in input image
     * @param nRowEnd bounding row of trapezoid in input image. This is one past
     * the actual last row processed in the input image.
     * @param nColLeftStart left edge of trapezoid on starting row.
     * @param nColRightStart right bound of trapezoid on starting row. This is
     * one past the actual last column processed in the input image.
     * @param nColLeftEnd left edge of trapezoid on ending row
     * @param nColRightEnd right bound of trapezoid on ending row. This is one
     * past the actual last column processed in the input image. The row
     * referred to as the ending row is the one above the bounding row, i.e.,
     * nRowEnd.
     * @throws jjil.core.Error if the trapezoid is empty or outside
     * the bounds of any image, i.e., if nRowStart < 0, or nRowEnd <= nRowStart,
     * or nColLeftStart <= nColRightStart, or nColLeftEnd <= nColRightEnd.
     */
    public Gray8TrapWarp(
            int nRowStart, 
	    int nRowEnd, 
	    int nColLeftStart, 
	    int nColRightStart,
	    int nColLeftEnd, 
	    int nColRightEnd) throws jjil.core.Error {
        setTrapezoid(nRowStart, nRowEnd, nColLeftStart, nColRightStart,
                nColLeftEnd, nColRightEnd);
    }
    
    
   /** Returns the left column position of the trapezoid in the ending row.
    *
    * @return the left edge position of the trapezoid in the ending row.
    */
   public int getColLeftEnd() {
       return this.nColLeftEnd;
   }
   
   /** Returns the left column position of the trapezoid in the starting row.
    *
    * @return the left edge position of the trapezoid in the starting row.
    */
   public int getColLeftStart() {
       return this.nColLeftStart;
   }
   
   /** Returns the right column position of the trapezoid in the ending row.
    *
    * @return the right edge position of the trapezoid in the ending row.
    */
   public int getColRightEnd() {
       return this.nColRightEnd;
   }
   
   /** Returns the right column position of the trapezoid in the starting row.
    *
    * @return the right edge position of the trapezoid in the starting row.
    */
   public int getColRightStart() {
       return this.nColRightStart;
   }
   
   /** Returns the ending row of the trapezoid.
    *
    * @return the bottom row of the trapezoid.
    */
   public int getRowEnd() {
       return this.nRowEnd;
   }
   
   /** Returns the starting row of the trapezoid.
    *
    * @return the top row of the trapezoid.
    */
   public int getRowStart() {
       return this.nRowStart;
   }

    /**
     * Warps a trapezoidal region in the input gray image into a rectangular
     * output image. Uses bilinear interpolation. The calculation of fractional
     * image coordinates is done by multiplying all the coordinates by 256,
     * to avoid floating point computation.
     *
     * @param image the input gray image.
     * @throws jjil.core.Error if the input image is not gray,
     * or the trapezoid already specified extends outside its bounds.
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
        if (this.nColRightStart > image.getWidth() ||
            this.nColRightEnd > image.getWidth() ||
            this.nRowEnd > image.getHeight()) {
            throw new Error(
    				Error.PACKAGE.CORE,
    				jjil.core.ErrorCodes.BOUNDS_OUTSIDE_IMAGE,
    				image.toString(),
    				this.toString(),
    				null);
        }
        int fLeft = (this.nColLeftStart * 256);
        int fRight = (this.nColRightStart * 256);
        int nHeight = imageOutput.getHeight();
        int nWidth = imageOutput.getWidth();
        int fLeftIncr = ((this.nColLeftEnd - this.nColLeftStart) * 256) / nHeight;
        int fRightIncr = ((this.nColRightEnd - this.nColRightStart) * 256) / nHeight;
        byte[] in = ((Gray8Image) image).getData();
        byte[] out = imageOutput.getData();
        for (int i=0; i<nHeight; i++) {
            // we scale everything by 8 bits for accurate computation without
            // floating point
            int fY = fLeft;
            int fYIncr = (fRight - fLeft) / nWidth;
            for (int j=0; j<nWidth; j++) {
                int iY = Math.min(image.getWidth() - 2, fY / 256);
                // truncate floating-point pixel position
                int leftPixel = in[(i+this.nRowStart)*image.getWidth()+iY];
                int rightPixel = in[(i+this.nRowStart)*image.getWidth()+iY+1];
                // calculate fractional component of pixel between left and right
                int interp = ((rightPixel - leftPixel) * (fY - (iY * 256))) / 256;
                out[i*nWidth+j] = (byte) (leftPixel + interp);
                fY += fYIncr;
            }
            fLeft += fLeftIncr;
            fRight += fRightIncr;
        }
        super.setOutput(this.imageOutput);
    }
    
    /** Sets the bounds of the trapezoid. Recreates the output image when they
     * change. The output height is set to the input trapezoid height, and the
     * output width is set to the larger of the trapezoid width at the first
     * and last rows.
     *
     * @param nRowStart starting row of trapezoid in input image
     * @param nRowEnd ending row of trapezoid in input image
     * @param nColLeftStart left edge of trapezoid on starting row.
     * @param nColRightStart right edge of trapezoid on starting row
     * @param nColLeftEnd left edge of trapezoid on ending row
     * @param nColRightEnd right edge of trapezoid on ending row
     * @throws jjil.core.Error if the trapezoid is empty or outside
     * the bounds of any image, i.e., if nRowStart < 0, or nRowEnd &le; nRowStart,
     * or nColLeftStart &le; nColRightStart, or nColLeftEnd &le; nColRightEnd.
     */
    public void setTrapezoid(
            int nRowStart, 
	    int nRowEnd, 
	    int nColLeftStart, 
	    int nColRightStart,
	    int nColLeftEnd, 
	    int nColRightEnd) throws jjil.core.Error {
        if (nRowStart >= nRowEnd) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.WARP_START_ROW_GE_END_ROW,
            				new Integer(nRowStart).toString(),
            				new Integer(nRowEnd).toString(),
            				null);
        }
        if (nColLeftStart >= nColRightStart) {
            throw new Error(
    				Error.PACKAGE.ALGORITHM,
    				ErrorCodes.WARP_START_LEFT_COL_GE_START_RIGHT_COL,
    				new Integer(nColLeftStart).toString(),
    				new Integer(nColRightStart).toString(),
    				null);

        }
        if (nColLeftEnd >= nColRightEnd) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.WARP_END_LEFT_COL_GE_END_RIGHT_COL,
                            new Integer(nColLeftEnd).toString(),
                            new Integer(nColRightEnd).toString(),
                            null);
        }
        this.nRowStart = nRowStart;
        this.nRowEnd = nRowEnd;
        this.nColLeftStart = nColLeftStart;
        this.nColRightStart = nColRightStart;
        this.nColLeftEnd = nColLeftEnd;
        this.nColRightEnd = nColRightEnd;
        if (this.nRowStart < 0 || this.nColLeftStart < 0 || this.nColRightStart < 0) {
            throw new Error(
                            Error.PACKAGE.CORE,
                            jjil.core.ErrorCodes.BOUNDS_OUTSIDE_IMAGE,
                            this.toString(),
                            null,
                            null);
        }
        int nHeight = this.nRowEnd - this.nRowStart;
	    int nWidth = Math.max(
                this.nColRightStart - this.nColLeftStart, 
                this.nColRightEnd - this.nColLeftEnd);
        this.imageOutput = new Gray8Image(nWidth, nHeight);

    }
    
    /**
     * Returns a string describing the current instance. All the constructor
     * parameters are returned in the order specified in the constructor.
     * @return The string describing the current instance. The string is of the form 
     * "jjil.algorithm.Gray8TrapWarpxxx (startRow,endRow,leftColStart,
     * rightColStart,leftColEnd,rightColEnd)"
     */
    public String toString() {
        return super.toString() + " (" + this.nRowStart + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nRowEnd + "," + this.nColLeftStart + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nColRightStart + "," + this.nColLeftEnd + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nColRightEnd + ")"; //$NON-NLS-1$
    }
}
