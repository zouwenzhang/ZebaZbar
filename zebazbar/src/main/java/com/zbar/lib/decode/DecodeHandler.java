package com.zbar.lib.decode;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zbar.lib.ZBarScan;
import com.zbar.lib.ZbarManager;
import com.zbar.lib.bitmap.PlanarYUVLuminanceSource;
import com.zbar.lib.view.interfaces.ZBarScanHandler;

/**
 * 解析Handler
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
final class DecodeHandler extends Handler {

	ZBarScanHandler activity = null;

	DecodeHandler(ZBarScanHandler activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case ZBarScan.decode:
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case ZBarScan.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * 二维码解析
	 * 
	 * @param data
	 *            图片数据
	 * @param width
	 *            原始宽度
	 * @param height
	 *            原始高度
	 */
	private void decode(byte[] data, int width, int height) {
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}

		// Here we are swapping, that's the difference to #11
		int tmp = width;
		width = height;
		height = tmp;

		// ZBar管理器
		ZbarManager manager = new ZbarManager();
		// 进行解码
		String result = manager.decode(rotatedData, width, height, true,
				activity.getX(), activity.getY(), activity.getCropWidth(),
				activity.getCropHeight());

		if (result != null) {
			// 需要保存扫描的二维码图片
			if (activity.isNeedCapture()) {
				// 生成bitmap
				PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
						rotatedData, width, height, activity.getX(),
						activity.getY(), activity.getCropWidth(),
						activity.getCropHeight(), false);
				int[] pixels = source.renderThumbnail();
				int w = source.getThumbnailWidth();
				int h = source.getThumbnailHeight();
				Bitmap bitmap = Bitmap.createBitmap(pixels, 0, w, w, h,
						Bitmap.Config.ARGB_8888);
				try {
					// 保存二维码图片
					String rootPath = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/Qrcode/";
					File root = new File(rootPath);
					if (!root.exists()) {
						root.mkdirs();
					}
					File f = new File(rootPath + "Qrcode.jpg");
					if (f.exists()) {
						f.delete();
					}
					f.createNewFile();

					FileOutputStream out = new FileOutputStream(f);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 向Activity发一条消息
			if (null != activity.getHandler()) {
				Message msg = new Message();
				msg.obj = result;
				msg.what = ZBarScan.decode_succeeded;
				activity.getHandler().sendMessage(msg);
			}
		} else {
			if (null != activity.getHandler()) {
				activity.getHandler().sendEmptyMessage(ZBarScan.decode_failed);
			}
		}
	}

}
