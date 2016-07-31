/*
 * Copyright 2008 by Jon A. Webb
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
 */

package jjil.core;

import java.io.Serializable;

/**
 * Implementation of 2-dimensional vector.
 * @author webb
 */
public class Vec2 implements Serializable {
    private int mnX, mnY;

    public Vec2(Point Last) {
        this.mnX = Last.getX();
        this.mnY = Last.getY();
    }
    
    /**
     * Create a new Vec2, specifying x and y values
     * @param nX x value
     * @param nY y value
     */
    public Vec2(int nX, int nY) {
        this.mnX = nX;
        this.mnY = nY;
    }
    
    /**
     * Copy constructor.
     * @param v vector to copy.
     */
    public Vec2(Vec2 v) {
        this.mnX = v.mnX;
        this.mnY = v.mnY;
    }
    
    /**
     * Create a new Vec2 extending from one Point (p1) to another (p2).
     * @param p1 starting Point
     * @param p2 ending Point
     */
     public Vec2(Point p1, Point p2) {
        this.mnX = p2.getX() - p1.getX();
        this.mnY = p2.getY() - p1.getY();
    }
     
   /**
     * Add one Vec2 to this Vec2, modifying and returning this Vec2.
     * @param v Vec2 to add
     * @return modified Vec2
     */
    public Vec2 add(Vec2 v) {
        this.mnX += v.mnX;
        this.mnY += v.mnY;
        return this;
    }
    
    /**
     * Add a vector to a point, returning the point
     * @param p point to adjust by this vector
     * @return new point, offset by this Vec2
     */
    public Vec2 add(Point p) {
        this.mnX += p.getX();
        this.mnY += p.getY();
        return this;
    }
 
    /**
     * Add (x,y) to a vector
     * @param nX x value to add
     * @param nY y value to add
     * @return modified vector
     */
    public Vec2 add(int nX, int nY) {
    	this.mnX += nX;
    	this.mnY += nY;
    	return this;
    }
    
    public Vec2 clone() {
        return new Vec2(this.mnX, this.mnY);
    }
    
    public int crossMag(Vec2 v) {
        return this.mnX * v.mnY - this.mnY * v.mnX;
    }
    
    /**
     * Divide a Vec2 by a scalar.
     * @param n divisor
     * @return modified Vec2.
     */
    public Vec2 div(int n) {
        this.mnX /= n;
        this.mnY /= n;
        return this;
    }
    
    /**
     * Form the scalar dot product of two Vec2's.
     * @param v second Vec2.
     * @return dot product of this and the second Vec2.
     */
    public int dot(Vec2 v) {
        return mnX*v.mnX + mnY*v.mnY;
    }
    
    /**
     * Returns true iff this vector equals the argument
     * @param o Vec2 object to compare
     * @return true iff the two vectors are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof Vec2)) {
            return false;
        }
        Vec2 v = (Vec2) o;
        return this.mnX == v.mnX && this.mnY == v.mnY;
    }

    /**
     * Get X component of the vector
     * @return x component
     */
    public int getX() {
        return this.mnX;
    }
    
    /**
     * Get Y component of the vector
     * @return y component
     */
    public int getY() {
        return this.mnY;
    }
    
    /**
     * Implement hashcode
     * @return code which can be used in hash tables
     */
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.mnX;
        hash = 37 * hash + this.mnY;
        return hash;
    }
    
    /**
     * Calculate length of this Vec2.
     * @return sqrt(mnX<sup>2</sup> + mnY<sup>2</sup>)
     * @throws jjil.core.Error if sqrt does, due to coding error
     */
    public int length() throws Error {
        return MathPlus.sqrt(mnX * mnX + mnY * mnY);
    }
    
    public int lengthSqr() {
        return mnX * mnX + mnY * mnY;
    }
      
    public Vec2 lsh(int n) {
        this.mnX <<= n;
        this.mnY <<= n;
        return this;
    }
    
    public Vec2 max(Vec2 v) {
        this.mnX = Math.max(this.mnX, v.mnX);
        this.mnY = Math.max(this.mnY, v.mnY);
        return this;
    }
    
    public Vec2 min(Vec2 v) {
        this.mnX = Math.min(this.mnX, v.mnX);
        this.mnY = Math.min(this.mnY, v.mnY);
        return this;
    }
    
    public Vec2 rlsh(int n) {
        this.mnX >>>= n;
        this.mnY >>>= n;
        return this;
    }
    
    public Vec2 rsh(int n) {
        this.mnX >>= n;
        this.mnY >>= n;
        return this;
    }
    
    public Vec2 setXY(int nX, int nY) {
    	this.mnX = nX;
    	this.mnY = nY;
    	return this;
    }

    public Vec2 sub(int x, int y) {
        this.mnX -= x;
        this.mnY -= y;
        return this;
    }
    
    public Vec2 sub(Vec2 v) {
        this.mnX -= v.mnX;
        this.mnY -= v.mnY;
        return this;
    }
    
    /**
     * Multiply a Vec2 by a scalar
     * @param n multiplicand
     * @return modified Vec2
     */
    public Vec2 times(int n) {
        this.mnX *= n;
        this.mnY *= n;
        return this;
    }
    
    public Vec2 times(Vec2 v) {
        this.mnX *= v.mnX;
        this.mnY *= v.mnY;
        return v;
    }
}
