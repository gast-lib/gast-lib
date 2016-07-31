/*
 * Gray8WienerDeconv.java
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
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.MathPlus;
import jjil.core.PipelineStage;
/**
 * Wiener deconvolution of input Gray8Image. You specify a point spread function
 * as a Gray8Image and a noise level. See PsfGray8 for point spread function
 * generating methods. The computation is done in the Fourier domain. The output
 * is of type Complex32Image.
 * @author webb
 */
public class Gray8WienerDeconv extends PipelineStage {
    private int nNoise;
    private static final int nThreshold = 5;
    Gray8Fft fft;
    Complex32Image cxmPsfInv;
    Gray32Image gPsfSq;
    
    /**
     * Creates a new instance of Gray8WienerDeconv.
     * @param psf the input point spread function. This is the expected blur
     * window, for example a disk or rectangle.
     * @param nNoise the noise level.
     * @throws jjil.core.Error if the input point spread function is not a Gray8Image or not square.
     */
    public Gray8WienerDeconv(Gray8Image psf, int nNoise) throws jjil.core.Error {
        if (psf.getWidth() != psf.getHeight()) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_SQUARE,
            				psf.toString(),
            				null,
            				null);
        }
        if (!(psf instanceof Gray8Image)) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
            				psf.toString(),
            				null,
            				null);
        }
        this.nNoise = nNoise;
        this.fft = new Gray8Fft();
        this.fft.push(psf);
        this.cxmPsfInv = (Complex32Image) this.fft.getFront();
        invertPsf();
    }
    
    /**
     * Compute the deconvolution of the input Gray8Image, producing a Complex32Image.
     * @param im the input Gray8Image.
     * @throws jjil.core.Error if the input image is not a Gray8Image or not square.
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
        if (im.getWidth() != this.cxmPsfInv.getWidth() ||
        	im.getHeight() != this.cxmPsfInv.getHeight()) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_SIZES_DIFFER,
            				im.toString(),
            				this.cxmPsfInv.toString(),
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
        Complex cxPsfInv[] = this.cxmPsfInv.getData();
        int nPsfSq[] = this.gPsfSq.getData();
        // compute Wiener filter
        for (int i=0; i<im.getWidth() * im.getHeight(); i++) {
            int nMag = cxIn[i].magnitude();
            int nScale = (nPsfSq[i] * nMag) / ((nPsfSq[i] * nMag) + this.nNoise);
            cxOut[i] = cxIn[i].times(cxPsfInv[i]).times(nScale).rsh(MathPlus.SHIFT);
        }
        super.setOutput(cxmResult);
    }
    
    private void invertPsf() throws jjil.core.Error {
        this.gPsfSq = new Gray32Image(this.cxmPsfInv.getWidth(), this.cxmPsfInv.getHeight());
        Complex cxPsf[] = this.cxmPsfInv.getData();
        int nData[] = this.gPsfSq.getData();
        for (int i=0; i<this.cxmPsfInv.getWidth() * this.cxmPsfInv.getHeight(); i++) {
            if (Math.abs(cxPsf[i].real()) > MathPlus.SCALE ||
                Math.abs(cxPsf[i].imag()) > MathPlus.SCALE) {
                cxPsf[i] = new Complex(0);
                nData[i] = 1;
            } else {
                int nSq = cxPsf[i].square();
                nData[i] = nSq;
                if (nSq < Gray8WienerDeconv.nThreshold) {
                    // if the square value is too small we will be enhancing noise
                    // too much
                    cxPsf[i] = new Complex(MathPlus.SCALE);
                    nData[i] = 1;
                } else {
                    cxPsf[i] = new Complex(MathPlus.SCALE).div(cxPsf[i]);
                }
            }
        }
    }
}
