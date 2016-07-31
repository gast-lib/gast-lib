/*
 * Gray16LinComb.java
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
import jjil.core.Gray16Image;
import jjil.core.Image;
import jjil.core.Ladder;

/**
 * Forms the linear combination of two Gray16Images. Designed to be used as a
 * combining stage in a Ladder. Calculates (nA * left image + nB * right image) / nC
 * @author webb
 */
public class Gray16LinComb implements Ladder.Join {
	private int nA, nB, nC;
    
    /**
     * Creates a new instance of Gray16LinComb which will form the linear combination of 
     * two images by calculating (nA * first image + nB * second image) / nC.
     * @param nA the multiplier for the first image.
     * @param nB the multiplier for the second image.
     * @param nC the divisor.
     */
    public Gray16LinComb(int nA, int nB, int nC) {
    	this.nA = nA;
    	this.nB = nB;
    	this.nC = nC;
    }
    
    /**
     * Form the linear combination of two Gray16Images. Output replaces the first
     * input.
     * @param imageFirst the left-hand image image (and output)
     * @param imageSecond the right-hand image image
     * @throws jjil.core.Error if either image is not a gray 16-bit
     * image.
     * @return the resulting Gray16Image.
     */
    public Image doJoin(Image imageFirst, Image imageSecond)
        throws jjil.core.Error
    {
        if (!(imageFirst instanceof Gray16Image)) {
            throw new jjil.core.Error(
            		jjil.core.Error.PACKAGE.ALGORITHM,
            		jjil.algorithm.ErrorCodes.IMAGE_NOT_GRAY16IMAGE,
            		imageFirst.toString(),
            		null,
            		null);
        }
        if (!(imageSecond instanceof Gray16Image)) {
            throw new jjil.core.Error(
            		jjil.core.Error.PACKAGE.ALGORITHM,
            		jjil.algorithm.ErrorCodes.IMAGE_NOT_GRAY16IMAGE,
            		imageSecond.toString(),
            		null,
            		null);
        }
        if (imageFirst.getWidth() != imageSecond.getWidth() ||
        	imageFirst.getHeight() != imageSecond.getHeight()) {
        	throw new jjil.core.Error(
            		jjil.core.Error.PACKAGE.ALGORITHM,
            		jjil.algorithm.ErrorCodes.IMAGE_SIZES_DIFFER,
        			imageFirst.toString(), 
        			imageSecond.toString(),
        			null);
        }
        short[] dataFirst = ((Gray16Image) imageFirst).getData();
        short[] dataSecond = ((Gray16Image) imageSecond).getData();
        for (int i=0; i<dataFirst.length; i++) {
        	dataFirst[i] = (short) ((this.nA * dataFirst[i] + this.nB * dataSecond[i]) / this.nC);
        }
        return imageFirst;
    }

}
