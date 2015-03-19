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

import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

/**
 * Created by Ret on 3/18/2015.
 */
public class Slider extends View{

    private Paint mPaint;
    private RectF mDrawRect;
    private RectF mTempRect;
    private Path mLeftRailPath;
    private Path mRightRailPath;
    private Path mMarkPath;

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
    private float mThumbFillPercent;

    private ThumbRadiusAnimator mThumbRadiusAnimator;
    private ThumbStrokeAnimator mThumbStrokeAnimator;
    private ThumbMoveAnimator mThumbMoveAnimator;

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
        mThumbPosition = 0.5f;

        mContinuosMode = true;

        mPrimaryColor = 0xFF4557B7;
        mSecondaryColor = 0xFFBFBFBF;
        mStrokeSize = ThemeUtil.dpToPx(context, 2);
        mStrokeCap = Paint.Cap.SQUARE;
        mThumbBorderSize = ThemeUtil.dpToPx(context, 2);
        mThumbRadius = ThemeUtil.dpToPx(context, 9);
        mTravelAnimationDuration = 800;
        mTransformAnimationDuration = 500;
        mInterpolator = new DecelerateInterpolator();

        mThumbFillPercent = mThumbPosition == 0 ? 0 : 1;
        mThumbCurrentRadius = mThumbRadius;
        mThumbFocusRadius = mThumbRadius * 2;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMemoPoint = new PointF();

