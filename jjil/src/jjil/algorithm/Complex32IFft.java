/*
 * Complex32IFft.java
 *
 * Created on October 31, 2007, 5:02 PM
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
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Computes the inverse FFT of the input Complex32Image. The output is a Gray8Image,
 * which is the magnitude of the inverse FFT. The output can be scaled so the
 * maximum and minimum values of the magnitude are mapped to Byte.MAX_VALUE and
 * Byte.MIN_VALUE.
 * @author webb
 */
public class Complex32IFft extends PipelineStage {
    private boolean bScale;
    private Fft1d fft = null;
    
    /**
     * Creates a new instance of Complex32IFft
     * @param bScale Whether or not to scale the output before converting it to a byte.
     */
    public Complex32IFft(boolean bScale) {
        this.bScale = bScale;
    }
    
    /**
     * Perform the inverse FFT on the input Complex32Image, producing a Gray8Image.
     * @param im Input image. Must be a power of 2 in size and of type Complex32Image.
     * @throws jjil.core.Error if the input is not a power of 2 in size or not a Complex32Image.
     */
    public void push(Image im) throws jjil.core.Error {
        if (!(im instanceof Complex32Image)) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_COMPLEX32IMAGE,
            				im.toString(),
            				null,
            				null);
        }
        // make sure the image width and height are powers of two
        int nWidth = im.getWidth();
        int nHeight = im.getHeight();
        if ((nWidth & (nWidth-1)) != 0) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.FFT_SIZE_NOT_POWER_OF_2,
            				im.toString(),
            				null,
            				null);
        }
        if ((nHeight & (nHeight-1)) != 0) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.FFT_SIZE_NOT_POWER_OF_2,
            				im.toString(),
            				null,
            				null);
        }
        // initialize FFT
        if (this.fft == null) {
            this.fft = new Fft1d(Math.max(nWidth, nHeight));
        } else {
            this.fft.setMaxWidth(Math.max(nWidth, nHeight));
        }
         // get access to the complex image
        Complex32Image cxmIn = (Complex32Image) im;
        Complex data[] = cxmIn.getData();
        // create output
        Complex32Image cxmResult = new Complex32Image(nWidth, nHeight);
        // take inverse FFT of each row
        Complex cxRow[] = new Complex[nWidth];
        for (int i=0; i<nHeight; i++) {
            System.arraycopy(data, i*nWidth, cxRow, 0, nWidth);
            // compute inverse FFT
            Complex cxResult[] = this.fft.ifft(cxRow);
            // save result
            System.arraycopy(cxResult, 0, cxmResult.getData(), i*nWidth, nWidth);
        }
        // take inverse FFT of each column
        Complex cxCol[] = new Complex[nHeight];
        for (int j=0; j<nWidth; j++) {
            // copy column into a 1-D array
            for (int i=0; i<nHeight; i++) {
                cxCol[i] = cxmResult.getData()[i*nWidth+j];
            }
            // compute inverse FFT
            Complex cxResult[] = this.fft.ifft(cxCol);
            // save result back into column
             for (int i=0; i<nHeight; i++) {
                cxmResult.getData()[i*nWidth+j] = cxResult[i];
            }
        }
        // convert back to a gray image
        // first convert it to an integer image
        Gray32Image imInteger = new Gray32Image(nWidth, nHeight);
        Complex cxData[] = cxmResult.getData();
        int nData[] = imInteger.getData();
        int nMinVal = Integer.MAX_VALUE;
        int nMaxVal = Integer.MIN_VALUE;
        for (int i = 0; i < nWidth * nHeight; i++) {
            // magnitude is always guaranteed to be >= 0 so we only have to clamp
            // below Byte.MAX_VALUE
            nData[i] = cxData[i].rsh(Gray8Fft.SCALE).magnitude();
            if (this.bScale) {
                nMinVal = Math.min(nMinVal, nData[i]);
                nMaxVal = Math.max(nMaxVal, nData[i]);
            }
        }
        // compute range of values in image and avoid division by 0 later
        int nDiff = Math.max(nMaxVal - nMinVal, 1);
        // this inverts the operation in Gray8Fft. The two must be kept in sync.
        Gray8Image imResult = new Gray8Image(nWidth, nHeight);
        byte bData[] = imResult.getData();
        if (bScale) {
            for (int i = 0; i < nWidth * nHeight; i++) {
                // magnitude is always guaranteed to be >= 0 so we only have to clamp
                // below Byte.MAX_VALUE
                bData[i] = (byte)  
                    (((nData[i] - nMinVal) * (Byte.MAX_VALUE - Byte.MIN_VALUE)) / 
                        nDiff + Byte.MIN_VALUE);
            }            
        } else {
            for (int i = 0; i < nWidth * nHeight; i++) {
                // magnitude is always guaranteed to be >= 0 so we only have to clamp
                // below Byte.MAX_VALUE
                bData[i] = (byte) Math.min(Byte.MAX_VALUE, nData[i] + Byte.MIN_VALUE);
            }
        }
        super.setOutput(imResult);
    }
}
