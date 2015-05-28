package com.rey.material.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.rey.material.drawable.CheckBoxDrawable;
import com.rey.material.drawable.RadioButtonDrawable;

public class RadioButton extends CompoundButton {

    public RadioButton(Context context) {
        super(context);
    }

    public RadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	public RadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

    public RadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super.applyStyle(context, attrs, defStyleAttr, defStyleRes);

        RadioButtonDrawable drawable = new RadioButtonDrawable.Builder(context, attrs, defStyleAttr, defStyleRes).build();
        drawable.setInEditMode(isInEditMode());
        drawable.setAnimEnable(false);
        setButtonDrawable(drawable);
        drawable.setAnimEnable(true);
    }
	
	@Override
    public void toggle() {
        // we override to prevent toggle when the radio is already
        // checked (as opposed to check boxes widgets)
        if (!isChecked()) {
            super.toggle();
        }
    }

    /**
     * Change the checked state of this button immediately without showing animation.
     * @param checked The checked state.
     */
    public void setCheckedImmediately(boolean checked){
        if(mButtonDrawable instanceof RadioButtonDrawable){
            RadioButtonDrawable drawable = (RadioButtonDrawable)mButtonDrawable;
            drawable.setAnimEnable(false);
            setChecked(checked);
            drawable.setAnimEnable(true);
        }
        else
            setChecked(checked);
    }
	
}
