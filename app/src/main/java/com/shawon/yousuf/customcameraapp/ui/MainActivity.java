package com.shawon.yousuf.customcameraapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.shawon.yousuf.customcameraapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.buttonCameraActivity)
    Button buttonCameraActivity;


    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.buttonCameraActivity)
    public void onClick() {

        Intent captureImageIntent = new Intent(this, CameraActivity.class);
        startActivity(captureImageIntent);
    }
}
