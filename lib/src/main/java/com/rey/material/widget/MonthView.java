package com.rey.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Rey on 12/29/2014.
 */
public class MonthView extends View{

    private Typeface mTypeface;
    private int mTextSize;
    private int mTextColor;
    private int mTextLabelColor;
    private int mTextHighlightColor;
    private int mTextDisableColor;
    private int mSelectionColor;

    private long mStartTime;
    private float mAnimProgress;
    private boolean mRunning;
    private int mAnimDuration;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;

    private Paint mPaint;
    private float mBaseWidth;
    private float mBaseHeight;
    private float mRowHeight;
    private float mColWidth;
    private int mPadding;
    private float mSelectionRadius;

    private int mTouchedDay = -1;

    private Calendar mCalendar;
    private int mMonth;
    private int mYear;
    private boolean mIsMondayFirst;
    private String[] mLabels = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private int mMaxDay;
    private int mFirstDayCol;
    private int mMinAvailDay = -1;
    private int mMaxAvailDay = Integer.MAX_VALUE;
    private int mToday = -1;
    private int mSelectedDay = -1;
    private int mPreviousSelectedDay = -1;
    private String mMonthText;

    public interface OnSelectionDayChangedListener{
        public void onSelectionDayChanged(int oldValue, int newValue);
    }

    private OnSelectionDayChangedListener mOnSelectionDayChangedListener;

    private static String[] mDayTexts;

