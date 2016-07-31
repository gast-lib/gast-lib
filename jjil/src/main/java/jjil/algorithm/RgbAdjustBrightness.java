/*
 * RgbAdjustBrightness.java
 *
 * Created on August 27, 2006, 2:38 PM
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
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Pipeline stage adjusts brightness of red, green, and blue bands independently
 * by a multiplicative factor.
 * @author webb
 */
public class RgbAdjustBrightness extends PipelineStage {
    int nRedFac, nGreenFac, nBlueFac;
    
    /** Creates a new instance of RgbAdjustBrightness. Multiplicative factors
     * are specified here.
     *
     * @param nRed red factor (scaled by 256)
     * @param nGreen green factor (scaled by 256)
     * @param nBlue blue factor (scaled by 256)
     */
    public RgbAdjustBrightness(
            int nRed,
            int nGreen,
            int nBlue) {
        this.nRedFac = nRed;
        this.nGreenFac = nGreen;
        this.nBlueFac = nBlue;
    }
    
    /** Adjust brightness of RGB image. This is an in-place modification;
     * input is modified.
     *
     * @param image the input image.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbImage)) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_RGBIMAGE,
                			image.toString(),
                			null,
                			null);
        }
        RgbImage imageInput = (RgbImage) image;
        int[] rgb = imageInput.getData();
        for (int i=0; i<imageInput.getHeight() * imageInput.getWidth(); i++) {
            // the scaling has to be done on unsigned values.
            int nRed = RgbVal.getR(rgb[i]) - Byte.MIN_VALUE;
            int nGreen = RgbVal.getG(rgb[i]) - Byte.MIN_VALUE;
            int nBlue = RgbVal.getB(rgb[i]) - Byte.MIN_VALUE;
            // scale and convert back to signed byte values
            nRed = Math.max(Byte.MIN_VALUE, 
                    Math.min(Byte.MAX_VALUE, 
                    nRed * this.nRedFac / 256 + Byte.MIN_VALUE));
            nGreen = Math.max(Byte.MIN_VALUE, 
                    Math.min(Byte.MAX_VALUE, 
                    nGreen * this.nGreenFac / 256 + Byte.MIN_VALUE));
            nBlue = Math.max(Byte.MIN_VALUE, 
                    Math.min(Byte.MAX_VALUE, 
                    nBlue * this.nBlueFac / 256 + Byte.MIN_VALUE));
            rgb[i] = RgbVal.toRgb((byte) nRed, (byte) nGreen, (byte) nBlue);
        }
        super.setOutput(imageInput);
    }
    
    
    
    /** Return a string describing the cropping operation.
     *
     * @return the string describing the cropping operation.
     */
    public String toString() {
        return super.toString() + " (" + this.nRedFac + "," +
                this.nGreenFac + "," +
                this.nBlueFac + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
