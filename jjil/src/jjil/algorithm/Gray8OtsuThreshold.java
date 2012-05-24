package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.MathPlus;
import jjil.core.PipelineStage;

/**
 * Choose and apply a threshold using Otsu's algorithm, which searches for a
 * threshold value based on best separation of the histogram into two
 * components.
 * <p>
 * Algorithm from
 * http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MORSE/threshold.pdf
 * 
 * @author webb
 * 
 */
public class Gray8OtsuThreshold extends PipelineStage {
	private boolean mbSmaller;
	private int mnAdjustFactor;
	private int mnCountBelow, mnCountAbove;

	/**
	 * Create new instances of Gray8OtsuThreshold, specifying whether pixels
	 * less than the threshold value will be considered to be "on"
	 * (bSmaller==true) or "off" (bSmaller==false).
	 * 
	 * @param bSmaller if bSmaller is true the direction of the threshold is
	 * chosen so that the smaller number of pixels will be set on (Byte.MAX_VALUE)
	 * and the reverse others.
	 */
	public Gray8OtsuThreshold(boolean bSmaller, int nAdjustFactor) {
		this.mbSmaller = bSmaller;
		this.mnAdjustFactor = nAdjustFactor;
	}

	/**
	 * Compute the Ostu threshold on an input Gray8Image and apply it, replacing
	 * the input. The result is a Gray8Image with all pixels set to
	 * Byte.MIN_VALUE or Byte.MAX_VALUE.
	 * 
	 * @param imageInput
	 *            the input Gray8Image. This image is overwritten on output.
	 */
	public void push(Image imageInput) throws Error {
		if (!(imageInput instanceof Gray8Image)) {
			throw new Error(Error.PACKAGE.ALGORITHM,
					ErrorCodes.IMAGE_NOT_GRAY8IMAGE, imageInput.toString(),
					null, null);
		}
		Gray8Image g8i = (Gray8Image) imageInput;
		/* compute histogram */
		int[] rnHistogram = Gray8Hist.computeHistogram(g8i);
		/* calculate Otsu threshold */
		int nThresh = calculateOtsuThreshold(rnHistogram);
		// determine whether small pixel values should get set on (bWithin = true)
		// or not
		boolean bWithin = this.mbSmaller == (this.mnCountBelow < this.mnCountAbove);
		if (bWithin) {
			nThresh = (nThresh * this.mnAdjustFactor) / 256;
		} else {
			nThresh = 256 - ((256 - nThresh) * this.mnAdjustFactor) / 256;
		}
//		if (bWithin) {
//			nThresh = (nThresh * 256) / this.mnBias;
//		} else {
//			nThresh = (nThresh * this.mnBias) / 256;
//		}
		// Gray8Threshold test is on byte value so we adjust
		// appropriately. The histogram value runs from 0-256 so
		// we have to offset it by Byte.MIN_VALUE.
		// Gray8Threshold replaces its input.
		Gray8Threshold g8t = new Gray8Threshold(nThresh + Byte.MIN_VALUE,
				bWithin);
		g8t.push(imageInput);
		super.setOutput(g8t.getFront());
	}

	/**
	 * Calculate the Otsu threshold. Also compute the number of pixels
	 * below and above the threshold value.
	 * 
	 * @param rnHistogram
	 *            the input histogram, or any array with similar structure
	 * @return an integer from 0-rnHistogram.length-1 which divides the array
	 *         into two maximally disjoint components, as computed by Otsu's
	 *         algorithm
	 */
	public int calculateOtsuThreshold(int[] rnHistogram) {
		int nBelow = 0;
		int nPixelSum = 0;
		int nAbove = 0;
		for (int i = 0; i < rnHistogram.length; i++) {
			nAbove += rnHistogram[i];
			nPixelSum += rnHistogram[i] * i;
		}
		// lSumBelow and lSumAbove are the sum of all pixel values above
		// and below the threshold
		long lSumBelow = 0;
		long lSumAbove = nPixelSum;
		int nBestThreshold = 0;
		long lBestSeparation = Long.MIN_VALUE;
		for (int i = 0; i < rnHistogram.length; i++) {
			// new count of pixels above and below the threshold
			nBelow = nBelow + rnHistogram[i];
			nAbove = nAbove - rnHistogram[i];
			// compute new means above and below threshold
			lSumBelow += rnHistogram[i] * i;
			int nMeanBelow, nMeanAbove;
			if (nBelow != 0) {
				nMeanBelow = (int) (lSumBelow / nBelow);
			} else {
				nMeanBelow = 0;
			}
			lSumAbove -= rnHistogram[i] * i;
			if (nAbove != 0) {
				nMeanAbove = (int) (lSumAbove / nAbove);
			} else {
				nMeanAbove = 0;
			}
			// compute variance with this separation (see article above)
			long lVariance = nBelow * nAbove * (long) (nMeanAbove - nMeanBelow)
					* (long) (nMeanAbove - nMeanBelow);
			// if this separation is better than previous update
			if (lVariance > lBestSeparation) {
				lBestSeparation = lVariance;
				nBestThreshold = i;
				this.mnCountBelow = nBelow;
				this.mnCountAbove = nAbove;
			}
		}
		return nBestThreshold;

	}
}
