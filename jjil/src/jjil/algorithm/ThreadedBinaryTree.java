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

import java.util.Vector;

/**
 * ThreadedBinaryTree is an implements threaded
 * binary trees, as the name implies. The implementation of
 * threaded binary trees here was guided by the tutorial at <a 
 * href="http://www.eternallyconfuzzled.com/tuts/datastructures/jsw_tut_bst1.aspx#thread">
 * http://www.eternallyconfuzzled.com/tuts/datastructures/jsw_tut_bst1.aspx#thread.
 * </a>
 * <p>
 * Threaded binary trees are like normal (unbalanced) binary
 * trees except that nodes which have no right successor have
 * a pointer to their inorder inserted in the right pointer.
 * This makes it possible to perform an inorder traversal
 * starting at any node without doing a full traversal of
 * the tree.
 * <p>
 * The use of threaded binary trees in JJIL is to make it possible
 * to add() a new element to the tree in average log n time
 * and then to traverse from any node to any other node in
 * time equal to the number of nodes between the two nodes.
 * If I'd used a normal binary tree to do the traversal the
 * time would have been equal to the number of nodes in the
 * tree. Both are O(n) but the number of nodes between the two
 * nodes being traversed should be a fraction of the total number
 * of nodes in the tree.
 * @author webb
 *
 */
public class ThreadedBinaryTree {

    /**
     * boolThread identifies the case where the right pointer
     * is the inorder successor, not the right descendant.
     */
    private boolean boolThread = false;
    /**
     * key is the comparison key stored at the node.
     */
    private final ComparableJ2me key;
    /**
     * left is a pointer to the left side of the tree, or
     * null if there is no left descendant.
     */
    private ThreadedBinaryTree left = null;
    /**
     * right is a pointer to the right side of the tree
     * or the inorder successor if there is no right descendant.
     */
    private ThreadedBinaryTree right = null;
    /**
     * value associated with this node
     */
    private Object value = null;
    /**
     * Create a new ThreadedBinaryTree node without
     * descendants or an inorder successor.
     * @param key the key for comparison.
     */
    public ThreadedBinaryTree(ComparableJ2me key) {
        this.key = key;
    }

    /**
     * Create a new ThreadedBinaryTree node with an
     * inorder successor.
     * @param key the key for comparison.
     * @param thread the inorder successor.
     */
    public ThreadedBinaryTree(
    		ComparableJ2me key, 
    		ThreadedBinaryTree thread) {
        this.key = key;
        this.right = thread;
        this.boolThread = true;
    }

    /**
     * Add a new key to an existing ThreadedBinaryTree, returning
     * the ThreadedBinaryTree node with the matching key. If the
     * key is already in the tree we make no change to the tree and
     * return the matching node. Otherwise we create a new node
     * and link it into the tree.
     * @param key the key for comparison.
     * @return the ThreadedBinaryTree node that matches key.
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
     */
    public ThreadedBinaryTree add(ComparableJ2me key) 
    	throws jjil.core.Error
    {
        int n = key.compareTo(this.key);
        if (n == 0) {
            return this; // already in tree
        } else if (n < 0) {
            if (this.left != null) {
                return this.left.add(key);
            } else {
                // left descendant's inorder successor is this
                this.left = new ThreadedBinaryTree(key, this);
                return this.left;
            }
        } else {
            // right descendant is (almost) never null, but boolThread
            // indicates we're at the bottom of the tree.
            if (this.right != null && !this.boolThread) {
                return this.right.add(key);
            } else {
                // link in new right descendant. Its inorder
                // successor is this node's inorder successor.
                // This node now has a right descendant and is
                // no longer a threaded node.
                this.right = new ThreadedBinaryTree(key, this.right);
                this.boolThread = false;
                return this.right;
            }
        }
    }

    /**
     * Find the ThreadedBinaryTree node that matches key.
     * @param key the key for comparison
     * @return the ThreadedBinaryTree node which matches key, or
     * null if nothing does.
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
    */
    public ThreadedBinaryTree find(ComparableJ2me key) 
    	throws jjil.core.Error
    {
        int n = this.key.compareTo(key);
        if (n == 0) {
            return this; // found
        } else if (n > 0) {
            // this is > than the key we are searching for. The match
            // is either to the left or not there
            if (this.left != null) {
                return this.left.find(key);
            } else {
                return null; // not found
            }
        } else {
            // this is < than the key we are searching for. The match
            // is either to the right or not there.
            if (this.right != null && !this.boolThread) {
                return this.right.find(key);
            } else {
                return null; // not found
            }
        }
    }

    /**
     * Find the ThreadedBinaryTree node with the largest key which is @le; the
     * given key.
     * @param key the key to compare
     * @return the ThreadedBinaryTree node which matches key, or
     * null if nothing does.
     * @throws jjil.core.Error in the case of type error (key wrong
     * type)
     */
    public ThreadedBinaryTree findNearest(ComparableJ2me key) 
    	throws jjil.core.Error
    {
        int n = this.key.compareTo(key);
        if (n == 0) {
            return this; // found exact match
        } else if (n < 0) {
            // this is < the key we are searching for. The best match is
            // the match to the right if there is one, otherwise this.
            if (this.right != null && !this.boolThread) {
                // the nearest is either this element, or down and to the
                // right
                ThreadedBinaryTree tbtRight = this.right.findNearest(key);
                if (tbtRight == null) {
                    return this;
                } else {
                    return tbtRight;
                }
            } else {
                // nothing to the right
                return this;
            }
        } else {
            // this is greater than the key. the nearest is either down and
            // to the left, or nothing
            if (this.left != null) {
                return this.left.findNearest(key);
            } else {
                return null; // not found
            }
        }
    }

