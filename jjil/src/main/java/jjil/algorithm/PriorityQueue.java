package jjil.algorithm;
// The code in this file is from
// http://www.java-tips.org/java-se-tips/java.lang/priority-queue-binary-heap-implementation-in-3.html

//PriorityQueue interface
//
// ******************PUBLIC OPERATIONS*********************
// Position insert( x )   --> Insert x
// ComparableJ2me deleteMin( )--> Return and remove smallest item
// ComparableJ2me findMin( )  --> Return smallest item
// boolean isEmpty( )     --> Return true if empty; else false
// void makeEmpty( )      --> Remove all items
// int size( )            --> Return size
// void decreaseKey( p, v)--> Decrease value in p to v
// ******************ERRORS********************************
// Throws UnderflowException for findMin and deleteMin when empty

/**
 * PriorityQueue interface. A priority queue maintains a sorted list of items,
 * and makes it possible to add new items or return and remove the least item
 * in sorted order in O(log n) time, where n is the number of items.<br>
 * Some priority queues may support a decreaseKey operation,
 * but this is considered an advanced operation. If so,
 * a Position is returned by insert.
 * Note that all "matching" is based on the compareTo method.
 * @author Mark Allen Weiss
 */
public interface PriorityQueue {
    /**
     * The Position interface represents a type that can
     * be used for the decreaseKey operation.
     */
    public interface Position {
        /**
         * Returns the value stored at this position.
         * @return the value stored at this position.
         */
        ComparableJ2me getValue( );
    }
    
    /**
     * Insert into the priority queue, maintaining heap order.
     * Duplicates are allowed.
     * @return may return a Position useful for decreaseKey.
     * @param x the item to insert.
     * @throws jjil.core.Error if x is not of the correct type or null.
     */
    Position insert( ComparableJ2me x ) throws jjil.core.Error;
    
    /**
     * Find the smallest item in the priority queue.
     * @return the smallest item.
     * @throws jjil.core.Error if the heap is empty.
     */
    ComparableJ2me findMin( ) throws jjil.core.Error;
    
    /**
     * Remove and return the smallest item from the priority queue.
     * @return the smallest item.
     * @throws jjil.core.Error if empty.
     */
    ComparableJ2me deleteMin( ) throws jjil.core.Error;
    
    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    boolean isEmpty( );
    
    /**
     * Make the priority queue logically empty.
     */
    void makeEmpty( );
    
    /**
     * Returns the size.
     * @return current size.
     */
    int size( );
    
    /**
     * Change the value of the item stored in the pairing heap.
     * This is considered an advanced operation and might not
     * be supported by all priority queues. A priority queue
     * will signal its intention to not support decreaseKey by
     * having insert return null consistently.
     * @param p any non-null Position returned by insert.
     * @param newVal the new value, which must be smaller
     *    than the currently stored value.
     * @throws IllegalArgumentException if p invalid.
     * @throws UnsupportedOperationException if appropriate.
     */
/*    void decreaseKey( Position p, ComparableJ2me newVal );
*/
}


