package com.zbar.lib.view.interfaces;

import android.os.Handler;

public interface ZBarScanHandler {
	public void handleDecode(String result);
	public int getX();
	public int getY();
	public int getCropWidth();
	public int getCropHeight();
	public boolean isNeedCapture();
	public Handler getHandler();
	
}
