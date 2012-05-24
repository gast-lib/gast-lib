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
 * Compute the Gray8Image that is the maximum absolute difference in red, green,
 * or blue between the RgbImage specified in the constructor and the RgbImage
 * supplied to the pipeline stage. This is intended to be used as part of a 
 * pipeline for separating out a fixed background from the new input. Since
 * the background can vary due to lighting conditions we find the maximum 
 * difference in any color channel and output that, making it easier to find
 * objects that may match the background in one or two channels.
 * @author webb
 */
public class RgbMaxDiff extends PipelineStage {
    private RgbImage rgbBack;

    /**
     * Set background image.
     * @param rgbBack background RgbImage.
     */
    public RgbMaxDiff(RgbImage rgbBack) {
        this.rgbBack = rgbBack;
    }
    
    /**
     * Process a foreground RgbImage and produce a Gray8Image in which each
     * pixel is the maximum of the differences between the input image and
     * the background image in the three color channels.
     * @param imInput input RgbImage
     * @throws jjil.core.Error if imInput is not an RgbImage or is not the same
     * size as the background image set in the constructor.
     */
    public void push(Image imInput) throws Error {
        {
        if (!(imInput instanceof RgbImage)) 
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_RGBIMAGE,
                			imInput.toString(),
                			null,
                			null);
        }
        if (imInput.getWidth() != this.rgbBack.getWidth() ||
        	imInput.getHeight() != this.rgbBack.getHeight()) {
        	throw new Error(
        				Error.PACKAGE.ALGORITHM,
        				ErrorCodes.IMAGE_SIZES_DIFFER,
        				imInput.toString(),
        				this.rgbBack.toString(),
        				null);
        
        }

        int wInput[] = ((RgbImage)imInput).getData();
        int wBack[] = this.rgbBack.getData();
        Gray8Image grayOut = new Gray8Image(
                this.rgbBack.getWidth(), 
                this.rgbBack.getHeight());        
        byte bGray[] = grayOut.getData();
        for (int i=0; i<imInput.getWidth() * imInput.getHeight(); i++) {
            int rIn = RgbVal.getR(wInput[i]);
            int gIn = RgbVal.getG(wInput[i]);
            int bIn = RgbVal.getB(wInput[i]);
            int rBack = RgbVal.getR(wBack[i]);
            int gBack = RgbVal.getG(wBack[i]);
            int bBack = RgbVal.getB(wBack[i]);
            int gRes = Math.max(Math.abs(rIn-rBack), 
                    Math.max(Math.abs(gIn-gBack),
                        Math.abs(bIn-bBack)));
            bGray[i] = (byte) Math.min(gRes, Byte.MAX_VALUE);
        }
        super.setOutput(grayOut);
    }

    /**
     * Implement toString, providing the background image information.
     * @return a string consisting of this class name followed by the
     * background image description.
     */
    public String toString() {
        return super.toString() + "(" + this.rgbBack.toString() + ")";
    }
}
