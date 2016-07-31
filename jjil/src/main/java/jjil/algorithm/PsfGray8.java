/*
 * PsfGray8.java
 *
 * Created on November 3, 2007, 2:58 PM
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
 * Provides point spread functions for use in inverse filtering.
 * @author webb
 */
public class PsfGray8 {
       
    /**
     * Computes a horizontal bar point spread function, with a given width.
     * This corresponds to a horizontal blur. The output image is scaled so the sum
     * of all values is 256.
     * @param nImageWidth The image width of the image to create. The image is always square so this is
     * also the height.
     * @param nBarHalfWidth The width of the bar, divided by 2 and rounded down. The actual width is
     * twice this width plus 1, centered on the center of the image.
     * @return A Gray8Image that can be passed to InverseFilter to remove horizontal blur.
     */
    public static Gray8Image horizBar(int nImageWidth, int nBarHalfWidth) {
        Gray8Image imResult = new Gray8Image(nImageWidth, nImageWidth, Byte.MIN_VALUE);
        byte bData[] = imResult.getData();
        int nC = nImageWidth / 2;
        int nArea = 2 * nBarHalfWidth + 1;
        int nRow = nC * nImageWidth;
        for (int j = -nBarHalfWidth; j <= nBarHalfWidth; j++) {
            bData[nRow + nC + j] = (byte) (Byte.MAX_VALUE / nArea);
        }
        return imResult;
    }
       
    /**
     * Computes a disk-shaped point spread function that can be used to do deblurring
     * for circular blur. The output image is scaled so the sum of the values is 256.
     * @param nImageWidth The output image width. The image is always square so this is also the 
     * height.
     * @param nRadius The radius of the disk.
     * @return A Gray8Image that can be passed to InverseFilter to remove circular blur,
     * for example that due to defocus.
     */
    public static Gray8Image disk(int nImageWidth, int nRadius) {
        Gray8Image imResult = new Gray8Image(nImageWidth, nImageWidth, Byte.MIN_VALUE);
        byte bData[] = imResult.getData();
        int nC = nImageWidth / 2;
        int nRadiusSq = nRadius * nRadius;
        int nArea = 0;
        // compute area of the disk. The rounding is tricky so I decided
        // to just count.
        for (int i = -nRadius; i <= nRadius; i++) {
            for (int j = -nRadius; j <= nRadius; j++) {
                if (i*i + j*j <= nRadiusSq) {
                    nArea ++;
                }
            }
        }
         for (int i = -nRadius; i <= nRadius; i++) {
            int nRow = (nC + i) * nImageWidth;
            for (int j = -nRadius; j <= nRadius; j++) {
                if (i*i + j*j <= nRadiusSq) {
                    bData[nRow + nC + j] = (byte) (Byte.MAX_VALUE / nArea);
                }
            }
        }
        return imResult;
    }
}
