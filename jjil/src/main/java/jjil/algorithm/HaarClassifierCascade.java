/*
 * HaarClassifierCascade.java
 *
 * Created on July 7, 2007, 3:23 PM
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import jjil.core.Error;
import jjil.core.Gray32Image;
import jjil.core.Gray8Image;
import jjil.core.Image;
/**
 * HaarClassifierCascade implements a Haar classifier, which is a trainable
 * image processing tool for detecting the presence of a feature or class of
 * features. A Haar classifier is trained by providing it with a large collection
 * of positive and negative sample images. The training technique develops a
 * collection of simple feature detection operations (like simple edge detectors)
 * that are applied to the image and then thresholded. The feature detectors
 * are organized into a tree so that they work as a cascade. An image which makes
 * it to the end of the cascade has a high probability of actually containing the
 * feature in question (depending on how well the sample image selection was done
 * and how thorough the training was.) <br>
 * The code here does not implement the training step, which is compute-intensive.
 * That should be run on a PC, using code from the Open Computer Vision (OpenCV)
 * library. The OpenCV is available on-line at 
 * http://sourceforge.net/projects/opencvlibrary/. It can be run under Windows or
 * Linux and has been optimized for best performance on Intel processors. It
 * also includes multiprocessor support. <br>
 * Once the Haar classifier has been trained using the OpenCV HaarTraining
 * application, the cascade has to be transformed into a text file that can be
 * loaded into this code. This is done with a C++ program called 
 * haar2j2me. Haar2j2me changes the floating-point values in the OpenCV's Haar
 * cascade into integer, scaling appropriately, and greatly reduces the size
 * of the file (the XML files produced by HaarTraining are just too large to fit
 * on many cellphones). You can find a copy of haar2j2me where you got this code.<br>
 * <b>Note:</b> the code below does not implement tilted features, and has not been
 * tested for anything but stump-based Haar classifiers.
 * @author webb
 */
