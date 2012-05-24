/*
 * Gray8AffineWarp.java
 *
 * Created on September 9, 2006, 3:17 PM
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
import jjil.core.Gray8OffsetImage;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.Vec2;

/**
 * This PipelineStage performs an affine transformation on an input
 * @author webb
 */
public class Gray8AffineWarp extends PipelineStage {   
    static final int WARP_X_FIRST = 1;
    static final int WARP_Y_FIRST = 2;
    
    int nMaxX, nMaxY, nMinX, nMinY;
    private int nWarpOrder; // either WARP_X_FIRST or WARP_Y_FIRST
    int nXOffset, nYOffset;
    int rnWarp[][];
    int rnWarpX[];
    int rnWarpY[];
    
    /** Creates a new instance of Gray8AffineWarp. Gray8AffineWarp performs
     * an affine warp on an input Gray8Image. The affine transformation is
     * decomposed into two stages, following the work of George Wolberg.
     * See http://www-cs.ccny.cuny.edu/~wolberg/diw.html for the definitive
     * work on image warping.
     * <p>
     * @param warp the 2x3 affine warp to be performed. The elements of this
     * matrix are assumed to be scaled by 2**16 for accuracy.
     * @throws jjil.core.Error if the warp is null or not a 2x3 matrix.
     */
    public Gray8AffineWarp(int[][] warp) throws jjil.core.Error {
        this.setWarp(warp);
    }
    
    
    /**
     * Calculate the affine transformation applied to p, keeping in mind
     * that the warp is scaled by 2**16, so we must shift to get the
     * correct result
     * @param a 2x3 affine transformation, scaled by 2**16
     * @param p input vector
     * @return transformed vector
     */
    private Vec2 affineTrans(int a[][], Vec2 p) {
        return new Vec2(
                (a[0][0] * p.getX() + a[0][1] * p.getY() + a[0][2])>>16,
                (a[1][0] * p.getX() + a[1][1] * p.getY() + a[1][2])>>16);
    }

