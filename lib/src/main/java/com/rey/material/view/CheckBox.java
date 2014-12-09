package com.rey.material.view;

import com.rey.material.drawable.CheckBoxDrawable;

import android.content.Context;
import android.util.AttributeSet;

public class CheckBox extends CompoundButton {
	
	public CheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);				
	}

	public CheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}

	public CheckBox(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle){
		CheckBoxDrawable drawable = new CheckBoxDrawable.Builder(context, attrs, defStyle).build();
		drawable.setInEditMode(isInEditMode());
        drawable.setAnimEnable(false);
        setButtonDrawable(drawable);
        drawable.setAnimEnable(true);
    }

}
