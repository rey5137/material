package com.rey.material.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CompoundButton extends android.widget.CompoundButton {

	private RippleManager mRippleManager = new RippleManager();
	private Drawable mButtonDrawable;
	
	public CompoundButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);				
	}

	public CompoundButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}

	public CompoundButton(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void init(Context context, AttributeSet attrs, int defStyle){
		mRippleManager.onCreate(this, context, attrs, defStyle);
		
		//a fix to reset paddingLeft attribute
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
			TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.padding, android.R.attr.paddingLeft}, 0, defStyle);
			
			if(!a.hasValue(0) && !a.hasValue(1))
				setPadding(0, getPaddingTop(), getPaddingRight(), getPaddingBottom());
			
			a.recycle();
		}
		
		setClickable(true);
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
		return mRippleManager.onTouchEvent(event) || result;
	}
	
	@Override
	public void setButtonDrawable(Drawable d) {
		mButtonDrawable = d;
		super.setButtonDrawable(d);
	}
	
	@Override
    public int getCompoundPaddingLeft() {
		int padding = super.getCompoundPaddingLeft();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        	return padding;
        
        if (mButtonDrawable != null)
            padding += mButtonDrawable.getIntrinsicWidth();   
        
        return padding;
    }
}
