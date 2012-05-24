/*
 * ComparableJ2me.java
 *  This is a replacement (for use with J2ME / CLDC 1.0) of the
 *  Java class Comparable defined in J2SE.
 *
 * Created on April 15, 2008, 7:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jjil.algorithm;

/**
 * This is a replacement (for use with J2ME / CLDC 1.0) of the
 *  Java class Comparable defined in J2SE. It is defined here (not in the j2me
 * library) because the class is needed in algorithms like EquivalenceClass.
 * @author webb
 */
public interface ComparableJ2me {
    /**
     * Compare this object with another object of the same type.
     * @param o input object to compare with
     * @throws jjil.core.Error if the input is null or not of the expected type.
     * @return -1 if this object is less than the compared object; 0 if they are equal; and 
     * 1 if this object is greater than the compared object.
     */
    int compareTo(Object o) throws jjil.core.Error;
}
