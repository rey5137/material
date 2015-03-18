package com.rey.material.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.util.ViewUtil;

/**
 * Created by Rey on 12/26/2014.
 */
public class CircleDrawable extends Drawable implements Animatable {

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

    public CircleDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setInEditMode(boolean b) {
        mInEditMode = b;
    }

    public void setAnimEnable(boolean b) {
        mAnimEnable = b;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidateSelf();
    }

    public void setAnimDuration(int duration) {
        mAnimDuration = duration;
    }

    public void setInterpolator(Interpolator in, Interpolator out) {
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

        if (mVisible != visible) {
            mVisible = visible;
            if (!mInEditMode && mAnimEnable)
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
        if (!mRunning) {
            if (mVisible)
                canvas.drawCircle(mX, mY, mRadius, mPaint);
        } else {
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

    private void resetAnimation() {
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

    private void update() {
        long curTime = SystemClock.uptimeMillis();
        mAnimProgress = Math.min(1f, (float) (curTime - mStartTime) / mAnimDuration);

        if (mAnimProgress == 1f)
            mRunning = false;

        if (isRunning())
            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

        invalidateSelf();
    }

}
