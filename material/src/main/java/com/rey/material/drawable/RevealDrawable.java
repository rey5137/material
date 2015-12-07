package com.rey.material.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.util.ColorUtil;
import com.rey.material.util.ViewUtil;

public class RevealDrawable extends Drawable implements Animatable {
	
	private boolean mRunning = false;
	private long mStartTime;
	private float mAnimProgress;
		
	private Paint mShaderPaint;
	private Paint mFillPaint;
	private int mCurColor;
	private RadialGradient mShader;
	private Matrix mMatrix;
	private RectF mRect;
	private float mMaxRadius;
	
	private ColorChangeTask[] mTasks;
	private int mCurTask;
	
	private boolean mCurColorTransparent;
	private boolean mNextColorTransparent;
	
	private static final float[] GRADIENT_STOPS = new float[]{0f, 0.99f, 1f};
	private static final float GRADIENT_RADIUS = 16;
		
	public RevealDrawable(int color){
		mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mShaderPaint.setStyle(Paint.Style.FILL);
		
		mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFillPaint.setStyle(Paint.Style.FILL);	
		
		mCurColor = color;
		
		mRect = new RectF();
		
		mMatrix = new Matrix();
	}
	
	public int getCurColor(){
		return mCurColor;
	}
	
	public void setCurColor(int color){
		if(mCurColor != color){
			mCurColor = color;
			mCurColorTransparent = Color.alpha(mCurColor) == 0;
			invalidateSelf();			
		}
	}
	
