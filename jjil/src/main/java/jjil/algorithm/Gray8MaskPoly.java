/*
 * Copyright 2008 by Jon pA. Webb
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR pA PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the Lesser GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Gray8MaskedImage;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.Point;
import jjil.core.Quad;
import jjil.core.Rect;

/**
 * Masks a polygon in an Gray8Image, setting the area inside
 * or outside the polygon to masked, depending on a parameter.
 * 
 * @author webb
 */
public class Gray8MaskPoly extends PipelineStage {

    private boolean bInside;
    private Point points[];
    private BinaryHeap hx[];
    private int rnX[][];
    
    /**
     * Initialize class, setting polygon to mask and
     * inside/outside choice.
     * @param points list of points to mask, in clockwise order (interior
     * on right).
     * @param bInside if true then area inside the polygon
     * is masked; if false, the area outside.
     */
    public Gray8MaskPoly(Point points[], boolean bInside) {
        this.points = points;
        this.bInside = bInside;
    }
    
    /**
     * Mask a quadrilateral.
     * @param quad quadrilateral to mask
     * @param bInside if true interior of quadrilateral is masked, if false
     * exterior
     * @throws jjil.core.Error if Error thrown from getCorner.
     */
    public Gray8MaskPoly(Quad quad, boolean bInside) throws Error {
        this.points = new Point[4];
        this.points[0] = quad.getCorner(0);
        this.points[1] = quad.getCorner(1);
        this.points[2] = quad.getCorner(2);
        this.points[3] = quad.getCorner(3);
        this.bInside = bInside;
    }
    
    /**
     * Mask a rectangle.
     * @param rect rectangle to mask
     * @param bInside if true, interior is masked; if false, exterior.
     */
    public Gray8MaskPoly(Rect rect, boolean bInside) {
        this.points = new Point[4];
        this.points[0] = new Point(rect.getLeft(), rect.getTop());
        this.points[1] = new Point(rect.getRight(), rect.getTop());
        this.points[2] = new Point(rect.getRight(), rect.getBottom());
        this.points[3] = new Point(rect.getLeft(), rect.getBottom());
        this.bInside = bInside;
    }
    
    
    /**
     * Build a vector of intersection points of the polygon for every
     * row in the output image.
     * @param nWidth output image width
     * @param nHeight output image height
     * @throws jjil.core.Error if the BinaryHeap code does.
     */
    private void buildVector(int nWidth, int nHeight) throws Error {
        this.hx = new BinaryHeap[nHeight];
        for (int i=0; i<nHeight; i++) {
            this.hx[i] = new BinaryHeap();
        }
        for (int i=0; i<this.points.length-1; i++) {
            drawLine(this.points[i],this.points[i+1]);
        }
        drawLine(this.points[this.points.length-1],this.points[0]);

        this.rnX = new int[nHeight][];
        for (int i=0; i<nHeight; i++) {
            int j = 0;
            if (!this.bInside) {
                    this.rnX[i] = new int[this.hx[i].size()+2];
                    this.rnX[i][0] = 0;
                    j = 1;
                
            }
            if (this.hx[i].size() > 0) {
                if (this.bInside) {
                    this.rnX[i] = new int[this.hx[i].size()];
                }
                while (!this.hx[i].isEmpty()) {
                    this.rnX[i][j++] = ((BinaryHeap.ComparableInt)
                            this.hx[i].deleteMin()).intValue();
                }
            }
            if (!this.bInside) {
                this.rnX[i][j] = nWidth;
            }
        }
    }
    
    /**
     * Starting at p1 and ending at p2, add all the intersection points for
     * a scanline to the array hx.
     * @param p1 starting point
     * @param p2 ending point
     * @throws jjil.core.Error if the BinaryHeap code does
     */
    private void drawLine(Point p1, Point p2) throws Error {
        if (p1.getY()>p2.getY()) {
            Point pSwap = p1;
            p1 = p2;
            p2 = pSwap;
        }
        int yDiff = p2.getY()-p1.getY();
        for (int y = 0; y<yDiff; y++) {
            int x = p2.getX() + (p1.getX()-p2.getX())*(yDiff-y)/yDiff;
            this.hx[y+p1.getY()].insert(new BinaryHeap.ComparableInt(x));
        }
    }

    /**
     * Mask the polygon in the input image. The algorithm used first
     * finds all intersections of the polygon with each row in the
     * output image and builds an array containing the x locations of those
     * intersections in sorted order. Then it scans the output image, setting
     * the mask value, for each row, starting and ending at each even-numbered
     * pair of intersection points.</br>
     * This code was strongly influenced by Darel Rex Finley's code described
     * at <a href="http://alienryderflex.com/polygon_fill/">
     * http://alienryderflex.com/polygon_fill/.</a>
     * @param imageInput input RgbImage
     * @throws jjil.core.Error if input is not RgbImage or the BinaryHeap
     * code used to do the sorting throws.
     */
    public void push(Image imageInput) throws Error {
        if (!(imageInput instanceof Gray8Image)) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.IMAGE_NOT_RGBIMAGE,
                            imageInput.toString(),
                            null,
                            null);
        }
        Gray8Image grayInput = (Gray8Image) imageInput;
        Gray8MaskedImage grayOutput = new Gray8MaskedImage(grayInput);
        buildVector(grayInput.getWidth(), grayInput.getHeight());
        for (int i=0; i<grayInput.getHeight(); i++) {
            if (this.rnX[i] != null) {
                for (int j=0; j<this.rnX[i].length; j+= 2) {
                    for (int k = this.rnX[i][j]; k<this.rnX[i][j+1]; k++) {
                        grayOutput.setMask(i, k);
                    }
                }
            }
        }
        super.setOutput(grayOutput);
    }
}
