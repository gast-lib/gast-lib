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
package root.gast.playground.image.detectlogo;

import jjil.android.Preview;
import jjil.core.Error;
import root.gast.image.ImageCameraView;
import root.gast.image.LogoView;
import root.gast.image.ManageCameraActivity;
import root.gast.image.ViewLogo;
import root.gast.playground.R;
import root.gast.playground.image.detectlogobetter.DetectLogo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.widget.Toast;

public class DetectLogoActivity extends ManageCameraActivity
{
    private DetectLogo mDetectLogo;
    private Preview mPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // check for failure to find a camera
        if (mNumberOfCameras == 0)
        {
            // nothing can be done; tell the user then exit
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.no_cameras, Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        
        setContentView(R.layout.image_detectlogo);
        mPreview = (Preview) findViewById(R.id.preview1);
        CameraInfo ci = new CameraInfo();
        Camera.getCameraInfo(mDefaultCameraId, ci);
        ImageCameraView icv = (ImageCameraView) findViewById(R.id.imageView1);
        LogoView lv = (LogoView) findViewById(R.id.logoView1);
        lv.setImageCameraView(icv);
        try {
            mDetectLogo = new DetectLogo(icv, lv);
        } catch (Error e) {
            finish();
        }
        setImageCameraView(icv);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mPreview.setCamera(mCamera);
        mCamera.setPreviewCallback(mDetectLogo);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        
        mPreview.setCamera(null);
    }

}