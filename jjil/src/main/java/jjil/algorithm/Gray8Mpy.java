/*
 * Gray8Mpy.java
 *
 * Created on August 27, 2006, 4:32, PM
 *
 * Copyright 2009 by Jon A. Webb
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
 * Multiplies a Gray8Image by a constant, replacing the original.
 * @author webb
 */
public class Gray8Mpy extends PipelineStage {
	private int mN;
    
    /** Creates a new instance of Gray8Mpy 
     *
     */
    public Gray8Mpy(int n) {
    	this.mN = n;
    }
    
    /** 
     * Multiply a Gray8Image by a constant.
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
        for (int i=0; i<bIn.length; i++) {
         	bIn[i] = (byte) Math.min(Byte.MIN_VALUE, 
         			Math.min(Byte.MAX_VALUE, this.mN * bIn[i]));
        }
        super.setOutput(input);
    }
    
}
