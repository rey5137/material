package com.rey.material.drawable;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class CheckBoxDrawable extends Drawable implements Animatable {
	
	private boolean mRunning = false;
	
	private Paint mPaint;
	
	private long mStartTime;
	private float mAnimProgress;
	private int mAnimDuration;
	private int mStrokeSize;
	private int mWidth;
	private int mHeight;
	private int mCornerRadius;
	private int mBoxSize;
	private int mTickColor;
	private int mPrevColor;
	private int mCurColor;
	private ColorStateList mStrokeColor;
	private RectF mBoxRect;
	private Path mTickPath;
	private float mTickPathProgress = -1f;
	private boolean mChecked = false;
	
	private boolean mInEditMode = false;
	private boolean mAnimEnable = true;
	
	private static final float[] TICK_DATA = new float[]{0f, 0.473f, 0.367f, 0.839f, 1f, 0.207f};
	private static final float FILL_TIME = 0.4f;
	
	private CheckBoxDrawable(int width, int height, int boxSize, int cornerRadius, int strokeSize, ColorStateList strokeColor, int tickColor, int animDuration){
		mWidth = width;
		mHeight = height;
		mBoxSize = boxSize;
		mCornerRadius = cornerRadius;
		mStrokeSize = strokeSize;
		mStrokeColor = strokeColor;
		mTickColor = tickColor;
		mAnimDuration = animDuration;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		
		mBoxRect = new RectF();
		mTickPath = new Path();
	}
	
	public void setInEditMode(boolean b){
		mInEditMode = b;
	}
	
	public void setAnimEnable(boolean b){
		mAnimEnable = b;
	}
	
	public boolean isAnimEnable(){
		return mAnimEnable;
	}
	
	@Override
	public int getIntrinsicWidth() {
		return mWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mHeight;
	}

	@Override
	public int getMinimumWidth() {
		return mWidth;
	}

	@Override
	public int getMinimumHeight() {
		return mHeight;
	}
	
	@Override
	public boolean isStateful() {
		return true;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		mBoxRect.set(bounds.exactCenterX() - mBoxSize / 2, bounds.exactCenterY() - mBoxSize / 2, bounds.exactCenterX() + mBoxSize / 2, bounds.exactCenterY() + mBoxSize / 2);
	}
	
	@Override
	public void draw(Canvas canvas) {					
		if(mChecked)
			drawChecked(canvas);
		else
			drawUnchecked(canvas);
	}
		
	private Path getTickPath(Path path, float x, float y, float size, float progress, boolean in){
		if(mTickPathProgress == progress)
			return path;
		
		mTickPathProgress = progress;
		
		float x1 = x + size * TICK_DATA[0];
		float y1 = y + size * TICK_DATA[1];
		float x2 = x + size * TICK_DATA[2];
		float y2 = y + size * TICK_DATA[3];
		float x3 = x + size * TICK_DATA[4];
		float y3 = y + size * TICK_DATA[5];
		
		float d1 = (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		float d2 = (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		float midProgress = d1 / (d1 + d2);
		
		path.reset();
		
		if(in){
			path.moveTo(x1, y1);			
			
			if(progress < midProgress){
				progress = progress / midProgress;
				path.lineTo(x1 * (1 - progress) + x2 * progress, y1 * (1 - progress) + y2 * progress);
			}
			else{
				progress = (progress - midProgress) / (1f - midProgress);
				path.lineTo(x2, y2);				
				path.lineTo(x2 * (1 - progress) + x3 * progress, y2 * (1 - progress) + y3 * progress);
			}	
		}	
		else{
			path.moveTo(x3, y3);
			
			if(progress < midProgress){
				progress = progress / midProgress;
				path.lineTo(x2, y2);
				path.lineTo(x1 * (1 - progress) + x2 * progress, y1 * (1 - progress) + y2 * progress);
			}
			else{
				progress = (progress - midProgress) / (1f - midProgress);
				path.lineTo(x2 * (1 - progress) + x3 * progress, y2 * (1 - progress) + y3 * progress);
			}
		}
		
		return path;
	}
	
	private void drawChecked(Canvas canvas){
		float size = mBoxSize - mStrokeSize * 2;
		float x = mBoxRect.left + mStrokeSize;
		float y = mBoxRect.top + mStrokeSize;
		
		if(isRunning()){
			if(mAnimProgress < FILL_TIME){
				float progress = mAnimProgress / FILL_TIME;
				float fillWidth = (mBoxSize - mStrokeSize) / 2f * progress;
				float padding = mStrokeSize / 2f + fillWidth / 2f - 0.5f;
				
				mPaint.setColor(ColorUtil.getMiddleColor(mPrevColor, mCurColor, progress));
				mPaint.setStrokeWidth(fillWidth);
				mPaint.setStyle(Paint.Style.STROKE);
				canvas.drawRect(mBoxRect.left + padding, mBoxRect.top + padding, mBoxRect.right - padding, mBoxRect.bottom - padding, mPaint);
								
				mPaint.setStrokeWidth(mStrokeSize);		
				canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);	
			}
			else{
				float progress = (mAnimProgress - FILL_TIME) / (1f - FILL_TIME);
				
				mPaint.setColor(mCurColor);
				mPaint.setStrokeWidth(mStrokeSize);
				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);			
				canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);	
				
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.MITER);
				mPaint.setStrokeCap(Paint.Cap.BUTT);
				mPaint.setColor(mTickColor);
				
				canvas.drawPath(getTickPath(mTickPath, x, y, size, progress, true), mPaint);				
			}						
		}
		else{
			mPaint.setColor(mCurColor);
			mPaint.setStrokeWidth(mStrokeSize);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);			
			canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);		
						
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.MITER);
			mPaint.setStrokeCap(Paint.Cap.BUTT);
			mPaint.setColor(mTickColor);
			
			canvas.drawPath(getTickPath(mTickPath, x, y, size, 1f, true), mPaint);	
		}		
	}
	
	private void drawUnchecked(Canvas canvas){		
		if(isRunning()){
			if(mAnimProgress < 1f - FILL_TIME){
				float size = mBoxSize - mStrokeSize * 2;
				float x = mBoxRect.left + mStrokeSize;
				float y = mBoxRect.top + mStrokeSize;
				float progress = mAnimProgress / (1f -FILL_TIME);
				
				mPaint.setColor(mPrevColor);
				mPaint.setStrokeWidth(mStrokeSize);
				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);			
				canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);	
				
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.MITER);
				mPaint.setStrokeCap(Paint.Cap.BUTT);
				mPaint.setColor(mTickColor);
				
				canvas.drawPath(getTickPath(mTickPath, x, y, size, progress, false), mPaint);	
			}
			else{
				float progress = (mAnimProgress + FILL_TIME - 1f) / FILL_TIME;				
				float fillWidth = (mBoxSize - mStrokeSize) / 2f * (1f - progress);
				float padding = mStrokeSize / 2f + fillWidth / 2f - 0.5f;
				
				mPaint.setColor(ColorUtil.getMiddleColor(mPrevColor, mCurColor, progress));
				mPaint.setStrokeWidth(fillWidth);
				mPaint.setStyle(Paint.Style.STROKE);
				canvas.drawRect(mBoxRect.left + padding, mBoxRect.top + padding, mBoxRect.right - padding, mBoxRect.bottom - padding, mPaint);
								
				mPaint.setStrokeWidth(mStrokeSize);		
				canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);	
			}
		}
		else{
			mPaint.setColor(mCurColor);
			mPaint.setStrokeWidth(mStrokeSize);
			mPaint.setStyle(Paint.Style.STROKE);			
			canvas.drawRoundRect(mBoxRect, mCornerRadius, mCornerRadius, mPaint);
		}
	}
	
	@Override
	protected boolean onStateChange(int[] state) {
		boolean checked = ViewUtil.hasState(state, android.R.attr.state_checked);		
		int color = mStrokeColor.getColorForState(state, mCurColor);
		boolean needRedraw = false;
				
		if(mChecked != checked){
			mChecked = checked;
			needRedraw = true;
			if(!mInEditMode && mAnimEnable)
				start();
		}
				
		if(mCurColor != color){
			mPrevColor = isRunning() ? mCurColor : color;
			mCurColor = color;
			needRedraw = true;
		}
		else if(!isRunning())
			mPrevColor = color;		
		
		return needRedraw;
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
	
	//Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/
		
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

	public static class Builder{
		
		private int mAnimDuration = 400;
		private int mStrokeSize = 4;
		private int mWidth = 64;
		private int mHeight = 64;
		private ColorStateList mStrokeColor;		
		private int mCornerRadius = 8;
		private int mBoxSize = 32;
		private int mTickColor = 0xFFFFFFFF;
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxDrawable, defStyleAttr, defStyleRes);
			
			width(a.getDimensionPixelSize(R.styleable.CheckBoxDrawable_cbd_width, ThemeUtil.dpToPx(context, 32)));
			height(a.getDimensionPixelSize(R.styleable.CheckBoxDrawable_cbd_height, ThemeUtil.dpToPx(context, 32)));
			boxSize(a.getDimensionPixelSize(R.styleable.CheckBoxDrawable_cbd_boxSize, ThemeUtil.dpToPx(context, 18)));
			cornerRadius(a.getDimensionPixelSize(R.styleable.CheckBoxDrawable_cbd_cornerRadius, ThemeUtil.dpToPx(context, 2)));
			strokeSize(a.getDimensionPixelSize(R.styleable.CheckBoxDrawable_cbd_strokeSize, ThemeUtil.dpToPx(context, 2)));
			strokeColor(a.getColorStateList(R.styleable.CheckBoxDrawable_cbd_strokeColor));
			tickColor(a.getColor(R.styleable.CheckBoxDrawable_cbd_tickColor, 0xFFFFFFFF));
			animDuration(a.getInt(R.styleable.CheckBoxDrawable_cbd_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			
			a.recycle();
			
			if(mStrokeColor == null){
				int[][] states = new int[][]{
						new int[]{-android.R.attr.state_checked},
						new int[]{android.R.attr.state_checked},
				};
				int[] colors = new int[]{
						ThemeUtil.colorControlNormal(context, 0xFF000000),
						ThemeUtil.colorControlActivated(context, 0xFF000000),
				};				
				strokeColor(new ColorStateList(states, colors));
			}			
		}
		
		public CheckBoxDrawable build(){
			if(mStrokeColor == null)
				mStrokeColor = ColorStateList.valueOf(0xFF000000);
			
			return new CheckBoxDrawable(mWidth, mHeight, mBoxSize, mCornerRadius, mStrokeSize, mStrokeColor, mTickColor, mAnimDuration);
		}
				
		public Builder width(int width){
			mWidth = width;
			return this;
		}
		
		public Builder height(int height){
			mHeight = height;
			return this;
		}
		
		public Builder strokeSize(int size){
			mStrokeSize = size;
			return this;
		}
		
		public Builder strokeColor(int color){
			mStrokeColor = ColorStateList.valueOf(color);
			return this;
		}
		
		public Builder strokeColor(ColorStateList color){
			mStrokeColor = color;
			return this;
		}
		
		public Builder tickColor(int color){
			mTickColor = color;
			return this;
		}
		
		public Builder cornerRadius(int radius){
			mCornerRadius = radius;
			return this;
		}
		
		public Builder boxSize(int size){
			mBoxSize = size;
			return this;
		}
		
		public Builder animDuration(int duration){
			mAnimDuration = duration;
			return this;
		}
	}
	
}