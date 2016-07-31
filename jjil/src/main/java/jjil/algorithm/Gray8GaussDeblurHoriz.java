/*
 * Gray8GaussDeblurHoriz.java
 *
 * Created on November 3, 2007, 3:07 PM
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
import jjil.core.Complex;
import jjil.core.Complex32Image;
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
/**
 * Uses deconvolution to remove blur from a Gray8Image. The blur removed is a 
 * horizontal Gaussian blur with a given standard deviation. The background noise
 * level in the input image can be adjusted. The output Gray8Image is rescaled so the
 * maximum and minimum values fill the range from Byte.MIN_VALUE to Byte.MAX_VALUE.
 * @author webb
 */
public class Gray8GaussDeblurHoriz extends PipelineStage {
    private int nNoise;
    private int nStdDev;
    Gray8Fft fft;
    Complex32IFft ifft;

    /**
     * These coefficients are the Fourier transform of the Gaussian
     *      f(t) = exp(-t**2 / (2*sigma**2))
     * which is the Gaussian
     *      F(k) = sigma * sqrt(2*pi) * exp(-2*(pi*k*sigma)**2)
     * where t ranges from 0.01 to 1.0 (t is multiplied by 100).
     * They have been multiplied by 256 and rounded to the nearest integer.
     * Coefficients of zero have been dropped.
     * They were computed by putting the Excel macro
     *  =ROW(A1)/100*SQRT(2*PI())*EXP(-2*POWER(PI()*(COLUMN(A1)-1)*ROW(A1)/100,2))
     * in cell A1, filling to create a table with 100 rows, then rounding with
     *  =ROUND(A1*256,0)
     */
    private int rxnCoeffs[][] = {
	{6,6,6,6,6,6,6,6,6,5,5,5,5,5,4,4,4,4,3,3,3,3,2,2,2,2,2,2,1,1,1,1,1,1,1,1},
	{13,13,12,12,11,11,10,9,8,7,6,5,4,3,3,2,2,1,1,1,1},
	{19,19,18,16,14,12,10,8,6,5,3,2,1,1,1},
	{26,25,23,19,15,12,8,5,3,2,1,1},
	{32,31,26,21,15,9,5,3,1,1},
	{39,36,29,20,12,7,3,1},
	{45,41,31,19,10,4,1},
	{51,45,31,16,7,2,1},
	{58,49,30,14,4,1},
	{64,53,29,11,3},
	{71,56,27,8,2},
	{77,58,25,6,1},
	{83,60,22,4},
	{90,61,19,3},
	{96,62,16,2},
	{103,62,14,1},
	{109,62,11,1},
	{116,61,9},
	{122,60,7},
	{128,58,5},
	{135,56,4},
	{141,54,3},
	{148,52,2},
	{154,49,2},
	{160,47,1},
	{167,44,1},
	{173,41,1},
	{180,38},
	{186,35},
	{193,33},
	{199,30},
	{205,27},
	{212,25},
	{218,22},
	{225,20},
	{231,18},
	{237,16},
	{244,14},
	{250,12},
	{257,11},
	{263,10},
	{270,8},
	{276,7},
	{282,6},
	{289,5},
	{295,5},
	{302,4},
	{308,3},
	{314,3},
	{321,2},
	{327,2},
	{334,2},
	{340,1},
	{347,1},
	{353,1},
	{359,1},
	{366,1},
	{372},
	{379},
	{385},
	{391},
	{398},
	{404},
	{411},
	{417},
	{424},
	{430},
	{436},
	{443},
	{449},
	{456},
	{462},
	{468},
	{475},
	{481},
	{488},
	{494},
	{501},
	{507},
	{513},
	{520},
	{526},
	{533},
	{539},
	{545},
	{552},
	{558},
	{565},
	{571},
	{578},
	{584},
	{590},
	{597},
	{603},
	{610},
	{616},
	{622},
	{629},
	{635},
    };
    
    /**
     * Creates a new instance of Gray8GaussDeblurHoriz.
     * @param nStdDev Standard deviation of the Gaussian blur operator to deblur with. The
     * value is multiplied by 100 so a value of 5 corresponds to a standard deviation
     * of the Gaussian of 0.05.
     * @param nNoise The expected noise level in the input image. The deconvolution has the
     * potential to amplify noise levels since it is a high pass filter. The noise
     * parameter limits this by not dividing by any Gaussian element (in the 
     * frequency domain) less than this value. The Gaussian elements have been scaled
     * by 256, so a value equal to, say, 64 keeps the maximum amplification of the
     * deconvolution less than 256/64 = 4.
     * @throws jjil.core.Error if the standard deviation parameter is out of range.
     */
    public Gray8GaussDeblurHoriz(int nStdDev, int nNoise) throws jjil.core.Error {
        setStdDev(nStdDev);
        this.nNoise = nNoise;
        this.fft = new Gray8Fft();
        this.ifft = new Complex32IFft(true);
    }
    
    /**
     * Deblurs an input Gray8Image which has been blurred by a horizontal Gaussian
     * of the given standard deviation and which has a background noise level less
     * than the given level.
     * @param im Input Gray8Image.
     * @throws jjil.core.Error if the input is not a Gray8Image or is not square.
     */
    public void push(Image im) throws jjil.core.Error {
        if (im.getWidth() != im.getHeight()) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_SQUARE,
                            im.toString(),
                            null,
                            null);
        }
        if (!(im instanceof Gray8Image)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                            im.toString(),
                            null,
                            null);
        }
        this.fft.push(im);
        Complex32Image cxmIm = (Complex32Image) this.fft.getFront();
        Complex cxIn[] = cxmIm.getData();
        Complex32Image cxmResult = new Complex32Image(im.getWidth(), im.getHeight());
        Complex cxOut[] = cxmResult.getData();
        // compute inverse filter
        int rnCoeff[] = this.rxnCoeffs[this.nStdDev];

        for (int i=0; i<cxmIm.getHeight(); i++) {
            int nRow = i * cxmIm.getWidth();
            for (int j=0; j<cxmIm.getWidth(); j++) {
                int nCoeff;
                if (j < cxmIm.getWidth()/2) {
                    nCoeff = j;
                } else {
                    nCoeff = cxmIm.getWidth() - j;
                }
                if (nCoeff < rnCoeff.length &&
                    rnCoeff[nCoeff] > this.nNoise) {
                    cxOut[nRow + j] = cxIn[nRow + j].lsh(8).div(rnCoeff[nCoeff]);
                } else {
                    cxOut[nRow + j] = cxIn[nRow + j];
                }
            }            
        }
        // inverse FFT to get result
        this.ifft.push(cxmResult);
        super.setOutput(this.ifft.getFront());
    }
    
    /**
     * Changes current standard deviation value.
     * @param nStdDev Input standard deviation, multiplied by 100.
     * @throws jjil.core.Error if the parameter is out of range.
     */
    public void setStdDev(int nStdDev) throws jjil.core.Error {
        if (nStdDev < 0 || nStdDev > this.rxnCoeffs.length) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.PARAMETER_OUT_OF_RANGE,
                            new Integer(nStdDev).toString(),
                            new Integer(0).toString(),
                            new Integer(this.rxnCoeffs.length).toString());
        }
        this.nStdDev = nStdDev;
    }
}
