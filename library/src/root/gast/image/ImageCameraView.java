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
package root.gast.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Displays an image while tracking the camera display orientation.
 * 
 * @author Jon A. Webb &#60;<a
 *         href="mailto:jonawebb@gmail.com">jonawebb@gmail.com</a>&#62;
 * 
 */
public class ImageCameraView extends ImageView
{
    private boolean mbMirror = false;
    private int mnDisplayOrientation = 0;
    private ScaleType mScaleType;

    public ImageCameraView(Context context)
    {
        super(context);
    }

    public ImageCameraView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ImageCameraView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setCameraDisplayCharacteristics(int nCameraFacing,
            int nDisplayOrientation)
    {
        mbMirror = nCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mnDisplayOrientation = nDisplayOrientation;
    }

    /**
     * In the override we apply the camera transformation and map the image to
     * the display.
     * 
     * @see android.widget.ImageView#setImageBitmap(android.graphics.Bitmap)
     */
    @Override
    public void setImageBitmap(Bitmap bitmap)
    {
        Matrix matrix = new Matrix();
        matrix.reset();
        // apply the camera display characteristics. First mirror the image...
        if (mbMirror)
        {
            matrix.postScale(-1f, 1f);
        }
        // then rotate it...
        matrix.postRotate(mnDisplayOrientation);
        /**
         * Math is hard! 
         *         -- Barbie
         * 
         * We could calculate the matrix that will map the rotated and possibly
         * mirrored image to the display, but it is much easier to compose a
         * matrix that gets the width and height right, and then translate it so
         * the top left corner maps to (0,0). This will correctly fit the image
         * to the display.
         * 
         * Rotated image width and height must match display width and height
         * see where the image rectangle maps now.
         */
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrix.mapRect(rect);
        switch (mScaleType)
        {
            case FIT_XY:
                // get the width and height of the image right
                matrix.postScale(this.getWidth() / rect.width(),
                        this.getHeight() / rect.height());
                break;
            case CENTER_CROP:
            /*
             * "Scale the image uniformly (maintain the image's aspect ratio) so
             * that both dimensions (width and height) of the image will be
             * equal to or LARGER than the corresponding dimension of the view
             * (minus padding)." (Android documentation)
             */
            {
                float scale = Math.max(this.getWidth() / rect.width(),
                        this.getHeight() / rect.height());
                matrix.postScale(scale, scale);
                break;
            }
            case CENTER:
                // everything is done below in if statement
                // translate center of image to center of display
                break;
            case CENTER_INSIDE:
            case FIT_START:
            case FIT_END:
            case FIT_CENTER:
            {
                /**
                 * "Scale the image uniformly (maintain the image's aspect
                 * ratio) so that both dimensions (width and height) of the
                 * image will be equal to or LESS than the corresponding
                 * dimension of the view (minus padding)." (Android documentation)
                 */
                float scale = Math.min(this.getWidth() / rect.width(),
                        this.getHeight() / rect.height());
                matrix.postScale(scale, scale);
                break;
            }
        }
        // we translate the image to obtain the other part of the postcondition.
        // Get the mapping of the image rectangle before translation.
        rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrix.mapRect(rect);
        if (mScaleType == ScaleType.CENTER
                || mScaleType == ScaleType.FIT_CENTER)
        {
            // translate center of image to center of display
            matrix.postTranslate(this.getWidth() / 2 - rect.centerX(),
                    this.getHeight() / 2 - rect.centerY());

        } else if (mScaleType == ScaleType.FIT_START
                || mScaleType == ScaleType.FIT_XY)
        {
            // now translate the image so the top left corner maps to (0,0)
            matrix.postTranslate(-rect.left, -rect.top);
        } else if (mScaleType == ScaleType.FIT_END)
        {
            // translate the image so the bottom right corner maps to (width(),
            // height())
            matrix.postTranslate(getWidth() - rect.width(),
                    getHeight() - rect.height());
        }
        super.setScaleType(ScaleType.MATRIX);
        super.setImageMatrix(matrix);
        super.setImageBitmap(bitmap);
    }

    /**
     * We override setScaleType so the actual scaletype used is always matrix
     * but we model the behavior of the other scaletypes. This allows this class's
     * matrix functions to be used to determine where things should go on the displayed
     * image.
     * @see android.widget.ImageView#setScaleType(android.widget.ImageView.ScaleType)
     */
    @Override
    public void setScaleType(ScaleType scaleType)
    {
        if (scaleType == ScaleType.MATRIX)
        {
            // I can't figure out how to support this
            throw new UnsupportedOperationException(
                    "ScaleType.MATRIX is not supported by ImageCameraView");
        }
        mScaleType = scaleType;
    }
}
