/*
 * RgbVal.java
 *
 * Created on September 9, 2006, 10:42 AM
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
 * Helper class for manipulating RGB values. All functions are static.
 * 
 * @author webb
 */
public class RgbVal {
    // for translating unsigned int values to signed byte values with the 
    // same bit field. I can't think of a simpler way to do this. 
    public final static byte[] unsignedIntToSignedByte = { (byte) 0, (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8,
            (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14,
            (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20,
            (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26,
            (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32,
            (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38,
            (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44,
            (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50,
            (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56,
            (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) 62,
            (byte) 63, (byte) 64, (byte) 65, (byte) 66, (byte) 67, (byte) 68,
            (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74,
            (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80,
            (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86,
            (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 91, (byte) 92,
            (byte) 93, (byte) 94, (byte) 95, (byte) 96, (byte) 97, (byte) 98,
            (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103,
            (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108,
            (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113,
            (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118,
            (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 123,
            (byte) 124, (byte) 125, (byte) 126, (byte) 127, (byte) -255,
            (byte) -254, (byte) -253, (byte) -252, (byte) -251, (byte) -250,
            (byte) -249, (byte) -248, (byte) -247, (byte) -246, (byte) -245,
            (byte) -244, (byte) -243, (byte) -242, (byte) -241, (byte) -240,
            (byte) -239, (byte) -238, (byte) -237, (byte) -236, (byte) -235,
            (byte) -234, (byte) -233, (byte) -232, (byte) -231, (byte) -230,
            (byte) -229, (byte) -228, (byte) -227, (byte) -226, (byte) -225,
            (byte) -224, (byte) -223, (byte) -222, (byte) -221, (byte) -220,
            (byte) -219, (byte) -218, (byte) -217, (byte) -216, (byte) -215,
            (byte) -214, (byte) -213, (byte) -212, (byte) -211, (byte) -210,
            (byte) -209, (byte) -208, (byte) -207, (byte) -206, (byte) -205,
            (byte) -204, (byte) -203, (byte) -202, (byte) -201, (byte) -200,
            (byte) -199, (byte) -198, (byte) -197, (byte) -196, (byte) -195,
            (byte) -194, (byte) -193, (byte) -192, (byte) -191, (byte) -190,
            (byte) -189, (byte) -188, (byte) -187, (byte) -186, (byte) -185,
            (byte) -184, (byte) -183, (byte) -182, (byte) -181, (byte) -180,
            (byte) -179, (byte) -178, (byte) -177, (byte) -176, (byte) -175,
            (byte) -174, (byte) -173, (byte) -172, (byte) -171, (byte) -170,
            (byte) -169, (byte) -168, (byte) -167, (byte) -166, (byte) -165,
            (byte) -164, (byte) -163, (byte) -162, (byte) -161, (byte) -160,
            (byte) -159, (byte) -158, (byte) -157, (byte) -156, (byte) -155,
            (byte) -154, (byte) -153, (byte) -152, (byte) -151, (byte) -150,
            (byte) -149, (byte) -148, (byte) -147, (byte) -146, (byte) -145,
            (byte) -144, (byte) -143, (byte) -142, (byte) -141, (byte) -140,
            (byte) -139, (byte) -138, (byte) -137, (byte) -136, (byte) -135,
            (byte) -134, (byte) -133, (byte) -132, (byte) -131, (byte) -130,
            (byte) -129, (byte) -128, (byte) -127, (byte) -126, (byte) -125,
            (byte) -124, (byte) -123, (byte) -122, (byte) -121, (byte) -120,
            (byte) -119, (byte) -118, (byte) -117, (byte) -116, (byte) -115,
            (byte) -114, (byte) -113, (byte) -112, (byte) -111, (byte) -110,
            (byte) -109, (byte) -108, (byte) -107, (byte) -106, (byte) -105,
            (byte) -104, (byte) -103, (byte) -102, (byte) -101, (byte) -100,
            (byte) -99, (byte) -98, (byte) -97, (byte) -96, (byte) -95,
            (byte) -94, (byte) -93, (byte) -92, (byte) -91, (byte) -90,
            (byte) -89, (byte) -88, (byte) -87, (byte) -86, (byte) -85,
            (byte) -84, (byte) -83, (byte) -82, (byte) -81, (byte) -80,
            (byte) -79, (byte) -78, (byte) -77, (byte) -76, (byte) -75,
            (byte) -74, (byte) -73, (byte) -72, (byte) -71, (byte) -70,
            (byte) -69, (byte) -68, (byte) -67, (byte) -66, (byte) -65,
            (byte) -64, (byte) -63, (byte) -62, (byte) -61, (byte) -60,
            (byte) -59, (byte) -58, (byte) -57, (byte) -56, (byte) -55,
            (byte) -54, (byte) -53, (byte) -52, (byte) -51, (byte) -50,
            (byte) -49, (byte) -48, (byte) -47, (byte) -46, (byte) -45,
            (byte) -44, (byte) -43, (byte) -42, (byte) -41, (byte) -40,
            (byte) -39, (byte) -38, (byte) -37, (byte) -36, (byte) -35,
            (byte) -34, (byte) -33, (byte) -32, (byte) -31, (byte) -30,
            (byte) -29, (byte) -28, (byte) -27, (byte) -26, (byte) -25,
            (byte) -24, (byte) -23, (byte) -22, (byte) -21, (byte) -20,
            (byte) -19, (byte) -18, (byte) -17, (byte) -16, (byte) -15,
            (byte) -14, (byte) -13, (byte) -12, (byte) -11, (byte) -10,
            (byte) -9, (byte) -8, (byte) -7, (byte) -6, (byte) -5, (byte) -4,
            (byte) -3, (byte) -2, (byte) -1 };

    /**
     * Converts byte R, G, and B values to an ARGB word. byte is a signed data
     * type but the ARGB word has unsigned bit fields. In other words the
     * minimum byte value is Byte.MIN_VALUE but the color black in the ARGB word
     * is represented as 0x00. So we must subtract Byte.MIN_VALUE to get an
     * unsigned byte value before shifting and combining the bit fields.
     * 
     * @param R
     *            input signed red byte
     * @param G
     *            input signed green byte
     * @param B
     *            input signed blue byte
     * @return the color ARGB word.
     */
    public static int toRgb(byte R, byte G, byte B) {
        return 0xFF000000 | (toUnsignedInt(R) << 16) | (toUnsignedInt(G) << 8)
                | toUnsignedInt(B);
    }

    /**
     * Compare two RgbVals in absolute value.
     * 
     * @return sum of absolute differences between pixel values
     */
    public static int getAbsDiff(int rgb1, int rgb2) {
        return Math.abs(RgbVal.getR(rgb1) - RgbVal.getR(rgb2))
                + Math.abs(RgbVal.getG(rgb1) - RgbVal.getG(rgb2))
                + Math.abs(RgbVal.getB(rgb1) - RgbVal.getB(rgb2));
    }

    /**
     * Compare two RgbVals in maximum difference in any band.
     * 
     * @return maximum difference between pixel values in any band
     */
    /**
     * Computes maximum difference (largest difference in color, R, G, or B) of
     * two color values.
     * 
     * @param ARGB1
     *            first color
     * @param ARGB2
     *            second color
     * @return largest difference. Will always be >= 0, <= 256.
     */
    public static int getMaxDiff(int ARGB1, int ARGB2) {
        int nR1 = RgbVal.getR(ARGB1);
        int nG1 = RgbVal.getG(ARGB1);
        int nB1 = RgbVal.getB(ARGB1);
        int nR2 = RgbVal.getR(ARGB2);
        int nG2 = RgbVal.getG(ARGB2);
        int nB2 = RgbVal.getB(ARGB2);
        return Math.max(Math.abs(nR1 - nR2),
                Math.max(Math.abs(nG1 - nG2), Math.abs(nB1 - nB2)));
    }

    public static int getProportionateDiff(int ARGB1, int ARGB2) {
        int nR1 = RgbVal.getR(ARGB1) - Byte.MIN_VALUE;
        int nG1 = RgbVal.getG(ARGB1) - Byte.MIN_VALUE;
        int nB1 = RgbVal.getB(ARGB1) - Byte.MIN_VALUE;
        int nR2 = RgbVal.getR(ARGB2) - Byte.MIN_VALUE;
        int nG2 = RgbVal.getG(ARGB2) - Byte.MIN_VALUE;
        int nB2 = RgbVal.getB(ARGB2) - Byte.MIN_VALUE;
        // We're solving the equation
        // min/r ((r*nR1 - nR2) + (r*nG1 - nG2) + (r*nB1 - nB2))**2
        // which gives 2*((r*nR1 - nR2)*nR1 + (r*nG1 - nG2)*nG1 + (r*nB1 -
        // nB2)*nB1) = 0
        // or r = (nR1*nR2 + nG1*nG2 + nB1*nB2) / (nR1*nR1 + nG1*nG1 + nB1*nB1)
        // we divide r into nNum / nDenom to avoid floating point
        int nNum = (nR1 * nR2 + nG1 * nG2 + nB1 * nB2);
        int nDenom = (nR1 * nR1 + nG1 * nG1 + nB1 * nB1);
        if (nDenom == 0) {
            return 3 * Byte.MAX_VALUE;
        }
        // the error is then ((r*nR1 - nR2) + (r*nG1 - nG2) + (r*nB1 - nB2))**2
        // or (r*(nR1 + nG1 + nB1) - (nR2 + nB2 + nG2))**2
        // or ((nNum*(nR1 + nG1 + nB1) - nDenom*(nR1 + nG2 + nB2)) / nDenom)**2
        // or ((nNum*(nR1 + nG1 + nB1) - nDenom*(nR1 + nG2 + nB2))**2 /
        // nDenom**2
        return MathPlus.square(8 * (nNum * (nR1 + nG1 + nB1) - nDenom
                * (nR1 + nG2 + nB2)))
                / MathPlus.square(nDenom);
    }

    /**
     * Compare two RgbVals in sum of squares difference.
     * 
     * @return sum of squares differences between pixel values
     */
    public static int getSqrDiff(int rgb1, int rgb2) {
        return MathPlus.square(RgbVal.getR(rgb1) - RgbVal.getR(rgb2))
                + MathPlus.square(RgbVal.getG(rgb1) - RgbVal.getG(rgb2))
                + MathPlus.square(RgbVal.getB(rgb1) - RgbVal.getB(rgb2));
    }

    /**
     * Extracts blue byte from input ARGB word. The bit fields in ARGB word are
     * unsigned, ranging from 0x00 to 0xff. To convert these to the returned
     * signed byte value we must add Byte.MIN_VALUE.
     * 
     * @return the blue byte value, converted to a signed byte
     * @param ARGB
     *            the input color ARGB word.
     */
    public static byte getB(int ARGB) {
        return toSignedByte((byte) (ARGB & 0xff));
    }

    /**
     * Extracts green byte from input ARGB word. The bit fields in ARGB word are
     * unsigned, ranging from 0x00 to 0xff. To convert these to the returned
     * signed byte value we must add Byte.MIN_VALUE.
     * 
     * @param ARGB
     *            the input color ARGB word.
     * @return the green byte value, converted to a signed byte
     */
    public static byte getG(int ARGB) {
        return toSignedByte((byte) ((ARGB >> 8) & 0xff));
    }

    /**
     * Extracts red byte from input ARGB word. The bit fields in ARGB word are
     * unsigned, ranging from 0x00 to 0xff. To convert these to the returned
     * signed byte value we must add Byte.MIN_VALUE.
     * 
     * @param ARGB
     *            the input color ARGB word.
     * @return the red byte value, converted to a signed byte
     */
    public static byte getR(int ARGB) {
        return toSignedByte((byte) ((ARGB >> 16) & 0xff));
    }

    /**
     * Return "vector" difference of Rgb values. Treating each Rgb value as a
     * 3-element vector form the value (ARGB-ARGBTarg) . ARGBVec where . is dot
     * product. Useful for determining whether an Rgb value is near another
     * weighted the different channels differently.
     * 
     * @param ARGB
     *            tested Rgb value
     * @param ARGBTarg
     *            target Rgb value
     * @param ARGBVec
     *            weighting
     * @return (ARGB-ARGBTarg) . ARGBVec where . is dot product and the Rgb
     *         values are treated as 3-vectors.
     */
    public static int getVecDiff(int ARGB, int ARGBTarg, int ARGBVec) {
        int nR1 = RgbVal.getR(ARGB);
        int nG1 = RgbVal.getG(ARGB);
        int nB1 = RgbVal.getB(ARGB);
        int nR2 = RgbVal.getR(ARGBTarg);
        int nG2 = RgbVal.getG(ARGBTarg);
        int nB2 = RgbVal.getB(ARGBTarg);
        int nR3 = RgbVal.getR(ARGBVec);
        int nG3 = RgbVal.getG(ARGBVec);
        int nB3 = RgbVal.getB(ARGBVec);
        return (nR1 - nR2) * nR3 + (nG1 - nG2) * nG3 + (nB1 - nB2) * nB3;
    }

    /**
     * Converts from an unsigned bit field (as stored in an ARGB word to a
     * signed byte value (that we can do computation on).
     * 
     * @return the signed byte value
     * @param b
     *            the unsigned byte value.
     */
    public static byte toSignedByte(byte b) {
        return (byte) (b + Byte.MIN_VALUE);
    }

    /**
     * Converts from a signed byte value (which we do computation on) to an
     * unsigned bit field (as stored in an ARGB word). The result is returned as
     * an int because the unsigned 8 bit value cannot be represented as a byte.
     * 
     * @return the unsigned bit field
     * @param b
     *            the signed byte value.
     */
    public static int toUnsignedInt(byte b) {
        return (b - Byte.MIN_VALUE);
    }

    /**
     * Provide a way to turn color values into strings
     * 
     * @param ARGB
     *            the input color value
     * @return a string describing the color
     */
    public static String toString(int ARGB) {
        return "[" + new Integer(RgbVal.getR(ARGB)).toString() + ","
                + new Integer(RgbVal.getR(ARGB)).toString() + ","
                + new Integer(RgbVal.getB(ARGB)).toString() + "]";
    }
}
