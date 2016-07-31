package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Compensates for uneven background illumination in an input image,
 * at the same time changing an unsigned byte image to a signed
 * byte image, which is the type used through JJIL. Unsigned byte
 * images are supplied by, e.g., the Google G1 phone when operating
 * in preview mode. 
 * @author webb
 *
 */
public class Gray8UnsignedBackgroundSubtract extends PipelineStage {
	Gray32Image mg32 = null;
	int mnHeight;
	int mnWidth;
	
	/**
	 * Set the width and height of the window used for averaging when computing
	 * the background illumination.
	 * @param nWidth width to average over
	 */
	public Gray8UnsignedBackgroundSubtract(int nWidth, int nHeight) {
		this.mnWidth = nWidth;
		this.mnHeight = nHeight;
	}

	/**
	 * Compute an output Gray8Image which is the difference of the input
	 * unsigned Gray8Image and an average of a window of width x height
	 * size of the input. This is done using a cumulative sum operation
	 * so the operation is done efficiently.
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
        if (this.mnWidth > imageInput.getWidth() || 
        		this.mnHeight > imageInput.getHeight()) {
            throw new Error(
    				Error.PACKAGE.ALGORITHM,
    				ErrorCodes.PARAMETER_OUT_OF_RANGE,
    				imageInput.toString(),
    				Integer.toString(this.mnWidth),
    				Integer.toString(this.mnHeight));
        }
        // if the image size is changed or this is the first time
        // allocate the intermediate Gray32Image.
        if (this.mg32 == null || 
        		!this.mg32.getSize().equals(imageInput.getSize())) {
        	this.mg32 = new Gray32Image(
        			imageInput.getWidth(), imageInput.getHeight());
        }
        Gray8Image gray = (Gray8Image) imageInput;
        byte[] grayData = gray.getData();
        int[] gray32Data = this.mg32.getData();
        // First row
        int nSum = 0;
        for (int j=0; j<gray.getWidth(); j++) {
            /* Store unsigned byte value into Gray32Image
             */
            int grayUnsigned = 0xff & grayData[j];
            /* Assign 32-bit output */
            nSum += grayUnsigned;
            gray32Data[j] = nSum;
        }
        // Other rows
        for (int i=1; i<gray.getHeight(); i++) {
            nSum = 0;
            for (int j=0; j<gray.getWidth(); j++) {
                /* Get unsigned byte value as an int
                 */
                int grayUnsigned = grayData[i*gray.getWidth()+j] & 0xff;
                nSum += grayUnsigned;
                gray32Data[i*gray.getWidth()+j] = 
                        gray32Data[(i-1)*gray.getWidth()+j] +
                        nSum;
            }
        }
        // now compute the average value at each pixel and subtract it
        // from the input, replacing the original image
        for (int i=0; i<gray.getHeight(); i++) {
        	/* nTop and nBottom are the top and bottom rows of the averaging
        	 * window, taking into account edge effects
        	 */
        	int nTop, nBottom;
        	if (i<this.mnHeight/2) {
        		nTop = 0;
        		nBottom = this.mnHeight;
        	} else if (i>=gray.getHeight()-this.mnHeight/2) {
        		nBottom = gray.getHeight()-1;
        		nTop = nBottom - this.mnHeight;
        	} else {
	        	nTop = i-this.mnHeight/2;
	        	nBottom = i+this.mnHeight/2;        		
        	}
	        for (int j=0; j<this.mnWidth/2; j++) {
        		/* nLeft and nRigth are the left and right edges of the 
        		 * averaging window, taking into account edge effects
        		 */
        		int nLeft = 0;
        		int nRight = this.mnWidth;
        		// compute the sum of the averaging window using the cumulative
        		// sum array
        		nSum = gray32Data[nBottom*gray.getWidth()+nRight] -
        			gray32Data[nTop*gray.getWidth()+nLeft];
        		// compute the difference between this pixel and the averaged
        		// value
        		grayData[i*gray.getWidth()+j] =
        			(byte) ((grayData[i*gray.getWidth()+j] & 0xff) -
        					nSum / (this.mnWidth * this.mnHeight));
        	}
	        for (int j=this.mnWidth/2; j<gray.getWidth()-this.mnWidth/2; j++) {
        		/* nLeft and nRigth are the left and right edges of the 
        		 * averaging window, taking into account edge effects
        		 */
        		int nLeft = j-this.mnWidth/2;
        		int nRight = j + this.mnWidth/2;
        		// compute the sum of the averaging window using the cumulative
        		// sum array
        		nSum = gray32Data[nBottom*gray.getWidth()+nRight] -
        			gray32Data[nTop*gray.getWidth()+nLeft];
        		// compute the difference between this pixel and the averaged
        		// value
        		grayData[i*gray.getWidth()+j] =
        			(byte) ((grayData[i*gray.getWidth()+j] & 0xff) -
        					nSum / (this.mnWidth * this.mnHeight));
        	}
	        for (int j=gray.getWidth()-this.mnWidth/2; j<gray.getWidth(); j++) {
        		/* nLeft and nRigth are the left and right edges of the 
        		 * averaging window, taking into account edge effects
        		 */
        		int nRight = gray.getWidth()-1;
        		int nLeft = nRight-this.mnWidth;
        		// compute the sum of the averaging window using the cumulative
        		// sum array
        		nSum = gray32Data[nBottom*gray.getWidth()+nRight] -
        			gray32Data[nTop*gray.getWidth()+nLeft];
        		// compute the difference between this pixel and the averaged
        		// value
        		grayData[i*gray.getWidth()+j] =
        			(byte) ((grayData[i*gray.getWidth()+j] & 0xff) -
        					nSum / (this.mnWidth * this.mnHeight));
        	}
        }
        // output result
        super.setOutput(gray);
	}

}
