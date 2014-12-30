package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Stack;

/**
 * Created by Rey on 12/30/2014.
 */
public class DatePicker extends ViewPager{

    private Typeface mTypeface;
    private int mTextSize;
    private int mTextColor;
    private int mTextLabelColor;
    private int mTextHighlightColor;
    private int mTextDisableColor;
    private int mSelectionColor;
    private int mAnimDuration;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;

    private Calendar mCalendar;
    private boolean mIsMondayFirst;
    private String[] mLabels = new String[7];

    private static String[] mDayTexts;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private MonthAdapter mAdapter;

    public interface OnDateChangedListener {
        public void onDateChanged(int oldDay, int oldMonth, int oldYear, int newDay, int newMonth, int newYear);
    }

    private OnDateChangedListener mOnDateChangedListener;

    public DatePicker(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        init(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mCalendar = Calendar.getInstance();
        mIsMondayFirst = mCalendar.getFirstDayOfWeek() == Calendar.MONDAY;

        int index = mCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        DateFormat format = new SimpleDateFormat(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? "EEEEE" : "E");
        for(int i = 0; i < 7; i++){
            mLabels[index] = format.format(mCalendar.getTime());
            index = (index + 1) % 7;
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        mAdapter = new MonthAdapter();
        setAdapter(mAdapter);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        mTextSize = a.getDimensionPixelSize(R.styleable.DatePicker_dp_dayTextSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_caption_material));
        mTextColor = a.getColor(R.styleable.DatePicker_dp_textColor, 0xFF000000);
        mTextHighlightColor = a.getColor(R.styleable.DatePicker_dp_textHighlightColor, 0xFFFFFFFF);
        mTextLabelColor = a.getColor(R.styleable.DatePicker_dp_textLabelColor, 0xFF767676);
        mTextDisableColor = a.getColor(R.styleable.DatePicker_dp_textDisableColor, 0xFF767676);
        mSelectionColor = a.getColor(R.styleable.DatePicker_dp_selectionColor, ThemeUtil.colorPrimary(context, 0xFF000000));
        mAnimDuration = a.getInteger(R.styleable.DatePicker_dp_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        int resId = a.getResourceId(R.styleable.DatePicker_dp_inInterpolator, 0);
        if(resId != 0)
            mInInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mInInterpolator = new DecelerateInterpolator();
        resId = a.getResourceId(R.styleable.DatePicker_dp_outInterpolator, 0);
        if(resId != 0)
            mOutInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mOutInterpolator = new DecelerateInterpolator();
        String familyName = a.getString(R.styleable.DatePicker_android_fontFamily);
        int style = a.getInteger(R.styleable.DatePicker_android_textStyle, Typeface.NORMAL);

        mTypeface = TypefaceUtil.load(context, familyName, style);
        a.recycle();
    }

    private String getDayText(int day){
        if(mDayTexts == null){
            synchronized (DatePicker.class){
                if(mDayTexts == null)
                    mDayTexts = new String[31];
            }
        }

        if(mDayTexts[day - 1] == null)
            mDayTexts[day - 1] = String.valueOf(day);

        return mDayTexts[day - 1];
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && getLayoutDirection() == LAYOUT_DIRECTION_RTL)
            setPadding(end, top, start, bottom);
        else
            setPadding(start, top, end, bottom);
    }

    public void setDayRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
        mAdapter.setDayRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
    }

    public void setDay(int day, int month, int year){
        mAdapter.setDay(day, month, year, false);
        setCurrentItem(mAdapter.positionOfMonth(month, year));
    }

    public void setOnDateChangedListener(OnDateChangedListener listener){
        mOnDateChangedListener = listener;
    }

    public int getDay(){
        return mAdapter.getDay();
    }

    public int getMonth(){
        return mAdapter.getMonth();
    }

    public int getYear(){
        return mAdapter.getYear();
    }

    public int getSelectionColor(){
        return mSelectionColor;
    }

    public int getTextSize(){
        return mTextSize;
    }

    public Typeface getTypeface(){
        return mTypeface;
    }

    public int getTextColor(){
        return mTextColor;
    }

    public int getTextLabelColor(){
        return mTextLabelColor;
    }

    public int getTextHighlightColor(){
        return mTextHighlightColor;
    }

    public int getTextDisableColor(){
        return mTextDisableColor;
    }

    private class MonthView extends View {

        private long mStartTime;
        private float mAnimProgress;
        private boolean mRunning;

