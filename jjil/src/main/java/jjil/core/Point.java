/*
 * Point.java
 *
 * Created on September 9, 2006, 1:46 PM
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

import java.io.Serializable;


/**
 * Point: an object holding a 2-dimensional point coordinate
 * @author webb
 */
public class Point implements Serializable {
    private int mnX;
    private int mnY;
    
    /** Creates a new instance of Point 
     *
     * @param mnX the Point's x position (column)
     * @param mnY the Point's y position (row)
     */
    public Point(int nX, int nY) {
        this.mnX = nX;
        this.mnY = nY;
    }
    
    /**
     * Offset a point by a 2-dimensional vector Vec2, returning modified point.
     * @param v Vec2 to offset this point by
     * @return modified Point
     */
    public Point add(Vec2 v) {
        this.mnX += v.getX();
        this.mnY += v.getY();
        return this;
    }
    
    /**
     * Make a copy of this Point, so that modifications by other
     * operations don't affect the original.
     */
    public Point clone() {
        return new Point(this.mnX, this.mnY);
    }

    /**
     * Compute a vector from another Point to this
     * @param pos starting point
     * @return a Vec2 which, when added to pos, will give this
     */
    public Vec2 diff(Point pos) {
        return new Vec2(this.mnX-pos.mnX, this.mnY-pos.mnY);
    }
    
    /**
     * Returns true iff this Point equals the first parameter.
     */
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        Point p = (Point) o;
        return this.mnX == p.mnX && this.mnY == p.mnY;
    }

    /**
     * Return the point's x-coordinate.
     * @return the horizontal position of the point.
     */
    public int getX() {
        return this.mnX;
    } 
    
    /**
     * Return the point's y-coordinate.
     * @return the vertical position of the point.
     */
    public int getY() {
        return this.mnY;
    }
    
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.mnX;
        hash = 67 * hash + this.mnY;
        return hash;
    }

    /**
     * Offset a point by a certain x,y
     * @param x x offset
     * @param y y offset
     * @return modified Point
     */
    public Point offset(int nX, int nY) {
        this.mnX += nX;
        this.mnY += nY;
        return this;
    }
    
    /**
     * Change the (x,y) coordinates of this Point
     * @param nX new X coordinate
     * @param nY new Y coordinate
     * @return the modified Point
     */
    public Point setXY(int nX, int nY) {
    	this.mnX = nX;
    	this.mnY = nY;
    	return this;
    }
    
    /**
     * Implement toString
     * @return Object address + (x,y)
     */
    public String toString() {
        return super.toString() + "(" +
                new Integer(this.mnX).toString() + "," +
                new Integer(this.mnY).toString() + ")";
    }
}
