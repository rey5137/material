package com.rey.material.drawable;

import com.rey.material.util.ViewUtil;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class ArrowDrawable extends Drawable implements Animatable{
	
	private boolean mRunning = false;
	private long mStartTime;
	private float mAnimProgress;
	private int mAnimDuration;
	
	private Paint mPaint;
	private ColorStateList mColorStateList;
	private int mSize;
	private int mCurColor;
	private int mMode;
	private Interpolator mInterpolator;
	
	private Path mPath;
		
	public static int MODE_DOWN = 0;
	public static int MODE_UP = 1;
	
	private boolean mClockwise = true;
	
	public ArrowDrawable(int mode, int size, ColorStateList colorStateList, int animDuration, Interpolator interpolator, boolean clockwise){
		mSize = size;
		mAnimDuration = animDuration;
		mMode = mode;
		mInterpolator = interpolator;
		if(mInterpolator == null)
			mInterpolator = new DecelerateInterpolator();
		mClockwise = clockwise;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		
		mPath = new Path();
		
		setColor(colorStateList);
	}
	
	public void setColor(ColorStateList colorStateList){
		mColorStateList = colorStateList;
		onStateChange(getState());
	}
	
	public void setMode(int mode, boolean animation){
		if(mMode != mode){
			mMode = mode;			
			if(animation && mAnimDuration > 0)
				start();			
			else
				invalidateSelf();
		}
	}	
	
	public int getMode(){
		return mMode;
	}
		
	@Override
	protected void onBoundsChange(Rect bounds) {
		float x = bounds.exactCenterX();
		float y = bounds.exactCenterY();
		
		mPath.reset();
		mPath.moveTo(x, y + mSize / 2f);
		mPath.lineTo(x - mSize, y - mSize / 2f);
		mPath.lineTo(x + mSize, y - mSize / 2f);
		mPath.close();
	}

	@Override
	public void draw(Canvas canvas) {
		int saveCount = canvas.save();
		Rect bounds = getBounds();
		
		if(!isRunning()){
			if(mMode == MODE_UP)
				canvas.rotate(180, bounds.exactCenterX(), bounds.exactCenterY());
		}
		else{
			float value = mInterpolator.getInterpolation(mAnimProgress);
			float degree;
			
			if(mClockwise){
				if(mMode == MODE_UP) // move down > up
					degree = 180 * value;
				else // move up > down
					degree = 180 * (1 + value);
			}
			else{
				if(mMode == MODE_UP) // move down > up
					degree = -180 * value;
				else // move up > down
					degree = -180 * (1 + value);
			}
			
			canvas.rotate(degree, bounds.exactCenterX(), bounds.exactCenterY());
		}
		
		mPaint.setColor(mCurColor);
		canvas.drawPath(mPath, mPaint);
		
		canvas.restoreToCount(saveCount);
	}
			
	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);			
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public boolean isStateful() {
		return true;
	}

	@Override
	protected boolean onStateChange(int[] state) {
		int color = mColorStateList.getColorForState(state, mCurColor);		
				
		if(mCurColor != color){
			mCurColor = color;	
			return true;
		}
			
		return false;
	}

	private void resetAnimation(){	
		mStartTime = SystemClock.uptimeMillis();
		mAnimProgress = 0f;
	}
	
	@Override
	public void start() {
		resetAnimation();			
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	    invalidateSelf();  
	}

	@Override
	public void stop() {
		mRunning = false;
		unscheduleSelf(mUpdater);
		invalidateSelf();
	}

	@Override
	public boolean isRunning() {
		return mRunning;
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		mRunning = true;
	    super.scheduleSelf(what, when);
	}
	
	private final Runnable mUpdater = new Runnable() {

	    @Override
	    public void run() {
	    	update();
	    }
		    
	};
		
	private void update(){
		long curTime = SystemClock.uptimeMillis();
		mAnimProgress = Math.min(1f, (float)(curTime - mStartTime) / mAnimDuration);	
		
		if(mAnimProgress == 1f)
			mRunning = false;
				
    	if(isRunning())
    		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
    	
    	invalidateSelf();
	}
	
}
