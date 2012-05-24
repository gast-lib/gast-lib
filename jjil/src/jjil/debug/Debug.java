/**
 * Copyright 2006 by Jon A. Webb
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
 * @author webb
 */
package jjil.debug;


import jjil.core.RgbImage;

/**
 * This class is used to generalize debugging operations so they can be used
 * in a program without making it dependent on a particular architecture. 
 * The operations in this class can be used to save images to files and display
 * them, and they will run properly on whatever architecture implements
 * the Show class.<p>
 * The idea is that your main program (which is architecture-dependent) creates
 * a Show object using the appropriate architecture-dependent library, then
 * passes the object to architecture-independent classes. When the 
 * architecture-independent classes need to show an image for debugging, for
 * example, they call the toDisplay method in their Show object, without being
 * aware of whether the toDisplay method is displaying the image on a PC 
 * (i.e., J2SE) or a mobile device (i.e., J2ME).
 */
public class Debug implements Show {
	private static Show show;
	
        /**
         * Assign a Show object to this Debug object. This has to be done only
         * once. After the Show object is assigned it will be used for any
         * subsequence toDisplay or toFile method calls.
         * @param show the Show object to use in the future.
         */
	public static void setShow(Show show) {
		Debug.show = show;
	}
	
        /**
         * Display an RgbImage on the display used by the Show object. If no
         * Show object has been assigned nothing is done.
         * @param rgb RgbImage to display.
         */
	public synchronized void toDisplay(RgbImage rgb) {
		if (Debug.show != null) {
			Debug.show.toDisplay(rgb);
		}
	}
	
        /**
         * Sqave an RgbImage to a file using the Show object specified
         * previously. If no Show object has been specified nothing is done.
         * @param rgb RgbImage to save.
         * @param szFilename filename to save the image in.
         */
	public void toFile(RgbImage rgb, String szFilename) {
		if (Debug.show != null) {
			Debug.show.toFile(rgb, szFilename);
		}
	}
}
