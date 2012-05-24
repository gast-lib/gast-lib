/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Converts an RGB image to an HSV image. The HSV values are
 * scaled to fit in a byte and are stored in a packed word
 * like RGB with the bytes in the same order and position. 
 * (H&rarr;R, S&rarr;G, V&rarr;B).</br>
 * The output image replaces the input.</br>
 * Hue runs from Byte.MIN_VALUE to Byte.MIN_VALUE + 239. Hue
 * is divided into 3 80-value ranges in the order red, green, blue.
 * Saturation and value run from Byte.MIN_VALUE to Byte.MAX_VALUE.</br>
 * The code here was adapted from 
 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
 * with changes to scale everything so it fit in byte values.
 * @author webb
 */
public class RgbHsv extends PipelineStage {

    /**
     * Converts an input RGB image into an HSV image. The input is replaced
     * by the output.<p>
     * The output is an RgbImage with the HSV values stored in the corresponding
     * RGB bytes.
     * @param imageInput input RgbImage 
     * @throws jjil.core.Error if input is not an RgbImage.
     */
    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof RgbImage)) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_RGBIMAGE,
                			imageInput.toString(),
                			null,
                			null);
        }
        RgbImage rgbInput = (RgbImage) imageInput;
        int[] rgbData = rgbInput.getData();
        for (int i=0; i<rgbInput.getWidth()*rgbInput.getHeight(); i++) {
            int nR = RgbVal.getR(rgbData[i]);
            int nG = RgbVal.getG(rgbData[i]);
            int nB = RgbVal.getB(rgbData[i]);
            int nMax, nMid, nMin;
            int nHueOffset;
            // determine color order
            if (nR > nG && nR > nB) {
                // red is max
                nMax = nR;
                nHueOffset = 0;
                if (nG > nB) {
                    nMid = nG;
                    nMin = nB;
                } else {
                    nMid = nB;
                    nMin = nG;
                }
            } else if (nG > nR && nG > nB) {
                // green is max
                nMax = nG;
                nHueOffset = 80;
                if (nR > nB) {
                    nMid = nR;
                    nMin = nB;
                } else {
                    nMid = nB;
                    nMin = nR;
                }
            } else {
                // blue is max
                nMax = nB;
                nHueOffset = 160;
                if (nR > nG) {
                    nMid = nR;
                    nMin = nG;
                } else {
                    nMid = nG;
                    nMin = nR;
                }
            }
            // if the max value is Byte.MIN_VALUE the RGB value
            // = 0 so the HSV value = 0 and needs no change.
            if (nMax > Byte.MIN_VALUE) {
                if (nMax == nMin) {
                    // color is gray. Hue, saturation are 0.
                    rgbData[i] = RgbVal.toRgb(
                            Byte.MIN_VALUE,
                            Byte.MIN_VALUE,
                            (byte) nMax);
                } else {
                    // compute hue scaled from 0-240.
                    int nHue = Math.min(239, nHueOffset + (40 * (nMid - nMin)) 
                            / (nMax - nMin));
                    // compute saturation scaled from 0-255.
                    int nSat = Math.min(255, (256 * (nMax - nMin)) 
                            / (nMax - Byte.MIN_VALUE));
                    rgbData[i] = RgbVal.toRgb(
                            (byte) (nHue + Byte.MIN_VALUE),
                            (byte) (nSat + Byte.MIN_VALUE),
                            (byte) nMax);
                }
            }
        }
        super.setOutput(rgbInput);
    }

}
