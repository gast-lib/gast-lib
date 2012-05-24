/*
 * Gray8VertTrapWarp.java
 *
 * Created on September 9, 2006, 3:17 PM
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
 * rectangular output image. The trapezoid is alined vertically, i.e., the
 * vertical edges are parallel
 * @author webb
 */
public class Gray8VertTrapWarp extends PipelineStage {
   private int nRowTopEnd;
   private int nRowTopStart;
   private int nRowBotEnd;
   private int nRowBotStart;
   private int nColEnd;
   private int nColStart;
   
    
    /** Creates a new instance of Gray8VertTrapWarp. Gray8VertTrapWarp warps a trapezoidal
     * region in an input gray image into a rectangular output image. The
     * left and right sides of the trapezoid are aligned with the columns of the image.
     * <p>
     * The bounds are specified here in a manner consistent with the way they
     * are specified as array bounds -- that is, the starting bound is closed
     * (&le;) and the ending bound is open (&lt;).
     *
     * @param nColStart starting column of trapezoid in input image
     * @param nColEnd bounding column of trapezoid in input image. This is one past
     * the actual last column processed in the input image.
     * @param nRowTopStart top edge of trapezoid in starting column.
     * @param nRowBotStart bottom bound of trapezoid in starting column. This is
     * one past the actual last row processed in the input image.
     * @param nRowTopEnd top edge of trapezoid in ending column
     * @param nRowBotEnd bottom bound of trapezoid in ending column. This is one
     * past the actual last column processed in the input image. The column
     * referred to as the ending row is the one above the bounding row, i.e.,
     * nColEnd.
     * @throws jjil.core.Error if the trapezoid is empty or outside
     * the bounds of any image, i.e., if nColStart &lt; 0, or nColEnd &le; nColStart,
     * or nRowTopStart &le; nRowBotStart, or nRowTopEnd &le; nRowBotEnd.
     */
    public Gray8VertTrapWarp(
            int nColStart, 
	    int nColEnd, 
	    int nRowTopStart, 
	    int nRowBotStart,
	    int nRowTopEnd, 
	    int nRowBotEnd) throws jjil.core.Error {
        setTrapezoid(nColStart, nColEnd, nRowTopStart, nRowBotStart,
                nRowTopEnd, nRowBotEnd);
    }
    
    
   /** Returns the left column position of the trapezoid in the ending row.
    *
    * @return the left edge position of the trapezoid in the ending row.
    */
   public int getColLeftEnd() {
       return this.nRowTopEnd;
   }
   
   /** Returns the left column position of the trapezoid in the starting row.
    *
    * @return the left edge position of the trapezoid in the starting row.
    */
   public int getColLeftStart() {
       return this.nRowTopStart;
   }
   
   /** Returns the right column position of the trapezoid in the ending row.
    *
    * @return the right edge position of the trapezoid in the ending row.
    */
   public int getColRightEnd() {
       return this.nRowBotEnd;
   }
   
   /** Returns the right column position of the trapezoid in the starting row.
    *
    * @return the right edge position of the trapezoid in the starting row.
    */
   public int getColRightStart() {
       return this.nRowBotStart;
   }
   
   /** Returns the ending row of the trapezoid.
    *
    * @return the bottom row of the trapezoid.
    */
   public int getRowEnd() {
       return this.nColEnd;
   }
   
