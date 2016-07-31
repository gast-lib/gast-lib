/*
 * Copyright 2011 Jon A. Webb
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.image.detectlogobetter;

import jjil.algorithm.Gray8ConnComp;
import jjil.algorithm.Gray8HistEq;
import jjil.algorithm.Gray8Reduce;
import jjil.algorithm.Gray8Rgb;
import jjil.algorithm.Gray8Threshold;
import jjil.android.DebugImage;
import jjil.android.Nv212RgbImage;
import jjil.android.RgbImageAndroid;
import jjil.core.Error;
import jjil.core.Image;
import jjil.core.PipelineStage;
import jjil.core.Rect;
import jjil.core.RgbImage;
import jjil.core.Sequence;
import root.gast.image.ImageCameraView;
import root.gast.image.LogoView;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.widget.ImageView.ScaleType;

public class DetectLogo implements android.hardware.Camera.PreviewCallback
{
    private static final int MIN_LOGO_SIZE = 50;

    private FindBrightestPoint mFbp;
    private Gray8ConnComp mG8cc;
    private PipelineStage mDisplay;
    private LogoView mLogoView;
    private RgbAbsDiffGrayWb mRadg;
    private Sequence mSeqThreshold;
    private ImageCameraView mImageView;

    public DetectLogo(ImageCameraView view, LogoView lv) throws jjil.core.Error
    {
        mImageView = view;
        mLogoView = lv;
        mImageView.setScaleType(ScaleType.FIT_XY);
        // calculate the absolute value of the difference between the RGB value
        // and magenta
        // a small number here corresponds to a greenish pixel
        mRadg = new RgbAbsDiffGrayWb(Color.GREEN);
        // then we reduce the image size
        Gray8Reduce g8r;
        g8r = new Gray8Reduce(2, 2);
        // then pass all pixels less than -96
        Gray8Threshold g8t = new Gray8Threshold(-96, true);
        // Now build the pipeline
        mSeqThreshold = new Sequence(mRadg);
        mSeqThreshold.add(g8r);
        mSeqThreshold.add(new Gray8HistEq());
        mSeqThreshold.add(g8t);
        mG8cc = new Gray8ConnComp();
        // for measuring the brightest pixel in the scene
        // we sample every 8th pixel horizontally and vertically
        mFbp = new FindBrightestPoint(8, 8);
        // for converting thresholded output for display
        // we have to model the rotation and mirroring applied to
        // the image when it is oriented with Camera.setDisplayOrientation()
        mDisplay = new Gray8Rgb();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        Parameters cameraParameters = camera.getParameters();
        int imageFormat = cameraParameters.getPreviewFormat();
        // we only know how to process NV21 format (the default format)
        if (imageFormat == ImageFormat.NV21)
        {
            Size size = camera.getParameters().getPreviewSize();
            /**
             * for debugging purposes, allow the current image to be saved and
             * reused
             */
            boolean bWrite = false, bRead = false;
            Integer width = size.width, height = size.height;
            if (bWrite)
            {
                DebugImage.writeNv21Image(data, width, height, "logo.png");
            }
            if (bRead)
            {
                DebugImage.Nv21Image nv21Image = DebugImage
                        .readImage2Nv21("logo.png");
                width = nv21Image.getWidth();
                height = nv21Image.getHeight();
                data = nv21Image.getData();
            }
            // first convert to an RGB image for processing
            RgbImage rgb = Nv212RgbImage
                    .getRgbImageReduced(data, width, height);
            // get the brightest pixel for white balance
            mFbp.push(rgb);
            int nBrightestColor = mFbp.getBrightestColor();
            // pass the white balance color
            mRadg.setWhiteBalance(nBrightestColor);
            // next push the rgb image into the pre-built pipeline
            try
            {
                mSeqThreshold.push(rgb);
                Image imThresholded = mSeqThreshold.getFront();
                // no need to make a copy here, neither PipelineStage
                // modifies its input
                mDisplay.push(imThresholded);
                // show the thresholded image
                RgbImage imDisplay = (RgbImage) mDisplay.getFront();
                mImageView.setImageBitmap(RgbImageAndroid.toBitmap(imDisplay));
                mImageView.invalidate();
                mG8cc.push(imThresholded);
                // calculate connected components. We make this a member
                // variable
                // so we can access the connected components
                // take the largest connected component
                int nBestCompactness = Integer.MAX_VALUE, nBestComponent = -1;
                for (int i = 0; i < mG8cc.getComponentCount()
                        && mG8cc.getPixelCount(i) > MIN_LOGO_SIZE; i++)
                {
                    int perimeter = mG8cc.getPerimeter(i);
                    int area = mG8cc.getPixelCount(i);
                    int compactness = perimeter * perimeter / area;
                    if (compactness < nBestCompactness)
                    {
                        nBestCompactness = compactness;
                        nBestComponent = i;
                    }
                }
                if (nBestComponent != -1)
                {
                    Rect r = mG8cc.getComponent(nBestComponent);
                    RectF rf = new RectF(r.getLeft(), r.getTop(), r.getRight(), r.getBottom());
                    mLogoView.setRect(rf);
                } else {
                    mLogoView.clearRect();
                }
            } catch (Error e)
            {
            }
        }
    }
}
