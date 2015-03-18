package com.rey.material.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

/**
 * Created by Ret on 3/18/2015.
 */
public class Slider extends View{

    private Paint mPaint;
    private RectF mDrawRect;
    private RectF mTempRect;
    private Path mLeftPath;
    private Path mRightPath;

    private int mMinValue;
    private int mMaxValue;

    private boolean mContinuosMode;

    private int mPrimaryColor;
    private int mSecondaryColor;
    private int mStrokeSize;
    private Paint.Cap mStrokeCap;
    private int mThumbBorderSize;
    private int mThumbRadius;
    private int mThumbFocusRadius;
    private float mThumbPosition;
    private int mGravity = Gravity.CENTER;
    private int mTravelAnimationDuration;
    private int mTransformAnimationDuration;
    private Interpolator mInterpolator;

    private int mTouchSlop;
    private PointF mMemoPoint;
    private boolean mIsDragging;
    private float mThumbCurrentRadius;

    private ThumbRadiusAnimator mThumbRadiusAnimator;

    public Slider(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){

        mMinValue = 0;
        mMaxValue = 100;
        mThumbPosition = 0f;

        mContinuosMode = true;

        mPrimaryColor = 0xFF4557B7;
        mSecondaryColor = 0xFFBFBFBF;
        mStrokeSize = ThemeUtil.dpToPx(context, 2);
        mStrokeCap = Paint.Cap.SQUARE;
        mThumbBorderSize = ThemeUtil.dpToPx(context, 2);
        mThumbRadius = ThemeUtil.dpToPx(context, 9);
        mTravelAnimationDuration = 1000;
        mTransformAnimationDuration = 500;
        mInterpolator = new DecelerateInterpolator();

        mThumbCurrentRadius = mThumbRadius;
        mThumbFocusRadius = mThumbRadius * 2;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMemoPoint = new PointF();

        mThumbRadiusAnimator = new ThumbRadiusAnimator();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawRect = new RectF();
        mTempRect = new RectF();
        mLeftPath = new Path();
        mRightPath = new Path();
    }

    public int getValue(){
        return (int)getExactValue();
    }

    public float getExactValue(){
        return (mMaxValue - mMinValue) * mThumbPosition + mMinValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                widthSize = getSuggestedMinimumWidth();
                break;
            case MeasureSpec.AT_MOST:
                widthSize = Math.min(widthSize, getSuggestedMinimumWidth());
                break;
        }

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                heightSize = getSuggestedMinimumHeight();
                break;
            case MeasureSpec.AT_MOST:
                heightSize = Math.min(heightSize, getSuggestedMinimumHeight());
                break;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public int getSuggestedMinimumWidth() {
        return (mThumbFocusRadius * 2 + mThumbBorderSize) * 2 + getPaddingLeft() + getPaddingRight();
    }

    @Override
    public int getSuggestedMinimumHeight() {
        return mThumbFocusRadius * 2 + mThumbBorderSize + getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDrawRect.left = getPaddingLeft();
        mDrawRect.right = w - getPaddingRight();

        int height = mThumbFocusRadius * 2 + mThumbBorderSize;
        int align = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

        switch (align) {
            case Gravity.TOP:
                mDrawRect.top = getPaddingTop();
                mDrawRect.bottom = mDrawRect.top + height;
                break;
            case Gravity.BOTTOM:
                mDrawRect.bottom = h - getPaddingBottom();
                mDrawRect.top = mDrawRect.bottom - height;
                break;
            default:
                mDrawRect.top = (h - height) / 2f;
                mDrawRect.bottom = mDrawRect.top + height;
                break;
        }
    }

    private boolean isThumbHit(float x, float y){
        float outerRadius = mThumbRadius + mThumbBorderSize / 2f;
        float cx = (mDrawRect.width() - outerRadius * 2) * mThumbPosition + mDrawRect.left + outerRadius;
        float cy = mDrawRect.centerY();

        return x >= cx - outerRadius && x <= cx + outerRadius && y >= cy - outerRadius && y < cy + outerRadius;
    }

