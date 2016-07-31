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

/**
 * EquivalenceClass implements equivalence classes using the efficient
 * union-find algorithm whose complexity grows as the inverse
 * Ackermann's function.
 * <p>
 * The code here is based on a Wikipedia article
 * en.wikipedia.org/wiki/Connected_Component_Labeling
 * <br>
 * new EquivalenceClass(int nLabel) creates a new equivalence class with 
 * the given label. Note: nLabel must be unique to calls to the constructor.
 * <br>
 * e.find() returns the equivalence class for class e.
 * <br>
 * e.union(f) unions class e and class f.
 * <br>
 * e.getLabel() returns the integer label for e.
 * e.getLabel() == f.getLabel() iff e and f are in the same
 * equivalence class. 
 * @author webb
 *
 */
public class EquivalenceClass {
	private static int nNextLabel = 1;
	private final int nLabel;
	private int nRank = 0;
	private EquivalenceClass eParent;
	
	/**
	 * Create a new set
	 */
	public EquivalenceClass() {
		this.nLabel = EquivalenceClass.nNextLabel ++;
		setParent(this);
	}
	
	private void incrRank() {
		this.nRank ++;
	}
	
	/**
	 * Look up the equivalence class for this set.
	 * @return this set's equivalence class.
	 */
	public EquivalenceClass find() {
		if (this.getParent() == this) {
			return this;
		} else {
			this.setParent(this.getParent().find());
			return this.getParent();
		}
	}
	
	/**
	 * Returns the unique label for this equivalence class.
	 * @return this equivalence class's label.
	 */
	public int getLabel() {
		return this.find().nLabel;
	}
        
        /**
         * Returns number of labels assigned since reset
         * @return max number of distinct labels
         */
	public static int getLabels() {
            return EquivalenceClass.nNextLabel;
        }
	
	private EquivalenceClass getParent() {
		return this.eParent;
	}
	private int getRank() {
		return this.nRank;
	}
	private void setParent(EquivalenceClass e) {
		this.eParent = e;
	}
        
    /** Must be called once when a new set of equivalence classes is
     *  to be defined.
     */
    public static void reset() {
        EquivalenceClass.nNextLabel = 1;
    }
	
    /**
     * Unifies this class with another class. After this operation
     * this and y will be in the same equivalence class.
     * @param y the class to unify with.
     */
    public void union(EquivalenceClass y) {
            EquivalenceClass xRoot = this.find();
            EquivalenceClass yRoot = y.find();
            if (xRoot.getRank() > yRoot.getRank()) {
                    yRoot.setParent(xRoot);
            } else if (xRoot.getRank() < yRoot.getRank()) {
                    xRoot.setParent(yRoot);
            } else if (xRoot != yRoot) {
                    yRoot.setParent(xRoot);
                    xRoot.incrRank();
            }
    }
}
