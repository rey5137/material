package com.rey.material.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class Button extends android.widget.Button {

	private RippleManager mRippleManager = new RippleManager();

    public Button(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

	public Button(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		mRippleManager.onCreate(this, context, attrs, defStyleAttr, defStyleRes);
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
