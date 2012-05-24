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
package root.gast.playground.image.livecaptureplus;

import java.util.List;

import jjil.android.Preview;
import root.gast.image.ManageCameraActivity;
import root.gast.playground.R;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LiveCapturePlusActivity extends ManageCameraActivity
    implements Preview.PreviewSizeChangedCallback
{
    private Button mButtonFlash, mButtonFocus, mButtonSwitch,
            mButtonWhiteBalance, mButtonZoom;
    private List<String> mlszFocusModes, mlszFlashModes, mlszWhiteBalanceModes;
    private int mnFlashMode, mnFocusMode, mnWhiteBalanceMode;
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

        // set up UI
        setContentView(R.layout.image_livecaptureplus);
        mPreview = (Preview) findViewById(R.id.preview1);
        mPreview.setPreviewSizeChangedCallback(this);
        mButtonFlash = (Button) findViewById(R.id.buttonFlash);
        mButtonFlash.setOnClickListener(mButtonClickListener);
        mButtonFocus = (Button) findViewById(R.id.buttonFocus);
        mButtonFocus.setOnClickListener(mButtonClickListener);
        mButtonSwitch = (Button) findViewById(R.id.buttonSwitchCamera);
        mButtonSwitch.setOnClickListener(mButtonClickListener);
        mButtonWhiteBalance = (Button) findViewById(R.id.buttonWhiteBalance);
        mButtonWhiteBalance.setOnClickListener(mButtonClickListener);
        mButtonZoom = (Button) findViewById(R.id.buttonZoom);
        mButtonZoom.setOnClickListener(mButtonClickListener);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // set UI for current camera
        switchCameraUI();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // clear current camera from UI
        mPreview.setCamera(null);
    }

    /**
     * switchCamera initializes the UI labels for the currently selected camera
     */
    private void switchCameraUI()
    {
        mPreview.switchCamera(mCamera);
        mButtonSwitch
                .setText(getText(R.string.camera) + " " + mDefaultCameraId);
        Camera.Parameters cameraParameters = mCamera.getParameters();
        mlszFlashModes = cameraParameters.getSupportedFlashModes();
        mlszFocusModes = cameraParameters.getSupportedFocusModes();
        mlszWhiteBalanceModes = cameraParameters.getSupportedWhiteBalance();
        mButtonFlash.setEnabled(mlszFlashModes != null
                && mlszFlashModes.size() > 0);
        mButtonZoom.setEnabled(cameraParameters.isZoomSupported()
                && cameraParameters.getMaxZoom() > 0);
        mnFlashMode = 0;
        mnFocusMode = 0;
        mnWhiteBalanceMode = 0;
        setCameraLabels(cameraParameters);
    }

    /**
     * set the button labels based on the current camera parameters
     * 
     * @param cameraParameters
     *            : the curren camera parameters object
     */
    void setCameraLabels(Camera.Parameters cameraParameters)
    {
        if (mlszFlashModes != null)
        {
            mButtonFlash.setText(getText(R.string.flash) + " "
                    + cameraParameters.getFlashMode());
        } else
        {
            mButtonFlash.setText(getText(R.string.flash));
            mButtonZoom.setEnabled(false);
        }
        if (mlszFocusModes != null)
        {
            mButtonFocus.setText(getText(R.string.focus) + " "
                    + cameraParameters.getFocusMode());
        } else
        {
            mButtonFocus.setText(getText(R.string.focus));
            mButtonFocus.setEnabled(false);
        }
        if (mlszWhiteBalanceModes != null)
        {
            mButtonWhiteBalance.setText(getText(R.string.whiteBalance) + " "
                    + cameraParameters.getWhiteBalance());
        } else
        {
            mButtonWhiteBalance.setText(getText(R.string.whiteBalance));
            mButtonWhiteBalance.setEnabled(false);
        }
        mButtonZoom.setText(getText(R.string.zoom) + " "
                + cameraParameters.getZoom());

    }

    /**
     * mButtonClickListener responds to all button click events
     */
    private OnClickListener mButtonClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Camera.Parameters cameraParameters = mCamera.getParameters();
            if (v == mButtonFlash)
            {
                mnFlashMode = (mnFlashMode + 1) % mlszFlashModes.size();
                cameraParameters.setFocusMode(mlszFlashModes.get(mnFlashMode));
            } else if (v == mButtonFocus)
            {
                mnFocusMode = (mnFocusMode + 1) % mlszFocusModes.size();
                cameraParameters.setFocusMode(mlszFocusModes.get(mnFocusMode));
            } else if (v == mButtonSwitch)
            {
                mCamera.stopPreview();
                advanceCamera();
                switchCameraUI();
                // mCamera.startPreview();
                // reset camera parameters
                cameraParameters = mCamera.getParameters();
            } else if (v == mButtonWhiteBalance)
            {
                mnWhiteBalanceMode = (mnWhiteBalanceMode + 1)
                        % mlszWhiteBalanceModes.size();
                cameraParameters.setFocusMode(mlszWhiteBalanceModes
                        .get(mnWhiteBalanceMode));
            } else if (v == mButtonZoom)
            {
                cameraParameters.setZoom((cameraParameters.getZoom() + 1)
                        % cameraParameters.getMaxZoom() + 1);
            }
            // stop camera preview because changing some parameters caused a
            // RuntimeException if it is running
            mCamera.stopPreview();
            try
            {
                mCamera.setParameters(cameraParameters);
            } catch (RuntimeException rx)
            {
                String szError = getApplicationContext().getString(R.string.set_parameters_failed) +
                        rx.toString();
                Toast t = Toast.makeText(getApplicationContext(), szError, Toast.LENGTH_SHORT);
                t.show();
                // the camera parameter change failed. Reset current value
                cameraParameters = mCamera.getParameters();
            }
            mCamera.startPreview();
            setCameraLabels(cameraParameters);
        }
    };

    @Override
    public void previewSizeChanged()
    {
        Camera.Parameters cameraParameters = mCamera.getParameters();
        mButtonZoom.setEnabled(cameraParameters.isZoomSupported()
                && cameraParameters.getMaxZoom() > 0);
    }
}