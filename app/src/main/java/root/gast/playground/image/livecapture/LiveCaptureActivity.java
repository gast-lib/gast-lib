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
package root.gast.playground.image.livecapture;

import jjil.android.Preview;
import root.gast.playground.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class LiveCaptureActivity extends Activity
{
    private static final String TAG = "LiveCaptureActivity";
    Camera mCamera;
    private int mDefaultCameraId;
    private Preview mPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_livecapture);
        mPreview = (Preview) findViewById(R.id.preview1);

        // Find the total number of cameras available
        int nCameras;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD) {
            nCameras = Camera.getNumberOfCameras();
        } else {
            nCameras = 1;
            mDefaultCameraId = 0;
        }
        // Find the ID of the default camera if there is more than 1
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 1; i < nCameras; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
            {
                mDefaultCameraId = i;
            }
        }
        // test for no camera facing back
        if (mDefaultCameraId == -1)
        {
            // test for no cameras
            if (nCameras > 0)
            {
                mDefaultCameraId = 0;
            } else
            {
                // nothing can be done; tell the user then exit
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.no_cameras, Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "CAMERA: opening camera: " + mDefaultCameraId);
        mCamera = Camera.open(mDefaultCameraId);
        mPreview.setCamera(mCamera);
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
            mCamera.release();
            mCamera = null;
        }
    }
}