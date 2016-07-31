package root.gast.image;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;

public class ManageCameraActivity extends Activity
{
    protected Camera mCamera;
    protected int mDefaultCameraId;
    private ImageCameraView mImageCameraView;
    protected int mNumberOfCameras;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Find the total number of cameras available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            mNumberOfCameras = Camera.getNumberOfCameras();
            // Find the ID of the default camera
            CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < mNumberOfCameras; i++)
            {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
                {
                    mDefaultCameraId = i;
                }
            }
        } else
        {
            // test for no camera facing back
            mNumberOfCameras = 1;
            mDefaultCameraId = 0;
        }

        // test for no camera facing back
        if (mDefaultCameraId == -1)
        {
            // test for no cameras
            if (mNumberOfCameras > 0)
            {
                mDefaultCameraId = 0;
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open(mDefaultCameraId);
        setCameraDisplayOrientation();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null)
        {
            // reset all the callbacks
            mCamera.autoFocus(null);
            mCamera.setErrorCallback(null);
            mCamera.setOneShotPreviewCallback(null);
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setZoomChangeListener(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Switching from one camera to another. Must stop the preview, free the
     * previous camera, and open the new camera, in that order.
     */
    protected void advanceCamera()
    {
        mCamera.stopPreview();
        mCamera.release();
        mDefaultCameraId = (mDefaultCameraId + 1) % mNumberOfCameras;
        mCamera = Camera.open(mDefaultCameraId);
        setCameraDisplayOrientation();
    }

    /**
     * Calculate the camera display orientation so the camera image matches
     * the user's expectation based on how the display is turned.
     * The rotation required is based on the orientation intrinsic to the
     * camera (CameraInfo.orientation), minus any rotation the display has
     * gone through due to being (think of the display as doing the work 
     * necessary to orient the camera). The camera orientation is reversed
     * if the camera is facing the user since the image is mirrored.
     */
    public void setCameraDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mDefaultCameraId, cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
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

        int desiredRotation = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) ? (360 - cameraInfo.orientation)
                : cameraInfo.orientation;
        int nRotation = (desiredRotation - degrees + 360) % 360;
        mCamera.setDisplayOrientation(nRotation);
        if (mImageCameraView != null) {
            mImageCameraView.setCameraDisplayCharacteristics(cameraInfo.facing,
                    nRotation);
        }

    }
    
    /**
     * The image camera view tracks the current camera orientation as set by the
     * setCameraDisplayOrientation method.
     * @param imageCameraView
     */
    public void setImageCameraView(ImageCameraView imageCameraView) {
        mImageCameraView = imageCameraView;
    }
}