    public MonthView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mPadding = ThemeUtil.dpToPx(context, 4);
        setWillNotDraw(false);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MonthView, defStyleAttr, defStyleRes);
        mTextSize = a.getDimensionPixelSize(R.styleable.MonthView_dp_dayTextSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_caption_material));
        mTextColor = a.getColor(R.styleable.MonthView_dp_textColor, 0xFF000000);
        mTextHighlightColor = a.getColor(R.styleable.MonthView_dp_textHighlightColor, 0xFFFFFFFF);
        mTextLabelColor = a.getColor(R.styleable.MonthView_dp_textLabelColor, 0xFF767676);
        mTextDisableColor = a.getColor(R.styleable.MonthView_dp_textDisableColor, 0xFF767676);
        mSelectionColor = a.getColor(R.styleable.MonthView_dp_selectionColor, ThemeUtil.colorPrimary(context, 0xFF000000));
        mAnimDuration = a.getInteger(R.styleable.MonthView_dp_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        int resId = a.getResourceId(R.styleable.MonthView_dp_inInterpolator, 0);
        if(resId != 0)
            mInInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mInInterpolator = new DecelerateInterpolator();
        resId = a.getResourceId(R.styleable.MonthView_dp_outInterpolator, 0);
        if(resId != 0)
            mOutInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mOutInterpolator = new DecelerateInterpolator();
        String familyName = a.getString(R.styleable.MonthView_android_fontFamily);
        int style = a.getInteger(R.styleable.MonthView_android_textStyle, Typeface.NORMAL);

        mTypeface = TypefaceUtil.load(context, familyName, style);
        a.recycle();
    }

    public void setOnSelectionDayChangedListener(OnSelectionDayChangedListener listener){
        mOnSelectionDayChangedListener = listener;
    }

    public void setMonth(int month, int year){
        if(mMonth != month || mYear != year){
            mMonth = month;
            mYear = year;
            calculateMonthView();
            invalidate();
        }
    }

    public void setSelectedDay(int day, boolean animation){
        if(mSelectedDay != day){
            mPreviousSelectedDay = mSelectedDay;
            mSelectedDay = day;

            if(mOnSelectionDayChangedListener != null)
                mOnSelectionDayChangedListener.onSelectionDayChanged(mPreviousSelectedDay, mSelectedDay);

            if(animation)
                startAnimation();
            else
                invalidate();
        }
    }

    public void setToday(int day){
        if(mToday != day){
            mToday = day;
            invalidate();
        }
    }

    public void setAvailableDay(int min, int max){
        if(mMinAvailDay != min || mMaxAvailDay != max){
            mMinAvailDay = min;
            mMaxAvailDay = max;
            invalidate();
        }
    }

    private Calendar getCalendar(){
        if(mCalendar == null)
            mCalendar = Calendar.getInstance();
        return mCalendar;
    }

    private void calculateMonthView(){
        mIsMondayFirst = getCalendar().getFirstDayOfWeek() == Calendar.MONDAY;

        Calendar  cal = getCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, mMonth);
        cal.set(Calendar.YEAR, mYear);

        mMaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        mFirstDayCol = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if(mIsMondayFirst)
            mFirstDayCol = mFirstDayCol == 0 ? 6 : mFirstDayCol - 1;
        mMonthText = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + mYear;
    }

    private String getDayText(int day){
        if(mDayTexts == null){
            synchronized (MonthView.class){
                if(mDayTexts == null)
                    mDayTexts = new String[31];
            }
        }

        if(mDayTexts[day - 1] == null)
            mDayTexts[day - 1] = String.valueOf(day);

        return mDayTexts[day - 1];
    }

    private void measureBaseSize(){
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        mBaseWidth = mPaint.measureText("88", 0, 2) + mPadding * 2;

        Rect bounds = new Rect();
        mPaint.getTextBounds("88", 0, 2 ,bounds);
        mBaseHeight = bounds.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureBaseSize();

        int size = Math.round(Math.max(mBaseWidth, mBaseHeight));

        int width = size * 7 + getPaddingLeft() + getPaddingRight();
        int height = Math.round(size * 7 + mBaseHeight + mPadding * 2 + getPaddingTop() + getPaddingBottom());

        switch (widthMode){
            case MeasureSpec.AT_MOST:
                width = Math.min(width, widthSize);
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
        }

        switch (heightMode){
            case MeasureSpec.AT_MOST:
                height = Math.min(height, heightSize);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mColWidth = (w - getPaddingLeft() - getPaddingRight()) / 7f;
        mRowHeight = (h - mBaseHeight - mPadding * 2 - getPaddingTop() - getPaddingBottom()) / 7f;
        mSelectionRadius = Math.min(mColWidth, mRowHeight) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //draw month text
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        float x = 3.5f * mColWidth + getPaddingLeft();
        float y = mPadding * 2 + mBaseHeight;
        mPaint.setFakeBoldText(true);
        mPaint.setColor(mTextColor);
        canvas.drawText(mMonthText, x, y, mPaint);

        //draw selection
        float paddingLeft = getPaddingLeft();
        float paddingTop = mPadding * 2 + mBaseHeight + getPaddingTop();
        if(mSelectedDay > 0){
            int col = (mFirstDayCol + mSelectedDay - 1) % 7;
            int row = (mFirstDayCol + mSelectedDay - 1) / 7 + 1;

            x = (col + 0.5f) * mColWidth + paddingLeft;
            y = (row + 0.5f) * mRowHeight + paddingTop;
            float radius = mRunning ? mInInterpolator.getInterpolation(mAnimProgress) * mSelectionRadius : mSelectionRadius;
            mPaint.setColor(mSelectionColor);
            canvas.drawCircle(x, y, radius, mPaint);
        }

        if(mRunning && mPreviousSelectedDay > 0){
            int col = (mFirstDayCol + mPreviousSelectedDay - 1) % 7;
            int row = (mFirstDayCol + mPreviousSelectedDay - 1) / 7 + 1;

            x = (col + 0.5f) * mColWidth + paddingLeft;
            y = (row + 0.5f) * mRowHeight + paddingTop;
            float radius = (1f - mOutInterpolator.getInterpolation(mAnimProgress)) * mSelectionRadius;
            mPaint.setColor(mSelectionColor);
            canvas.drawCircle(x, y, radius, mPaint);
        }

        //draw label
        mPaint.setFakeBoldText(false);
        mPaint.setColor(mTextLabelColor);
        paddingTop += (mRowHeight + mBaseHeight) / 2f;
        for(int i = 0; i < 7; i++){
            x = (i + 0.5f) * mColWidth + paddingLeft;
            y = paddingTop;
            int index = mIsMondayFirst ? (i == 6 ? 0 : i - 1) : i;
            canvas.drawText(mLabels[index], x, y, mPaint);
        }

        //draw day text
        int col = mFirstDayCol;
        int row = 1;
        for(int day = 1; day <= mMaxDay; day++){
            if(day == mSelectedDay)
                mPaint.setColor(mTextHighlightColor);
            else if(day < mMinAvailDay || day > mMaxAvailDay)
                mPaint.setColor(mTextDisableColor);
            else if(day == mToday)
                mPaint.setColor(mSelectionColor);
            else
                mPaint.setColor(mTextColor);

            x = (col + 0.5f) * mColWidth + paddingLeft;
            y = row * mRowHeight + paddingTop;

            canvas.drawText(getDayText(day), x, y, mPaint);
            col++;
            if(col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private int getTouchedDay(float x, float y){
        float paddingTop = mPadding * 2 + mBaseHeight + getPaddingTop() + mRowHeight;
        if(x < getPaddingLeft() || x > getWidth() - getPaddingRight() || y < paddingTop || y > getHeight() - getPaddingBottom())
            return -1;

        int col = (int)Math.floor((x - getPaddingLeft()) / mColWidth);
        int row = (int)Math.floor((y - paddingTop) / mRowHeight);

        int day = row * 7 + col - mFirstDayCol + 1;
        if(day < 0 || day < mMinAvailDay || day > mMaxAvailDay)
            return -1;

        return day;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchedDay = getTouchedDay(event.getX(), event.getY());
                if(mTouchedDay > 0)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
                if(getTouchedDay(event.getX(), event.getY()) == mTouchedDay)
                    setSelectedDay(mTouchedDay, true);
                mTouchedDay = -1;
                return true;
            case MotionEvent.ACTION_CANCEL:
                mTouchedDay = -1;
                break;
        }

        return false;
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

        ss.month = mMonth;
        ss.year = mYear;
        ss.minAvailDay = mMinAvailDay;
        ss.maxAvailDay = mMaxAvailDay;
        ss.today = mToday;
        ss.selectedDay = mSelectedDay;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setMonth(ss.month, ss.year);
        setAvailableDay(ss.minAvailDay, ss.maxAvailDay);
        setToday(ss.today);
        setSelectedDay(ss.selectedDay, false);
    }

    static class SavedState extends BaseSavedState {
        int month;
        int year;
        int minAvailDay;
        int maxAvailDay;
        int today;
        int selectedDay;

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
            month = in.readInt();
            year = in.readInt();
            minAvailDay = in.readInt();
            maxAvailDay = in.readInt();
            today = in.readInt();
            selectedDay = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(month);
            out.writeValue(year);
            out.writeValue(minAvailDay);
            out.writeValue(maxAvailDay);
            out.writeValue(today);
            out.writeValue(selectedDay);
        }

        @Override
        public String toString() {
            return "MonthView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " month=" + month
                    + " year=" + year
                    + " minAvailDay=" + minAvailDay
                    + " maxAvailDay=" + maxAvailDay
                    + " today=" + today
                    + " selectedDay=" + selectedDay + "}";
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
