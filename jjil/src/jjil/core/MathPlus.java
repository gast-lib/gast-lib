/*
 * MathPlus.java
 *   
 *
 * Created on October 29, 2007, 1:39 PM
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

package jjil.core;

/**
 * Mathematical routines which are normally provided by the Java Math class but
 * which aren't available with CLDC 1.0.
 * @author webb
 */
public class MathPlus {
    // PI is PI * 2**16, rounded to the nearest integer
    /**
     * PI, scaled by SCALE.
     */
    public static int PI = 205887;
    // Scale factor applied to all calculations
    /**
     * The scale factor for this class. parameters and results are scaled by
     * this factor, currently 2**16 = 65536.
     */
    public static int SCALE = 65536; // 2**16
    /**
     *  Number of log base 2 of SCALE. Used when we want to divide or multiply
     * using shifting.
     */
    public static int SHIFT = 16;
    
    /**
     * Returns the cosine of the argument, scaled by SCALE. The argument also must be 
     * scaled by SCALE. The calculation is done using the Taylor series expansion.
     * @param x the angle to take the sin of (measured in radians). 
     * @return the sin of the provided angle.
     */
    public static int cos(int x) {
        // reduce x so it lies between -PI/2 and PI/2
        // of course since x is scaled by 2**16 this is -PI*2**15 to PI*2**15
        int n = (2 * x/MathPlus.PI + MathPlus.sign(x))>>1;
        x -= n * MathPlus.PI;
        int num = SCALE;
        int den = 1;
        int sum = SCALE;
        int fact = 0; // The denominator contains fact!
        for (int i = 0; i < 4; ++i) {
            // each time we multiply by x were also scaling by 2**16
            // so we shift to keep in scale
            num = ((-(num>>8)*(x>>8))>>8)*(x>>8);
            den *= ++fact;
            den *= ++fact;
            sum += num/den;
        }
        // remember when we reduced x so it was between -PI/2 and PI/2?
        // now restore the sign so the computation works for x from -PI to PI
        return (n%2 == 0) ? sum : -sum;
    }
    
    /**
     * Returns the complex number e**(ix), that is,<br>
     * <center>    cos x + i sin x          (de Moivre's rule) </center>
     * @return e**(ix), i.e., cos x + i sin x, scaled by SCALE.
     * @param x the number to compute the imaginary exponential of. Should be scaled
     * by SCALE, as is the result.
     */
    public static Complex expImag(int x) {
        // reduce x so it lies between -PI/2 and PI/2
        // of course since x is scaled by 2**16 this is -PI*2**15 to PI*2**15
        int n = (2 * x/MathPlus.PI + MathPlus.sign(x))>>1;
        x -= n * MathPlus.PI;
        int num = x;
        int den = 1;
        int sumCos = SCALE;
        int sumSin = x;
        int fact = 1; // The denominator contains fact!
        // 5 iterations are enough for precision with 16-bit inputs
        for (int i = 0; i < 4; ++i) {
            // each time we multiply by x were also scaling by 2**16
            // so we shift to keep in scale. The shift is divided into
            // two parts to reduce loss of precision.
            num = ((-num)>>8)*(x>>8); // (+/-) x ** (2*i)
            den *= ++fact; // (i*2 + 2)!
            sumCos += num/den;
            num = (num>>8)*(x>>8); // (+/-) x ** (2*i + 1)
            den *= ++fact; // (i*2 + 3)!
            sumSin += num/den;
        }
        // remember when we reduced x so it was between -PI/2 and PI/2?
        // now restore the sign so the computation works for x from -PI to PI
        return (n%2 == 0) ? 
            new Complex(sumCos, sumSin) : 
            new Complex(-sumCos, -sumSin);
    }
   
    /**
     * Returns sign of the argument: 0 if arg = 0, 1 if arg is > 0, -1 otherwise.
     * @param x Number to take the sign of.
     * @return the sign of the argument: <br>
     * <quote>0 if arg = 0 <br>
     * 1 if arg > 0<br>
     * -1 if arg < 0.
     */
    public static int sign(int x) {
        if (x == 0) return 0;
        return (x>0) ? 1 : -1;
    }

    /**
     * Returns the sine of the argument, scaled by SCALE. The argument also must be 
     * scaled by SCALE. The calculation is done using the Taylor series expansion.
     * @param x the angle to take the sin of (measured in radians). 
     * @return the sin of the provided angle.
     */
    public static int sin(int x) {
        // reduce x so it lies between -PI/2 and PI/2
        // of course since x is scaled by 2**16 this is -PI*2**15 to PI*2**15
        int n = (2 * x/MathPlus.PI + MathPlus.sign(x))>>1;
        x -= n * MathPlus.PI;
        int num = x;
        int den = 1;
        int sum = x;
        int fact = 1; // The denominator contains fact!
        for (int i = 0; i < 4; ++i) {
            // each time we multiply by x were also scaling by 2**16
            // so we shift to keep in scale
            num = ((-(num>>8)*(x>>8))>>8)*(x>>8);
            den *= ++fact;
            den *= ++fact;
            sum += num/den;
        }
        // remember when we reduced x so it was between -PI/2 and PI/2?
        // now restore the sign so the computation works for x from -PI to PI
        return (n%2 != 0) ? -sum : sum;
    }
 
    /**
     * Computes square root using Newton's iteration.
     * @param x number to take square root of
     * @throws jjil.core.Error if x < 0
     * @return the square root of x
     */
    public static int sqrt(int x) throws jjil.core.Error {
        if (x < 0) {
            throw new Error(
            				Error.PACKAGE.CORE,
            				ErrorCodes.MATH_NEGATIVE_SQRT,
            				new Integer(x).toString(),
            				null,
            				null);
        }
        // special case for 0
        if (x == 0) {
            return 0;
        }
        int nSqrt; // final result
        int nSqrtNew = Math.max(1, x >> 1);  // initial estimate
        do  {
            nSqrt = nSqrtNew;
            nSqrtNew = (nSqrt + x / nSqrt) >> 1;
            // the loop might not converge to an exact value because
            // of the division. We test for a value within 1 of the
            // right value
        } while (Math.abs(nSqrt - nSqrtNew) > 1);
        return nSqrtNew;
    }
    
    /**
     * Compute square.
     * @param n integer to square
     * @return n<sup>2</sup>
     */
    public static int square(int n) {
        return n*n;
    }
}
