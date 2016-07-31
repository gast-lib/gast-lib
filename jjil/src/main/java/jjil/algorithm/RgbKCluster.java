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

import java.util.Enumeration;
import java.util.Vector;
import jjil.core.RgbVal;

/**
 * Cluster a vector of RGB values using a simple means-based algorithm.
 * Copyright 2008 by Jon A. Webb
 * @author webb
 */
public class RgbKCluster {
    public static class RgbCluster {
        int nRedMean, nGreenMean, nBlueMean;
        int nPixels;
        
        public RgbCluster(int nRed, int nGreen, int nBlue, int nPixels) {
            this.nRedMean = nRed;
            this.nGreenMean = nGreen;
            this.nBlueMean = nBlue;
            this.nPixels = nPixels;
        }
        
        public RgbCluster add(RgbCluster c) {
            this.nRedMean = (this.nRedMean*this.nPixels + c.nRedMean*c.nPixels) 
                    / (this.nPixels + c.nPixels);
            this.nGreenMean = (this.nGreenMean*this.nPixels + c.nGreenMean*c.nPixels) 
                    / (this.nPixels + c.nPixels);
            this.nBlueMean = (this.nBlueMean*this.nPixels + c.nBlueMean*c.nPixels) 
                    / (this.nPixels + c.nPixels);
            return this;
        }
        
        public int getPixels() {
            return this.nPixels;
        }
        public int getDiff(RgbCluster c) {
            return Math.abs(this.nRedMean - c.nRedMean) +
                    Math.abs(this.nGreenMean - c.nGreenMean) +
                    Math.abs(this.nBlueMean - c.nBlueMean);
        }
        public int getRgb() {
            return RgbVal.toRgb(
                    (byte)Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, this.nRedMean)), 
                    (byte)Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, this.nGreenMean)), 
                    (byte)Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, this.nBlueMean)));
        }
    }
    
    private int nClusters;
    private int nTolerance;
    
    public RgbKCluster(int nClusters, int nTolerance) {
        this.nClusters = nClusters;
        this.nTolerance = nTolerance;
    }
    
    public Vector cluster(Vector vRgbClusters) {
        Vector vResult = new Vector();
        do {
            // find the largest cluster
            RgbCluster cLarge = null;
            for (Enumeration e = vRgbClusters.elements(); e.hasMoreElements();) {
                RgbCluster c = (RgbCluster) e.nextElement();
                if (cLarge == null || cLarge.getPixels() < c.getPixels()) {
                    cLarge = c;
                }
            }
            vRgbClusters.removeElement(cLarge);
            // group all the remaining clusters together with the largest cluster
            // if they fall within a tolerance
            Vector vRemaining = new Vector();
            for (Enumeration e = vRgbClusters.elements(); e.hasMoreElements();) {
                RgbCluster c = (RgbCluster) e.nextElement();
                if (cLarge.getDiff(c) < this.nTolerance) {
                    cLarge.add(c);
                } else {
                    vRemaining.addElement(c);
                }
            }
            vResult.addElement(cLarge);
            vRgbClusters = vRemaining;
        } while (vResult.size() < this.nClusters && vRgbClusters.size() > 0);
        return vResult;
    }
}
