/*
 * GaussHoriz.java
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
 * Computes a vertical Gaussian blur for an input gray image. The sigma
 * value for the operator is set in the constructor or setSigma. All calculations
 * are done in integer (per CLDC 1.0) and the sigma value is specified as
 * multiplied by 10.0. The minimum value for the unmultiplied sigma is 0.1;
 * the maximum value is about 10.0. Larger sigma values give an operator which
 * is more blurred.
 * <p>
 * Hungarian prefix is 'gshz'.
 * @author webb
 */
public class Gray8GaussSmoothVert extends PipelineStage {
    /** cSigma is the sigma value we'll be using. It has been multiplied
     * by 10.0 and converted to integer because CLDC 1.0 doesn't allow
     * floating point. The minimum legal value for cSigma is 1; the maximum
     * is given by the length of nCoeff below.
     */
    private int cSigma;
    /** nCoeff row i is the precomputed Gauss coefficients for sigma = i/10.0.
     * They have been scaled by 256 and converted to integer because CLDC 1.0
     * doesn't allow floating point. The coefficients have been scaled and
     * normalized so the sum is 0 and the sum of the absolute values is
     * 256 (counting both sides of the Canny operator -- the operator is
     * symmetric so we just give one side below.) 
     */
    /** The array below was generated using two Excel macros:
     * In cell b2 and filled out to column AM and down: 
     *      =EXP(-(COLUMN(B2)-1)*(COLUMN(B2)-1)/((ROW(B2)-1)/10*(ROW(B2)-1)/10))
     * To compute the value shown below:
     *      =ROUND(B2/SUM($B2:$AM2)*256,0)
     */
    private int[][] nCoeff = {
	{0},  // unused, inserted to make lookup simpler
	{256},
	{256},
	{256},
	{256},
	{256},
	{256},
	{255,1},
	{254,2},
	{250,6},
	{244,12},
	{236,20},
	{227,28,1},
	{217,37,2},
	{207,45,4},
	{198,52,6},
	{189,58,8,1},
	{180,64,11,1},
	{172,68,15,2},
	{164,71,18,3},
	{157,74,21,4},
	{150,76,24,5,1},
	{144,77,28,6,1},
	{138,78,30,8,1},
	{132,79,33,10,2},
	{127,79,35,12,3},
	{122,79,37,13,4,1},
	{118,78,39,15,4,1},
	{114,78,41,17,5,1},
	{110,77,42,18,6,2},
	{106,76,44,20,7,2,1},
	{103,75,45,22,8,3,1},
	{99,74,46,23,10,3,1},
	{96,73,46,24,11,4,1},
	{93,72,47,26,12,5,1},
	{91,71,47,27,13,5,2,1},
	{88,70,48,28,14,6,2,1},
	{86,69,48,29,15,7,3,1},
	{83,68,48,29,16,7,3,1},
	{81,67,48,30,17,8,3,1},
	{79,65,48,31,18,9,4,2,1},
	{77,64,48,32,18,10,4,2,1},
	{75,63,48,32,19,10,5,2,1},
	{73,62,48,33,20,11,5,2,1},
	{72,61,47,33,21,12,6,3,1},
	{70,60,47,33,21,12,7,3,1,1},
	{68,59,47,34,22,13,7,3,2,1},
	{67,58,46,34,23,14,8,4,2,1},
	{65,57,46,34,23,14,8,4,2,1},
	{64,56,46,34,24,15,9,5,2,1},
	{63,55,45,34,24,15,9,5,3,1,1},
	{61,55,45,34,24,16,10,5,3,1,1},
	{60,54,45,34,25,16,10,6,3,2,1},
	{59,53,44,35,25,17,11,6,3,2,1},
	{58,52,44,35,25,17,11,7,4,2,1},
	{57,51,43,34,26,18,12,7,4,2,1,1},
	{56,50,43,34,26,18,12,7,4,2,1,1},
	{55,50,43,34,26,19,12,8,5,3,1,1},
	{54,49,42,34,26,19,13,8,5,3,2,1},
	{53,48,42,34,26,19,13,9,5,3,2,1},
	{52,48,41,34,27,20,14,9,6,3,2,1},
	{51,47,41,34,27,20,14,9,6,4,2,1,1},
	{50,46,41,34,27,20,14,10,6,4,2,1,1},
	{49,46,40,34,27,20,15,10,7,4,2,1,1},
	{48,45,40,33,27,21,15,10,7,4,3,1,1},
	{48,44,39,33,27,21,15,11,7,5,3,2,1},
	{47,44,39,33,27,21,16,11,7,5,3,2,1,1},
	{46,43,39,33,27,21,16,11,8,5,3,2,1,1},
	{45,42,38,33,27,21,16,12,8,5,3,2,1,1},
	{45,42,38,33,27,21,16,12,8,6,4,2,1,1},
	{44,41,37,32,27,22,17,12,9,6,4,2,1,1},
	{43,41,37,32,27,22,17,12,9,6,4,3,2,1,1},
	{43,40,37,32,27,22,17,13,9,6,4,3,2,1,1},
	{42,40,36,32,27,22,17,13,9,7,4,3,2,1,1},
	{41,39,36,32,27,22,17,13,10,7,5,3,2,1,1},
	{41,39,35,31,27,22,17,13,10,7,5,3,2,1,1},
	{40,38,35,31,27,22,18,14,10,7,5,3,2,1,1},
	{40,38,35,31,27,22,18,14,10,7,5,4,2,1,1,1},
	{39,37,34,31,26,22,18,14,11,8,5,4,2,2,1,1},
	{39,37,34,30,26,22,18,14,11,8,6,4,3,2,1,1},
	{38,36,34,30,26,22,18,14,11,8,6,4,3,2,1,1},
	{38,36,33,30,26,22,18,14,11,8,6,4,3,2,1,1},
	{37,36,33,30,26,22,18,15,11,9,6,4,3,2,1,1,1},
	{37,35,33,30,26,22,18,15,12,9,6,5,3,2,1,1,1},
	{36,35,32,29,26,22,18,15,12,9,7,5,3,2,2,1,1},
	{36,34,32,29,26,22,18,15,12,9,7,5,4,2,2,1,1},
	{35,34,32,29,26,22,19,15,12,9,7,5,4,3,2,1,1},
	{35,34,32,29,26,22,19,15,12,9,7,5,4,3,2,1,1},
	{35,33,31,29,25,22,19,15,12,10,7,5,4,3,2,1,1,1},
	{34,33,31,28,25,22,19,15,12,10,8,6,4,3,2,1,1,1},
	{34,33,31,28,25,22,19,16,13,10,8,6,4,3,2,1,1,1},
	{33,32,30,28,25,22,19,16,13,10,8,6,4,3,2,2,1,1},
	{33,32,30,28,25,22,19,16,13,10,8,6,5,3,2,2,1,1},
	{33,32,30,27,25,22,19,16,13,10,8,6,5,3,2,2,1,1,1},
	{32,31,30,27,25,22,19,16,13,11,8,6,5,4,3,2,1,1,1},
	{32,31,29,27,25,22,19,16,13,11,8,7,5,4,3,2,1,1,1},
	{32,31,29,27,24,22,19,16,13,11,9,7,5,4,3,2,1,1,1},
	{31,30,29,27,24,22,19,16,13,11,9,7,5,4,3,2,1,1,1},
	{31,30,28,26,24,21,19,16,13,11,9,7,5,4,3,2,2,1,1},
	{31,30,28,26,24,21,19,16,14,11,9,7,6,4,3,2,2,1,1,1},
	{30,29,28,26,24,21,19,16,14,11,9,7,6,4,3,2,2,1,1,1}
       };
    
