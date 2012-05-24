/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Find local 3x3 peaks in the Gray8Image. A pixel is set to Byte.MIN_VALUE
 * if it is not equal to the local maximum.
 * @author webb
 */
public class Gray8Peak3x3 extends PipelineStage {
    /**
     * Scan the image and set all pixels not equal to the local 3x3 maximum
     * to Byte.MIN_VALUE.
     * @param imageInput input Gray8Image. Not modified.
     * @throws jjil.core.Error if input is not a Gray8Image.
     */
    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof Gray8Image)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                            imageInput.toString(),
                            null,
                            null);
        }
        Gray8Image grayInput = (Gray8Image) imageInput;
        byte[] bData = grayInput.getData();
        Gray8Image grayOutput = new Gray8Image(imageInput.getWidth(),imageInput.getHeight());
        byte[] bDataOut = grayOutput.getData();
        for (int i=1; i<grayInput.getHeight()-1; i++) {
            for (int j=1; j<grayInput.getWidth()-1; j++) {
                if (bData[i*grayInput.getWidth()+j] != 
                        Math.max(bData[(i-1)*grayInput.getWidth()+j-1],
                        Math.max(bData[(i-1)*grayInput.getWidth()+j], 
                        Math.max(bData[(i-1)*grayInput.getWidth()+j+1], 
                        Math.max(bData[i*grayInput.getWidth()+j-1], 
                        Math.max(bData[i*grayInput.getWidth()+j+1], 
                        Math.max(bData[(i+1)*grayInput.getWidth()+j-1], 
                        Math.max(bData[(i+1)*grayInput.getWidth()+j], 
                        bData[(i+1)*grayInput.getWidth()+j+1])))))))) 
                {
                            bDataOut[i*grayInput.getWidth()+j] =
                                    Byte.MIN_VALUE;
                } else {
                    bDataOut[i*grayInput.getWidth()+j] =
                            bData[i*grayInput.getWidth()+j];

                }

            }
        }
        super.setOutput(grayOutput);
    }

}