        private Paint mPaint;
        private float mBaseWidth;
        private float mBaseHeight;
        private float mRowHeight;
        private float mColWidth;
        private int mPadding;
        private float mSelectionRadius;

        private int mTouchedDay = -1;

        private int mMonth;
        private int mYear;
        private int mMaxDay;
        private int mFirstDayCol;
        private int mMinAvailDay = -1;
        private int mMaxAvailDay = -1;
        private int mToday = -1;
        private int mSelectedDay = -1;
        private int mPreviousSelectedDay = -1;
        private String mMonthText;

        public MonthView(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextAlign(Paint.Align.CENTER);

            mPadding = ThemeUtil.dpToPx(context, 4);
            setWillNotDraw(false);
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

        private void calculateMonthView(){
            mCalendar.set(Calendar.DAY_OF_MONTH, 1);
            mCalendar.set(Calendar.MONTH, mMonth);
            mCalendar.set(Calendar.YEAR, mYear);

            mMaxDay = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            mFirstDayCol = mCalendar.get(Calendar.DAY_OF_WEEK) - 1;
            if(mIsMondayFirst)
                mFirstDayCol = mFirstDayCol == 0 ? 6 : mFirstDayCol - 1;
            mMonthText = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + mYear;
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
            int maxDay = mMaxAvailDay > 0 ? Math.min(mMaxAvailDay, mMaxDay) : mMaxDay;
            for(int day = 1; day <= mMaxDay; day++){
                if(day == mSelectedDay)
                    mPaint.setColor(mTextHighlightColor);
                else if(day < mMinAvailDay || day > maxDay)
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
            int maxDay = mMaxAvailDay > 0 ? Math.min(mMaxAvailDay, mMaxDay) : mMaxDay;

            int day = row * 7 + col - mFirstDayCol + 1;
            if(day < 0 || day < mMinAvailDay || day > maxDay)
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
                        mAdapter.setDay(mTouchedDay, mMonth, mYear, true);
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

    }

    private class MonthAdapter extends RecyclerPagerAdapter{
        private int mDay = -1;
        private int mMonth = -1;
        private int mYear = -1;
        private int mMinDay = -1;
        private int mMinMonth = -1;
        private int mMinYear = -1;
        private int mMaxDay = -1;
        private int mMaxMonth = -1;
        private int mMaxYear = -1;
        private int mToday;
        private int mTodayMonth;
        private int mTodayYear;
        private int mMinMonthValue;
        private int mMaxMonthValue;

        public void setDayRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
            int minMonthValue = minDay < 0 || minMonth < 0 || minYear < 0 ? 0 : minYear * 12 + minMonth;
            int maxMonthValue = maxDay < 0 || maxMonth < 0 || maxYear < 0 ? Integer.MAX_VALUE - 1: maxYear * 12 + maxMonth;

            if(minDay != mMinDay || mMinMonthValue != minMonthValue || maxDay != mMaxDay || mMaxMonthValue != maxMonthValue){
                mMinDay = minDay;
                mMinMonth = minMonth;
                mMinYear = minYear;

                mMaxDay = maxDay;
                mMaxMonth = maxMonth;
                mMaxYear = maxYear;

                mMinMonthValue = minMonthValue;
                mMaxMonthValue = maxMonthValue;
                notifyDataSetChanged();
            }
        }

        public void setDay(int day, int month, int year, boolean animation){
            if(mMonth != month || mYear != year) {
                MonthView v = getView(positionOfMonth(mMonth, mYear));
                if (v != null)
                    v.setSelectedDay(-1, false);

                int oldDay = mDay;
                int oldMonth = mMonth;
                int oldYear = mYear;

                mDay = day;
                mMonth = month;
                mYear = year;

                v = getView(positionOfMonth(mMonth, mYear));
                if(v != null)
                    v.setSelectedDay(mDay, animation);

                if(mOnDateChangedListener != null)
                    mOnDateChangedListener.onDateChanged(oldDay, oldMonth, oldYear, mDay, mMonth, mYear);
            }
            else if(day != mDay){
                int oldDay = mDay;

                mDay = day;

                MonthView v = getView(positionOfMonth(mMonth, mYear));
                if(v != null)
                    v.setSelectedDay(mDay, animation);

                if(mOnDateChangedListener != null)
                    mOnDateChangedListener.onDateChanged(oldDay, mMonth, mYear, mDay, mMonth, mYear);
            }
        }

        public int positionOfMonth(int month, int year){
            return year * 12 + month - mMinMonthValue;
        }

        public int getDay(){
            return mDay;
        }

        public int getMonth(){
            return mMonth;
        }

        public int getYear(){
            return mYear;
        }

        private MonthView getView(int position){
            for(int i = getChildCount() - 1; i >= 0; i--){
                View v = getChildAt(i);
                if((Integer)v.getTag(RecyclerPagerAdapter.TAG) == position)
                    return (MonthView)v;
            }

            return null;
        }

        private void calToday(){
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mToday = mCalendar.get(Calendar.DAY_OF_MONTH);
            mTodayMonth = mCalendar.get(Calendar.MONTH);
            mTodayYear = mCalendar.get(Calendar.YEAR);
        }

        @Override
        public int getCount() {
            return mMaxMonthValue - mMinMonthValue + 1;
        }

        @Override
        protected Object getItem(int position) {
            return position + mMinMonthValue;
        }

        @Override
        protected View getView(int position, View convertView, ViewGroup parent) {
            MonthView v = (MonthView)convertView;
            if(v == null){
                v = new MonthView(parent.getContext());
                v.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
            }

            calToday();
            int monthValue = (Integer)getItem(position);
            int year = monthValue / 12;
            int month = monthValue % 12;
            int minDay = month == mMinMonth && year == mMinYear ? mMinDay : -1;
            int maxDay = month == mMaxMonth && year == mMaxYear ? mMaxDay : -1;
            int today = mTodayMonth == month && mTodayYear == year ?  mToday : -1;
            int day = month == mMonth && year == mYear ? mDay : -1;

            v.setMonth(month, year);
            v.setToday(today);
            v.setAvailableDay(minDay, maxDay);
            v.setSelectedDay(day, false);

            System.out.println("get: " + position + " " + v);

            return v;
        }

    }

    public static abstract class RecyclerPagerAdapter extends PagerAdapter {

        ArrayList<Integer> mInstantiatedPositions = new ArrayList<>();

        private Stack<Reference<View>> mRecycler = new Stack<>();

        public static final int TAG = R.id.action_bar;

        private void putViewToRecycler(View v){
            synchronized (mRecycler){
                mRecycler.push(new WeakReference<View>(v));
            }
        }

        private View getViewFromRecycler(){
            View v = null;
            synchronized (mRecycler) {
                while (v == null && !mRecycler.isEmpty())
                    v = mRecycler.pop().get();
            }
            return v;
        }

        @Override
        public final void startUpdate(ViewGroup container) {
            mInstantiatedPositions.clear();
        }

        @Override
        public final Object instantiateItem(ViewGroup container, int position) {
            mInstantiatedPositions.add(position);
            return position;
        }

        @Override
        public final void destroyItem(ViewGroup container, int position, Object object) {
            //remove view attached to this object and put it to recycler.
            System.out.println("destroy: " + position + " " + object);
            for(int i = container.getChildCount() - 1; i >= 0; i --){
                View v = container.getChildAt(i);
                System.out.println(v + " " + v.getTag(TAG) + "  " + isViewFromObject(v, object));
                if(isViewFromObject(v, object)){
                    container.removeView(v);
                    putViewToRecycler(v);
                    System.out.println("destroyed: " + position + " " + v);
                    break;
                }
            }
        }

        @Override
        public final void finishUpdate(ViewGroup container) {
            // Render views and attach them to the container. Page views are reused
            // whenever possible.
            for (Integer pos : mInstantiatedPositions) {
                View convertView = getViewFromRecycler();

                if (convertView != null) {
                    // Re-add existing view before rendering so that we can make change inside getView()
                    container.addView(convertView);
                    convertView = getView(pos, convertView, container);
                } else {
                    convertView = getView(pos, convertView, container);
                    container.addView(convertView);
                }

                convertView.requestLayout();
                convertView.invalidate();
                convertView.forceLayout();

                // Set another tag id to not break ViewHolder pattern
                convertView.setTag(TAG, pos);
            }

            mInstantiatedPositions.clear();
        }

        @Override
        public final boolean isViewFromObject(View view, Object object) {
            return view.getTag(TAG) != null && ((Integer)view.getTag(TAG)).intValue() == ((Integer)object).intValue();
        }

        protected abstract Object getItem(int position);

        protected abstract View getView(int position, View convertView, ViewGroup parent);

    }
}
