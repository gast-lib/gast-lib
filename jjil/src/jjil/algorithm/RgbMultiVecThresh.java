/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Thresholds an RgbImage against a number of input colors.
 * @author webb
 */
public class RgbMultiVecThresh extends PipelineStage {
    /**
     * Simplify lookup of color values in array.
     */
    private final int R = 0;
    private final int G = 1;
    private final int B = 2;
    
    private int[][] nRgbVals;
    /**
     * Input color values, unpacked.
     */
    private int[][] nRgbVecs;
    /**
     * Input threshold value.
     */
    private int nThreshold;
    /**
     * Thresholds an RgbImage against a number of input colors.
     * Each color is described using a target value (rgbVal) and
     * a vector (rgbVec). The idea is that a pixel matches a target
     * value if its difference from the target value, projected
     * on the vector, is less than the threshold. One threshold
     * is used for all colors and the minimum absolute value of
     * all color distances is compared with the threshold. Since
     * the vectors can be unnormalized the relative importance of
     * each target color value can be adjusted. </br>
     * One way to use this is to set the target color to the mean
     * Rgb value of a region and the target vector to the standard
     * deviation. The threshold would then be the standard deviation
     * squared. Pixels further away than one standard deviation would
     * be rejected.
     * @param rgbVals packed arry of target Rgb values
     * @param rgbVecs packed array of target Rgb vectors
     * @param nThreshold threshold value
     * @throws Error if the input Rgb vectors are not the same
     * length.
     */
    public RgbMultiVecThresh(int [] rgbVals, int[] rgbVecs, int nThreshold) 
            throws Error
    {
        if (rgbVals.length != rgbVecs.length) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.PARAMETER_SIZES_DIFFER,
                            rgbVals.toString(),
                            rgbVals.toString(),
                            null);
}
        this.nRgbVecs = new int[rgbVecs.length][3];
        this.nRgbVals = new int[rgbVecs.length][3];
        for (int i=0; i<rgbVecs.length; i++) {
            this.nRgbVals[i][R] = RgbVal.getR(rgbVals[i]);
            this.nRgbVals[i][G] = RgbVal.getG(rgbVals[i]);
            this.nRgbVals[i][B] = RgbVal.getB(rgbVals[i]);
            
            this.nRgbVecs[i][R] = RgbVal.getR(rgbVecs[i]);
            this.nRgbVecs[i][G] = RgbVal.getG(rgbVecs[i]);
            this.nRgbVecs[i][B] = RgbVal.getB(rgbVecs[i]);
        }
        this.nThreshold = nThreshold;
    }

    /**
     * Compares input RgbImage with the color values set in the
     * constructor and outputs Byte.MAX_VALUE for any pixels within
     * the threshold value of any of the input colors. Other
     * pixels get Byte.MIN_VALUE.
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
        int [] rgbData = rgbInput.getData();
        Gray8Image grayOutput = new Gray8Image(
                rgbInput.getWidth(), 
                rgbInput.getHeight());
        byte [] grayData = grayOutput.getData();
        for (int i=0; i<rgbInput.getHeight()*rgbInput.getWidth(); i++) {
            int nR = RgbVal.getR(rgbData[i]);
            int nG = RgbVal.getG(rgbData[i]);
            int nB = RgbVal.getB(rgbData[i]);
            int nMinVal = Integer.MAX_VALUE;
            for (int j=0; j<this.nRgbVecs.length; j++) {
                int nVal = (nR - this.nRgbVals[j][R]) * this.nRgbVecs[j][R] +
                        (nG - this.nRgbVals[j][G]) * this.nRgbVecs[j][G] +
                        (nB - this.nRgbVals[j][B]) * this.nRgbVecs[j][B];
                nMinVal = Math.min(nMinVal, Math.abs(nVal));
            }
            if (nMinVal < this.nThreshold) {
                grayData[i] = Byte.MAX_VALUE;
            } else {
                grayData[i] = Byte.MIN_VALUE;
            }
        }
        super.setOutput(grayOutput);
    }

    /**
     * Implements toString
     * @return a string including all the input color values
     * and the input threshold value.
     */
    public String toString() {
        String szParams = "{";
        for (int i=0; i<this.nRgbVecs.length; i++) {
            szParams += "[R=" + Integer.toString(this.nRgbVecs[i][R]) + "," +
                    "G=" + Integer.toString(this.nRgbVecs[i][G]) + "," +
                    "B=" + Integer.toString(this.nRgbVecs[i][B]) + "],";
        }
        szParams += "Threshold=" + Integer.toString(this.nThreshold) + ")";
        return super.toString() + szParams;
    }
}
