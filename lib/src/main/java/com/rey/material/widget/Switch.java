package com.rey.material.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Checkable;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class Switch extends View implements Checkable {
		
	private RippleManager mRippleManager = new RippleManager();
	
	private boolean mRunning = false;
	
	private Paint mPaint;
	private RectF mDrawRect;
	private RectF mTempRect;
	private Path mPath;
	
	private int mStrokeSize;
	private ColorStateList mStrokeColors;
	private Paint.Cap mStrokeCap;	
	private int mThumbBorderSize;
	private int mThumbRadius;
	private ColorStateList mThumbColors;
	private float mThumbPosition;
	private int mMaxAnimDuration;
	private Interpolator mInterpolator;	
	private int mGravity = Gravity.CENTER_VERTICAL;
	
	private boolean mChecked = false;
	private float mMemoX;
	
	private float mStartX;
	private float mFlingVelocity;
	
	private long mStartTime;
	private int mAnimDuration;
	private float mStartPosition;
	
	private int[] mTempStates = new int[2];
		
	public Switch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);				
	}

	public Switch(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}

	public Switch(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle){
		mRippleManager.onCreate(this, context, attrs, defStyle);
				
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch, 0, defStyle);
		
		mStrokeSize = a.getDimensionPixelSize(R.styleable.Switch_sw_strokeSize, ThemeUtil.dpToPx(context, 2));
		mStrokeColors = a.getColorStateList(R.styleable.Switch_sw_strokeColor);
		int cap = a.getInteger(R.styleable.Switch_sw_strokeCap, 0);
		if(cap == 0)
			mStrokeCap = Paint.Cap.BUTT;
		else if(cap == 1)
			mStrokeCap = Paint.Cap.ROUND;
		else
			mStrokeCap = Paint.Cap.SQUARE;
		mThumbBorderSize = a.getDimensionPixelSize(R.styleable.Switch_sw_thumbBorderSize, ThemeUtil.dpToPx(context, 2));
		mThumbColors = a.getColorStateList(R.styleable.Switch_sw_thumbColor);
		mThumbRadius = a.getDimensionPixelSize(R.styleable.Switch_sw_thumbRadius, ThemeUtil.dpToPx(context, 8));
		mMaxAnimDuration = a.getInt(R.styleable.Switch_sw_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
		mGravity = a.getInt(R.styleable.Switch_android_gravity, Gravity.CENTER_VERTICAL);
		mChecked = a.getBoolean(R.styleable.Switch_android_checked, false);
		mThumbPosition = mChecked ? 1f : 0f;
		int resId = a.getResourceId(R.styleable.Switch_sw_interpolator, 0);
		mInterpolator = resId != 0 ? AnimationUtils.loadInterpolator(context, resId) : new DecelerateInterpolator();
		mFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();		
		
		a.recycle();
		
		if(mStrokeColors == null){
			int[][] states = new int[][]{
					new int[]{-android.R.attr.state_checked},
					new int[]{android.R.attr.state_checked},
			};
			int[] colors = new int[]{
					ThemeUtil.colorControlNormal(context, 0xFF000000),
					ThemeUtil.colorControlActivated(context, 0xFF000000),
			};				
			
			mStrokeColors = new ColorStateList(states, colors);
		}
		
		if(mThumbColors == null){
			int[][] states = new int[][]{
					new int[]{-android.R.attr.state_checked},
					new int[]{android.R.attr.state_checked},
			};
			int[] colors = new int[]{
					ThemeUtil.colorSwitchThumbNormal(context, 0xFF000000),
					ThemeUtil.colorControlActivated(context, 0xFF000000),
			};		
			
			mThumbColors = new ColorStateList(states, colors);
		}
		
		mPaint = new Paint();		
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(mStrokeCap);
		
		mDrawRect = new RectF();
		mTempRect = new RectF();
		mPath = new Path();		
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		if(l == mRippleManager)
			super.setOnClickListener(l);
		else{
			mRippleManager.setOnClickListener(l);
			setOnClickListener(mRippleManager);
		}
	}
		
	@Override
	public void setChecked(boolean checked) {		
		if(mChecked != checked)
			mChecked = checked;
		
		float desPos = mChecked ? 1f : 0f;
		
		if(mThumbPosition != desPos)
			startAnimation();
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {	
		if(isEnabled())
			setChecked(!mChecked);
	}
				
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		super.onTouchEvent(event);
		mRippleManager.onTouchEvent(event);
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mMemoX = event.getX();
				mStartX = mMemoX;
				mStartTime = SystemClock.uptimeMillis();
				break;
			case MotionEvent.ACTION_MOVE:
				float offset = (event.getX() - mMemoX) / (mDrawRect.width() - mThumbRadius * 2 - mThumbBorderSize); 
				mThumbPosition = Math.min(1f, Math.max(0f, mThumbPosition + offset));
				mMemoX = event.getX();
				invalidate();
				break;
			case MotionEvent.ACTION_UP:	
				float velocity = (event.getX() - mStartX) / (SystemClock.uptimeMillis() - mStartTime) * 1000;
				if(Math.abs(velocity) >= mFlingVelocity)
					setChecked(velocity > 0);
				else if((!mChecked && mThumbPosition < 0.1f) || (mChecked && mThumbPosition > 0.9f))
					toggle();
				else					
					setChecked(mThumbPosition > 0.5f);				
				break;
			case MotionEvent.ACTION_CANCEL:
				setChecked(mThumbPosition > 0.5f);
				break;
		}
		
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		switch (widthMode) {
			case MeasureSpec.UNSPECIFIED:
				widthSize = getSuggestedMinimumWidth();
				break;
			case MeasureSpec.AT_MOST:
				widthSize = Math.min(widthSize, getSuggestedMinimumWidth());
				break;
		}
				
		switch (heightMode) {
			case MeasureSpec.UNSPECIFIED:
				heightSize = getSuggestedMinimumHeight();
				break;
			case MeasureSpec.AT_MOST:
				heightSize = Math.min(heightSize, getSuggestedMinimumHeight());
				break;
		}
		
		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	public int getSuggestedMinimumWidth() {
		return (mThumbRadius * 2 + mThumbBorderSize) * 2 + getPaddingLeft() + getPaddingRight();
	}

	@Override
	public int getSuggestedMinimumHeight() {
		return mThumbRadius * 2 + mThumbBorderSize + getPaddingTop() + getPaddingBottom();
	}
		
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mDrawRect.left = getPaddingLeft();
		mDrawRect.right = w - getPaddingRight();
		
		int height = mThumbRadius * 2 + mThumbBorderSize;		
		int align = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
								
		switch (align) {
			case Gravity.TOP:
				mDrawRect.top = getPaddingTop();
				mDrawRect.bottom = mDrawRect.top + height;
				break;
			case Gravity.BOTTOM:
				mDrawRect.bottom = h - getPaddingBottom();
				mDrawRect.top = mDrawRect.bottom - height;
				break;
			default:
				mDrawRect.top = (h - height) / 2f;
				mDrawRect.bottom = mDrawRect.top + height;
				break;
		}		
	}

	private int getStrokeColor(boolean checked){
		mTempStates[0] = isEnabled() ? android.R.attr.state_enabled : -android.R.attr.state_enabled;
		mTempStates[1] = checked ? android.R.attr.state_checked : -android.R.attr.state_checked;
		
		return mStrokeColors.getColorForState(mTempStates, 0);
	}
	
	private int getThumbColor(boolean checked){
		mTempStates[0] = isEnabled() ? android.R.attr.state_enabled : -android.R.attr.state_enabled;
		mTempStates[1] = checked ? android.R.attr.state_checked : -android.R.attr.state_checked;
		
		return mThumbColors.getColorForState(mTempStates, 0);
	}
		
	private void getPath(float x, float y, float radius){
		float halfStroke = mStrokeSize / 2f;		
		
		mPath.reset();
		
		if(mStrokeCap != Paint.Cap.ROUND){
			mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
			float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);
			
			if(x - radius > mDrawRect.left){			
				mPath.moveTo(mDrawRect.left, y - halfStroke);		
				mPath.arcTo(mTempRect, 180 + angle, -angle * 2);
				mPath.lineTo(mDrawRect.left, y + halfStroke);
				mPath.close();
			}
			
			if(x + radius < mDrawRect.right){
				mPath.moveTo(mDrawRect.right, y - halfStroke);
				mPath.arcTo(mTempRect, -angle, angle * 2);
				mPath.lineTo(mDrawRect.right, y + halfStroke);
				mPath.close();
			}
		}
		else{
			float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);
			
			if(x - radius > mDrawRect.left){					
				float angle2 = (float)(Math.acos(Math.max(0f, (mDrawRect.left + halfStroke - x + radius) / halfStroke)) / Math.PI * 180);
				
				mTempRect.set(mDrawRect.left, y - halfStroke, mDrawRect.left + mStrokeSize, y + halfStroke);
				mPath.arcTo(mTempRect, 180 - angle2, angle2 * 2);
				
				mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
				mPath.arcTo(mTempRect, 180 + angle, -angle * 2);
				mPath.close();
			}
			
			if(x + radius < mDrawRect.right){
				float angle2 = (float)Math.acos(Math.max(0f, (x + radius - mDrawRect.right + halfStroke) / halfStroke));
				mPath.moveTo((float)(mDrawRect.right - halfStroke + Math.cos(angle2) * halfStroke), (float)(y + Math.sin(angle2) * halfStroke));
				
				angle2 = (float)(angle2 / Math.PI * 180);				
				mTempRect.set(mDrawRect.right - mStrokeSize, y - halfStroke, mDrawRect.right, y + halfStroke);
				mPath.arcTo(mTempRect, angle2, -angle2 * 2);
				
				mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
				mPath.arcTo(mTempRect, -angle, angle * 2);
				mPath.close();
			}
		}		
	}
	
	private float getInterpolation(float value){
		return 2 * value * (1f - value);
	}
	
	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		
		float shinkWidth = getInterpolation(mThumbPosition) * mThumbBorderSize;
		
		float outerRadius = mThumbRadius + mThumbBorderSize / 2f - shinkWidth;
		float x = (mDrawRect.width() - outerRadius * 2) * mThumbPosition + mDrawRect.left + outerRadius;
		float y = mDrawRect.centerY();
				
		getPath(x, y, outerRadius);
		mPaint.setColor(ColorUtil.getMiddleColor(getStrokeColor(false), getStrokeColor(true), mThumbPosition));
		mPaint.setStyle(Paint.Style.FILL);		
		canvas.drawPath(mPath, mPaint);		
		
		float strokeWidth = mThumbBorderSize + (mThumbRadius - mThumbBorderSize / 2f) * mThumbPosition - shinkWidth;			
		float radius = mThumbRadius - (strokeWidth - mThumbBorderSize) / 2f - shinkWidth;
		
		mPaint.setColor(ColorUtil.getMiddleColor(getThumbColor(false), getThumbColor(true), mThumbPosition));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(strokeWidth);
		canvas.drawCircle(x, y, radius, mPaint);
	}

	private void resetAnimation(){	
		mStartTime = SystemClock.uptimeMillis();
		mStartPosition = mThumbPosition;
		mAnimDuration = (int)(mMaxAnimDuration * (mChecked ? (1f - mStartPosition) : mStartPosition));
	}
		
	private void startAnimation() {
		if(getHandler() != null){
			resetAnimation();		
			mRunning = true;
			getHandler().postAtTime(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
		}
		else
			mThumbPosition = mChecked ? 1f : 0f;		
	    invalidate();  	    
	}
	
	private void stopAnimation() {			
		mRunning = false;
		getHandler().removeCallbacks(mUpdater);
		invalidate();
	}
	
	private final Runnable mUpdater = new Runnable() {

	    @Override
	    public void run() {
	    	update();
	    }
		    
	};
		
	private void update(){
		long curTime = SystemClock.uptimeMillis();	
		float progress = Math.min(1f, (float)(curTime - mStartTime) / mAnimDuration);
		float value = mInterpolator.getInterpolation(progress);
		
		mThumbPosition = mChecked ? (mStartPosition * (1 - value) + value) : (mStartPosition * (1 - value));
		
		if(progress == 1f)
			stopAnimation();
				
    	if(mRunning)
    		getHandler().postAtTime(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
    	
    	invalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.checked = isChecked();
        return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		  
        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
	}
	
	static class SavedState extends BaseSavedState {
        boolean checked;

        /**
         * Constructor called from {@link Switch#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }
        
        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean)in.readValue(null);
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        @Override
        public String toString() {
            return "Switch.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
	
}
