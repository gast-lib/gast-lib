package jjil.algorithm;

import java.util.HashMap;

public class Ean13Barcode1D {
    /**
     * Description of the barcode layout.
     */
    public static final int LeftDigits = 6; // number of digits in the left half of the barcode
    public static final int RightDigits = 6; // number of digits in the right half of the barcode
    public static final int LeftWidth = 3; // number of elementary bars in the left-side pattern
    public static final int RightWidth = 3; // number of elementary bars in the right-side pattern
    public static final int MidWidth = 5; // number of elementary bars in the middle pattern
    public static final int DigitWidth = 7; // number of elementary bars in a digit
    // the offset to the middle pattern, in elementary bars
    public static final int MidOffset = LeftWidth + LeftDigits*DigitWidth;
    // the offset to the right-side pattern, in elementary bars
    public static final int RightOffset = LeftWidth + 
            (LeftDigits+RightDigits)*DigitWidth + MidWidth;
    // the total number of elementary bars in a UPC barcode
    public static final int TotalWidth = 
            LeftWidth + DigitWidth * LeftDigits + MidWidth + 
            DigitWidth * RightDigits + RightWidth;
    
    // Main procedure, for unit test.
    // input is a string of digits representing a scanned barcode pattern
    // each digit is an image brightness value 0 = black 9 = white
    // the string can be of any length so long as it is at least 95 digits
    // (i.e., has at least 1 digit / barcode stripe)
    public static void main(String[] args) {
        Ean13Barcode1D eb = new Ean13Barcode1D();
        int[] nValues = new int[args[0].length()];
        int nMid = 0;
        for (int i=0; i<args[0].length(); i++) {
            if (!Character.isDigit(args[0].charAt(i))) {
                System.err.println("argument must be a numeric barcode string");
                return;
            }
            nValues[i] = Character.digit(args[0].charAt(i), 10);
            nMid += nValues[i];
        }
        nMid /= nValues.length;
        System.out.println(eb.decodeBarcode(nValues, 0, nValues.length, nMid));
    }
    
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
     * @param nMid -- the threshold
     * @return the 13-digit decoded barcode if one was found, or null if not
     */
    public String decodeBarcode(int[] nValues, int nStart, int nEnd, int nMid) {
        mbSuccess = false;
        // the image has to be at least the width of a barcode
        if (nEnd - nStart < TotalWidth) {
            return null;
        }
        // let's start by turning the image pixels into a bit pattern
        byte[] bStripes = new byte[TotalWidth];
        int nInterpSum = 0;
        int nPixVal = 0, nPixCount = 0;
        int j = 0;
        // compress the image values into an array of width TotalWidth
        // using a Bresenham-like interpolation technique
        for (int i = nStart; i < nEnd; i++) {
            nPixVal += nValues[i];
            nPixCount ++;
            nInterpSum += TotalWidth;
            if (nInterpSum >= nEnd - nStart) {
                bStripes[j++] = (byte) ((nPixVal > nPixCount * nMid) ? 1 : 0);
                nInterpSum -= nEnd - nStart;
                nPixVal = 0;
                nPixCount = 0;
            }
        }
        // skip past the marker on the left
        int nCurr = LeftWidth;
        StringBuilder sbBarcode = new StringBuilder();
        int nLeftParity = 0;
        // decode each digit, detecting the parity of each
        for (int nDigit = 0; nDigit < LeftDigits; nDigit++) {
            int nSum = 0;
            // build an index into digitCodes for this pattern
            for (int l = 0; l < DigitWidth; l++) {
                nSum = nSum * 2 + bStripes[nCurr++];
            }
            if (nDigit == 0) {
                // in EAN-13 the first digit always has odd parity
                if (mhOddLeft.containsKey(nSum)) {
                    sbBarcode.append(mhOddLeft.get(nSum));
                    nLeftParity = 1;
                } else {
                    // the first digit didn't match any of the codes
                    return sbBarcode.toString();
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
        nCurr += MidWidth;
        for (int nDigit = 0; nDigit < RightDigits; nDigit++) {
            int nSum = 0;
            // build an index into digitCodes for this pattern
            for (int l = 0; l < DigitWidth; l++) {
                nSum = nSum * 2 + (1 - bStripes[nCurr++]);
            }
            if (mhOddLeft.containsKey(nSum)) {
                sbBarcode.append(mhOddLeft.get(nSum));
            } else {
                // the first digit didn't match any of the codes
                return sbBarcode.toString();
            }
        }
        String szBarcode = sbBarcode.toString();
        mbSuccess = verifyCheckDigit(szBarcode);
        return szBarcode;
    }

    
    /**
     * Verifies the check digit in a decoded barcode string. Returns true
     * if the check digit passes the verify test.
     *
     * @param digits the barcode to be verified.
     * @return true iff the barcode passes the check digit test.
     */
    private static boolean verifyCheckDigit(String digits) {
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
    
    public boolean wasSuccessful() {
        return mbSuccess;
    }
    
    /**
     * Set to true when barcode passes all tests
     */
    boolean mbSuccess;

    /**
     * Hashmaps for the various digit patterns
     */
    HashMap<Integer, Character> mhOddLeft, mhEvenLeft, mhFirstDigit;
}
