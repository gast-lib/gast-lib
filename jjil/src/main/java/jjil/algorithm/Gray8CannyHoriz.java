/*
 * Gray8CannyHoriz.java
 *
 * Created on August 27, 2006, 4:32, PM
 *
 * To change this templatef, choose Tools | Template Manager
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
 * Computes the horizontal Canny operator for an input gray image. The sigma
 * value for the operator is set in the constructor or setSigma. All calculations
 * are done in integer (per CLDC 1.0) and the sigma value is specified as
 * multiplied by 10.0. The minimum value for the unmultiplied sigma is 0.1;
 * the maximum value is about 10.0. Larger sigma values give an operator which
 * is less sensitive to high frequencies and more sensitive to low frequencies.
 * <p>
 * @author webb
 */
public class Gray8CannyHoriz extends PipelineStage {
    /** cSigma is the sigma value we'll be using. It has been multiplied
     * by 10.0 and converted to integer because CLDC 1.0 doesn't allow
     * floating point. The minimum legal value for cSigma is 1; the maximum
     * is given by the length of nCoeff below.
     */
    private int cSigma;
    /** nCoeff row i is the precomputed Canny coefficients for sigma = i/10.0.
     * They have been scaled by 256 and converted to integer because CLDC 1.0
     * doesn't allow floating point. The coefficients have been scaled and
     * normalized so the sum is 0 and the sum of the absolute values is
     * 256 (counting both sides of the Canny operator -- the operator is
     * symmetric so we just give one side below.) The number of coefficients
     * was determined by generating them out to the point at which the
     * unscaled coefficient was less than a threshold, here 0.05.
     */
    private int[][] nCoeff = {
        {0},
        {-127, 21, 21, 21},
        {-127, 21, 21, 21},
        {-127, 25, 19, 19},
        {-128, 42, 10, 10},
        {-127, 56, 3, 3},
        {-128, 57, 5},
        {-128, 48, 15},
        {-128, 32, 29, 1},
        {-128, 16, 42, 5},
        {-128, 0, 51, 11},
        {-104, -11, 45, 16, 1},
        {-89, -19, 39, 20, 3},
        {-79, -24, 33, 24, 5},
        {-72, -27, 27, 26, 8, 1},
        {-67, -30, 21, 27, 11, 2},
        {-63, -32, 16, 27, 14, 4},
        {-61, -33, 12, 27, 17, 6},
        {-58, -34, 8, 26, 20, 8},
        {-56, -35, 4, 25, 22, 11},
        {-55, -36, 0, 22, 22, 12, 4, 1},
        {-51, -35, -2, 19, 22, 14, 6, 2},
        {-48, -34, -5, 16, 21, 15, 7, 2},
        {-45, -33, -7, 13, 20, 16, 9, 3},
        {-43, -32, -9, 11, 19, 17, 10, 5},
        {-41, -32, -10, 9, 18, 17, 11, 6},
        {-40, -31, -11, 7, 17, 18, 13, 7},
        {-39, -31, -12, 5, 16, 18, 14, 8},
        {-38, -31, -13, 4, 15, 18, 15, 10},
        {-37, -30, -14, 2, 14, 18, 16, 11},
        {-36, -30, -15, 1, 13, 18, 17, 12},
        {-34, -29, -16, -1, 10, 15, 15, 11, 7, 4},
        {-33, -28, -16, -2, 9, 14, 15, 12, 8, 4},
        {-32, -27, -16, -3, 7, 13, 14, 12, 8, 5},
        {-31, -27, -16, -4, 6, 13, 14, 13, 9, 6},
        {-30, -26, -17, -4, 5, 12, 14, 13, 10, 7},
        {-29, -26, -17, -5, 4, 11, 14, 13, 11, 7},
        {-29, -25, -17, -6, 3, 11, 14, 14, 11, 8},
        {-28, -25, -17, -6, 3, 10, 14, 14, 12, 9},
        {-27, -25, -17, -7, 2, 9, 13, 14, 13, 10},
        {-27, -24, -17, -7, 1, 9, 13, 14, 13, 11},
        {-26, -23, -17, -9, 0, 6, 11, 12, 11, 9, 7, 5},
        {-25, -23, -17, -9, 0, 5, 10, 12, 11, 10, 7, 5},
        {-25, -22, -17, -9, -1, 5, 9, 11, 11, 10, 8, 6},
        {-24, -22, -17, -9, -2, 4, 9, 11, 11, 10, 8, 6},
        {-23, -22, -17, -10, -2, 4, 8, 11, 12, 11, 9, 7},
        {-23, -21, -17, -10, -3, 3, 8, 11, 12, 11, 9, 7},
        {-23, -21, -16, -10, -3, 3, 7, 10, 12, 11, 10, 8},
        {-22, -21, -16, -10, -3, 2, 7, 10, 12, 11, 10, 8},
        {-22, -20, -16, -10, -4, 2, 7, 10, 12, 12, 10, 9},
        {-22, -20, -16, -11, -4, 1, 6, 10, 11, 12, 11, 9},
        {-21, -19, -16, -11, -5, 0, 4, 8, 9, 10, 9, 8, 6, 5},
        {-20, -19, -16, -11, -5, 0, 4, 7, 9, 10, 9, 8, 7, 5},
        {-20, -19, -16, -11, -6, 0, 3, 7, 9, 10, 10, 8, 7, 6},
        {-20, -19, -16, -11, -6, -1, 3, 7, 9, 10, 10, 9, 7, 6},
        {-19, -18, -15, -11, -6, -1, 3, 6, 9, 10, 10, 9, 8, 6},
        {-19, -18, -15, -11, -6, -1, 2, 6, 8, 10, 10, 9, 8, 7},
        {-19, -18, -15, -11, -6, -1, 2, 6, 8, 10, 10, 9, 8, 7},
        {-18, -18, -15, -11, -7, -2, 2, 5, 8, 9, 10, 10, 9, 7},
        {-18, -17, -15, -11, -7, -2, 1, 5, 8, 9, 10, 10, 9, 8},
        {-18, -17, -15, -11, -7, -2, 1, 5, 8, 9, 10, 10, 9, 8},
        {-17, -17, -14, -11, -7, -3, 0, 3, 6, 8, 8, 8, 8, 7, 6, 5},
        {-17, -16, -14, -11, -7, -3, 0, 3, 6, 7, 8, 8, 8, 7, 6, 5},
        {-17, -16, -14, -11, -8, -4, 0, 3, 5, 7, 8, 9, 8, 7, 6, 5},
        {-17, -16, -14, -11, -8, -4, 0, 2, 5, 7, 8, 9, 8, 8, 7, 6},
        {-16, -16, -14, -11, -8, -4, 0, 2, 5, 7, 8, 9, 8, 8, 7, 6},
        {-16, -16, -14, -11, -8, -4, 0, 2, 5, 7, 8, 9, 8, 8, 7, 6},
        {-16, -15, -14, -11, -8, -4, -1, 2, 4, 6, 8, 8, 9, 8, 7, 6},
        {-16, -15, -14, -11, -8, -4, -1, 1, 4, 6, 8, 8, 9, 8, 8, 7},
        {-16, -15, -13, -11, -8, -4, -1, 1, 4, 6, 8, 8, 9, 8, 8, 7},
        {-15, -15, -13, -11, -8, -5, -1, 1, 4, 6, 8, 8, 9, 9, 8, 7},
        {-15, -14, -13, -11, -8, -5, -2, 0, 3, 5, 6, 7, 7, 7, 7, 6, 5, 5},
        {-15, -14, -13, -11, -8, -5, -2, 0, 2, 4, 6, 7, 7, 7, 7, 6, 6, 5},
        {-15, -14, -13, -11, -8, -5, -2, 0, 2, 4, 6, 7, 7, 7, 7, 7, 6, 5},
        {-14, -14, -13, -11, -8, -5, -2, 0, 2, 4, 6, 7, 7, 8, 7, 7, 6, 5},
        {-14, -14, -13, -11, -8, -5, -3, 0, 2, 4, 6, 7, 7, 8, 7, 7, 6, 6},
        {-14, -14, -12, -11, -8, -6, -3, 0, 2, 4, 5, 7, 7, 8, 7, 7, 6, 6},
        {-14, -14, -12, -11, -8, -6, -3, 0, 1, 4, 5, 7, 7, 8, 8, 7, 7, 6},
        {-14, -13, -12, -11, -8, -6, -3, 0, 1, 3, 5, 6, 7, 8, 8, 7, 7, 6},
        {-14, -13, -12, -10, -8, -6, -3, 0, 1, 3, 5, 6, 7, 8, 8, 7, 7, 6},
        {-13, -13, -12, -10, -8, -6, -3, -1, 1, 3, 5, 6, 7, 8, 8, 8, 7, 7},
        {-13, -13, -12, -10, -8, -6, -4, -1, 0, 2, 4, 5, 6, 6, 7, 7, 6, 6, 5, 4},
        {-13, -13, -12, -10, -8, -6, -4, -1, 0, 2, 4, 5, 6, 6, 7, 7, 6, 6, 5, 5},
        {-13, -12, -12, -10, -8, -6, -4, -1, 0, 2, 3, 5, 6, 6, 7, 7, 6, 6, 5, 5},
        {-13, -12, -11, -10, -8, -6, -4, -2, 0, 2, 3, 5, 6, 6, 7, 7, 6, 6, 6, 5},
        {-13, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 5, 6, 6, 7, 7, 7, 6, 6, 5},
        {-12, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 4, 6, 6, 7, 7, 7, 6, 6, 5},
        {-12, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 4, 5, 6, 7, 7, 7, 6, 6, 5},
        {-12, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 4, 5, 6, 7, 7, 7, 7, 6, 6},
        {-12, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 4, 5, 6, 7, 7, 7, 7, 6, 6},
        {-12, -12, -11, -10, -8, -6, -4, -2, 0, 1, 3, 4, 5, 6, 7, 7, 7, 7, 6, 6},
        {-12, -11, -11, -10, -8, -6, -5, -3, -1, 0, 2, 3, 4, 5, 6, 6, 6, 6, 6, 5, 5, 4},
        {-11, -11, -11, -10, -8, -6, -5, -3, -1, 0, 2, 3, 4, 5, 6, 6, 6, 6, 6, 5, 5, 4},
        {-11, -11, -11, -9, -8, -6, -5, -3, -1, 0, 1, 3, 4, 5, 6, 6, 6, 6, 6, 5, 5, 4},
        {-11, -11, -10, -9, -8, -6, -5, -3, -1, 0, 1, 3, 4, 5, 6, 6, 6, 6, 6, 6, 5, 5},
        {-11, -11, -10, -9, -8, -7, -5, -3, -1, 0, 1, 3, 4, 5, 5, 6, 6, 6, 6, 6, 5, 5},
        {-11, -11, -10, -9, -8, -7, -5, -3, -1, 0, 1, 3, 4, 5, 5, 6, 6, 6, 6, 6, 5, 5},
        {-11, -11, -10, -9, -8, -7, -5, -3, -1, 0, 1, 2, 4, 5, 5, 6, 6, 6, 6, 6, 5, 5},
        {-11, -11, -10, -9, -8, -7, -5, -3, -1, 0, 1, 2, 4, 5, 5, 6, 6, 6, 6, 6, 6, 5},
        {-11, -11, -10, -9, -8, -7, -5, -3, -1, 0, 1, 2, 4, 5, 5, 6, 6, 6, 6, 6, 6, 5},
       };
    