   /** Returns the starting row of the trapezoid.
    *
    * @return the top row of the trapezoid.
    */
   public int getRowStart() {
       return this.nColStart;
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
        if (this.nRowBotStart > image.getHeight() ||
            this.nRowBotEnd > image.getHeight() ||
            this.nColEnd > image.getWidth()) {
            throw new Error(
    				Error.PACKAGE.CORE,
    				jjil.core.ErrorCodes.BOUNDS_OUTSIDE_IMAGE,
    				image.toString(),
    				this.toString(),
    				null);
        }
        // we scale everything by 8 bits for accurate computation without
        // floating point
        int fTop = this.nRowTopStart << 8;
        int fBot = this.nRowBotStart << 8;
        int nHeight = imageOutput.getHeight();
        int nWidth = imageOutput.getWidth();
        int fTopIncr = ((this.nRowTopEnd - this.nRowTopStart) << 8) / nWidth;
        int fBotIncr = ((this.nRowBotEnd - this.nRowBotStart) << 8) / nWidth;
        byte[] in = ((Gray8Image) image).getData();
        byte[] out = ((Gray8Image)super.imageOutput).getData();
        for (int i=0; i<nWidth; i++) {
            // these are scaled by 8 bits
            int fY = fTop;
            int fYIncr = (fBot - fTop) / nHeight;
            for (int j=0; j<nHeight; j++) {
                // truncate scaled pixel position
                int iY = Math.min(image.getHeight() - 2, fY >> 8);
                int topPixel = in[i+this.nColStart+(iY)*image.getWidth()];
                int botPixel = in[i+this.nColStart+(iY+1)*image.getWidth()];
                // calculate fractional component of pixel between left and right
                int interp = ((botPixel - topPixel) * ((fY - (iY << 8))) >> 8);
                out[i+j*nWidth] = (byte) Math.max(
                        Byte.MIN_VALUE, 
                        Math.min(
                            Byte.MAX_VALUE, 
                            topPixel + interp));
                fY += fYIncr;
            }
            fTop += fTopIncr;
            fBot += fBotIncr;
        }
        super.setOutput(super.imageOutput);
    }
    
    /** Sets the bounds of the trapezoid. Recreates the output image when they
     * change. The output height is set to the input trapezoid height, and the
     * output width is set to the larger of the trapezoid width at the first
     * and last rows.
     *
     * @param nColStart starting row of trapezoid in input image
     * @param nColEnd ending row of trapezoid in input image
     * @param nRowTopStart left edge of trapezoid on starting row.
     * @param nRowBotStart right edge of trapezoid on starting row
     * @param nRowTopEnd left edge of trapezoid on ending row
     * @param nRowBotEnd right edge of trapezoid on ending row
     * @throws jjil.core.Error if the trapezoid is empty or outside
     * the bounds of any image, i.e., if nColStart &lt; 0, or nColEnd &le; nColStart,
     * or nRowTopStart &le; nRowBotStart, or nRowTopEnd &le; nRowBotEnd.
     */
    public void setTrapezoid(
            int nColStart, 
	    int nColEnd, 
	    int nRowTopStart, 
	    int nRowBotStart,
	    int nRowTopEnd, 
	    int nRowBotEnd) throws jjil.core.Error {
        if (nColStart >= nColEnd || 
                nRowTopStart >= nRowBotStart ||
                nRowTopEnd >= nRowBotEnd) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.PARAMETER_OUT_OF_RANGE,
            				new Integer(nColStart).toString(),
            				new Integer(nColEnd).toString(),
            				null);
        }
        this.nColStart = nColStart;
        this.nColEnd = nColEnd;
        this.nRowTopStart = nRowTopStart;
        this.nRowBotStart = nRowBotStart;
        this.nRowTopEnd = nRowTopEnd;
        this.nRowBotEnd = nRowBotEnd;
        if (this.nColStart < 0 || 
                this.nRowTopStart < 0 || 
                this.nRowBotStart < 0) {
            throw new Error(
                            Error.PACKAGE.CORE,
                            jjil.core.ErrorCodes.BOUNDS_OUTSIDE_IMAGE,
                            this.toString(),
                            null,
                            null);
        }
        int nWidth = this.nColEnd - this.nColStart;
	    int nHeight = Math.max(
                this.nRowBotStart - this.nRowTopStart, 
                this.nRowBotEnd - this.nRowTopEnd);
        super.imageOutput = new Gray8Image(nWidth, nHeight);

    }
    
    /**
     * Returns a string describing the current instance. All the constructor
     * parameters are returned in the order specified in the constructor.
     * @return The string describing the current instance. The string is of the form 
     * "jjil.algorithm.Gray8VertTrapWarpxxx (startRow,endRow,leftColStart,
     * rightColStart,leftColEnd,rightColEnd)"
     */
    public String toString() {
        return super.toString() + " (" + this.nColStart + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nColEnd + "," + this.nRowTopStart + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nRowBotStart + "," + this.nRowTopEnd + "," + //$NON-NLS-1$ //$NON-NLS-2$
                this.nRowBotEnd + ")"; //$NON-NLS-1$
    }
}
