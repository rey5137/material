package com.rey.material.drawable;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.ProgressView;

public class LinearProgressDrawable extends Drawable implements Animatable {
	
	private long mLastUpdateTime;
	private long mLastProgressStateTime;
	private long mLastRunStateTime;
			
	private int mProgressState;
	
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
	
	public static final int ALIGN_TOP = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_BOTTOM = 2;
	
	private Paint mPaint;
	private float mStartLine;
	private float mLineWidth;
	private int mStrokeColorIndex;
	private float mAnimTime;
		
	private Path mPath;
	private DashPathEffect mPathEffect;
	
	private float mProgressPercent;
	private float mSecondaryProgressPercent;
	private int mMaxLineWidth;
	private float mMaxLineWidthPercent;
	private int mMinLineWidth;
	private float mMinLineWidthPercent;
	private int mStrokeSize;
	private int mVerticalAlign;
	private int[] mStrokeColors;
	private int mStrokeSecondaryColor;	
	private boolean mReverse;
	private int mTravelDuration;
	private int mTransformDuration;
	private int mKeepDuration;	
	private int mInAnimationDuration;
	private int mOutAnimationDuration;
	private int mProgressMode;
	private Interpolator mTransformInterpolator; 
		
	private LinearProgressDrawable(float progressPercent, float secondaryProgressPercent, int maxLineWidth, float maxLineWidthPercent, int minLineWidth, float minLineWidthPercent, int strokeSize, int verticalAlign, int[] strokeColors, int strokeSecondaryColor, boolean reverse, int travelDuration, int transformDuration, int keepDuration, Interpolator transformInterpolator, int progressMode, int inAnimDuration, int outAnimDuration){
		setProgress(progressPercent);
		setSecondaryProgress(secondaryProgressPercent);
		mMaxLineWidth = maxLineWidth;
		mMaxLineWidthPercent = maxLineWidthPercent;
		mMinLineWidth = minLineWidth;
		mMinLineWidthPercent = minLineWidthPercent;
		mStrokeSize = strokeSize;
		mVerticalAlign = verticalAlign;
		mStrokeColors = strokeColors;
		mStrokeSecondaryColor = strokeSecondaryColor;
		mReverse = reverse;
		mTravelDuration = travelDuration;
		mTransformDuration = transformDuration;
		mKeepDuration = keepDuration;
		mTransformInterpolator = transformInterpolator;
		mProgressMode = progressMode;
		mInAnimationDuration = inAnimDuration;
		mOutAnimationDuration = outAnimDuration;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);	
		
