/*
 * Gray82Gray32.java
 *
 * Created on August 27, 2006, 9:02 AM
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
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
/** Gray32Scale2Gray8 converts an 32-bit gray image to a 8-bit
 *  gray image. Input values are scaled so that the max value in
 *  the image maps to Byte.MAX_VALUE and the minimum value to
 *  Byte.MIN_VALUE.
 *
 * @author webb
 */
public class Gray32Scale2Gray8 extends PipelineStage {
    
    /** Creates a new instance of Gray82Gray32 */
    public Gray32Scale2Gray8() {
    }
    
    /** Converts a 32-bit gray image into an 8-bit gray image. 
     *
     *
     * @param image the input image.
     * @throws jjil.core.Error if the input is not a Gray8Image
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray32Image)) {
            throw new Error(
    				Error.PACKAGE.ALGORITHM,
    				ErrorCodes.IMAGE_NOT_GRAY32IMAGE,
    				image.toString(),
    				null,
    				null);
        }
        Gray32Image gray32 = (Gray32Image) image;
        Gray8Image gray8 = new Gray8Image(image.getWidth(), image.getHeight());
        int[] gray32Data = gray32.getData();
        byte[] gray8Data = gray8.getData();
        int nMax = Integer.MIN_VALUE;
        int nMin = Integer.MAX_VALUE;
        for (int i=0; i<gray32.getWidth() * gray32.getHeight(); i++) {
            nMax = Math.max(nMax, gray32Data[i]);
            nMin = Math.min(nMin, gray32Data[i]);
        }
        int nDiff = nMax - nMin;
        if (nDiff == 0) {
            nDiff = 1;
            nMax = 0;
            nMin = 0;
        }
        for (int i=0; i<gray32.getWidth() * gray32.getHeight(); i++) {
            /* Convert from signed byte value to unsigned byte for storage
             * in the 32-bit image.
             */
             /* Assign 32-bit output */
            gray8Data[i] = (byte)
                    (Byte.MIN_VALUE + 
                    ((Byte.MAX_VALUE) - (Byte.MIN_VALUE) * 
                        (gray32Data[i] - nMin)) / nDiff);
        }
        super.setOutput(gray8);
    }
}
