package com.rey.material.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class RippleDrawable extends Drawable implements Animatable,	OnTouchListener {
	
	private boolean mRunning = false;
		
	private Paint mShaderPaint;
	private Paint mFillPaint;
	private Mask mMask;
	private RadialGradient mInShader;
	private RadialGradient mOutShader;
	private Matrix mMatrix;	
	private int mAlpha = 255;
	
	private Drawable mBackgroundDrawable;
	private RectF mBackgroundBounds;
	private Path mBackground;
	private int mBackgroundAnimDuration;
	private int mBackgroundColor;
	private float mBackgroundAlphaPercent;
		
	private PointF mRipplePoint;
	private float mRippleRadius;
	private int mRippleType;
	private int mMaxRippleRadius;
	private int mRippleAnimDuration;
	private int mRippleColor;
	private float mRippleAlphaPercent;
	private int mDelayClickType;

	private Interpolator mInInterpolator;
	private Interpolator mOutInterpolator;
	
	private long mStartTime;	
			
	private int mState = STATE_OUT;

    public static final int DELAY_CLICK_NONE = 0;
    public static final int DELAY_CLICK_UNTIL_RELEASE = 1;
    public static final int DELAY_CLICK_AFTER_RELEASE = 2;

	private static final int STATE_OUT = 0;
	private static final int STATE_PRESS = 1;
	private static final int STATE_HOVER = 2;
	private static final int STATE_RELEASE_ON_HOLD = 3;	
	private static final int STATE_RELEASE = 4;

    private static final int TYPE_TOUCH_MATCH_VIEW = -1;
	private static final int TYPE_TOUCH = 0;
	private static final int TYPE_WAVE = 1;
	
	private static final float[] GRADIENT_STOPS = new float[]{0f, 0.99f, 1f};
	private static final float GRADIENT_RADIUS = 16;
		
	private RippleDrawable(Drawable backgroundDrawable, int backgroundAnimDuration, int backgroundColor, int rippleType, int delayClickType,  int maxRippleRadius, int rippleAnimDuration, int rippleColor, Interpolator inInterpolator, Interpolator outInterpolator, int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom){
		setBackgroundDrawable(backgroundDrawable);
		mBackgroundAnimDuration = backgroundAnimDuration;
		mBackgroundColor = backgroundColor;
		
		mRippleType = rippleType;
        setDelayClickType(delayClickType);
		mMaxRippleRadius = maxRippleRadius;
		mRippleAnimDuration = rippleAnimDuration;
		mRippleColor = rippleColor;

        if(mRippleType == TYPE_TOUCH && mMaxRippleRadius <= 0)
            mRippleType = TYPE_TOUCH_MATCH_VIEW;
		
		mInInterpolator = inInterpolator;
		mOutInterpolator = outInterpolator;
		
		setMask(type, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius, left, top, right, bottom);
		
		mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFillPaint.setStyle(Paint.Style.FILL);		
		
		mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mShaderPaint.setStyle(Paint.Style.FILL);
		
		mBackground = new Path();
		mBackgroundBounds = new RectF();
		
		mRipplePoint = new PointF();
		
		mMatrix = new Matrix();
		
		mInShader = new RadialGradient(0, 0, GRADIENT_RADIUS, new int[]{mRippleColor, mRippleColor, 0}, GRADIENT_STOPS, Shader.TileMode.CLAMP);		
		if(mRippleType == TYPE_WAVE)
			mOutShader = new RadialGradient(0, 0, GRADIENT_RADIUS, new int[]{0, ColorUtil.getColor(mRippleColor, 0f), mRippleColor}, GRADIENT_STOPS, Shader.TileMode.CLAMP);		
	}

    public void setBackgroundDrawable(Drawable backgroundDrawable){
        mBackgroundDrawable = backgroundDrawable;
        if(mBackgroundDrawable != null)
            mBackgroundDrawable.setBounds(getBounds());
    }

    public int getDelayClickType(){
        return mDelayClickType;
    }

    public void setDelayClickType(int type){
        mDelayClickType = type;
    }

    public void setMask(int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom){
        mMask = new Mask(type, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius, left, top, right, bottom);
    }

	@Override
	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}

	@Override
	public void setColorFilter(ColorFilter filter) {
		mFillPaint.setColorFilter(filter);
		mShaderPaint.setColorFilter(filter);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
		
	public long getClickDelayTime(){
        switch (mDelayClickType){
            case DELAY_CLICK_NONE:
                return -1;
            case DELAY_CLICK_UNTIL_RELEASE:
                if(mState == STATE_RELEASE_ON_HOLD)
                    return Math.max(mBackgroundAnimDuration, mRippleAnimDuration) - (SystemClock.uptimeMillis() - mStartTime);
                break;
            case DELAY_CLICK_AFTER_RELEASE:
                if(mState == STATE_RELEASE_ON_HOLD)
                    return 2 * Math.max(mBackgroundAnimDuration, mRippleAnimDuration) - (SystemClock.uptimeMillis() - mStartTime);
                else if(mState == STATE_RELEASE)
                    return Math.max(mBackgroundAnimDuration, mRippleAnimDuration) - (SystemClock.uptimeMillis() - mStartTime);
                break;
        }
		
		return -1;
	}
	
	private void setRippleState(int state){
		if(mState != state){
			mState = state;
									
			if(mState != STATE_OUT){				
				if(mState != STATE_HOVER)
					start();
				else
					stop();
			}
			else
				stop();			
		}
	}
	
	private boolean setRippleEffect(float x, float y, float radius){
		if(mRipplePoint.x != x || mRipplePoint.y != y || mRippleRadius != radius){
			mRipplePoint.set(x, y);
			mRippleRadius = radius;			
			radius = mRippleRadius / GRADIENT_RADIUS;
			mMatrix.reset();
			mMatrix.postTranslate(x, y);
			mMatrix.postScale(radius, radius, x, y);
			mInShader.setLocalMatrix(mMatrix);
			if(mOutShader != null)
				mOutShader.setLocalMatrix(mMatrix);
			
			return true;
		}		
		
		return false;
	}
			
	@Override
	protected void onBoundsChange(Rect bounds) {		
		if(mBackgroundDrawable != null)
			mBackgroundDrawable.setBounds(bounds);
		
		mBackgroundBounds.set(bounds.left + mMask.left, bounds.top + mMask.top, bounds.right - mMask.right, bounds.bottom - mMask.bottom);
		mBackground.reset();
		
		switch (mMask.type) {
			case Mask.TYPE_OVAL:
				mBackground.addOval(mBackgroundBounds, Direction.CW);
				break;
			case Mask.TYPE_RECTANGLE:
				mBackground.addRoundRect(mBackgroundBounds, mMask.cornerRadius, Direction.CW);
				break;
		}
	}
		
	@Override
	public boolean isStateful() {
		return mBackgroundDrawable != null && mBackgroundDrawable.isStateful();
	}
	
	@Override
	protected boolean onStateChange(int[] state) {
        return mBackgroundDrawable != null && mBackgroundDrawable.setState(state);

    }
	
	@Override
	public void draw(Canvas canvas) {
		if(mBackgroundDrawable != null)
			mBackgroundDrawable.draw(canvas);
		
		switch (mRippleType) {
			case TYPE_TOUCH:
            case TYPE_TOUCH_MATCH_VIEW:
				drawTouch(canvas);
				break;
			case TYPE_WAVE:
				drawWave(canvas);
				break;
		}
	}
	
	private void drawTouch(Canvas canvas){
		if(mState != STATE_OUT){						
			if(mBackgroundAlphaPercent > 0){
				mFillPaint.setColor(mBackgroundColor);
				mFillPaint.setAlpha(Math.round(mAlpha * mBackgroundAlphaPercent));				
				canvas.drawPath(mBackground, mFillPaint);
			}
									
			if(mRippleRadius > 0 && mRippleAlphaPercent > 0){
				mShaderPaint.setAlpha(Math.round(mAlpha * mRippleAlphaPercent));
				mShaderPaint.setShader(mInShader);
				canvas.drawPath(mBackground, mShaderPaint);
			}			
		}
	}
	
	private void drawWave(Canvas canvas){
		if(mState != STATE_OUT){
			if(mState == STATE_RELEASE){
				if(mRippleRadius == 0){
					mFillPaint.setColor(mRippleColor);
					canvas.drawPath(mBackground, mFillPaint);
				}
				else{
					mShaderPaint.setShader(mOutShader);
					canvas.drawPath(mBackground, mShaderPaint);
				}				
			}
			else if(mRippleRadius > 0){
				mShaderPaint.setShader(mInShader);
				canvas.drawPath(mBackground, mShaderPaint);
			}
		}			
	}
		
	private int getMaxRippleRadius(float x, float y){
		float x1 = x < mBackgroundBounds.centerX() ? mBackgroundBounds.right : mBackgroundBounds.left;
		float y1 = y < mBackgroundBounds.centerY() ? mBackgroundBounds.bottom : mBackgroundBounds.top;
		
		return (int)Math.round(Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2)));
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if(mState == STATE_OUT || mState == STATE_RELEASE){
					if(mRippleType == TYPE_WAVE || mRippleType == TYPE_TOUCH_MATCH_VIEW)
						mMaxRippleRadius = getMaxRippleRadius(event.getX(), event.getY());
					
					setRippleEffect(event.getX(), event.getY(), 0);
					setRippleState(STATE_PRESS);
				}
				else if(mRippleType == TYPE_TOUCH){
					if(setRippleEffect(event.getX(), event.getY(), mRippleRadius))		
						invalidateSelf();
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:	
				if(mState != STATE_OUT){
					if(mState == STATE_HOVER){
						if(mRippleType == TYPE_WAVE || mRippleType == TYPE_TOUCH_MATCH_VIEW)
							setRippleEffect(mRipplePoint.x, mRipplePoint.y, 0);
						
						setRippleState(STATE_RELEASE);
					}
					else
						setRippleState(STATE_RELEASE_ON_HOLD);	
				}
				break;
		}
		return true;
	}

	//Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/
	
	public void cancel(){
		setRippleState(STATE_OUT);
	}
	
	private void resetAnimation(){	
		mStartTime = SystemClock.uptimeMillis();
	}
	
	@Override
	public void start() {		
		if(isRunning())
			return;
		
		resetAnimation();
		
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	    invalidateSelf();  
	}

	@Override
	public void stop() {		
		if(!isRunning())
			return;
				
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
	    	switch (mRippleType) {
				case TYPE_TOUCH:
                case TYPE_TOUCH_MATCH_VIEW:
					updateTouch();
					break;
				case TYPE_WAVE:
					updateWave();
					break;
			}	    	
	    }
		    
	};
		
	private void updateTouch(){		
		if(mState != STATE_RELEASE){
			float backgroundProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mBackgroundAnimDuration);
			mBackgroundAlphaPercent = mInInterpolator.getInterpolation(backgroundProgress) * Color.alpha(mBackgroundColor) / 255f;
			
			float touchProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mRippleAnimDuration);			
			mRippleAlphaPercent = mInInterpolator.getInterpolation(touchProgress);
			
			setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mInInterpolator.getInterpolation(touchProgress));
			
			if(backgroundProgress == 1f && touchProgress == 1f){
				mStartTime = SystemClock.uptimeMillis();
				setRippleState(mState == STATE_PRESS ? STATE_HOVER : STATE_RELEASE);
			}
			
		}
		else{
			float backgroundProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mBackgroundAnimDuration);
			mBackgroundAlphaPercent = (1f - mOutInterpolator.getInterpolation(backgroundProgress)) * Color.alpha(mBackgroundColor) / 255f;
			
			float touchProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mRippleAnimDuration);			
			mRippleAlphaPercent = 1f - mOutInterpolator.getInterpolation(touchProgress);
			
			setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * (1f + 0.5f * mOutInterpolator.getInterpolation(touchProgress)));
			
			if(backgroundProgress == 1f && touchProgress == 1f)
				setRippleState(STATE_OUT);	
		}
		
		if(isRunning())
			scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
		
		invalidateSelf();		
	}
	
	private void updateWave(){
		float progress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mRippleAnimDuration);
		
		if(mState != STATE_RELEASE){			
			setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mInInterpolator.getInterpolation(progress));
			
			if(progress == 1f){
				mStartTime = SystemClock.uptimeMillis();
				if(mState == STATE_PRESS)
					setRippleState(STATE_HOVER);				
				else{
					setRippleEffect(mRipplePoint.x, mRipplePoint.y, 0);
					setRippleState(STATE_RELEASE);
				}
			}
		}
		else{			
			setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mOutInterpolator.getInterpolation(progress));
			
			if(progress == 1f)
				setRippleState(STATE_OUT);
		}
		
		if(isRunning())
			scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
		
		invalidateSelf();		
	}

	public static class Mask{
		
		public static final int TYPE_RECTANGLE = 0;
		public static final int TYPE_OVAL = 1;
		
		final int type;
				
		final float[] cornerRadius = new float[8];
				
		final int left;
		final int top;
		final int right;
		final int bottom;
		
		public Mask(int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom){
			this.type = type;

			cornerRadius[0] = topLeftCornerRadius;
			cornerRadius[1] = topLeftCornerRadius;
			
			cornerRadius[2] = topRightCornerRadius;
			cornerRadius[3] = topRightCornerRadius;
			
			cornerRadius[4] = bottomRightCornerRadius;
			cornerRadius[5] = bottomRightCornerRadius;
			
			cornerRadius[6] = bottomLeftCornerRadius;
			cornerRadius[7] = bottomLeftCornerRadius;
			
			this.left = left;
			this.top = top;
			this.right = right;			
			this.bottom = bottom;
		}
		
	}
	
	public static class Builder{
		private Drawable mBackgroundDrawable;
		private int mBackgroundAnimDuration = 200;
		private int mBackgroundColor;
			
		private int mRippleType;
		private int mMaxRippleRadius;
		private int mRippleAnimDuration = 400;
		private int mRippleColor;
		private int mDelayClickType;

		private Interpolator mInInterpolator;
		private Interpolator mOutInterpolator;
				
		private int mMaskType;
		private int mMaskTopLeftCornerRadius;
		private int mMaskTopRightCornerRadius;
		private int mMaskBottomLeftCornerRadius;
		private int mMaskBottomRightCornerRadius;
		private int mMaskLeft;
		private int mMaskTop;
		private int mMaskRight;
		private int mMaskBottom;
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleDrawable, defStyleAttr, defStyleRes);
            int type, resId;

			backgroundColor(a.getColor(R.styleable.RippleDrawable_rd_backgroundColor, 0));
			backgroundAnimDuration(a.getInteger(R.styleable.RippleDrawable_rd_backgroundAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			rippleType(a.getInteger(R.styleable.RippleDrawable_rd_rippleType, RippleDrawable.TYPE_TOUCH));
            delayClickType(a.getInteger(R.styleable.RippleDrawable_rd_delayClick, RippleDrawable.DELAY_CLICK_NONE));
            type = ThemeUtil.getType(a, R.styleable.RippleDrawable_rd_maxRippleRadius);
            if(type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT)
                maxRippleRadius(a.getInteger(R.styleable.RippleDrawable_rd_maxRippleRadius, -1));
            else
			    maxRippleRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_maxRippleRadius, ThemeUtil.dpToPx(context, 48)));
			rippleColor(a.getColor(R.styleable.RippleDrawable_rd_rippleColor, ThemeUtil.colorControlHighlight(context, 0)));
			rippleAnimDuration(a.getInteger(R.styleable.RippleDrawable_rd_rippleAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			if((resId = a.getResourceId(R.styleable.RippleDrawable_rd_inInterpolator, 0)) != 0)
				inInterpolator(AnimationUtils.loadInterpolator(context, resId));
			if((resId = a.getResourceId(R.styleable.RippleDrawable_rd_outInterpolator, 0)) != 0)
				outInterpolator(AnimationUtils.loadInterpolator(context, resId));
			maskType(a.getInteger(R.styleable.RippleDrawable_rd_maskType, Mask.TYPE_RECTANGLE));
			cornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_cornerRadius, 0));
			topLeftCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topLeftCornerRadius, mMaskTopLeftCornerRadius));
			topRightCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topRightCornerRadius, mMaskTopRightCornerRadius));
			bottomRightCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomRightCornerRadius, mMaskBottomRightCornerRadius));
			bottomLeftCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomLeftCornerRadius, mMaskBottomLeftCornerRadius));
			padding(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_padding, 0));
			left(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_leftPadding, mMaskLeft));
			right(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_rightPadding, mMaskRight));
			top(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topPadding, mMaskTop));
			bottom(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomPadding, mMaskBottom));
			
			a.recycle();			
		}
		
		public RippleDrawable build(){
			if(mInInterpolator == null)
				mInInterpolator = new AccelerateInterpolator();
			
			if(mOutInterpolator == null)
				mOutInterpolator = new DecelerateInterpolator();
			
			return new RippleDrawable(mBackgroundDrawable, mBackgroundAnimDuration, mBackgroundColor, mRippleType, mDelayClickType, mMaxRippleRadius, mRippleAnimDuration, mRippleColor, mInInterpolator, mOutInterpolator, mMaskType, mMaskTopLeftCornerRadius, mMaskTopRightCornerRadius, mMaskBottomRightCornerRadius, mMaskBottomLeftCornerRadius, mMaskLeft, mMaskTop, mMaskRight, mMaskBottom);
		}
		
		public Builder backgroundDrawable(Drawable drawable){
			mBackgroundDrawable = drawable;
			return this;
		}
		
		public Builder backgroundAnimDuration(int duration){
			mBackgroundAnimDuration = duration;
			return this;
		}
		
		public Builder backgroundColor(int color){
			mBackgroundColor = color;
			return this;
		}
		
		public Builder rippleType(int type){
			mRippleType = type;
			return this;
		}

        public Builder delayClickType(int type){
            mDelayClickType = type;
            return this;
        }

		public Builder maxRippleRadius(int radius){
			mMaxRippleRadius = radius;
			return this;
		}
		
		public Builder rippleAnimDuration(int duration){
			mRippleAnimDuration = duration;
			return this;
		}
		
		public Builder rippleColor(int color){
			mRippleColor = color;
			return this;
		}
		
		public Builder inInterpolator(Interpolator interpolator){
			mInInterpolator = interpolator;
			return this;
		}
		
		public Builder outInterpolator(Interpolator interpolator){
			mOutInterpolator = interpolator;
			return this;
		}
		
		public Builder maskType(int type){
			mMaskType = type;
			return this;
		}
		
		public Builder cornerRadius(int radius){
			mMaskTopLeftCornerRadius = radius;
			mMaskTopRightCornerRadius = radius;
			mMaskBottomLeftCornerRadius = radius;
			mMaskBottomRightCornerRadius = radius;
			return this;
		}
		
		public Builder topLeftCornerRadius(int radius){
			mMaskTopLeftCornerRadius = radius;
			return this;
		}
		
		public Builder topRightCornerRadius(int radius){
			mMaskTopRightCornerRadius = radius;
			return this;
		}
		
		public Builder bottomLeftCornerRadius(int radius){
			mMaskBottomLeftCornerRadius = radius;
			return this;
		}
		
		public Builder bottomRightCornerRadius(int radius){
			mMaskBottomRightCornerRadius = radius;
			return this;
		}
		
		public Builder padding(int padding){
			mMaskLeft = padding;
			mMaskTop = padding;
			mMaskRight = padding;
			mMaskBottom = padding;
			return this;
		}
		
		public Builder left(int padding){
			mMaskLeft = padding;
			return this;
		}
		
		public Builder top(int padding){
			mMaskTop = padding;
			return this;
		}
		
		public Builder right(int padding){
			mMaskRight = padding;
			return this;
		}
		
		public Builder bottom(int padding){
			mMaskBottom = padding;
			return this;
		}
	}
}
