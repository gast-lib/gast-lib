/*
 * Computes the mean and standard deviation of n RgbImage.
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jjil.algorithm;

import jjil.core.Error;
import jjil.core.MathPlus;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 *
 * @author webb
 */
public class RgbMeanStdDev {
    /**
     * nRgbMean is the computed mean value of the image, in a
     * packed int.
     */
    int nRgbMean;
    /**
     * nRgbStdDev is the computed standard deviation of the
     * image, in a packed int.
     */
    int nRgbStdDev;

    /**
     * Compute mean and standard deviation of an RGB image.
     * Both are stored in packed RGB ints.
     * The computation is done taking into account the scaling
     * needed to avoid overflow for up to 4096 &times; 4096 images.
     * @param rgb the RgbImage to compute the mean and standard
     * deviation of.
     */
    public void push(RgbImage rgb) {
        int nSumR = 0, nSumG = 0, nSumB = 0;
        int nSumRSq = 0, nSumGSq = 0, nSumBSq = 0;
        int[] nData = rgb.getData();
        int nArea = rgb.getWidth() * rgb.getHeight();
        for (int i = 0; i < nArea; i++) {
            int nR = RgbVal.getR(nData[i]);
            int nG = RgbVal.getG(nData[i]);
            int nB = RgbVal.getB(nData[i]);
            // the RGB values are 8-bit and integer is 32-bit
            // so this sum will work
            // for up to 2**12 = 4096 x 2096 images
            nSumR += nR;
            nSumG += nG;
            nSumB += nB;
            // 8-bit RGB values give a 16-bit product scaled
            // so that the sum will work for up to 4k x 4k 
            // images
            nSumRSq += (nR * nR) >> 8;
            nSumGSq += (nG * nG) >> 8;
            nSumBSq += (nB * nB) >> 8;
        }
        int nRMean = nSumR/nArea;
        int nGMean = nSumG/nArea;
        int nBMean = nSumB/nArea;
        this.nRgbMean = RgbVal.toRgb(
                (byte) nRMean, 
                (byte) nGMean, 
                (byte) nBMean);
        // compute variance. Remember the sum*Sq values have been
        // scaled 8 bits to prevent overflow.
        int nVarR = ((nSumRSq / nArea) << 8) - nRMean * nRMean;
        int nVarG = ((nSumGSq / nArea) << 8) - nGMean * nGMean;
        int nVarB = ((nSumBSq / nArea) << 8) - nBMean * nBMean;
        int nRStdDev = 0;
        int nGStdDev = 0;
        int nBStdDev = 0;
        if (nVarR > 0) {
            try {
                nRStdDev = MathPlus.sqrt(nVarR);
            } catch (Error ex) {
                // this should never happen. It can occur
                // only when nVarR is < 0, which we test for
            }
        }
        if (nVarG > 0) {
            try {
                nGStdDev = MathPlus.sqrt(nVarG);
            } catch (Error ex) {
            }
        }
        if (nVarB > 0) {
            try {
                nBStdDev = MathPlus.sqrt(nVarB);
            } catch (Error ex) {
            }
        }
        this.nRgbStdDev = RgbVal.toRgb(
                (byte) nRStdDev,
                (byte) nGStdDev,
                (byte) nBStdDev);
    }
    
    /**
     * Get RGB mean value
     * @return mean value, packed into an RGB int.
     */
    public int getMean() {
        return this.nRgbMean;
    }
    
    /**
     * Get RGB standard deviation
     * @return standard deviation, packed into an RGB int.
     */
    public int getStdDev() {
        return this.nRgbStdDev;
    }
}
