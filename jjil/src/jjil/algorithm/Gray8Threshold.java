/*
 * Gray8Threshold.java
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
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * Threshold. Output is a Gray8Image with values less than threshold
 * set to Byte.MAX_VALUE, below threshold set to Byte.MIN_VALUE if
 * bWithin is true, opposite if bWithin is false.
 * @author webb
 */
public class Gray8Threshold extends PipelineStage {
    boolean bWithin;
    int nThreshold;
    
    /**
     * Creates a new instance of Gray8Threshold
     * @param nThreshold threshold value.
     * @param bWithin direction of threshold. If true then
     * output is true (Byte.MAX_VALUE) iff < threshold.
     */
    public Gray8Threshold(int nThreshold, boolean bWithin) {
    	this.nThreshold = nThreshold;
        this.bWithin = bWithin;
    }
    
    /** Threshold gray image. Output is Byte.MAX_VALUE over threshold,
     * Byte.MIN_VALUE under.
     *
     * @param image the input image (and output)
     * @throws jjil.core.Error if the image is not a gray 8-bit
     * image.
     */
    public void push(Image image)
        throws jjil.core.Error
    {
        if (!(image instanceof Gray8Image)) {
            throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
            				image.toString(),
            				null,
            				null);
        }
        Gray8Image gray = (Gray8Image) image;
        byte[] data = gray.getData();
        for (int i=0; i<data.length; i++) {
            data[i] = (((data[i]) < this.nThreshold)==this.bWithin) ?
            		Byte.MAX_VALUE : Byte.MIN_VALUE;
        }
        super.setOutput(image);
    }
    
    /**
     * 
     * @return String describing class instance.
     */
    public String toString() {
    	return super.toString() + "(" + this.nThreshold + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
