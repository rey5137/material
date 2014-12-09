package com.rey.material.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.rey.material.R;
import com.rey.material.drawable.CircularProgressDrawable;
import com.rey.material.drawable.LinearProgressDrawable;

public class ProgressView extends View {

	private boolean mAutostart;
	private boolean mCircular;
	private int mProgressId;
	
	public static final int MODE_DETERMINATE = 0;
	public static final int MODE_INDETERMINATE = 1;
	public static final int MODE_BUFFER = 2;
	public static final int MODE_QUERY = 3;
	
	
	private Drawable mProgressDrawable;
	
	public ProgressView(Context context) {
		this(context, null, 0);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public ProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, defStyle);
		mAutostart = a.getBoolean(R.styleable.ProgressView_pv_autostart, true);
		mCircular = a.getBoolean(R.styleable.ProgressView_pv_circular, true);
		mProgressId = a.getResourceId(R.styleable.ProgressView_pv_progressStyle, 0);	
		
		a.recycle();
		
		if(mProgressId > 0){
			if(mCircular)
				mProgressDrawable = new CircularProgressDrawable.Builder(context, attrs, mProgressId).build();
			else
				mProgressDrawable = new LinearProgressDrawable.Builder(context, attrs, mProgressId).build();
						
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
				setBackground(mProgressDrawable);
			else
				setBackgroundDrawable(mProgressDrawable);
		}
	}
	
	@Override
    public void setVisibility(int v) {
        if(getVisibility() != v) {
            super.setVisibility(v);

            if (getProgressMode() == MODE_INDETERMINATE && mAutostart) {
                if (v == GONE || v == INVISIBLE)
                	stop();
                else
                	start();
            }
        }
    }
	
	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);

	    if (getProgressMode() == MODE_INDETERMINATE && mAutostart) {
	    	if (visibility == GONE || visibility == INVISIBLE)
	    		stop();
            else
             	start();
        }
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	    if (getProgressMode() == MODE_INDETERMINATE && mAutostart) 
	    	start();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (getProgressMode() == MODE_INDETERMINATE && mAutostart) 
	    	stop();
		
	    super.onDetachedFromWindow();
	}
	 
	public int getProgressMode(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getProgressMode();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getProgressMode();
	}
	
	public float getProgress(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getProgress();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getProgress();
	}
	
	public float getSecondaryProgress(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getSecondaryProgress();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getSecondaryProgress();
	}
	
	public void setProgress(float percent){
		if(mCircular)
			((CircularProgressDrawable)mProgressDrawable).setProgress(percent);
		else
			((LinearProgressDrawable)mProgressDrawable).setProgress(percent);
	}
	
	public void setSecondaryProgress(float percent){
		if(mCircular)
			((CircularProgressDrawable)mProgressDrawable).setSecondaryProgress(percent);
		else
			((LinearProgressDrawable)mProgressDrawable).setSecondaryProgress(percent);
	}
	
	public void start(){
		if(mProgressDrawable != null)
			((Animatable)mProgressDrawable).start();
	}
	
	public void stop(){
		if(mProgressDrawable != null)
			((Animatable)mProgressDrawable).stop();
	}

}