    /**
     * Perform an inorder traversal of the tree from this
     * to end and return a Vector containing all the nodes
     * traversed, in the order of traversal, 
     * or null if there is no inorder path starting
     * at this and ending at end.
     * @param end the end of the path.
     * @return a Vector containing all the nodes traversed, in
     * the order of traversal, or null if there is no path.
     */
    public Vector inorderTraverse(
            ThreadedBinaryTree end) {
        Vector v = new Vector();
        // Start traversal with empty Vector. The traversal
        // is initialized with bFollowedThread = false because
        // we want to do inorder traversal from this node.
        return this.inorderTraverse(v, false, end);
    }

    /**
     * Helper function for actually doing the traversal.
     * Appends the next node traversed to the end of v
     * and continues the traversal recursively.
     * @param v the input Vector; this becomes the output on
     * a successful traversal.
     * @param bFollowedThread if true indicates we followed a
     * thread (an upward link) to get to this node.
     * @param end the ending node for the traversal.
     * @return the parameter v with the nodes traversed from
     * this to end appended to it, if there is a path; otherwise,
     * null.
     */
    private Vector inorderTraverse(
            Vector v,
            boolean bFollowedThread,
            ThreadedBinaryTree end) {
        if (!bFollowedThread) {
            // last traversal was downwards, not a threaded link.
            // this means that we should continue down and to the left.
            if (this.left != null) {
                return this.left.inorderTraverse(v, false, end);
            } else {
                // can't continue down and to the left. Add current
                // node to the traversal list and continue right,
                // either continuing down or up following thread.
                v.addElement(this);
                if (this == end) {
                    // success 
                    return v;
                } else {
                    return this.right.inorderTraverse(v, this.boolThread, end);
                }
            }
        } else {
            // last traversal was upwards (thread) link. We must
            // add current node to traversal list and continue
            // to the right (following descendant or thread).
            v.addElement(this);
            if (this == end) {
                // success
                return v;
            }
            if (this.right != null) {
                return this.right.inorderTraverse(v, this.boolThread, end);
            } else {
                // there is no right successor, so we reached end of tree
                // without finding end. There is no path.
                return null;
            }
        }
    }

    /**
     * Return the key associated with this node.
     * @return the key associated with this node.
     */
    public ComparableJ2me getKey() {
        return this.key;
    }
    
    /**
     * Return the current value associated with this node.
     * @return the value associated with this node.
     */
    public Object getValue() {
        return this.value;
    }
    
    /**
     * Change the value associated with this node.
     * @param value the new value to associate with this node.
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * Implement toString.
     */
    public String toString() {
    	String szRes = super.toString() + "(key=";
    	if (this.key != null) {
    		szRes += this.key.toString();
    	} else {
    		szRes += "null";
    	}
    	szRes += ",left=";
    	if (this.left != null) {
    		szRes += this.left.toString();
    	} else {
    		szRes += "null";
    	}
    	szRes += ",right=";
    	if (this.boolThread) {
    		szRes += "thread," + ((Object) this.right).toString();
    	} else if (this.right == null) {
    		szRes += "null";
    	} else {
    		szRes += this.right.toString();
    	}
    	szRes += ")";
    	return szRes;
    }
    
    // Test code
    // Tries all possible ThreadedBinaryTrees with elements 0-5 and verifies
    // inorder traversal from 0-5.
//		public int valueOf() {
//			return this.n;
//		}
//	}
//            try {
//            for (int i = 0; i<6; i++) {
//                for (int j = 0; j<6; j++) {
//                    if (i != j) for (int k=0; k<6; k++) {
//                        if (k != i && k != j) for (int l=0; l<6; l++) {
//                            if (l != k && l != j && l != i) for (int m=0; m<6; m++) {
//                                if (m != l && m != k && m != j && m != i) for (int n=0; n<6; n++) {
//                                    if (n != m && n != l && n != k && n != j && n != i) {
//                                    ThreadedBinaryTree t = new ThreadedBinaryTree(new ComparableInt(i));
//                                    t.add(new ComparableInt(j));
//                                    t.add(new ComparableInt(k));
//                                    t.add(new ComparableInt(l));
//                                    t.add(new ComparableInt(m));
//                                    t.add(new ComparableInt(n));
//                                    ThreadedBinaryTree tbt0 = t.find(new ComparableInt(0));
//                                    ThreadedBinaryTree tbt5 = t.find(new ComparableInt(5));
//                                    Vector v = tbt0.inorderTraverse(tbt5);
//                                    for (int o = 0; o<6; o++) {
//                                        ThreadedBinaryTree tbt = (ThreadedBinaryTree)v.elementAt(o);
//                                        ComparableInt ci = (ComparableInt)tbt.getKey();
//                                        if (ci.valueOf() != o) {
//                                            Vector u = tbt0.inorderTraverse(tbt5);
//                                            int error = 1;
//                                        }
//                                        
//                                    }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (jjil.core.Error e) {
//            
//        }
}
