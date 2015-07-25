package com.rey.material.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.LineMorphingDrawable;
import com.rey.material.drawable.OvalShadowDrawable;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class FloatingActionButton extends View implements ThemeManager.OnThemeChangedListener {

	private OvalShadowDrawable mBackground;
	private Drawable mIcon;
    private Drawable mPrevIcon;
    private int mAnimDuration = -1;
    private Interpolator mInterpolator;
    private SwitchIconAnimator mSwitchIconAnimator;
	private int mIconSize = -1;
		
	private RippleManager mRippleManager;
    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;
			
	public static FloatingActionButton make(Context context, int resId){
		return new FloatingActionButton(context, null, resId);
	}
	
 	public FloatingActionButton(Context context) {
		super(context);
		
		init(context, null, 0, 0);
	}
	
	public FloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0, 0);
	}
	
	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		setClickable(true);
        mSwitchIconAnimator = new SwitchIconAnimator();
        applyStyle(context, attrs, defStyleAttr, defStyleRes);

        mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
	}

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, defStyleRes);

        int radius = -1;
        int elevation = -1;
        ColorStateList bgColor = null;
        int bgAnimDuration = -1;
        int iconSrc = 0;
        int iconLineMorphing = 0;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.FloatingActionButton_fab_radius)
                radius = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_elevation)
                elevation = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_backgroundColor)
                bgColor = a.getColorStateList(attr);
            else if(attr == R.styleable.FloatingActionButton_fab_backgroundAnimDuration)
                bgAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_iconSrc)
                iconSrc = a.getResourceId(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_iconLineMorphing)
                iconLineMorphing = a.getResourceId(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_iconSize)
                mIconSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_animDuration)
                mAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.FloatingActionButton_fab_interpolator){
                int resId = a.getResourceId(R.styleable.FloatingActionButton_fab_interpolator, 0);
                if(resId != 0)
                    mInterpolator = AnimationUtils.loadInterpolator(context, resId);
            }
        }

        a.recycle();

        if(mIconSize < 0)
            mIconSize = ThemeUtil.dpToPx(context, 24);

        if(mAnimDuration < 0)
            mAnimDuration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        if(mInterpolator == null)
            mInterpolator = new DecelerateInterpolator();

        if(mBackground == null){
            if(radius < 0)
                radius = ThemeUtil.dpToPx(context, 28);

            if(elevation < 0)
                elevation = ThemeUtil.dpToPx(context, 4);

            if(bgColor == null)
                bgColor = ColorStateList.valueOf(ThemeUtil.colorAccent(context, 0));

            if(bgAnimDuration < 0)
                bgAnimDuration = 0;

            mBackground = new OvalShadowDrawable(radius, bgColor, elevation, elevation, bgAnimDuration);
            mBackground.setInEditMode(isInEditMode());
            mBackground.setBounds(0, 0, getWidth(), getHeight());
            mBackground.setCallback(this);
        }
        else{
            if(radius >= 0)
                mBackground.setRadius(radius);

            if(bgColor != null)
                mBackground.setColor(bgColor);

            if(elevation >= 0)
                mBackground.setShadow(elevation, elevation);

            if(bgAnimDuration >= 0)
                mBackground.setAnimationDuration(bgAnimDuration);
        }

        if(iconLineMorphing != 0)
            setIcon(new LineMorphingDrawable.Builder(context, iconLineMorphing).build(), false);
        else if(iconSrc != 0)
            setIcon(context.getResources().getDrawable(iconSrc), false);

        getRippleManager().onCreate(this, context, attrs, defStyleAttr, defStyleRes);
        Drawable background = getBackground();
        if(background != null && background instanceof RippleDrawable){
            RippleDrawable drawable = (RippleDrawable)background;
            drawable.setBackgroundDrawable(null);
            drawable.setMask(RippleDrawable.Mask.TYPE_OVAL, 0, 0, 0, 0, (int)mBackground.getPaddingLeft(), (int)mBackground.getPaddingTop(), (int)mBackground.getPaddingRight(), (int)mBackground.getPaddingBottom());
        }
    }

    @Override
    public void onThemeChanged(ThemeManager.OnThemeChangedEvent event) {
        int style = ThemeManager.getInstance().getCurrentStyle(mStyleId);
        if(mCurrentStyle != style){
            mCurrentStyle = style;
            applyStyle(mCurrentStyle);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mStyleId != 0) {
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
            onThemeChanged(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRippleManager.cancelRipple(this);
        if(mStyleId != 0)
            ThemeManager.getInstance().unregisterOnThemeChangedListener(this);
    }


    /**
     * @return The radius of the button.
     */
	public int getRadius(){
		return mBackground.getRadius();
	}

    /**
     * Set radius of the button.
     * @param radius The radius in pixel.
     */
	public void setRadius(int radius){
		if(mBackground.setRadius(radius))
			requestLayout();
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public float getElevation() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return super.getElevation();
		
		return mBackground.getShadowSize();
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void setElevation(float elevation) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			super.setElevation(elevation);
		else if(mBackground.setShadow(elevation, elevation))
			requestLayout();
	}

    /**
     * @return The line state of LineMorphingDrawable that is used as this button's icon.
     */
	public int getLineMorphingState(){
		if(mIcon != null && mIcon instanceof LineMorphingDrawable)
			return ((LineMorphingDrawable)mIcon).getLineState();
		
		return -1;
	}

    /**
     * Set the line state of LineMorphingDrawable that is used as this button's icon.
     * @param state The line state.
     * @param animation Indicate should show animation when switch line state or not.
     */
	public void setLineMorphingState(int state, boolean animation){
		if(mIcon != null && mIcon instanceof LineMorphingDrawable)
            ((LineMorphingDrawable)mIcon).switchLineState(state, animation);
	}

    /**
     * @return The background color of this button.
     */
	public ColorStateList getBackgroundColor(){
		return mBackground.getColor();
	}

    /**
     * @return The drawable is used as this button's icon.
     */
	public Drawable getIcon(){
		return mIcon;
	}

    /**
     * Set the drawable that is used as this button's icon.
     * @param icon The drawable.
     * @param animation Indicate should show animation when switch drawable or not.
     */
	public void setIcon(Drawable icon, boolean animation){
        if(icon == null)
            return;

        if(animation) {
            mSwitchIconAnimator.startAnimation(icon);
            invalidate();
        }
        else{
            if(mIcon != null){
                mIcon.setCallback(null);
                unscheduleDrawable(mIcon);
            }

            mIcon = icon;
            float half = mIconSize / 2f;
            mIcon.setBounds((int)(mBackground.getCenterX() - half), (int)(mBackground.getCenterY() - half), (int)(mBackground.getCenterX() + half), (int)(mBackground.getCenterY() + half));
            mIcon.setCallback(this);
            invalidate();
        }
	}

	public void setBackgroundColor(ColorStateList color){
		mBackground.setColor(color);
		invalidate();
	}

	@Override
	public void setBackgroundColor(int color){
		mBackground.setColor(color);
        invalidate();
	}

    /**
     * Show this button at the specific location. If this button isn't attached to any parent view yet,
     * it will be add to activity's root view. If not, it will just update the location.
     * @param activity The activity that this button will be attached to.
     * @param x The x value of anchor point.
     * @param y The y value of anchor point.
     * @param gravity The gravity apply with this button.
     *
     * @see android.view.Gravity
     */
	public void show(Activity activity, int x, int y, int gravity){		
		if(getParent() == null){						
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
			updateParams(x, y, gravity, params);

			activity.getWindow().addContentView(this, params);
		}
		else
			updateLocation(x, y, gravity);
	}

    /**
     * Show this button at the specific location. If this button isn't attached to any parent view yet,
     * it will be add to activity's root view. If not, it will just update the location.
     * @param parent The parent view. Should be {@link android.widget.FrameLayout} or {@link android.widget.RelativeLayout}
     * @param x The x value of anchor point.
     * @param y The y value of anchor point.
     * @param gravity The gravity apply with this button.
     *
     * @see android.view.Gravity
     */
	public void show(ViewGroup parent, int x, int y, int gravity){		
		if(getParent() == null){						
			ViewGroup.LayoutParams params = parent.generateLayoutParams(null);
			params.width = mBackground.getIntrinsicWidth();
			params.height = mBackground.getIntrinsicHeight();
			updateParams(x, y, gravity, params);	
			
			parent.addView(this, params);
		}
		else
			updateLocation(x, y, gravity);
	}

    /**
     * Update the location of this button. This method only work if it's already attached to a parent view.
     * @param x The x value of anchor point.
     * @param y The y value of anchor point.
     * @param gravity The gravity apply with this button.
     *
     * @see android.view.Gravity
     */
	public void updateLocation(int x, int y, int gravity){
		if(getParent() != null)
			updateParams(x, y, gravity, getLayoutParams());
        else
            Log.v(FloatingActionButton.class.getSimpleName(), "updateLocation() is called without parent");
	}
	
	private void updateParams(int x, int y, int gravity, ViewGroup.LayoutParams params){		
		int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
		
		switch (horizontalGravity) {
			case Gravity.LEFT:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft()));
				break;
			case Gravity.CENTER_HORIZONTAL:
				setLeftMargin(params, (int)(x - mBackground.getCenterX()));
				break;
			case Gravity.RIGHT:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft() - mBackground.getRadius() * 2));
				break;	
			default:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft()));
				break;
		}
		
		int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
		
		switch (verticalGravity) {
			case Gravity.TOP:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop()));
				break;
			case Gravity.CENTER_VERTICAL:
				setTopMargin(params, (int)(y - mBackground.getCenterY()));
				break;
			case Gravity.BOTTOM:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop() - mBackground.getRadius() * 2));
				break;		
			default:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop()));
				break;
		}

        setLayoutParams(params);
	}
		
	private void setLeftMargin(ViewGroup.LayoutParams params, int value){
		if(params instanceof FrameLayout.LayoutParams)
			((FrameLayout.LayoutParams)params).leftMargin = value;
		else if(params instanceof RelativeLayout.LayoutParams)
			((RelativeLayout.LayoutParams)params).leftMargin = value;
        else
            Log.v(FloatingActionButton.class.getSimpleName(), "cannot recognize LayoutParams: " + params);
	}
	
	private void setTopMargin(ViewGroup.LayoutParams params, int value){
		if(params instanceof FrameLayout.LayoutParams)
			((FrameLayout.LayoutParams)params).topMargin = value;
		else if(params instanceof RelativeLayout.LayoutParams)
			((RelativeLayout.LayoutParams)params).topMargin = value;
        else
            Log.v(FloatingActionButton.class.getSimpleName(), "cannot recognize LayoutParams: " + params);
	}

    /**
     * Remove this button from parent view.
     */
	public void dismiss(){
		if(getParent() != null)
			((ViewGroup)getParent()).removeView(this);		
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mBackground == who || mIcon == who || mPrevIcon == who;
    }

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if(mBackground != null)
			mBackground.setState(getDrawableState());
		if(mIcon != null)
			mIcon.setState(getDrawableState());
		if(mPrevIcon != null)
			mPrevIcon.setState(getDrawableState());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBackground.setBounds(0, 0, w, h);
		
		if(mIcon != null){
			float half = mIconSize / 2f;
			mIcon.setBounds((int)(mBackground.getCenterX() - half), (int)(mBackground.getCenterY() - half), (int)(mBackground.getCenterX() + half), (int)(mBackground.getCenterY() + half));
		}

        if(mPrevIcon != null){
            float half = mIconSize / 2f;
            mPrevIcon.setBounds((int)(mBackground.getCenterX() - half), (int)(mBackground.getCenterY() - half), (int)(mBackground.getCenterX() + half), (int)(mBackground.getCenterY() + half));
        }
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		mBackground.draw(canvas);
		super.draw(canvas);
        if(mPrevIcon != null)
            mPrevIcon.draw(canvas);
		if(mIcon != null)
			mIcon.draw(canvas);
	}

	protected RippleManager getRippleManager(){
		if(mRippleManager == null){
			synchronized (RippleManager.class){
				if(mRippleManager == null)
					mRippleManager = new RippleManager();
			}
		}

		return mRippleManager;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		RippleManager rippleManager = getRippleManager();
		if (l == rippleManager)
			super.setOnClickListener(l);
		else {
			rippleManager.setOnClickListener(l);
			setOnClickListener(rippleManager);
		}
	}

	@Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
		int action = event.getActionMasked();
		
		if(action == MotionEvent.ACTION_DOWN && ! mBackground.isPointerOver(event.getX(), event.getY()))
			return false;
		
		boolean result = super.onTouchEvent(event);		
		return  getRippleManager().onTouchEvent(event) || result;
	}

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.state = getLineMorphingState();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        if(ss.state >= 0)
            setLineMorphingState(ss.state, false);
        requestLayout();
    }

    static class SavedState extends BaseSavedState {
        int state;

        /**
         * Constructor called from {@link Slider#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            state = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
        }

        @Override
        public String toString() {
            return "FloatingActionButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " state=" + state + "}";
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

    class SwitchIconAnimator implements Runnable{

        boolean mRunning = false;
        long mStartTime;

        public void resetAnimation(){
            mStartTime = SystemClock.uptimeMillis();
            mIcon.setAlpha(0);
            mPrevIcon.setAlpha(255);
        }

        public boolean startAnimation(Drawable icon) {
            if(mIcon == icon)
                return false;

            mPrevIcon = mIcon;
            mIcon = icon;
            float half = mIconSize / 2f;
            mIcon.setBounds((int)(mBackground.getCenterX() - half), (int)(mBackground.getCenterY() - half), (int)(mBackground.getCenterX() + half), (int)(mBackground.getCenterY() + half));
            mIcon.setCallback(FloatingActionButton.this);

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else {
                mPrevIcon.setCallback(null);
                unscheduleDrawable(mPrevIcon);
                mPrevIcon = null;
            }

            invalidate();
            return true;
        }

        public void stopAnimation() {
            mRunning = false;
            mPrevIcon.setCallback(null);
            unscheduleDrawable(mPrevIcon);
            mPrevIcon = null;
            mIcon.setAlpha(255);
            if(getHandler() != null)
                getHandler().removeCallbacks(this);
            invalidate();
        }

        @Override
        public void run() {
            long curTime = SystemClock.uptimeMillis();
            float progress = Math.min(1f, (float)(curTime - mStartTime) / mAnimDuration);
            float value = mInterpolator.getInterpolation(progress);

            mIcon.setAlpha(Math.round(255 * value));
            mPrevIcon.setAlpha(Math.round(255 * (1f - value)));

            if(progress == 1f)
                stopAnimation();

            if(mRunning) {
                if(getHandler() != null)
                    getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
                else
                    stopAnimation();
            }

            invalidate();
        }

    }
}
