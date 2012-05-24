/*
 * RgbShrink.java.
 *    Reduces a color image to a new size by averaging the pixels nearest each
 * target pixel's pre-image. This is done by converting each band of the image
 * into a gray image, shrinking them individually, then recombining them into
 * an RgbImage
 *
 * Created on October 13, 2007, 2:21 PM
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
 * Shrinks a color (RgbImage) to a given size. Each band is shrunk independently.
 * The pixels that each pixel maps to are averaged. There is no between-target-pixel
 * smoothing. The output image must be smaller than or equal to the size of the 
 * input.
 * @author webb
 */
public class RgbShrink extends PipelineStage {
    private int cHeight;
    private int cWidth;
    private Sequence seqR, seqG, seqB;
    /** Creates a new instance of RgbShrink. 
     *
     * @param cWidth new image width
     * @param cHeight new image height
     * @throws jjil.core.Error if either is less than or equal to zero.
     */
    public RgbShrink(int cWidth, int cHeight) 
        throws jjil.core.Error {
        setWidth(cWidth);
        setHeight(cHeight);
        setupPipeline();
    }
         
    /** Gets current target height 
     *
     * @return current height
     */
    public int getHeight() {
        return this.cHeight;
    }
    
    /** Gets current target width
     *
     * @return current width
     */
    public int getWidth() {
        return this.cWidth;
    }
    
    /**
     * Process an image.
     * @param image the input RgbImage.
     * @throws jjil.core.Error if the input is not an RgbImage, or is smaller than the target image either
     * horizontally or vertically.
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
        if (image.getWidth() < this.cWidth || image.getHeight() < this.cHeight) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.SHRINK_OUTPUT_LARGER_THAN_INPUT,
                			image.toString(),
                			this.toString(),
                			null);
        }
        /* shrink R band */
        this.seqR.push(image);
        /* shrink G band */
        this.seqG.push(image);
        /* shrink B band */
        this.seqB.push(image);
        /* recombine bands */
        Gray3Bands2Rgb g3rgb = new Gray3Bands2Rgb();
        super.setOutput(g3rgb.push(
                (Gray8Image)this.seqR.getFront(), 
                (Gray8Image)this.seqG.getFront(), 
                (Gray8Image)this.seqB.getFront()));
    }
        
    /** Changes target height
     * 
     * @param cHeight the new target height.
     * @throws jjil.core.Error if height is not positive
     */
    private void setHeight(int cHeight) throws jjil.core.Error {
        if (cHeight <= 0) {
            throw new Error(
        			Error.PACKAGE.ALGORITHM,
        			ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
        			new Integer(cHeight).toString(),
        			null,
        			null);
        }
        this.cHeight = cHeight;
    }
    
    private void setupPipeline() throws jjil.core.Error
    {
        RgbSelectGray sel = new RgbSelectGray(RgbSelectGray.RED);
        this.seqR = new Sequence(sel);
        Gray8Shrink gs = new Gray8Shrink(cWidth, cHeight);
        this.seqR.add(gs);
        sel = new RgbSelectGray(RgbSelectGray.GREEN);
        this.seqG = new Sequence(sel);
        gs = new Gray8Shrink(cWidth, cHeight);
        this.seqG.add(gs);
        sel = new RgbSelectGray(RgbSelectGray.BLUE);
        this.seqB = new Sequence(sel);
        gs = new Gray8Shrink(cWidth, cHeight);
        this.seqB.add(gs);
    }
    
    /** Changes target width
     * 
     * @param cWidth the new target width.
     * @throws jjil.core.Error if height is not positive
     */
    private void setWidth(int cWidth) throws jjil.core.Error {
        if (cWidth <= 0) {
            throw new Error(
                			Error.PACKAGE.ALGORITHM,
                			ErrorCodes.OUTPUT_IMAGE_SIZE_NEGATIVE,
                			new Integer(cWidth).toString(),
                			null,
                			null);
        }
        this.cWidth = cWidth;
    }
    
   
        
    /** Return a string describing the shrinking operation.
     *
     * @return the string describing the shrinking operation.
     */
    public String toString() {
        return super.toString() + " (" + this.cWidth + "," + this.cHeight + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
