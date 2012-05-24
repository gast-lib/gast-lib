/*
 * CannyHoriz.java
 *
 * Created on August 27, 2006, 4:32, PM
 *
 * To change this templatef, choose Tools | Template Manager
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
 * Computes a simple horizontal edge measure. The measure is simply the difference
 * between the current pixel and the one to the left, clamped between Byte.MIN_VALUE
 * and Byte.MAX_VALUE. The output replaces the input.
 * @author webb
 */
public class Gray8HorizSimpleEdge extends PipelineStage {
    
    /** Creates a new instance of Gray8HorizSimpleEdge 
     */
    public Gray8HorizSimpleEdge() {
    }
    
    /** Compute a simple horizontal edge measure. The measure is simply the difference
     * between the current pixel and the one to the left, clamped between Byte.MIN_VALUE
     * and Byte.MAX_VALUE. The output replaces the input.
     *
     * @param image the input Gray8Image
     * @throws jjil.core.Error if image is not a Gray8Image
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray8Image)) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                			image.toString(),
                			null,
                			null);
        }
        Gray8Image input = (Gray8Image) image;
        byte[] bIn = input.getData();
        int cWidth = input.getWidth();
        for (int i=0; i<input.getHeight(); i++) {
        	int nPrev;
        	int nThis = bIn[i*cWidth];
            for (int j=0; j<cWidth; j++) {
            	nPrev = nThis;
            	nThis = bIn[i*cWidth + j];
            	int nVal = Math.max(Byte.MIN_VALUE, 
            			Math.min(Byte.MAX_VALUE, nPrev - nThis));
            	bIn[i*cWidth + j] = (byte) nVal;
            }
        }
        super.setOutput(input);
    }
    
}
