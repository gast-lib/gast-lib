/*
 * Gray16Gray8.java
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
import jjil.core.Gray16Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
/** Gray16Gray8 converts an 16-bit gray image to an 8-bit
 *  gray image. The most significant 8 bits of each pixel are dropped.
 *
 * @author webb
 */
public class Gray16Gray8 extends PipelineStage {
    
    /** Creates a new instance of Gray16Gray8 */
    public Gray16Gray8() {
    }
    
    /** Converts an 16-bit gray image into an 8-bit image by and'ing off
     * the top 8 bits of every pixel.
     *
     * @param image the input image.
     * @throws jjil.core.Error if the input is not a Gray8Image
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray16Image)) {
            throw new Error(
    				Error.PACKAGE.ALGORITHM,
    				ErrorCodes.IMAGE_NOT_GRAY16IMAGE,
    				image.toString(),
    				null,
    				null);
        }
        Gray16Image gray = (Gray16Image) image;
        Gray8Image gray8 = new Gray8Image(image.getWidth(), image.getHeight());
        short[] grayData = gray.getData();
        byte[] gray8Data = gray8.getData();
        for (int i=0; i<gray.getWidth() * gray.getHeight(); i++) {
            /* Convert from 16-bit value to 8-bit value, discarding
             * most significant bits.
             */
            gray8Data[i] = (byte) (grayData[i] & 0xff);
        }
        super.setOutput(gray8);
    }
}
