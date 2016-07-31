/*
 * Gray8ConnComp.java
 *
 * Created on September 9, 2006, 10:25 AM
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
package jjil.algorithm;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import jjil.core.Error;
import jjil.core.Gray16Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.Point;
import jjil.core.Rect;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * Gray connected components. Input is a Gray8Image. Pixels with value
 * Byte.MIN_VALUE are taken to be background. Other connected pixels are labeled 
 * with unique labels. The connected component image can be retrieved, as can the
 * connected component bounding rectangles, sorted by area.
 * 
 * @author webb
 */
public class Gray8ConnComp extends PipelineStage {
   // class variables
    private boolean bComponents = false;
    private Gray16Image imLabeled = null;
    private int nSortedLabels = -1;
    private PriorityQueue pqLabels = null;
    Random random = new Random();
    private EquivalenceClass reClasses[];
    private int rnFinalLabels[];
    private int rnPerimeters[];
    private short sClasses = 0;
    private Label rSortedLabels[] = null;

    private class Label implements ComparableJ2me {
        private int nLabel = 0;
        private int nPixelCount = 0;
        private Rect rectBounding;

        public Label(Point p, int nLabel) {
            this.rectBounding = new Rect(p);
            this.nLabel = nLabel;
            this.nPixelCount = 1;
        }

        public void add(Point p) {
            this.rectBounding.add(p);
            this.nPixelCount++;
        }

        public int compareTo(Object o) throws jjil.core.Error {
            if (o == null) {
                throw new Error(
                        Error.PACKAGE.ALGORITHM,
                        ErrorCodes.CONN_COMP_LABEL_COMPARETO_NULL,
                        null,
                        null,
                        null);
            }
            if (!(o instanceof Label)) {
                throw new Error(
                        Error.PACKAGE.ALGORITHM,
                        ErrorCodes.OBJECT_NOT_EXPECTED_TYPE,
                        o.toString(),
                        "Label",
                        null);
            }
            Label l = (Label) o;
            if (l.nPixelCount == this.nPixelCount) {
                return 0;
            }
            return (l.nPixelCount < this.nPixelCount) ? -1 : 1;
        }
        
        public int getLabel() {
            return this.nLabel;
        }

        public int getPixelCount() {
            return this.nPixelCount;
        }

        public Rect getRect() {
            return this.rectBounding;
        }
    } 
    /**
     * Creates a new instance of Gray8ConnComp.
     * 
     */
    public Gray8ConnComp() {
    }

    /**
     * Calculate the perimeter of all the components in the labeled image.
     */
    private void calculatePerimeters() {
        if (this.rnPerimeters != null) {
            return;
        }
        this.rnPerimeters = new int[EquivalenceClass.getLabels()];
        for (int i = 0; i < this.rnPerimeters.length; i++) {
            this.rnPerimeters[i] = 0;
        }
        short[] sData = this.imLabeled.getData();
        for (int i = 0; i < this.imLabeled.getHeight(); i++) {
            for (int j = 0; j < this.imLabeled.getWidth(); j++) {
                short sCurr = sData[i * this.imLabeled.getWidth() + j];
                short sUp = (i > 0) ? sData[(i - 1) * this.imLabeled.getWidth() + j] : 0;
                short sLeft = (j > 0) ? sData[i * this.imLabeled.getWidth() + j - 1] : 0;
                short sRight = (j < this.imLabeled.getWidth() - 1) ? sData[i * this.imLabeled.getWidth() + j + 1] : 0;
                short sDown = (i < this.imLabeled.getHeight() - 1) ? sData[(i + 1) * this.imLabeled.getWidth() + j] : 0;
                if (sCurr != sUp) {
                    this.rnPerimeters[sCurr]++;
                }
                if (sCurr != sLeft) {
                    this.rnPerimeters[sCurr]++;
                }
                if (sCurr != sRight) {
                    this.rnPerimeters[sCurr]++;
                }
                if (sCurr != sDown) {
                    this.rnPerimeters[sCurr]++;
                }
            }
        }
    }

