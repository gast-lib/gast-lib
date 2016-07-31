package jjil.android;

import java.util.HashMap;

public class Ean13Barcode1D {
    /**
     * Description of the barcode layout.
     */
    public static final int LEFT_DIGITS = 6; // number of digits in the left half of the barcode
    public static final int RIGHT_DIGITS = 6; // number of digits in the right half of the barcode
    public static final int LEFT_WIDTH = 3; // number of elementary bars in the left-side pattern
    public static final int RIGHT_WIDTH = 3; // number of elementary bars in the right-side pattern
    public static final int MID_WIDTH = 5; // number of elementary bars in the middle pattern
    public static final int DIGIT_WIDTH = 7; // number of elementary bars in a digit
    // the offset to the middle pattern, in elementary bars
    public static final int MID_OFFSET = LEFT_WIDTH + LEFT_DIGITS*DIGIT_WIDTH;
    // the offset to the right-side pattern, in elementary bars
    public static final int RIGHT_OFFSET = LEFT_WIDTH + 
            (LEFT_DIGITS+RIGHT_DIGITS)*DIGIT_WIDTH + MID_WIDTH;
    // the total number of elementary bars in a UPC barcode
    public static final int TOTAL_WIDTH = 
            LEFT_WIDTH + DIGIT_WIDTH * LEFT_DIGITS + MID_WIDTH + 
            DIGIT_WIDTH * RIGHT_DIGITS + RIGHT_WIDTH;
    
    public Ean13Barcode1D() {
        initDigitCodes();
    }
    
    private void initDigitCodes()
    {
        /*  The odd parity left (character set A) barcodes for the ten digits are:
        0 = 3-2-1-1 = 0001101 = 0x0d
        1 = 2-2-2-1 = 0011001 = 0x19
        2 = 2-1-2-2 = 0010011 = 0x13
        3 = 1-4-1-1 = 0111101 = 0x3d
        4 = 1-1-3-2 = 0100011 = 0x23
        5 = 1-2-3-1 = 0110001 = 0x31
        6 = 1-1-1-4 = 0101111 = 0x2f
        7 = 1-3-1-2 = 0111011 = 0x3b
        8 = 1-2-1-3 = 0110111 = 0x37
        9 = 3-1-1-2 = 0001011 = 0x0b
    */
        mhOddLeft = new HashMap<Integer, Character>();
        mhOddLeft.put(0x0d, '0');
        mhOddLeft.put(0x19, '1');
        mhOddLeft.put(0x13, '2');
        mhOddLeft.put(0x3d, '3');
        mhOddLeft.put(0x23, '4');
        mhOddLeft.put(0x31, '5');
        mhOddLeft.put(0x2f, '6');
        mhOddLeft.put(0x3b, '7');
        mhOddLeft.put(0x37, '8');
        mhOddLeft.put(0x0b, '9');
    // The R barcodes for the digits are the 1-complements
    // of the L odd barcodes. But we encode the R digits by 
    // encoding a white bar as 1 and a black bar as 0
    // so we automatically get the 1-complement without
    // while using the same table
     /*  The even parity left (character set B) barcodes for the ten digits are:
        0 = 1-1-2-3 = 0100111 = 0x27
        1 = 1-2-2-2 = 0110011 = 0x33
        2 = 2-2-1-2 = 0011011 = 0x1b
        3 = 1-1-4-1 = 0100001 = 0x21
        4 = 2-3-1-1 = 0011101 = 0x1d
        5 = 1-3-2-1 = 0111001 = 0x39
        6 = 4-1-1-1 = 0000101 = 0x05
        7 = 2-1-3-1 = 0010001 = 0x11
        8 = 3-1-2-1 = 0001001 = 0x09
        9 = 2-1-1-3 = 0010111 = 0x17
    */
        mhEvenLeft = new HashMap<Integer, Character>();
        mhEvenLeft.put(0x27, '0');
        mhEvenLeft.put(0x33, '1');
        mhEvenLeft.put(0x1b, '2');
        mhEvenLeft.put(0x21, '3');
        mhEvenLeft.put(0x1d, '4');
        mhEvenLeft.put(0x39, '5');
        mhEvenLeft.put(0x05, '6');
        mhEvenLeft.put(0x11, '7');
        mhEvenLeft.put(0x09, '8');
        mhEvenLeft.put(0x17, '9');
        /**
         * The first digit is implied based on
         * the parity of the other digits in the
         * left half. Below 1 = odd parity 0 = even
         * 0    111111 = 0x3f
         * 1    110100 = 0x34
         * 2    110010 = 0x32
         * 3    110001 = 0x31
         * 4    101100 = 0x2c
         * 5    100110 = 0x26
         * 6    100011 = 0x23
         * 7    101010 = 0x2a
         * 8    101001 = 0x29
         * 9    100101 = 0x25
         */
        mhFirstDigit = new HashMap<Integer, Character>();
        mhFirstDigit.put(0x3f, '0');
        mhFirstDigit.put(0x34, '1');
        mhFirstDigit.put(0x32, '2');
        mhFirstDigit.put(0x31, '3');
        mhFirstDigit.put(0x2c, '4');
        mhFirstDigit.put(0x26, '5');
        mhFirstDigit.put(0x23, '6');
        mhFirstDigit.put(0x2a, '7');
        mhFirstDigit.put(0x29, '8');
        mhFirstDigit.put(0x25, '9');
    }

