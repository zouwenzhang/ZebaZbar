package cn.bertsir.zbar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class ZBarScanFGView extends View{
	private Paint paint;
	private Rect mScanRect;
	private Rect mBarRect;
	private int paintWidth=1;
	private int paintHeight=1;
	private Paint mPaint;
	LinearGradient lg;
	private long startTime=0;//开始时间
	private int rectHeight;//扫描框高度
	private long runTime=3500;//动画时间
	private int bgColor;
	private int lineColor=-1;
	public ZBarScanFGView(Context context) {
		super(context);
		init(context);
	}
	public ZBarScanFGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public ZBarScanFGView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	private void init(Context context){
		mPaint=new Paint();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintWidth=dip2px(context, 3);
		paintHeight=dip2px(context, 15);
		bgColor=Color.parseColor("#40000000");
		lineColor=Color.parseColor("#ff1616");
	}
	public void setRect(Rect rect){
		mScanRect=rect;
		int w=mScanRect.right-mScanRect.left;
		rectHeight=w;
		w=w/6/2;
		mBarRect=new Rect(mScanRect);
		mBarRect.left=mScanRect.left;
		mBarRect.right=mScanRect.right;
		mBarRect.bottom=mBarRect.top+paintWidth;
		int[] colors=new int[]{
				Color.parseColor("#00000000"),
				lineColor,
				Color.parseColor("#00000000"),};
		float[] positions=new float[]{0f,0.5f,1f};
		lg=new LinearGradient(mBarRect.left,mBarRect.top,mBarRect.right,mBarRect.bottom,colors,positions,TileMode.CLAMP); 
		//参数一为渐变起初点坐标x位置，参数二为y轴位置，参数三和四分辨对应渐变终点，最后参数为平铺方式，这里设置为镜像
		mPaint.setShader(lg);
		startTime=System.currentTimeMillis();
		invalidate();
	}

	public void setBgColor(int color){
		bgColor=color;
	}

	public void setLineColor(int color){
		lineColor=color;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (mScanRect == null) {
			return;
		}
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(bgColor);
		canvas.drawRect(0, 0, getMeasuredWidth(), mScanRect.top, paint);
		canvas.drawRect(0, mScanRect.top, mScanRect.left, mScanRect.bottom + 1,
				paint);
		canvas.drawRect(mScanRect.right + 1, mScanRect.top, getMeasuredWidth(),
				mScanRect.bottom + 1, paint);
		canvas.drawRect(0, mScanRect.bottom + 1, getMeasuredWidth(),
				getMeasuredHeight(), paint);

		paint.setColor(lineColor);

		// 左上角
		canvas.drawRect(mScanRect.left, mScanRect.top, mScanRect.left + paintHeight,
				mScanRect.top + paintWidth, paint);
		canvas.drawRect(mScanRect.left, mScanRect.top, mScanRect.left + paintWidth,
				mScanRect.top + paintHeight, paint);
		// 右上角
		canvas.drawRect(mScanRect.right - paintHeight, mScanRect.top, mScanRect.right,
				mScanRect.top + paintWidth, paint);
		canvas.drawRect(mScanRect.right - paintWidth, mScanRect.top, mScanRect.right+1,
				mScanRect.top + paintHeight, paint);
		// 左下角
		canvas.drawRect(mScanRect.left, mScanRect.bottom - paintHeight,
				mScanRect.left + paintWidth, mScanRect.bottom+1, paint);
		canvas.drawRect(mScanRect.left, mScanRect.bottom - paintWidth,
				mScanRect.left + paintHeight, mScanRect.bottom+1, paint);
		// 右下角
		canvas.drawRect(mScanRect.right - paintHeight, mScanRect.bottom - paintWidth,
				mScanRect.right, mScanRect.bottom+1, paint);
		canvas.drawRect(mScanRect.right - paintWidth, mScanRect.bottom - paintHeight,
				mScanRect.right+1, mScanRect.bottom, paint);
		
		long nowt=(System.currentTimeMillis()-startTime)%runTime;
		int h=(int) (rectHeight*(nowt*1f/runTime));
		canvas.save();
		canvas.translate(0, h);
		canvas.drawRect(mBarRect.left, mBarRect.top, mBarRect.right, mBarRect.bottom, mPaint);
		canvas.restore();
		postInvalidate();
	}
	public static int dip2px(Context context, float dpValue) {
		if (context == null) {
			return (int) dpValue;
		}
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
