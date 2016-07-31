/*
 * RgbClip.java
 *
 * Created on May 13, 2006, 2:38 PM
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
 * Pipeline stage performs color clipping, setting all pixels that
 * do not meet the threshold test to 0, otherwise leaving them alone.
 * <p>
 * The test is abs(pixel.R - red) + abs(pixel.G - green) + abs(pixel.B - blue)
 * < limit.
 * <p>
 * The test direction can be reversed using the dir parameter.
 * @author webb
 */
public class RgbClip extends PipelineStage {
    boolean bDir; /* clipping direction */
    byte nB; /* blue value */
    byte nG; /* green value */
    int nLimit; /* the clipping limit */
    byte nR; /* red value */
    
    /**
     * Creates a new instance of RgbClip. The clip test is defined here.
     * 
     * 
     * @param r red value
     * @param g green value
     * @param b value
     * @param l the threshold
     * @param dir if true pixels that fail test are set to 0; if false
     * pixels that pass test are set to 0.
     */
    public RgbClip(
            byte r,
            byte g,
            byte b,
            int l,
            boolean dir) {
        this.nR = r;
        this.nG = g;
        this.nB = b;
        this.nLimit = l;
        this.bDir = dir;
    }
    
    /**
     * Creates a new instance of RgbClip. The clip test is defined here.
     * 
     * 
     * @param rgb int value containg packed RGB
     * @param l the threshold
     * @param dir if true pixels that fail test are set to 0; if false
     * pixels that pass test are set to 0.
     */
    public RgbClip(
            int rgb,
            int l,
            boolean dir) {
        this.nR = RgbVal.getR(rgb);
        this.nG = RgbVal.getG(rgb);
        this.nB = RgbVal.getB(rgb);
        this.nLimit = l;
        this.bDir = dir;
    }

    /** Clips the RGB image and sets all pixels that fail/pass the
     * test (according to bDir) to 0.
     *
     * @param image the input image.
     * @throws jjil.core.Error if the cropping window
     *    extends outside the input image, or the input image
     *    is not an RgbImage.
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
        RgbImage rgbImage = (RgbImage) image;
        int[] src = rgbImage.getData();
        int nWidth = rgbImage.getWidth();
        for (int i=0; i<rgbImage.getHeight(); i++) {
            for (int j=0; j<rgbImage.getWidth(); j++) {
                int nColorPixel = src[i*nWidth + j];
                int nRed = RgbVal.getR(nColorPixel);
                int nGreen = RgbVal.getG(nColorPixel);
                int nBlue = RgbVal.getB(nColorPixel);
                int nDiff = Math.abs(nRed - this.nR) + 
                            Math.abs(nGreen - this.nG) + 
                            Math.abs(nBlue - this.nB);
                if ((nDiff < this.nLimit) != this.bDir) {
                    src[i*nWidth+j] = 0;
                }
            }
        }
        super.setOutput(image);
    }
        
    /**
     * Change the threshold parameters.
     * @param r red value
     * @param g green value
     * @param b value
     * @param l the threshold
     * @param dir if true pixels that fail test are set to 0; if false
     * pixels that pass test are set to 0.
     */
    public void setParameters(
            byte r,
            byte g,
            byte b,
            int l,
            boolean dir) {
        this.nR = r;
        this.nG = g;
        this.nB = b;
        this.nLimit = l;
        this.bDir = dir;
    }
    
    /** Return a string describing the clipping operation.
     *
     * @return the string describing the clipping operation.
     */
    public String toString() {
        return super.toString() + " (" + this.nR + "," + this.nG +  //$NON-NLS-1$ //$NON-NLS-2$
                "," + this.nB + "," + this.nLimit + "," + this.bDir + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