    /**
     * Returns the nComponent'th bounding rectangle in order by size. Sorts only
     * as many components as are necessary to reach the requested component.
     * Does this by observing the state of rSortedLabels. If it is null, it has
     * to be allocated. If the nComponent'th element is null, more need to be
     * copied from pqLabels. This is done by copying and deleting the minimum
     * element until we reach the requested component.
     * @return the nComponent'th bounding rectangle, ordered by pixel count,
     *         largest first
     * @param nComponent the number of the component to return.
     * @throws jjil.core.Error if nComponent is greater than the number of components available.
     */
    public Rect getComponent(int nComponent) throws jjil.core.Error {
        // see if we've created the sorted labels array
        // allocate it if we haven't.
        // If the components haven't been computed getComponentCount()
        // will compute them.
        if (this.rSortedLabels == null) {
            this.rSortedLabels = new Label[getComponentCount()];
            this.nSortedLabels = -1;
        }
        // see if the requested component is out of bounds
        if (nComponent >= this.rSortedLabels.length) {
            throw new Error(
                    Error.PACKAGE.ALGORITHM,
                    ErrorCodes.CONN_COMP_LABEL_OUT_OF_BOUNDS,
                    new Integer(nComponent).toString(),
                    this.rSortedLabels.toString(),
                    null);
        }
        // now see if we've figured out what the nComponent'th
        // component is. If not compute it by finding and
        // deleting min until we reach it.
        if (this.nSortedLabels < nComponent) {
            while (this.nSortedLabels < nComponent) {
                this.rSortedLabels[++this.nSortedLabels] =
                        (Label) this.pqLabels.findMin();
                this.pqLabels.deleteMin();
            }
        }
        return rSortedLabels[nComponent].getRect();
    }

    /**
     * Get the number of connected components in the labeled image. Computes the
     * components from the labeled image if necessary.
     * @return the number of connected components.
     * @throws jjil.core.Error if BinaryHeap returns jjil.core.Error due to coding error.
     */
    public int getComponentCount() throws jjil.core.Error {
        // see if we've already calculated the components
        if (this.bComponents) {
            return this.pqLabels.size();
        }
        // no, we need to calculate it.
        // determine the pixel count and bounding rectangle
        // of all the components in the image
        short sData[] = this.imLabeled.getData();
        Label vLabels[] = new Label[this.sClasses+1];
        int nComponents = 0;
        for (int i = 0; i < this.imLabeled.getHeight(); i++) {
            int nRow = i * this.imLabeled.getWidth();
            for (int j = 0; j < this.imLabeled.getWidth(); j++) {
                if (sData[nRow + j] != 0) {
                    int nLabel = sData[nRow + j];
                    // has this label been seen before?
                    if (vLabels[nLabel] == null) {
                        // no, create a new label
                        vLabels[nLabel] = new Label(new Point(j, i), nLabel);
                        nComponents++;
                    } else {
                        // yes, extend its bounding rectangle
                        vLabels[nLabel].add(new Point(j, i));
                    }
                }
            }
        }
        // set up priority queue
        // first create a new array of the labels
        // with all the null elements eliminated
        Label vCompressLabels[] = new Label[nComponents];
        int j = 0;
        for (int i = 0; i < vLabels.length; i++) {
            if (vLabels[i] != null) {
                vCompressLabels[j++] = vLabels[i];
            }
        }
        // now create the priority queue from the array
        this.pqLabels = new BinaryHeap(vCompressLabels);
        // clear the sorted labels array, it has to be recomputed.
        this.rSortedLabels = null;
        // we're done
        this.bComponents = true;
        return this.pqLabels.size();
    }
    
    public int getComponentLabel(int n) {
        return this.rSortedLabels[n].getLabel();
    }

    public Enumeration getComponentPixels(int n) throws Error {
        Rect r = getComponent(n);
        // build a Vector of all points in the component
        Vector vPoints = new Vector();
        short[] sData = this.imLabeled.getData();
        int nLabel = this.rSortedLabels[n].nLabel;
        for (int i=r.getTop(); i<=r.getBottom(); i++) {
            for (int j=r.getLeft(); j<=r.getRight(); j++) {
                if (sData[i*this.imLabeled.getWidth()+j] == nLabel) {
                    vPoints.addElement(new Point(j, i));
                }
            }
        }
        return vPoints.elements();
    }

