package com.zeba.zbar;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.zeba.zbar.camera.CameraManager;

import java.io.IOException;

public class ZBarScanView2 extends RelativeLayout implements Callback {

	/** 是否有SurfaceView */
	private boolean hasSurface;
	private SurfaceView mSurfaceView;
	private ZBarScanFGView mZBarScanFGView;

	public ZBarScanView2(Context context) {
		super(context);

		init(context);
	}

	public ZBarScanView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ZBarScanView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context contxt) {
		// 初始化 CameraManager
		CameraManager.init(contxt);
		mSurfaceView = new SurfaceView(contxt);
		addView(mSurfaceView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mZBarScanFGView = new ZBarScanFGView(contxt);
		addView(mZBarScanFGView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	public void setBgColor(int color){
		mZBarScanFGView.setBgColor(color);
	}

	public void setLineColor(int color){
		mZBarScanFGView.setLineColor(color);
	}

	public void onResume() {
//		try {
//			// 已经初始化过相机预览控件
//			if (hasSurface) {
//				// 初始化相机
//				initCamera(mSurfaceView.getHolder());
//			} else {
//				// 调用接口初始化相机预览控件
//				mSurfaceView.getHolder().addCallback(this);
//				mSurfaceView.getHolder().setType(
//						SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void onPause() {
		try {
			// 相机关闭
			CameraManager.get().closeDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 初始化相机
		initCamera(holder);
		mSurfaceView.getHolder().addCallback(this);
		mSurfaceView.getHolder().setType(
				SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 标记位更改
		hasSurface = false;
	}

	private Point mPoint;
	private Rect mScanRect;

	private void initRect() {
		mScanRect = new Rect();
		int w = getMeasuredWidth() / 3 * 2;
		mScanRect.left = getMeasuredWidth() / 2 - w / 2;
		mScanRect.right = mScanRect.left + w;
		mScanRect.top = getMeasuredHeight() / 2 - w / 2;
		mScanRect.bottom = mScanRect.top + w;

		// // 预览图的宽度，也即camera的分辨率宽度
		// int width = point.y;
		// // 预览图的高度，也即camera的分辨率高度
		// int height = point.x;
	}

	private int x;
	private int y;
	private int cropWidth;
	private int cropHeight;

	/**
	 * 初始化照相机
	 * 
	 * @param surfaceHolder
	 *            SurfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			// 打开相机
			CameraManager.get().openDriver(surfaceHolder);
			// 预览图
			mPoint = CameraManager.get().getCameraResolution();
			if (mScanRect == null) {
				initRect();
			}
			mZBarScanFGView.setRect(mScanRect);
			// 预览图的宽度，也即camera的分辨率宽度
			int width = mPoint.y;
			// 预览图的高度，也即camera的分辨率高度
			int height = mPoint.x;
			// 获取预览图中二维码图片的左上顶点x坐标
			x = mScanRect.left * width / getMeasuredWidth();
			// 预览图中二维码图片的左上顶点y坐标
			y = mScanRect.top * height / getMeasuredHeight();
			// 获取预览图中二维码图片的宽度
			cropWidth = (mScanRect.right - mScanRect.left) * width
					/ getMeasuredWidth();
			// 预览图中二维码图片的高度
			cropHeight = (mScanRect.bottom - mScanRect.top) * height
					/ getMeasuredHeight();
			/**************************************************************/
			// x： 预览图中二维码图片的左上顶点x坐标，也就是手机中相机预览中看到的待扫描二维码的位置的x坐标
			// y： 预览图中二维码图片的左上顶点y坐标，也就是手机中相机预览中看到的待扫描二维码的位置的y坐标
			// cropHeight： 预览图中二维码图片的高度
			// cropWidth： 预览图中二维码图片的宽度
			// height：预览图的高度，也即camera的分辨率高度
			// width： 预览图的宽度，也即camera的分辨率宽度
			//
			// captureCropLayout.getLeft()： 布局文件中扫描框的左上顶点x坐标
			// captureCropLayout.getTop() 布局文件中扫描框的左上顶点y坐标
			// captureCropLayout.getHeight()： 布局文件中扫描框的高度
			// captureCropLayout.getWidth()： 布局文件中扫描框的宽度
			// captureContainter.getHeight()：布局文件中相机预览控件的高度
			// captureContainter.getWidth()： 布局文件中相机预览控件的宽度
			//
			// 其中存在这样一个等比例公式：
			//
			// x / width = captureCropLayout.getLeft() /
			// captureContainter.getWidth();
			// y / height = captureCropLayout.getTop() /
			// captureContainter.getHeight();
			// cropWidth / width = captureCropLayout.getWidth() /
			// captureContainter.getWidth();
			// cropHeight / height = captureCropLayout.getHeight() /
			// captureContainter.getHeight();
			//
			// 即：
			//
			// x = captureCropLayout.getLeft() * width /
			// captureContainter.getWidth() ;
			// y = captureCropLayout.getTop() * height /
			// captureContainter.getHeight() ;
			// cropWidth = captureCropLayout.getWidth() * width /
			// captureContainter.getWidth() ;
			// cropHeight = captureCropLayout.getHeight() * height /
			// captureContainter.getHeight() ;
			/**************************************************************/
		} catch (IOException ioe) {
			// 异常处理
			return;
		} catch (RuntimeException e) {
			// 异常处理
			return;
		}

	}

	/** 重新扫描 */
	public void restartScan() {
	}

}
