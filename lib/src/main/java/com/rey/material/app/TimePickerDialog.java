package com.rey.material.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Rey on 12/24/2014.
 */
public class TimePickerDialog extends Dialog{

    private TimePickerLayout mTimePickerLayout;
    private float mCornerRadius;

    public interface OnTimeChangedListener{

        public void onAmChanged(boolean isAm);

        public void onHourChanged(int oldValue, int newValue);

        public void onMinuteChanged(int oldValue, int newValue);
    }

    public TimePickerDialog(Context context) {
        super(context);
    }

    public TimePickerDialog(Context context, int style) {
        super(context, style);
    }

    @Override
    protected void onCreate() {
        mTimePickerLayout = new TimePickerLayout(getContext());
        contentView(mTimePickerLayout);
    }

    @Override
    public Dialog applyStyle(int resId) {
        super.applyStyle(resId);

        if(resId == 0)
            return this;

        mTimePickerLayout.applyStyle(resId);
        layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return this;
    }

    @Override
    public Dialog layoutParams(int width, int height) {
        return super.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public Dialog cornerRadius(float radius){
        mCornerRadius = radius;
        return super.cornerRadius(radius);
    }

    public TimePickerDialog hour(int hour){
        mTimePickerLayout.setHour(hour);
        return this;
    }

    public TimePickerDialog minute(int minute){
        mTimePickerLayout.setMinute(minute);
        return this;
    }

    public TimePickerDialog am(boolean am){
        mTimePickerLayout.setAm(am);
        return this;
    }

    public TimePickerDialog onTimeChangedListener(OnTimeChangedListener listener){
        mTimePickerLayout.setOnTimeChangedListener(listener);
        return this;
    }

    public int getHour(){
        return mTimePickerLayout.getHour();
    }

    public int getMinute(){
        return mTimePickerLayout.getMinute();
    }

    public boolean isAm(){
        return mTimePickerLayout.isAm();
    }

    private class CircleCheckedTextView extends android.widget.CheckedTextView {

        private CircleDrawable mBackground;

        public CircleCheckedTextView(Context context) {
            super(context);

            setGravity(Gravity.CENTER);
            setPadding(0, 0, 0, 0);

            mBackground = new CircleDrawable();
            mBackground.setInEditMode(isInEditMode());
            mBackground.setAnimEnable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                setBackground(mBackground);
            else
                setBackgroundDrawable(mBackground);
            mBackground.setAnimEnable(true);
        }

        @Override
        public void setBackgroundColor(int color) {
            mBackground.setColor(color);
        }

        public void setAnimDuration(int duration) {
            mBackground.setAnimDuration(duration);
        }

        public void setInterpolator(Interpolator in, Interpolator out) {
            mBackground.setInterpolator(in, out);
        }

        public void setCheckedImmediately(boolean checked){
            mBackground.setAnimEnable(false);
            setChecked(checked);
            mBackground.setAnimEnable(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (getMeasuredWidth() != getMeasuredHeight()) {
                int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
                int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
                super.onMeasure(spec, spec);
            }
        }

        private class CircleDrawable extends Drawable implements Animatable {

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

    }

    private class TimePickerLayout extends android.widget.FrameLayout implements View.OnClickListener, TimePicker.OnTimeChangedListener{

        private int mHeaderHeight;
        private int mTextTimeColor;
        private int mTextTimeSize;

        private boolean mIsAm = true;
        private int mCheckBoxSize;

        private int mHeaderRealWidth;
        private int mHeaderRealHeight;

        private CircleCheckedTextView mAmView;
        private CircleCheckedTextView mPmView;
        private TimePicker mTimePicker;

        private Paint mPaint;
        private Path mHeaderBackground;
        private RectF mRect;

        private static final String TIME_DIVIDER = ":";
        private static final String BASE_TEXT = "0";

        private boolean mLocationDirty = true;
        private float mBaseY;
        private float mHourX;
        private float mDividerX;
        private float mMinuteX;
        private float mMiddayX;
        private float mHourWidth;
        private float mMinuteWidth;
        private float mBaseHeight;

        private String mHour;
        private String mMinute;
        private String mMidday;

        private OnTimeChangedListener mOnTimeChangedListener;

        public TimePickerLayout(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setTextAlign(Paint.Align.LEFT);
            mHeaderBackground = new Path();
            mRect = new RectF();

            mAmView = new CircleCheckedTextView(context);
            mPmView = new CircleCheckedTextView(context);
            mTimePicker = new TimePicker(context);

            mTimePicker.setPadding(mContentPadding, mContentPadding, mContentPadding, mContentPadding);
            mTimePicker.setOnTimeChangedListener(this);
            mAmView.setCheckedImmediately(mIsAm);
            mPmView.setCheckedImmediately(!mIsAm);
            mAmView.setOnClickListener(this);
            mPmView.setOnClickListener(this);

            mHour = String.valueOf(mTimePicker.getHour() + 1);
            mMinute = String.format("%02d", mTimePicker.getMinute());

            addView(mTimePicker);
            addView(mAmView);
            addView(mPmView);

            setWillNotDraw(false);
        }

        public void applyStyle(int resId){
            mTimePicker.applyStyle(resId);

            Context context = getContext();
            mCheckBoxSize = ThemeUtil.dpToPx(context, 48);

            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.TimePickerDialog);
            mHeaderHeight = a.getDimensionPixelSize(R.styleable.TimePickerDialog_tp_headerHeight, ThemeUtil.dpToPx(context, 120));
            mTextTimeColor = a.getColor(R.styleable.TimePickerDialog_tp_textTimeColor, 0xFF000000);
            mTextTimeSize = a.getDimensionPixelSize(R.styleable.TimePickerDialog_tp_textTimeSize, ThemeUtil.spToPx(context, 24));
            String am = a.getString(R.styleable.TimePickerDialog_tp_am);
            String pm = a.getString(R.styleable.TimePickerDialog_tp_pm);
            a.recycle();

            if(am == null)
                am = DateUtils.getAMPMString(Calendar.AM);

            if(pm == null)
                pm = DateUtils.getAMPMString(Calendar.PM);

            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked},
            };
            int[] colors = new int[]{
                    mTimePicker.getTextColor(),
                    mTimePicker.getTextHighlightColor(),
            };
            mAmView.setBackgroundColor(mTimePicker.getSelectionColor());
            mAmView.setAnimDuration(mTimePicker.getAnimDuration());
            mAmView.setInterpolator(mTimePicker.getInInterpolator(), mTimePicker.getOutInterpolator());
            mAmView.setTypeface(mTimePicker.getTypeface());
            mAmView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTimePicker.getTextSize());
            mAmView.setTextColor(new ColorStateList(states, colors));
            mAmView.setText(am);

            mPmView.setBackgroundColor(mTimePicker.getSelectionColor());
            mPmView.setAnimDuration(mTimePicker.getAnimDuration());
            mPmView.setInterpolator(mTimePicker.getInInterpolator(), mTimePicker.getOutInterpolator());
            mPmView.setTypeface(mTimePicker.getTypeface());
            mPmView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTimePicker.getTextSize());
            mPmView.setTextColor(new ColorStateList(states, colors));
            mPmView.setText(pm);

