package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.drawable.ToolbarRippleDrawable;

public final class RippleManager implements View.OnClickListener, Runnable{

	private View.OnClickListener mClickListener;
	private View mView;
		
	public RippleManager(){}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onCreate(View v, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		if(v.isInEditMode())
			return;

		mView = v;
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleView, defStyleAttr, defStyleRes);
        int rippleStyle = a.getResourceId(R.styleable.RippleView_rd_style, 0);
		RippleDrawable drawable = null;

		if(rippleStyle != 0)
			drawable = new RippleDrawable.Builder(context, rippleStyle).backgroundDrawable(mView.getBackground()).build();
		else{
			boolean rippleEnable = a.getBoolean(R.styleable.RippleView_rd_enable, false);
			if(rippleEnable)
				drawable = new RippleDrawable.Builder(context, attrs, defStyleAttr, defStyleRes).backgroundDrawable(mView.getBackground()).build();
		}

		a.recycle();

		if(drawable != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				mView.setBackground(drawable);
			else
				mView.setBackgroundDrawable(drawable);
		}
	}
	
	public boolean isDelayClick(){
        Drawable background = mView.getBackground();
        if(background instanceof RippleDrawable)
            return ((RippleDrawable)background).isDelayClick();
        else if(background instanceof ToolbarRippleDrawable)
            return ((ToolbarRippleDrawable)background).isDelayClick();

        return false;
	}
	
	public void setDelayClick(boolean delay){
        Drawable background = mView.getBackground();
        if(background instanceof RippleDrawable)
            ((RippleDrawable)background).setDelayClick(delay);
        else if(background instanceof ToolbarRippleDrawable)
            ((ToolbarRippleDrawable)background).setDelayClick(delay);
	}
	
	public void setOnClickListener(View.OnClickListener l) {
		mClickListener = l;
	}


	public boolean onTouchEvent(MotionEvent event){
		Drawable background = mView.getBackground();
        return background instanceof RippleDrawable && ((RippleDrawable) background).onTouch(mView, event);
    }
	
	@Override
	public void onClick(View v) {
		Drawable background = mView.getBackground();
		long delay = 0;
						
		if(background instanceof RippleDrawable)
			delay = ((RippleDrawable)background).getClickDelayTime();
		else if(background instanceof ToolbarRippleDrawable)
			delay = ((ToolbarRippleDrawable)background).getClickDelayTime();
			
		if(delay > 0 && mView.getHandler() != null)
			mView.getHandler().postDelayed(this, delay);
		else
			run();
	}
		
	@Override
    public void run() {
    	if(mClickListener != null)
    		mClickListener.onClick(mView);
    }

	public static void cancelRipple(View v){
		Drawable background = v.getBackground();
		if(background instanceof RippleDrawable)
			((RippleDrawable)background).cancel();
		else if(background instanceof ToolbarRippleDrawable)
			((ToolbarRippleDrawable)background).cancel();
		
		if(v instanceof ViewGroup){
			ViewGroup vg = (ViewGroup)v;
			for(int i = 0, count = vg.getChildCount(); i < count; i++)
				RippleManager.cancelRipple(vg.getChildAt(i));
		}
	}
	
}