    /** Creates a new instance of GaussHoriz 
     *
     * @param cSigma the sigma value for the operator, which is the sigma
     * in the Gaussian distribution multipied by 10.0 and converted to integer.
     * @throws jjil.core.Error is sigma is out of range.
     */
    public Gray8GaussSmoothVert(int cSigma) throws jjil.core.Error {
        this.setSigma(cSigma);
    }
    
    /** Apply the Gaussian horizontally to the input input image.
     * The sigma value for the operator is set in the class constructor.
     * We handle the borders of the image a little carefully to avoid creating
     * spurious edges at them. The image value at the border is reflected so
     * that image(-1,0), for example, is made equal to image(1,0). 
     *
     * @param image the input Gray8Image
     * @throws jjil.core.Error if image is not a Gray8Image
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
        int cHeight = input.getHeight();
        for (int j=0; j<input.getWidth(); j++) {
            for (int i=0; i<cHeight; i++) {
                /* left side of Gaussian */
                int wSum = 0;
                /* Use Math.abs to mirror the index at the border
                 */
                for (int k=1; k<wCoeff.length; k++) {
                    wSum += wCoeff[k] * bIn[Math.abs(i-k)*cHeight + j];
                }
                /* right side of Gaussian */
                for (int k=0; 
                         k<wCoeff.length; 
                         k++) {
                    if (i + k < cHeight) {
                        wSum += wCoeff[k] * bIn[(i+k)*cHeight + j];
                    } else {
                        // reflect at border. i + k >= cHeight so
                        // 2*cWidth - (i + k + 1) < cHeight
                        int cPos = 2*cHeight - (i + k + 1);
                        wSum += wCoeff[k] * bIn[cPos*cHeight + j];
                    }
                }
                /* Gaussian coefficients are scaled so sum is 256.
                 */
                wSum = wSum >> 8;
                bResult[i*cHeight + j] = (byte) wSum;
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
    
    /** sets a new value for sigma. Sigma controls the frequency of edges
     * that the operator responds to. A small sigma value gives less blur.
     *
     * @param cSigma the new sigma value
     * @throws jjil.core.Error if cSigma is out of range --
     * less than or equal to 1 or greater than the number of coefficients
     * we're precomputed.
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
    
    /** returns a string describing this Gaussian blur.
     *
     * @return a string describing the Gaussian blur.
     */
    public String toString() {
        return super.toString() + " (" + this.cSigma + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
