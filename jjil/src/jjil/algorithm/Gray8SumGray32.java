/*
 * Gray8SumGray32.java
 *   "Sum Gray8[Sub]Image to Gray32[Sub]Image".
 *   Forms integral image by summing pixels in a
 *   Gray8[Sub]Image to form a Gray32[Sub]Image.
 * The computation is O(i,j) = Sum for k<=i,l<=j of I(k,l)
 * Note output type is 32 bit because otherwise we'd get
 * truncation. With 32-bit output we can go up to 
 * 65,536 = 256x256 pixels in the input image.
 *
 * Created on July 1, 2007, 3:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jjil.algorithm;
import jjil.core.Error;
import jjil.core.Gray32Image;
import jjil.core.Gray32OffsetImage;
import jjil.core.Gray8Image;
import jjil.core.Gray8OffsetImage;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Forms integral image by summing pixels in a
 * Gray8[Sub]Image to form a Gray32[Sub]Image.<br>
 * The computation is O(i,j) = &sum;<sub>k &le; i, l &le; j</sub> I(k,l)<br>
 * Note output type is 32 bit because otherwise we'd get
 * truncation. With 32-bit output we can go up to 
 * 2<sup>12</sup>&times; 2<sup>12</sup> = 4096 &times; 4096 pixels in the input image.
 *
 * @author webb
 */
public class Gray8SumGray32 extends PipelineStage {
    
    /**
     * Creates a new instance of Gray8SumGray32
     */
    public Gray8SumGray32() {
    }
    
    /**
     * Form the cumulative sum<p>
     * Output(i,j) = &sum;<sub>k &le; i, l &le; j</sub> Input(k,l)
     * @param image input image.
     * @throws jjil.core.Error if the input is not a Gray8Image.
     */
    public void push(Image image) throws jjil.core.Error
    {
        if (!(image instanceof Gray8Image)) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                			image.toString(),
                			null,
                			null);
        }
        Gray32Image imageResult;
        if (image instanceof Gray8OffsetImage) {
            Gray8OffsetImage sub = (Gray8OffsetImage) image;
            imageResult = 
                    new Gray32OffsetImage(sub.getWidth(),
                        sub.getHeight(),
                        sub.getXOffset(),
                        sub.getYOffset());
        } else {
            // must be a Gray8Image
            imageResult = 
                    new Gray32Image(image.getWidth(), image.getHeight());
        }
        byte[] inData = ((Gray8Image) image).getData();
        // pointer to output data area, whether Gray32Image or Gray32OffsetImage
        int[] outData = imageResult.getData();
        // initialize first row
        int prevPixel = 0;
        for (int i=0; i<image.getWidth(); i++) {
            prevPixel += inData[i];
            outData[i] = prevPixel;
        }
        // initialize first column
        prevPixel = 0;
        for (int i=0; i<image.getHeight(); i++) {
            prevPixel += inData[i*image.getWidth()];
            outData[i*image.getWidth()] = prevPixel;
        }
        // fill in the rest of the array
        int nPrevRow = 0;
        for (int i=1; i<image.getHeight(); i++) {
            int nCurrRow = nPrevRow + image.getWidth();
            prevPixel = inData[nCurrRow];
            for (int j=1; j<image.getWidth(); j++) {
                outData[nCurrRow + j] = 
                    inData[nCurrRow + j] +
                    prevPixel +
                    outData[nPrevRow + j];
                prevPixel += inData[nCurrRow + j];
            }
            nPrevRow = nCurrRow;
        }
        super.setOutput(imageResult);
    }
    
}
