package com.rey.material.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import android.util.AttributeSet;
import android.view.MotionEvent;

import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ViewUtil;

public class CheckedTextView extends AppCompatCheckedTextView implements ThemeManager.OnThemeChangedListener {

	private RippleManager mRippleManager;
    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

    /**
     * Interface definition for a callback to be invoked when the checked state is changed.
     */
    public interface OnCheckedChangeListener{
        /**
         * Called when the checked state is changed.
         * @param view The CheckedTextView view.
         * @param checked The checked state.
         */
        void onCheckedChanged(CheckedTextView view, boolean checked);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckedTextView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public CheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

	public CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

	protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
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

    /**
     * Set a listener will be called when the checked state is changed.
     * @param listener The {@link OnCheckedChangeListener} will be called.
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
        mOnCheckedChangeListener = listener;
    }

    @Override
    public void setChecked(boolean checked) {
        boolean change = isChecked() != checked;
        super.setChecked(checked);
        if(change && mOnCheckedChangeListener != null)
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
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
}
