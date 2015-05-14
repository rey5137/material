package com.rey.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

/**
 * Created by Rey on 12/19/2014.
 */
public class TimePicker extends View{

    private int mBackgroundColor;
    private int mSelectionColor;
    private int mSelectionRadius;
    private int mTickSize;
    private Typeface mTypeface;
    private int mTextSize;
    private int mTextColor;
    private int mTextHighlightColor;
    private boolean m24Hour = true;

    private int mAnimDuration;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;
    private long mStartTime;
    private float mAnimProgress;
    private boolean mRunning;

    private Paint mPaint;

    private PointF mCenterPoint;
    private float mOuterRadius;
    private float mInnerRadius;
    private float mSecondInnerRadius;

    private float[] mLocations = new float[72];
    private Rect mRect;
    private String[] mTicks;

    private int mMode = MODE_HOUR;

    public static final int MODE_HOUR = 0;
    public static final int MODE_MINUTE = 1;

    private int mHour = 0;
    private int mMinute = 0;

    private boolean mEdited = false;

    /**
     * Interface definition for a callback to be invoked when the selected time is changed.
     */
    public interface OnTimeChangedListener{

        /**
         * Called when the select mode is changed
         * @param mode The current mode. Can be {@link #MODE_HOUR} or {@link #MODE_MINUTE}.
         */
        public void onModeChanged(int mode);

        /**
         * Called then the selected hour is changed.
         * @param oldValue The old hour value.
         * @param newValue The new hour value.
         */
        public void onHourChanged(int oldValue, int newValue);

        /**
         * Called then the selected minute is changed.
         * @param oldValue The old minute value.
         * @param newValue The new minute value.
         */
        public void onMinuteChanged(int oldValue, int newValue);
    }

    private OnTimeChangedListener mOnTimeChangedListener;

