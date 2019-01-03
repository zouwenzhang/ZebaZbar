package com.zbar.lib.view.interfaces;

public interface ZBarScanListener {
	public void scanResult(String result);
	public void scanError(int what);
	public void restartScan();
}
