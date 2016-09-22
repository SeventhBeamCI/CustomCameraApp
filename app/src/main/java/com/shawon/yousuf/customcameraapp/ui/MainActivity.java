package com.shawon.yousuf.customcameraapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.shawon.yousuf.customcameraapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.buttonCameraActivity)
    Button buttonCameraActivity;
    @Bind(R.id.buttonRecordVideo)
    Button buttonRecordVideo;


    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }


    @OnClick({R.id.buttonCameraActivity, R.id.buttonRecordVideo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCameraActivity:

                Intent captureImageIntent = new Intent(this, CameraActivity.class);
                startActivity(captureImageIntent);
                break;
            case R.id.buttonRecordVideo:

                Intent recordVideoIntent = new Intent(this, VideoRecordActivity.class);
                startActivity(recordVideoIntent);
                break;
        }
    }
}
