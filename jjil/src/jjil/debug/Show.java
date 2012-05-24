/*
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
package jjil.debug;

import jjil.core.RgbImage;

/**
 * An interface used to implement a level of indirection between a program
 * and debugging routines used for showing or saving images.<p>
 * The idea is that portions of a program can be written in an architecture-independent
 * fashion, so that the code that actually has to be architecture-dependent is
 * isolated in the main program only.<p>
 * The main program creates an object implementing Show using the appropriate
 * architecture-dependent library and then passes that Show object to the
 * architecture-independent methods. The architecture-independent methods can
 * then call toDisplay or toFile without worrying about whether they're running
 * in J2SE, J2ME, etc.
 * @author webb
 */
public interface Show {
    /**
     * Send an RgbImage to the display
     * @param rgb image to display.
     */
    void toDisplay(RgbImage rgb);

    /**
     * Save an RgbImage to a file.
     * @param rgb image to save
     * @param szFilename filname
     */
    void toFile(RgbImage rgb, String szFilename);
}