    private double distance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = isThumbHit(event.getX(), event.getY());
                mMemoPoint.set(event.getX(), event.getY());
                if(mIsDragging)
                    mThumbRadiusAnimator.startAnimation(mThumbFocusRadius);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsDragging) {
                    float offset = (event.getX() - mMemoPoint.x) / (mDrawRect.width() - mThumbRadius * 2 - mThumbBorderSize);
                    mThumbPosition = Math.min(1f, Math.max(0f, mThumbPosition + offset));
                    mMemoPoint.x = event.getX();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mIsDragging) {
                    mThumbRadiusAnimator.startAnimation(mThumbRadius);
                    mIsDragging = false;
                }
                else if(distance(mMemoPoint.x, mMemoPoint.y, event.getX(), event.getY()) <= mTouchSlop){
                    mThumbPosition = Math.min(1f, Math.max(0f, (event.getX() - mDrawRect.left - mThumbRadius - mThumbBorderSize / 2f) / (mDrawRect.width() - mThumbRadius * 2 - mThumbBorderSize)));
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                if(mIsDragging) {
                    mThumbRadiusAnimator.startAnimation(mThumbRadius);
                    mIsDragging = false;
                }
                break;
        }

        return true;
    }

    private void getPath(float x, float y, float radius){
        float halfStroke = mStrokeSize / 2f;

        mLeftPath.reset();
        mRightPath.reset();

        if(mStrokeCap != Paint.Cap.ROUND){
            mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
            float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);

            if(x - radius > mDrawRect.left){
                mLeftPath.moveTo(mDrawRect.left, y - halfStroke);
                mLeftPath.arcTo(mTempRect, 180 + angle, -angle * 2);
                mLeftPath.lineTo(mDrawRect.left, y + halfStroke);
                mLeftPath.close();
            }

            if(x + radius < mDrawRect.right){
                mRightPath.moveTo(mDrawRect.right, y - halfStroke);
                mRightPath.arcTo(mTempRect, -angle, angle * 2);
                mRightPath.lineTo(mDrawRect.right, y + halfStroke);
                mRightPath.close();
            }
        }
        else{
            float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);

            if(x - radius > mDrawRect.left){
                float angle2 = (float)(Math.acos(Math.max(0f, (mDrawRect.left + halfStroke - x + radius) / halfStroke)) / Math.PI * 180);

                mTempRect.set(mDrawRect.left, y - halfStroke, mDrawRect.left + mStrokeSize, y + halfStroke);
                mLeftPath.arcTo(mTempRect, 180 - angle2, angle2 * 2);

                mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
                mLeftPath.arcTo(mTempRect, 180 + angle, -angle * 2);
                mLeftPath.close();
            }

            if(x + radius < mDrawRect.right){
                float angle2 = (float)Math.acos(Math.max(0f, (x + radius - mDrawRect.right + halfStroke) / halfStroke));
                mRightPath.moveTo((float) (mDrawRect.right - halfStroke + Math.cos(angle2) * halfStroke), (float) (y + Math.sin(angle2) * halfStroke));

                angle2 = (float)(angle2 / Math.PI * 180);
                mTempRect.set(mDrawRect.right - mStrokeSize, y - halfStroke, mDrawRect.right, y + halfStroke);
                mRightPath.arcTo(mTempRect, angle2, -angle2 * 2);

                mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
                mRightPath.arcTo(mTempRect, -angle, angle * 2);
                mRightPath.close();
            }
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        float outerBaseRadius = mThumbRadius + mThumbBorderSize / 2f;
        float x = (mDrawRect.width() - outerBaseRadius * 2) * mThumbPosition + mDrawRect.left + outerBaseRadius;
        float y = mDrawRect.centerY();

        float outerRadius = mThumbCurrentRadius + mThumbBorderSize / 2f;

        getPath(x, y, outerRadius);
        mPaint.setColor(isEnabled() ? mPrimaryColor : mSecondaryColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mLeftPath, mPaint);
        mPaint.setColor(mSecondaryColor);
        canvas.drawPath(mRightPath, mPaint);

        float radius;

        if(mThumbPosition == 0f){
            radius = isEnabled() ? mThumbCurrentRadius : mThumbCurrentRadius - mThumbBorderSize;
            mPaint.setColor(mSecondaryColor);
            mPaint.setStrokeWidth(mThumbBorderSize);
            mPaint.setStyle(Paint.Style.STROKE);
        }
        else{
            radius = isEnabled() ? outerRadius : outerRadius - mThumbBorderSize;
            mPaint.setColor(isEnabled() ? mPrimaryColor : mSecondaryColor);
            mPaint.setStyle(Paint.Style.FILL);
        }

        canvas.drawCircle(x, y, radius, mPaint);
    }

    class ThumbRadiusAnimator implements Runnable{

        boolean mRunning = false;
        long mStartTime;
        float mStartRadius;
        int mRadius;

        public void resetAnimation(){
            mStartTime = SystemClock.uptimeMillis();
            mStartRadius = mThumbCurrentRadius;
        }

        public void startAnimation(int radius) {
            mRadius = radius;

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else
                mThumbCurrentRadius = mRadius;

            invalidate();
        }

        public void stopAnimation() {
            mRunning = false;
            getHandler().removeCallbacks(this);
            invalidate();
        }

        @Override
        public void run() {
            long curTime = SystemClock.uptimeMillis();
            float progress = Math.min(1f, (float)(curTime - mStartTime) / mTransformAnimationDuration);
            float value = mInterpolator.getInterpolation(progress);

            mThumbCurrentRadius = (mRadius - mStartRadius) * value + mStartRadius;

            if(progress == 1f)
                stopAnimation();

            if(mRunning)
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

            invalidate();
        }

    }

}
