/*
 * Gray8Flip.java
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

/**
 * Pipeline stage rotates an image 90 degrees clockwise or counter-clockwise
 * <p>
 * 
 * @author webb
 */
public class Gray8Flip extends PipelineStage
{
    public enum Axis
    {
        HORIZONTAL, VERTICAL
    }

    private Axis mAxis;

    /**
     * Creates a new instance of Gray8Flip. The axis direction is specified
     * here.
     * 
     */
    public Gray8Flip(Axis axis)
    {
        mAxis = axis;
    }

    /**
     * Flips the image around a horizontal or vertical axis
     * 
     * @param image
     *            the input image.
     */
    public void push(Image image) throws jjil.core.Error
    {
        if (!(image instanceof Gray8Image))
        {
            throw new Error(Error.PACKAGE.ALGORITHM,
                    ErrorCodes.IMAGE_NOT_GRAY8IMAGE, image.toString(), null,
                    null);
        }
        Gray8Image imageInput = (Gray8Image) image;
        byte[] inputData = imageInput.getData();
        if (mAxis == Axis.HORIZONTAL)
        {
            byte[] bCopy = new byte[imageInput.getWidth()];
            int nWidth = imageInput.getWidth();
            for (int i = 0; i < imageInput.getHeight() / 2; i++)
            {
                // exchange next row from top with next row from bottom
                System.arraycopy(inputData, i * nWidth, bCopy, 0, nWidth);
                System.arraycopy(inputData, (imageInput.getHeight() - 1 - i)
                        * nWidth, imageInput, i * nWidth, nWidth);
                System.arraycopy(bCopy, 0, inputData,
                        (imageInput.getHeight() - 1 - i) * nWidth, nWidth);
            }
        } else
        {
            for (int i = 0; i < imageInput.getHeight(); i++)
            {
                for (int j = 0; j < imageInput.getWidth() / 2; j++)
                {
                    // exchange next pixel from left with next pixel from right
                    byte b = inputData[i * imageInput.getWidth() + j];
                    inputData[i * imageInput.getWidth() + j] = inputData[i
                            * imageInput.getWidth() + imageInput.getWidth() - 1
                            - j];
                    inputData[i * imageInput.getWidth() + imageInput.getWidth()
                            - 1 - j] = b;
                }
            }
        }
        super.setOutput(imageInput);
    }

    /**
     * Return a string describing the rotation operation.
     * 
     * @return the string describing the rotation operation.
     */
    public String toString()
    {
        return super.toString() + " (" + this.mAxis + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
