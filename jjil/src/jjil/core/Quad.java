/*
 * Quad.java
 *
 * Created on December 10, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jjil.core;

/**
 * Quad represents a quadrilateral. The quadrilteral is specified using its
 * four corners.
 * @author webb
 */
public class Quad {
    Point p[];
    
    public Quad() {
        // default constructor used for overriding class
    }
       
    /**
     * Create a new Quad specifying the four corners.
     * @param p1 first corner
     * @param p2 second corner
     * @param p3 third corner
     * @param p4 fourth corner
     */
    public Quad(Point p1, Point p2, Point p3, Point p4) {
        this.p = new Point[4];
        this.p[0] = p1;
        this.p[1] = p2;
        this.p[2] = p3;
        this.p[3] = p4;
    }
    
    /**
     * Get bottom-most extent of quadrilateral
     * @return bottom-most corner y position
     */
    public int getBottom() {
        return Math.max(this.p[0].getY(), 
                Math.max(this.p[1].getY(),
                Math.max(this.p[2].getY(), 
                this.p[3].getY())));
    }
    
    /**
     * Return designated corner of quadrilateral
     * @param nCorner corner to return, from 0-3
     * @return designated corner of the quadrilateral
     * @throws jjil.core.Error if nCorner &lt; 0 or &gt; 3
     */
    public Point getCorner(int nCorner) throws Error {
        if (nCorner < 0 || nCorner > 3) {
            throw new Error(
                            Error.PACKAGE.CORE,
                            ErrorCodes.ILLEGAL_PARAMETER_VALUE,
                            new Integer(nCorner).toString(),
                            "0",
                            "3");
            
        }
        return this.p[nCorner];
    }
    
    /**
     * Return height of quadrilateral
     * @return height of quadrilateral
     */
    public int getHeight() {
        return this.getBottom() - this.getTop();
    }
    
    /**
     * Get leftmost extent of quadrilateral
     * @return leftmost corner x position
     */
    public int getLeft() {
        return Math.min(this.p[0].getX(), 
                Math.min(this.p[1].getX(),
                Math.min(this.p[2].getX(), 
                this.p[3].getX())));
    }
    
    /**
     * Get rightmost extent of quadrilateral
     * @return rightmost corner x position
     */
    public int getRight() {
        return Math.max(this.p[0].getX(), 
                Math.max(this.p[1].getX(),
                Math.max(this.p[2].getX(), 
                this.p[3].getX())));
    }

    /**
     * Get top-most extent of quadrilateral
     * @return top-most corner y position
     */
    public int getTop() {
        return Math.min(this.p[0].getY(), 
                Math.min(this.p[1].getY(),
                Math.min(this.p[2].getY(), 
                this.p[3].getY())));
    }
    
    /**
     * Returns maximum width of the quadrilateral
     * @return width of the quadrilateral
     */
    public int getWidth() {
        return this.getRight() - this.getLeft();
    }
    
    public Quad offset(int nX, int nY) {
        for (int i=0; i<4; i++) {
            this.p[i].offset(nX, nY);
        }
        return this;
    }
    
    /**
     * Implement toString
     * @return class name followed by (p1,p2,p3,p4)
     */
    public String toString() {
        return super.toString() + "(" + 
                this.p[0].toString() + "," +
                this.p[1].toString() + "," +
                this.p[2].toString() + "," +
                this.p[3].toString() + ")";
                
    }
}
