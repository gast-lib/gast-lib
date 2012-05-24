/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.algorithm;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import jjil.core.Error;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.Rect;
import jjil.core.RgbImage;
import jjil.core.RgbRegion;
import jjil.core.RgbRegion.MeanVar;
import jjil.core.RgbVal;

/**
 * Uses region splitting to break the input image down into rectangular areas
 * of similar color. The region granularity can be controlled by setting a 
 * maximum standard deviation in each color channel.
 * @author webb
 */
public class RgbSplit extends PipelineStage {
    int nRVar, nGVar, nBVar;
    Random random = new Random();
    RgbImage rgbInput;
    Vector vecROk = null;  // vector of RgbRegions
    
    /**
     * Construct the class, setting the maximum standard deviation in each
     * color channel.
     * @param rStdDev maximum red standard deviation
     * @param gStdDev maximum green standard deviation
     * @param bStdDev maximum blue standard deviation.
     */
    public RgbSplit(int rStdDev, int gStdDev, int bStdDev) {
        this.nRVar = rStdDev;
        this.nGVar = gStdDev;
        this.nBVar = bStdDev;
        this.nRVar *= this.nRVar;
        this.nGVar *= this.nGVar;
        this.nBVar *= this.nBVar;
    }
    
    /**
     * Compute variance of a rectangle in the input image.
     * @param r the Rect outlining the region to compute
     * @return a MeanVar object containing the mean and variance of the region.
     */
    private MeanVar computeVariance(Rect r) {
        int[] nData = rgbInput.getData();
        int nSumR = 0, nSumG = 0, nSumB = 0;
        int nSumRSq = 0, nSumGSq = 0, nSumBSq = 0;
        for (int i=r.getTop(); i<r.getBottom(); i++) {
            for (int j=r.getLeft(); j<r.getRight(); j++) {
                int nRgbVal = nData[i*this.rgbInput.getWidth()+j];
                int nR = RgbVal.getR(nRgbVal);
                int nG = RgbVal.getG(nRgbVal);
                int nB = RgbVal.getB(nRgbVal);
                nSumR += nR;
                nSumG += nG;
                nSumB += nB;
                nSumRSq += nR * nR;
                nSumGSq += nG * nG;
                nSumBSq += nB * nB;
            }
        }
        // compute average
        int nAvgR = nSumR / r.getArea();
        int nAvgG = nSumG / r.getArea();
        int nAvgB = nSumB / r.getArea();
        int nVarR = nSumRSq / r.getArea() - nAvgR * nAvgR;
        int nVarG = nSumGSq / r.getArea() - nAvgG * nAvgG;
        int nVarB = nSumBSq / r.getArea() - nAvgB * nAvgB;
        return new MeanVar(
                RgbVal.toRgb((byte)nAvgR, (byte)nAvgG, (byte)nAvgB),
                nVarR, nVarG, nVarB);
    }
    
    /**
     * Split an input RgbImage into rectangular regions of standard deviation
     * less than or equal to the thresholds specified in the constructor. The
     * minimum region size is 2x2. Regions are split horizontally and vertically
     * in each pass so as to converge to roughly square blocks.
     * @param rgbImage the input RgbImage
     */
    public void split(RgbImage rgbImage) {
        this.rgbInput = rgbImage;
        Vector vecRNotOk = new Vector();
        this.vecROk = new Vector();
        vecRNotOk.addElement(new Rect(
                0,
                0,
                this.rgbInput.getWidth(),
                this.rgbInput.getHeight()));
        while (!vecRNotOk.isEmpty()) {
            Rect r = (Rect) vecRNotOk.elementAt(0);
            vecRNotOk.removeElementAt(0);
            if (r.getHeight() >= 2 && r.getWidth() >= 2) {
                MeanVar nVar = computeVariance(r);
                if (nVar.getRVar()>this.nRVar ||
                        nVar.getGVar()>this.nGVar ||
                        nVar.getB()>this.nBVar) {
                    // split horizontally or vertically, whichever
                    // is longer
                    if (r.getWidth() >= r.getHeight()) {
                        // split horizontally
                        int nHalfWidth = r.getWidth()/2;
                        Rect rNew = 
                                new Rect(r.getLeft(), 
                                r.getTop(), 
                                nHalfWidth, 
                                r.getHeight());
                        vecRNotOk.addElement(rNew);
                        rNew = new Rect(r.getLeft()+nHalfWidth, 
                                r.getTop(),
                                r.getWidth() - nHalfWidth,
                                r.getHeight());
                        vecRNotOk.addElement(rNew);
                    } else {
                        // split vertically
                        int nHalfHeight = r.getHeight()/2;
                        Rect rNew = new Rect(r.getLeft(), 
                                r.getTop(),
                                r.getWidth(),
                                nHalfHeight);
                        vecRNotOk.addElement(rNew);
                        rNew = new Rect(r.getLeft(), 
                                r.getTop()+nHalfHeight,
                                r.getWidth(),
                                r.getHeight() - nHalfHeight);
                        vecRNotOk.addElement(rNew);
                    }
                } else {
                    RgbRegion reg = new RgbRegion(r, nVar);
                    this.vecROk.addElement(reg);
                }
            } else {
                // region too small, stop splitting
                MeanVar nVar = computeVariance(r);
                    RgbRegion reg = new RgbRegion(r, nVar);
                    this.vecROk.addElement(reg);                
            }
        }
    }
    
