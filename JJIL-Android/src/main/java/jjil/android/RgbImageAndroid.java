package jjil.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jjil.core.RgbImage;
import android.content.Context;
import android.graphics.Bitmap;

public class RgbImageAndroid  {
	/**
	 * The sole way to create an RgbImage from an image captured from the camera.
	 * The parameters are the pointer to the byte data passed to the JPEG picture
	 * callback and the width and height image you want. You must reduce the
	 * image size because otherwise you will run out of memory. Width and height
	 * reduction by a factor of 2 works on the GPhone.<p>
	 * Ex. usage<p>
	 * public void onPictureTaken(byte [] jpegData, android.hardware.Camera camera) {
     *  	RgbImage rgb = RgbImageAndroid.toRgbImage(jpegData, 
     *  			camera.getParameters().getPictureSize().width/2,
     *  			camera.getParameters().getPictureSize().height/2);
     * }
	 * @param jpegData image data supplied to JpegPictureCallback
	 * @param nWidth target width image to return
	 * @param nHeight target height image to return
	 * @return RgbImage initialized with the image from the camera.
	 */
    static public RgbImage toRgbImage(Bitmap bmp) {
    	int nWidth = bmp.getWidth();
    	int nHeight = bmp.getHeight();
    	RgbImage rgb = new RgbImage(nWidth, nHeight);
    	bmp.getPixels(rgb.getData(), 0, nWidth, 0, 0, nWidth, nHeight);
    	return rgb;
    }

    static public Bitmap toBitmap(RgbImage rgb)
    {
    	return Bitmap.createBitmap(
    			rgb.getData(), 
    			rgb.getWidth(), 
    			rgb.getHeight(), 
    			Bitmap.Config.ARGB_8888);
    }
    
    static public void toFile(Context context, RgbImage rgb, int nQuality, String szPath) 
    	throws IOException
    {
     	OutputStream os = new FileOutputStream(szPath);
     	try {
	     	Bitmap bmp = toBitmap(rgb);
	     	Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
	     	szPath = szPath.toLowerCase();
	     	if (szPath.endsWith("jpg") || szPath.endsWith("jpeg")) { //$NON-NLS-1$ //$NON-NLS-2$
     			format = Bitmap.CompressFormat.JPEG;
	     	} else if (szPath.endsWith("png")) { //$NON-NLS-1$
     			format = Bitmap.CompressFormat.PNG;
     		}
	     	bmp.compress(format, nQuality, os);
     	} finally {
     		os.close();
     	}
    }
}
