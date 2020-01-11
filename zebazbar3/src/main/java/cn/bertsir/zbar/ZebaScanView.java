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

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import cn.bertsir.zbar.view.ZBarScanFGView;

/**
 * <p>QRCode Camera preview, include QRCode recognition.</p>
 */
public class ZebaScanView extends FrameLayout implements SurfaceHolder.Callback {

    private CameraManager mCameraManager;
    private CameraScanAnalysis mPreviewCallback;
    private SurfaceView mSurfaceView;
    private boolean isPreviewStart = false;
    private ZBarScanFGView zBarScanFGView;

    public ZebaScanView(Context context) {
        this(context, null);
    }

    public ZebaScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZebaScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCameraManager = new CameraManager(context);
        mPreviewCallback = new CameraScanAnalysis();
        zBarScanFGView=new ZBarScanFGView(context);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Rect mScanRect = new Rect();
        int w1 = w / 3 * 2;
        mScanRect.left = w / 2 - w1 / 2;
        mScanRect.right = mScanRect.left + w1;
        mScanRect.top = h / 2 - w1 / 2;
        mScanRect.bottom = mScanRect.top + w1;
        zBarScanFGView.setRect(mScanRect);
    }

    /**
     * Set Scan results callback.
     *
     * @param callback {@link ScanListener}.
     */
    public void setScanListener(ScanListener callback) {
        mPreviewCallback.setScanCallback(callback);
    }
    public void setBgColor(int color) {
        zBarScanFGView.setBgColor(color);
    }

    public void setLineColor(int color) {
        zBarScanFGView.setLineColor(color);
    }
    /**
     * Camera start preview.
     */
    public boolean start() {
        try {
            mCameraManager.openDriver();
        } catch (Exception e) {
            Toast.makeText(getContext(), "摄像头权限被拒绝！", Toast.LENGTH_SHORT).show();
            return false;
        }
        mPreviewCallback.onStart();

        if (mSurfaceView == null) {
            mSurfaceView = new SurfaceView(getContext());
            addView(mSurfaceView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(zBarScanFGView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        startCameraPreview(mSurfaceView.getHolder());
        return true;
    }

    /**
     * Camera stop preview.
     */
    public void stop() {
        removeCallbacks(mAutoFocusTask);
        mPreviewCallback.onStop();

        mCameraManager.stopPreview();
        mCameraManager.closeDriver();
    }

    private void startCameraPreview(SurfaceHolder holder) {
        try {
            mCameraManager.startPreview(holder, mPreviewCallback);
            mCameraManager.autoFocus(mFocusCallback);
            isPreviewStart = true;
        } catch (Exception e) {
            e.printStackTrace();
            //如果异常延迟200ms再试
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCameraManager.autoFocus(mFocusCallback);
                }
            }, 200);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        mCameraManager.stopPreview();
        startCameraPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private Camera.AutoFocusCallback mFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            postDelayed(mAutoFocusTask, 500);
        }
    };

    private Runnable mAutoFocusTask = new Runnable() {
        public void run() {
            mCameraManager.autoFocus(mFocusCallback);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    public void setFlash() {
        mCameraManager.setFlash();
    }

    public void setFlash(boolean open) {
        mCameraManager.setFlash(open);
    }

    public void setZoom(float zoom){
        mCameraManager.setCameraZoom(zoom);
    }

    public void handleZoom(boolean isZoomIn){
        mCameraManager.handleZoom(isZoomIn);
    }

    public boolean isPreviewStart(){
        return this.isPreviewStart;
    }
}