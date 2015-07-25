package com.rey.material.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.rey.material.util.ViewUtil;

public class DividerDrawable extends Drawable implements Animatable{
	
	private boolean mRunning = false;
	private long mStartTime;
	private float mAnimProgress;
	private int mAnimDuration;
	
	private Paint mPaint;
	private ColorStateList mColorStateList;
	private int mHeight;
	private int mPrevColor;
	private int mCurColor;
	
	private boolean mEnable = true;
	private PathEffect mPathEffect;		
	private Path mPath;
	
	private boolean mInEditMode = false;
	private boolean mAnimEnable = true;

    private int mPaddingLeft;
    private int mPaddingRight;

    public DividerDrawable(int height, ColorStateList colorStateList, int animDuration){
        this(height, 0, 0, colorStateList, animDuration);
    }

	public DividerDrawable(int height, int paddingLeft, int paddingRight, ColorStateList colorStateList, int animDuration){
		mHeight = height;
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
		mAnimDuration = animDuration;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mHeight);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		
		mPath = new Path();
		
		mAnimEnable = false;
		setColor(colorStateList);
		mAnimEnable = true;
	}

    public void setDividerHeight(int height){
        if(mHeight != height){
            mHeight = height;
            mPaint.setStrokeWidth(mHeight);
            invalidateSelf();
        }
    }

    public int getDividerHeight(){
        return mHeight;
    }

    public void setPadding(int left, int right){
        if(mPaddingLeft != left || mPaddingRight != right){
            mPaddingLeft = left;
            mPaddingRight = right;
            invalidateSelf();
        }
    }

    public int getPaddingLeft(){
        return mPaddingLeft;
    }

    public int getPaddingRight(){
        return mPaddingRight;
    }

	public void setInEditMode(boolean b){
		mInEditMode = b;
	}
	
	public void setAnimEnable(boolean b){
		mAnimEnable = b;
	}
	
	public void setColor(ColorStateList colorStateList){
		mColorStateList = colorStateList;
		onStateChange(getState());
	}

    public void setAnimationDuration(int duration){
        mAnimDuration = duration;
    }

	private PathEffect getPathEffect(){
		if(mPathEffect == null)
			mPathEffect = new DashPathEffect(new float[]{0.2f, mHeight * 2}, 0f);
		
		return mPathEffect;
	}
	
	@Override
	public void draw(Canvas canvas) {
        if(mHeight == 0)
            return;

		Rect bounds = getBounds();
		float y = bounds.bottom - mHeight / 2;

		if(!isRunning()){
			mPath.reset();
			mPath.moveTo(bounds.left + mPaddingLeft, y);
			mPath.lineTo(bounds.right - mPaddingRight, y);
			mPaint.setPathEffect(mEnable ? null : getPathEffect());
			mPaint.setColor(mCurColor);
			canvas.drawPath(mPath, mPaint);
		}
		else{
            float centerX = (bounds.right + bounds.left - mPaddingRight + mPaddingLeft) / 2f;
			float start = centerX * (1f - mAnimProgress) + (bounds.left + mPaddingLeft) * mAnimProgress;
			float end = centerX * (1f - mAnimProgress) + (bounds.right + mPaddingRight) * mAnimProgress;
			
			mPaint.setPathEffect(null);
			
			if(mAnimProgress < 1f){
				mPaint.setColor(mPrevColor);				
				mPath.reset();
				mPath.moveTo(bounds.left + mPaddingLeft, y);
				mPath.lineTo(start, y);
				mPath.moveTo(bounds.right - mPaddingRight, y);
				mPath.lineTo(end, y);				
				canvas.drawPath(mPath, mPaint);
			}
			
			mPaint.setColor(mCurColor);
			mPath.reset();
			mPath.moveTo(start, y);
			mPath.lineTo(end, y);
			canvas.drawPath(mPath, mPaint);
		}
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
		mEnable = ViewUtil.hasState(state, android.R.attr.state_enabled);		
		int color = mColorStateList.getColorForState(state, mCurColor);		
				
		if(mCurColor != color){
			if(!mInEditMode && mAnimEnable && mEnable && mAnimDuration > 0){
				mPrevColor = isRunning() ? mPrevColor : mCurColor;
				mCurColor = color;	
				start();				
			}
			else{
				mPrevColor = color;
				mCurColor = color;
			}		
			return true;
		}
		else if(!isRunning())
			mPrevColor = color;
			
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
