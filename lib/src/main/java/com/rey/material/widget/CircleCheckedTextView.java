package com.rey.material.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

/**
 * Created by Rey on 12/23/2014.
 */
public class CircleCheckedTextView extends android.widget.CheckedTextView{

    private CircleDrawable mBackground;

    public CircleCheckedTextView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public CircleCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public CircleCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public CircleCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        setGravity(Gravity.CENTER);
        setPadding(0, 0, 0, 0);
        setClickable(true);

        mBackground = new CircleDrawable();
        mBackground.setColor(ThemeUtil.colorPrimary(context, 0xFF000000));
        mBackground.setInEditMode(isInEditMode());
        mBackground.setAnimEnable(false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setBackground(mBackground);
        else
            setBackgroundDrawable(mBackground);
        mBackground.setAnimEnable(true);
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackground.setColor(color);
    }

    public void setAnimDuration(int duration){
        mBackground.setAnimDuration(duration);
    }

    public void setInterpolator(Interpolator in, Interpolator out){
        mBackground.setInterpolator(in, out);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(getMeasuredWidth() != getMeasuredHeight()) {
            int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
            int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            super.onMeasure(spec, spec);
        }
    }

    private class CircleDrawable extends Drawable implements Animatable{

        private boolean mRunning = false;
        private long mStartTime;
        private float mAnimProgress;
        private int mAnimDuration = 1000;
        private Interpolator mInInterpolator = new DecelerateInterpolator();
        private Interpolator mOutInterpolator = new DecelerateInterpolator();

        private Paint mPaint;

        private float mX;
        private float mY;
        private float mRadius;

        private boolean mVisible;
        private boolean mInEditMode = false;
        private boolean mAnimEnable = true;

        public CircleDrawable(){
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
        }

        public void setInEditMode(boolean b){
            mInEditMode = b;
        }

        public void setAnimEnable(boolean b){
            mAnimEnable = b;
        }

        public void setColor(int color){
            mPaint.setColor(color);
            invalidateSelf();
        }

        public void setAnimDuration(int duration){
            mAnimDuration = duration;
        }

        public void setInterpolator(Interpolator in, Interpolator out){
            mInInterpolator = in;
            mOutInterpolator = out;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        protected boolean onStateChange(int[] state) {
            boolean visible = ViewUtil.hasState(state, android.R.attr.state_checked) || ViewUtil.hasState(state, android.R.attr.state_pressed);

            if(mVisible != visible){
                mVisible = visible;
                if(!mInEditMode && mAnimEnable)
                    start();
                return true;
            }

            return false;
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            mX = bounds.exactCenterX();
            mY = bounds.exactCenterY();
            mRadius = Math.min(bounds.width(), bounds.height()) / 2f;
        }

        @Override
        public void draw(Canvas canvas) {
            if(!mRunning){
                if(mVisible)
                    canvas.drawCircle(mX, mY, mRadius, mPaint);
            }
            else{
                float radius = mVisible ? mInInterpolator.getInterpolation(mAnimProgress) * mRadius : (1f - mOutInterpolator.getInterpolation(mAnimProgress)) * mRadius;
                canvas.drawCircle(mX, mY, radius, mPaint);
            }
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
            mAnimProgress = Math.min(1f, (float)(curTime - mStartTime) / mAnimDuration);

            if(mAnimProgress == 1f)
                mRunning = false;

            if(isRunning())
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

            invalidateSelf();
        }

    }

}
