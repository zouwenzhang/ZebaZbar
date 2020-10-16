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
package cn.bertsir.zbar;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.bertsir.zbar.Qr.Config;
import cn.bertsir.zbar.Qr.Image;
import cn.bertsir.zbar.Qr.ImageScanner;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.Qr.SymbolSet;
import cn.bertsir.zbar.utils.CameraUtil;
import cn.bertsir.zbar.view.ZBarScanFGView;

/**
 */
class CameraScanAnalysis implements Camera.PreviewCallback {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ImageScanner mImageScanner;
    private Handler mHandler=new Handler(Looper.getMainLooper());
    private ScanListener mCallback;
    private TakePhotoListener takePhotoListener;
    private AtomicBoolean isTakePhoto=new AtomicBoolean(false);
    private static final String TAG = "CameraScanAnalysis";

    private boolean allowAnalysis = true;
    private Image barcode;
    private Camera.Size size;
    private long lastResultTime = 0;
    private ZBarScanFGView fgView;

    CameraScanAnalysis(ZBarScanFGView view) {
        fgView=view;
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
//        if (Symbol.scanType == QrConfig.TYPE_QRCODE) {
//            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
//            mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
//        } else if (Symbol.scanType == QrConfig.TYPE_BARCODE) {
//            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
//            mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
//            mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
//        } else if (Symbol.scanType == QrConfig.TYPE_ALL) {
//            mImageScanner.setConfig(Symbol.NONE, Config.X_DENSITY, 3);
//            mImageScanner.setConfig(Symbol.NONE, Config.Y_DENSITY, 3);
//        } else if (Symbol.scanType == QrConfig.TYPE_CUSTOM) {
//            mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
//            mImageScanner.setConfig(Symbol.scanFormat, Config.ENABLE, 1);
//        } else {
//            mImageScanner.setConfig(Symbol.NONE, Config.X_DENSITY, 3);
//            mImageScanner.setConfig(Symbol.NONE, Config.Y_DENSITY, 3);
//        }

    }

    void setScanCallback(ScanListener callback) {
        this.mCallback = callback;
    }

    void onStop() {
        this.allowAnalysis = false;
    }

    void onStart() {
        this.allowAnalysis = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(isTakePhoto.get()){
            isTakePhoto.set(false);
            takePhoto(data,camera);
        }
        if (allowAnalysis) {
            allowAnalysis = false;
            size = camera.getParameters().getPreviewSize();
            barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
            //用于框中的自动拉伸和对识别数据的裁剪
//            int w=(int)(size.height/3f*2);
//            barcode.setCrop(size.width/2-w/2, size.height/2-w/2, w, w);
//            barcode.setCrop(0,0,size.width,size.height);
            if(Symbol.looperScan  &&  (System.currentTimeMillis() - lastResultTime < Symbol.looperWaitTime)){
                allowAnalysis = true;
                return;
            }
            executorService.execute(mAnalysisTask);
        }
    }

    public void takePhoto(TakePhotoListener takePhotoListener){
        this.takePhotoListener=takePhotoListener;
        isTakePhoto.set(true);
    }

    private void takePhoto(final byte[] data,final Camera camera){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if(takePhotoListener!=null){
                    final Bitmap bitmap=CameraUtil.toBitmap(data,camera);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Camera.Size size = camera.getParameters().getPreviewSize();
                            takePhotoListener.onResult(data,size.width,size.height, bitmap);
                            takePhotoListener=null;
                        }
                    });
                }
            }
        });
    }

    /**
     * 相机设置焦距
     */
    public void cameraZoom(Camera mCamera) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (!parameters.isZoomSupported()) {
                return;
            }
            int maxZoom = parameters.getMaxZoom();
            if (maxZoom == 0) {
                return;
            }
            if (parameters.getZoom() + 10 > parameters.getMaxZoom()) {
                return;
            }
            parameters.setZoom(parameters.getZoom() + 10);
            mCamera.setParameters(parameters);
        }
    }

    private Runnable mAnalysisTask = new Runnable() {
        @Override
        public void run() {
            int result = mImageScanner.scanImage(barcode);
            if(result==0){
                allowAnalysis = true;
                return;
            }
            String resultStr = null;
            int resultType = -1;
            if (result != 0) {
                Symbol sy=null;
                SymbolSet symSet = mImageScanner.getResults();
                for (Symbol sym : symSet){
                    resultStr = sym.getData();
                    resultType= sym.getType();
                    sy=sym;
                    break;
                }
                if(sy!=null){
//                    Log.e("zwz","w="+barcode.getWidth()+",h="+barcode.getHeight()+",l="+b[0]+",t="+b[1]+",r="+b[2]+",b="+b[3]);
                    final int[] dps=sy.getBounds();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fgView.setShowPoint(dps);
                        }
                    });
                }
            }
            if (!TextUtils.isEmpty(resultStr)) {
                final ScanResult scanResult = new ScanResult();
                scanResult.setContent(resultStr);
                scanResult.setType(resultType == Symbol.QRCODE ? ScanResult.CODE_QR : ScanResult.CODE_BAR);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onScanResult(scanResult);
                    }
                },300);
                lastResultTime = System.currentTimeMillis();
                if (Symbol.looperScan) {
                    allowAnalysis = true;
                }
            }
        }
    };
}