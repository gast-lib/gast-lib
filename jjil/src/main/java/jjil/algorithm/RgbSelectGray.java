/*
 * RgbSelectGray.java
 *
 * Created on August 27, 2006, 11:33 AM
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
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.RgbImage;
import jjil.core.RgbVal;

/**
 * <p>
 * Transforms a RgbImage into a Gray8Image by selecting one of the three
 * bands. The pixel value chosen is adjusted from the range 0&rarr;255 to the
 * range -128&rarr;127.
 * </p>
 * <p>
 *    Usage: <br>
 * 
 *    <CODE>
 *        RgbImage imageRgb = ...;<br>
 *        RgbSelectGray rgb = new RgbSelectGray(RgbSelectGray.RED);<br>
 *        rgb.push(imageRgb);
 *    </CODE>
 * </p>
 * @author webb
 */
public class RgbSelectGray extends PipelineStage {
    /* In the absence of enums in version 1.3 we use three empty singleton
     * classes to represent the color choice.
     */
    /**
     * Used to represent the three colors red, green, or blue.
     */
    public static class ColorClass {
        private String name;
        private static final ColorClass RED = new ColorClass("RED");
        private static final ColorClass GREEN = new ColorClass("GREEN");
        private static final ColorClass BLUE = new ColorClass("BLUE");
        
        
        private ColorClass(String name) {
            this.name = name;
        }
        
        /**
         * Represents the color red.
         * @return A ColorClass object that represents the color red.
         */
        public static ColorClass Red() {
            return RED;
        }
        
        /**
         * Represents the color green.
         * @return A ColorClass object that represents the color green.
         */
        public static ColorClass Green() {
            return GREEN;
        }
        
        /**
         * Represents the color blue.
         * @return A ColorClass object that represents the color blue.
         */
        public static ColorClass Blue() {
            return BLUE;
        }
        
        /**
         * Returns a string representation of the RgbSelectGray operation.
         * @return a String representing the RgbSelectGray operation.
         */
        public String toString() {
            return name;
        }
     };

    /**
     * The class represents the color red. It is used like an enumerated value when
     * calling the RgbSelectGray constructor.
     */
    public static final ColorClass RED = ColorClass.Red();

    /**
     * The class represents the color green. It is used like an enumerated value when
     * calling the RgbSelectGray constructor.
     */
    public static final ColorClass GREEN = ColorClass.Green();
    /**
     * The class represents the color blue. It is used like an enumerated value when
     * calling the RgbSelectGray constructor.
     */
    public static final ColorClass BLUE = ColorClass.Blue();
    
    /**
     * Aliases for red, green, and blue used when we're thinking of the RGB image
     * as an HSV image.
     */
    public static final ColorClass HUE = ColorClass.Red();
    public static final ColorClass SATURATION = ColorClass.Green();
    public static final ColorClass VALUE = ColorClass.Blue();
    
    private ColorClass colorChosen;
    
    /**
     * Creates a new instance of RgbSelectGray.
     * @param color the color selected from the color image to create the gray image.
     * @throws jjil.core.Error if color is not RED, GREEN, or BLUE.
     */
    public RgbSelectGray(ColorClass color) throws jjil.core.Error {
        setColor(color);
    }
    
    /**
     * Returns the current color selected.
     *
     * @return the current color, as a ColorClass object.
     */
    public ColorClass getColor() {
        return this.colorChosen;
    }
    
    /** Convert a color image to gray by selecting one of the color
     * bands: red, green, or blue. The band selected is chosen in the
     * class constructor. The gray pixel value is adjusted so its range
     * is from -128&rarr;127 instead of the 0-255 value in the ARGB word.
     *
     * @param image the input image
     * @throws jjil.core.Error if the input image is not a color
     *   image.
     */
    public void push(Image image) throws jjil.core.Error {
        if (!(image instanceof RgbImage)) {
            throw new Error(
            			Error.PACKAGE.ALGORITHM,
            			ErrorCodes.IMAGE_NOT_RGBIMAGE,
            			image.toString(),
            			null,
            			null);
        }
        RgbImage rgb = (RgbImage) image;
        int[] rgbData = rgb.getData();
        Gray8Image gray = new Gray8Image(image.getWidth(), image.getHeight());
        byte[] grayData = gray.getData();
        if (colorChosen.equals(RED)) {
            for (int i=0; i<image.getWidth() * image.getHeight(); i++) {
                /* get individual color value, unmasking it from the
                 * ARGB word
                 */
                grayData[i] = RgbVal.getR(rgbData[i]);
            }
        } else if (this.colorChosen.equals(GREEN)) {
            for (int i=0; i<image.getWidth() * image.getHeight(); i++) {
                /* get individual color value, unmasking it from the
                 * ARGB word 
                 */
                grayData[i] = RgbVal.getG(rgbData[i]);
            }
        } else /* must be BLUE, from constructor */ {
            for (int i=0; i<image.getWidth() * image.getHeight(); i++) {
                /* get individual color value, unmasking it from the
                 * ARGB word */
                grayData[i] = RgbVal.getB(rgbData[i]);
            }
        }
        super.setOutput(gray);
    }

    /**
     * Changes the color selected.
     * @param color the new color selected
     * @throws jjil.core.Error if the input color is not ColorClass.RED, GREEN, or BLUE.
     */
    public void setColor(ColorClass color) throws jjil.core.Error {
        /* as I understand the language this can't happen, but just in
         * case...
         */
        if (!(color.equals(RgbSelectGray.RED) || 
                color.equals(RgbSelectGray.GREEN) || 
                color.equals(RgbSelectGray.BLUE))) {
            throw new Error(
        			Error.PACKAGE.ALGORITHM,
        			ErrorCodes.ILLEGAL_COLOR_CHOICE,
        			color.toString(),
        			null,
        			null);

        }
        this.colorChosen = color;
    }
    
    /** Return a string describing the RGB select operation.
     *
     * @return the string describing the RGB select operation.
     */
    public String toString() {
        return super.toString() + " (" + this.colorChosen.toString() +  //$NON-NLS-1$
                ")"; //$NON-NLS-1$
    }
    
}