    /**
     * Creates a new instance of Gray8CannyHoriz
     * @param cSigma the sigma value for the operator, which is the sigma
     * in the Gaussian distribution multipied by 10.0 and converted to integer.
     * @throws jjil.core.Error if cSigma is out of range.
     */
    public Gray8CannyHoriz(int cSigma) throws jjil.core.Error {
        this.setSigma(cSigma);
    }
    
    /**
     * Apply the Canny operator horizontally to the input input image.
     * The sigma value for the operator is set in the class constructor.
     * We handle the borders of the image a little carefully to avoid creating
     * spurious edges at them. The image value at the border is reflected so
     * that image(0,-1), for example, is made equal to image(0,1).
     * @param image the input Gray8Image
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
        Gray8Image result = new Gray8Image(image.getWidth(), image.getHeight());
        byte[] bIn = input.getData();
        byte[] bResult = result.getData();
        int[] wCoeff = this.nCoeff[this.cSigma];
        int cWidth = input.getWidth();
        for (int i=0; i<input.getHeight(); i++) {
            for (int j=0; j<cWidth; j++) {
                /* left side of Canny operator */
                int wSum = 0;
                /* Use Math.abs to mirror the index at the border
                 */
                for (int k=1; k<wCoeff.length; k++) {
                    wSum += wCoeff[k] * bIn[i*cWidth + Math.abs(j - k)];
                }
                /* right side of Canny operator */
                for (int k=0; 
                         k<wCoeff.length; 
                         k++) {
                    if (j + k < cWidth) {
                        wSum += wCoeff[k] * bIn[i*cWidth + j + k];
                    } else {
                        // reflect at border. j + k >= cWidth so
                        // 2*cWidth - (j + k + 1) < cWidth
                        int cPos = 2*cWidth - (j + k + 1);
                        wSum += wCoeff[k] * bIn[i*cWidth + cPos];
                    }
                }
                /* Canny coefficients are scaled so sum of absolute values 
                 * is 256.
                 */
                wSum = wSum >> 8;
                bResult[i*cWidth + j] = (byte) wSum;
            }
        }
        super.setOutput(result);
    }
    
     /** Returns the current value of sigma.
     *
     * @return the sigma value
     */
    public int  getSigma() {
        return this.cSigma;
    }
    
    /**
     * sets a new value for sigma. Sigma controls the frequency of edges
     * that the operator responds to. A small sigma value gives higher
     * frequency edges, while a larger sigma gives lower frequency edges.
     * @param cSigma the new sigma value
     * @throws jjil.core.Error if cSigma is out of range.
     */
    public void setSigma(int cSigma) throws jjil.core.Error {
        if (cSigma <= 1 || cSigma >= this.nCoeff.length) {
            throw new Error(
        			Error.PACKAGE.ALGORITHM,
        			ErrorCodes.PARAMETER_OUT_OF_RANGE,
        			new Integer(cSigma).toString(),
        			new Integer(1).toString(),
        			new Integer(this.nCoeff.length).toString());
        }
        this.cSigma = cSigma;
    }
    
    /** returns a string describing this Canny operator.
     *
     * @return a string describing the Canny operator.
     */
    public String toString() {
        return super.toString() + " (" + this.cSigma + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
