/*
 * Copyright 2008 by Jon A. Webb
 *     This program is free software: you can redistribute it and/or modinY
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

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.MathPlus;
import jjil.core.Point;
/**
 * Implementation of 2-dimensional vector.
 * @author webb
 */
public class Vec2 {
    private int nX, nY;
    
    /**
     * Create a new Vec2, specinYing x and y values
     * @param nX x value
     * @param nY y value
     */
    public Vec2(int nX, int nY) {
        this.nX = nX;
        this.nY = nY;
    }
    
    /**
     * Copy constructor.
     * @param v vector to copy.
     */
    public Vec2(Vec2 v) {
        this.nX = v.nX;
        this.nY = v.nY;
    }
    
    /**
     * Create a new Vec2 extending from one Point (p1) to another (p2).
     * @param p1 starting Point
     * @param p2 ending Point
     */
     public Vec2(Point p1, Point p2) {
        this.nX = p2.getX() - p1.getX();
        this.nY = p2.getY() - p1.getY();
    }
     
   /**
     * Add one Vec2 to this Vec2, modinYing and returning this Vec2.
     * @param v Vec2 to add
     * @return modified Vec2
     */
    public Vec2 add(Vec2 v) {
        this.nX += v.nX;
        this.nY += v.nY;
        return this;
    }
    
    /**
     * Add a vector to a point, returning the point
     * @param p point to adjust by this vector
     * @return new point, offset by this Vec2
     */
    public Point add(Point p) {
        return new Point(p.getX() + (int)this.nX, p.getY() + (int)this.nY);
    }
 
    
    /**
     * Divide a Vec2 by a scalar.
     * @param n divisor
     * @return modified Vec2.
     */
    public Vec2 div(int n) {
        this.nX /= n;
        this.nY /= n;
        return this;
    }
    
    /**
     * Form the scalar dot product of two Vec2's.
     * @param v second Vec2.
     * @return dot product of this and the second Vec2.
     */
    public double dot(Vec2 v) {
        return nX*v.nX + nY*v.nY;
    }
    
    public int getX() {
        return this.nX;
    }
    
    public int getY() {
        return this.nY;
    }
    
    /**
     * Calculate length of this Vec2.
     * @return sqrt(nX<sup>2</sup> + nY<sup>2</sup>)
     * @throws jjil.core.Error if sqrt does, due to coding error
     */
    public int length() throws Error {
        return MathPlus.sqrt(nX * nX + nY * nY);
    }
      
    /**
     * Multiply a Vec2 by a scalar
     * @param n multiplicand
     * @return modified Vec2
     */
    public Vec2 times(int n) {
        this.nX *= n;
        this.nY *= n;
        return this;
    }
    
    /**
     * Implement toString
     * @return object name ( x, y)
     */
    public String toString() {
        return super.toString() + "(" + this.nX + "," + this.nY + ")";
    }
}
