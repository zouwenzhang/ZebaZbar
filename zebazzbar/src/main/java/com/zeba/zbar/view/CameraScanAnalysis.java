/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zeba.zbar.view;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yanzhenjie.zbar.Config;
import com.yanzhenjie.zbar.Image;
import com.yanzhenjie.zbar.ImageScanner;
import com.yanzhenjie.zbar.Symbol;
import com.yanzhenjie.zbar.SymbolSet;
import com.zeba.zbar.interfaces.ZBarScanListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yan Zhenjie on 2017/5/5.
 */
public class CameraScanAnalysis implements Camera.PreviewCallback {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageScanner mImageScanner;
    private Handler mHandler;
    private ZBarScanListener mCallback;

    private boolean allowAnalysis = true;
    private Image barcode;
    private int x;
    private int y;
    private int width;
    private int height;

    public CameraScanAnalysis() {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mCallback != null) mCallback.scanResult((String) msg.obj);
            }
        };
    }

    public void setScanCallback(ZBarScanListener callback) {
        this.mCallback = callback;
    }

    public void setCorp(int x,int y,int w,int h){
        this.x=x;
        this.y=y;
        this.width=w;
        this.height=h;
    }

    public void onStop() {
        this.allowAnalysis = false;
    }

    public void onStart() {
        this.allowAnalysis = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (allowAnalysis) {
            allowAnalysis = false;

            Camera.Size size = camera.getParameters().getPreviewSize();

            barcode = new Image(size.width, size.height, "Y800");
            barcode.setCrop(x, y, width, height);
            barcode.setData(data);
            Log.e("zwz","sw="+size.width+",sh="+size.height);
            Log.e("zwz","x="+x+",y="+y+",w="+width+",h="+height);

            executorService.execute(mAnalysisTask);
        }
    }

    private Runnable mAnalysisTask = new Runnable() {
        @Override
        public void run() {
            int result = mImageScanner.scanImage(barcode);

            String resultStr = null;
            if (result != 0) {
                SymbolSet symSet = mImageScanner.getResults();
                for (Symbol sym : symSet)
                    resultStr = sym.getData();
            }

            if (!TextUtils.isEmpty(resultStr)) {
                Message message = mHandler.obtainMessage();
                message.obj = resultStr;
                message.sendToTarget();
            } else allowAnalysis = true;
        }
    };
}