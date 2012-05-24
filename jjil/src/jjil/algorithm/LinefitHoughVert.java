/*
 * LinefitHoughVert.java
 *
 * Created on September 9, 2006, 1:15 PM
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
import java.util.Enumeration;
import java.util.Vector;

import jjil.core.Error;
import jjil.core.Point;

/**
 * Finds a line in an array of points using Hough transform. Not a pipeline
 * stage. Returns the most likely line as y-slope and x-intercept through
 * member access functions. The line search for should be oriented more or less
 * vertically (within the slope range specified).<p>
 * Lines are traditionally represented in terms of slope m and x-intercept b, i.e.,
 * y = m * x + b. In this code we are concerned with vertical or nearly vertical
 * lines so we change the representation to x = m * y + b. b is then the x-intercept
 * (y = 0 gives x = b) and m is the "y-slope". m = 0 gives a vertical line at x = b.
 * @author webb
 */
public class LinefitHoughVert {
    /** @var cHoughAccum the Hough accumulator array */
    int[][] cHoughAccum;
    /** @var cCount the number of points on the line that was found */
    int cCount = 0;
    /** @var cMaxSlope the maximum allowable slope, times 256 */
    final int cMaxSlope;
    /** @var cMaxX the maximum allowable x-intercept */
    final int cMaxX;
    /** @var cMinSlope the minimum allowable slope, times 256 */
    final int cMinSlope;
    /** @var cMinX the minimum allowable x-intercept */
    final int cMinX;
    /** @var cSlope the slope of the line that was found, times 256 */
    int cSlope;
    /** @var cSteps the number of steps to take from cMinSlope to cMaxSlope */
    final int cSteps;
    /** @var cXInt the x-intercept of the line that was found */
    int cXInt;
    
    /** Creates a new instance of LinefitHoughVert 
     *
     * @param cMinX minimum X value
     * @param cMaxX maximum X value
     * @param cMinSlope minimum slope (multiplied by 256)
     * @param cMaxSlope maximum slope (multiplied by 256)
     * @param cSteps steps taken in Hough accumulator between minimum and
     * maximum slope.
     * @throws jjil.core.Error if X or slope range is empty, or
     * cSteps is not positive.
     */
    public LinefitHoughVert(
            int cMinX, 
            int cMaxX, 
            int cMinSlope, 
            int cMaxSlope, 
            int cSteps) throws jjil.core.Error {
        if (cMaxX < cMinX) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.PARAMETER_RANGE_NULL_OR_NEGATIVE,
                			new Integer(cMinX).toString(),
                			new Integer(cMaxX).toString(),
                			null);
        }
        this.cMinX = cMinX;
        this.cMaxX = cMaxX;
        if (cMaxSlope < cMinSlope) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.PARAMETER_RANGE_NULL_OR_NEGATIVE,
                			new Integer(cMinSlope).toString(),
                			new Integer(cMaxSlope).toString(),
                			null);
        }
        if (cSteps <= 0) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.PARAMETER_OUT_OF_RANGE,
                			new Integer(cSteps).toString(),
                			new Integer(1).toString(),
                			new Integer(Integer.MAX_VALUE).toString());
        }
        this.cMinSlope = cMinSlope;
        this.cMaxSlope = cMaxSlope;
        this.cSteps = cSteps;
    }
    
    /** Add a new point to the Hough accumulator array. We increment along the 
     * line in the array
     * from (cMinSlope>>8, xIntStart) to (cMaxSlope>>8, xIntEnd), where
     * xIntStart is the x-intercept assuming the slope is at the minimum,
     * and xIntEnd is the x-intercept assuming the slope is maximal.
     *
     * @param p the point to add to the accumulator array
     */
    private void addPoint(Point p) {
        // Remember the line we are fitting is
        // x = slope * x + intercept
        // compute initial intercept. cMinSlope is the real slope minimum
        // * 256.
        int xIntStart = (p.getX() * 256 - p.getY() * this.cMinSlope) / 256;
        // compute final intercept. cMaxSlope is the real slope maximum
        // * 256.
        int xIntEnd = (p.getX() * 256 - p.getY() * this.cMaxSlope) / 256;
        /** work along the line from (0,xIntStart) to (cSteps,xIntEnd),
         * incrementing the Hough accumulator.
         */
        for (int slope = 0; slope < this.cSteps; slope++) {
           int xInt = (xIntEnd - xIntStart) * slope / this.cSteps +
                   xIntStart;
           /** check if the current position falls inside the Hough 
            * accumulator.
            */
           if (xInt >= this.cMinX && xInt < this.cMaxX) {
                this.cHoughAccum[slope][xInt-this.cMinX]++;
           }
        };
    }
    
    /** Find the peak in the Hough array. Updates cCount, cSlope, and cXInt.
     */
    private void findPeak() {
        this.cCount = Integer.MIN_VALUE;
        for (int slope=0; slope<this.cSteps; slope++) {
            for (int x=0; x<this.cMaxX-this.cMinX; x++) {
                if (this.cHoughAccum[slope][x] > this.cCount) {
                    this.cCount = this.cHoughAccum[slope][x];
                    this.cSlope = slope * (this.cMaxSlope - this.cMinSlope) 
                        / this.cSteps + this.cMinSlope;
                    this.cXInt = x + this.cMinX;
                }
            }
        }
    }
    
    /** Returns the count of points on the line that was found.
     *
     * @return the point count.
     */
    public int getCount() {
        return this.cCount;
    }
    
    /** Returns the y-slope of the line that was found.
     *
     * @return the line slope (*256)
     */
    public int getSlope() {
        return this.cSlope;
    }
    
    /** Returns the x-intercept of the line that was found.
     *
     * @return the x-intercept.
     */
    public int getX() {
        return this.cXInt;
    }
    
    /** Finds the most likely line passing through the points in the Vector.
     * 
     * @param points the input Vector of point positions
     * @throws jjil.core.Error if points is not a Vector of 
     * point objects.
     */
    public void push(Vector points) throws jjil.core.Error {
        /* create Hough accumulator */
        this.cHoughAccum = 
                new int[this.cSteps][this.cMaxX-this.cMinX];
        /* fill the Hough accumulator
         */
        for (Enumeration e = points.elements(); e.hasMoreElements();) {
            Object o = e.nextElement(); 
            if (!(o instanceof Point)) {
                throw new Error(
                    			Error.PACKAGE.ALGORITHM,
                    			ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                    			o.toString(),
                    			"Point",
                    			null);
            }
            Point p = (Point) o;
            addPoint(p);
        }
        findPeak(); // sets cXInt, cSlope, cCount for access by caller
        this.cHoughAccum = null; // free memory
    }
       
    /** Return a string describing the current instance, giving the values
     * of the constructor parameters.
     *
     * @return the string describing the current instance.
     */
    public String toString() {
        return super.toString() + "(" + this.cMinX + "," + this.cMaxX + "," + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                this.cMinSlope + "," + this.cMaxSlope + "," + this.cSteps + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
