/*
 * Gray8HorizVertContrast.java
 *
 * Created on August 27, 2006, 4:32, PM
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
import jjil.core.Image;
import jjil.core.Ladder;
import jjil.core.PipelineStage;

/**
 * Computes a simple measure of horizontal-vertical contrast.
 * The measure is the difference in variance
 * over a window of a given size measured horizontally and vertically.
 * The measure used is a linear combination of the horizontal and vertical
 * contrast, clamped between Byte.MIN_VALUE and Byte.MAX_VALUE. 
 * @author webb
 */
public class Gray8HorizVertContrast extends PipelineStage {
	PipelineStage pipe;
    
    /** Creates a new instance of Gray8HorizVertContrast. Builds the pipeline
     * which computes the horizontal-vertical contrast measure. Result is
     * (nA * horizontal + nB * vertical) / nC.
     * @param nWindow the size of the window to compute contrast over.
     * @param nA factor to multiply horizontal contrast by
     * @param nB factor to multiply vertical contrast by
     * @param nC divisor
     */
    public Gray8HorizVertContrast(int nWindow, int nA, int nB, int nC) {
    	Gray8HorizVar ghv = new Gray8HorizVar(nWindow);
    	Gray8VertVar gvv = new Gray8VertVar(nWindow);
    	Gray16LinComb glc = new Gray16LinComb(nA, nB, nC);
    	this.pipe = new Ladder(ghv, gvv, glc);
    }
    
    /** Apply the contrast measure to the input input image.
     * @param image the input Gray16Image
     * @throws jjil.core.Error if image is not a Gray16Image
     */
    public void push(Image image) throws jjil.core.Error {
    	this.pipe.push(image);
        super.setOutput(this.pipe.getFront());
    }
    
}
