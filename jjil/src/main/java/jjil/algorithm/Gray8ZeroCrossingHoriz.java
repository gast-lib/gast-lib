/*
 * Gray8ZeroCrossingHoriz.java
 *
 * Created on September 9, 2006, 3:19 PM
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
import jjil.core.Gray8Image;

/**
 * Computes an array of zero crossing positions in the input gray image.
 * Not a pipeline stage. The output is an array of arrays of exact positions of 
 * zero crossings, one array per row of the input image. A threshold
 * parameter lets you set the zero crossing strength. The zero crossings
 * are returned as an array of arrays of ints, one array per row, each
 * integer referring to the position of a zero crossing in the row,
 * multiplied by 256 so fractional positions can be represented.
 * @author webb
 */
public class Gray8ZeroCrossingHoriz {
    private int wThreshold;
    
    /** Creates a new instance of Gray8ZeroCrossingHoriz. Gray8ZeroCrossingHoriz computes
     * the horizontal zero crossings of a signed byte image. 
     *
     * @param wThreshold -- the minimum strength for a zero crossing to
     * be considered significant.
     * @throws jjil.core.Error if the threshold is less than 0. Use
     * 0 if you want all zero crossings.
     */
    public Gray8ZeroCrossingHoriz(int wThreshold) throws jjil.core.Error {
        setThreshold(wThreshold);
    }
    
    /** Returns the current threshold.
     *
     * @return the current threshold.
     */
    public int getThreshold() {
        return this.wThreshold;
    }
    
    /** We have two pixel values, the first negative, the second positive.
     *  Compute the position of the zero crossing on the line 
     *  (0,negPix)->(256,posPix).
     * 
     * @param negPix the negative pixel
     * @param posPix the positive pixel
     * @return the zero crossing position
     */
    private int computeZeroCrossing(
            int leftPos, 
            int leftPix, 
            int rightPos, 
            int rightPix) {
       /* adjust x coordinate to increase scale and allow fractional positions
         * to be calculated
         */
        leftPos <<= 8;
        rightPos <<= 8;
        int b = (leftPix * rightPos - rightPix * leftPos)/(rightPos-leftPos);
        /* Taking the line as y = mx + b we have
         *      y = leftPix when x = leftPos
         *      y = rightPix when x = rightPos
         * Hence b = (leftPix*rightPos - rightPix*leftPos)/(rightPos-leftPos)
         *   and m = (rightPix - b) / rightPos
         * y = 0 when x = -b / m
         * So x = -b / ((rightPos - b) / rightPos)
         * and reordering for accurate computation gives 
         */
         return b * rightPos / (b - rightPix);
    }
    
    /** Copy a vector of Integer objects into an array of ints.
     *
     * @param v the Vector of Integers
     * @return the array of ints
     */
    private int[] copyVectorToArray(Vector v) {
        /* copy v into an array of ints
         */
        int[] resultRow = new int[v.size()];
        int elem=0;
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            resultRow[elem++] = ((Integer)e.nextElement()).intValue();
        }
        return resultRow;
    }
    
    /** Computes the zero crossings of an input gray image that are greater
     * than a threshold.
     *
     * @param image the input image.
     * @return an array of arrays of zero crossings. There is one array
     * for each row in the input. The array elements are the zero crossing
     * positions within the rows, multiplied by 256 so fractional values
     * can be represented. A value of null means there were no zero crossings
     * in the row.
     */
    public int[][] push(Gray8Image image) {
        byte[] data = image.getData();
        int[][] result = new int[image.getHeight()][];
        for (int i=0; i<image.getHeight(); i++) {
            /* for holding the variable number of zero crossings
             * as we find them.
             */
            Vector v = new Vector();
            int cLastPos = -1;
            byte wLastEdge = 0;
            for (int j=0; j<image.getWidth(); j++) {
                byte bThisPix = data[i*image.getWidth()+j];
                if (Math.abs(bThisPix) > this.wThreshold) {
                    /* we found an edge. see if the last edge was
                     * of opposite sign. This also tests if there
                     * was a last edge, because if there wasn't
                     * wLastEdge will be 0 and the test will fail.
                     */
                    if (bThisPix*wLastEdge < 0) {
                        /* the last edge was of opposite sign.
                         * Compute the exact zero crossing.
                         */
                        int posEdge = 
                            computeZeroCrossing(cLastPos, wLastEdge,
                                j, bThisPix);
                        // test for negative or positive zero crossing
                        if (bThisPix < 0) posEdge = -posEdge;
                        v.addElement(new Integer(posEdge));
                    }
                    cLastPos = j;
                    wLastEdge = bThisPix;
                }
            }
            if (!v.isEmpty()) {
               result[i] = copyVectorToArray(v);
            }
        }
        return result;
    }
    
    /**
     * Returns a string describing this instance of Gray8ZeroCrossingHoriz,
     * including the minimum strength parameter.
     * @return the string -- looks like
     * "jjil.algorithm.Gray8ZeroCrossingHoriz@xxx (number)"
     */
    public String toString() {
        return super.toString() + " (" + this.wThreshold + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /** Changes the zero crossing threshold.
     *
     * @param wThreshold the new threshold.
     * @throws jjil.core.Error if wThreshold is less than 0. Use 0
     * if you want all zero crossings.
     */
    public void setThreshold(int wThreshold) throws jjil.core.Error {
        if (wThreshold < 0) {
            throw new Error(
        			Error.PACKAGE.CORE,
        			ErrorCodes.THRESHOLD_NEGATIVE,
        			new Integer(wThreshold).toString(),
        			null,
        			null);
            }
        this.wThreshold = wThreshold;
    }
}
