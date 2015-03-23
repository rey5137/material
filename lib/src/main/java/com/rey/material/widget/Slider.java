package com.rey.material.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
    private int mStepValue;

    private boolean mContinuousMode;

    private int mPrimaryColor;
    private int mSecondaryColor;
    private int mStrokeSize;
    private Paint.Cap mStrokeCap;
    private int mThumbBorderSize;
    private int mThumbRadius;
    private int mThumbFocusRadius;
    private float mThumbPosition;
    private Typeface mTypeface;
    private int mTextSize;
    private int mTextColor;
    private int mGravity = Gravity.CENTER;
    private int mTravelAnimationDuration;
    private int mTransformAnimationDuration;
    private Interpolator mInterpolator;

    private int mTouchSlop;
    private PointF mMemoPoint;
    private boolean mIsDragging;
    private float mThumbCurrentRadius;
    private float mThumbFillPercent;
    private int mTextHeight;
    private int mMemoValue;
    private String mValueText;

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
        mStepValue = 10;
        mThumbPosition = 0.5f;

        mContinuousMode = true;

        mPrimaryColor = 0xFF4557B7;
        mSecondaryColor = 0xFFBFBFBF;
        mStrokeSize = ThemeUtil.dpToPx(context, 2);
        mStrokeCap = Paint.Cap.SQUARE;
        mThumbBorderSize = ThemeUtil.dpToPx(context, 2);
        mThumbRadius = ThemeUtil.dpToPx(context, 9);
        mTravelAnimationDuration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mTransformAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        mInterpolator = new DecelerateInterpolator();
        mTextSize = ThemeUtil.spToPx(context, 12);
        mTextColor = 0xFFFFFFFF;
        mTypeface = Typeface.DEFAULT;


        mThumbFillPercent = mThumbPosition == 0 ? 0 : 1;
        mThumbCurrentRadius = mThumbRadius;
        mThumbFocusRadius = mThumbRadius * 2;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMemoPoint = new PointF();

        mThumbRadiusAnimator = new ThumbRadiusAnimator();
        mThumbStrokeAnimator = new ThumbStrokeAnimator();
        mThumbMoveAnimator = new ThumbMoveAnimator();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(mTypeface);
        mDrawRect = new RectF();
        mTempRect = new RectF();
        mLeftRailPath = new Path();
        mRightRailPath = new Path();

        measureText();
    }

    private void measureText(){
        Rect temp = new Rect();
        String text = String.valueOf(mMaxValue);
        mPaint.setTextSize(mTextSize);
        float width = mPaint.measureText(text);
        float maxWidth = (float)(mThumbRadius * Math.sqrt(2) * 2 - ThemeUtil.dpToPx(getContext(), 8));
        if(width > maxWidth){
            float textSize = mTextSize * maxWidth / width;
            mPaint.setTextSize(textSize);
        }

        mPaint.getTextBounds(text, 0, text.length(), temp);
        mTextHeight = temp.height();
    }

    private String getValueText(){
        int value = getValue();
        if(mValueText == null || mMemoValue != value){
            mMemoValue = value;
            mValueText = String.valueOf(mMemoValue);
        }

        return mValueText;
    }

    public int getValue(){
        return Math.round(getExactValue());
    }

    public float getExactValue(){
        return (mMaxValue - mMinValue) * getPosition() + mMinValue;
    }

    public float getPosition(){
        return mThumbMoveAnimator.isRunning() ? mThumbMoveAnimator.getPosition() : mThumbPosition;
    }

    public void setPosition(float pos, boolean animation){
        if(animation) {
            if(!mThumbMoveAnimator.startAnimation(pos)){
                if(!mIsDragging)
                    mThumbRadiusAnimator.startAnimation(mThumbRadius);
                mThumbStrokeAnimator.startAnimation(pos == 0 ? 0 : 1);
            }
        }
        else {
            mThumbPosition = pos;
            mThumbCurrentRadius = mThumbRadius;
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
        mDrawRect.left = getPaddingLeft() + mThumbRadius;
        mDrawRect.right = w - getPaddingRight() - mThumbRadius;

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

    private boolean isThumbHit(float x, float y, float radius){
        float cx = mDrawRect.width() * mThumbPosition + mDrawRect.left;
        float cy = mDrawRect.centerY();

        return x >= cx - radius && x <= cx + radius && y >= cy - radius && y < cy + radius;
    }

    private double distance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private float correctPosition(float position){
        if(!mContinuousMode)
            return position;

        int totalOffset = mMaxValue - mMinValue;
        int valueOffset = Math.round(totalOffset * position);
        int stepOffset = valueOffset / mStepValue;
        if(valueOffset - stepOffset * mStepValue < (stepOffset + 1) * mStepValue - valueOffset)
            position = (stepOffset * mStepValue) / (float)totalOffset;
        else
            position = (stepOffset + 1) * mStepValue / (float)totalOffset;

        return position;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = isThumbHit(event.getX(), event.getY(), mThumbRadius) && !mThumbMoveAnimator.isRunning();
                mMemoPoint.set(event.getX(), event.getY());
                if(mIsDragging)
                    mThumbRadiusAnimator.startAnimation(mContinuousMode ? 0 : mThumbFocusRadius);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsDragging) {
                    if(mContinuousMode) {
                        float position = correctPosition(Math.min(1f, Math.max(0f, (event.getX() - mDrawRect.left) / mDrawRect.width())));
                        setPosition(position, true);
                    }
                    else{
                        float offset = (event.getX() - mMemoPoint.x) / mDrawRect.width();
                        mThumbPosition = Math.min(1f, Math.max(0f, mThumbPosition + offset));
                        mMemoPoint.x = event.getX();
                        mThumbStrokeAnimator.startAnimation(mThumbPosition == 0 ? 0 : 1);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mIsDragging) {
                    mIsDragging = false;
                    setPosition(getPosition(), true);
                }
                else if(distance(mMemoPoint.x, mMemoPoint.y, event.getX(), event.getY()) <= mTouchSlop){
                    float position = correctPosition(Math.min(1f, Math.max(0f, (event.getX() - mDrawRect.left) / mDrawRect.width())));
                    setPosition(position, true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if(mIsDragging) {
                    mIsDragging = false;
                    setPosition(getPosition(), true);
                }
                break;
        }

        return true;
    }

    private void getRailPath(float x, float y, float radius){
        float halfStroke = mStrokeSize / 2f;

        mLeftRailPath.reset();
        mRightRailPath.reset();

        if(radius - 1f < halfStroke){
            if(mStrokeCap != Paint.Cap.ROUND){
                if(x > mDrawRect.left){
                    mLeftRailPath.moveTo(mDrawRect.left, y - halfStroke);
                    mLeftRailPath.lineTo(x, y - halfStroke);
                    mLeftRailPath.lineTo(x, y + halfStroke);
                    mLeftRailPath.lineTo(mDrawRect.left, y + halfStroke);
                    mLeftRailPath.close();
                }

                if(x < mDrawRect.right){
                    mRightRailPath.moveTo(mDrawRect.right, y + halfStroke);
                    mRightRailPath.lineTo(x, y + halfStroke);
                    mRightRailPath.lineTo(x, y - halfStroke);
                    mRightRailPath.lineTo(mDrawRect.right, y - halfStroke);
                    mRightRailPath.close();
                }
            }
            else{
                if(x > mDrawRect.left){
                    mTempRect.set(mDrawRect.left, y - halfStroke, mDrawRect.left + mStrokeSize, y + halfStroke);
                    mLeftRailPath.arcTo(mTempRect, 90, 180);
                    mLeftRailPath.lineTo(x, y - halfStroke);
                    mLeftRailPath.lineTo(x, y + halfStroke);
                    mLeftRailPath.close();
                }

                if(x < mDrawRect.right){
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
            double d2 = d1 / Math.tan(Math.PI * (1f - factor) / 4);

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

        float x = mDrawRect.width() * mThumbPosition + mDrawRect.left;
        float y = mDrawRect.centerY();
        int filledPrimaryColor = ColorUtil.getMiddleColor(mSecondaryColor, isEnabled() ? mPrimaryColor : mSecondaryColor, mThumbFillPercent);

        getRailPath(x, y, mThumbCurrentRadius);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSecondaryColor);
        canvas.drawPath(mRightRailPath, mPaint);
        mPaint.setColor(filledPrimaryColor);
        canvas.drawPath(mLeftRailPath, mPaint);

        if(mContinuousMode){
            float factor = 1f - mThumbCurrentRadius / mThumbRadius;

            if(factor > 0){
                mMarkPath = getMarkPath(mMarkPath, x, y, mThumbRadius, factor);
                mPaint.setStyle(Paint.Style.FILL);
                int saveCount = canvas.save();
                canvas.translate(0, -mThumbRadius * 2 * factor);
                canvas.drawPath(mMarkPath, mPaint);
                mPaint.setColor(ColorUtil.getColor(mTextColor, factor));
                canvas.drawText(getValueText(), x, y + mTextHeight / 2f - mThumbRadius * factor, mPaint);
                canvas.restoreToCount(saveCount);
            }

            float radius = isEnabled() ? mThumbCurrentRadius : mThumbCurrentRadius - mThumbBorderSize;
            if(radius > 0) {
                mPaint.setColor(filledPrimaryColor);
                canvas.drawCircle(x, y, radius, mPaint);
            }
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

        public boolean startAnimation(int radius) {
            if(mThumbCurrentRadius == radius)
                return false;

            mRadius = radius;

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else
                mThumbCurrentRadius = mRadius;

            invalidate();
            return true;
        }

        public void stopAnimation() {
            mRunning = false;
            mThumbCurrentRadius = mRadius;
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

        public boolean startAnimation(int fillPercent) {
            if(mThumbFillPercent == fillPercent)
                return false;

            mFillPercent = fillPercent;

            if(getHandler() != null){
                resetAnimation();
                mRunning = true;
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            }
            else
                mThumbFillPercent = mFillPercent;

            invalidate();
            return true;
        }

        public void stopAnimation() {
            mRunning = false;
            mThumbFillPercent = mFillPercent;
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
            mDuration = mContinuousMode && !mIsDragging ? mTransformAnimationDuration * 2 + mTravelAnimationDuration : mTravelAnimationDuration;
        }

        public boolean startAnimation(float position) {
            if(mThumbPosition == position)
                return false;

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

            return true;
        }

        public void stopAnimation() {
            mRunning = false;
            mThumbCurrentRadius = mContinuousMode && mIsDragging ? 0 : mThumbRadius;
            mThumbFillPercent = mFillPercent;
            mThumbPosition = mPosition;
            getHandler().removeCallbacks(this);
            invalidate();
        }

        @Override
        public void run() {
            long curTime = SystemClock.uptimeMillis();
            float progress = Math.min(1f, (float)(curTime - mStartTime) / mDuration);
            float value = mInterpolator.getInterpolation(progress);

            if(mContinuousMode){
                if(mIsDragging) {
                    mThumbPosition = (mPosition - mStartPosition) * value + mStartPosition;
                    mThumbFillPercent = (mFillPercent - mStartFillPercent) * value + mStartFillPercent;
                }
                else{
                    float p1 = (float)mTravelAnimationDuration / mDuration;
                    float p2 = (float)(mTravelAnimationDuration + mTransformAnimationDuration)/ mDuration;
                    if(progress < p1) {
                        value = mInterpolator.getInterpolation(progress / p1);
                        mThumbCurrentRadius = mStartRadius * (1f - value);
                        mThumbPosition = (mPosition - mStartPosition) * value + mStartPosition;
                        mThumbFillPercent = (mFillPercent - mStartFillPercent) * value + mStartFillPercent;
                    }
                    else if(progress > p2){
                        mThumbCurrentRadius = mThumbRadius * (progress - p2) / (1 - p2);
                    }
                }
            }
            else{
                mThumbPosition = (mPosition - mStartPosition) * value + mStartPosition;
                mThumbFillPercent = (mFillPercent - mStartFillPercent) * value + mStartFillPercent;

                if(progress < 0.2)
                    mThumbCurrentRadius = Math.max(mThumbRadius + mThumbBorderSize * progress * 5, mThumbCurrentRadius);
                else if(progress >= 0.8)
                    mThumbCurrentRadius = mThumbRadius + mThumbBorderSize * (5f - progress * 5);
            }


            if(progress == 1f)
                stopAnimation();

            if(mRunning)
                getHandler().postAtTime(this, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

            invalidate();
        }

    }
}
