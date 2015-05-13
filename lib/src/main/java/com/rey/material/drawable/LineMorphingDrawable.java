package com.rey.material.drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class LineMorphingDrawable extends Drawable implements Animatable{
	
	private boolean mRunning = false;
	
	private Paint mPaint;
	
	private int mPaddingLeft = 12;
	private int mPaddingTop = 12;
	private int mPaddingRight = 12;
	private int mPaddingBottom = 12;
	
	private RectF mDrawBound;
	
	private int mPrevState;
	private int mCurState;
	private long mStartTime;
	private float mAnimProgress;
	private int mAnimDuration;
	private Interpolator mInterpolator;
	private int mStrokeSize;
	private int mStrokeColor;
	private boolean mClockwise;
	private Paint.Cap mStrokeCap;
	private Paint.Join mStrokeJoin;
    private boolean mIsRtl;
	
	private Path mPath;
	
	private State[] mStates;
	
	private LineMorphingDrawable(State[] states, int curState, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom, int animDuration, Interpolator interpolator, int strokeSize, int strokeColor, Paint.Cap strokeCap, Paint.Join strokeJoin, boolean clockwise, boolean isRtl){
		mStates = states;
		mPaddingLeft = paddingLeft;
		mPaddingTop = paddingTop;
		mPaddingRight = paddingRight;
		mPaddingBottom = paddingBottom;
		
		mAnimDuration = animDuration;
		mInterpolator = interpolator;
		mStrokeSize = strokeSize;
		mStrokeColor = strokeColor;
		mStrokeCap = strokeCap;
		mStrokeJoin = strokeJoin;
		mClockwise = clockwise;
        mIsRtl = isRtl;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(mStrokeCap);
		mPaint.setStrokeJoin(mStrokeJoin);
		mPaint.setColor(mStrokeColor);
		mPaint.setStrokeWidth(mStrokeSize);
		
		mDrawBound = new RectF();
		
		mPath = new Path();			
			
		switchLineState(curState, false);
	}
	
	@Override
	public void draw(Canvas canvas) {
		int restoreCount = canvas.save();		
		float degrees = (mClockwise ? 180 : -180) * ((mPrevState < mCurState ?  0f : 1f) + mAnimProgress);

        if(mIsRtl)
            canvas.scale(-1f, 1f, mDrawBound.centerX(), mDrawBound.centerY());

		canvas.rotate(degrees, mDrawBound.centerX(), mDrawBound.centerY());		
		canvas.drawPath(mPath, mPaint);
		canvas.restoreToCount(restoreCount);
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

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		
		mDrawBound.left = bounds.left + mPaddingLeft;
		mDrawBound.top = bounds.top + mPaddingTop;
		mDrawBound.right = bounds.right - mPaddingRight;
		mDrawBound.bottom = bounds.bottom - mPaddingBottom;
		
		updatePath();
	}
	
	public void switchLineState(int state, boolean animation){
		if(mCurState != state){
			mPrevState = mCurState;
			mCurState = state;
			if(animation)
				start();
			else{
				mAnimProgress = 1f;
				updatePath();
			}
		}
		else if(!animation){
			mAnimProgress = 1f;
			updatePath();
		}
	}
	
	public boolean setLineState(int state, float progress){
		if(mCurState != state){
			mPrevState = mCurState;
			mCurState = state;
			mAnimProgress = progress;
			updatePath();
			return true;
		}
		else if(mAnimProgress != progress){				
			mAnimProgress = progress;
			updatePath();
			return true;
		}
		
		return false;
	}
	
	public int getLineState(){
		return mCurState;
	}
	
	public int getLineStateCount(){
		return mStates == null ? 0 : mStates.length;
	}
	
	public float getAnimProgress(){
		return mAnimProgress;
	}
	
	private void updatePath(){
		mPath.reset();
		
		if(mStates == null)
			return;
		
		if(mAnimProgress == 0f || (mStates[mPrevState].links != null && mAnimProgress < 0.05f))
			updatePathWithState(mPath, mStates[mPrevState]);
		else if(mAnimProgress == 1f || (mStates[mCurState].links != null && mAnimProgress >0.95f))
			updatePathWithState(mPath, mStates[mCurState]);
		else
			updatePathBetweenStates(mPath, mStates[mPrevState], mStates[mCurState], mInterpolator.getInterpolation(mAnimProgress));
			
		invalidateSelf();
	}
	
	private void updatePathWithState(Path path, State state){
		if(state.links != null){
			for(int i = 0; i < state.links.length; i+= 2){
				int index1 = state.links[i] * 4;
				int index2 = state.links[i + 1] * 4;
				
				float x1 = getX(state.points[index1]);
				float y1 = getY(state.points[index1 + 1]);
				float x2 = getX(state.points[index1 + 2]);
				float y2 = getY(state.points[index1 + 3]);
				
				float x3 = getX(state.points[index2]);
				float y3 = getY(state.points[index2 + 1]);
				float x4 = getX(state.points[index2 + 2]);
				float y4 = getY(state.points[index2 + 3]);
				
				if(x1 == x3 && y1 == y3){
					path.moveTo(x2, y2);
					path.lineTo(x1, y1);
					path.lineTo(x4, y4);
				}
				else if(x1 == x4 && y1 == y4){
					path.moveTo(x2, y2);
					path.lineTo(x1, y1);
					path.lineTo(x3, y3);
				}
				else if(x2 == x3 && y2 == y3){
					path.moveTo(x1, y1);
					path.lineTo(x2, y2);
					path.lineTo(x4, y4);
				}
				else{
					path.moveTo(x1, y1);
					path.lineTo(x2, y2);
					path.lineTo(x3, y3);
				}
			}
			
			for(int i = 0, count = state.points.length / 4; i < count; i ++){
				boolean exist = false;
				for(int j = 0; j < state.links.length; j++)
					if(state.links[j] == i){
						exist = true;
						break;
					}
				
				if(exist)
					continue;
				
				int index = i * 4;
				
				path.moveTo(getX(state.points[index]), getY(state.points[index + 1]));
				path.lineTo(getX(state.points[index + 2]), getY(state.points[index + 3]));
			}
		}
		else{
			for(int i = 0, count = state.points.length / 4; i < count; i ++){				
				int index = i * 4;
				
				path.moveTo(getX(state.points[index]), getY(state.points[index + 1]));
				path.lineTo(getX(state.points[index + 2]), getY(state.points[index + 3]));
			}
		}
	}
	
	private void updatePathBetweenStates(Path path, State prev, State cur, float progress){
		int count = Math.max(prev.points.length, cur.points.length) / 4;
		
		for(int i = 0; i < count; i++){
			int index = i * 4;
			
			float x1;
			float y1;
			float x2;
			float y2;			
			if(index >= prev.points.length){
				x1 = 0.5f;
				y1 = 0.5f;
				x2 = 0.5f;
				y2 = 0.5f;
			}
			else{
				x1 = prev.points[index];
				y1 = prev.points[index + 1];
				x2 = prev.points[index + 2];
				y2 = prev.points[index + 3];
			}
			
			float x3;
			float y3;
			float x4;
			float y4;			
			if(index >= cur.points.length){
				x3 = 0.5f;
				y3 = 0.5f;
				x4 = 0.5f;
				y4 = 0.5f;
			}
			else{
				x3 = cur.points[index];
				y3 = cur.points[index + 1];
				x4 = cur.points[index + 2];
				y4 = cur.points[index + 3];
			}
			
			mPath.moveTo(getX(x1 + (x3 - x1) * progress), getY(y1 + (y3 - y1) * progress));
			mPath.lineTo(getX(x2 + (x4 - x2) * progress), getY(y2 + (y4 - y2) * progress));
		}
	}	
	
	private float getX(float value){
		return mDrawBound.left + mDrawBound.width() * value;
	}
	
	private float getY(float value){
		return mDrawBound.top + mDrawBound.height() * value;
	}
	
	//Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/
		
	private void resetAnimation(){	
		mStartTime = SystemClock.uptimeMillis();
		mAnimProgress = 0f;
	}
	
	@Override
	public void start() {
		resetAnimation();
		
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	    invalidateSelf();  
	}

	@Override
	public void stop() {
		if(!isRunning()) 
			return;
				
		mRunning = false;
		unscheduleSelf(mUpdater);
		invalidateSelf();
	}
	
	@Override
	public boolean isRunning() {
		return mRunning;
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		mRunning = true;
	    super.scheduleSelf(what, when);
	}
	
	private final Runnable mUpdater = new Runnable() {

	    @Override
	    public void run() {
	    	update();
	    }
		    
	};
		
	private void update(){
		long curTime = SystemClock.uptimeMillis();
		float value = Math.min(1f, (float)(curTime - mStartTime) / mAnimDuration);	
		
		if(value == 1f){
			setLineState(mCurState, 1f);
			mRunning = false;
		}
		else
			setLineState(mCurState, mInterpolator.getInterpolation(value));
				
    	if(isRunning())
    		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	}
	
	public static class State{
		float[] points;
		int[] links;
		
		public State(){}
		
		public State(float[] points, int[] links){
			this.points = points;
			this.links = links;
		}
	}
	
	public static class Builder{
		private int mCurState;
		
		private int mPaddingLeft;
		private int mPaddingTop;
		private int mPaddingRight;
		private int mPaddingBottom;
		
		private int mAnimDuration;
		private Interpolator mInterpolator;
		private int mStrokeSize;
		private int mStrokeColor;
		private boolean mClockwise;
		private Paint.Cap mStrokeCap;
		private Paint.Join mStrokeJoin;
        private boolean mIsRtl;
		
		private State[] mStates;
		
		private static final String TAG_STATE_LIST = "state-list";
		private static final String TAG_STATE = "state";
		private static final String TAG_POINTS = "points";
		private static final String TAG_LINKS = "links";
		private static final String TAG_ITEM = "item";
		
		public Builder(){}

        public Builder(Context context, int defStyleRes){
            this(context, null, 0, defStyleRes);
        }

		public Builder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineMorphingDrawable, defStyleAttr, defStyleRes);
			int resId;
			
			if((resId = a.getResourceId(R.styleable.LineMorphingDrawable_lmd_state, 0)) != 0)
				states(readStates(context, resId));			
			curState(a.getInteger(R.styleable.LineMorphingDrawable_lmd_curState, 0));			
			padding(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_padding, 0));
			paddingLeft(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_paddingLeft, mPaddingLeft));
			paddingTop(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_paddingTop, mPaddingTop));
			paddingRight(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_paddingRight, mPaddingRight));
			paddingBottom(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_paddingBottom, mPaddingBottom));
			animDuration(a.getInteger(R.styleable.LineMorphingDrawable_lmd_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
			if((resId = a.getResourceId(R.styleable.LineMorphingDrawable_lmd_interpolator, 0)) != 0)
				interpolator(AnimationUtils.loadInterpolator(context, resId));
			strokeSize(a.getDimensionPixelSize(R.styleable.LineMorphingDrawable_lmd_strokeSize, ThemeUtil.dpToPx(context, 3)));
			strokeColor(a.getColor(R.styleable.LineMorphingDrawable_lmd_strokeColor, 0xFFFFFFFF));
			int cap = a.getInteger(R.styleable.LineMorphingDrawable_lmd_strokeCap, 0);
			if(cap == 0)
				strokeCap(Paint.Cap.BUTT);
			else if(cap == 1)
				strokeCap(Paint.Cap.ROUND);
			else
				strokeCap(Paint.Cap.SQUARE);
			int join = a.getInteger(R.styleable.LineMorphingDrawable_lmd_strokeJoin, 0);
			if(join == 0)
				strokeJoin(Paint.Join.MITER);
			else if(join == 1)
				strokeJoin(Paint.Join.ROUND);
			else
				strokeJoin(Paint.Join.BEVEL);
			clockwise(a.getBoolean(R.styleable.LineMorphingDrawable_lmd_clockwise, true));

            int direction = a.getInteger(R.styleable.LineMorphingDrawable_lmd_layoutDirection, View.LAYOUT_DIRECTION_LTR);
            if(direction == View.LAYOUT_DIRECTION_LOCALE)
                rtl(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL);
            else
                rtl(direction == View.LAYOUT_DIRECTION_RTL);
			
			a.recycle();
		}
		
		private State[] readStates(Context context, int id){
			XmlResourceParser parser = null;
			List<State> states = new ArrayList<>();
			
			try {
				parser = context.getResources().getXml(id);
				
				int eventType = parser.getEventType();
		        String tagName;
		        boolean lookingForEndOfUnknownTag = false;
		        String unknownTagName = null;

		        // This loop will skip to the state-list start tag
		        do {
		            if (eventType == XmlPullParser.START_TAG) {
		                tagName = parser.getName();
		                if (tagName.equals(TAG_STATE_LIST)) {
		                    eventType = parser.next();
		                    break;
		                }                
		                throw new RuntimeException("Expecting menu, got " + tagName);
		            }
		            eventType = parser.next();
		        } while (eventType != XmlPullParser.END_DOCUMENT);
		        
		        boolean reachedEndOfStateList = false;		        
		        State state = null;
		        List<String> array = new ArrayList<>();
		        StringBuilder currentValue = new StringBuilder();
		        
		        while (!reachedEndOfStateList) {
		            switch (eventType) {
		                case XmlPullParser.START_TAG:
		                    if (lookingForEndOfUnknownTag) 
		                        break;   
		                    
		                    tagName = parser.getName();
                            switch (tagName) {
                                case TAG_STATE:
                                    state = new State();
                                    break;
                                case TAG_POINTS:
                                case TAG_LINKS:
                                    array.clear();
                                    break;
                                case TAG_ITEM:
                                    currentValue.delete(0, currentValue.length());
                                    break;
                                default:
                                    lookingForEndOfUnknownTag = true;
                                    unknownTagName = tagName;
                                    break;
                            }
		                    break;
		                    
		                case XmlPullParser.END_TAG:
		                    tagName = parser.getName();
		                    
		                    if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
		                        lookingForEndOfUnknownTag = false;
		                        unknownTagName = null;
		                    }

                            switch (tagName) {
                                case TAG_STATE_LIST:
                                    reachedEndOfStateList = true;
                                    break;
                                case TAG_STATE:
                                    states.add(state);
                                    break;
                                case TAG_POINTS:
                                    state.points = new float[array.size()];
                                    for (int i = 0; i < state.points.length; i++)
                                        state.points[i] = Float.parseFloat(array.get(i));
                                    break;
                                case TAG_LINKS:
                                    state.links = new int[array.size()];
                                    for (int i = 0; i < state.links.length; i++)
                                        state.links[i] = Integer.parseInt(array.get(i));
                                    break;
                                case TAG_ITEM:
                                    array.add(currentValue.toString());
                                    break;
                            }
		                    
		                    break;
		                    
		                case XmlPullParser.TEXT:
		                	currentValue.append(parser.getText());
		                	break;
		                    
		                case XmlPullParser.END_DOCUMENT:
                            reachedEndOfStateList = true;
                            break;
		            }
		            
		            eventType = parser.next();
		        }
		        
		    } 
		    catch (Exception e) {} 
		    finally {
		    	if(parser != null) 
		    		parser.close();
		    }
			
			if(states.isEmpty())
				return null;
			
			return states.toArray(new State[states.size()]);
		}
		
		public LineMorphingDrawable build(){
			if(mStrokeCap == null)
				mStrokeCap = Paint.Cap.BUTT;
						
			if(mStrokeJoin == null)
				mStrokeJoin = Paint.Join.MITER;
			
			if(mInterpolator == null)
				mInterpolator = new AccelerateInterpolator();
							
			return new LineMorphingDrawable(mStates, mCurState, mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom, mAnimDuration, mInterpolator, mStrokeSize, mStrokeColor, mStrokeCap, mStrokeJoin, mClockwise, mIsRtl);
		}
		
		public Builder states(State... states){			
			mStates = states;
			return this;
		}
		
		public Builder curState(int state){
			mCurState = state;
			return this;
		}
		
		public Builder padding(int padding){
			mPaddingLeft = padding;
			mPaddingTop = padding;
			mPaddingRight = padding;
			mPaddingBottom = padding;
			return this;
		}
		
		public Builder paddingLeft(int padding){
			mPaddingLeft = padding;
			return this;
		}
		
		public Builder paddingTop(int padding){
			mPaddingTop = padding;
			return this;
		}
		
		public Builder paddingRight(int padding){
			mPaddingRight = padding;
			return this;
		}
		
		public Builder paddingBottom(int padding){
			mPaddingBottom = padding;
			return this;
		}
		
		public Builder animDuration(int duration){
			mAnimDuration = duration;
			return this;
		}
		
		public Builder interpolator(Interpolator interpolator){
			mInterpolator = interpolator;
			return this;
		}
		
		public Builder strokeSize(int size){
			mStrokeSize = size;
			return this;
		}
		
		public Builder strokeColor(int strokeColor){
			mStrokeColor = strokeColor;
			return this;
		}
		
		public Builder strokeCap(Paint.Cap cap){
			mStrokeCap = cap;
			return this;
		}
				
		public Builder strokeJoin(Paint.Join join){
			mStrokeJoin = join;
			return this;
		}
		
		public Builder clockwise(boolean clockwise){
			mClockwise = clockwise;
			return this;
		}

        public Builder rtl(boolean rtl){
            mIsRtl = rtl;
            return this;
        }
		
	}
}
