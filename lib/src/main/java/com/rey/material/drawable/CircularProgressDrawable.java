package com.rey.material.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.ProgressView;

public class CircularProgressDrawable extends Drawable implements Animatable {
	
	private long mLastUpdateTime;
	private long mLastProgressStateTime;
	private long mLastRunStateTime;
			
	private int mProgressState;
	
	private static final int PROGRESS_STATE_HIDE = -1;
	private static final int PROGRESS_STATE_STRETCH = 0;
	private static final int PROGRESS_STATE_KEEP_STRETCH = 1;
	private static final int PROGRESS_STATE_SHRINK = 2;
	private static final int PROGRESS_STATE_KEEP_SHRINK = 3;
	
	private int mRunState = RUN_STATE_STOPPED;
	
	private static final int RUN_STATE_STOPPED = 0;
	private static final int RUN_STATE_STARTING = 1;
	private static final int RUN_STATE_STARTED = 2;
	private static final int RUN_STATE_RUNNING = 3;
	private static final int RUN_STATE_STOPPING = 4;
	
	private Paint mPaint;
	private RectF mRect;
	private float mStartAngle;
	private float mSweepAngle;
	private int mStrokeColorIndex;
	
	private int mPadding;	
	private float mInitialAngle;	
	private float mProgressPercent;
	private float mSecondaryProgressPercent;
	private float mMaxSweepAngle;	
	private float mMinSweepAngle;	
	private int mStrokeSize;
	private int[] mStrokeColors;
	private int mStrokeSecondaryColor;	
	private boolean mReverse;
	private int mRotateDuration;
	private int mTransformDuration;
	private int mKeepDuration;	
	private float mInStepPercent;
	private int[] mInColors;
	private int mInAnimationDuration;
	private int mOutAnimationDuration;
	private int mProgressMode;
	private Interpolator mTransformInterpolator; 
		
	private CircularProgressDrawable(int padding, float initialAngle, float progressPercent, float secondaryProgressPercent, float maxSweepAngle, float minSweepAngle, int strokeSize, int[] strokeColors, int strokeSecondaryColor, boolean reverse, int rotateDuration, int transformDuration, int keepDuration, Interpolator transformInterpolator, int progressMode, int inAnimDuration, float inStepPercent, int[] inStepColors, int outAnimDuration){
		mPadding = padding;
		mInitialAngle = initialAngle;
		setProgress(progressPercent);
		setSecondaryProgress(secondaryProgressPercent);
		mMaxSweepAngle = maxSweepAngle;
		mMinSweepAngle = minSweepAngle;
		mStrokeSize = strokeSize;
		mStrokeColors = strokeColors;
		mStrokeSecondaryColor = strokeSecondaryColor;
		mReverse = reverse;
		mRotateDuration = rotateDuration;
		mTransformDuration = transformDuration;
		mKeepDuration = keepDuration;
		mTransformInterpolator = transformInterpolator;
		mProgressMode = progressMode;
		mInAnimationDuration = inAnimDuration;
		mInStepPercent = inStepPercent;
		mInColors = inStepColors;
		mOutAnimationDuration = outAnimDuration;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);		
		
