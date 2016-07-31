package jjil.android;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;

public abstract class PreviewCallback implements android.hardware.Camera.PreviewCallback {
    
    public abstract void ProcessFrame(int[][] frame);
    
    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        int imageFormat = arg1.getParameters().getPreviewFormat();
        Size size = arg1.getParameters().getPreviewSize();
        for (int row = 0; row < size.height; row++) {
            for (int col = 0; col < size.width; col++) {
                switch(imageFormat) {
                    case ImageFormat.JPEG:
                        break;
                    
                    case ImageFormat.NV16:
                        break;
                        
                    case ImageFormat.NV21:
                        break;
                        
                    case ImageFormat.RGB_565:
                        break;
                        
                    case ImageFormat.UNKNOWN:
                        break;
                        
                    case ImageFormat.YUY2: // aka YCbCr_422_I
                        /*
                         * The first question, however, is more difficult. The only specs I've 
                         * been able to find on the 422 is that it's essentially 4 bytes of data 
                         * with 2 pixels in it (equating to 6 rgb bytes.  To "decompress" the 
                         * data use the following algorithms. 
                         * U  = yuv[0] 
                         * Y1 = yuv[1] 
                         * V  = yuv[2] 
                         * Y2 = yuv[3] 
                         * Then use equation to convert YUV to RGB is (remember to apply it to 
                         * both Y1 and Y2): 
                         * R = Y + 1.140V 
                         * G = y - (0.395 * U) - (0.581 * V) 
                         * B = Y + (2.032 * U) 
                         */
                        break;
                        
                    case ImageFormat.YV12:
                        break;
                }
            }
        }
    }

}
