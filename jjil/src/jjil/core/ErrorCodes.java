package jjil.core;

/**
 * Define error codes specific to the jjil.core library. Other libraries
 * define their own codes in their own ErrorCodes object.<p>
 * J2ME's version of Java doesn't support enums. So we simulate them
 * using an old trick...
 */
public class ErrorCodes {
    /**
     * Input bounds outside input image.
     */
    public static final int BOUNDS_OUTSIDE_IMAGE = 0;		// 
    /**
     * Random parameter value was out of range
     */
        public static final int ILLEGAL_PARAMETER_VALUE = 
                BOUNDS_OUTSIDE_IMAGE + 1;	
    /**
     * image and mask sizes don't match
     */
        public static final int IMAGE_MASK_SIZE_MISMATCH = 
                ILLEGAL_PARAMETER_VALUE + 1;	
    /**
     * attempt to divide by zero
     */
        public static final int MATH_DIVISION_ZERO = 
        	IMAGE_MASK_SIZE_MISMATCH + 1;				
    /**
     * attempt to take sqrt of negative
     */
        public static final int MATH_NEGATIVE_SQRT = 
        	MATH_DIVISION_ZERO + 1;						
    /**
     * operands too large to multiply
     */
        public static final int MATH_PRODUCT_TOO_LARGE = 
        	MATH_NEGATIVE_SQRT + 1;						
    /**
     * operand too large to take square
     */
        public static final int MATH_SQUARE_TOO_LARGE = 
        	MATH_PRODUCT_TOO_LARGE + 1;					
    /**
     * No result was available when it was requested.
     */
        public static final int NO_RESULT_AVAILABLE = 
        	MATH_SQUARE_TOO_LARGE + 1;					
    /**
     * pipeline empty when image is being pushed
     */
        public static final int PIPELINE_EMPTY_PUSH = 
        	NO_RESULT_AVAILABLE + 1;					
        
    /**
     * Count of error codes.
     */
        public static final int COUNT = PIPELINE_EMPTY_PUSH + 1;
}
