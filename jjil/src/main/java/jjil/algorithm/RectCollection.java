/**
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
 *
 */

package jjil.algorithm;

import java.util.Enumeration;
import java.util.Vector;

import jjil.core.Error;
import jjil.core.Point;
import jjil.core.Rect;

/**
 * RectCollection includes a data structure and algorithms to efficiently represent
 * a collection of axis-aligned rectangles and allow the fast (O(log n)) 
 * determination of whether a given point is in one of them.
 * @author webb
 */
public class RectCollection {
    
    /**
     * Vector of all rectangles in the collection.
     */
    private Vector vAllRect = new Vector();
    
    /**
     * treeHoriz is a collection of all the rectangles, projected horizontally.
     * Each node in the tree contains the rectangles at that y-coordinate.
     * There is one y-coordinate for each unique top or bottom position in a
     * rectangle.
     */
    private ThreadedBinaryTree treeHoriz = null;
    /**
     * treeVert is a collection of all the rectangles, projected vertically.
     * Each node in the tree contains the rectangles at that x-coordinate.
     * There is one x-coordinate for each unique left and right position in a
     * rectangle
     */
    private ThreadedBinaryTree treeVert = null;
    
    /**
     * Default constructor.
     */
    public RectCollection() {
    }
    
    /**
     * Add a new rectangle to the collection. This includes adding its top and
     * bottom coordinates to the horizontal projection collection and its
     * left and right coordinates to the vertical projection collection, and
     * adding the rectangle itself to every rectangle list between its starting
     * and ending coordinates.
     * @param r the rectangle to add
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
     */
    public void add(Rect r)
    	throws jjil.core.Error
    {
        this.vAllRect.addElement(r);
        this.treeHoriz =
                addRect(r,
                r.getLeft(),
                r.getTop() + r.getHeight(),
                this.treeHoriz);
        this.treeVert =
                addRect(r,
                r.getLeft(),
                r.getTop() + r.getWidth(),
                this.treeVert);
    }
    
    /**
     * Add a rectangle to all rectangle lists between a starting and ending
     * coordinate. First we get pointers to the nodes in the trees for the
     * starting and ending coordinates. Then we add the rectangle to all the
     * lists in the inorder traversal from the start to the end node.
     * @param r the rectangle to add
     * @param start starting coordinate
     * @param end ending coordinate
     * @param tbtRoot the root of the ThreadedBinaryTree that we are modifying
     * @return the modified binary tree (= tbtRoot if it already exists, 
     * otherwise it will be created)
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
     */
    private ThreadedBinaryTree addRect(
            Rect r, 
            int start, 
            int end, 
            ThreadedBinaryTree tbtRoot)
    	throws jjil.core.Error
    {
        // get lists of Rectangles enclosing the given rectangle start and end
        ThreadedBinaryTree tbtFind = null;
        if (tbtRoot != null) {
            tbtFind = tbtRoot.findNearest(new BinaryHeap.ComparableInt(start));
        }
        Vector vExistingStart = null;
        if (tbtFind != null) {
            vExistingStart = (Vector) tbtFind.getValue();
        }
        Vector vExistingEnd = null;
        if (tbtRoot != null) {
            tbtFind = tbtRoot.findNearest(new BinaryHeap.ComparableInt(end));
        }
        if (tbtFind != null) {
            vExistingEnd = (Vector) tbtFind.getValue();
        }
        // now add the new points to the tree
        ThreadedBinaryTree tbtStart = null;
        // the ThreadedBinaryTree may not already exist. Create it if necessary
        if (tbtRoot == null) {
            tbtRoot = tbtStart = 
                    new ThreadedBinaryTree(new BinaryHeap.ComparableInt(start));
        } else {
            // already exists, add or get the start node
            tbtStart = tbtRoot.add(new BinaryHeap.ComparableInt(start));
            // add existing enclosing rectangles to this node's list
            // if it doesn't already exist
            if (vExistingStart != null) {
                if (tbtStart.getValue() == null) {
                    Vector v = new Vector();
                    for (Enumeration e = vExistingStart.elements();
                        e.hasMoreElements();) {
                        v.addElement(e.nextElement());
                    }
                    tbtStart.setValue(v);
                }
            }
        }
        // add or get the end node
        ThreadedBinaryTree tbtEnd = 
                tbtRoot.add(new BinaryHeap.ComparableInt(end));
        // add existing enclosing rectangles to this node's list
        // if it doesn't already exist
        if (vExistingEnd != null) {
            if (tbtEnd.getValue() == null) {
                Vector v = new Vector();
                for (Enumeration e = vExistingEnd.elements();
                    e.hasMoreElements();) {
                    v.addElement(e.nextElement());
                }
                tbtEnd.setValue(v);
            }
        }
        // now traverse the path from tbtStart to tbtEnd and add r to all 
        // rectangle lists
        Vector vTrav = 
                tbtStart.inorderTraverse(tbtEnd);
        // for eacn node in the inorder traversal, create the Vector of 
        // rectangles if necessary, and put this rectangle on it
        for (Enumeration e = 
                vTrav.elements(); e.hasMoreElements();) {
            ThreadedBinaryTree tbt = 
                    (ThreadedBinaryTree) e.nextElement();
            // Vector doesn't exist
            if (tbt.getValue() == null) {
                Vector v = new Vector();
                v.addElement(r);
                // set value
                tbt.setValue(v);
            } else {
                // already exists, modify value
                ((Vector) tbt.getValue()).addElement(r);
            }
        }
        // return modified / created tree
        return tbtRoot;
    }
    
