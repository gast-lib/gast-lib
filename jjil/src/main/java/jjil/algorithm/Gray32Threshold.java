/*
 * Gray32Threshold.java
 *
 * Created on September 9, 2006, 10:25 AM
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
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Threshold. Output is a Gray8Image with values greater than or equal to threshold
 * set to Byte.MAX_VALUE, below threshold set to Byte.MIN_VALUE. Input
 * is a Gray32Image.
 * @author webb
 */
public class Gray32Threshold extends PipelineStage {
	int nThreshold;
	Gray8Image imageOutput = null;
    
    /**
     * Creates a new instance of Gray32Threshold
     * @param nThreshold the threshold.
     */
    public Gray32Threshold(int nThreshold) {
    	this.nThreshold = nThreshold;
    }
    
    /** Threshold gray image. Output is Byte.MAX_VALUE over or equal to threshold,
     * Byte.MIN_VALUE under.
     *
     * @param image the input Gray32Image
     * @throws jjil.core.Error if the image is not a Gray32Image.
     */
    public void push(Image image)
        throws jjil.core.Error
    {
        if (!(image instanceof Gray32Image)) {
            throw new jjil.core.Error(
            		jjil.core.Error.PACKAGE.ALGORITHM,
            		jjil.algorithm.ErrorCodes.IMAGE_NOT_GRAY32IMAGE,
                    image.toString(),
                    null,
                    null);
        }
        if (this.imageOutput == null ||
        	this.imageOutput.getWidth() != image.getWidth() ||
        	this.imageOutput.getHeight() != image.getHeight()) {
        	this.imageOutput = new Gray8Image(
        			image.getWidth(),
        			image.getHeight());
        }
        Gray32Image gray = (Gray32Image) image;
        int[] data = gray.getData();
        byte[] dataOut = this.imageOutput.getData();
        for (int i=0; i<data.length; i++) {
            dataOut[i] = (data[i] >= this.nThreshold) ?
            		Byte.MAX_VALUE : Byte.MIN_VALUE;
        }
        super.setOutput(this.imageOutput);
    }
    
    /**
     * Implement toString
     * @return a String describing the class instance.
     */
    public String toString() {
    	return super.toString() + "(" + this.nThreshold + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
