/*
 * Gray8Hist.java
 *
 * Created on September 3, 2006, 2:20 PM
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
import jjil.core.Gray8Image;

/**
 * Computes the histogram of a gray image. 
 * 
 * @author webb
 */
public class Gray8Hist {
    
    /** Not intended for use -- getHistogram is static. */
    private Gray8Hist() {
    }
    
    /** Compute the histogram of the input gray image.
     *
     * @param image the input image
     * @return the histogram, a 256-element int array. The
     * array is offset so element 0 corresponds to signed byte value
     * Byte.MIN_VALUE.
     */
    public static int[] computeHistogram(Gray8Image image) {
        int[] result = new int[256];
        for (int i=0; i<256; i++) {
            result[i] = 0;
        }
        byte[] data = image.getData();
        for (int i=0; i<image.getHeight(); i++) {
            for (int j=0; j<image.getWidth(); j++) {
                result[data[i*image.getWidth()+j]-Byte.MIN_VALUE] ++;
            }
        }
        return result;
    }
    
}
