/*
 * Gray8Gray32.java
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
/** Gray8Gray32 converts an 8-bit gray image to a 32-bit
 *  gray image.
 *
 * @author webb
 */
public class Gray8Gray32 extends PipelineStage {
    
    /** Creates a new instance of Gray8Gray32 */
    public Gray8Gray32() {
    }
    
    /** Converts an 8-bit gray image into a 32-bit image by replicating
     * changing the data range of the bytes from -128->127 to 0->255.
     *
     * @param image the input image.
     * @throws jjil.core.Error if the input is not a Gray8Image
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
        Gray8Image gray = (Gray8Image) image;
        Gray32Image gray32 = new Gray32Image(image.getWidth(), image.getHeight());
        byte[] grayData = gray.getData();
        int[] gray32Data = gray32.getData();
        for (int i=0; i<gray.getWidth() * gray.getHeight(); i++) {
            /* Convert from signed byte value to unsigned byte for storage
             * in the 32-bit image.
             */
            int grayUnsigned = (grayData[i]) - Byte.MIN_VALUE;
            /* Assign 32-bit output */
            gray32Data[i] = grayUnsigned;
        }
        super.setOutput(gray32);
    }
}
