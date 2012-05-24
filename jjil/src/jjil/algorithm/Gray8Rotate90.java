/*
 * Gray8Rotate90.java
 *
 * Created on November 17, 2011
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2011 by Jon A. Webb
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
import jjil.core.Rect;

/**
 * Pipeline stage rotates an image 90 degrees clockwise or
 * counter-clockwise
 * <p>
 * @author webb
 */
public class Gray8Rotate90 extends PipelineStage {
    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }
    
    private Direction mDirection;
    
    /** Creates a new instance of Gray8Rotate90. The rotation direction
     * is specified here.
     *
     */
    public Gray8Rotate90(
            Direction direction) {
    	mDirection = direction;
    }
       
    /** Rotates the image 90 degrees clockwise or counter-clockwise
     *
     * @param image the input image.
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
        Gray8Image imageInput = (Gray8Image) image;
        Gray8Image imageResult = new Gray8Image(image.getHeight(), image.getWidth());
        byte[] inputData = imageInput.getData();
        byte[] outputData = imageResult.getData();
        if (mDirection == Direction.CLOCKWISE) {
            for (int i=0; i<imageInput.getHeight(); i++) {
                for (int j=0; j<imageInput.getWidth(); j++) {
                    outputData[j*imageOutput.getWidth() + (imageInput.getHeight() - i - 1)] =
                            inputData[i * imageInput.getWidth() + j];
                }
            }
        } else {
            for (int i=0; i<imageInput.getHeight(); i++) {
                for (int j=0; j<imageInput.getWidth(); j++) {
                    outputData[(imageResult.getHeight() - j - 1)*imageResult.getWidth() + i] =
                            inputData[i * imageInput.getWidth() + j];
                }
            }
        }
        super.setOutput(imageResult);
    }
    
    /** Return a string describing the rotation operation.
     *
     * @return the string describing the rotation operation.
     */
    public String toString() {
        return super.toString() + " (" + this.mDirection + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