    /**
     * Override getFront. This is necessary because we don't actually compute
     * the output image when we are computing the components. The output is
     * an RgbImage with colors randomly assigned to the components. It is 
     * intended to be used for debugging, to make it easy to see how the
     * components of the image are connected.
     * @return an RgbImage with colors randomly assigned to the components
     * @throws jjil.core.Error if no components were found in the input image
     */
    public Image getFront() throws Error {
        if (this.getComponentCount() == 0 ||
                this.imLabeled == null) {
            throw new Error(
                    Error.PACKAGE.CORE,
                    jjil.core.ErrorCodes.NO_RESULT_AVAILABLE,
                    null,
                    null,
                    null);

        } else {
            RgbImage rgbOutput = new RgbImage(
                    this.imLabeled.getWidth(),
                    this.imLabeled.getHeight());
            int[] rgbData = rgbOutput.getData();
            int nMaxLabel = EquivalenceClass.getLabels();
            short[] grayData = this.imLabeled.getData();
            int[] rgbLabels = new int[nMaxLabel + 1];
            for (int i = 0; i < rgbLabels.length; i++) {
                rgbLabels[i] = RgbVal.toRgb(
                        (byte) ((this.random.nextInt() & 0xff) + Byte.MIN_VALUE),
                        (byte) ((this.random.nextInt() & 0xff) + Byte.MIN_VALUE),
                        (byte) ((this.random.nextInt() & 0xff) + Byte.MIN_VALUE));
            }
            for (int i = 0; i < rgbData.length; i++) {
                try {
                    rgbData[i] = rgbLabels[grayData[i]];
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return rgbOutput;
        }
    }
    
    /**
     * Get the labeled image.
     * 
     * @return a Gray16Image with final labels assigned to every pixel
     */
    public Gray16Image getLabeledImage() {
        return this.imLabeled;
    }

    /**
     * Returns the perimeter of the n'th largest component
     * @param n number of component to return (not its label)
     * @return perimeter of the component
     * @throws jjil.core.Error if the requested component doesn't exist
     */
    public int getPerimeter(int n) throws Error {
        // first calculate all the perimeters
        this.calculatePerimeters();
        // next make sure we've figured out what the n'th largest
        // component is
        this.getComponent(n);
        // get the label of that ocmponent
        int nLabel = this.rSortedLabels[n].getLabel();
        // look up the perimeter of that component
        return this.rnPerimeters[nLabel];
    }

    
    /**
     * Returns the pixel count of the n'th largest component
     * @param n number of component to return (not label)
     * @return number of pixels in the component (not bounding rectangle area)
     * @throws jjil.core.Error if the call to getComponent() does, say if
     * there aren't that many components
     */
    public int getPixelCount(int n) throws Error {
        // first make sure the n'th component is figured out
        getComponent(n); // retult discarded, used for side effect
        return this.rSortedLabels[n].getPixelCount();
    }
    
    public boolean isEmpty() {
        return this.imLabeled == null;
    }

    /**
     * Compute connected components of input gray image using a union-find
     * algorithm.
     * 
     * @param image
     *            the input image.
     * @throws jjil.core.Error
     *             if the image is not a gray 8-bit image.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray8Image)) {
            throw new Error(
                    Error.PACKAGE.ALGORITHM,
                    ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                    image.toString(),
                    null,
                    null);
        }
        // initialize the label lookup array
        EquivalenceClass.reset();
        this.reClasses = new EquivalenceClass[image.getWidth() * image.getHeight()];

        // note that we've not computed the final labels or
        // the sorted components yet
        this.bComponents = false;
        this.rnPerimeters = null;

        Gray8Image gray = (Gray8Image) image;
        byte[] bData = gray.getData();
        // for each pixel in the input image assign a label,
        // performing equivalence operations when two labels
        // are adjacent (8-connected)
        for (int i = 0; i < gray.getHeight(); i++) {
            int nRow = i * gray.getWidth();
            // we use sUpLeft to refer to the pixel up and to
            // the left of the current, etc.
            EquivalenceClass eUpLeft = null,eUp  = null,eUpRight  = null;
            // after first row, initialize pixels above and
            // to the right
            if (i > 0) {
                eUp = reClasses[nRow - gray.getWidth()];
                eUpRight = reClasses[nRow - gray.getWidth() + 1];
            }
            // starting a new row the pixel to the left is 0
            EquivalenceClass eLeft = null;
            // nBitPatt encodes the state of the pixels around the
            // current pixel. The pattern is
            // 8 4 2
            // 1 current
            int nBitPatt = ((eUp != null) ? 4 : 0) + ((eUpRight != null) ? 2 : 0);
            // (at left column eLeft and eUpLeft will always be 0)
            for (int j = 0; j < gray.getWidth(); j++) {
                if (bData[nRow + j] != Byte.MIN_VALUE) {
                    switch (nBitPatt) {
                        // the cases below are derived from the bit
                        // pattern illustrated above. The general
                        // rule is to choose the most recently-scanned
                        // label when copying a label. Of course, we
                        // also do unions only as necessary
                        case 0:
                            // 0 0 0
                            // 0 X
                            reClasses[nRow + j] =
                                    new EquivalenceClass();
                            this.sClasses++;
                            break;
                        case 1:
                            // 0 0 0
                            // X X
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 2:
                            // 0 0 X
                            // 0 X
                            reClasses[nRow + j] = eUpRight.find();
                            break;
                        case 3:
                            // 0 0 X
                            // X X
                            eLeft.union(eUpRight);
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 4:
                            // 0 X 0
                            // 0 X
                            reClasses[nRow + j] = eUp.find();
                            break;
                        case 5:
                            // 0 X 0
                            // X X
                            // we must already have union'ed
                            // eLeft and eUp
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 6:
                            // 0 X X
                            // 0 X
                            // we must already have union'ed
                            // eUp and eUpRight
                            reClasses[nRow + j] = eUpRight.find();
                            break;
                        case 7:
                            // 0 X X
                            // X X
                            // we must already have union'ed
                            // eLeft and eUp, and eUp and eUpRight
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 8:
                            // X 0 0
                            // 0 X
                            reClasses[nRow + j] = eUpLeft.find();
                            break;
                        case 9:
                            // X 0 0
                            // X X
                            // we must already have union'ed
                            // eLeft and eUpLeft
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 10:
                            // X 0 X
                            // 0 X
                            eUpLeft.union(eUpRight);
                            reClasses[nRow + j] = eUpLeft.find();
                            break;
                        case 11:
                            // X 0 X
                            // X X
                            // we must already have union'ed
                            // eLeft and eUpLeft
                            eLeft.union(eUpRight);
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 12:
                            // X X 0
                            // 0 X
                            // we must already have union'ed
                            // eUpLeft and eUp
                            reClasses[nRow + j] = eUp.find();
                            break;
                        case 13:
                            // X X 0
                            // X X
                            // we must already have union'ed
                            // eLeft and eUpLeft, and eUpLeft and eUp
                            reClasses[nRow + j] = eLeft.find();
                            break;
                        case 14:
                            // X X X
                            // 0 X
                            // we must already have union'ed
                            // eUpLeft, eUp, and eUpRight
                            reClasses[nRow + j] = eUpRight.find();
                            break;
                        case 15:
                            // X X X
                            // X X
                            // we must already have union'ed
                            // eLeft, eUpLeft, eUp, and eUpRight
                            reClasses[nRow + j] = eLeft.find();
                            break;
                    }
                }
                // shift right to next pixel
                eUpLeft = eUp;
                eUp = eUpRight;
                eLeft = reClasses[nRow + j];
                // if we're not at the right column and after the first
                // row read a new right pixel
                if (i > 0 && j < gray.getWidth() - 1) {
                    eUpRight = reClasses[nRow - gray.getWidth() + j + 2];
                } else {
                    eUpRight = null;
                }

                // compute the new bit pattern. This is the old pattern
                // with eUpLeft and eLeft and'ed off (& 6), shifted left,
                // with the new eLeft and eUpRight or'ed in
                nBitPatt = ((nBitPatt & 6) << 1) + ((eLeft != null) ? 1 : 0) + 
                        ((eUpRight != null) ? 2 : 0);
            }
        }
        // initialize the labeled image
        this.imLabeled = new Gray16Image(gray.getWidth(), gray.getHeight(),
                (short) 0);
        short[] sLabels = this.imLabeled.getData();
        // assign label pixels their final values
        for (int i = 0; i < sLabels.length; i++) {
            if (reClasses[i] != null) {
                sLabels[i] = (short) reClasses[i].getLabel();
            }
        }
        // free memory for reClasses
        this.reClasses = null;
    }
}
