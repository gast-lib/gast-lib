package jjil.android;

import android.graphics.Color;

public class AndroidColors {
    /**
     * Converts the YUV used in the Android NV21 format into an
     * RGB value using code from 
     * http://msdn.microsoft.com/en-us/library/ms893078
     * @param nY - y value (from WxH byte array)
     * @param nU - u value (second byte in WxH/2 byte array)
     * @param nV - v value (first byte in WxH/2 byte array)
     * @return Android Color for the converted color
     */
    public static int yuv2Color(int nY, int nU, int nV) {
       int nC = nY - 16;
       int nD = nU - 128;
       int nE = nV - 128;

      int nR = Math.max(0, Math.min(255, (( 298 * nC            + 409 * nE + 128) >> 8)));
      int nG = Math.max(0, Math.min(255, (( 298 * nC - 100 * nD - 208 * nE + 128) >> 8)));
      int nB = Math.max(0, Math.min(255, (( 298 * nC + 516 * nD            + 128) >> 8)));
      return Color.argb(255, nR, nG, nB);
    }

    /**
     * Converts from RGB to YUV using code from 
     * http://msdn.microsoft.com/en-us/library/ms893078.
     * Returns the value as a Android Color value in with the red byte
     * is the Y value, green the U value, and blue the V value.
     * @param nR - red value (0-255)
     * @param nG - green value (0-255)
     * @param nB - blue value (0-255)
     * @return
     */
    public static int rgb2yuv(int nR, int nG, int nB) {
        int nY = ( (  66 * nR + 129 * nG +  25 * nB + 128) >> 8) +  16;
        int nU = ( ( -38 * nR -  74 * nG + 112 * nB + 128) >> 8) + 128;
        int nV = ( ( 112 * nR -  94 * nG -  18 * nB + 128) >> 8) + 128;
        return Color.argb(255, nY, nU, nV);
    }
}
