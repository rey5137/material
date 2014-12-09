package com.rey.material.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class Button extends android.widget.Button {

	private RippleManager mRippleManager = new RippleManager();
	
	public Button(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);
	}

	public Button(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}

	public Button(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle){
		mRippleManager.onCreate(this, context, attrs, defStyle);
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
    public boolean onTouchEvent(@NonNull MotionEvent event) {
		boolean result = super.onTouchEvent(event);
		return  mRippleManager.onTouchEvent(event) || result;
	}

}