        mThumbRadiusAnimator = new ThumbRadiusAnimator();
        mThumbStrokeAnimator = new ThumbStrokeAnimator();
        mThumbMoveAnimator = new ThumbMoveAnimator();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawRect = new RectF();
        mTempRect = new RectF();
        mLeftRailPath = new Path();
        mRightRailPath = new Path();
    }

    public int getValue(){
        return (int)getExactValue();
    }

    public float getExactValue(){
        return (mMaxValue - mMinValue) * getPosition() + mMinValue;
    }

    public float getPosition(){
        return mThumbMoveAnimator.isRunning() ? mThumbMoveAnimator.getPosition() : mThumbPosition;
    }

    public void setPosition(float pos, boolean animation){
        if(animation)
            mThumbMoveAnimator.startAnimation(pos);
        else if (mThumbPosition != pos){
            mThumbPosition = pos;
            mThumbFillPercent = mThumbPosition == 0 ? 0 : 1;
            invalidate();
        }
    }

    public void setValue(float value, boolean animation){
        value = Math.min(mMaxValue, Math.max(value, mMinValue));
        setPosition((value - mMinValue) / (mMaxValue - mMinValue), animation);
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
        return mThumbFocusRadius * 4 + getPaddingLeft() + getPaddingRight();
    }

    @Override
    public int getSuggestedMinimumHeight() {
        return mThumbFocusRadius * 2 + getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDrawRect.left = getPaddingLeft();
        mDrawRect.right = w - getPaddingRight();

        int height = mThumbFocusRadius * 2;
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
        float cx = (mDrawRect.width() - mThumbRadius * 2) * mThumbPosition + mDrawRect.left + mThumbRadius;
        float cy = mDrawRect.centerY();

        return x >= cx - mThumbRadius && x <= cx + mThumbRadius && y >= cy - mThumbRadius && y < cy + mThumbRadius;
    }

    private double distance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = isThumbHit(event.getX(), event.getY()) && !mThumbMoveAnimator.isRunning();
                mMemoPoint.set(event.getX(), event.getY());
                if(mIsDragging)
                    mThumbRadiusAnimator.startAnimation(mContinuosMode ? 0 : mThumbFocusRadius);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsDragging) {
                    float offset = (event.getX() - mMemoPoint.x) / (mDrawRect.width() - mThumbRadius * 2);
                    mThumbPosition = Math.min(1f, Math.max(0f, mThumbPosition + offset));
                    mThumbStrokeAnimator.startAnimation(mThumbPosition == 0 ? 0 : 1);
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
                    float position = Math.min(1f, Math.max(0f, (event.getX() - mDrawRect.left - mThumbRadius) / (mDrawRect.width() - mThumbRadius * 2)));
                    mThumbMoveAnimator.startAnimation(position);
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

    private void getRailPath(float x, float y, float radius){
        float halfStroke = mStrokeSize / 2f;

        mLeftRailPath.reset();
        mRightRailPath.reset();

        if(radius < halfStroke || radius < 1f){
            if(mStrokeCap != Paint.Cap.ROUND){
                if(x - radius > mDrawRect.left){
                    mLeftRailPath.moveTo(mDrawRect.left, y - halfStroke);
                    mLeftRailPath.lineTo(x, y - halfStroke);
                    mLeftRailPath.lineTo(x, y + halfStroke);
                    mLeftRailPath.lineTo(mDrawRect.left, y + halfStroke);
                    mLeftRailPath.close();
                }

                if(x + radius < mDrawRect.right){
                    mRightRailPath.moveTo(mDrawRect.right, y + halfStroke);
                    mRightRailPath.lineTo(x, y + halfStroke);
                    mRightRailPath.lineTo(x, y - halfStroke);
                    mRightRailPath.lineTo(mDrawRect.right, y - halfStroke);
                    mRightRailPath.close();
                }
            }
            else{
                if(x - radius > mDrawRect.left){
                    mTempRect.set(mDrawRect.left, y - halfStroke, mDrawRect.left + mStrokeSize, y + halfStroke);
                    mLeftRailPath.arcTo(mTempRect, 90, 180);
                    mLeftRailPath.lineTo(x, y - halfStroke);
                    mLeftRailPath.lineTo(x, y + halfStroke);
                    mLeftRailPath.close();
                }

                if(x + radius < mDrawRect.right){
                    mTempRect.set(mDrawRect.right - mStrokeSize, y - halfStroke, mDrawRect.right, y + halfStroke);
                    mRightRailPath.arcTo(mTempRect, 270, 180);
                    mRightRailPath.lineTo(x, y + halfStroke);
                    mRightRailPath.lineTo(x, y - halfStroke);
                    mRightRailPath.close();
                }
            }
        }
        else{
            if(mStrokeCap != Paint.Cap.ROUND){
                mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
                float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);

                if(x - radius > mDrawRect.left){
                    mLeftRailPath.moveTo(mDrawRect.left, y - halfStroke);
                    mLeftRailPath.arcTo(mTempRect, 180 + angle, -angle * 2);
                    mLeftRailPath.lineTo(mDrawRect.left, y + halfStroke);
                    mLeftRailPath.close();
                }

                if(x + radius < mDrawRect.right){
                    mRightRailPath.moveTo(mDrawRect.right, y - halfStroke);
                    mRightRailPath.arcTo(mTempRect, -angle, angle * 2);
                    mRightRailPath.lineTo(mDrawRect.right, y + halfStroke);
                    mRightRailPath.close();
                }
            }
            else{
                float angle = (float)(Math.asin(halfStroke / (radius - 1f)) / Math.PI * 180);

                if(x - radius > mDrawRect.left){
                    float angle2 = (float)(Math.acos(Math.max(0f, (mDrawRect.left + halfStroke - x + radius) / halfStroke)) / Math.PI * 180);

                    mTempRect.set(mDrawRect.left, y - halfStroke, mDrawRect.left + mStrokeSize, y + halfStroke);
                    mLeftRailPath.arcTo(mTempRect, 180 - angle2, angle2 * 2);

                    mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
                    mLeftRailPath.arcTo(mTempRect, 180 + angle, -angle * 2);
                    mLeftRailPath.close();
                }

                if(x + radius < mDrawRect.right){
                    float angle2 = (float)Math.acos(Math.max(0f, (x + radius - mDrawRect.right + halfStroke) / halfStroke));
                    mRightRailPath.moveTo((float) (mDrawRect.right - halfStroke + Math.cos(angle2) * halfStroke), (float) (y + Math.sin(angle2) * halfStroke));

                    angle2 = (float)(angle2 / Math.PI * 180);
                    mTempRect.set(mDrawRect.right - mStrokeSize, y - halfStroke, mDrawRect.right, y + halfStroke);
                    mRightRailPath.arcTo(mTempRect, angle2, -angle2 * 2);

                    mTempRect.set(x - radius + 1f, y - radius + 1f, x + radius - 1f, y + radius - 1f);
                    mRightRailPath.arcTo(mTempRect, -angle, angle * 2);
                    mRightRailPath.close();
                }
            }
        }
    }

    private Path getMarkPath(Path path, float cx, float cy, float radius, float factor){
        if(path == null)
            path = new Path();
        else
            path.reset();

        float x1 = cx - radius;
        float y1 = cy;
        float x2 = cx + radius;
        float y2 = cy;
        float x3 = cx;
        float y3 = cy + radius;

        float nCx = cx;
        float nCy = cy - radius * factor;

        // calculate first arc
        float angle = (float)(Math.atan2(y2 - nCy, x2 - nCx) * 180 / Math.PI);
        float nRadius = (float)distance(nCx, nCy, x1, y1);
        mTempRect.set(nCx - nRadius, nCy - nRadius, nCx + nRadius, nCy + nRadius);
        path.moveTo(x1, y1);
        path.arcTo(mTempRect, 180 - angle, 180 + angle * 2);

        if(factor > 0.9f)
            path.lineTo(x3, y3);
        else{
            // find center point for second arc
            float x4 = (x2 + x3) / 2;
            float y4 = (y2 + y3) / 2;

            double d1 = distance(x2, y2, x4, y4);
            double d2 = d1 / Math.tan(Math.PI / 2 * (1f - factor) / 2);

            nCx = (float)(x4 - Math.cos(Math.PI / 4) * d2);
            nCy = (float)(y4 - Math.sin(Math.PI / 4) * d2);

            // calculate second arc
            angle = (float)(Math.atan2(y2 - nCy, x2 - nCx) * 180 / Math.PI);
            float angle2 = (float)(Math.atan2(y3 - nCy, x3 - nCx) * 180 / Math.PI);
            nRadius = (float)distance(nCx, nCy, x2, y2);
            mTempRect.set(nCx - nRadius, nCy - nRadius, nCx + nRadius, nCy + nRadius);
            path.arcTo(mTempRect, angle, angle2 - angle);

            // calculate third arc
            nCx = cx * 2 - nCx;
            angle = (float)(Math.atan2(y3 - nCy, x3 - nCx) * 180 / Math.PI);
            angle2 = (float)(Math.atan2(y1 - nCy, x1 - nCx) * 180 / Math.PI);
            mTempRect.set(nCx - nRadius, nCy - nRadius, nCx + nRadius, nCy + nRadius);
            path.arcTo(mTempRect, angle + (float)Math.PI / 4, angle2 - angle);
        }

        path.close();

        return path;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        float x = (mDrawRect.width() - mThumbRadius * 2) * mThumbPosition + mDrawRect.left + mThumbRadius;
        float y = mDrawRect.centerY();

        getRailPath(x, y, mThumbCurrentRadius);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSecondaryColor);
        canvas.drawPath(mRightRailPath, mPaint);
        mPaint.setColor(ColorUtil.getMiddleColor(mSecondaryColor, isEnabled() ? mPrimaryColor : mSecondaryColor, mThumbFillPercent));
        canvas.drawPath(mLeftRailPath, mPaint);

        if(mContinuosMode){
            float factor = 1f - mThumbCurrentRadius / mThumbRadius;

            mMarkPath = getMarkPath(mMarkPath, x, y, mThumbRadius, factor);
            mPaint.setStyle(Paint.Style.FILL);
            int saveCount = canvas.save();
            canvas.translate(0, -mThumbRadius * 2 * factor);
            canvas.drawPath(mMarkPath, mPaint);
            canvas.restoreToCount(saveCount);

            float radius = isEnabled() ? mThumbCurrentRadius : mThumbCurrentRadius - mThumbBorderSize;
            if(radius > 0)
                canvas.drawCircle(x, y, radius, mPaint);
        }
        else{
            float radius = isEnabled() ? mThumbCurrentRadius : mThumbCurrentRadius - mThumbBorderSize;
            if(mThumbFillPercent == 1)
                mPaint.setStyle(Paint.Style.FILL);
            else{
                float strokeWidth = (radius - mThumbBorderSize) * mThumbFillPercent + mThumbBorderSize;
                radius = radius - strokeWidth / 2f;
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(strokeWidth);
            }
            canvas.drawCircle(x, y, radius, mPaint);
        }
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
            if(mThumbCurrentRadius == radius)
                return;

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

    class ThumbStrokeAnimator implements Runnable{

        boolean mRunning = false;
        long mStartTime;
        float mStartFillPercent;
        int mFillPercent;

        public void resetAnimation(){
            mStartTime = SystemClock.uptimeMillis();
            mStartFillPercent = mThumbFillPercent;
        }

        public void startAnimation(int fillPercent) {
            if(mThumbFillPercent == fillPercent)
                return;

            mFillPercent = fillPercent;

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else
                mThumbFillPercent = mFillPercent;

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

            mThumbFillPercent = (mFillPercent - mStartFillPercent) * value + mStartFillPercent;

            if(progress == 1f)
                stopAnimation();

            if(mRunning)
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

            invalidate();
        }

    }

    class ThumbMoveAnimator implements Runnable{

        boolean mRunning = false;
        long mStartTime;
        float mStartFillPercent;
        float mStartRadius;
        float mStartPosition;
        float mPosition;
        float mFillPercent;
        int mDuration;

        public boolean isRunning(){
            return mRunning;
        }

        public float getPosition(){
            return mPosition;
        }

        public void resetAnimation(){
            mStartTime = SystemClock.uptimeMillis();
            mStartPosition = mThumbPosition;
            mStartFillPercent = mThumbFillPercent;
            mStartRadius = mThumbCurrentRadius;
            mFillPercent = mPosition == 0 ? 0 : 1;
            mDuration = (int)(mTravelAnimationDuration * Math.abs(mPosition - mThumbPosition));
        }

        public void startAnimation(float position) {
            if(mThumbPosition == position)
                return;

            mPosition = position;

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else {
                mThumbPosition = position;
                mThumbCurrentRadius = mThumbRadius;
                mThumbFillPercent = mThumbPosition == 0 ? 0 : 1;
            }

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
            float progress = Math.min(1f, (float)(curTime - mStartTime) / mDuration);
            float value = mInterpolator.getInterpolation(progress);

            mThumbPosition = (mPosition - mStartPosition) * value + mStartPosition;
            mThumbFillPercent = (mFillPercent - mStartFillPercent) * value + mStartFillPercent;

            if(progress < 0.2)
                mThumbCurrentRadius = Math.max(mThumbRadius + mThumbBorderSize * progress * 5, mThumbCurrentRadius);
            else if(progress < 0.8)
                mThumbCurrentRadius = mThumbRadius + mThumbBorderSize;
            else
                mThumbCurrentRadius = mThumbRadius + mThumbBorderSize * (5f - progress * 5);

            if(progress == 1f)
                stopAnimation();

            if(mRunning)
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

            invalidate();
        }

    }
}