	private float getMaxRadius(float x, float y, Rect bounds){
		float x1 = x < bounds.centerX() ? bounds.right : bounds.left;
		float y1 = y < bounds.centerY() ? bounds.bottom : bounds.top;
		
		return (float)Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2));
	}
		
	private RadialGradient getShader(ColorChangeTask task){
		if(mShader == null){
			if(task.isOut){
				int color_middle = ColorUtil.getColor(mCurColor, 0f);
				mShader = new RadialGradient(task.x, task.y, GRADIENT_RADIUS, new int[]{0, color_middle, mCurColor}, GRADIENT_STOPS, Shader.TileMode.CLAMP);
			}
			else{
				int color_middle = ColorUtil.getColor(task.color, 0f);
				mShader = new RadialGradient(task.x, task.y, GRADIENT_RADIUS, new int[]{0, color_middle, task.color}, GRADIENT_STOPS, Shader.TileMode.CLAMP);
			}
		}
		
		return mShader;
	}
	
	private void fillCanvas(Canvas canvas, int color, boolean transparent){
		if(transparent)
			return;
		
		mFillPaint.setColor(color);
		canvas.drawRect(getBounds(), mFillPaint);
	}
	
	private void fillCanvasWithHole(Canvas canvas, ColorChangeTask task, float radius, boolean transparent){
		if(transparent)
			return;
		
		float scale = radius / GRADIENT_RADIUS;
		
		mMatrix.reset();
		mMatrix.postScale(scale, scale, task.x, task.y);
		RadialGradient shader = getShader(task);
		shader.setLocalMatrix(mMatrix);
		mShaderPaint.setShader(shader);
		canvas.drawRect(getBounds(), mShaderPaint);
	}
	
	private void fillCircle(Canvas canvas, float x, float y, float radius, int color, boolean transparent){
		if(transparent)
			return;
		
		mFillPaint.setColor(color);
		mRect.set(x - radius, y - radius, x + radius, y + radius);
		canvas.drawOval(mRect, mFillPaint);
	}
	
	@Override
	public void draw(Canvas canvas) {		
		if(!isRunning())
			fillCanvas(canvas, mCurColor, mCurColorTransparent);		
		else{
			ColorChangeTask task = mTasks[mCurTask];
			
			if(mAnimProgress == 0f)
				fillCanvas(canvas, mCurColor, mCurColorTransparent);			
			else if(mAnimProgress == 1f)
				fillCanvas(canvas, task.color, mNextColorTransparent);			
			else if(task.isOut){
				float radius = mMaxRadius * task.interpolator.getInterpolation(mAnimProgress);
				
				if(Color.alpha(task.color) == 255)
					fillCanvas(canvas, mCurColor, mCurColorTransparent);
				else
					fillCanvasWithHole(canvas, task, radius, mCurColorTransparent);
				
				fillCircle(canvas, task.x, task.y, radius, task.color, mNextColorTransparent);
			}
			else{
				float radius = mMaxRadius * task.interpolator.getInterpolation(mAnimProgress);
				
				if(Color.alpha(mCurColor) == 255)
					fillCanvas(canvas, task.color, mNextColorTransparent);
				else
					fillCanvasWithHole(canvas, task, radius, mNextColorTransparent);
				
				fillCircle(canvas, task.x, task.y, radius, mCurColor, mCurColorTransparent);
			}
		}
	}
	
	public void changeColor(int color, int duration, Interpolator interpolator, float x, float y, boolean out){
		changeColor(new ColorChangeTask(color, duration, interpolator, x, y, out));
	}
	
	public void changeColor(ColorChangeTask... tasks){
        synchronized (RevealDrawable.class){
            if(!isRunning()){
                for(int i = 0; i < tasks.length; i++)
                    if(tasks[i].color != mCurColor){
                        mCurTask = i;
                        mTasks = tasks;
                        start();
                        break;
                    }
            }
            else{
                int curLength = mTasks.length - mCurTask;
                ColorChangeTask[] newTasks = new ColorChangeTask[curLength + tasks.length];
                System.arraycopy(mTasks, mCurTask, newTasks, 0, curLength);
                System.arraycopy(tasks, 0, newTasks, curLength, tasks.length);
                mTasks = newTasks;
                mCurTask = 0;
            }
        }
	}

	@Override
	public void setAlpha(int alpha) {
		mShaderPaint.setAlpha(alpha);
		mFillPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mShaderPaint.setColorFilter(cf);
		mFillPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
		
	private void resetAnimation(){	
		mStartTime = SystemClock.uptimeMillis();
		mAnimProgress = 0f;
		mCurColorTransparent = Color.alpha(mCurColor) == 0;
		mNextColorTransparent = Color.alpha(mTasks[mCurTask].color) == 0;
		mMaxRadius = getMaxRadius(mTasks[mCurTask].x, mTasks[mCurTask].y, getBounds());
		mShader = null;
	}
	
	@Override
	public void start() {
		if(isRunning()) 
			return;
						
		resetAnimation();
		
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	    invalidateSelf();  
	}

	@Override
	public void stop() {
		if(!isRunning()) 
			return;
				
		mTasks = null;
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
        synchronized (RevealDrawable.class) {
            mAnimProgress = Math.min(1f, (float) (curTime - mStartTime) / mTasks[mCurTask].duration);

            if (mAnimProgress == 1f) {
                setCurColor(mTasks[mCurTask].color);
                for (mCurTask = mCurTask + 1; mCurTask < mTasks.length; mCurTask++)
                    if (mTasks[mCurTask].color != mCurColor) {
                        resetAnimation();
                        break;
                    }

                if (mCurTask == mTasks.length)
                    stop();
            }
        }
		
		invalidateSelf();
		
    	if(isRunning())
    		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
	}
	
	public static class ColorChangeTask{
		public final int color;
		public final int duration;
		public final Interpolator interpolator;
		public final float x;
		public final float y;
		public final boolean isOut;
		
		public ColorChangeTask(int color, int duration, Interpolator interpolator, float x, float y, boolean out){
			this.color = color;
			this.duration = duration;
			this.interpolator = interpolator == null ? new DecelerateInterpolator() : interpolator;
			this.x = x;
			this.y = y;
			this.isOut = out;
		}
	}

}