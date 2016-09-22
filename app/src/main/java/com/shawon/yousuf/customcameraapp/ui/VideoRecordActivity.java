package com.shawon.yousuf.customcameraapp.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoRecordActivity extends AppCompatActivity {

    @Bind(R.id.camera_preview)
    FrameLayout cameraPreview;
    @Bind(R.id.button_capture)
    Button buttonCapture;



    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;

    File savedVideoFile;

    private boolean isRecording = false;
    private int imageRotation = 0;
    private int workingCameraId;
    OrientationEventListener orientationEventListener;


    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        ButterKnife.bind(this);

        workingCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        if (mCamera == null) {
            mCamera = Util.getCameraInstance();

        }


        if (mCamera == null) {
            Toast.makeText(this, "Can't open camera", Toast.LENGTH_LONG).show();
            finish();
        }


        Util.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);


        mPreview = new CameraPreview(this, mCamera);
        cameraPreview.addView(mPreview);


        mCamera.setParameters(parameters);



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

        if (orientationEventListener != null) {
            orientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event

        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }

    }

    private boolean prepareVideoRecorder(){

        // Create an instance of Camera

        if (mCamera == null) {
            return false;
        }



        mMediaRecorder = new MediaRecorder();


        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);



        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        savedVideoFile = Util.getOutputMediaFile(Constants.MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(savedVideoFile.toString());
        Log.d(TAG, "video file: "  + savedVideoFile.toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());


      //  Camera.Parameters parameters = mCamera.getParameters();
       // parameters.setRotation(imageRotation);
       // mCamera.setParameters(parameters);

         mMediaRecorder.setOrientationHint(imageRotation);




        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }



    private void releaseCamera(){
        if (mCamera != null){
            Log.d(TAG, "Releasing camera");
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }



    private void mediaScan(Uri uri){

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri ));
    }


    private void startStopRecord(){

        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            buttonCapture.setText("Capture");
            isRecording = false;
            mediaScan(Uri.fromFile(savedVideoFile));
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                // inform the user that recording has started
                buttonCapture.setText("Stop");
                isRecording = true;
            } else {
                Log.d(TAG, "can't prepare video recorder");
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }


    }






    @OnClick(R.id.button_capture)
    public void onClick() {

        startStopRecord();
    }
}
