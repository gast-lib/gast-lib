/*
 * Complex.java
 *   Implementation of complex numbers, for use in FFT etc.
 *   This uses integers to store the real and imaginary components.
 *   Scale arguments to the constructor appropriately.
 *   All operations are done in-place so, e.g., x.div(y) modifies x.
 *
 * Created on October 29, 2007, 12:53 PM
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
 * A simple implementation of complex numbers for use in FFT, etc.
 * @author webb
 */
public class Complex {
    private int nImag;
    private int nReal;
    
    
    /**
     * Default constructor.
     */
    public Complex() {
        this.nReal = 0;
        this.nImag = 0;
    }

    /**
     * Copy constructor.
     * @param cx the complex number to copy.
     */
    public Complex(Complex cx) {
        this.nReal = cx.nReal;
        this.nImag = cx.nImag;
    }

    /**
     * Creates a new instance of Complex from real and imaginary arguments.
     * @param nReal Real component.
     * @param nImag Imaginary component.
     */
    public Complex(int nReal, int nImag) {
        this.nReal = nReal;
        this.nImag = nImag;
    }

    /**
     * Create a new Complex number from a real number. The imaginary component will
     * be 0.
     * @param nReal The real component.
     */
    public Complex(int nReal) {
        this.nReal = nReal;
        this.nImag = 0;
    }
    
    /**
     * Complex conjugate
     * @return the complex conjugate of this.
     */
    public Complex conjugate() {
        this.nImag = -this.nImag;
        return this;
    }
        
    /**
     * Divide the complex number by a real ineger.
     * @param n the divisor.
     * @return the Complex number resulting from the division (replaces this).
     * @throws jjil.core.Error if n = 0
     */
    public Complex div(int n) throws jjil.core.Error {
        if (n==0) {
            throw new Error(
            				Error.PACKAGE.CORE,
            				ErrorCodes.MATH_DIVISION_ZERO, 
            				this.toString(), 
            				new Integer(n).toString(), 
            				null);
        }
        this.nReal /= n;
        this.nImag /= n;
        return this;
    }
    
   /**
     * Divides one complex number by another
     * @param cx The complex number to divide by.
     * @return the result of dividing this number by cx.
     * @throws jjil.core.Error If division by 0 is attempted, i.e., cx.square() is 0.
     */
    public Complex div(Complex cx) throws jjil.core.Error {
        int nShift = 0;
        if (Math.abs(cx.real()) >= MathPlus.SCALE ||
            Math.abs(cx.imag()) >= MathPlus.SCALE) {
            cx = new Complex(cx).rsh(MathPlus.SHIFT);
            nShift = MathPlus.SHIFT;
        }
        int nSq = cx.square();
        if (nSq == 0) {
            throw new Error(
            				Error.PACKAGE.CORE,
            				ErrorCodes.MATH_PRODUCT_TOO_LARGE,
            				this.toString(),
            				cx.toString(),
            				null);
        }
        // cx is right shifted by SHIFT bits. So multiplying by it and 
        // dividing by its square shifts left by SHIFT bits. We shift back to
        // compensate
        int nR = ((this.nReal * cx.nReal + this.nImag * cx.nImag) / nSq) >> nShift;
        int nI = ((this.nImag * cx.nReal - this.nReal * cx.nImag) / nSq) >> nShift;
        this.nReal = nR;
        this.nImag = nI;
        return this;
    }
    
    /**
     * Equality test.
     * @param cx the Complex number to compare with.
     * @return true iff the two Complex numbers are equal.
     */
    public boolean equals(Complex cx) {
        return this.nReal == cx.nReal && this.nImag == cx.nImag;
    }
    
    /**
     * The imaginary component of the complex number.
     * @return the imaginary component.
     */
    public int imag() {
        return this.nImag;
    }

    /**
     * Shifts a complex number left a certain number of bits.
     * @param n The number of bits to shift by.
     * @return the result of shifting the complex number left the number of bits.
     */
    public Complex lsh(int n) {
        this.nReal <<= n;
        this.nImag <<= n;
        return this;
    }

    /**
     * Complex magnitude.
     * @return the magnitude of this number, i.e., sqrt(real**2 + imag**2)
     * @throws jjil.core.Error if the square value computed is too large.
     */
    public int magnitude() throws jjil.core.Error {
        // special case when one component is 0
        if (this.nReal == 0 || this.nImag == 0) {
            return Math.abs(this.nReal) + Math.abs(this.nImag);
        }
        // try to extend the range of numbers we can take the magnitude of beyond
        // 2**16
        if (Math.abs(this.nReal) > (MathPlus.SCALE >> 1) || 
            Math.abs(this.nImag) > (MathPlus.SCALE >> 1)) {
            // squaring the number will result in overflow
            // so we shift right first instead
            int nR = this.nReal >> MathPlus.SHIFT;
            int nI = this.nImag >> MathPlus.SHIFT;
            return MathPlus.sqrt(nR * nR + nI * nI) << MathPlus.SHIFT;
        } else {
            return MathPlus.sqrt(square());           
        }
    }
    
     /**
     * Subtracts one complex number from another.
     * @param cx the complex number to subtract.
     * @return the difference of the two complex numbers.
     */
    public Complex minus(Complex cx) {
        this.nReal -= cx.nReal;
        this.nImag -= cx.nImag;
        return this;
    }

    /**
     * Adds two complex numbers.
     * @param cx the complex number to add.
     * @return the sum of the two complex numbers.
     */
    public Complex plus(Complex cx) {
        this.nReal += cx.nReal;
        this.nImag += cx.nImag;
        return this;
    }

    /**
     * The real component of the complex number.
     * @return the real component of the complex number.
     */
    public int real() {
        return this.nReal;
    }

     /**
     * Shifts a complex number right a certain number of bits.
     * @param n The number of bits to shift by.
     * @return the result of shifting the complex number the number of bits.
     */
    public Complex rsh(int n) {
        this.nReal >>= n;
        this.nImag >>= n;
        return this;
    }

    /**
     * Computes the absolute square.
     * @return The absolute square, i.e, real**2 + imag**2.
     * @throws jjil.core.Error if Complex value is too large.
     */
    public int square() throws jjil.core.Error {
        if (Math.abs(this.nReal) > MathPlus.SCALE ||
            Math.abs(this.nImag) > MathPlus.SCALE) {
            throw new Error(
                            Error.PACKAGE.CORE,
                            ErrorCodes.MATH_SQUARE_TOO_LARGE,
                            this.toString(),
                            null,
                            null);
        }
        return this.nReal * this.nReal + this.nImag * this.nImag;
    }
    
    /**
     * Multiplies two complex numbers.
         * @param cx The complex number to multiply by.
     * @return The product of the two numbers.
     */
    public Complex times(Complex cx) {
        int nR = this.nReal * cx.nReal - this.nImag * cx.nImag;
        int nI = this.nReal * cx.nImag + this.nImag * cx.nReal;
        this.nReal = nR;
        this.nImag = nI;
        return this;
    }

    /**
     * Multiplies a complex number by a real number.
         * @param nX The complex number to multiply by.
     * @return The product of the two numbers.
     */
    public Complex times(int nX) {
        int nR = this.nReal * nX;
        int nI = this.nReal * nX;
        this.nReal = nR;
        this.nImag = nI;
        return this;
    }

    /**
     * Returns a String representation of the complex number
     * @return the string (real, imag)
     */
    public String toString() {
        return "(" + this.nReal + ", " + this.nImag + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