    /**
     * Affine warp of an image.
     *
     * @param image the input gray image.
     * @throws jjil.core.Error if the input image is not gray,
     * or the trapezoid already specified extends outside its bounds.
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
        // first calculate bounds of output image
        Vec2 p00 = affineTrans(this.rnWarp, new Vec2(0, 0));
        Vec2 p01 = affineTrans(this.rnWarp, new Vec2(0, image.getHeight()));
        Vec2 p10 = affineTrans(this.rnWarp, new Vec2(image.getWidth(), 0));
        Vec2 p11 = affineTrans(this.rnWarp, new Vec2(image.getWidth(), 
                 image.getHeight()));
        this.nMinX = (int) Math.min(p00.getX(), 
                Math.min(p01.getX(), Math.min(p10.getX(), p11.getX())));
        this.nMaxX = (int) Math.max(p00.getX(), 
                Math.max(p01.getX(), Math.max(p10.getX(), p11.getX())));
        this.nMinY = (int) Math.min(p00.getY(), 
                Math.min(p01.getY(), Math.min(p10.getY(), p11.getY())));
        this.nMaxY = (int) Math.max(p00.getY(), 
                Math.max(p01.getY(), Math.max(p10.getY(), p11.getY())));
        this.nXOffset = -this.nMinX;
        this.nYOffset = -this.nMinY;
        if (this.nWarpOrder == Gray8AffineWarp.WARP_X_FIRST) {
            Gray8Image grayX = warpX((Gray8Image) image);
            //super.setOutput(new Gray8OffsetImage(grayX, this.nXOffset, this.nYOffset));
            Gray8Image grayY = warpY(grayX);
            super.setOutput(new Gray8OffsetImage(grayY, this.nXOffset, this.nYOffset));
        } else {
            Gray8Image grayY = warpY((Gray8Image) image);
            //super.setOutput(new Gray8OffsetImage(grayY, this.nXOffset, this.nYOffset));
            Gray8Image grayX = warpX(grayY);
            super.setOutput(new Gray8OffsetImage(grayX, this.nXOffset, this.nYOffset));
        }
    }
    
    /** Sets the warp in use and decomposes it into two stages, determining
     * the order of the warp (x first or y first).
     * @param warp the 2 x 3 affine warp transformation. The matrix is 
     * assumed to have been scaled by 2**16 for accuracy.
     * @throws jjil.core.Error if the warp is not 2x3 or the warp is not
     * decomposable (warp[0][0] or warp[1][1] is 0).
     */
    public void setWarp(int[][] warp) throws jjil.core.Error {
        if (warp.length != 2 || warp[0].length != 3 || warp[1].length != 3) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            jjil.algorithm.ErrorCodes.PARAMETER_WRONG_SIZE,
                            warp.toString(),
                            null,
                            null);
        }
        // nDivisor is scaled by 2**16. Since warp is scaled by 2**16 multiplying
        // its elements would scale by 2**32, resulting in overflow. So we
        // rescale before multiplying
        int nDivisorY = ((warp[0][0]>>8) * (warp[1][1]>>8)) - 
                ((warp[0][1]>>8) * (warp[1][0]>>8));
        if (warp[0][0] == 0 || warp[1][1] == 0 || nDivisorY == 0) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            jjil.algorithm.ErrorCodes.PARAMETER_OUT_OF_RANGE,
                            warp.toString(),
                            null,
                            null);
        }
        this.rnWarp = warp;
        if (Math.abs(warp[0][0]) > Math.abs(warp[1][1])) {
            // warp in x direction first
            this.nWarpOrder = Gray8AffineWarp.WARP_X_FIRST;
            // copy x warp
            // rnWarp is scaled by 2**16, as is warp. If we divided warp
            // by nDivisor, scaled by 2**16 we'd scale by 2**8 so we must rescale 
            // carefully to get the output scaled properly while not overflowing
            // the warp values (except column 2)
            // are all < 1 so scaling by 2**16 gives a number
            // less than 2**16 which means it can be safely scaled up 8 bits
            this.rnWarpX = new int[3];
            this.rnWarpX[0] = (warp[1][1]<<8) / (nDivisorY>>8);
            this.rnWarpX[1] = -(warp[0][1]<<8) / (nDivisorY>>8);
            this.rnWarpX[2] = ((warp[0][1]>>4)*(warp[1][2]>>4) - 
                    (warp[0][2]>>4)*(warp[1][1]>>4))/(nDivisorY>>8);
            // calculate y warp
            this.rnWarpY = new int[3];
            this.rnWarpY[0] = -(warp[1][0]<<8)/(warp[1][1]>>8);
            this.rnWarpY[1] = (1<<24) / (warp[1][1]>>8);
            this.rnWarpY[2] = -(warp[1][2]<<8) / (warp[1][1]>>8);
        } else {
            // warp in y direction first
            this.nWarpOrder = Gray8AffineWarp.WARP_Y_FIRST;
            // copy y warp
            this.rnWarpY = new int[3];
            this.rnWarpY[0] = -(warp[1][0]<<8) / (nDivisorY>>8);
            this.rnWarpY[1] = (warp[0][0]<<8) / (nDivisorY>>8);
            this.rnWarpY[2] = ((warp[0][2]>>4)*(warp[1][0]>>4) - 
                    (warp[0][0]>>4)*(warp[1][2]>>4))/(nDivisorY>>8);
            // calculate x warp
            this.rnWarpX = new int[3];
            this.rnWarpX[0] = (1<<24) / (warp[0][0]>>8);
            this.rnWarpX[1] = -(warp[0][1]<<8) / (warp[0][0]>>8);
            this.rnWarpX[2] = -(warp[0][2]<<8) / (warp[0][0]>>8);
        }
    }
    
    public Vec2 warpVec(Vec2 p) {
        int x = (p.getX() * this.rnWarp[0][0] + p.getY() * this.rnWarp[0][1] +
                this.rnWarp[0][2])>>16;
        int y = (p.getX() * this.rnWarp[1][0] + p.getY() * this.rnWarp[1][1] +
                this.rnWarp[1][2])>>16;
        return new Vec2(x,y);
    }

    private Gray8Image warpX(Gray8Image grayIn) {
        // allocate image. it is implicitly offset by nMinX
        Gray8Image grayOut = new Gray8Image(
                nMaxX - nMinX, 
                grayIn.getHeight(), 
                Byte.MIN_VALUE);
        // pointer to input
        byte[] bDataIn = grayIn.getData();
        byte[] bDataOut = grayOut.getData();
        for (int x = nMinX; x<nMaxX; x++) {
            for (int y = 0; y<grayIn.getHeight(); y++) {
                // calculate x in original image.
                // nX is scaled by 2**16.
                // y does not change but is offset by nYOffset
                int nX = x * this.rnWarpX[0] +
                        this.rnWarpX[1] * (y + this.nYOffset) +
                        this.rnWarpX[2];
                // nXfloor is the integer value of nX, unscaled
                int nXfloor = nX >> 16;
                // nXfrace is the fractional component of nX, scaled by 2**16
                int nXfrac = nX - (nXfloor<<16);
                // interpolate to get point
                if (nXfloor >= 0 && nXfloor < grayIn.getWidth()-1) {
                    int bIn = bDataIn[y*grayIn.getWidth() + nXfloor];
                    int bInP1 = bDataIn[y*grayIn.getWidth() + nXfloor + 1];
                    int bOut = (bIn * ((1<<16)- nXfrac) + 
                                bInP1 * nXfrac)>>16;
                    bDataOut[grayOut.getWidth()*y + x-nMinX] = (byte) bOut;
                }
            }
        }
        this.nXOffset = nMinX;
        return grayOut;
    }

    private Gray8Image warpY(Gray8Image grayIn) {
        // allocate image. it is implicitly offset by nMinY
        Gray8Image grayOut = new Gray8Image(
                grayIn.getWidth(), 
                nMaxY - nMinY, 
                Byte.MIN_VALUE);
        // pointer to input
        byte[] bDataIn = grayIn.getData();
        byte[] bDataOut = grayOut.getData();
        for (int y = nMinY; y<nMaxY; y++) {
            for (int x = 0; x<grayIn.getWidth(); x++) {
                // calculate y in original image
                // x does not change
                // nY is scaled by 2**16
                int nY = this.rnWarpY[0] * (x + this.nXOffset) +
                        this.rnWarpY[1] * y +
                        this.rnWarpY[2];
                // nYfloor is the integer portion of nY, unscaled
                int nYfloor = nY >> 16;
                // nYfrac is the fractional portion of nY, scaled by 2**16
                int nYfrac = nY - (nYfloor<<16);
                // interpolate to get point
                if (nYfloor >= 0 && nYfloor < grayIn.getHeight()-1) {
                    int bIn = bDataIn[nYfloor*grayIn.getWidth() + x];
                    int bInP1 = bDataIn[(nYfloor+1)*grayIn.getWidth() + x];
                    int bOut =  (bIn * ((1<<16) - nYfrac) + 
                                 bInP1 * nYfrac)>>16;
                    bDataOut[grayOut.getWidth()*(y-nMinY) + x] = (byte) bOut;
                }
            }
        }
        this.nYOffset = nMinY;
        return grayOut;
    }
    
    /**
     * Returns a string describing the current instance. All the constructor
     * parameters are returned in the order specified in the constructor.
     * @return The string describing the current instance. The string is of the form 
     * "jjil.algorithm.Gray8AffineWarpxxx (startRow,endRow,leftColStart,
     * rightColStart,leftColEnd,rightColEnd)"
     */
    public String toString() {
        return super.toString() + " (" + this.rnWarp.toString() + ")"; //$NON-NLS-1$
    }
}
