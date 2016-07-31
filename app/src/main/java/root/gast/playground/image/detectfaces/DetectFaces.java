package root.gast.playground.image.detectfaces;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import root.gast.image.ImageCameraView;
import root.gast.image.LogoView;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.widget.ImageView.ScaleType;

public class DetectFaces implements android.hardware.Camera.PreviewCallback
{
    private ImageCameraView mImageView;
    private LogoView mLogoView;
    private int mnLastWidth = 0, mnLastHeight = 0;
    private FaceDetector mFd = null;

    public DetectFaces(ImageCameraView view, LogoView lv)
    {
        mImageView = view;
        mLogoView = lv;
        mImageView.setScaleType(ScaleType.FIT_XY);
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
            // get the input image into a Bitmap 
            Bitmap bmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ALPHA_8);
            Buffer src = ByteBuffer.wrap(data, 0, size.width * size.height);
            bmp.copyPixelsFromBuffer(src);
            if (mnLastWidth != size.width || mnLastHeight != size.height) {
                // recreate the face detector if the image size changed
                mFd = new FaceDetector(size.width, size.height, 1);
            }
            FaceDetector.Face[] faces = {null};
            int nFaces = mFd.findFaces(bmp, faces);
            if (nFaces != 0) {
                PointF midpoint = new PointF();
                faces[0].getMidPoint(midpoint);
                RectF rf = new RectF(midpoint.x - 10, midpoint.y - 10, midpoint.x + 10, midpoint.y + 10);
                mLogoView.setRect(rf);
            }
        }
    }
}
