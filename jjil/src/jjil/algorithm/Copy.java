/*
 * Copy.java
 *
 * Created on November 9, 2007, 8:48 AM
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
import jjil.core.Image;
import jjil.core.PipelineStage;
/**
 * Copies an input image to the output. This is a shallow copy, so if anything
 * modifies the image contents the copy will be modified too.
 * @author webb
 */
public class Copy extends PipelineStage {
    
    /** Creates a new instance of Copy */
    public Copy() {
    }
    
    /**
     * Copy an input image to the output without creating a deep copy of the contents.
     * @param im Input image. May be of any Image type.
     */
    public void push(Image im) {
        super.setOutput(im);
    }
}