            mPaint.setTypeface(mTimePicker.getTypeface());
            mMidday = mIsAm ? mAmView.getText().toString() : mPmView.getText().toString();

            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);
        }

        public void setHour(int hour){
            mTimePicker.setHour(hour);
        }

        public int getHour(){
            return mTimePicker.getHour();
        }

        public void setMinute(int minute){
            mTimePicker.setMinute(minute);
        }

        public int getMinute(){
            return mTimePicker.getMinute();
        }

        public void setAm(boolean am){
            if(mIsAm != am){
                mIsAm = am;
                mAmView.setChecked(mIsAm);
                mPmView.setChecked(!mIsAm);
                mMidday = mIsAm ? mAmView.getText().toString() : mPmView.getText().toString();
                invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

                if(mOnTimeChangedListener != null)
                    mOnTimeChangedListener.onAmChanged(mIsAm);
            }
        }

        public boolean isAm(){
            return mIsAm;
        }

        public void setOnTimeChangedListener(OnTimeChangedListener listener){
            mOnTimeChangedListener = listener;
        }

        @Override
        public void onClick(View v) {
            setAm(v == mAmView);
        }

        @Override
        public void onModeChanged(int mode){
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);
        }

        @Override
        public void onHourChanged(int oldValue, int newValue) {
            mHour = String.valueOf(newValue + 1);
            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onHourChanged(oldValue, newValue);
        }

        @Override
        public void onMinuteChanged(int oldValue, int newValue) {
            mMinute = String.format("%02d", newValue);
            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onMinuteChanged(oldValue, newValue);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                if(heightMode == MeasureSpec.AT_MOST)
                    heightSize = Math.min(heightSize, mCheckBoxSize + widthSize + mHeaderHeight);

                int spec = MeasureSpec.makeMeasureSpec(mCheckBoxSize, MeasureSpec.EXACTLY);
                mAmView.measure(spec, spec);
                mPmView.measure(spec, spec);

                spec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                mTimePicker.measure(spec, spec);

                setMeasuredDimension(widthSize, heightSize);
            }
            else{
                int halfWidth = widthSize / 2;

                if(heightMode == MeasureSpec.AT_MOST)
                    heightSize = Math.min(heightSize, Math.max(mCheckBoxSize + mHeaderHeight + mContentPadding, halfWidth));

                int spec = MeasureSpec.makeMeasureSpec(mCheckBoxSize, MeasureSpec.EXACTLY);
                mAmView.measure(spec, spec);
                mPmView.measure(spec, spec);

                spec = MeasureSpec.makeMeasureSpec(halfWidth, MeasureSpec.EXACTLY);
                mTimePicker.measure(spec, spec);

                setMeasuredDimension(widthSize, heightSize);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            mLocationDirty = true;

            if(isPortrait){
                mHeaderRealWidth = w;
                mHeaderRealHeight = h - mCheckBoxSize - w;
                mHeaderBackground.reset();
                if(mCornerRadius == 0)
                    mHeaderBackground.addRect(0, 0, mHeaderRealWidth, mHeaderRealHeight, Path.Direction.CW);
                else{
                    mHeaderBackground.moveTo(0, mHeaderRealHeight);
                    mHeaderBackground.lineTo(0, mCornerRadius);
                    mRect.set(0, 0, mCornerRadius * 2, mCornerRadius * 2);
                    mHeaderBackground.arcTo(mRect, 180f, 90f, false);
                    mHeaderBackground.lineTo(mHeaderRealWidth - mCornerRadius, 0);
                    mRect.set(mHeaderRealWidth - mCornerRadius * 2, 0, mHeaderRealWidth, mCornerRadius * 2);
                    mHeaderBackground.arcTo(mRect, 270f, 90f, false);
                    mHeaderBackground.lineTo(mHeaderRealWidth, mHeaderRealHeight);
                    mHeaderBackground.close();
                }
            }
            else{
                mHeaderRealWidth = w / 2;
                mHeaderRealHeight = h - mCheckBoxSize - mContentPadding;
                mHeaderBackground.reset();
                if(mCornerRadius == 0)
                    mHeaderBackground.addRect(0, 0, mHeaderRealWidth, mHeaderRealHeight, Path.Direction.CW);
                else{
                    mHeaderBackground.moveTo(0, mHeaderRealHeight);
                    mHeaderBackground.lineTo(0, mCornerRadius);
                    mRect.set(0, 0, mCornerRadius * 2, mCornerRadius * 2);
                    mHeaderBackground.arcTo(mRect, 180f, 90f, false);
                    mHeaderBackground.lineTo(mHeaderRealWidth, 0);
                    mHeaderBackground.lineTo(mHeaderRealWidth, mHeaderRealHeight);
                    mHeaderBackground.close();
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                int paddingHorizontal = mContentPadding + mActionPadding;
                int paddingVertical = mContentPadding - mActionPadding;
                mAmView.layout(childLeft + paddingHorizontal, childBottom - paddingVertical - mCheckBoxSize, childLeft + paddingHorizontal + mCheckBoxSize, childBottom - paddingVertical);
                mPmView.layout(childRight - paddingHorizontal - mCheckBoxSize, childBottom - paddingVertical - mCheckBoxSize, childRight - paddingHorizontal, childBottom - paddingVertical);

                childTop += mHeaderRealHeight;
                childBottom -= mCheckBoxSize;
                mTimePicker.layout(childLeft, childTop, childRight, childBottom);
            }
            else{
                int paddingVertical = (childBottom - mTimePicker.getMeasuredHeight()) / 2;
                mTimePicker.layout(childRight - mTimePicker.getMeasuredWidth(), childTop + paddingVertical, childRight, childTop + paddingVertical + mTimePicker.getMeasuredHeight());

                int paddingHorizontal = mContentPadding + mActionPadding;
                paddingVertical = mContentPadding - mActionPadding;
                childRight -= mTimePicker.getMeasuredWidth();
                mAmView.layout(childLeft + paddingHorizontal, childBottom - paddingVertical - mCheckBoxSize, childLeft + paddingHorizontal + mCheckBoxSize, childBottom - paddingVertical);
                mPmView.layout(childRight - paddingHorizontal - mCheckBoxSize, childBottom - paddingVertical - mCheckBoxSize, childRight - paddingHorizontal, childBottom - paddingVertical);
            }
        }

        private void measureTimeText(){
            if(!mLocationDirty)
                return;

            mPaint.setTextSize(mTextTimeSize);

            Rect bounds = new Rect();
            mPaint.getTextBounds(BASE_TEXT, 0, BASE_TEXT.length(), bounds);
            mBaseHeight = bounds.height();

            mBaseY = (mHeaderRealHeight + mBaseHeight) / 2f;

            float dividerWidth = mPaint.measureText(TIME_DIVIDER, 0, TIME_DIVIDER.length());
            mHourWidth = mPaint.measureText(mHour, 0, mHour.length());
            mMinuteWidth = mPaint.measureText(mMinute, 0, mMinute.length());

            mDividerX = (mHeaderRealWidth - dividerWidth) / 2f;
            mHourX = mDividerX - mHourWidth;
            mMinuteX = mDividerX + dividerWidth;
            mMiddayX = mMinuteX + mMinuteWidth;

            mLocationDirty = false;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mTimePicker.getSelectionColor());
            canvas.drawPath(mHeaderBackground, mPaint);

            measureTimeText();

            mPaint.setTextSize(mTextTimeSize);
            mPaint.setColor(mTimePicker.getMode() == TimePicker.MODE_HOUR ? mTimePicker.getTextHighlightColor() : mTextTimeColor);
            canvas.drawText(mHour, mHourX, mBaseY, mPaint);

            mPaint.setColor(mTextTimeColor);
            canvas.drawText(TIME_DIVIDER, mDividerX, mBaseY, mPaint);

            mPaint.setColor(mTimePicker.getMode() == TimePicker.MODE_MINUTE ? mTimePicker.getTextHighlightColor() : mTextTimeColor);
            canvas.drawText(mMinute, mMinuteX, mBaseY, mPaint);

            mPaint.setTextSize(mTimePicker.getTextSize());
            mPaint.setColor(mTextTimeColor);
            canvas.drawText(mMidday, mMiddayX, mBaseY, mPaint);
        }

        private boolean isTouched(float left, float top, float right, float bottom, float x, float y){
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean handled =  super.onTouchEvent(event);

            if(handled)
                return handled;

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(isTouched(mHourX, mBaseY - mBaseHeight, mHourX + mHourWidth, mBaseY, event.getX(), event.getY()))
                        return mTimePicker.getMode() == TimePicker.MODE_MINUTE;

                    if(isTouched(mMinuteX, mBaseY - mBaseHeight, mMinuteX + mMinuteWidth, mBaseY, event.getX(), event.getY()))
                        return mTimePicker.getMode() == TimePicker.MODE_HOUR;
                    break;
                case MotionEvent.ACTION_UP:
                    if(isTouched(mHourX, mBaseY - mBaseHeight, mHourX + mHourWidth, mBaseY, event.getX(), event.getY()))
                        mTimePicker.setMode(TimePicker.MODE_HOUR, true);

                    if(isTouched(mMinuteX, mBaseY - mBaseHeight, mMinuteX + mMinuteWidth, mBaseY, event.getX(), event.getY()))
                        mTimePicker.setMode(TimePicker.MODE_MINUTE, true);
                    break;
            }

            return false;
        }
    }
}
