/*
 * PipelineStage.java
 *
 * Created on August 27, 2006, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2006 by Jon A. Webb
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

package jjil.core;

/**
 * PipelineStage is the class from which all image to image
 * processing operations must derive. It holds the output image
 * (in imageOutput) and notes whether there is an image available
 * or not (in fReady). It is intended to be used as a single-level
 * stack element.
 *
 *
 * @author webb
 */
public abstract class PipelineStage 
{
    /** An image is available iff fReady is true 
     */
    protected boolean fReady = false;
    /** The output image from this stage.
     */
    protected Image imageOutput = null;
   
    /** Class constructor
     */
    protected PipelineStage()
    {
    }
    
    /** Returns true iff this pipeline stage does not have an
     * output available.
     *
     * @return true iff the pipeline stage does not have an image available.
     */
    public boolean isEmpty()
    {
        return !this.fReady;
    }
    
    /**
     * Returns the current output, and pops it off the stack.
     * @return the current output
     * @throws jjil.core.Error if no output is available
     */
    public Image getFront() throws jjil.core.Error
    {
        if (!this.fReady) {
            throw new Error(
                            Error.PACKAGE.CORE,
                            ErrorCodes.NO_RESULT_AVAILABLE,
                            this.toString(),
                            null,
                            null);
        }
        Image imageResult = this.imageOutput;
        this.imageOutput = null;
        this.fReady = false;
        return imageResult;
    }
       
    /**
     * Actual processing is done in the derived class here.
     * @param imageInput the input image
     * @throws jjil.core.Error typically, when the image is not of the expected type.
     */
    public abstract void push(Image imageInput) throws jjil.core.Error;
    
    /** Derived classes use setOutput to pass their result back
     * here.
     *
     * @param imageResult the output image
     */
    protected void setOutput(Image imageResult)
    {
        this.imageOutput = imageResult;
        this.fReady = true;
    }
}
