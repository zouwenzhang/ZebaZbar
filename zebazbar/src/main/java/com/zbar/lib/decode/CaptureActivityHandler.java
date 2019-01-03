package com.zbar.lib.decode;

import android.os.Handler;
import android.os.Message;

import com.zbar.lib.ZBarScan;
import com.zbar.lib.camera.CameraManager;
import com.zbar.lib.view.interfaces.ZBarScanHandler;

/**
 * 扫描画面Handler
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
public final class CaptureActivityHandler extends Handler {

	/** 解析线程 */
	DecodeThread decodeThread = null;

	/** 解析画面 */
	ZBarScanHandler activity = null;

	/** 状态 */
	private State state;

	/**
	 * 状态枚举
	 * 
	 * @author Hitoha
	 * @version 1.00 2015/04/29 新建
	 */
	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	/**
	 * 构造方法
	 * 
	 * @param activity
	 *            解析画面
	 */
	public CaptureActivityHandler(ZBarScanHandler activity) {
		this.activity = activity;
		// 新建解析线程
		decodeThread = new DecodeThread(activity);
		// 线程开始
		decodeThread.start();
		// 状态设为成功
		state = State.SUCCESS;
		// 相机开始预览
		CameraManager.get().startPreview();
		// 预览与二维码解析
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		// 相机自动对焦时
		case ZBarScan.auto_focus:
			// 解析状态为预览时
			if (state == State.PREVIEW) {
				// 自动对焦
				CameraManager.get().requestAutoFocus(this, ZBarScan.auto_focus);
			}
			break;
		// 二维码解析中
		case ZBarScan.restart_preview:
			// 预览解析
			restartPreviewAndDecode();
			break;
		// 二维码解析成功
		case ZBarScan.decode_succeeded:
			// 状态设为解析成功
			state = State.SUCCESS;
			// 解析成功，回调
			activity.handleDecode((String) message.obj);
			break;
		// 二维码解析失败
		case ZBarScan.decode_failed:
			// 解析状态设为预览时
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					ZBarScan.decode);
			break;
		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		removeMessages(ZBarScan.decode_succeeded);
		removeMessages(ZBarScan.decode_failed);
		removeMessages(ZBarScan.decode);
		removeMessages(ZBarScan.auto_focus);
	}

	/**
	 * 开始解析二维码
	 */
	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					ZBarScan.decode);
			CameraManager.get().requestAutoFocus(this, ZBarScan.auto_focus);
		}
	}

}
