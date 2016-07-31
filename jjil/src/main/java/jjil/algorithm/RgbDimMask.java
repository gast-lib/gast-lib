/*
 * RgbDimMask.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2008 by Jon A. Webb
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
import jjil.core.RgbMaskedImage;
import jjil.core.RgbVal;

/**
 * Pipeline stage shows a masked area in an RgbMaskedImage by dimming
 * or brightening the color value in the masked area a specified amount.
 * @author webb
 */
public class RgbDimMask extends PipelineStage {
    int nDim; // amount to dim, scaled by 256
    
    /**
     * Creates a new instance of RgbDimMask that will dim any RgbMaskedImage
     * according the to nDim parameter.
     * @param nDim the amount to dim, scaled by 256. nDim > 256 actually
     * brightens the mask area.
     */
    public RgbDimMask(int nDim) {
        this.nDim = nDim;
    }
    

    /** Dim the input RgbMaskedImage by the amount specified in the nDim
     * parameter in the constructor.
     * @param image the input image.
     * @throws jjil.core.Error if  the input image
     *    is not an RgbImage.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbMaskedImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                            image.toString(),
                            "RgbMaskedImage",
                            null);
        }
        RgbMaskedImage rgbImage = (RgbMaskedImage) image;
        int[] src = rgbImage.getData();
        RgbImage rgbOutput = new RgbImage(rgbImage.getWidth(), rgbImage.getHeight());
        int[] dst = rgbOutput.getData();
        for (int i=0; i<rgbImage.getHeight(); i++) {
            for (int j=0; j<rgbImage.getWidth(); j++) {
                int nColorPixel = src[i*rgbImage.getWidth() + j];
                if (rgbImage.isMasked(i, j)) {
                    int nRed = RgbVal.getR(nColorPixel);
                    int nGreen = RgbVal.getG(nColorPixel);
                    int nBlue = RgbVal.getB(nColorPixel);
                    nRed = Math.max(Byte.MIN_VALUE, 
                                Math.min(Byte.MAX_VALUE, 
                                (nRed*this.nDim)>>8));
                    nGreen = Math.max(Byte.MIN_VALUE, 
                                Math.min(Byte.MAX_VALUE, 
                                (nGreen*this.nDim)>>8));
                    nBlue = Math.max(Byte.MIN_VALUE, 
                                Math.min(Byte.MAX_VALUE, 
                                (nBlue*this.nDim)>>8));
                    dst[i*rgbImage.getWidth()+j] = 
                        RgbVal.toRgb((byte)nRed, (byte)nGreen, (byte)nBlue);
                } else {
                    dst[i*rgbImage.getWidth()+j] = nColorPixel;
                }
            }
        }
        super.setOutput(rgbOutput);
    }
        
    /** Return a string describing the clipping operation.
     *
     * @return the string describing the clipping operation.
     */
    public String toString() {
        return super.toString() + " (" + 
                new Integer(this.nDim).toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
