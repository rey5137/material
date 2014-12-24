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

    private float[] mLocations = new float[48];
    private Rect mRect;
    private static final String[] TICKS = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "00"};

    private int mMode = -1;

    public static final int MODE_HOUR = 0;
    public static final int MODE_MINUTE = 1;

    private int mHour = -1;
    private int mMinute = -1;

    private boolean mEdited = false;

    public interface OnTimeChangedListener{
        public void onHourChanged(int oldValue, int newValue);

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
        setMode(a.getInteger(R.styleable.TimePicker_tp_mode, MODE_HOUR), false);
        setHour(a.getInteger(R.styleable.TimePicker_tp_hour, 0));
        setMinute(a.getInteger(R.styleable.TimePicker_tp_minute, 0));

        String familyName = a.getString(R.styleable.TimePicker_android_fontFamily);
        int style = a.getInteger(R.styleable.TimePicker_android_textStyle, Typeface.NORMAL);

        mTypeface = TypefaceUtil.load(context, familyName, style);

        a.recycle();
    }

    public void applyStyle(int styleId){
        Context context = getContext();
        TypedArray a = context.obtainStyledAttributes(styleId, R.styleable.TimePicker);
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
        setMode(a.getInteger(R.styleable.TimePicker_tp_mode, MODE_HOUR), false);
        setHour(a.getInteger(R.styleable.TimePicker_tp_hour, 0));
        setMinute(a.getInteger(R.styleable.TimePicker_tp_minute, 0));

        String familyName = a.getString(R.styleable.TimePicker_android_fontFamily);
        int style = a.getInteger(R.styleable.TimePicker_android_textStyle, Typeface.NORMAL);

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

    public void setMode(int mode, boolean animation){
        if(mMode != mode){
            mMode = mode;

            if(animation)
                startAnimation();
            else
                invalidate();
        }
    }

    public void setHour(int hour){
        hour = Math.min(Math.max(hour, 0), 11);

        if(mHour != hour){
            int old = mHour;
            mHour = hour;

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onHourChanged(old, mHour);

            if(mMode == MODE_HOUR)
                invalidate();
        }
    }

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

    public void setOnTimeChangedListener(OnTimeChangedListener listener){
        mOnTimeChangedListener = listener;
    }

    private float getAngle(int value, int mode){
        switch (mode){
            case MODE_HOUR:
                return (float)(-Math.PI / 3 + Math.PI / 6 * value);
            case MODE_MINUTE:
                return (float)(-Math.PI / 2 + Math.PI / 30 * value);
            default:
                return 0f;
        }
    }

    private int getSelectedTick(int value, int mode){
        switch (mode){
            case MODE_HOUR:
                return value;
            case MODE_MINUTE:
                if(value % 5 == 0)
                    return (value == 0) ? 23 : (value / 5 + 11);
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
        double step = Math.PI / 6;
        double angle = -Math.PI / 3;
        float x, y;

        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        mPaint.setTextAlign(Paint.Align.CENTER);

        for(int i = 0; i < TICKS.length; i++){
            x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
            y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;

            mPaint.getTextBounds(TICKS[i], 0, TICKS[i].length(), mRect);
            mLocations[i * 2] = x;
            mLocations[i * 2 + 1] = y + mRect.height() / 2f;

            angle += step;
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

    private int getPointedValue(float x, float y){
        float radius = (float)Math.sqrt(Math.pow(x - mCenterPoint.x, 2) + Math.pow(y - mCenterPoint.y, 2));
        if(radius > mInnerRadius + mSelectionRadius || radius < mInnerRadius - mSelectionRadius)
            return -1;

        float angle = (float)Math.atan2(y - mCenterPoint.y, x - mCenterPoint.x);
        if(angle < 0)
            angle += Math.PI * 2;

        if(mMode == MODE_HOUR){
            int value = (int)Math.round(angle * 6 / Math.PI) + 2;
            return value > 11 ? value - 12 : value;
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
                int value = getPointedValue(event.getX(), event.getY());
                if(value < 0)
                    return false;
                else if(mMode == MODE_HOUR)
                    setHour(value);
                else if(mMode == MODE_MINUTE)
                    setMinute(value);
                mEdited = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                value = getPointedValue(event.getX(), event.getY());
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

            if(mMode == MODE_HOUR){
                angle = getAngle(mHour, MODE_HOUR);
                selectedTick = getSelectedTick(mHour, MODE_HOUR);
                start = 0;
            }
            else{
                angle = getAngle(mMinute, MODE_MINUTE);
                selectedTick = getSelectedTick(mMinute, MODE_MINUTE);
                start = 12;
            }

            mPaint.setColor(mSelectionColor);
            float x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
            float y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;
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
            for(int i = 0; i < 12; i++) {
                index = start + i;
                mPaint.setColor(index == selectedTick ? mTextHighlightColor : mTextColor);
                canvas.drawText(TICKS[index], mLocations[index * 2], mLocations[index * 2 + 1], mPaint);
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
            int outSelectedTick;
            int inSelectedTick;

            if(mMode == MODE_MINUTE){
                outAngle = getAngle(mHour, MODE_HOUR);
                inAngle = getAngle(mMinute, MODE_MINUTE);
                outOffset = mOutInterpolator.getInterpolation(mAnimProgress) * maxOffset;
                inOffset = (1f - mInInterpolator.getInterpolation(mAnimProgress)) * -maxOffset;
                outStart = 0;
                inStart = 12;
                outSelectedTick = getSelectedTick(mHour, MODE_HOUR);
                inSelectedTick = getSelectedTick(mMinute, MODE_MINUTE);
            }
            else{
                outAngle = getAngle(mMinute, MODE_MINUTE);
                inAngle = getAngle(mHour, MODE_HOUR);
                outOffset = mOutInterpolator.getInterpolation(mAnimProgress) * -maxOffset;
                inOffset = (1f - mInInterpolator.getInterpolation(mAnimProgress)) * maxOffset;
                outStart = 12;
                inStart = 0;
                outSelectedTick = getSelectedTick(mMinute, MODE_MINUTE);
                inSelectedTick = getSelectedTick(mHour, MODE_HOUR);
            }

            mPaint.setColor(ColorUtil.getColor(mSelectionColor, 1f - mAnimProgress));
            float x = mCenterPoint.x + (float)Math.cos(outAngle) * (mInnerRadius + outOffset);
            float y = mCenterPoint.y + (float)Math.sin(outAngle) * (mInnerRadius + outOffset);
            canvas.drawCircle(x, y, mSelectionRadius, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mTickSize);
            x -= (float)Math.cos(outAngle) * mSelectionRadius;
            y -= (float)Math.sin(outAngle) * mSelectionRadius;
            canvas.drawLine(mCenterPoint.x, mCenterPoint.y, x, y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(ColorUtil.getColor(mSelectionColor, mAnimProgress));
            x = mCenterPoint.x + (float)Math.cos(inAngle) * (mInnerRadius + inOffset);
            y = mCenterPoint.y + (float)Math.sin(inAngle) * (mInnerRadius + inOffset);
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

            for(int i = 0; i < 12; i++){
                index = i + outStart;
                x = mLocations[index * 2] + (float)Math.cos(angle) * outOffset;
                y = mLocations[index * 2 + 1] + (float)Math.sin(angle) * outOffset;
                mPaint.setColor(index == outSelectedTick ? textHighlightOutColor : textOutColor);
                canvas.drawText(TICKS[index], x, y, mPaint);
                angle += step;
            }

            for(int i = 0; i < 12; i++){
                index = i + inStart;
                x = mLocations[index * 2] + (float)Math.cos(angle) * inOffset;
                y = mLocations[index * 2 + 1] + (float)Math.sin(angle) * inOffset;
                mPaint.setColor(index == inSelectedTick ? textHighlightInColor : textInColor);
                canvas.drawText(TICKS[index], x, y, mPaint);
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

        if(mRunning)
            getHandler().postAtTime(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.mode = mMode;
        ss.hour = mHour;
        ss.minute = mMinute;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setMode(ss.mode, false);
        setHour(ss.hour);
        setMinute(ss.minute);
    }

    static class SavedState extends BaseSavedState {
        int mode;
        int hour;
        int minute;

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
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mode);
            out.writeValue(hour);
            out.writeValue(minute);
        }

        @Override
        public String toString() {
            return "TimePicker.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " mode=" + mode
                    + " hour=" + hour
                    + " minute=" + minute + "}";
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
