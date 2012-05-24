package jjil.android;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

public class ReadBarcode implements android.hardware.Camera.PreviewCallback,
        android.hardware.Camera.AutoFocusCallback {
    
    private static final String TAG = "ReadBarcode";
    
    private Handler handler = new Handler();
    
    public ReadBarcode(double dPerpPos, TextView tv, CheckBox ck, CrosshairOverlay co) {
        me13b = new Ean13Barcode1D();
        mdBarcodePerpPos = dPerpPos;
        mTextViewResult = tv;
        mCheckBoxResult = ck;
        mCrosshairOverlay = co;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.d(TAG, "on auto focus " + success);
        if (!success) {
            // try again

            //code in the book was:
//            camera.autoFocus(this);
            autoFocusLater(camera);
        } else {
            Log.d(TAG, "reset mnFocus");
            mnFocused = 15;
        }
    }
    
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Parameters cameraParameters = camera.getParameters();
    	String focusMode = camera.getParameters().getFocusMode();
        boolean bUseAutoFocus =  focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
    			focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO);
        if (bUseAutoFocus && (mnFocused == 0 || mbFoundBarcode)) {
            Log.d(TAG, "exit, mnFocused is 0 or mbFoundBarcode is "
                    + mbFoundBarcode + " use auto " + bUseAutoFocus);
            return;
        }
        try {
            int imageFormat = cameraParameters.getPreviewFormat();
            // we only know how to process NV21 format (the default format)
            if (imageFormat == ImageFormat.NV21) {
                /**
                 * NV21 consists of an 8-bit Y (intensity) plane followed by an
                 * interleaved subsampled U/V (hue) plane. We don't care about
                 * color in this app so we just read the intensity plane
                 */
                Size size = camera.getParameters().getPreviewSize();
                /**
                 * for debugging purposes, allow the current image to be saved
                 * and reused
                 */
                boolean bWrite = false, bRead = false;
                Integer width = size.width, height = size.height;
                if (bWrite) {
                    DebugImage.writeGrayImage(data, width, height,
                            "barcode.png");
                }
                if (bRead) {
                    DebugImage.Nv21Image nv21Image = DebugImage.readGrayImage("barcode.png");
                    data = nv21Image.getData();
                    width = nv21Image.getWidth();
                    height = nv21Image.getHeight();
                }
                int[] nValues;
                int nRow, nCol;
                // First search for the barcode columnwise
                nRow = (int) (mdBarcodePerpPos * height);
                int nStartCol = 0;
                int nEndCol = width;
                nValues = new int[nEndCol - nStartCol];
                int nRowOffset = width * nRow;
                /**
                 * Byte values run from -128 (0x80) to + 127 (0x7f) but image
                 * values run from 0x00 to 0xff. It will be easier to do the
                 * math if we convert to integer. We also compute the max and
                 * min value to use in thresholding
                 */
                int i = 0;
                for (nCol = nStartCol; nCol < nEndCol; nCol++) {
                    int nValue = 0xff & (int) data[nRowOffset + nCol];
                    nValues[i++] = nValue;
                }
                String szBarcode = me13b.searchForBarcode(nValues, mCrosshairOverlay, true);
                // show the barcode and exit
                if (szBarcode != null && mTextViewResult != null) {
                    mTextViewResult.setText(szBarcode);
                    mbFoundBarcode = Ean13Barcode1D.verifyCheckDigit(szBarcode);
                    mCheckBoxResult.setChecked(mbFoundBarcode);
                    return;
                }
                // now search rowwsize
                nCol = (int) (mdBarcodePerpPos * width);
                int nStartRow = 0;
                int nEndRow = height;
                nValues = new int[nEndRow - nStartRow];
                int nColOffset = nCol;
                /**
                 * Byte values run from -128 (0x80) to + 127 (0x7f) but image
                 * values run from 0x00 to 0xff. It will be easier to do the
                 * math if we convert to integer. We also compute the max and
                 * min value to use in thresholding
                 */
                i = 0;
                for (nRow = nStartRow; nRow < nEndRow; nRow++) {
                    int nValue = 0xff & (int) data[nColOffset + nRow
                            * width];
                    nValues[i++] = nValue;
                }
                szBarcode = me13b.searchForBarcode(nValues, mCrosshairOverlay, false);
                if (mTextViewResult != null && szBarcode != null) {
                    mTextViewResult.setText(szBarcode);
                    mbFoundBarcode = Ean13Barcode1D.verifyCheckDigit(szBarcode);
                    mCheckBoxResult.setChecked(mbFoundBarcode);
                    Log.d(TAG, "bar code");
                    return;
                }
                else
                {
                    Log.d(TAG, "no bar code");
                }
            }
        } finally {
            if (bUseAutoFocus && (--mnFocused == 0 && !mbFoundBarcode)) {
                Log.d(TAG, "refocusing");
                //code in the book was:
                //camera.autoFocus(this);
                autoFocusLater(camera);
            }
        }
    }

    /**
     * useful method for starting auto focus not too soon
     */
    public void autoFocusLater(final Camera camera)
    {
        final ReadBarcode finalContext = this;
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    camera.autoFocus(finalContext);
                }
                catch (RuntimeException e)
                {
                    Log.d(TAG, "error focusing, camera may be closing");
                }
            }
        }, 100);
    }
        
    /**
     * mbFocused is true when the camera has successfully autofocused
     */
    int mnFocused = 0;

    /**
     * 1D Barcode decoder
     */
    Ean13Barcode1D me13b;

    /**
     * The expected position of the barcode, measured perpendicular
     * to the orientation of the barcode, from 0-1
     */
    double mdBarcodePerpPos;
    
    private boolean mbFoundBarcode;
    private TextView mTextViewResult;
    private CheckBox mCheckBoxResult;
    private CrosshairOverlay mCrosshairOverlay;

}
