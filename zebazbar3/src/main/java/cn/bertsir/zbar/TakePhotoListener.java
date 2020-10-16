package cn.bertsir.zbar;

import android.graphics.Bitmap;

public interface TakePhotoListener {

    void onResult(byte[] data, int w, int h, Bitmap bitmap);
}
