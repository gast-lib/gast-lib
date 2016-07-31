/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.core;

/**
 * Class for managing regtangular regions in an RgbImage and their color mean
 * and variance values.
 * @author webb
 */
public class RgbRegion {
    /**
     * Class for holding a mean and variance color value.
     */
    public static class MeanVar {
        int nRgbMean;
        int nR, nG, nB;
        /**
         * Create a new MeanVar value, specifying color mean as a packed RGB
         * word and variance as int variables.
         * @param nRgbMean the packed RGB mean
         * @param nR red variance
         * @param nG green variance
         * @param nB blue variance
         */
        public MeanVar(int nRgbMean, int nR, int nG, int nB) {
            this.nRgbMean = nRgbMean;
            this.nR = nR;
            this.nG = nG;
            this.nB = nB;
        }
        
        /**
         * Return packed RGB mean color.
         * @return mean RGB color, packed in int
         */
        public int getMean() {
            return this.nRgbMean;
        }
        
        /**
         * Return red variance.
         * @return red variance
         */
        public int getRVar() {
            return this.nR;
        }
        
        /**
         * Return green variance
         * @return green variance
         */
        public int getGVar() {
            return this.nG;
        }
        
        /**
         * Return blue variance
         * @return blue variance
         */
        public int getB() {
            return this.nB;
        }
        
        public String toString() {
            return super.toString() + "(Mean=" +
                    RgbVal.toString(this.nRgbMean) + ",Var=[" +
                    new Integer(this.nR).toString() + "," +
                    new Integer(this.nG).toString() + "," +
                    new Integer(this.nB).toString() + "])";
        }
    }
    
    private MeanVar var;
    private Rect r;

    /**
     * Construct a new RgbRegion
     * @param r Rect boundaries of the region
     * @param var MeanVar mean and variance of the region
     */
    public RgbRegion(Rect r, MeanVar var) {
        this.r = r;
        this.var = var;
    }

    /**
     * Return mean color of the region
     * @return packed RGB word giving color of region
     */
    public int getColor() {
        return this.var.getMean();
    }

    /**
     * Return boundaries of region
     * @return Rect boundary of region
     */
    public Rect getRect() {
        return this.r;
    }
    
    /**
     * Implement toString
     * @return the class name, rectangle, and mean/variance.
     */
    public String toString() {
        return super.toString() + "," + this.r.toString() + "," +
                this.var.toString();
    }
}
