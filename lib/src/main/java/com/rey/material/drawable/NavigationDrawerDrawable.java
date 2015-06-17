package com.rey.material.drawable;

import com.rey.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class NavigationDrawerDrawable extends Drawable implements Drawable.Callback{

	private ToolbarRippleDrawable mRippleDrawable;
	private LineMorphingDrawable mLineDrawable;
		
	public static final int STATE_DRAWER = 0;
	public static final int STATE_ARROW = 1;
	
	public NavigationDrawerDrawable(ToolbarRippleDrawable rippleDrawable, LineMorphingDrawable lineDrawable){
		mRippleDrawable = rippleDrawable;
		mLineDrawable = lineDrawable;
		
		mRippleDrawable.setCallback(this);
		mLineDrawable.setCallback(this);
	}
	
	public void switchIconState(int state, boolean animation){
		mLineDrawable.switchLineState(state, animation);
	}
	
	public int getIconState(){
		return mLineDrawable.getLineState();
	}
	
	public boolean setIconState(int state, float progress){
		return mLineDrawable.setLineState(state, progress);
	}

    public float getIconAnimProgress(){
        return mLineDrawable.getAnimProgress();
    }
	
	@Override
	public void draw(Canvas canvas) {
		mRippleDrawable.draw(canvas);
		mLineDrawable.draw(canvas);
	}

	@Override
	public void setAlpha(int alpha) {
		mRippleDrawable.setAlpha(alpha);
		mLineDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mRippleDrawable.setColorFilter(cf);
		mLineDrawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		mRippleDrawable.setBounds(left, top, right, bottom);
		mLineDrawable.setBounds(left, top, right, bottom);
	}
	
	@Override
	public void setDither(boolean dither) {
		mRippleDrawable.setDither(dither);
		mLineDrawable.setDither(dither);
	}
	
	@Override
	public void invalidateDrawable(Drawable who) {
		invalidateSelf();
	}

	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when) {
		scheduleSelf(what, when);
	}

	@Override
	public void unscheduleDrawable(Drawable who, Runnable what) {
		unscheduleSelf(what);
	}
	
	@Override
	public boolean isStateful() {
		return true;		
	}
	
	@Override
	protected boolean onStateChange(int[] state) {		
		return mRippleDrawable.onStateChange(state);
	}
	
	public static class Builder{
		private ToolbarRippleDrawable mRippleDrawable;
		private LineMorphingDrawable mLineDrawable;
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavigationDrawerDrawable, defStyleAttr, defStyleRes);
						
			if(a != null){
				int rippleId = a.getResourceId(R.styleable.NavigationDrawerDrawable_nd_ripple, 0);
				int lineId = a.getResourceId(R.styleable.NavigationDrawerDrawable_nd_icon, 0);
					
				if(rippleId > 0)
					ripple(new ToolbarRippleDrawable.Builder(context, rippleId).build());
				
				if(lineId > 0){
					LineMorphingDrawable.Builder builder = new LineMorphingDrawable.Builder(context, lineId);
					line(builder.build());
				}
						
				a.recycle();
			}		
		}
		
		public NavigationDrawerDrawable build(){
			return new NavigationDrawerDrawable(mRippleDrawable, mLineDrawable);
		}
		
		public Builder ripple(ToolbarRippleDrawable drawable){
			mRippleDrawable = drawable;
			
			return this;
		}
		
		public Builder line(LineMorphingDrawable drawable){
			mLineDrawable = drawable;
			
			return this;
		}
		
	}
}