    /**
     * Decode an EAN-13 barcode from the image values in nValues, which should be
     * thresholded at the value 'nMid', starting at position nStart and ending
     * at position nEnd
     * @param nValues -- the image values
     * @param nStart -- column to start decoding the barcode
     * @param nEnd -- column to end decoding the barcode
     * @return the 13-digit decoded barcode if one was found, or null if not
     */
    public String decodeBarcode(byte[] bCompressed, int nStart) {      
        int nEnd = nStart + TOTAL_WIDTH;
        // verify that the barcode starts and ends with the right patterns
        if (bCompressed[nStart] != 1 || bCompressed[nStart+1] != 0 || bCompressed[nStart+2] != 1) {
            return null;
        }
        if (bCompressed[nEnd-3] != 1 || bCompressed[nEnd-2] != 0 || bCompressed[nEnd-1] != 1) {
            return null;
        }
        // skip past the marker on the left
        int nCurr = LEFT_WIDTH + nStart;
        StringBuilder sbBarcode = new StringBuilder();
        int nLeftParity = 0;
        // decode each digit, detecting the parity of each
        for (int nDigit = 0; nDigit < LEFT_DIGITS; nDigit++) {
            int nSum = 0;
            // build an index into digitCodes for this pattern
            for (int l = 0; l < DIGIT_WIDTH; l++) {
                nSum = nSum * 2 + bCompressed[nCurr++];
            }
            if (nDigit == 0) {
                // in EAN-13 the first digit always has odd parity
                if (mhOddLeft.containsKey(nSum)) {
                    sbBarcode.append(mhOddLeft.get(nSum));
                    nLeftParity = 1;
                } else {
                    // the first digit didn't match any of the codes
                    return null;
                }
            } else {
                // determine the parity of the digit
                if (mhOddLeft.containsKey(nSum)) {
                    sbBarcode.append(mhOddLeft.get(nSum));
                    nLeftParity = (nLeftParity * 2) + 1;
                } else if (mhEvenLeft.containsKey(nSum)) {
                    sbBarcode.append(mhEvenLeft.get(nSum));
                    nLeftParity = nLeftParity * 2;
                } else {
                    return sbBarcode.toString();
                }
            }
        }
        // check parity and add prefix character
        if (mhFirstDigit.containsKey(nLeftParity)) {
            sbBarcode.insert(0, mhFirstDigit.get(nLeftParity));
        } else {
            return sbBarcode.toString();
        }
        // now do the right side digits
        nCurr += MID_WIDTH;
        for (int nDigit = 0; nDigit < RIGHT_DIGITS; nDigit++) {
            int nSum = 0;
            // build an index into digitCodes for this pattern
            for (int n = 0; n < DIGIT_WIDTH; n++) {
                nSum = nSum * 2 + (1 - bCompressed[nCurr++]);
            }
            if (mhOddLeft.containsKey(nSum)) {
                sbBarcode.append(mhOddLeft.get(nSum));
            } else {
                // the first digit didn't match any of the codes
                return sbBarcode.toString();
            }
        }
        return sbBarcode.toString();
    }

    
    public String searchForBarcode(int[] nValues, CrosshairOverlay co, boolean bHorizontal) {
        // this is the number of pixels we look left and right to determine
        // the local average.
        final int LOCAL_THRESH = 32;

        // compute the cumulative sum of nValues. We use this for local
        // thresholding
        int[] nCumulativeSum = new int[nValues.length];
        nCumulativeSum[0] = nValues[0];
        for (int i = 1; i < nValues.length; i++) {
            nCumulativeSum[i] = nCumulativeSum[i - 1] + nValues[i];
        }
        for (int nPixelsPerBar = 1; nPixelsPerBar < nValues.length / TOTAL_WIDTH; nPixelsPerBar++) {

            int nPixelSum = 0, nPixCount = 0, j = 0;
            byte[] bCompressed = new byte[nValues.length];
            for (int i = 0; i < nValues.length; i++) {
                nPixelSum += nValues[i];
                nPixCount++;
                if (nPixCount == nPixelsPerBar) {
                    int nEnd = Math.min(nValues.length - 1, i + LOCAL_THRESH);
                    int nStart = Math.max(0, i - LOCAL_THRESH);
                    int nPixelValue = nPixelSum / nPixCount;
                    int nLocalAverage = (nCumulativeSum[nEnd] - nCumulativeSum[nStart])
                            / (nEnd - nStart);
                    if (nPixelValue > nLocalAverage) {
                        bCompressed[j++] = 0;
                    } else {
                        bCompressed[j++] = 1;
                    }
                    nPixelSum = 0;
                    nPixCount = 0;
                }
            }
            // EAN-13 barcodes start and end with a black-white-black
            // pattern preceded by white space. This is the bit pattern
            // 00101 = 0x05, encoding white bars as 0 and dark bars as 1
            int nStartCode = (bCompressed[0] << 3) + (bCompressed[1] << 2)
                    + (bCompressed[2] << 1) + (bCompressed[3]);
            for (int i = 2; i < bCompressed.length - TOTAL_WIDTH - 2; i++) {
                nStartCode = ((nStartCode & 0x0f) << 1) + bCompressed[i + 2];
                if (nStartCode == 5) {
                    // found the left code, the right code will code as
                    // 10100 = 0x14 = 20
                    int nEndCode = (bCompressed[i + TOTAL_WIDTH - 3] << 4)
                            + (bCompressed[i + TOTAL_WIDTH - 2] << 3)
                            + (bCompressed[i + TOTAL_WIDTH - 1] << 2)
                            + (bCompressed[i + TOTAL_WIDTH] << 1)
                            + (bCompressed[i + TOTAL_WIDTH + 1]);
                    if (nEndCode == 20) {
                        if (co != null) {
                            if (bHorizontal) {
                                co.setHorizLimits(i*nPixelsPerBar, (i+TOTAL_WIDTH)*nPixelsPerBar);
                            } else {
                                co.setVertLimits(i*nPixelsPerBar, (i+TOTAL_WIDTH)*nPixelsPerBar);
                            }
                        }
                        String szBarcode = decodeBarcode(bCompressed, i);
                        if (szBarcode != null) {
                            return szBarcode;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Verifies the check digit in a decoded barcode string. Returns true
     * if the check digit passes the verify test.
     *
     * @param digits the barcode to be verified.
     * @return true iff the barcode passes the check digit test.
     */
    public static boolean verifyCheckDigit(String digits) {
        if (digits == null || digits.length() != 13) {
            return false;
        }
        // compute check digit
        // add odd digits
        int nOddSum = 0;
        for (int i=1; i<digits.length()-1; i+=2) {
            nOddSum += Character.digit(digits.charAt(i), 10);
        }
        // add even digits
        int nEvenSum = 0;
        for (int i=0;i<digits.length()-1; i+=2) {
            nEvenSum += Character.digit(digits.charAt(i), 10);
        }
        // compute even digit sum * 3 + odd digit sum;
        int nTotal = nOddSum*3 + nEvenSum;
        // check digit is this sum subtracted from the next higher multiple of 10
        int checkDigit = (nTotal/10 + 1) * 10 - nTotal;
        return Character.digit(
                digits.charAt(digits.length()-1), 10) == checkDigit;
    }

    /**
     * Hashmaps for the various digit patterns
     */
    HashMap<Integer, Character> mhOddLeft, mhEvenLeft, mhFirstDigit;
}
