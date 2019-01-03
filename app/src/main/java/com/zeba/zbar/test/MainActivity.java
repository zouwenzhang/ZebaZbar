package com.zeba.zbar.test;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zbar.lib.view.ZBarScanView;
import com.zbar.lib.view.interfaces.ZBarScanListener;

public class MainActivity extends AppCompatActivity {

    private ZBarScanView zBarScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zBarScanView=findViewById(R.id.zbarscan_view);
        zBarScanView.setLineColor(Color.parseColor("#ff34ff"));
        zBarScanView.setScanResultListener(new ZBarScanListener() {
            @Override
            public void scanResult(String result) {

            }
            @Override
            public void scanError(int what) {

            }
            @Override
            public void restartScan() {
                zBarScanView.restartScan();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        zBarScanView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zBarScanView.onPause();
    }
}