    /**
     * Clear collection.
     */
    public void clear() {
    	this.vAllRect = new Vector();
    	this.treeHoriz = null;
    	this.treeVert = null;
    }
    
    /**
     * Determines whether a point is in the collection by intersecting the lists
     * of rectangles that the point projects into horizontally and vertically,
     * and then determining if the point lies in one of the rectangles in the
     * intersection.
     * @param p the point to test
     * @return a Rect that contains p if it is contained by any Rect, otherwise
     * null
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
     */
    public Rect contains(Point p) 
    	throws jjil.core.Error
    {
        // if no rectangles are in collection answer is null
        if (this.treeHoriz == null || this.treeVert == null) {
            return null;
        }
        ThreadedBinaryTree tbtHorizProj =
                this.treeHoriz.findNearest(new BinaryHeap.ComparableInt(p.getY()));
        ThreadedBinaryTree tbtVertProj =
                this.treeVert.findNearest(new BinaryHeap.ComparableInt(p.getX()));
        // if no tree node is <= this point the return null
        if (tbtHorizProj == null || tbtVertProj == null) {
            return null;
        }
        // check for intersection between two lists; if non-empty check
        // for contains
        Vector vHoriz = (Vector) tbtHorizProj.getValue();
        Vector vVert = (Vector) tbtVertProj.getValue();
        for (Enumeration e = vHoriz.elements(); e.hasMoreElements();) {
            Rect r = (Rect) e.nextElement();
            for (Enumeration f = vVert.elements(); f.hasMoreElements();) {
                Rect s = (Rect) f.nextElement();
                if (r == s) {
                    if (s.contains(p)) {
                        return s;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns an enumeration of all rectangles in the collection
     * @return Enumeration of all rectangles in order they were added to
     * the collection.
     */
    public Enumeration elements() {
        return this.vAllRect.elements();
    }
    
    /**
     * Implement toString
     */
    public String toString() {
    	String szRes = super.toString() + "(all=" + this.vAllRect.toString() + 
    		",horiz=";
    	if (this.treeHoriz != null) {
    		szRes += this.treeHoriz.toString();
    	} else {
    		szRes += "null";
    	}
    	szRes += ",vert=";;
    	if (this.treeVert != null) {
    		szRes += this.treeVert.toString();
    	} else {
    		szRes += "null";
    	}
    	szRes += ")";
    	return szRes;
    }
}
