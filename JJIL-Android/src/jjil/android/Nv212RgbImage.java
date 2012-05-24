package jjil.android;

import jjil.core.RgbImage;

public class Nv212RgbImage {
    public static RgbImage getRgbImage(byte[] data, int width, int height) {
        RgbImage rgb = new RgbImage(width, height);
        int nVuOffset = width * height;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int nY = 0xff & data[i * width + j];
                int nV = 0xff & data[nVuOffset + (i / 2) * width + (j / 2) * 2];
                int nU = 0xff & data[nVuOffset + (i / 2) * width + (j / 2) * 2
                        + 1];
                // AndroidColors.yuv2Color does exactly the right thing to
                // convert
                // into an RgbImage color
                rgb.getData()[i * width + j] = AndroidColors.yuv2Color(nY, nU,
                        nV);
            }
        }
        return rgb;
    }

    /**
     * Returns a reduced size RGB image by averaging every 2x2 Y block and
     * applying the corresponding VU pixels to get the color.
     * @param data -- NV21 image
     * @param width -- width of image
     * @param height -- height of image
     * @return a (width/2)x(height/2) RgbImage
     */
    public static RgbImage getRgbImageReduced(byte[] data, int width, int height) {
        RgbImage rgb = new RgbImage(width / 2, height / 2);
        int nVuOffset = width * height;
        for (int i = 0; i < height; i += 2) {
            for (int j = 0; j < width; j += 2) {
                int nY = 0xff & data[i * width + j];
                nY += 0xff & data[i * width + j + 1];
                nY += 0xff & data[(i + 1) * width + j];
                nY += 0xff & data[(i + 1) * width + j + 1];
                nY /= 4;
                int nV = 0xff & data[nVuOffset + (i / 2) * width + j];
                int nU = 0xff & data[nVuOffset + (i / 2) * width + j + 1];
                // AndroidColors.yuv2Color does exactly the right thing to
                // convert
                // into an RgbImage color
                rgb.getData()[i / 2 * width / 2 + j / 2] = AndroidColors.yuv2Color(
                        nY, nU, nV);
            }
        }
        return rgb;
    }
}