		mRect = new RectF();
	}
		
	@Override
	public void draw(Canvas canvas) {			
		switch (mProgressMode) {
			case ProgressView.MODE_DETERMINATE:
				drawDeterminate(canvas);
				break;
			case ProgressView.MODE_INDETERMINATE:
				drawIndeterminate(canvas);
				break;
		}		
	}
	
	private void drawDeterminate(Canvas canvas){
		Rect bounds = getBounds();		
		float radius = 0f;
		float size = 0f;
		
		if(mRunState == RUN_STATE_STARTING){
			size = (float)mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;				
			if(size > 0)			
				radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;	
		}
		else if(mRunState == RUN_STATE_STOPPING){
			size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;		
			if(size > 0)
				radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;
		}
		else if(mRunState != RUN_STATE_STOPPED){
			size = mStrokeSize;
			radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize) / 2f;
		}
		
		if(radius > 0){
			float x = (bounds.left + bounds.right) / 2f;
			float y = (bounds.top + bounds.bottom) / 2f;
			
			mPaint.setStrokeWidth(size);
			mPaint.setStyle(Paint.Style.STROKE);
			
			if(mProgressPercent == 1f){
				mPaint.setColor(mStrokeColors[0]);
				canvas.drawCircle(x, y, radius, mPaint);
			}
			else if(mProgressPercent == 0f){
				mPaint.setColor(mStrokeSecondaryColor);
				canvas.drawCircle(x, y, radius, mPaint);
			}
			else{
				float sweepAngle = (mReverse ? -360 : 360) * mProgressPercent;
				
				mRect.set(x - radius, y - radius, x + radius, y + radius);				
				mPaint.setColor(mStrokeSecondaryColor);			
				canvas.drawArc(mRect, mInitialAngle + sweepAngle, (mReverse ? -360 : 360) - sweepAngle, false, mPaint);
							
				mPaint.setColor(mStrokeColors[0]);
				canvas.drawArc(mRect, mInitialAngle, sweepAngle, false, mPaint);			
			}
		}		
	}
		
	private int getIndeterminateStrokeColor(){
		if(mProgressState != PROGRESS_STATE_KEEP_SHRINK || mStrokeColors.length == 1)
			return mStrokeColors[mStrokeColorIndex];
		
		float value = Math.max(0f, Math.min(1f, (float)(SystemClock.uptimeMillis() - mLastProgressStateTime) / mKeepDuration));
		int prev_index = mStrokeColorIndex == 0 ? mStrokeColors.length - 1 : mStrokeColorIndex - 1;
		
		return ColorUtil.getMiddleColor(mStrokeColors[prev_index], mStrokeColors[mStrokeColorIndex], value);
	}
	
	private void drawIndeterminate(Canvas canvas){
		if(mRunState == RUN_STATE_STARTING){
			Rect bounds = getBounds();				
			float x = (bounds.left + bounds.right) / 2f;
			float y = (bounds.top + bounds.bottom) / 2f;
			float maxRadius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2) / 2f;
			
			float stepTime = 1f / (mInStepPercent * (mInColors.length + 2) + 1);
			float time = (float)(SystemClock.uptimeMillis() - mLastRunStateTime) / mInAnimationDuration;
			float steps =  time / stepTime;
							
			float outerRadius = 0f;
			float innerRadius = 0f; 
			
			for(int i = (int)Math.floor(steps); i >= 0; i--){
				innerRadius = outerRadius;
				outerRadius = Math.min(1f, (steps - i) * mInStepPercent) * maxRadius;
									
				if(i >= mInColors.length)
					continue;
				
				if(innerRadius == 0){
					mPaint.setColor(mInColors[i]);
					mPaint.setStyle(Paint.Style.FILL);
					canvas.drawCircle(x, y, outerRadius, mPaint);
				}
				else if(outerRadius > innerRadius){
					float radius = (innerRadius + outerRadius) / 2;
					mRect.set(x - radius, y - radius, x + radius, y + radius);	
					
					mPaint.setStrokeWidth(outerRadius - innerRadius);
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setColor(mInColors[i]);
					
					canvas.drawCircle(x, y, radius, mPaint);
				}
				else
					break;
			}
			
			if(mProgressState == PROGRESS_STATE_HIDE){
				if(steps >= 1 / mInStepPercent || time >= 1)
					resetAnimation();
			}					
			else{
				float radius = maxRadius - mStrokeSize / 2f;
				
				mRect.set(x - radius, y - radius, x + radius, y + radius);				
				mPaint.setStrokeWidth(mStrokeSize);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setColor(getIndeterminateStrokeColor());
				
				canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
			}
		}
		else if(mRunState == RUN_STATE_STOPPING){
			float size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;								
			
			if(size > 0){
				Rect bounds = getBounds();
				float radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;
				float x = (bounds.left + bounds.right) / 2f;
				float y = (bounds.top + bounds.bottom) / 2f;
				
				mRect.set(x - radius, y - radius, x + radius, y + radius);					
				mPaint.setStrokeWidth(size);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setColor(getIndeterminateStrokeColor());
				
				canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
			}
		}
		else if(mRunState != RUN_STATE_STOPPED){
			Rect bounds = getBounds();
			float radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize) / 2f;
			float x = (bounds.left + bounds.right) / 2f;
			float y = (bounds.top + bounds.bottom) / 2f;
			
			mRect.set(x - radius, y - radius, x + radius, y + radius);				
			mPaint.setStrokeWidth(mStrokeSize);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(getIndeterminateStrokeColor());
			
			canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
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
	
	public int getProgressMode(){
		return mProgressMode;
	}

    public void setProgressMode(int mode){
        if(mProgressMode != mode) {
            mProgressMode = mode;
            invalidateSelf();
        }
    }

	public float getProgress(){
		return mProgressPercent;
	}
	
	public float getSecondaryProgress(){
		return mSecondaryProgressPercent;
	}
	
	public void setProgress(float percent){
		percent = Math.min(1f, Math.max(0f, percent));
		if(mProgressPercent != percent){
			mProgressPercent = percent;
			if(isRunning())
				invalidateSelf();
			else if(mProgressPercent != 0f)
				start();
		}
	}
	
	public void setSecondaryProgress(float percent){
		percent = Math.min(1f, Math.max(0f, percent));
		if(mSecondaryProgressPercent != percent){
			mSecondaryProgressPercent = percent;
			if(isRunning())
				invalidateSelf();
			else if(mSecondaryProgressPercent != 0f)
				start();
		}
	}
	
	//Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/
	
	private void resetAnimation(){		
		mLastUpdateTime = SystemClock.uptimeMillis();
		mLastProgressStateTime = mLastUpdateTime;
		mStartAngle = mInitialAngle;
		mStrokeColorIndex = 0;
		mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
		mProgressState = PROGRESS_STATE_STRETCH;
	}
	
	@Override
	public void start() {
		start(mInAnimationDuration > 0);	    
	}

	@Override
	public void stop() {
		stop(mOutAnimationDuration > 0);
	}
		
	private void start(boolean withAnimation){
		if(isRunning()) 
			return;
						
		if(withAnimation){
			mRunState = RUN_STATE_STARTING;
			mLastRunStateTime = SystemClock.uptimeMillis();
			mProgressState = PROGRESS_STATE_HIDE;
		}
		else   
		    resetAnimation();
		
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	    invalidateSelf();  
	}
	
	private void stop(boolean withAnimation){
		if(!isRunning()) 
			return;
		
		if(withAnimation){				
			mLastRunStateTime = SystemClock.uptimeMillis();
			if(mRunState == RUN_STATE_STARTED){
				scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
			    invalidateSelf();  
			}
			mRunState = RUN_STATE_STOPPING;	
		}
		else{			
			mRunState = RUN_STATE_STOPPED;
			unscheduleSelf(mUpdater);
			invalidateSelf();
		}		
	}
	
	@Override
	public boolean isRunning() {
		return mRunState != RUN_STATE_STOPPED;
	}
		
	@Override
	public void scheduleSelf(Runnable what, long when) {
		if(mRunState == RUN_STATE_STOPPED)
			mRunState = mInAnimationDuration > 0 ? RUN_STATE_STARTING : RUN_STATE_RUNNING;
	    super.scheduleSelf(what, when);
	}
	
	private final Runnable mUpdater = new Runnable() {

	    @Override
	    public void run() {
	    	update();
	    }
		    
	};
		
	private void update(){
		switch (mProgressMode) {
			case ProgressView.MODE_DETERMINATE:
				updateDeterminate();
				break;
			case ProgressView.MODE_INDETERMINATE:
				updateIndeterminate();
				break;
		}
	}
	
	private void updateDeterminate(){
		long curTime = SystemClock.uptimeMillis();
		
		if(mRunState == RUN_STATE_STARTING){
    		if(curTime - mLastRunStateTime > mInAnimationDuration){
    			mRunState = RUN_STATE_STARTED;
    			return;
    		}
    	}
    	else if(mRunState == RUN_STATE_STOPPING){
    		if(curTime - mLastRunStateTime > mOutAnimationDuration){
    			stop(false);
    			return;
    		}
    	}
		
    	if(isRunning())
    		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

    	invalidateSelf();
	}
	
	private void updateIndeterminate(){
		//update animation
    	long curTime = SystemClock.uptimeMillis();
    	float rotateOffset = (curTime - mLastUpdateTime) * 360f / mRotateDuration;	    	
    	if(mReverse)
    		rotateOffset = -rotateOffset;	    	
    	mLastUpdateTime = curTime;    		    	

    	switch (mProgressState) {
			case PROGRESS_STATE_STRETCH:
				if(mTransformDuration <= 0){
					mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
					mProgressState = PROGRESS_STATE_KEEP_STRETCH;
					mStartAngle += rotateOffset;
					mLastProgressStateTime = curTime;
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					float maxAngle = mReverse ? -mMaxSweepAngle : mMaxSweepAngle;
					float minAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
					
					mStartAngle += rotateOffset;
					mSweepAngle = mTransformInterpolator.getInterpolation(value) * (maxAngle - minAngle) + minAngle;
										
					if(value > 1f){
						mSweepAngle = maxAngle;
		    			mProgressState = PROGRESS_STATE_KEEP_STRETCH;
		    			mLastProgressStateTime = curTime;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_STRETCH:
				mStartAngle += rotateOffset;
				
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_SHRINK;
					mLastProgressStateTime = curTime;
				}
				break;
			case PROGRESS_STATE_SHRINK:		
				if(mTransformDuration <= 0){
					mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
					mProgressState = PROGRESS_STATE_KEEP_SHRINK;
					mStartAngle += rotateOffset;
					mLastProgressStateTime = curTime;
					mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					float maxAngle = mReverse ? -mMaxSweepAngle : mMaxSweepAngle;
					float minAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
					
					float newSweepAngle = (1f - mTransformInterpolator.getInterpolation(value)) * (maxAngle - minAngle) + minAngle;						
					mStartAngle += rotateOffset + mSweepAngle - newSweepAngle;
					mSweepAngle = newSweepAngle;
					
					if(value > 1f){
						mSweepAngle = minAngle;
		    			mProgressState = PROGRESS_STATE_KEEP_SHRINK;
		    			mLastProgressStateTime = curTime;
		    			mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_SHRINK:
				mStartAngle += rotateOffset;
				
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_STRETCH;
					mLastProgressStateTime = curTime;
				}
				break;					
		}
    	
    	if(mRunState == RUN_STATE_STARTING){
    		if(curTime - mLastRunStateTime > mInAnimationDuration){
    			mRunState = RUN_STATE_RUNNING;	    		
    			if(mProgressState == PROGRESS_STATE_HIDE)
    				resetAnimation();
    		}
    	}
    	else if(mRunState == RUN_STATE_STOPPING){
    		if(curTime - mLastRunStateTime > mOutAnimationDuration){
    			stop(false);
    			return;
    		}
    	}
    	
    	if (isRunning())
    		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

    	invalidateSelf();
	}
	
	public static class Builder{
		private int mPadding;
		private float mInitialAngle;
		private float mProgressPercent;
		private float mSecondaryProgressPercent;
		private float mMaxSweepAngle;
		private float mMinSweepAngle;	
		private int mStrokeSize;
		private int[] mStrokeColors;
		private int mStrokeSecondaryColor;		
		private boolean mReverse;
		private int mRotateDuration;
		private int mTransformDuration;
		private int mKeepDuration;	
		private Interpolator mTransformInterpolator;
		private int mProgressMode;
		private float mInStepPercent;
		private int[] mInColors;
		private int mInAnimationDuration;
		private int mOutAnimationDuration;
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressDrawable, defStyleAttr, defStyleRes);
			int resId;
			
			padding(a.getDimensionPixelSize(R.styleable.CircularProgressDrawable_cpd_padding, 0));
			initialAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_initialAngle, 0));
			progressPercent(a.getFloat(R.styleable.CircularProgressDrawable_pv_progress, 0));
			secondaryProgressPercent(a.getFloat(R.styleable.CircularProgressDrawable_pv_secondaryProgress, 0));
			maxSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_maxSweepAngle, 270));
			minSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_minSweepAngle, 1));
			strokeSize(a.getDimensionPixelSize(R.styleable.CircularProgressDrawable_cpd_strokeSize, ThemeUtil.dpToPx(context, 4)));
			strokeColors(a.getColor(R.styleable.CircularProgressDrawable_cpd_strokeColor, ThemeUtil.colorPrimary(context, 0xFF000000)));
			if((resId = a.getResourceId(R.styleable.CircularProgressDrawable_cpd_strokeColors, 0)) != 0){
				TypedArray ta = context.getResources().obtainTypedArray(resId);				        	
				int[] colors = new int[ta.length()];
				for(int j = 0; j < ta.length(); j++)
				    colors[j] = ta.getColor(j, 0);				        	
				ta.recycle();
				strokeColors(colors);
			}
			strokeSecondaryColor(a.getColor(R.styleable.CircularProgressDrawable_cpd_strokeSecondaryColor, 0));
			reverse(a.getBoolean(R.styleable.CircularProgressDrawable_cpd_reverse, false));
			rotateDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_rotateDuration, context.getResources().getInteger(android.R.integer.config_longAnimTime)));
			transformDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_transformDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			keepDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_keepDuration, context.getResources().getInteger(android.R.integer.config_shortAnimTime)));
			if((resId = a.getResourceId(R.styleable.CircularProgressDrawable_cpd_transformInterpolator, 0)) != 0)
				transformInterpolator(AnimationUtils.loadInterpolator(context, resId));
			progressMode(a.getInteger(R.styleable.CircularProgressDrawable_pv_progressMode, ProgressView.MODE_INDETERMINATE));
			inAnimDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_inAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			if((resId = a.getResourceId(R.styleable.CircularProgressDrawable_cpd_inStepColors, 0)) != 0){
				TypedArray ta = context.getResources().obtainTypedArray(resId);				        	
				int[] colors = new int[ta.length()];
				for(int j = 0; j < ta.length(); j++)
				    colors[j] = ta.getColor(j, 0);				        	
				ta.recycle();
				inStepColors(colors);
			}
			inStepPercent(a.getFloat(R.styleable.CircularProgressDrawable_cpd_inStepPercent, 0.5f));
			outAnimDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_outAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			a.recycle();
		}
		
		public CircularProgressDrawable build(){
			if(mStrokeColors == null)
				mStrokeColors = new int[]{0xFF0099FF};
			
			if(mInColors == null && mInAnimationDuration > 0)
				mInColors = new int[]{0xFFB5D4FF, 0xFFDEEAFC, 0xFFFAFFFE};
			
			if(mTransformInterpolator == null)
				mTransformInterpolator = new DecelerateInterpolator();
			
			return new CircularProgressDrawable(mPadding, mInitialAngle, mProgressPercent, mSecondaryProgressPercent, mMaxSweepAngle, mMinSweepAngle, mStrokeSize, mStrokeColors, mStrokeSecondaryColor, mReverse, mRotateDuration, mTransformDuration, mKeepDuration, mTransformInterpolator, mProgressMode, mInAnimationDuration, mInStepPercent, mInColors, mOutAnimationDuration);
		}
		
		public Builder padding(int padding){
			mPadding = padding;
			return this;
		}
		
		public Builder initialAngle(float angle){
			mInitialAngle = angle;
			return this;
		}
		
		public Builder progressPercent(float percent){
			mProgressPercent = percent;
			return this;
		}
		
		public Builder secondaryProgressPercent(float percent){
			mSecondaryProgressPercent = percent;
			return this;
		}
		
		public Builder maxSweepAngle(float angle){
			mMaxSweepAngle = angle;
			return this;
		}
		
		public Builder minSweepAngle(float angle){
			mMinSweepAngle = angle;
			return this;
		}
		
		public Builder strokeSize(int strokeSize){
			mStrokeSize = strokeSize;
			return this;
		}
		
		public Builder strokeColors(int... strokeColors){
			mStrokeColors = strokeColors;
			return this;
		}
		
		public Builder strokeSecondaryColor(int color){
			mStrokeSecondaryColor = color;
			return this;
		}
		
		public Builder reverse(boolean reverse){
			mReverse = reverse;
			return this;
		}
		
		public Builder reverse(){
			return reverse(true);
		}
		
		public Builder rotateDuration(int duration){
			mRotateDuration = duration;
			return this;
		}
		
		public Builder transformDuration(int duration){
			mTransformDuration = duration;
			return this;
		}
		
		public Builder keepDuration(int duration){
			mKeepDuration = duration;
			return this;
		}
		
		public Builder transformInterpolator(Interpolator interpolator){
			mTransformInterpolator = interpolator;
			return this;
		}
		
		public Builder progressMode(int mode){
			mProgressMode = mode;
			return this;
		}
				
		public Builder inAnimDuration(int duration){
			mInAnimationDuration = duration;
			return this;
		}
		
		public Builder inStepPercent(float percent){
			mInStepPercent = percent;
			return this;
		}
		
		public Builder inStepColors(int... colors){
			mInColors = colors;
			return this;
		}
		
		public Builder outAnimDuration(int duration){
			mOutAnimationDuration = duration;
			return this;
		}
		
	}
}
