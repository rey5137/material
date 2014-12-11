package com.rey.material.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rey.material.R;
import com.rey.material.drawable.LineMorphingDrawable;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;

public class FloatingActionButton extends Button {

	private OvalShadowDrawable mBackground;
	private LineMorphingDrawable mIcon;
	private int mIconSize;
	private boolean mAutoSwitch;
		
	private RippleManager mRippleManager = new RippleManager();
	private RippleDrawable mRipple;
			
	public static FloatingActionButton make(Context context, int resId){
		return new FloatingActionButton(context, null, resId);
	}
	
 	public FloatingActionButton(Context context) {
		super(context);
		
		init(context, null, 0);
	}
	
	public FloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0);
	}
	
	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr);
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void init(Context context, AttributeSet attrs, int defStyleAttr) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, defStyleAttr);
		
		int radius = a.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_radius, ThemeUtil.dpToPx(context, 28));
		int elevation = a.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_elevation, ThemeUtil.dpToPx(context, 4));
		int bgColor = a.getColor(R.styleable.FloatingActionButton_fab_backgroundColor, ThemeUtil.colorAccent(context, 0xFFFAFAFA));
		int iconId = a.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
		mIconSize = a.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_iconSize, ThemeUtil.dpToPx(context, 24));		
		mAutoSwitch = a.getBoolean(R.styleable.FloatingActionButton_fab_autoSwitch, true);
		int rippleId = a.getResourceId(R.styleable.FloatingActionButton_ripple, 0);
		boolean delayClick = a.getBoolean(R.styleable.FloatingActionButton_delayClick, false);
		
		a.recycle();
		
		mBackground = new OvalShadowDrawable(radius, bgColor, elevation, elevation);
		
		if(iconId != 0)
			setIcon(new LineMorphingDrawable.Builder(context, attrs, iconId).build());
		
		mRippleManager.onCreate(this, context, null, 0);
		mRippleManager.setDelayClick(delayClick);
				
		if(rippleId != 0){
			RippleDrawable.Builder buidler = new RippleDrawable.Builder(context, attrs, rippleId);
			
			buidler.maskType(RippleDrawable.Mask.TYPE_OVAL)
					.backgroundDrawable(null)
					.left((int)mBackground.getPaddingLeft())
					.top((int)mBackground.getPaddingTop())
					.right((int)mBackground.getPaddingRight())
					.bottom((int)mBackground.getPaddingBottom());
			
			mRipple = buidler.build();
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				setBackground(mRipple);
			else
				setBackgroundDrawable(mRipple);
		}
		
		setClickable(true);
	}
	
	public int getRadius(){
		return mBackground.getRadius();
	}
	
	public void setRadius(int radius){
		if(mBackground.setRadius(radius))
			requestLayout();
	}
	
	@TargetApi(Build.VERSION_CODES.L)
	@Override
	public float getElevation() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return super.getElevation();
		
		return mBackground.getShadowSize();
	}
	
	@TargetApi(Build.VERSION_CODES.L)
	@Override
	public void setElevation(float elevation) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			super.setElevation(elevation);
		else if(mBackground.setShadow(elevation, elevation))
			requestLayout();
	}
	
	public int getIconState(){
		if(mIcon != null)
			return mIcon.getLineState();
		
		return -1;
	}
	
	public void setIconState(int state, boolean animation){
		if(mIcon != null)
			mIcon.switchLineState(state, animation);
	}
	
	public int getBackgroundColor(){
		return mBackground.getColor();
	}
	
	public LineMorphingDrawable getIcon(){
		return mIcon;
	}
	
	public void setIcon(LineMorphingDrawable icon){
		if(mIcon != null){
			mIcon.setCallback(null);
			unscheduleDrawable(mIcon);
		}
		
		mIcon = icon;
		mIcon.setCallback(this);
	}
	
	@Override
	public void setBackgroundColor(int color){
		mBackground.setColor(color);
	}
		
	public void show(Activity activity, int x, int y, int gravity){		
		if(getParent() == null){						
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
			updateParams(x, y, gravity, params);	
			
			activity.getWindow().addContentView(this, params);
		}
		else
			updateLocation(x, y, gravity);
	}
	
	public void show(ViewGroup parent, int x, int y, int gravity){		
		if(getParent() == null){						
			ViewGroup.LayoutParams params = parent.generateLayoutParams(null);
			params.width = mBackground.getIntrinsicWidth();
			params.height = mBackground.getIntrinsicHeight();
			updateParams(x, y, gravity, params);	
			
			parent.addView(this, params);
		}
		else
			updateLocation(x, y, gravity);
	}
	
	public void updateLocation(int x, int y, int gravity){
		if(getParent() != null)
			updateParams(x, y, gravity, getLayoutParams());		
	}
	
	private void updateParams(int x, int y, int gravity, ViewGroup.LayoutParams params){		
		int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
		
		switch (horizontalGravity) {
			case Gravity.LEFT:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft()));
				break;
			case Gravity.CENTER_HORIZONTAL:
				setLeftMargin(params, (int)(x - mBackground.getCenterX()));
				break;
			case Gravity.RIGHT:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft() - mBackground.getRadius() * 2));
				break;	
			default:
				setLeftMargin(params, (int)(x - mBackground.getPaddingLeft()));
				break;
		}
		
		int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
		
		switch (verticalGravity) {
			case Gravity.TOP:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop()));
				break;
			case Gravity.CENTER_VERTICAL:
				setTopMargin(params, (int)(y - mBackground.getCenterY()));
				break;
			case Gravity.BOTTOM:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop() - mBackground.getRadius() * 2));
				break;		
			default:
				setTopMargin(params, (int)(y - mBackground.getPaddingTop()));
				break;
		}
	}
		
	private void setLeftMargin(ViewGroup.LayoutParams params, int value){
		if(params instanceof FrameLayout.LayoutParams)
			((FrameLayout.LayoutParams)params).leftMargin = value;
		else if(params instanceof RelativeLayout.LayoutParams)
			((RelativeLayout.LayoutParams)params).leftMargin = value;
	}
	
	private void setTopMargin(ViewGroup.LayoutParams params, int value){
		if(params instanceof FrameLayout.LayoutParams)
			((FrameLayout.LayoutParams)params).topMargin = value;
		else if(params instanceof RelativeLayout.LayoutParams)
			((RelativeLayout.LayoutParams)params).topMargin = value;
	}
	
	public void dismiss(){
		if(getParent() != null)
			((ViewGroup)getParent()).removeView(this);		
	}
	
	@Override
	public boolean performClick() {		
		if(mIcon != null && mAutoSwitch)
			mIcon.switchLineState((mIcon.getLineState() + 1) % mIcon.getLineStateCount(), true);
		
		return super.performClick();
	}
	
	@Override
	protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mBackground == who || mIcon == who;
    }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mBackground.getIntrinsicWidth(), mBackground.getIntrinsicHeight());
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBackground.setBounds(0, 0, w, h);
		
		if(mIcon != null){
			float half = mIconSize / 2f;
			mIcon.setBounds((int)(mBackground.getCenterX() - half), (int)(mBackground.getCenterY() - half), (int)(mBackground.getCenterX() + half), (int)(mBackground.getCenterY() + half));
		}
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		mBackground.draw(canvas);
		super.draw(canvas);
		if(mIcon != null)
			mIcon.draw(canvas);
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
		int action = event.getActionMasked();
		
		if(action == MotionEvent.ACTION_DOWN && ! mBackground.isPointerOver(event.getX(), event.getY()))
			return false;
		
		boolean result = super.onTouchEvent(event);		
		return  mRippleManager.onTouchEvent(event) || result;
	}
	
	private class OvalShadowDrawable extends Drawable{

		private Paint mShadowPaint;
		private Paint mGlowPaint;
		private Paint mPaint;
		
		private int mRadius;
		private float mShadowSize;
		private float mShadowOffset;
		
		private Path mShadowPath;
		private Path mGlowPath;
		
		private RectF mTempRect = new RectF();
		
		private int mColor;
		
		private boolean mNeedBuildShadow = true;
		
		private static final int COLOR_SHADOW_START = 0x4C000000;
		private static final int COLOR_SHADOW_END = 0x00000000;
		
		public OvalShadowDrawable(int radius, int color, float shadowSize, float shadowOffset){
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
			mPaint.setStyle(Paint.Style.FILL);
			
			setColor(color);
			setRadius(radius);
			setShadow(shadowSize, shadowOffset);
		}
		
		public boolean setRadius(int radius){
			if(mRadius != radius){
				mRadius = radius;
				mNeedBuildShadow = true;
				invalidateSelf();
				
				return true;
			}
			
			return false;
		}
		
		public boolean setShadow(float size, float offset){
			if(mShadowSize != size || mShadowOffset != offset){
				mShadowSize = size;
				mShadowOffset = offset;
				mNeedBuildShadow = true;
				invalidateSelf();
				
				return true;
			}
			
			return false;
		}
		
		public void setColor(int color){
			if(mColor != color){
				mColor = color;
				mPaint.setColor(mColor);
				invalidateSelf();
			}
		}
		
		public int getColor(){
			return mColor;
		}
		
		public int getRadius(){
			return mRadius;
		}
		
		public float getShadowSize(){
			return mShadowSize;
		}
		
		public float getShadowOffset(){
			return mShadowOffset;
		}
		
		public float getPaddingLeft(){
			return mShadowSize;
		}
		
		public float getPaddingTop(){
			return mShadowSize;
		}
		
		public float getPaddingRight(){
			return mShadowSize;
		}
		
		public float getPaddingBottom(){
			return mShadowSize + mShadowOffset;
		}
		
		public float getCenterX(){
			return mRadius + mShadowSize; 
		}
		
		public float getCenterY(){
			return mRadius + mShadowSize;
		}
		
		public boolean isPointerOver(float x, float y){
			float distance = (float)Math.sqrt(Math.pow(x - getCenterX(), 2) + Math.pow(y - getCenterY(), 2));
			
			return distance < mRadius;
		}
		
		@Override
		public int getIntrinsicWidth() {
			return (int)((mRadius + mShadowSize) * 2 + 0.5f);
		}

		@Override
		public int getIntrinsicHeight() {			
			return (int)((mRadius + mShadowSize) * 2 + mShadowOffset + 0.5f);
		}

		private void buildShadow(){
			if(mShadowSize <= 0)
				return;
			
			if(mShadowPaint == null){
				mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
				mShadowPaint.setStyle(Paint.Style.FILL);
				mShadowPaint.setDither(true);
			}
			float startRatio = (float)mRadius / (mRadius + mShadowSize + mShadowOffset);
			mShadowPaint.setShader(new RadialGradient(0, 0, mRadius + mShadowSize,
	                new int[]{COLOR_SHADOW_START, COLOR_SHADOW_START, COLOR_SHADOW_END},
	                new float[]{0f, startRatio, 1f}
	                , Shader.TileMode.CLAMP));
			
			if(mShadowPath == null){
				mShadowPath = new Path();
				mShadowPath.setFillType(Path.FillType.EVEN_ODD);
			}
			else
				mShadowPath.reset();
			float radius = mRadius + mShadowSize;
			mTempRect.set(-radius, -radius, radius, radius);
			mShadowPath.addOval(mTempRect, Path.Direction.CW);
			radius = mRadius - 1;
			mTempRect.set(-radius, -radius - mShadowOffset, radius, radius - mShadowOffset);
			mShadowPath.addOval(mTempRect, Path.Direction.CW);
			
			if(mGlowPaint == null){
				mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
				mGlowPaint.setStyle(Paint.Style.FILL);
				mGlowPaint.setDither(true);
			}
			startRatio = (mRadius - mShadowSize / 2f) / (mRadius + mShadowSize / 2f);
			mGlowPaint.setShader(new RadialGradient(0, 0, mRadius + mShadowSize / 2f,
	                new int[]{COLOR_SHADOW_START, COLOR_SHADOW_START, COLOR_SHADOW_END},
	                new float[]{0f, startRatio, 1f}
	                , Shader.TileMode.CLAMP));
			
			if(mGlowPath == null){
				mGlowPath = new Path();
				mGlowPath.setFillType(Path.FillType.EVEN_ODD);
			}
			else
				mGlowPath.reset();
			
			radius = mRadius + mShadowSize / 2f;
			mTempRect.set(-radius, -radius, radius, radius);
			mGlowPath.addOval(mTempRect, Path.Direction.CW);
			radius = mRadius - 1;
			mTempRect.set(-radius, -radius, radius, radius);
			mGlowPath.addOval(mTempRect, Path.Direction.CW);
		}
		
		@Override
		public void draw(Canvas canvas) {
			if(mNeedBuildShadow){
				buildShadow();
				mNeedBuildShadow = false;
			}
			int saveCount;
			
			if(mShadowSize > 0){
				saveCount = canvas.save();			
				canvas.translate(mShadowSize + mRadius,  mShadowSize + mRadius + mShadowOffset);
				canvas.drawPath(mShadowPath, mShadowPaint);
				canvas.restoreToCount(saveCount);
			}
			
			saveCount = canvas.save();
			canvas.translate(mShadowSize + mRadius, mShadowSize + mRadius);
			if(mShadowSize > 0)
				canvas.drawPath(mGlowPath, mGlowPaint);			
			mTempRect.set(-mRadius, -mRadius, mRadius, mRadius);
			canvas.drawOval(mTempRect, mPaint);
			canvas.restoreToCount(saveCount);			
		}

		@Override
		public void setAlpha(int alpha) {
			mShadowPaint.setAlpha(alpha);
			mPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mShadowPaint.setColorFilter(cf);
			mPaint.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		
	}
	
}
