package com.shawon.yousuf.customcameraapp.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.shawon.yousuf.customcameraapp.R;
import com.shawon.yousuf.customcameraapp.utils.CameraPreview;
import com.shawon.yousuf.customcameraapp.utils.Constants;
import com.shawon.yousuf.customcameraapp.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @Bind(R.id.camera_preview)
    FrameLayout cameraPreview;
    @Bind(R.id.button_capture)
    Button buttonCapture;

    private Camera mCamera;
    private CameraPreview mPreview;

    private int imageRotation = 0;
    private int workingCameraId;

    OrientationEventListener orientationEventListener;

    private String TAG = getClass().getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.bind(this);

        workingCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL ) {
            @Override
            public void onOrientationChanged(int orientation) {

                Log.d(TAG, "onOrientationChanged" );
                if (orientation == ORIENTATION_UNKNOWN) return;
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(workingCameraId, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }

                imageRotation = rotation;
                Log.d(TAG, "rotation: " + rotation);

                //  mParameters.setRotation(rotation);


            }
        };



    }




    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        openCamera();
        if (orientationEventListener != null) {
           // orientationEventListener.enable();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        releaseCamera();
        if (orientationEventListener != null) {
         //   orientationEventListener.disable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    private void openCamera(){

        cameraPreview.removeAllViews();

        // Create an instance of Camera
        if (mCamera == null) {
            mCamera = Util.getCameraInstance();

        }

        if (mCamera == null) {
            Log.d(TAG, "camera is null");
        }else {

            int numOfCamera = Camera.getNumberOfCameras();

            for( int i =0; i<numOfCamera; i++ ){
                Log.d(TAG, "" + i + ": ");
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo != null) {
                    Log.d(TAG, "camera info: facing - "  + cameraInfo.facing + " orientation- " + cameraInfo.orientation );
                }
            }


            Util.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            mCamera.setParameters(parameters);

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            cameraPreview.addView(mPreview);

        }

    }




    private void releaseCamera(){
        if (mCamera != null){
            Log.d(TAG, "Releasing camera");
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private void mediaScan(Uri uri){

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri ));
    }




    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = Util.getOutputMediaFile(Constants.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: " );
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                showToast("Image saved.");
                mediaScan(Uri.fromFile(pictureFile));
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            openCamera();
        }
    };



    @OnClick(R.id.button_capture)
    public void onClick() {


        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(imageRotation);
        mCamera.setParameters(parameters);
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);

        // openCamera();

    }



}
