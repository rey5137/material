package com.rey.material.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.rey.material.drawable.RadioButtonDrawable;

public class RadioButton extends CompoundButton {
	
	public RadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);				
	}

	public RadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}

	public RadioButton(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle){
		RadioButtonDrawable drawable = new RadioButtonDrawable.Builder(context, attrs, defStyle).build();
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
	
}
