package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.PaddingDrawable;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ViewUtil;

public class CompoundButton extends android.widget.CompoundButton implements ThemeManager.OnThemeChangedListener {

	private RippleManager mRippleManager;
    private volatile PaddingDrawable mPaddingDrawable;
    private boolean mIsRtl = false;

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

    public CompoundButton(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public CompoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

	public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

	protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            applyPadding(context, attrs, defStyleAttr, defStyleRes);

		setClickable(true);
        ViewUtil.applyFont(this, attrs, defStyleAttr, defStyleRes);
        applyStyle(context, attrs, defStyleAttr, defStyleRes);

        if(!isInEditMode())
            mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
	}

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        getRippleManager().onCreate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void applyPadding(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.padding, android.R.attr.paddingLeft, android.R.attr.paddingTop, android.R.attr.paddingRight, android.R.attr.paddingBottom, android.R.attr.paddingStart, android.R.attr.paddingEnd}, defStyleAttr, defStyleRes);

        int padding = -1;
        int leftPadding = -1;
        int topPadding = -1;
        int rightPadding = -1;
        int bottomPadding = -1;
        int startPadding = Integer.MIN_VALUE;
        int endPadding = Integer.MIN_VALUE;

        boolean startPaddingDefined = false;
        boolean endPaddingDefined = false;
        boolean leftPaddingDefined = false;
        boolean rightPaddingDefined = false;

        for(int i = 0, count = a.getIndexCount(); i < count; i++) {
            int attr = a.getIndex(i);
            if(attr == 0) {
                padding = a.getDimensionPixelSize(attr, -1);
                leftPaddingDefined = true;
                rightPaddingDefined = true;
            }
            else if(attr == 1) {
                leftPadding = a.getDimensionPixelSize(attr, -1);
                leftPaddingDefined = true;
            }
            else if(attr == 2)
                topPadding = a.getDimensionPixelSize(attr, -1);
            else if(attr == 3) {
                rightPadding = a.getDimensionPixelSize(attr, -1);
                rightPaddingDefined = true;
            }
            else if(attr == 4)
                bottomPadding = a.getDimensionPixelSize(attr, -1);
            else if(attr == 5) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    startPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                    startPaddingDefined = (startPadding != Integer.MIN_VALUE);
                }
            }
            else if(attr == 6) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    endPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                    endPaddingDefined = (endPadding != Integer.MIN_VALUE);
                }
            }
        }

        a.recycle();

        if (padding >= 0)
            setPadding(padding, padding, padding, padding);
        else{
            if(leftPaddingDefined || rightPaddingDefined)
                setPadding(leftPaddingDefined ? leftPadding : getPaddingLeft(),
                        topPadding >= 0 ? topPadding : getPaddingTop(),
                        rightPaddingDefined ? rightPadding : getPaddingRight(),
                        bottomPadding >= 0 ? bottomPadding : getPaddingBottom());

            if(startPaddingDefined || endPaddingDefined)
                setPaddingRelative(startPaddingDefined ? startPadding : getPaddingStart(),
                        topPadding >= 0 ? topPadding : getPaddingTop(),
                        endPaddingDefined ? endPadding : getPaddingEnd(),
                        bottomPadding >= 0 ? bottomPadding : getPaddingBottom());
        }
    }

    private PaddingDrawable getPaddingDrawable(){
        if(mPaddingDrawable == null){
            synchronized (this){
                if(mPaddingDrawable == null)
                    mPaddingDrawable = new PaddingDrawable(null);
            }
        }

        return mPaddingDrawable;
    }

    @Override
    public void setTextAppearance(int resId) {
        ViewUtil.applyTextAppearance(this, resId);
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        ViewUtil.applyTextAppearance(this, resId);
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
        RippleManager.cancelRipple(this);
        if(mStyleId != 0)
            ThemeManager.getInstance().unregisterOnThemeChangedListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
        if(mIsRtl != rtl) {
            mIsRtl = rtl;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            else
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());

            setCompoundDrawablePadding(getCompoundDrawablePadding());
            invalidate();
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        Drawable background = getBackground();
        if(background instanceof RippleDrawable && !(drawable instanceof RippleDrawable))
            ((RippleDrawable) background).setBackgroundDrawable(drawable);
        else
            super.setBackgroundDrawable(drawable);
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
		boolean result = super.onTouchEvent(event);
		return  getRippleManager().onTouchEvent(this, event) || result;
	}
	
	@Override
	public void setButtonDrawable(Drawable d) {
        super.setButtonDrawable(null);
        getPaddingDrawable().setWrappedDrawable(d);
		super.setButtonDrawable(getPaddingDrawable());
	}

    @Override
    public Drawable getButtonDrawable(){
        return getPaddingDrawable().getWrappedDrawable();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        PaddingDrawable drawable = getPaddingDrawable();
        if (mIsRtl)
            drawable.setPadding(drawable.getPaddingLeft(), top, right, bottom);
        else
            drawable.setPadding(left, top, drawable.getPaddingRight(), bottom);

        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        PaddingDrawable drawable = getPaddingDrawable();
        if (mIsRtl)
            drawable.setPadding(drawable.getPaddingLeft(), top, start, bottom);
        else
            drawable.setPadding(start, top, drawable.getPaddingRight(), bottom);

        super.setPaddingRelative(start, top, end, bottom);
    }

    @Override
    public void setCompoundDrawablePadding(int pad) {
        PaddingDrawable drawable = getPaddingDrawable();
        if (mIsRtl)
            drawable.setPadding(pad, drawable.getPaddingTop(), drawable.getPaddingRight(), drawable.getPaddingBottom());
        else
            drawable.setPadding(drawable.getPaddingLeft(), drawable.getPaddingTop(), pad, drawable.getPaddingBottom());

        super.setCompoundDrawablePadding(pad);
    }

    @Override
    public int getCompoundPaddingLeft() {
        if(mIsRtl)
            return getPaddingLeft();
        else {
            PaddingDrawable drawable = getPaddingDrawable();
            return drawable.getIntrinsicWidth();
        }
    }

    @Override
    public int getCompoundPaddingRight() {
        if(!mIsRtl)
            return getPaddingRight();
        else{
            PaddingDrawable drawable = getPaddingDrawable();
            return drawable.getIntrinsicWidth();
        }
    }
}
