/*
 * Gray3Bands2Rgb.java
 *   Takes 3 gray images and combines them to make an RGB image.
 *   This is not a pipeline stage.
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
import jjil.core.Gray8Image;
import jjil.core.RgbImage;
/** Gray3Bands2Rgb converts three 8-bit gray images to RGB by copying the 
 * three input gray values into R, G, and B. The signed byte values in the gray
 * image are changed into unsigned byte values in the ARGB word.
 *
 * @author webb
 */
public class Gray3Bands2Rgb {
    
    /**
     * Converts 3 8-bit gray images into an RGB image by combining the
     * R, G, and B values. Also changes the data range of the bytes
     * from -128->127 to 0->255 since the bit shift in Java would treat them
     * as signed values otherwise.
     * @param imRed the input red image.
     * @param imGreen the input green image.
     * @param imBlue the input blue image.
     * @return the color image
     * @throws jjil.core.Error if the input sizes do not match
     */
    static public RgbImage push(
            Gray8Image imRed, 
            Gray8Image imGreen, 
            Gray8Image imBlue) throws jjil.core.Error {
        if (imRed.getWidth() != imGreen.getWidth() ||
            imGreen.getWidth() != imBlue.getWidth() ||
            imRed.getHeight() != imGreen.getHeight() ||
            imGreen.getHeight() != imBlue.getHeight()) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.IMAGE_SIZES_DIFFER,
                			imRed.toString(),
                			imGreen.toString(),
                			imBlue.toString());
        }
        RgbImage rgb = new RgbImage(imRed.getWidth(), imRed.getHeight());
        byte[] redData = imRed.getData();
        byte[] greenData = imGreen.getData();
        byte[] blueData = imBlue.getData();
        int[] rgbData = rgb.getData();
        for (int i=0; i<imRed.getWidth() * imRed.getHeight(); i++) {
            /* Convert from signed byte value to unsigned byte for storage
             * in the RGB image.
             */
            int redUnsigned = (redData[i]) - Byte.MIN_VALUE;
            int greenUnsigned = (greenData[i]) - Byte.MIN_VALUE;
            int blueUnsigned = (blueData[i]) - Byte.MIN_VALUE;
            /* Create ARGB word */
            rgbData[i] = 
                    (0xFF000000) |              // mask is off
                    ((redUnsigned)<<16) | 
                    ((greenUnsigned)<<8) | 
                    blueUnsigned;
        }
        return rgb;
    }
}
