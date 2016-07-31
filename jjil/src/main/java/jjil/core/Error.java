/**
 * Error defines a common error-reporting mechanism for all JJIL classes.
 * It includes an error code and up to three string objects representing
 * objects that explain the error, for example file names or images.
 * 
 * Build-specific libraries like jjil.android or jjil.j2se will define
 * a Error.toString() class that converts the Error object into a localized
 * error message.
 * 
 * 
 */
package jjil.core;

/**
 * Error defines a common error-reporting mechanism for all JJIL classes.
 * It includes an error code and up to three string objects representing
 * objects that explain the error, for example file names or images.
 * @author webb
 *
 */
public class Error extends Throwable {
	
        /**
         * J2ME's Java is only 1.4 so no enums. We must simulate them...
         */
	public static class PACKAGE {
        /**
         * Error code is defined in jjil.algorithm package.
         */
            public static final int ALGORITHM = 0;
        /**
         * Error code is defined in jjil.android package.
         */
            public static final int ANDROID = ALGORITHM + 1;
        /**
         * Error code is defined in jjil.core package.
         */
            public static final int CORE = ANDROID + 1;
        /**
         * Error code is defined in jjil.j2me package.
         */
            public static final int J2ME = CORE + 1;
        /**
         * Error code is defined in jjil.j2se package.
         */
            public static final int J2SE = J2ME + 1;
            
        /**
         * Count of packages.
         */
            public static final int COUNT = J2SE + 1;
	}
	
	
	/**
	 * nCode is a general error code. Possible values are defined in the CODES enumerated
	 * type (really, we use ints for compatibility with J2ME).
	 */
	private int nCode;
	
	/**
	 * The package where the error code is defined.
	 */
	private int nPackage;
	
	
	/**
	 * szParam1 is a primary parameter giving detailed error information.
	 */
	private String szParam1;
	/**
	 * szParam2 is a secondary parameter giving detailed error information.
	 */
	private String szParam2;
	/**
	 * szParam3 is a tertiary parameter giving detailed error information.
	 */
	private String szParam3;
        
        /**
     * Copy constructor.
     * @param e Error object to copy.
     */
        public Error(Error e) {
            this.nPackage = e.getPackage();
            this.nCode = e.getCode();
            this.szParam1 = e.getParam1();
            this.szParam2 = e.getParam2();
            this.szParam3 = e.getParam3();
        }
	
	/**
     * This is how Error objects are created. The first two parameters determine
     * the specific type of error. The other parameters give information about
     * the objects causing the error.
     * @param nPackage package where error code is defined.
     * @param nCode : the error code
     * @param szParam1 : a first parameter giving detailed information
     * @param szParam2 : a second parameter giving detailed information
     * @param szParam3 : a third parameter giving detailed information
     */
	public Error(
			int nPackage,
			int nCode, 
			String szParam1, 
			String szParam2, 
			String szParam3) {
		this.nPackage = nPackage;
		this.nCode = nCode;
		this.szParam1 = szParam1;
		this.szParam2 = szParam2;
		this.szParam3 = szParam3;
	}

    /**
     * 
     * @return the error code.
     */
	public int getCode() {
		return this.nCode;
	}
	
    /**
     * 
     * @return the package where the error code is defined.
     */
	public int getPackage() {
		return this.nPackage;
	}
	
    /**
     * 
     * @return first parameter describing error.
     */
	public String getParam1() {
		return this.szParam1;
	}
	
    /**
     * 
     * @return second parameter describing error.
     */
	public String getParam2() {
		return this.szParam2;
	}
	
    /**
     * 
     * @return third parameter describing error.
     */
	public String getParam3() {
		return this.szParam3;
	}
        
    /**
     * 
     * @return String including all parameters describing error.
     */
    protected String parameters() {
        String sz = "(";
        if (this.getParam1() != null) {
            sz += this.getParam1();
        }
        sz += ",";
        if (this.getParam2() != null) {
            sz += this.getParam2();
        }
        sz += ",";
        if (this.getParam3() != null) {
            sz += this.getParam3();
        }
        sz += ")";
        return sz;
    }

    /**
     * 
     * @return String describing this instance of Error.
     */
    public String toString() {
            return new Integer(this.nPackage).toString() + " " +
                    new Integer(this.nCode).toString() + 
                    parameters();
        }
}
