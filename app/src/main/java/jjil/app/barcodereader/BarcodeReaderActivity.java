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
package jjil.app.barcodereader;

import root.gast.playground.R;
import jjil.android.CrosshairOverlay;
import jjil.android.Preview;
import jjil.android.ReadBarcode;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.widget.CheckBox;
import android.widget.TextView;

public class BarcodeReaderActivity extends Activity
{
    private static final String TAG = "BarcodeReaderActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_barcode);

        TextView tvBarcode = (TextView) findViewById(R.id.textView1);
        CheckBox ckSuccess = (CheckBox) findViewById(R.id.checkBox1);
        CrosshairOverlay co = (CrosshairOverlay) findViewById(R.id.crosshairoverlay1);
        // Find the total number of cameras available
        int numberOfCameras = Camera.getNumberOfCameras();
        mPreview = (Preview) findViewById(R.id.preview1);
        // set the position where we expect the barcodes to appear
        // 0.5 = middle of the screen
        mReadBarcode = new ReadBarcode(0.5d, tvBarcode, ckSuccess, co);

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
            {
                mDefaultCameraId = i;
            }
        }
        mCameraCurrentlyLocked = mDefaultCameraId;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mCamera = Camera.open(mCameraCurrentlyLocked);
        mPreview.setCamera(mCamera);
        mCamera.setPreviewCallback(mReadBarcode);
        String focusMode = mCamera.getParameters().getFocusMode();
        if (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)
                || focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO))
        {
            //original code from the book
//            mCamera.autoFocus(mReadBarcode);
            //delayed focusing works on more devices
            mReadBarcode.autoFocusLater(mCamera);
        }
        else
        {
        }

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null)
        {
            mPreview.setCamera(null);
            mCamera.autoFocus(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void setCameraDisplayOrientation()
    {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraCurrentlyLocked, cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            mnCameraOrientation = (cameraInfo.orientation + degrees) % 360;
            mnCameraOrientation = (360 - mnCameraOrientation) % 360; // compensate
                                                                     // the
                                                                     // mirror
        } else
        { // back-facing
            mnCameraOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(mnCameraOrientation);
    }

    private int mnCameraOrientation;
    private ReadBarcode mReadBarcode;
    private Camera mCamera;
    private int mCameraCurrentlyLocked;
    private int mDefaultCameraId;
    private Preview mPreview;
}