    /**
     * Returns a color image with colors randomly assigned to regions. This
     * is used during debugging to see how the image has been split so that
     * the threshold can be adjusted.
     * @return RgbImage with colors randomly assigned to regions.
     * @throws jjil.core.Error if push() hasn't been called yet
     */
    public RgbImage getRandomizedRgbImage() throws jjil.core.Error {
        if (this.vecROk == null) {
            throw new jjil.core.Error(
                            jjil.core.Error.PACKAGE.CORE,
                            jjil.core.ErrorCodes.NO_RESULT_AVAILABLE,
                            null,
                            null,
                            null);
        }
        RgbImage rgbImage = new RgbImage(
                this.rgbInput.getWidth(),
                this.rgbInput.getHeight());
        for (Enumeration e = this.vecROk.elements(); e.hasMoreElements();) {
            RgbRegion r = (RgbRegion) e.nextElement();
            int nRgb = RgbVal.toRgb(
                    (byte)((this.random.nextInt()&0xff)+Byte.MIN_VALUE), 
                    (byte)((this.random.nextInt()&0xff)+Byte.MIN_VALUE), 
                    (byte)((this.random.nextInt()&0xff)+Byte.MIN_VALUE));
            Rect rect = r.getRect();
            rgbImage = rgbImage.fill(rect, nRgb);
        }
        return rgbImage;
        
    }
    
    /**
     * Return the region list.
     * @return an Enumeration on RgbRegion objects.
     * @throws jjil.core.Error if push() hasn't been called yet
     */
    public Enumeration getRegions() throws jjil.core.Error {
        if (this.vecROk == null) {
            throw new jjil.core.Error(
                            jjil.core.Error.PACKAGE.CORE,
                            jjil.core.ErrorCodes.NO_RESULT_AVAILABLE,
                            null,
                            null,
                            null);
        }
        return this.vecROk.elements();
    }
    
    /**
     * Return an RgbImage with the mean color of each region assigned. This
     * should be an approximation of the original input image, except more 
     * blocky, depending on the thresholds set.
     * @return RgbImage with the mean color of each region assigned to the
     * region.
     * @throws jjil.core.Error if push() hasn't been called yet
     */
    public RgbImage getRgbImage() throws jjil.core.Error {
        if (this.vecROk == null) {
            throw new jjil.core.Error(
                            jjil.core.Error.PACKAGE.CORE,
                            jjil.core.ErrorCodes.NO_RESULT_AVAILABLE,
                            null,
                            null,
                            null);
        }
        RgbImage rgbImage = new RgbImage(
                this.rgbInput.getWidth(),
                this.rgbInput.getHeight());
        for (Enumeration e = this.vecROk.elements(); e.hasMoreElements();) {
            RgbRegion r = (RgbRegion) e.nextElement();
            int nRgb = r.getColor();
            Rect rect = r.getRect();
            rgbImage = rgbImage.fill(rect, nRgb);
        }
        return rgbImage;
    }
    
    /**
     * Implement toString
     * @return a string giving the name of this class, the parameters, and
     * the vector result if push has been called.
     */
    public String toString() {
        String szResult = super.toString() + "(" + new Integer(this.nRVar).toString() +
                "," + new Integer(this.nGVar).toString() + 
                "," + new Integer(this.nBVar).toString();
        if (this.vecROk != null) {
            szResult += "," + this.vecROk.toString();
        }
        return szResult + ")";
    }

    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof RgbImage)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_RGBIMAGE,
                            imageInput.toString(),
                            null,
                            null);
        }
        this.split((RgbImage)imageInput);
        super.setOutput(this.getRgbImage());
    }
}
