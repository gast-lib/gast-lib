/*
 * RgbVertGaussSmooth.java
 *
 * Created on September 9, 2006, 3:17 PM
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
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.Sequence;

/**
 * This PipelineStage blurs an RgbImage using a Gaussian blur.
 * @author webb
 */
public class RgbVertGaussSmooth extends PipelineStage {
   private Sequence seqR, seqG, seqB;
   int nSigma;
    
    /** Smooth an image vertically using a Gaussian blur.
     * @param nSigma the sigma value of window to smooth over
     * @throws jjil.core.Error if sigma is out of range
     */
    public RgbVertGaussSmooth(int nSigma) throws jjil.core.Error {
        setWidth(nSigma);
    }
    
    
    /**
     * Smooth an RgbImage vertically using a Gaussian blur operator
     * @param image the input RgbImage image.
     * @throws jjil.core.Error if the input is not an RgbImage
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbImage)) {
            throw new Error(
    				Error.PACKAGE.ALGORITHM,
    				ErrorCodes.IMAGE_NOT_RGBIMAGE,
    				image.toString(),
    				null,
    				null);
        }
        this.seqR.push(image);
        this.seqG.push(image);
        this.seqB.push(image);
        super.setOutput(Gray3Bands2Rgb.push(
                (Gray8Image)this.seqR.getFront(), 
                (Gray8Image)this.seqG.getFront(), 
                (Gray8Image)this.seqB.getFront()));
    }
    
    /** Set sigma value of Gaussian blur
     * @param nSigma the sigma of the window to blur over
     * @throws jjil.core.Error if nSigma is out of range.
     */
    public void setWidth(int nSigma) throws jjil.core.Error {
        this.nSigma = nSigma;
        this.seqR = new Sequence(new RgbSelectGray(RgbSelectGray.RED));
        this.seqR.add(new Gray8GaussSmoothVert(nSigma));
        this.seqG = new Sequence(new RgbSelectGray(RgbSelectGray.GREEN));
        this.seqG.add(new Gray8GaussSmoothVert(nSigma));
        this.seqB = new Sequence(new RgbSelectGray(RgbSelectGray.BLUE));
        this.seqB.add(new Gray8GaussSmoothVert(nSigma));
    }
    
    /**
     * Returns a string describing the current instance. All the constructor
     * parameters are returned in the order specified in the constructor.
     * @return The string describing the current instance. The string is of the form 
     * "jjil.algorithm.RgbVertGaussSmoothxxx (startRow,endRow,leftColStart,
     * rightColStart,leftColEnd,rightColEnd)"
     */
    public String toString() {
        return super.toString() + " (" + this.nSigma + ")"; //$NON-NLS-1$
    }
}
