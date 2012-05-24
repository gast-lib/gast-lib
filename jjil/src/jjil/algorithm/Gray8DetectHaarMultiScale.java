package jjil.algorithm;
/*
 * Gray8DetectHaarMultiScale.java
 *
 * Created on August 19, 2007, 7:33 PM
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Gray8MaskedImage;
import jjil.core.Gray8OffsetImage;
import jjil.core.Image;
import jjil.core.PipelineStage;

/**
 * DetectHaar applies a Haar cascade at multiple locations and multiple scales
 * to an input Gray8Image. The result is a mask with the masked (non-Byte.MIN_VALUE)
 * locations indicating the areas where the feature was detected.<br>
 * The Haar cascade is applied at multiple scales, starting with the coarsest scale,
 * and working down to the finest scale. At each scale, the cascade is applied to
 * subimages spread across the image. If the cascade detects a feature, the area of
 * the mask corresponding to that subimage is set to Byte.MAX_VALUE. When a subimage
 * is to be tested, the mask is first examined to see if the central pixel in the
 * mask area corresponding to that subimage is masked. If it is, the subimage is 
 * skipped. When transitioning to a finer scale, the mask is stretched to the new
 * size. This results in areas where features have been detected at a coarser scale
 * not being re-searched at a finer scale.<br>
 * Gray8DetectHaarMultiScale is structured as a pipeline stage so push'ing an image
 * results in a new mask being available on getFront. The mask can be further processed
 * by doing connected component detection to determine the feature characteristics,
 * or the mask can be displayed in an overlay on the original image to show the
 * feature areas.
 * @author webb
 */
public class Gray8DetectHaarMultiScale extends PipelineStage {
    private HaarClassifierCascade hcc;
    // maximum scale is the largest factor the image is divided by
    private int nMaxScale = 10;
    // minimum scale is the smallest factor the image is divided by
    private int nMinScale = 5;
    // scale change is the change in scale from one search to the next
    // times 256
    private int nScaleChange = 12 * 256 / 10;
       
    /**
     * Creates a new instance of Gray8DetectHaarMultiScale. The scale parameters correspond
     * to the size of a square area in the original input image that are averaged to
     * create a single pixel in the image used for detection. A scale factor of 1 would
     * do detection at full image resolution.
     * @param is Input stream containing the Haar cascade. This input stream is created
     * by the Haar2J2me program (run on a PC) from a Haar cascade that has been
     * trained using the OpenCV. See {http://sourceforge.net/projects/opencv} for
     * more information about the OpenCV. The Haar2J2me program should be available
     * wherever you got this code from.
     * @param nMinScale Minimum (finest) scale at which features will be detected.
     * @param nMaxScale Maximum (coarsest) scale at which features will be detected.
     * @throws jjil.core.Error if there is an error in the input file.
     * @throws java.io.IOException if there is an I/O error reading the input file.
     */
    public Gray8DetectHaarMultiScale(InputStream is, int nMinScale, int nMaxScale) 
    	throws jjil.core.Error, IOException
    {
        this.nMinScale = nMinScale;
        this.nMaxScale = nMaxScale;
        // load Haar classifier cascade
        InputStreamReader isr = new InputStreamReader(is);
        this.hcc = HaarClassifierCascade.fromStream(isr);
    }
    
    /**
     * Apply multi-scale Haar cascade and prepare a mask image showing where features
     * were detected.
     * @param image Input Gray8Image.
     * @throws jjil.core.Error if the input is not a Gray8Image or is too small.
     */
         
    public void push(Image image) throws jjil.core.Error
    {
        Gray8Image imGray;
        if (image instanceof Gray8Image) {
            imGray = (Gray8Image) image;
        } else {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                            image.toString(),
                            null,
                            null);
        }
        if (image.getWidth() < this.hcc.getWidth() ||
            image.getHeight() < this.hcc.getHeight()) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_TOO_SMALL,
                            image.toString(),
                            this.hcc.toString(),
                            null);
        }
        int nScale = Math.min(this.nMaxScale, 
                Math.min(image.getWidth() / this.hcc.getWidth(),
                image.getHeight() / this.hcc.getHeight()));
        // Zero the mask
        Gray8Image imMask = new Gray8Image(1,1,Byte.MIN_VALUE);
        while (nScale >= this.nMinScale) {
            // shrink the input image
            int nTargetWidth = imGray.getWidth() / nScale;
            int nTargetHeight = imGray.getHeight() / nScale;
            Gray8Shrink gs = new Gray8Shrink(nTargetWidth, nTargetHeight);
            gs.push(imGray);
            Gray8Image imShrunk = (Gray8Image) gs.getFront();
            // scale the mask to the new size
            Gray8RectStretch grs = new Gray8RectStretch(nTargetWidth, nTargetHeight);
            grs.push(imMask);
            imMask = (Gray8Image) grs.getFront();
            // combine the image and mask to make a masked image
            Gray8MaskedImage gmi = new Gray8MaskedImage(imShrunk, imMask);
            // pass the masked image to a subimage generator
            MaskedGray8SubImgGen mgsi = new MaskedGray8SubImgGen(
                    this.hcc.getWidth(),
                    this.hcc.getHeight(),
                    Math.max(1, gmi.getWidth() / 30),
                    Math.max(1, gmi.getHeight() / 30));
            mgsi.push(gmi);
            // now run Haar detection on each scaled image
            int nxLastFound = -hcc.getWidth();
            int nyLastFound = -hcc.getHeight();
            while (!mgsi.isEmpty()) {
                Gray8OffsetImage imSub = (Gray8OffsetImage) mgsi.getFront();
                // if we've found a feature recently we skip forward until
                // we're outside the masked region. There's no point rerunning
                // the detector
                if (imSub.getXOffset() > nxLastFound + hcc.getWidth() &&
                    imSub.getYOffset() > nyLastFound + hcc.getHeight()) {
                    if (hcc.eval(imSub)) {
                        // Found something. 
                        nxLastFound = imSub.getXOffset();
                        nyLastFound = imSub.getYOffset();
                        // assign Byte.MAX_VALUE to the feature area so we don't
                        // search it again
                        Gray8Rect gr = new Gray8Rect(nxLastFound, 
                                nyLastFound, 
                                this.hcc.getWidth(), 
                                this.hcc.getHeight(), 
                                Byte.MAX_VALUE);
                        gr.push(imMask);
                        imMask = (Gray8Image) gr.getFront();
                     }
                }
            }
            nScale = nScale * 256 / this.nScaleChange;
        }
        // Stretch imMask to original image size; this is the result
        Gray8RectStretch grs = new Gray8RectStretch(image.getWidth(), image.getHeight());
        grs.push(imMask);
        super.setOutput(grs.getFront());
    }
     
    /**
     * Set minimum and maximum scale.
     * @param nMinScale The finest scale -- a scale factor of 1 corresponds to the full image resolution.
     * @param nMaxScale The coarsest scale. A scale factor equal to the image width (for a square
     * image) would mean the entire image is reduced to a single pixel.<br>
     * <B>Note.</B> The maximum scale actually used is the maximum of this 
     * number and the scale which would  reduce the image size to the smallest
     * size that the image used in the Haar cascade would fit inside.
     */
    public void setScale(int nMinScale, int nMaxScale) {
        this.nMinScale = nMinScale;
        this.nMaxScale = nMaxScale;
    }
}
