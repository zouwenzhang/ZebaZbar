package cn.bertsir.zbar;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.bertsir.zbar.Qr.Config;
import cn.bertsir.zbar.Qr.Image;
import cn.bertsir.zbar.Qr.ImageScanner;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.Qr.SymbolSet;

public class ZebaScan {

    public static void scanImage(Bitmap bitmap, ScanListener listener){
        scanImage(getBitmapByte(bitmap),bitmap.getWidth(),bitmap.getHeight(),listener);
    }

    public static void scanImage(byte[] data,int w,int h,final ScanListener listener){
        ImageScanner mImageScanner=new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
        Image barcode = new Image(w, h, "Y800");
        barcode.setData(data);
        int result = mImageScanner.scanImage(barcode);
        String rs="";
        if(result!=0){
            SymbolSet symSet = mImageScanner.getResults();
            if(symSet!=null&&!symSet.isEmpty()){
                for(Symbol sym : symSet){
                    rs=sym.getData();
                }
            }
        }
        if(listener!=null){
            final ScanResult sr=new ScanResult();
            sr.content=rs;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onScanResult(sr);
                }
            });
        }
    }

    public static byte[] getBitmapByte(Bitmap bitmap){   //将bitmap转化为byte[]类型也就是转化为二进制
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
