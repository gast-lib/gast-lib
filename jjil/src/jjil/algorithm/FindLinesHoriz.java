/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jjil.algorithm;

import java.util.Vector;
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Point;

/** Find the most likely roughly horizontally-oriented line in a Gray8Image.
 * The input image is assumed to be thresholded so any points not equal to
 * Byte.MIN_VALUE can be included in the line.
 * Copyright 2008 by Jon A. Webb
 * @author webb
 */
public class FindLinesHoriz {
    LinefitHoughHoriz hough;
    
    public FindLinesHoriz(int cMinY, 
            int cMaxY, 
            int cMinSlope, 
            int cMaxSlope, 
            int cSteps) throws Error {
            this.hough = new LinefitHoughHoriz(cMinY, 
                cMaxY, 
                cMinSlope, 
                cMaxSlope, 
                cSteps);
    }
    
    public void push(Gray8Image im) throws Error {
        byte[] bData = im.getData();
        Vector points = new Vector();
        for (int i=0; i<im.getHeight(); i++) {
            for (int j=0; j<im.getWidth(); j++) {
                if (bData[i*im.getWidth()+j] != Byte.MIN_VALUE) {
                    points.addElement(new Point(i,j));
                }
            }
        }
        this.hough.push(points);
    }
    
    public int getCount() {
        return this.hough.getCount();
    }
    
    public int getSlope() {
        return this.hough.getSlope();
    }
    
    public int getY() {
        return this.hough.getY();
    }
}
