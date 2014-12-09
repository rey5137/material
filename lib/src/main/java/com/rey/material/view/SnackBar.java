package com.rey.material.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;

public class SnackBar extends FrameLayout {

	private TextView mText;
	private Button mAction;
	
	public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
	public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
	
	private BackgroundDrawable mBackground;
	private int mMarginLeft;
	private int mMarginBottom;
	private int mWidth;
	private int mHeight;
	private int mInAnimationId;
	private int mOutAnimationId;
	private long mDuration = -1;
	private int mActionId;
	
	private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };
	
	private int mState = STATE_DISMISSED;
		
	public static final int STATE_DISMISSED = 0;
	public static final int STATE_SHOWED = 1;
	public static final int STATE_SHOWING = 2;
	public static final int STATE_DISMISSING = 3;
	
	public interface OnActionClickListener{
		
		public void onActionClick(SnackBar sb, int actionId);
	}
	
	private OnActionClickListener mActionClickListener;
	
	public interface OnStateChangeListener{
		
		public void onStateChange(SnackBar sb, int oldState, int newState);
	}
	
	private OnStateChangeListener mStateChangeListener;
		
	public static SnackBar make(Context context){
		return new SnackBar(context);
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private SnackBar(Context context){
		super(context);
		
		mText = new TextView(context);
		mText.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
		addView(mText, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mAction = new Button(context);
		mAction.setBackgroundResource(0);
		mAction.setGravity(Gravity.CENTER);
		mAction.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mActionClickListener != null)
					mActionClickListener.onActionClick(SnackBar.this, mActionId);
				
				dismiss();
			}
			
		});
		addView(mAction, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		
		mBackground = new BackgroundDrawable();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			setBackground(mBackground);
		else
			setBackgroundDrawable(mBackground);
		
		setClickable(true);
	}
		
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = 0;
		int height = 0;
		
		if(mAction.getVisibility() == View.VISIBLE){
			mAction.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightMeasureSpec);
			mText.measure(MeasureSpec.makeMeasureSpec(widthSize - (mAction.getMeasuredWidth() - mText.getPaddingRight()), widthMode), heightMeasureSpec);
			width = mText.getMeasuredWidth() + mAction.getMeasuredWidth() - mText.getPaddingRight();
		}
		else{
			mText.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), heightMeasureSpec);
			width = mText.getMeasuredWidth();
		}
				
		height = Math.max(mText.getMeasuredHeight(), mAction.getMeasuredHeight());
		
		switch (widthMode) {
			case MeasureSpec.AT_MOST:
				width = Math.min(widthSize, width);
				break;
			case MeasureSpec.EXACTLY:
				width = widthSize;
				break;
		}
		
		switch (heightMode) {
			case MeasureSpec.AT_MOST:
				height = Math.min(heightSize, height);
				break;
			case MeasureSpec.EXACTLY:
				height = heightSize;
				break;
		}
				
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = getPaddingLeft();
		int childRight = r - l - getPaddingRight();
		int childTop = getPaddingTop();
		int childBottom = b - t - getPaddingBottom();
				
		if(mAction.getVisibility() == View.VISIBLE){
			mAction.layout(childRight - mAction.getMeasuredWidth(), childTop, childRight, childBottom);
			mText.layout(childLeft, childTop, childRight - mAction.getMeasuredWidth() + mText.getPaddingRight(), childBottom);
		}
		else			
			mText.layout(childLeft, childTop, childRight, childBottom);
	}		
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public SnackBar applyStyle(int resId){
		Context context = getContext();
		TypedArray a = context.obtainStyledAttributes(null, R.styleable.SnackBar, 0, resId);
		
		int backgroundColor = a.getColor(R.styleable.SnackBar_sb_backgroundColor, 0xFF323232);
		int backgroundRadius = a.getDimensionPixelSize(R.styleable.SnackBar_sb_backgroundCornerRadius, 0);			
		int horizontalPadding = a.getDimensionPixelSize(R.styleable.SnackBar_sb_horizontalPadding, ThemeUtil.dpToPx(context, 24));
		int verticalPadding = a.getDimensionPixelSize(R.styleable.SnackBar_sb_verticalPadding, 0);
		TypedValue value = a.peekValue(R.styleable.SnackBar_sb_width);
		if(value.type == TypedValue.TYPE_INT_DEC)
			mWidth = a.getInteger(R.styleable.SnackBar_sb_width, MATCH_PARENT);
		else
			mWidth = a.getDimensionPixelSize(R.styleable.SnackBar_sb_width, MATCH_PARENT);		
		int minWidth = a.getDimensionPixelSize(R.styleable.SnackBar_sb_minWidth, 0);
		int maxWidth = a.getDimensionPixelSize(R.styleable.SnackBar_sb_maxWidth, 0);
		value = a.peekValue(R.styleable.SnackBar_sb_height);
		if(value.type == TypedValue.TYPE_INT_DEC)
			mHeight = a.getInteger(R.styleable.SnackBar_sb_height, WRAP_CONTENT);
		else
			mHeight = a.getDimensionPixelSize(R.styleable.SnackBar_sb_height, WRAP_CONTENT);	
		mMarginLeft = a.getDimensionPixelSize(R.styleable.SnackBar_sb_marginLeft, 0);
		mMarginBottom = a.getDimensionPixelSize(R.styleable.SnackBar_sb_marginBottom, 0);
		int textSize = a.getDimensionPixelSize(R.styleable.SnackBar_sb_textSize, 0);
		boolean hasTextColor = a.hasValue(R.styleable.SnackBar_sb_textColor);		
		int textColor = hasTextColor ? a.getColor(R.styleable.SnackBar_sb_textColor, 0xFFFFFFFF) : 0;
		int textAppearance = a.getResourceId(R.styleable.SnackBar_sb_textAppearance, 0);
		boolean singleLine = a.getBoolean(R.styleable.SnackBar_sb_singleLine, true);
		int maxLines = a.getInteger(R.styleable.SnackBar_sb_maxLines, 0);
		int lines = a.getInteger(R.styleable.SnackBar_sb_lines, 0);
		int ellipsize = a.getInteger(R.styleable.SnackBar_sb_ellipsize, 0);
		int actionTextSize = a.getDimensionPixelSize(R.styleable.SnackBar_sb_actionTextSize, 0);
		ColorStateList actionTextColor;
		value = a.peekValue(R.styleable.SnackBar_sb_actionTextColor);
		if(value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
			actionTextColor = ColorStateList.valueOf(a.getColor(R.styleable.SnackBar_sb_actionTextColor, 0xFF000000));
		else
			actionTextColor = a.getColorStateList(R.styleable.SnackBar_sb_actionTextColor);
		int actionTextAppearance = a.getResourceId(R.styleable.SnackBar_sb_actionTextAppearance, 0);
		int actionRipple = a.getResourceId(R.styleable.SnackBar_sb_actionRipple, 0);
		mInAnimationId = a.getResourceId(R.styleable.SnackBar_sb_inAnimation, 0);
		mOutAnimationId = a.getResourceId(R.styleable.SnackBar_sb_outAnimation, 0);
		
		a.recycle();
				
		backgroundColor(backgroundColor)
				.backgroundRadius(backgroundRadius);
		
		padding(horizontalPadding, verticalPadding);
		
		textAppearance(textAppearance);
		if(textSize > 0)
			textSize(textSize);
		if(hasTextColor)
			textColor(textColor);	
		singleLine(singleLine);
		if(maxLines > 0)
			maxLines(maxLines);
		if(lines > 0)
			lines(lines);
		if(minWidth > 0)
			minWidth(minWidth);
		if(maxWidth > 0)
			maxWidth(maxWidth);
		switch (ellipsize) {
			case 1:
				ellipsize(TruncateAt.START);
				break;
			case 2:
				ellipsize(TruncateAt.MIDDLE);
				break;
			case 3:
				ellipsize(TruncateAt.END);
				break;
			case 4:
				ellipsize(TruncateAt.MARQUEE);
				break;
			default:
				ellipsize(TruncateAt.END);
				break;					
		}
		
		if(textAppearance != 0)
			actionTextAppearance(actionTextAppearance);
		actionTextSize(actionTextSize);
		actionTextColor(actionTextColor != null ? actionTextColor : ColorStateList.valueOf(ThemeUtil.colorAccent(context, 0xFF000000)));		
		actionRipple(actionRipple);			
		return this;
	}
	
	public SnackBar text(CharSequence text){
		mText.setText(text);
		return this;
	}
	
	public SnackBar text(int id){
		return text(getContext().getResources().getString(id));
	}
	
	public SnackBar textColor(int color){
		mText.setTextColor(color);
		return this;
	}
	
	public SnackBar textSize(int size){
		mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		return this;
	}
	
	public SnackBar textAppearance(int resId){
		if(resId != 0)
			mText.setTextAppearance(getContext(), resId);
		return this;
	}
	
	public SnackBar ellipsize(TruncateAt at){
		mText.setEllipsize(at);
		return this;
	}
	
	public SnackBar singleLine(boolean b){
		mText.setSingleLine(b);
		return this;
	}
	
	public SnackBar maxLines(int lines){
		mText.setMaxLines(lines);
		return this;
	}
	
	public SnackBar lines(int lines){
		mText.setLines(lines);
		return this;
	}
	
	public SnackBar actionId(int id){
		mActionId = id;		
		return this;
	}
	
	public SnackBar actionText(CharSequence text){
		if(text == null)
			mAction.setVisibility(View.INVISIBLE);
		else{
			mAction.setVisibility(View.VISIBLE);		
			mAction.setText(text);
		}
		return this;
	}
	
	public SnackBar actionText(int id){
		if(id == 0)
			return actionText(null);
		
		return actionText(getContext().getResources().getString(id));
	}
	
	public SnackBar actionTextColor(int color){
		mAction.setTextColor(color);
		return this;
	}
	
	public SnackBar actionTextColor(ColorStateList colors){
		mAction.setTextColor(colors);
		return this;
	}
	
	public SnackBar actionTextAppearance(int resId){
		if(resId != 0)
			mAction.setTextAppearance(getContext(), resId);
		return this;
	}
	
	public SnackBar actionTextSize(int size){
		mAction.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		return this;
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public SnackBar actionRipple(int resId){
		if(resId != 0){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				mAction.setBackground(new RippleDrawable.Builder(getContext(), null, resId).build());
			else
				mAction.setBackgroundDrawable(new RippleDrawable.Builder(getContext(), null, resId).build());
		}	
		return this;
	}
	
	public SnackBar duration(long duration){
		mDuration = duration;
		return this;
	}
	
	public SnackBar backgroundColor(int color){
		mBackground.setColor(color);
		return this;
	}
	
	public SnackBar backgroundRadius(int radius){
		mBackground.setRadius(radius);
		return this;
	}
	
	public SnackBar horizontalPadding(int padding){
		mText.setPadding(padding, mText.getPaddingTop(), padding, mText.getPaddingBottom());	
		mAction.setPadding(padding, mAction.getPaddingTop(), padding, mAction.getPaddingBottom());
		return this;
	}
	
	public SnackBar verticalPadding(int padding){
		mText.setPadding(mText.getPaddingLeft(), padding, mText.getPaddingRight(), padding);	
		mAction.setPadding(mAction.getPaddingLeft(), padding, mAction.getPaddingRight(), padding);
		return this;
	}
	
	public SnackBar padding(int horizontalPadding, int verticalPadding){
		mText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);		
		mAction.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);		
		return this;
	}
	
	public SnackBar width(int width){
		mWidth = width;
		return this;
	}
	
	public SnackBar minWidth(int width){
		mText.setMinWidth(width);
		return this;
	}
	
	public SnackBar maxWidth(int width){
		mText.setMaxWidth(width);
		return this;
	}
	
	public SnackBar height(int height){
		mHeight = height;
		return this;
	}
	
	public SnackBar marginLeft(int size){
		mMarginLeft = size;
		return this;
	}
	
	public SnackBar marginBottom(int size){
		mMarginBottom = size;
		return this;
	}
	
	public SnackBar actionClickListener(OnActionClickListener listener){
		mActionClickListener = listener;
		return this;
	}
	
	public SnackBar stateChangeListener(OnStateChangeListener listener){
		mStateChangeListener = listener;
		return this;
	}
	
	public void show(Activity activity){
		if(mState != STATE_DISMISSED)
			return;
		
		if(getParent() != null && getParent() instanceof ViewGroup)
			((ViewGroup)getParent()).removeView(this);
				
		LayoutParams params = new LayoutParams(mWidth, mHeight);
        params.gravity = Gravity.BOTTOM;
        params.leftMargin = mMarginLeft;
        params.bottomMargin = mMarginBottom;
        
		activity.getWindow().addContentView(this, params);
		
		if(mInAnimationId != 0){
			Animation anim = AnimationUtils.loadAnimation(getContext(), mInAnimationId);
			anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					setState(STATE_SHOWING);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {					
					setState(STATE_SHOWED);
					startTimer();
				}
			});
			startAnimation(anim);
		}
		else{
			setState(STATE_SHOWED);
			startTimer();
		}
	}
	
	private void startTimer(){
		removeCallbacks(mDismissRunnable);
		if(mDuration > 0)
			postDelayed(mDismissRunnable, mDuration);
	}
	
	public void dismiss(){
		if(mState != STATE_SHOWED)
			return;
		
		removeCallbacks(mDismissRunnable);
		
		if(mOutAnimationId != 0){
			Animation anim = AnimationUtils.loadAnimation(getContext(), mOutAnimationId);
			anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					setState(STATE_DISMISSING);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if(getParent() != null && getParent() instanceof ViewGroup)
						((ViewGroup)getParent()).removeView(SnackBar.this);
					
					setState(STATE_DISMISSED);
				}
			});
			startAnimation(anim);
		}
		else{
			if(getParent() != null && getParent() instanceof ViewGroup)
				((ViewGroup)getParent()).removeView(this);
			
			setState(STATE_DISMISSED);
		}		
		
	}
	
	public int getState(){
		return mState;
	}
	
	private void setState(int state){
		if(mState != state){
			int oldState = mState;
			mState = state;
			if(mStateChangeListener != null)
				mStateChangeListener.onStateChange(this, oldState, mState);
		}
	}
	
	private class BackgroundDrawable extends Drawable{

		private int mBackgroundColor;
		private int mBackgroundRadius;
		
		private Paint mPaint;
		private RectF mRect;
		
		public BackgroundDrawable(){
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.FILL);
			
			mRect = new RectF();
		}		
		
		public void setColor(int color){
			if(mBackgroundColor != color){
				mBackgroundColor = color;
				mPaint.setColor(mBackgroundColor);
				invalidateSelf();
			}
		}
		
		public void setRadius(int radius){
			if(mBackgroundRadius != radius){
				mBackgroundRadius = radius;
				invalidateSelf();
			}
		}
		
		@Override
		protected void onBoundsChange(Rect bounds) {
			mRect.set(bounds);
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawRoundRect(mRect, mBackgroundRadius, mBackgroundRadius, mPaint);
		}

		@Override
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		
	}
}
