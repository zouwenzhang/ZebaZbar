package com.zeba.zbar.test;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import cn.bertsir.zbar.ZebaScanView;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.ScanListener;


public class TestActivity extends AppCompatActivity {
    private ZebaScanView cameraPreview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        cameraPreview=findViewById(R.id.cp);
        cameraPreview.setScanListener(new ScanListener() {
            @Override
            public void onScanResult(ScanResult result) {
                Log.e("zwz","content="+result.content);
                Toast.makeText(TestActivity.this,result.content,Toast.LENGTH_LONG).show();
            }
        });
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.stop();
    }
}