		mPath = new Path();
	}

    public void applyStyle(Context context, int resId){
        TypedArray a = context.obtainStyledAttributes(resId, R.styleable.LinearProgressDrawable);

        int strokeColor = 0;
        boolean strokeColorDefined = false;
        int[] strokeColors = null;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.LinearProgressDrawable_pv_progress)
                setProgress(a.getFloat(attr, 0));
            else if(attr == R.styleable.LinearProgressDrawable_pv_secondaryProgress)
                setSecondaryProgress(a.getFloat(attr, 0));
            else if(attr == R.styleable.LinearProgressDrawable_lpd_maxLineWidth){
                TypedValue value = a.peekValue(attr);
                if(value.type == TypedValue.TYPE_FRACTION) {
                    mMaxLineWidthPercent = a.getFraction(attr, 1, 1, 0.75f);
                    mMaxLineWidth = 0;
                }
                else {
                    mMaxLineWidth = a.getDimensionPixelSize(attr, 0);
                    mMaxLineWidthPercent = 0f;
                }
            }
            else if(attr == R.styleable.LinearProgressDrawable_lpd_minLineWidth){
                TypedValue value = a.peekValue(attr);
                if(value.type == TypedValue.TYPE_FRACTION) {
                    mMinLineWidthPercent = a.getFraction(attr, 1, 1, 0.25f);
                    mMinLineWidth = 0;
                }
                else {
                    mMinLineWidth = a.getDimensionPixelSize(attr, 0);
                    mMinLineWidthPercent = 0f;
                }
            }
            else if(attr == R.styleable.LinearProgressDrawable_lpd_strokeSize)
                mStrokeSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_verticalAlign)
                mVerticalAlign = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_strokeColor) {
                strokeColor = a.getColor(attr, 0);
                strokeColorDefined = true;
            }
            else if(attr == R.styleable.LinearProgressDrawable_lpd_strokeColors){
                TypedArray ta = context.getResources().obtainTypedArray(a.getResourceId(attr, 0));
                strokeColors = new int[ta.length()];
                for(int j = 0; j < ta.length(); j++)
                    strokeColors[j] = ta.getColor(j, 0);
                ta.recycle();
            }
            else if(attr == R.styleable.LinearProgressDrawable_lpd_strokeSecondaryColor)
                mStrokeSecondaryColor = a.getColor(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_reverse)
                mReverse = a.getBoolean(attr, false);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_travelDuration)
                mTravelDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_transformDuration)
                mTransformDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_keepDuration)
                mKeepDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_transformInterpolator)
                mTransformInterpolator = AnimationUtils.loadInterpolator(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.LinearProgressDrawable_pv_progressMode)
                mProgressMode = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_inAnimDuration)
                mInAnimationDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.LinearProgressDrawable_lpd_outAnimDuration)
                mOutAnimationDuration = a.getInteger(attr, 0);
        }

        a.recycle();

        if(strokeColors != null)
            mStrokeColors = strokeColors;
        else if(strokeColorDefined)
            mStrokeColors = new int[]{strokeColor};

        if(mStrokeColorIndex >= mStrokeColors.length)
            mStrokeColorIndex = 0;

        invalidateSelf();
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
			case ProgressView.MODE_BUFFER:
				drawBuffer(canvas);
				break;
			case ProgressView.MODE_QUERY:
				drawQuery(canvas);
				break;
		}		
	}
	
	private void drawLinePath(Canvas canvas, float x1, float y1, float x2, float y2, Paint paint){
		mPath.reset();
		mPath.moveTo(x1, y1);
		mPath.lineTo(x2, y2);
		canvas.drawPath(mPath, paint);
	}
	
	private void drawDeterminate(Canvas canvas){
		Rect bounds = getBounds();		
		int width = bounds.width();
		float size = 0f;
		
		if(mRunState == RUN_STATE_STARTING)
			size = (float)mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;			
		else if(mRunState == RUN_STATE_STOPPING)
			size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;				
		else if(mRunState != RUN_STATE_STOPPED)
			size = mStrokeSize;		
		
		if(size > 0){
			float y = 0;
			float lineWidth = width * mProgressPercent;
			
			switch (mVerticalAlign) {
				case ALIGN_TOP:
					y = size / 2;
					break;
				case ALIGN_CENTER:
					y = bounds.height() / 2f;
					break;
				case ALIGN_BOTTOM:
					y = bounds.height() - size / 2;
					break;
			}
						
			mPaint.setStrokeWidth(size);
			mPaint.setStyle(Paint.Style.STROKE);
			
			if(mProgressPercent != 1f){
				mPaint.setColor(mStrokeSecondaryColor);
				
				if(mReverse)					
					canvas.drawLine(0, y, width - lineWidth, y, mPaint);
				else
					canvas.drawLine(lineWidth, y, width, y, mPaint);
			}
			
			if(mProgressPercent != 0f){
				mPaint.setColor(mStrokeColors[0]);
				if(mReverse)
					drawLinePath(canvas, width - lineWidth, y, width, y, mPaint);
				else
					drawLinePath(canvas, 0, y, lineWidth, y, mPaint);
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
		Rect bounds = getBounds();		
		int width = bounds.width();
		float size = 0f;
		
		if(mRunState == RUN_STATE_STARTING)
			size = (float)mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;			
		else if(mRunState == RUN_STATE_STOPPING)
			size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;				
		else if(mRunState != RUN_STATE_STOPPED)
			size = mStrokeSize;		
		
		if(size > 0){
			float y = 0;
			
			switch (mVerticalAlign) {
				case ALIGN_TOP:
					y = size / 2;
					break;
				case ALIGN_CENTER:
					y = bounds.height() / 2f;
					break;
				case ALIGN_BOTTOM:
					y = bounds.height() - size / 2;
					break;
			}
						
			mPaint.setStrokeWidth(size);
			mPaint.setStyle(Paint.Style.STROKE);
						
			float endLine = offset(mStartLine, mLineWidth, width);
						
			if(mReverse){
				if(endLine <= mStartLine){
					mPaint.setColor(mStrokeSecondaryColor);
					if(endLine > 0)
						canvas.drawLine(0, y, endLine, y, mPaint);
					if(mStartLine < width)
						canvas.drawLine(mStartLine, y, width, y, mPaint);
					
					mPaint.setColor(getIndeterminateStrokeColor());
					drawLinePath(canvas, endLine, y, mStartLine, y, mPaint);
				}
				else{
					mPaint.setColor(mStrokeSecondaryColor);
					canvas.drawLine(mStartLine, y, endLine, y, mPaint);
					
					mPaint.setColor(getIndeterminateStrokeColor());
					mPath.reset();
					
					if(mStartLine > 0){
						mPath.moveTo(0, y);
						mPath.lineTo(mStartLine, y);
					}
					if(endLine < width){
						mPath.moveTo(endLine, y);
						mPath.lineTo(width, y);
					}
						
					canvas.drawPath(mPath, mPaint);
				}	
			}
			else{
				if(endLine >= mStartLine){
					mPaint.setColor(mStrokeSecondaryColor);
					if(mStartLine > 0)
						canvas.drawLine(0, y, mStartLine, y, mPaint);
					if(endLine < width)
						canvas.drawLine(endLine, y, width, y, mPaint);
					
					mPaint.setColor(getIndeterminateStrokeColor());
					drawLinePath(canvas, mStartLine, y, endLine, y, mPaint);
				}
				else{
					mPaint.setColor(mStrokeSecondaryColor);
					canvas.drawLine(endLine, y, mStartLine, y, mPaint);
					
					mPaint.setColor(getIndeterminateStrokeColor());
					mPath.reset();
					
					if(endLine > 0){
						mPath.moveTo(0, y);
						mPath.lineTo(endLine, y);
					}
					if(mStartLine < width){
						mPath.moveTo(mStartLine, y);
						mPath.lineTo(width, y);
					}
					
					canvas.drawPath(mPath, mPaint);
				}	
			}					
		}				
	}

	private PathEffect getPathEffect(){
		if(mPathEffect == null)
			mPathEffect = new DashPathEffect(new float[]{0.1f, mStrokeSize * 2}, 0f);
		
		return mPathEffect;
	}
	
	private void drawBuffer(Canvas canvas){
		Rect bounds = getBounds();		
		int width = bounds.width();
		float size = 0f;
		
		if(mRunState == RUN_STATE_STARTING)
			size = (float)mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;			
		else if(mRunState == RUN_STATE_STOPPING)
			size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;				
		else if(mRunState != RUN_STATE_STOPPED)
			size = mStrokeSize;		
		
		if(size > 0){
			float y = 0;
			float lineWidth = width * mProgressPercent;
			float secondaryLineWidth = width * mSecondaryProgressPercent;
			
			switch (mVerticalAlign) {
				case ALIGN_TOP:
					y = size / 2;
					break;
				case ALIGN_CENTER:
					y = bounds.height() / 2f;
					break;
				case ALIGN_BOTTOM:
					y = bounds.height() - size / 2;
					break;
			}
			
			mPaint.setStyle(Paint.Style.STROKE);
						
			if(mProgressPercent != 1f){
				mPaint.setStrokeWidth(size);
				mPaint.setColor(mStrokeSecondaryColor);
				mPaint.setPathEffect(null);
				
				if(mReverse)
					drawLinePath(canvas, width - secondaryLineWidth, y, width - lineWidth, y, mPaint);			
				else
					drawLinePath(canvas, secondaryLineWidth, y, lineWidth, y, mPaint);			
												
				mPaint.setStrokeWidth(mLineWidth);				
				mPaint.setPathEffect(getPathEffect());		
				float offset = mStrokeSize * 2 - mStartLine;
				
				if(mReverse)
					drawLinePath(canvas, -offset, y, width - secondaryLineWidth, y, mPaint);				
				else								
					drawLinePath(canvas, width + offset, y, secondaryLineWidth, y, mPaint);				
			}
			
			if(mProgressPercent != 0f){
				mPaint.setStrokeWidth(size);
				mPaint.setColor(mStrokeColors[0]);			
				mPaint.setPathEffect(null);
				
				if(mReverse)
					drawLinePath(canvas, width - lineWidth, y, width, y, mPaint);				
				else
					drawLinePath(canvas, 0, y, lineWidth, y, mPaint);				
			}						
		}		
	}
	
	private int getQueryStrokeColor(){    
		return ColorUtil.getColor(mStrokeColors[0], mAnimTime);
	}
	
	private void drawQuery(Canvas canvas){
		Rect bounds = getBounds();		
		int width = bounds.width();
		float size = 0f;
		
		if(mRunState == RUN_STATE_STARTING)
			size = (float)mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;			
		else if(mRunState == RUN_STATE_STOPPING)
			size = (float)mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;				
		else if(mRunState != RUN_STATE_STOPPED)
			size = mStrokeSize;		
		
		if(size > 0){
			float y = 0;
			
			switch (mVerticalAlign) {
				case ALIGN_TOP:
					y = size / 2;
					break;
				case ALIGN_CENTER:
					y = bounds.height() / 2f;
					break;
				case ALIGN_BOTTOM:
					y = bounds.height() - size / 2;
					break;
			}
						
			mPaint.setStrokeWidth(size);
			mPaint.setStyle(Paint.Style.STROKE);
			
			if(mProgressPercent != 1f){
				mPaint.setColor(mStrokeSecondaryColor);
				canvas.drawLine(0, y, width, y, mPaint);
				
				if(mAnimTime < 1f){
					float endLine = Math.max(0, Math.min(width, mStartLine + mLineWidth));
					mPaint.setColor(getQueryStrokeColor());
					drawLinePath(canvas, mStartLine, y, endLine, y, mPaint);
				}
			}
			
			if(mProgressPercent != 0f){
				float lineWidth = width * mProgressPercent;
				mPaint.setColor(mStrokeColors[0]);	
				
				if(mReverse)
					drawLinePath(canvas, width - lineWidth, y, width, y, mPaint);
				else
					drawLinePath(canvas, 0, y, lineWidth, y, mPaint);
			}
			
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
		if(mProgressMode == ProgressView.MODE_INDETERMINATE){
			mStartLine = mReverse ? getBounds().width() : 0;
			mStrokeColorIndex = 0;
			mLineWidth = mReverse ? -mMinLineWidth : mMinLineWidth;
			mProgressState = PROGRESS_STATE_STRETCH;
		}
		else if(mProgressMode == ProgressView.MODE_BUFFER){
			mStartLine = 0;
		}	
		else if(mProgressMode == ProgressView.MODE_QUERY){
			mStartLine = !mReverse ? getBounds().width() : 0;
			mStrokeColorIndex = 0;
			mLineWidth = !mReverse ? -mMaxLineWidth : mMaxLineWidth;
		}		
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
		}
		
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
			case ProgressView.MODE_BUFFER:
				updateBuffer();
				break;
			case ProgressView.MODE_QUERY:
				updateQuery();
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
	
	private float offset(float pos, float offset, float max){
		pos += offset;
		if(pos > max)
			return pos - max;
		if(pos < 0)
			return max + pos;
		return pos;
	}
	
	private void updateIndeterminate(){
		Rect bounds = getBounds();
    	int width = bounds.width();
		
    	long curTime = SystemClock.uptimeMillis();
    	float travelOffset = (float)(curTime - mLastUpdateTime) * width / mTravelDuration;	    	
    	if(mReverse)
    		travelOffset = -travelOffset;	    	
    	mLastUpdateTime = curTime;    	

    	switch (mProgressState) {
			case PROGRESS_STATE_STRETCH:
				if(mTransformDuration <= 0){
					mLineWidth = mMinLineWidth == 0 ? width * mMinLineWidthPercent : mMinLineWidth;
					if(mReverse)
						mLineWidth = -mLineWidth;
					mStartLine = offset(mStartLine, travelOffset, width);
					mProgressState = PROGRESS_STATE_KEEP_STRETCH;
					mLastProgressStateTime = curTime;					
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					float maxWidth = mMaxLineWidth == 0 ? width * mMaxLineWidthPercent : mMaxLineWidth;
					float minWidth = mMinLineWidth == 0 ? width * mMinLineWidthPercent : mMinLineWidth;
					
					mStartLine = offset(mStartLine, travelOffset, width);
					mLineWidth = mTransformInterpolator.getInterpolation(value) * (maxWidth - minWidth) + minWidth;
					if(mReverse)
						mLineWidth = -mLineWidth;
					
					if(value > 1f){
						mLineWidth = mReverse ? -maxWidth : maxWidth;
		    			mProgressState = PROGRESS_STATE_KEEP_STRETCH;
		    			mLastProgressStateTime = curTime;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_STRETCH:
				mStartLine = offset(mStartLine, travelOffset, width);
				
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_SHRINK;
					mLastProgressStateTime = curTime;
				}
				break;
			case PROGRESS_STATE_SHRINK:		
				if(mTransformDuration <= 0){
					mLineWidth = mMinLineWidth == 0 ? width * mMinLineWidthPercent : mMinLineWidth;
					if(mReverse)
						mLineWidth = -mLineWidth;
					mStartLine = offset(mStartLine, travelOffset, width);
					mProgressState = PROGRESS_STATE_KEEP_SHRINK;
					mLastProgressStateTime = curTime;		
					mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					float maxWidth = mMaxLineWidth == 0 ? width * mMaxLineWidthPercent : mMaxLineWidth;
					float minWidth = mMinLineWidth == 0 ? width * mMinLineWidthPercent : mMinLineWidth;
					
					float newLineWidth = (1f - mTransformInterpolator.getInterpolation(value)) * (maxWidth - minWidth) + minWidth;		
					if(mReverse)
						newLineWidth = -newLineWidth;
					
					mStartLine = offset(mStartLine, travelOffset + mLineWidth - newLineWidth, width);
					mLineWidth = newLineWidth;
					
					if(value > 1f){
						mLineWidth = mReverse ? -minWidth : minWidth;
		    			mProgressState = PROGRESS_STATE_KEEP_SHRINK;
		    			mLastProgressStateTime = curTime;
		    			mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_SHRINK:
				mStartLine = offset(mStartLine, travelOffset, width);
				
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_STRETCH;
					mLastProgressStateTime = curTime;
				}
				break;					
		}
    	
    	if(mRunState == RUN_STATE_STARTING){
    		if(curTime - mLastRunStateTime > mInAnimationDuration)
    			mRunState = RUN_STATE_RUNNING;	  
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
	
	private void updateBuffer(){
		long curTime = SystemClock.uptimeMillis();
		float maxDistance = mStrokeSize * 2;		
		mStartLine += maxDistance * (float)(curTime - mLastUpdateTime) / mTravelDuration;
		while(mStartLine > maxDistance)
			mStartLine -= maxDistance;
    	mLastUpdateTime = curTime;        	
    	
		switch (mProgressState) {
			case PROGRESS_STATE_STRETCH:
				if(mTransformDuration <= 0){
					mProgressState = PROGRESS_STATE_KEEP_STRETCH;
					mLastProgressStateTime = curTime;		
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					mLineWidth = mTransformInterpolator.getInterpolation(value) * mStrokeSize;
										
					if(value > 1f){
						mLineWidth = mStrokeSize;
		    			mProgressState = PROGRESS_STATE_KEEP_STRETCH;
		    			mLastProgressStateTime = curTime;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_STRETCH:
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_SHRINK;
					mLastProgressStateTime = curTime;
				}
				break;
			case PROGRESS_STATE_SHRINK:		
				if(mTransformDuration <= 0){
					mProgressState = PROGRESS_STATE_KEEP_SHRINK;
					mLastProgressStateTime = curTime;		
				}
				else{
					float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
					mLineWidth = (1f - mTransformInterpolator.getInterpolation(value)) * mStrokeSize;
					
					if(value > 1f){
						mLineWidth = 0;
		    			mProgressState = PROGRESS_STATE_KEEP_SHRINK;
		    			mLastProgressStateTime = curTime;
		    		}
				}				
				break;
			case PROGRESS_STATE_KEEP_SHRINK:				
				if(curTime - mLastProgressStateTime > mKeepDuration){
					mProgressState = PROGRESS_STATE_STRETCH;
					mLastProgressStateTime = curTime;
				}
				break;					
		}
    			
		if(mRunState == RUN_STATE_STARTING){
    		if(curTime - mLastRunStateTime > mInAnimationDuration)
    			mRunState = RUN_STATE_RUNNING;   
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
	
	private void updateQuery(){
    	long curTime = SystemClock.uptimeMillis();    	
    	mAnimTime = (float)(curTime - mLastProgressStateTime) / mTravelDuration;
    	boolean requestUpdate = mRunState == RUN_STATE_STOPPING || mProgressPercent == 0 || mAnimTime < 1f;
    	
    	if(mAnimTime > 1f){
    		mLastProgressStateTime = Math.round(curTime - (mAnimTime - 1f) * mTravelDuration);
    		mAnimTime -= 1f;
    	}
    	
    	if(requestUpdate && mRunState != RUN_STATE_STOPPING){
    		Rect bounds = getBounds();
        	int width = bounds.width();
        	
        	float maxWidth = mMaxLineWidth == 0 ? width * mMaxLineWidthPercent : mMaxLineWidth;
    		float minWidth = mMinLineWidth == 0 ? width * mMinLineWidthPercent : mMinLineWidth;				
    		mLineWidth = mTransformInterpolator.getInterpolation(mAnimTime) * (minWidth - maxWidth) + maxWidth;
    		if(mReverse)
    			mLineWidth = -mLineWidth;    		
    		
    		mStartLine = mReverse ? mTransformInterpolator.getInterpolation(mAnimTime) * (width + minWidth) : ((1f - mTransformInterpolator.getInterpolation(mAnimTime)) * (width + minWidth) - minWidth);    
    	}
    	
    	if(mRunState == RUN_STATE_STARTING){
    		if(curTime - mLastRunStateTime > mInAnimationDuration)
    			mRunState = RUN_STATE_RUNNING;    		
    	}
    	else if(mRunState == RUN_STATE_STOPPING){
    		if(curTime - mLastRunStateTime > mOutAnimationDuration){
    			stop(false);
    			return;
    		}
    	}   
    	
		if (isRunning()){
			if(requestUpdate)
				scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
			else if(mRunState == RUN_STATE_RUNNING)
				mRunState = RUN_STATE_STARTED;	
		}

    	invalidateSelf();
	}
	
	public static class Builder{
		private float mProgressPercent = 0;
		private float mSecondaryProgressPercent = 0;
		private int mMaxLineWidth;
		private float mMaxLineWidthPercent;
		private int mMinLineWidth;
		private float mMinLineWidthPercent;
		private int mStrokeSize = 8;
		private int mVerticalAlign = LinearProgressDrawable.ALIGN_BOTTOM;
		private int[] mStrokeColors;
		private int mStrokeSecondaryColor;		
		private boolean mReverse = false;
		private int mTravelDuration = 1000;
		private int mTransformDuration = 800;
		private int mKeepDuration = 200;	
		private Interpolator mTransformInterpolator;
		private int mProgressMode = ProgressView.MODE_INDETERMINATE;
		private int mInAnimationDuration = 400;
		private int mOutAnimationDuration = 400;
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearProgressDrawable, defStyleAttr, defStyleRes);
			int resId;
			
			progressPercent(a.getFloat(R.styleable.LinearProgressDrawable_pv_progress, 0));	
			secondaryProgressPercent(a.getFloat(R.styleable.LinearProgressDrawable_pv_secondaryProgress, 0));
			
			TypedValue value = a.peekValue(R.styleable.LinearProgressDrawable_lpd_maxLineWidth);
			if(value == null)
				maxLineWidth(0.75f);
			else if(value.type == TypedValue.TYPE_FRACTION)
				maxLineWidth(a.getFraction(R.styleable.LinearProgressDrawable_lpd_maxLineWidth, 1, 1, 0.75f));
			else 
				maxLineWidth(a.getDimensionPixelSize(R.styleable.LinearProgressDrawable_lpd_maxLineWidth, 0));
			
			value = a.peekValue(R.styleable.LinearProgressDrawable_lpd_minLineWidth);
			if(value == null)
				minLineWidth(0.25f);
			else if(value.type == TypedValue.TYPE_FRACTION)
				minLineWidth(a.getFraction(R.styleable.LinearProgressDrawable_lpd_minLineWidth, 1, 1, 0.25f));
			else 
				minLineWidth(a.getDimensionPixelSize(R.styleable.LinearProgressDrawable_lpd_minLineWidth, 0));
			
			strokeSize(a.getDimensionPixelSize(R.styleable.LinearProgressDrawable_lpd_strokeSize, ThemeUtil.dpToPx(context, 4)));
			verticalAlign(a.getInteger(R.styleable.LinearProgressDrawable_lpd_verticalAlign, LinearProgressDrawable.ALIGN_BOTTOM));
			strokeColors(a.getColor(R.styleable.LinearProgressDrawable_lpd_strokeColor, ThemeUtil.colorPrimary(context, 0xFF000000)));
			if((resId = a.getResourceId(R.styleable.LinearProgressDrawable_lpd_strokeColors, 0)) != 0){
				TypedArray ta = context.getResources().obtainTypedArray(resId);				        	
				int[] colors = new int[ta.length()];
				for(int j = 0; j < ta.length(); j++)
				    colors[j] = ta.getColor(j, 0);				        	
				ta.recycle();
				strokeColors(colors);
			}
			strokeSecondaryColor(a.getColor(R.styleable.LinearProgressDrawable_lpd_strokeSecondaryColor, 0));
			reverse(a.getBoolean(R.styleable.LinearProgressDrawable_lpd_reverse, false));
			travelDuration(a.getInteger(R.styleable.LinearProgressDrawable_lpd_travelDuration, context.getResources().getInteger(android.R.integer.config_longAnimTime)));
			transformDuration(a.getInteger(R.styleable.LinearProgressDrawable_lpd_transformDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			keepDuration(a.getInteger(R.styleable.LinearProgressDrawable_lpd_keepDuration, context.getResources().getInteger(android.R.integer.config_shortAnimTime)));
			if((resId = a.getResourceId(R.styleable.LinearProgressDrawable_lpd_transformInterpolator, 0)) != 0)
				transformInterpolator(AnimationUtils.loadInterpolator(context, resId));
			progressMode(a.getInteger(R.styleable.LinearProgressDrawable_pv_progressMode, ProgressView.MODE_INDETERMINATE));
			inAnimDuration(a.getInteger(R.styleable.LinearProgressDrawable_lpd_inAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			outAnimDuration(a.getInteger(R.styleable.LinearProgressDrawable_lpd_outAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			
			a.recycle();				
		}
		
		public LinearProgressDrawable build(){
			if(mStrokeColors == null)
				mStrokeColors = new int[]{0xFF0099FF};
						
			if(mTransformInterpolator == null)
				mTransformInterpolator = new DecelerateInterpolator();
			
			return new LinearProgressDrawable(mProgressPercent, mSecondaryProgressPercent, mMaxLineWidth, mMaxLineWidthPercent, mMinLineWidth, mMinLineWidthPercent, mStrokeSize, mVerticalAlign, mStrokeColors, mStrokeSecondaryColor, mReverse, mTravelDuration, mTransformDuration, mKeepDuration, mTransformInterpolator, mProgressMode, mInAnimationDuration, mOutAnimationDuration);
		}
		
		public Builder secondaryProgressPercent(float percent){
			mSecondaryProgressPercent = percent;
			return this;
		}
		
		public Builder progressPercent(float percent){
			mProgressPercent = percent;
			return this;
		}
		
		public Builder maxLineWidth(int width){
			mMaxLineWidth = width;
			return this;
		}
		
		public Builder maxLineWidth(float percent){
			mMaxLineWidthPercent = Math.max(0f, Math.min(1f, percent));
			mMaxLineWidth = 0;
			return this;
		}
		
		public Builder minLineWidth(int width){
			mMinLineWidth = width;
			return this;
		}
		
		public Builder minLineWidth(float percent){
			mMinLineWidthPercent = Math.max(0f, Math.min(1f, percent));
			mMinLineWidth = 0;
			return this;
		}
		
		public Builder strokeSize(int strokeSize){
			mStrokeSize = strokeSize;
			return this;
		}
		
		public Builder verticalAlign(int align){
			mVerticalAlign = align;
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
		
		public Builder travelDuration(int duration){
			mTravelDuration = duration;
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
				
		public Builder outAnimDuration(int duration){
			mOutAnimationDuration = duration;
			return this;
		}
		
	}
}
