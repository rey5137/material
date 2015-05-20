package com.rey.material.widget;

import com.rey.material.R;
import com.rey.material.drawable.CheckBoxDrawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class CheckBox extends CompoundButton {

    public CheckBox(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

	public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxDrawable);
            mIsDrawableOnRight = styledAttrs.getBoolean(R.styleable.CheckBoxDrawable_cbd_drawableOnRight, false);
        }
		applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        CheckBoxDrawable drawable = new CheckBoxDrawable.Builder(context, attrs, defStyleAttr, defStyleRes).build();
        drawable.setInEditMode(isInEditMode());
        drawable.setAnimEnable(false);
        setButtonDrawable(drawable);
        if(mIsDrawableOnRight) {
            setButtonDrawable(null);
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
        drawable.setAnimEnable(true);
    }

    /**
     * Change the checked state of this button immediately without showing animation.
     * @param checked The checked state.
     */
    public void setCheckedImmediately(boolean checked){
        if(mButtonDrawable instanceof CheckBoxDrawable){
            CheckBoxDrawable drawable = (CheckBoxDrawable)mButtonDrawable;
            drawable.setAnimEnable(false);
            setChecked(checked);
            drawable.setAnimEnable(true);
        }
        else
            setChecked(checked);
    }

}
