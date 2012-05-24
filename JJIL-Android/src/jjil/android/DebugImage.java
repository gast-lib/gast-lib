package jjil.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jjil.core.RgbVal;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;

public class DebugImage {
    public static class Nv21Image {
        public Nv21Image(byte[] data, int width, int height) {
            this.mData = data;
            this.mWidth = width;
            this.mHeight = height;
        }
        
        public byte[] getData() {
            return mData;
        }
        
        public int getHeight() {
            return mHeight;
        }
        
        public int getWidth() {
            return mWidth;
        }
        
        private byte[] mData;
        private int mHeight, mWidth;
    }
    public static Nv21Image readGrayImage(String szFilename) {
        Bitmap bmp = readBitmap(szFilename);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte[] bResult = new byte[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // turn the color image to gray
                int pix = bmp.getPixel(j, i);
                int r = (pix & 0x00ff0000) >> 16;
                int g = (pix & 0x0000ff00) >> 8;
                int b = (pix & 0x000000ff);
                int gray = (r + g + b) / 3;
                bResult[i * width + j] = RgbVal.unsignedIntToSignedByte[gray];
            }
        }
        return new Nv21Image(bResult, width, height);
    }

    public static Nv21Image readImage2Nv21(String szFilename) {
        Bitmap bmp = readBitmap(szFilename);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte[] bResult = new byte[width * height + width * height / 2];
        int nVuOffset = width * height;
        // we compute the VU value on each 4 pixel block and the gray
        // value on every pixel
        for (int i = 0; i < height; i += 2) {
            for (int j = 0; j < width; j += 2) {
                int pix = bmp.getPixel(j, i);
                int r = (pix & 0x00ff0000) >> 16;
                int g = (pix & 0x0000ff00) >> 8;
                int b = (pix & 0x000000ff);
                int nSumR = r;
                int nSumG = g;
                int nSumB = b;
                int nGray = (r + g + b) / 3;
                bResult[i * width + j] = RgbVal.unsignedIntToSignedByte[nGray];
                pix = bmp.getPixel(j + 1, i);
                r = (pix & 0x00ff0000) >> 16;
                g = (pix & 0x0000ff00) >> 8;
                b = (pix & 0x000000ff);
                nSumR += r;
                nSumG += g;
                nSumB += b;
                nGray = (r + g + b) / 3;
                bResult[i * width + j + 1] = RgbVal.unsignedIntToSignedByte[nGray];
                pix = bmp.getPixel(j, i + 1);
                r = (pix & 0x00ff0000) >> 16;
                g = (pix & 0x0000ff00) >> 8;
                b = (pix & 0x000000ff);
                nSumR += r;
                nSumG += g;
                nSumB += b;
                nGray = (r + g + b) / 3;
                bResult[(i + 1) * width + j] = RgbVal.unsignedIntToSignedByte[nGray];
                pix = bmp.getPixel(j + 1, i + 1);
                r = (pix & 0x00ff0000) >> 16;
                g = (pix & 0x0000ff00) >> 8;
                b = (pix & 0x000000ff);
                nSumR += r;
                nSumG += g;
                nSumB += b;
                nGray = (r + g + b) / 3;
                bResult[(i + 1) * width + j + 1] = RgbVal.unsignedIntToSignedByte[nGray];
                // now compute the UV value for the 4-pixel block
                int nYUV = AndroidColors.rgb2yuv(nSumR / 4, nSumG / 4,
                        nSumB / 4);
                bResult[nVuOffset + i / 2 * width + j] = RgbVal.unsignedIntToSignedByte[nYUV & 0x000000ff];
                bResult[nVuOffset + i / 2 * width + j + 1] = RgbVal.unsignedIntToSignedByte[(nYUV & 0x0000ff00) >> 8];
            }
        }
        return new Nv21Image(bResult, width, height);
    }

    private static Bitmap readBitmap(String szFilename) {
        File path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, szFilename);
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    private static boolean writeBitmap(String szFilename, Bitmap bmp) {
        try {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            path.mkdirs();
            File file = new File(path, szFilename);
            OutputStream os = new FileOutputStream(file);
            if (szFilename.toLowerCase().endsWith(".jpg")) {
                bmp.compress(CompressFormat.JPEG, 100, os);
            } else {
                bmp.compress(CompressFormat.PNG, 100, os);
            }
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean writeGrayImage(byte[] bImageData, int width,
            int height, String szFilename) {
        Bitmap bmp = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] nImageData = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int nValue = 0xff & bImageData[i];
            nImageData[i] = Color.argb(0xff, nValue, nValue, nValue);
        }
        bmp.setPixels(nImageData, 0, width, 0, 0, width, height);
        return writeBitmap(szFilename, bmp);
    }

    public static boolean writeNv21Image(byte[] bImageData, int width,
            int height, String szFilename) {
        Bitmap bmp = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] nImageData = new int[width * height];
        int nVuOffset = width * height;
        for (int i = 0; i < height; i++) {
            // we produce two pixels for each VU pair in the color plane
            for (int j = 0; j < width; j += 2) {
                int nY = 0xff & bImageData[i * width + j];
                int nV = 0xff & bImageData[nVuOffset + (i / 2) * width + j];
                int nU = 0xff & bImageData[nVuOffset + (i / 2) * width + j + 1];
                nImageData[i * width + j] = AndroidColors.yuv2Color(nY, nU, nV);
                nY = 0xff & bImageData[i * width + j + 1];
                nImageData[i * width + j + 1] = AndroidColors.yuv2Color(nY, nU,
                        nV);
            }
        }
        bmp.setPixels(nImageData, 0, width, 0, 0, width, height);
        return writeBitmap(szFilename, bmp);
    }
}
