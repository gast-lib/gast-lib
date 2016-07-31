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

/** Manage three points in a triangle.
 * @author webb
 */
public class Triangle {
    private Point p1, p2, p3;
    private int l1, l2;
    private Vec2 v1, v2;
    
    /**
     * Create a new triangle, specifying the corners.
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.v1 = new Vec2(p1,p2);
        this.l1 = this.v1.dot(this.v1);
        this.v2 = new Vec2(p1,p3);
        this.l2 = this.v2.dot(this.v2);
    }
    
    /**
     * Returns true iff the triangle contains the given point
     * @param p Point to test
     * @return true iff p is in the interior of the triangle
     */
    public boolean contains(Point p) {
        Vec2 v = new Vec2(this.p1, p);
        return v.dot(v1) <= this.l1 && v.dot(this.v2) <= this.l2;
    }
    
    /**
     * Return first vertex
     * @return first vertex
     */
    public Point getP1() {
        return this.p1;
    }
    
    /**
     * Return second vertex
     * @return second vertex
     */
    public Point getP2() {
        return this.p2;
    }
     
    /**
     * Return third vertex
     * @return third vertex
     */
    public Point getP3() {
        return this.p3;
    }
    
    /**
     * Implement toString
     * @return String including the triangle vertices
     */
    public String toString() {
        return super.toString() + "(" +
                this.p1.toString() + "," +
                this.p2.toString() + "," +
                this.p3.toString() + ")";
    }
}