public abstract class HaarClassifierCascade implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3103210998383577218L;
	/**
     * The width of the image.
     */
    protected int width;
    /**
     * Haar cascade image height.
     */
    protected int height; // size of image
    
    /**
     * Returns the Haar cascade image width.
     * @return the Haar cascade image width.
     */
    public int getWidth() {
        return width;
    };
    /**
     * Returns the Haar cascade image height.
     * @return the Haar cascade image height.
     */
    public int getHeight() {
        return height;
    }
    
   
    /**
     * Reads an array of characters, skipping newlines.
     * @param isr the input stream to read
     * @param rChars array of characters
     * @param nStart starting position in array to assign
     * @param nLength number of characters to read
     * @throws IOException if InputStreamReader.read() does
     * @throws Error if input is terminated before all characters are read
     */
    protected static void readChars(InputStreamReader isr, char[] rChars, int nStart, int nLength) 
    	throws IOException, Error
    	{
    		for (int i=0; i<nLength; i++) {
    			int n = isr.read();
    			if (n == -1) {
                    throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.INPUT_TERMINATED_EARLY,
                            isr.toString(),
                            null,
                            null);
    			}
    			rChars[i+nStart] = (char) n;
    		}
    	}
       
    /**
     * Returns true iff the input image passes all the tests in the Haar cascade, i.e.,
     * is a member of the positive sample image set, so far as it can tell.
     * @param i The input Gray8Image. The image size must be equal to the expected size
     * (as given by getWidth() and getHeight()).
     * @return true iff the input image passes all the tests in the Haar cascade.
     * @throws jjil.core.Error if the input image is not a Gray8Image or is the wrong size.
     */
    public abstract boolean eval(Image i) throws jjil.core.Error;
    
    /**
     * Support method for reading integers from an input stream. The single-character
     * separator following the integer is also read. So a stream containing
     * "5678 2134)9928" will return 5678, then 2134, then 9928. Negative numbers
     * are supported.<br>
     * The separator character can be any non-numeric character.<br>
     * Note that I can't use Integer.parseInt because I don't know how many 
     * characters are to be read.
     * @return the next integer read from the input stream.
     * @param isr The input stream.
     * @throws jjil.core.Error if there is a parse error in the file.
     * @throws java.io.IOException if the read method of isr returns an IOException.
     */
    protected static int readInt(InputStreamReader isr) 
        throws jjil.core.Error, IOException, IOException
    {
        int n = 0;
        int sign = 1;
        do {
           int nChar = isr.read();
            if (nChar == -1) {
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.INPUT_TERMINATED_EARLY,
                                isr.toString(),
                                null,
                                null);
            }
            char c = (char) nChar;
            if (c == '-') {
                sign = -1;
            } else {
                if (!Character.isDigit(c)) {
                    return n * sign;
                }
                n = n * 10 + Character.digit(c, 10);
            }
        } while (true);
    }
    
    
    
    
    // One Haar feature. Each consists of up to three weighted rectangles
    /**
     * HaarFeature defines an individual feature used by the Haar cascade.
     * A feature consists of up to three weighted rectangles (implemented
     * by HaarRect) which are convolved with the image. Their sum is the
     * result of applying the HaarFeature to the image.
     */
    protected class HaarFeature implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1636121702312072988L;

		/*
        * HaarRect.java
        * HaarRect is an abstract class to make the computation of the rectangular
        * basic component of a Haar feature. It is abstract because the placement
        * of the rectangle affects the sum that has to be done.
        * The basic computation is shown by the diagram below
        *      ..........n1|_______n3|
        *      ............|/////////|
        *      ............|/////////| (// is the area we're summing)
        *      ..........n4|///////n2|
        * xx is the sum of all pixels at and to the left and above of positions xx.
        * The computation is br - bl - tr + tl (tl is added because bl and
        * tr both include tl).
        * If the rectangle is at the edge of the image we don't use some of tl, tr,
        * or bl because they're off the edge of the image. So we have HaarRectTop,
        * HaarRectTopLeft, etc.
        */
        abstract class HaarRect implements Serializable {
            /**
			 * 
			 */
			private static final long serialVersionUID = -8673919841412300053L;
			// eval returns the rectangle feature value for the current image.
            // input image is the cumulative sum of the original
            protected abstract int eval(Gray32Image i);
            // We precompute the indices of the features so we have to
            // change their values whenever the image width changes.
            protected abstract void setWidth(int nWidth);

        }
        
        // Used for third null rectangle when a HaarFeature only uses 2 
        // rectangles.
        class HaarRectNone extends HaarRect implements Serializable {

            /**
			 * 
			 */
			private static final long serialVersionUID = 3419647846408882196L;

			/** Creates a new instance of HaarRectNone */
            public HaarRectNone() {
            }

            protected int eval(Gray32Image i) {
                return 0;
            }
            
            protected void setWidth(int nWidth) {
            }
            
            public String toString() {
                return "(hr 0 0 0 0 0)"; //$NON-NLS-1$
            }
        }
        
        // HaarRect describes one rectangle in a Haar feature. It is used except
        // at the top or left side of the image or when the area of
        // the rectangle is 0.
        // There are up to 3 rectangles in a feature
        class HaarRectAny extends HaarRect implements Serializable {
            /**
			 * 
			 */
			private static final long serialVersionUID = -6033181980691702489L;
			private int n1, n2, n3, n4;
            private int tlx, tly, w, h; // rectangle coordinates
            private int weight; // convolution weight assigned to rectangle

            public HaarRectAny(int tlx, int tly, int w, int h, int weight) {
                this.tlx = tlx;
                this.tly = tly;
                this.w = w;
                this.h = h;
                this.weight = weight;
            }

            protected int eval(Gray32Image image) {
                int data[] =  image.getData();
                return weight * ( data[this.n1] + data[this.n2] -
                                  data[this.n3] - data[this.n4] );
            }
            
            // when image width is changed we have to recompute the indices.
            protected void setWidth(int nWidth) {
                    this.n1 = (tly-1)*nWidth + (tlx-1);
                    this.n2 = (tly+h-1)*nWidth + (tlx+w-1);
                    this.n3 = (tly-1)*nWidth + (tlx+w-1);
                    this.n4 = (tly+h-1)*nWidth + (tlx-1);
             }
            
            public String toString() {
                return "(hr " + this.tlx + " " + this.tly + //$NON-NLS-1$ //$NON-NLS-2$
                        " " + this.w + " " + this.h +  //$NON-NLS-1$ //$NON-NLS-2$
                        " " + this.weight + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        
        // HaarRectLeft describes one rectangle in a Haar feature
        // where the rectangle is at the left side of the image (x = 0 and y != 0)
        // There are up to 3 rectangles in a feature.
        class HaarRectLeft extends HaarRect implements Serializable {
            /**
			 * 
			 */
			private static final long serialVersionUID = 3287079704778038972L;
			private int n2, n3;
            private int tly, w, h; // rectangle coordinates
            private int weight; // convolution weight assigned to rectangle

            public HaarRectLeft(int tly, int w, int h, int weight) {
                this.tly = tly;
                this.w = w;
                this.h = h;
                this.weight = weight;
            }


            protected int eval(Gray32Image image) {
                int data[] = image.getData();

                return weight * ( data[this.n2] - data[this.n3] );
            }
            
            protected void setWidth(int nWidth) {
                // we precompute the indices so that we don't
                // have to do computation using nWidth in the
                // usual case, when nWidth doesn't change.'
                this.n2 = (tly+h-1)*nWidth + w - 1;
                this.n3 = (tly-1)*nWidth + w - 1;
            }
            
            public String toString() {
                return "(hr 0 " + this.tly + //$NON-NLS-1$
                        " " + this.w + " " + this.h +  //$NON-NLS-1$ //$NON-NLS-2$
                        " " + this.weight + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        
        // HaarRectTop describes one rectangle in a Haar feature
        // where the rectangle is at the top of the image (y = 0 but x != 0)
        // There are up to 3 rectangles in a feature.
        class HaarRectTop extends HaarRect implements Serializable {
            /**
			 * 
			 */
			private static final long serialVersionUID = 770007602125395214L;
			private int n2, n4;
            private int tlx, w, h; // rectangle coordinates
            private int weight; // convolution weight assigned to rectangle

            public HaarRectTop(int tlx, int w, int h, int weight) {
                this.tlx = tlx;
                this.w = w;
                this.h = h;
                this.weight = weight;
            }


            protected int eval(Gray32Image image) {
                int data[] = image.getData();

                return weight * ( data[this.n2] - data[this.n4] );
            }
            
            protected void setWidth(int nWidth) {
                // we precompute the indices so that we don't
                // have to do computation using nWidth in the
                // usual case, when nWidth doesn't change.'
                    this.n2 = (h - 1)*nWidth + (tlx+w - 1);
                    this.n4 = (h - 1)*nWidth + (tlx-1);
            }
            
            public String toString() {
                return "(hr " + this.tlx + " 0 " +  //$NON-NLS-1$ //$NON-NLS-2$
                        this.w + " " + this.h +  //$NON-NLS-1$
                        " " + this.weight + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        
        // Used when the rectangle is at the top left of the image (x=0 and y=0)
        class HaarRectTopLeft extends HaarRect implements Serializable {
            /**
			 * 
			 */
			private static final long serialVersionUID = -2184273133213370871L;
			private int n2;
            private int w, h; // rectangle coordinates
            private int weight; // convolution weight assigned to rectangle

            public HaarRectTopLeft(int w, int h, int weight) {
                this.w = w;
                this.h = h;
                this.weight = weight;
            }


            protected int eval(Gray32Image image) {
                int data[] = image.getData();

                return weight * ( data[this.n2] );
            }
            
             protected void setWidth(int nWidth) {
                // we precompute the indices so that we don't
                // have to do computation using nWidth in the
                // usual case, when nWidth doesn't change.'
                this.n2 = (h - 1)*nWidth + w - 1;
             }
             
            public String toString() {
                return "(hr 0 0 " + this.w + " " + this.h +  //$NON-NLS-1$ //$NON-NLS-2$
                        " " + this.weight + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
       };
        
        // construct from input
        // the expected input is
        // (hr <tlx>,<tly>,<w>,<h>,<weight>)
       // note: this is really a static constructor for HaarRect and
       // should be a static member of the abstract HaarRect class but
       // since I've made HaarRect an inner class it can't be. I couldn't
       // think of a better way to do this than to make this a member of
       // HaarFeature and make the name as below.
        private HaarRect makeHaarRectFromStream(InputStreamReader isr) 
            throws jjil.core.Error, IOException
        {

            char[] rC = new char[4];
            readChars(isr,rC, 0, 4);
            if ("(hr ".compareTo(new String(rC)) != 0) { //$NON-NLS-1$
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.PARSE_ERROR,
                                new String(rC),
                                "(hr ",
                                isr.toString());
            }

            int tlx = readInt(isr);
            int tly = readInt(isr);
            int w = readInt(isr);
            int h = readInt(isr);
            int weight = readInt(isr);
            if (w == 0 || h == 0) {
                return new HaarRectNone();
            } else if (tlx == 0 && tly == 0) {
                return new HaarRectTopLeft(w, h, weight);
            } else if (tlx == 0) {
                return new HaarRectLeft(tly, w, h, weight);
            } else if (tly == 0) {
                return new HaarRectTop(tlx, w, h, weight);
            } else {
                return new HaarRectAny(tlx, tly, w, h, weight);
            }
        }
        
        // Private variables in HaarFeature
        private boolean bTilted;  // in the present implementation bTilted 
                                  // must always be false
        private HaarRect rect[];
        
        // create HaarFeature from stream
        // expected input: (hf <bTilted><HaarRect><HaarRect><HaarRect>)
        /**
         * Loads a HaarFeature from an input stream. This is the only way
         * to create a HaarFeature. The expected input is "(hf "Haar rect [0]"
         * "Haar rect [1]" "Haar rect [2]" tilted) where tilted is 1 if the rectangles
         * are tilted, 0 if not. Tilted rectangles are currently not implemented. The
         * Haar rectangles can be null, which means they have an area of 0.
         * @param isr Input stream. The expected input is "(hf "Haar rect [0]"
         * "Haar rect [1]" "Haar rect [2]" tilted) where tilted is 1 if the rectangles
         * are tilted, 0 if not.
         * @throws java.io.IOException if the input stream reader methods return IOException, or we get an early
         * end of file.
         * @throws jjil.core.Error if the input is not in the expected format.
         */
        public HaarFeature(InputStreamReader isr)
           throws jjil.core.Error, IOException 
        {
            char[] rC = new char[4];
            readChars(isr,rC, 0, 4);
            if ("(hf ".compareTo(new String(rC)) != 0) { //$NON-NLS-1$
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.PARSE_ERROR,
                                new String(rC),
                                "(hf ",
                                isr.toString());
            }
            this.rect = new HaarRect[3];
            this.rect[0] = makeHaarRectFromStream(isr);
            this.rect[1] = makeHaarRectFromStream(isr);
            this.rect[2] = makeHaarRectFromStream(isr);
            this.bTilted = (readInt(isr) == 1);
        }
        
        /**
         * Applies the HaarFeature to the image and returns the integer equal to the
         * result of convolving the rectangles in the feature with the image.
         * @param image the input image to which the feature is to be applied. The width should be
         * equal to the last width parameter passed to setWidth().
         * @return the integer equal to the result of convolving the rectangles in the feature with the image.
         */
        public int eval(Gray32Image image) {
            int nSum = 0;
            for (int i=0; i<rect.length; i++) {
                nSum += rect[i].eval(image);
            }
            return nSum;
        }
        
        /**
         * Changes the image width for the current feature. The image width is used
         * to pre-calculate the offsets of the rectangles within the image.
         * @param nWidth The expected image width.
         */
        public void setWidth(int nWidth) {
            for (int i=0; i<rect.length; i++) {
                rect[i].setWidth(nWidth);
            }           
        }
        
        /**
         * Returns a String representation of the HaarFeature. Passing this String
         * to the constructor via an input stream will create a HaarFeature with the
         * same behavior.
         * @return A string representation of the HaarFeature.
         */
        public String toString() {
            return "(hf " + this.rect[0].toString() +  //$NON-NLS-1$
                    this.rect[1].toString() + this.rect[2].toString() +
                    (this.bTilted ? "1" : "0") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    };
    
    /**
     * A weak classifier is a leaf in the Haar classifier cascade. It implements a
     * test which returns an integer. The results of multiple weak classifiers are
     * summed and the result is thresholded to determine whether the current image
     * passes the Haar classifier test at this stage.
     */
    protected interface HaarWeakClassifier {
        /**
         * Applies a HaarWeakClassifier to an image and returns an integer which can be
         * summed and thresholded to determine whether this image is an example or not
         * of the feature we are detecting.
         * @param image Input image.
         * @return The result of applying the weak classifier to the image.
         */
        public int eval(Gray32Image image);
    };
    
    /**
     * A stage classifier applies multiple HaarWeakClassifiers to an image and
     * sums the results. The result is then thresholded and that determines whether 
     * the image passes the current stage classifier or not (in a stump-based
     * classifier).
     */
    protected interface HaarStageClassifier {
        /**
         * Applies a HaarStageClassifier to an image and returns true if the image
         * passes this stage of the Haar classifier cascade, or false if it does not.
         * Any image that fails is rejected (in a stump-based classifier).
         * @param image input image.
         * @return true if the input image passes this stage of the classifer, false
         * if not.
         */
        public boolean eval(Gray32Image image);
    };
 

    /**
     * Creates a new instance of HaarClassifierCascade from an input stream as 
     * generated by haar2j2me. The data structure is (hcsb "Haar classifer stump base")
     * where "Haar classifer stump base" is the string for a stump-based Haar
     * classifer (this loader only loads stump-based Haar classifiers).
     * @param isr Input stream containing the description of the Haar classifier.
     * @return The created HaarClassifierCascade. This will always be of
     * type HaarClassifierStumpBase.
     * @throws java.io.IOException if the read from isr returns an IOException, or if end of file is encountered unexpectedly.
     * @throws jjil.core.Error If the input doesn't match what is expected.
     */
    public static HaarClassifierCascade fromStream(InputStreamReader isr) 
           throws jjil.core.Error, IOException 
    {
        // read the first token from the stream
        String szToken = ""; //$NON-NLS-1$
        char c;
        do {
            int nCh = isr.read();
            if (nCh == -1) {
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.INPUT_TERMINATED_EARLY,
                                isr.toString(),
                                null,
                                null);
            }
            c = (char) nCh;
            szToken += c;
        } while (c != ' ');
        if (szToken.compareTo("(hcsb ") == 0) return new HaarClassifierStumpBase(isr); //$NON-NLS-1$
        else
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.PARSE_ERROR,
                            szToken,
                            "(hcsb ",
                            isr.toString());
    }
    
}
        
class HaarClassifierTreeBase extends HaarClassifierCascade
	implements Serializable 
{
    //////////////////////////////////////////////////////////////////////////
    //
    // Tree-structured Haar classifier classes
    //
    //////////////////////////////////////////////////////////////////////////
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8507574576144755449L;

	// A tree-structured Haar classifier consists of a Haar feature and a
    // threshold. The Haar feature is evaluated. If it is less than
    // the threshold then we choose the left Haar classifier as the next
    // stage; if it is greater or equal we choose the right Haar classifier.
    // When we reach a leaf (the next node index is null) we return alpha as
    // the result of the classifier.
    public class HaarWeakClassifierTree 
    	implements HaarWeakClassifier, Serializable 
    {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1106803066661189899L;
		private HaarFeature feature;   // Haar feature tested by this classifier
        private int threshold;         // threshold feature compared with
        private HaarWeakClassifier left;   // successor HaarClassifer if <
        private HaarWeakClassifier right;  // successor HaarClassifier if >=
        private int alpha;             // return result if successor = 0
        
        public int eval(Gray32Image image) {
            int nHf = this.feature.eval(image);
            HaarWeakClassifier hcNext;
            if (nHf < this.threshold) {
                hcNext = this.left;
            } else {
                hcNext = this.right;
            }
            if (hcNext == null) {
                return this.alpha;
            } else {
                return hcNext.eval(image);
            }

        }
    };
    
   
    // A HaarStageClassifer consists of an array of HaarClassifier's.
    // Each is evaluated and the sum of the results is compared with the
    // threshold. If it is >= the threshold then we
    // evaluate the HaarStageClassifer at child (if child is null we are successful).
    // If it is < the threshold we
    // go to the parent HaarStageClassifier and then continue at the parent's next,
    // unless it is null, in which case the result is 0.

    private int threshold;
    private HaarWeakClassifier classifier[];
    private HaarStageClassifier next;
    private HaarStageClassifier child;
    private HaarClassifierTreeBase parent;

    public boolean eval(Image image) throws jjil.core.Error {
        if (!(image instanceof Gray32Image)) {
             throw new Error(
            				Error.PACKAGE.ALGORITHM,
            				ErrorCodes.IMAGE_NOT_GRAY32IMAGE,
            				image.toString(),
            				null,
            				null);
        }
        Gray32Image g32 = (Gray32Image) image;
        int nSumHc = 0;
        for (int i=0; i<this.classifier.length; i++) {
            HaarWeakClassifier hc = this.classifier[i];
            nSumHc += hc.eval(g32);
        }
        if (nSumHc >= this.threshold) {
            if (this.child == null) {
                return true;
            } else {
                return this.child.eval(g32);
            }
        } else {
            if (this.parent == null || this.parent.next == null) {
                return false;
            } else {
                return this.parent.next.eval(g32);
            }
        }
    }
}


/////////////////////////////////////////////////////////////////////////
//
// Stump-structured Haar classifier clases
//
/////////////////////////////////////////////////////////////////////////

// A stump-structured Haar classifier consists of a Haar feature and a
// threshold. The Haar feature is evaluated. 
    
class  HaarClassifierStumpBase extends HaarClassifierCascade 
	implements Serializable
	{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8724165260201623422L;
	// A stump-structured Haar classifier consists of a Haar feature and a
    // threshold. The Haar feature is evaluated. The result is compared with
    // t = threshold * variance_norm_factor. If < t then it returns a,
    // o/w b.
    private int nWidth = 0;     // for detecting when image width changes
    
    public class HaarWeakClassifierStump 
    	implements HaarWeakClassifier, Serializable
    	{

        /**
		 * 
		 */
		private static final long serialVersionUID = -559548922459735861L;
		private HaarFeature feature;   // Haar feature tested by this classifier
        // threshold, a, and b are scaled by 2**16 = 65536
        private int modThreshold;      // calculated threshold
        private int threshold;         // threshold feature compared with
        private int a, b;              // return result if successor = 0
        private int stdDev;            // the standard deviation of the image,
                                       // scaled by 256
        private int width, height;
        // create from input stream
        // expected data: (hwcs <feature><threshold>,<alpha>)
        public HaarWeakClassifierStump(InputStreamReader isr, int width, int height) 
           throws jjil.core.Error, IOException 
        {
            char[] rC = new char[6];
            readChars(isr,rC, 0, 6);
            if ("(hwcs ".compareTo(new String(rC)) != 0) { //$NON-NLS-1$
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.PARSE_ERROR,
                                new String(rC),
                                "(hwcs ",
                                isr.toString());
            }
            this.feature = new HaarFeature(isr);
            this.threshold = readInt(isr);
            this.a = readInt(isr);
            this.b = readInt(isr);
            this.width = width;
            this.height = height;
        }
        
        public int eval(Gray32Image image) {
            int nHf = this.feature.eval(image) << 12;
            if (nHf < this.modThreshold) { 
                return a;
            } else {
                return b;
            }
        }
        
        public void setWidth(int nWidth) {
            this.feature.setWidth(nWidth);
            // width affects threshold
            setThreshold();
        }
        
        // this should be called whenever the underlying image changes
        // it accepts the standard deviation of the image, multiplied by
        // 256
        public void setStdDev(int stdDev) {
            this.stdDev = stdDev;
            setThreshold();
        }
        
        private void setThreshold() {
            this.modThreshold = ((this.threshold * this.stdDev >> 6)
                    * this.width * this.height) >> 6;
                //    this.threshold * this.stdDev / 256 / 65536;
            
        }
        
        public String toString() {
            return "(hwcs " + this.feature.toString() + //$NON-NLS-1$
                    this.threshold + " " + this.a + " " + this.b + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    this.width + " " + this.height + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    };
    
    // A stump-structured Haar classifier consists of a Haar feature and a
    // threshold. The Haar feature is evaluated. 
    
    public class HaarClassifierStump 
    	implements HaarStageClassifier, Serializable
    	{

        /**
		 * 
		 */
		private static final long serialVersionUID = 8964434533022477157L;
		private HaarWeakClassifierStump[] hwcs;   // Haar feature tested by this classifier
        // theshold is scaled by 2**16 = 65536
        private int threshold;         // threshold feature compared with
        
        // create from stream
        // expected input (hcs <count><HaarClassifierStumpLimb>^count<threshold>)
        public HaarClassifierStump(InputStreamReader isr, int width, int height) 
           throws jjil.core.Error, IOException 
        {
            char[] rC = new char[5];
            readChars(isr,rC, 0, 5);
            if ("(hcs ".compareTo(new String(rC)) != 0) { //$NON-NLS-1$
                throw new Error(
                                Error.PACKAGE.ALGORITHM,
                                ErrorCodes.PARSE_ERROR,
                                new String(rC),
                                "(hcs ",
                                isr.toString());
            }
            int n = readInt(isr);
            this.hwcs = new HaarWeakClassifierStump[n];
            for (int i=0; i<n; i++) {
                this.hwcs[i] = new HaarWeakClassifierStump(isr, width, height);
            }
            this.threshold = readInt(isr);
        }
        
        public boolean eval(Gray32Image image) {         
            int stageSum = 0;
            for (int i=0; i<this.hwcs.length; i++) {
                stageSum += this.hwcs[i].eval(image);
            }
            return (stageSum >= this.threshold);
        }
        
        public void setWidth(int nWidth) {
            for (int i=0; i<this.hwcs.length; i++) {
                this.hwcs[i].setWidth(nWidth);
            }
        }
        
        public void setStdDev(int stdDev) {
            for (int i=0; i<this.hwcs.length; i++) {
                this.hwcs[i].setStdDev(stdDev);
            }
        }
        
        public String toString() {
            String sz =  "(hcs " + this.hwcs.length; //$NON-NLS-1$
            for (int i=0; i<this.hwcs.length; i++) {
                sz += " " + this.hwcs[i].toString(); //$NON-NLS-1$
            }
            sz += " " + this.threshold + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            return sz;
        }
    };

       
        public boolean eval(Image image) throws jjil.core.Error {
            if (!(image instanceof Gray8Image)) {
                 throw new Error(
                                 Error.PACKAGE.ALGORITHM,
                                 ErrorCodes.IMAGE_NOT_GRAY8IMAGE,
                                 image.toString(),
                                 null,
                                 null);
            }
            // calculate the standard deviation of the input mage
            Gray8Statistics gs = new Gray8Statistics();     // for computing standard deviation
            gs.push(image);
            int stdDev = gs.getStdDev();
            int nWidth = image.getWidth();
            if (this.nWidth != nWidth) {
                for (int i=0; i<this.hsc.length; i++) {
                    this.hsc[i].setWidth(nWidth);
                }
            }
            this.nWidth = nWidth;
            // form the cumulative sum of the image
            Gray8QmSum gcs = new Gray8QmSum(); // for forming cumulative sum
            gcs.push(image);
            Gray32Image g32 = (Gray32Image) gcs.getFront();
            for (int i=0; i<this.hsc.length; i++) {
                this.hsc[i].setStdDev(stdDev);
                if (!this.hsc[i].eval(g32)) {
                    return false;
                }
            }
            return true;
        }
    
    private HaarClassifierStump[] hsc;   // Haar feature tested by this classifier

    // create from stream
    // Expected input (hcsb <width> <height> <count><HaarClassifierStump>^count)
    // the '(hcsb ' has already been read before this gets called
    public HaarClassifierStumpBase(InputStreamReader isr) 
       throws jjil.core.Error, IOException 
    {
        /*
        char[] rC = new char[6];
        readChars(isr,rC, 0, 6);
        if ("(hcsb ".compareTo(new String(rC)) != 0) {
            throw new ParseException("Error at " + isr.toString() + 
                    "; read '" + new String(rC) + "'; expected '(hcsb '");
        }
         */
        this.width = readInt(isr);
        this.height = readInt(isr);
        int n = readInt(isr);
        this.hsc = new HaarClassifierStump[n];
        for (int i=0; i<n; i++) {
            this.hsc[i] = new HaarClassifierStump(isr, this.width, this.height);
        }
        n = isr.read();
        if (n == -1) {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.INPUT_TERMINATED_EARLY,
                            isr.toString(),
                            null,
                            null);
        }
        char c = (char) n;
        if (c != ')') {
            throw new Error(
                            Error.PACKAGE.ALGORITHM,
                            ErrorCodes.PARSE_ERROR,
                            new Character(c).toString(),
                            ")",
                            isr.toString());
        }
    }
        
    public String toString() {
        String sz = "(hcsb " + this.width + " " + this.height + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                this.hsc.length;
         for (int i=0; i<this.hsc.length; i++) {
            sz += " " + this.hsc[i].toString(); //$NON-NLS-1$
        }
        sz += ")"; //$NON-NLS-1$
        return sz;
    }
}

