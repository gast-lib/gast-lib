/*
 * Gray8VertVar.java
 *
 * Created on February 3, 2008, 4:32, PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2008 by Jon A. Webb
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

/**
 * Computes the variance of pixels vertically distributed around
 * the current pixel.
 * @author webb
 */
public class Gray8VertVar extends PipelineStage {
	/**
	 * The window size -- pixels within nWindow of the current
	 * pixel are included in the window.
	 */
	int nWindow;
	/**
	 * The output image.
	 */
	Gray16Image g16 = null;
    
    /**
     * Creates a new instance of Gray8VertVar
     * @param nWindow height of window to calculate variance over.
     */
    public Gray8VertVar(int nWindow) {
    	this.nWindow = nWindow;
    }
    
    /** Compute the vertical variance of pixels within nWindow
     * of the current pixel.
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
        if (this.g16 == null || 
        	this.g16.getWidth() != image.getWidth() ||
        	this.g16.getHeight() != image.getHeight()) {
        	this.g16 = new Gray16Image(
        			image.getWidth(), 
        			image.getHeight());
        }
        Gray8Image input = (Gray8Image) image;
        byte[] bIn = input.getData();
        int cHeight = input.getHeight();
        int cWidth = input.getWidth();
        short[] sOut = g16.getData();
        for (int i=0; i<cWidth; i++) {
	        int nSum = 0;
	        int nSumSq = 0;
	        int nCount = 0;
	        for (int j=0; j<nWindow; j++) {
	        	nSum += bIn[j*cWidth + i];
	        	nSumSq += bIn[j*cWidth + i] * bIn[j*cWidth + i];
	        	nCount ++;
	        }
            for (int j=0; j<cHeight; j++) {
            	if (j + this.nWindow < cHeight) {
            		nSum += bIn[(j + this.nWindow)*cWidth + i];
            		nSumSq += bIn[(j + this.nWindow)*cWidth + i] *
            			bIn[(j + this.nWindow)*cWidth + i];
            		nCount ++;
            	}
            	if (j >= this.nWindow) {
            		nSum -= bIn[(j - this.nWindow)*cWidth + i];
            		nSumSq -= bIn[(j - this.nWindow)*cWidth + i] *
            			bIn[(j - this.nWindow)*cWidth + i];
            		nCount --;
            	}
            	short nVar = (short)
            		Math.min(Short.MAX_VALUE, 
            			(nSumSq - nSum * nSum / nCount) / (nCount - 1)); 
            	sOut[j*cWidth + i] = nVar;
            }
        }
        super.setOutput(g16);
    }
    
}