    public TimePicker(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();

        initTickLabels();

        setWillNotDraw(false);
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Init the localized label of ticks. The value of ticks in order:
     * 1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
     * "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "0",
     * "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "0"
     */
    private void initTickLabels(){
        String format = "%2d";
        mTicks = new String[36];
        for(int i = 0; i < 23; i++)
            mTicks[i] = String.format(format, i + 1);
        mTicks[23] = String.format(format, 0);
        mTicks[35] = mTicks[23];
        for(int i = 24; i < 35; i++)
            mTicks[i] = String.format(format, (i - 23) * 5);
    }

    public void applyStyle(int styleId){
        applyStyle(getContext(), null, 0, styleId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        mBackgroundColor = a.getColor(R.styleable.TimePicker_tp_backgroundColor, ColorUtil.getColor(ThemeUtil.colorPrimary(context, 0xFF000000), 0.25f));
        mSelectionColor = a.getColor(R.styleable.TimePicker_tp_selectionColor, ThemeUtil.colorPrimary(context, 0xFF000000));
        mSelectionRadius = a.getDimensionPixelOffset(R.styleable.TimePicker_tp_selectionRadius, ThemeUtil.dpToPx(context, 8));
        mTickSize = a.getDimensionPixelSize(R.styleable.TimePicker_tp_tickSize, ThemeUtil.dpToPx(context, 1));
        mTextSize = a.getDimensionPixelSize(R.styleable.TimePicker_tp_textSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_caption_material));
        mTextColor = a.getColor(R.styleable.TimePicker_tp_textColor, 0xFF000000);
        mTextHighlightColor = a.getColor(R.styleable.TimePicker_tp_textHighlightColor, 0xFFFFFFFF);
        mAnimDuration = a.getInteger(R.styleable.TimePicker_tp_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        int resId = a.getResourceId(R.styleable.TimePicker_tp_inInterpolator, 0);
        mInInterpolator = resId == 0 ? new DecelerateInterpolator() : AnimationUtils.loadInterpolator(context, resId);
        resId = a.getResourceId(R.styleable.TimePicker_tp_outInterpolator, 0);
        mOutInterpolator = resId == 0 ? new DecelerateInterpolator() : AnimationUtils.loadInterpolator(context, resId);
        setMode(a.getInteger(R.styleable.TimePicker_tp_mode, mMode), false);
        if(a.hasValue(R.styleable.TimePicker_tp_24Hour))
            set24Hour(a.getBoolean(R.styleable.TimePicker_tp_24Hour, m24Hour));
        else
            set24Hour(DateFormat.is24HourFormat(context));
        setHour(a.getInteger(R.styleable.TimePicker_tp_hour, mHour));
        setMinute(a.getInteger(R.styleable.TimePicker_tp_minute, mMinute));

        String familyName = a.getString(R.styleable.TimePicker_tp_fontFamily);
        int style = a.getInteger(R.styleable.TimePicker_tp_textStyle, Typeface.NORMAL);

        mTypeface = TypefaceUtil.load(context, familyName, style);

        a.recycle();
    }

    public int getBackgroundColor(){
        return mBackgroundColor;
    }

    public int getSelectionColor(){
        return mSelectionColor;
    }

    public Typeface getTypeface(){
        return mTypeface;
    }

    public int getTextSize(){
        return mTextSize;
    }

    public int getTextColor(){
        return mTextColor;
    }

    public int getTextHighlightColor(){
        return mTextHighlightColor;
    }

    public int getAnimDuration(){
        return mAnimDuration;
    }

    public Interpolator getInInterpolator(){
        return mInInterpolator;
    }

    public Interpolator getOutInterpolator(){
        return mOutInterpolator;
    }

    /**
     * @return The current select mode. Can be {@link #MODE_HOUR} or {@link #MODE_MINUTE}.
     */
    public int getMode(){
        return mMode;
    }

    /**
     * @return The selected hour value.
     */
    public int getHour(){
        return mHour;
    }

    /**
     * @return The selected minute value.
     */
    public int getMinute(){
        return mMinute;
    }

    /**
     * @return this TimePicker use 24-hour format or not.
     */
    public boolean is24Hour(){
        return m24Hour;
    }

    /**
     * Set the select mode of this TimePicker.
     * @param mode The select mode. Can be {@link #MODE_HOUR} or {@link #MODE_MINUTE}.
     * @param animation Indicate that should show animation when switch select mode or not.
     */
    public void setMode(int mode, boolean animation){
        if(mMode != mode){
            mMode = mode;

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onModeChanged(mMode);

            if(animation)
                startAnimation();
            else
                invalidate();
        }
    }

    /**
     * Set the selected hour value.
     * @param hour The selected hour value.
     */
    public void setHour(int hour){
        if(m24Hour)
            hour = Math.max(hour, 0) % 24;
        else
            hour = Math.max(hour, 0) % 12;

        if(mHour != hour){
            int old = mHour;
            mHour = hour;

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onHourChanged(old, mHour);

            if(mMode == MODE_HOUR)
                invalidate();
        }
    }

    /**
     * Set the selected minute value.
     * @param minute The selected minute value.
     */
    public void setMinute(int minute){
        minute = Math.min(Math.max(minute, 0), 59);

        if(mMinute != minute){
            int old = mMinute;
            mMinute = minute;

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onMinuteChanged(old, mMinute);

            if(mMode == MODE_MINUTE)
                invalidate();
        }
    }

    /**
     * Set a listener will be called when the selected time is changed.
     * @param listener The {@link TimePicker.OnTimeChangedListener} will be called.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener listener){
        mOnTimeChangedListener = listener;
    }

    /**
     * Set this TimePicker use 24-hour format or not.
     * @param b
     */
    public void set24Hour(boolean b){
        if(m24Hour != b){
            m24Hour = b;
            if(!m24Hour && mHour > 11)
                setHour(mHour - 12);
            calculateTextLocation();
        }
    }

    private float getAngle(int value, int mode){
        switch (mode){
            case MODE_HOUR:
                return (float)(-Math.PI / 2 + Math.PI / 6 * value);
            case MODE_MINUTE:
                return (float)(-Math.PI / 2 + Math.PI / 30 * value);
            default:
                return 0f;
        }
    }

    private int getSelectedTick(int value, int mode){
        switch (mode){
            case MODE_HOUR:
                return value == 0 ? (m24Hour ? 23 : 11) : value - 1;
            case MODE_MINUTE:
                if(value % 5 == 0)
                    return (value == 0) ? 35 : (value / 5 + 23);
            default:
                return -1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = (widthMode == MeasureSpec.UNSPECIFIED) ?  mSelectionRadius * 12 : MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = (heightMode == MeasureSpec.UNSPECIFIED) ?  mSelectionRadius * 12 : MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int size = Math.min(widthSize, heightSize);

        int width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : size;
        int height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : size;

        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
    }

    private void calculateTextLocation(){
        if(mCenterPoint == null)
            return;

        double step = Math.PI / 6;
        double angle = -Math.PI / 3;
        float x, y;

        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        mPaint.setTextAlign(Paint.Align.CENTER);

        if(m24Hour){
            for(int i = 0; i < 12; i++){
                mPaint.getTextBounds(mTicks[i], 0, mTicks[i].length(), mRect);
                if(i == 0)
                    mSecondInnerRadius = mInnerRadius - mSelectionRadius - mRect.height();

                x = mCenterPoint.x + (float)Math.cos(angle) * mSecondInnerRadius;
                y = mCenterPoint.y + (float)Math.sin(angle) * mSecondInnerRadius;

                mLocations[i * 2] = x;
                mLocations[i * 2 + 1] = y + mRect.height() / 2f;

                angle += step;
            }

            for(int i = 12; i < mTicks.length; i++){
                x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
                y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;

                mPaint.getTextBounds(mTicks[i], 0, mTicks[i].length(), mRect);
                mLocations[i * 2] = x;
                mLocations[i * 2 + 1] = y + mRect.height() / 2f;

                angle += step;
            }
        }
        else{
            for(int i = 0; i < 12; i++){
                x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
                y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;

                mPaint.getTextBounds(mTicks[i], 0, mTicks[i].length(), mRect);
                mLocations[i * 2] = x;
                mLocations[i * 2 + 1] = y + mRect.height() / 2f;

                angle += step;
            }

            for(int i = 24; i < mTicks.length; i++){
                x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
                y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;

                mPaint.getTextBounds(mTicks[i], 0, mTicks[i].length(), mRect);
                mLocations[i * 2] = x;
                mLocations[i * 2 + 1] = y + mRect.height() / 2f;

                angle += step;
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int size = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());

        if(mCenterPoint == null)
            mCenterPoint = new PointF();

        mOuterRadius = size / 2f;
        mCenterPoint.set(left + mOuterRadius, top + mOuterRadius);
        mInnerRadius = mOuterRadius - mSelectionRadius - ThemeUtil.dpToPx(getContext(), 4);

        calculateTextLocation();
    }

    private int getPointedValue(float x, float y, boolean isDown){
        float radius = (float) Math.sqrt(Math.pow(x - mCenterPoint.x, 2) + Math.pow(y - mCenterPoint.y, 2));
        if(isDown) {
            if(mMode == MODE_HOUR && m24Hour){
                if (radius > mInnerRadius + mSelectionRadius || radius < mSecondInnerRadius - mSelectionRadius)
                    return -1;
            }
            else if (radius > mInnerRadius + mSelectionRadius || radius < mInnerRadius - mSelectionRadius)
                return -1;
        }

        float angle = (float)Math.atan2(y - mCenterPoint.y, x - mCenterPoint.x);
        if(angle < 0)
            angle += Math.PI * 2;

        if(mMode == MODE_HOUR){
            if(m24Hour){
                if(radius > mSecondInnerRadius + mSelectionRadius / 2){
                    int value = (int) Math.round(angle * 6 / Math.PI) + 15;
                    if(value == 24)
                        return 0;
                    else if(value > 24)
                        return value - 12;
                    else
                        return value;
                }
                else{
                    int value = (int) Math.round(angle * 6 / Math.PI) + 3;
                    return value > 12 ? value - 12 : value;
                }
            }
            else {
                int value = (int) Math.round(angle * 6 / Math.PI) + 3;
                return value > 11 ? value - 12 : value;
            }
        }
        else if(mMode == MODE_MINUTE){
            int value = (int)Math.round(angle * 30 / Math.PI) + 15;
            return value > 59 ? value - 60 : value;
        }

        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                int value = getPointedValue(event.getX(), event.getY(), true);
                if(value < 0)
                    return false;
                else if(mMode == MODE_HOUR)
                    setHour(value);
                else if(mMode == MODE_MINUTE)
                    setMinute(value);
                mEdited = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                value = getPointedValue(event.getX(), event.getY(), false);
                if(value < 0)
                    return true;
                else if(mMode == MODE_HOUR)
                    setHour(value);
                else if(mMode == MODE_MINUTE)
                    setMinute(value);
                mEdited = true;
                return true;
            case MotionEvent.ACTION_UP:
                if(mEdited && mMode == MODE_HOUR){
                    setMode(MODE_MINUTE, true);
                    mEdited = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mEdited = false;
                break;
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOuterRadius, mPaint);

        if(!mRunning){
            float angle;
            int selectedTick;
            int start;
            int length;
            float radius;

            if(mMode == MODE_HOUR){
                angle = getAngle(mHour, MODE_HOUR);
                selectedTick = getSelectedTick(mHour, MODE_HOUR);
                start = 0;
                length = m24Hour ? 24 : 12;
                radius = m24Hour && selectedTick < 12 ? mSecondInnerRadius : mInnerRadius;
            }
            else{
                angle = getAngle(mMinute, MODE_MINUTE);
                selectedTick = getSelectedTick(mMinute, MODE_MINUTE);
                start = 24;
                length = 12;
                radius = mInnerRadius;
            }

            mPaint.setColor(mSelectionColor);
            float x = mCenterPoint.x + (float)Math.cos(angle) * radius;
            float y = mCenterPoint.y + (float)Math.sin(angle) * radius;
            canvas.drawCircle(x, y, mSelectionRadius, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mTickSize);
            x -= (float)Math.cos(angle) * mSelectionRadius;
            y -= (float)Math.sin(angle) * mSelectionRadius;
            canvas.drawLine(mCenterPoint.x, mCenterPoint.y, x, y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mTextColor);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mTickSize * 2, mPaint);

            mPaint.setTextSize(mTextSize);
            mPaint.setTypeface(mTypeface);
            mPaint.setTextAlign(Paint.Align.CENTER);

            int index;
            for(int i = 0; i < length; i++) {
                index = start + i;
                mPaint.setColor(index == selectedTick ? mTextHighlightColor : mTextColor);
                canvas.drawText(mTicks[index], mLocations[index * 2], mLocations[index * 2 + 1], mPaint);
            }
        }
        else{
            float maxOffset = mOuterRadius - mInnerRadius + mTextSize / 2;
            int textOutColor = ColorUtil.getColor(mTextColor, 1f - mAnimProgress);
            int textHighlightOutColor= ColorUtil.getColor(mTextHighlightColor, 1f - mAnimProgress);
            int textInColor = ColorUtil.getColor(mTextColor, mAnimProgress);
            int textHighlightInColor= ColorUtil.getColor(mTextHighlightColor, mAnimProgress);
            float outOffset;
            float inOffset;
            float outAngle;
            float inAngle;
            int outStart;
            int inStart;
            int outLength;
            int inLength;
            int outSelectedTick;
            int inSelectedTick;
            float outRadius;
            float inRadius;

            if(mMode == MODE_MINUTE){
                outAngle = getAngle(mHour, MODE_HOUR);
                inAngle = getAngle(mMinute, MODE_MINUTE);
                outOffset = mOutInterpolator.getInterpolation(mAnimProgress) * maxOffset;
                inOffset = (1f - mInInterpolator.getInterpolation(mAnimProgress)) * -maxOffset;
                outSelectedTick = getSelectedTick(mHour, MODE_HOUR);
                inSelectedTick = getSelectedTick(mMinute, MODE_MINUTE);
                outStart = 0;
                outLength = m24Hour ? 24 : 12;
                outRadius = m24Hour && outSelectedTick < 12 ? mSecondInnerRadius : mInnerRadius;
                inStart = 24;
                inLength = 12;
                inRadius = mInnerRadius;
            }
            else{
                outAngle = getAngle(mMinute, MODE_MINUTE);
                inAngle = getAngle(mHour, MODE_HOUR);
                outOffset = mOutInterpolator.getInterpolation(mAnimProgress) * -maxOffset;
                inOffset = (1f - mInInterpolator.getInterpolation(mAnimProgress)) * maxOffset;
                outSelectedTick = getSelectedTick(mMinute, MODE_MINUTE);
                inSelectedTick = getSelectedTick(mHour, MODE_HOUR);
                outStart = 24;
                outLength = 12;
                outRadius = mInnerRadius;
                inStart = 0;
                inLength = m24Hour ? 24 : 12;
                inRadius = m24Hour && inSelectedTick < 12 ? mSecondInnerRadius : mInnerRadius;
            }

            mPaint.setColor(ColorUtil.getColor(mSelectionColor, 1f - mAnimProgress));
            float x = mCenterPoint.x + (float)Math.cos(outAngle) * (outRadius + outOffset);
            float y = mCenterPoint.y + (float)Math.sin(outAngle) * (outRadius + outOffset);
            canvas.drawCircle(x, y, mSelectionRadius, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mTickSize);
            x -= (float)Math.cos(outAngle) * mSelectionRadius;
            y -= (float)Math.sin(outAngle) * mSelectionRadius;
            canvas.drawLine(mCenterPoint.x, mCenterPoint.y, x, y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(ColorUtil.getColor(mSelectionColor, mAnimProgress));
            x = mCenterPoint.x + (float)Math.cos(inAngle) * (inRadius + inOffset);
            y = mCenterPoint.y + (float)Math.sin(inAngle) * (inRadius + inOffset);
            canvas.drawCircle(x, y, mSelectionRadius, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mTickSize);
            x -= (float)Math.cos(inAngle) * mSelectionRadius;
            y -= (float)Math.sin(inAngle) * mSelectionRadius;
            canvas.drawLine(mCenterPoint.x, mCenterPoint.y, x, y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mTextColor);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mTickSize * 2, mPaint);

            mPaint.setTextSize(mTextSize);
            mPaint.setTypeface(mTypeface);
            mPaint.setTextAlign(Paint.Align.CENTER);

            double step = Math.PI / 6;
            double angle = -Math.PI / 3;
            int index;

            for(int i = 0; i < outLength; i++){
                index = i + outStart;
                x = mLocations[index * 2] + (float)Math.cos(angle) * outOffset;
                y = mLocations[index * 2 + 1] + (float)Math.sin(angle) * outOffset;
                mPaint.setColor(index == outSelectedTick ? textHighlightOutColor : textOutColor);
                canvas.drawText(mTicks[index], x, y, mPaint);
                angle += step;
            }

            for(int i = 0; i < inLength; i++){
                index = i + inStart;
                x = mLocations[index * 2] + (float)Math.cos(angle) * inOffset;
                y = mLocations[index * 2 + 1] + (float)Math.sin(angle) * inOffset;
                mPaint.setColor(index == inSelectedTick ? textHighlightInColor : textInColor);
                canvas.drawText(mTicks[index], x, y, mPaint);
                angle += step;
            }
        }
    }

    private void resetAnimation(){
        mStartTime = SystemClock.uptimeMillis();
        mAnimProgress = 0f;
    }

    private void startAnimation() {
        if(getHandler() != null){
            resetAnimation();
            mRunning = true;
            getHandler().postAtTime(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
        }

        invalidate();
    }

    private void stopAnimation() {
        mRunning = false;
        mAnimProgress = 1f;
        if(getHandler() != null)
            getHandler().removeCallbacks(mUpdater);
        invalidate();
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
            stopAnimation();

        if(mRunning) {
            if(getHandler() != null)
                getHandler().postAtTime(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            else
                stopAnimation();
        }

        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.mode = mMode;
        ss.hour = mHour;
        ss.minute = mMinute;
        ss.is24Hour = m24Hour;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        set24Hour(ss.is24Hour);
        setMode(ss.mode, false);
        setHour(ss.hour);
        setMinute(ss.minute);
    }

    static class SavedState extends BaseSavedState {
        int mode;
        int hour;
        int minute;
        boolean is24Hour;

        /**
         * Constructor called from {@link Switch#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mode = in.readInt();
            hour = in.readInt();
            minute = in.readInt();
            is24Hour = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mode);
            out.writeValue(hour);
            out.writeValue(minute);
            out.writeValue(is24Hour ? 1 : 0);
        }

        @Override
        public String toString() {
            return "TimePicker.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " mode=" + mode
                    + " hour=" + hour
                    + " minute=" + minute
                    + "24hour=" + is24Hour + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
