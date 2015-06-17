package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.BlankDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Rey on 12/31/2014.
 */
public class DatePicker extends ListView implements AbsListView.OnScrollListener{

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

    private Typeface mTypeface = Typeface.DEFAULT;
    private int mTextSize = -1;
    private int mTextColor = 0xFF000000;
    private int mTextLabelColor = 0xFF767676;
    private int mTextHighlightColor = 0xFFFFFFFF;
    private int mTextDisableColor;
    private int mSelectionColor;
    private int mAnimDuration = -1;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;

    private Paint mPaint;
    private float mDayBaseWidth;
    private float mDayBaseHeight;
    private float mDayHeight;
    private float mDayWidth;
    private int mDayPadding;
    private float mSelectionRadius;
    private int mMonthRealWidth;
    private int mMonthRealHeight;

    private Calendar mCalendar;
    private int mFirstDayOfWeek;
    private String[] mLabels = new String[7];
    private static String[] mDayTexts;

    private MonthAdapter mAdapter;

    /**
     * Interface definition for a callback to be invoked when the selected date is changed.
     */
    public interface OnDateChangedListener {

        /**
         * Called when the selected date is changed.
         * @param oldDay The day value of old date.
         * @param oldMonth The month value of old date.
         * @param oldYear The year value of old date.
         * @param newDay The day value of new date.
         * @param newMonth The month value of new date.
         * @param newYear The year value of new date.
         */
        public void onDateChanged(int oldDay, int oldMonth, int oldYear, int newDay, int newMonth, int newYear);
    }

    private OnDateChangedListener mOnDateChangedListener;

    protected static final int SCROLL_DURATION = 250;
    protected static final int SCROLL_CHANGE_DELAY = 40;
    protected static final int LIST_TOP_OFFSET = -1;

    protected Handler mHandler = new Handler();

    protected int mCurrentScrollState = 0;
    protected long mPreviousScrollPosition;
    protected int mPreviousScrollState = 0;
    protected float mFriction = 1.0F;
    protected ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private static final String DAY_FORMAT = "%2d";
    private static final String YEAR_FORMAT = "%4d";

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
        setWillNotDraw(false);
        setSelector(BlankDrawable.getInstance());
        setCacheColorHint(0);
        setDivider(null);
        setItemsCanFocus(true);
        setFastScrollEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOnScrollListener(this);
        setFadingEdgeLength(0);
        setFrictionIfSupported(ViewConfiguration.getScrollFriction() * mFriction);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mDayPadding = ThemeUtil.dpToPx(context, 4);

        mSelectionColor = ThemeUtil.colorPrimary(context, 0xFF000000);

        mCalendar = Calendar.getInstance();
        mFirstDayOfWeek = mCalendar.getFirstDayOfWeek();

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

    @Override
    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super.applyStyle(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);

        String familyName = null;
        int style = -1;

        int padding = -1;
        int paddingLeft = -1;
        int paddingRight = -1;
        int paddingTop = -1;
        int paddingBottom = -1;
        boolean paddingDefined = false;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.DatePicker_dp_dayTextSize)
                mTextSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_textColor)
                mTextColor = a.getColor(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_textHighlightColor)
                mTextHighlightColor = a.getColor(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_textLabelColor)
                mTextLabelColor = a.getColor(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_textDisableColor)
                mTextDisableColor = a.getColor(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_selectionColor)
                mSelectionColor = a.getColor(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_animDuration)
                mAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.DatePicker_dp_inInterpolator)
                mInInterpolator = AnimationUtils.loadInterpolator(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.DatePicker_dp_outInterpolator)
                mOutInterpolator = AnimationUtils.loadInterpolator(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.DatePicker_dp_fontFamily)
                familyName = a.getString(attr);
            else if(attr == R.styleable.DatePicker_dp_textStyle)
                style = a.getInteger(attr, 0);
            else if(attr == R.styleable.DatePicker_android_padding) {
                padding = a.getDimensionPixelSize(attr, 0);
                paddingDefined = true;
            }
            else if(attr == R.styleable.DatePicker_android_paddingLeft) {
                paddingLeft = a.getDimensionPixelSize(attr, 0);
                paddingDefined = true;
            }
            else if(attr == R.styleable.DatePicker_android_paddingTop) {
                paddingTop = a.getDimensionPixelSize(attr, 0);
                paddingDefined = true;
            }
            else if(attr == R.styleable.DatePicker_android_paddingRight) {
                paddingRight = a.getDimensionPixelSize(attr, 0);
                paddingDefined = true;
            }
            else if(attr == R.styleable.DatePicker_android_paddingBottom) {
                paddingBottom = a.getDimensionPixelSize(attr, 0);
                paddingDefined = true;
            }
        }

        if(mTextSize < 0)
            mTextSize = context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_caption_material);

        if(mAnimDuration < 0)
             mAnimDuration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        if(mInInterpolator == null)
            mInInterpolator = new DecelerateInterpolator();

        if(mOutInterpolator == null)
            mOutInterpolator = new DecelerateInterpolator();

        if(familyName != null || style >= 0)
            mTypeface = TypefaceUtil.load(context, familyName, style);

        a.recycle();

        if(paddingDefined){
            if(padding >= 0)
                setContentPadding(padding, padding, padding, padding);

            if(paddingLeft >= 0)
                mPaddingLeft = paddingLeft;

            if(paddingTop >= 0)
                mPaddingTop = paddingTop;

            if(paddingRight >= 0)
                mPaddingRight = paddingRight;

            if(paddingBottom >= 0)
                mPaddingBottom = paddingBottom;
        }

        requestLayout();
        mAdapter.notifyDataSetInvalidated();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setFrictionIfSupported(float friction) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setFriction(friction);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        MonthView child = (MonthView) view.getChildAt(0);
        if (child == null)
            return;

        // Figure out where we are
        mPreviousScrollPosition = getFirstVisiblePosition() * child.getHeight() - child.getBottom();
        mPreviousScrollState = mCurrentScrollState;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scroll) {
        mScrollStateChangedRunnable.doScrollStateChange(absListView, scroll);
    }

    private void measureBaseSize(){
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        mDayBaseWidth = mPaint.measureText("88", 0, 2) + mDayPadding * 2;

        Rect bounds = new Rect();
        mPaint.getTextBounds("88", 0, 2 ,bounds);
        mDayBaseHeight = bounds.height();
    }

    private void measureMonthView(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureBaseSize();

        int size = Math.round(Math.max(mDayBaseWidth, mDayBaseHeight));

        int width = size * 7 + mPaddingLeft + mPaddingRight;
        int height = Math.round(size * 7 + mDayBaseHeight + mDayPadding * 2 + mPaddingTop + mPaddingBottom);

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

        mMonthRealWidth = width;
        mMonthRealHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureMonthView(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDayWidth = (w - mPaddingLeft - mPaddingRight) / 7f;
        mDayHeight = (h - mDayBaseHeight - mDayPadding * 2 - mPaddingTop - mPaddingBottom) / 7f;
        mSelectionRadius = Math.min(mDayWidth, mDayHeight) / 2f;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    public void setContentPadding(int left, int top, int right, int bottom){
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    private String getDayText(int day){
        if(mDayTexts == null){
            synchronized (DatePicker.class){
                if(mDayTexts == null)
                    mDayTexts = new String[31];
            }
        }

        if(mDayTexts[day - 1] == null)
            mDayTexts[day - 1] = String.format(DAY_FORMAT, day);

        return mDayTexts[day - 1];
    }

    /**
     * Set the range of selectable dates.
     * @param minDay The day value of minimum date.
     * @param minMonth The month value of minimum date.
     * @param minYear The year value of minimum date.
     * @param maxDay The day value of maximum date.
     * @param maxMonth The month value of maximum date.
     * @param maxYear The year value of maximum date.
     */
    public void setDateRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
        mAdapter.setDateRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
    }

    /**
     * Jump to the view of a specific month.
     * @param month
     * @param year
     */
    public void goTo(int month, int year){
        int position = mAdapter.positionOfMonth(month, year);
        postSetSelectionFromTop(position, 0);
    }

    public void postSetSelectionFromTop(final int position, final int offset) {
        post(new Runnable() {
            @Override
            public void run() {
                setSelectionFromTop(position, offset);
                requestLayout();
            }
        });
    }

    /**
     * Set the selected date of this DatePicker.
     * @param day The day value of selected date.
     * @param month The month value of selected date.
     * @param year The year value of selected date.
     */
    public void setDate(int day, int month, int year){
        if(mAdapter.getYear() == year && mAdapter.getMonth() == month && mAdapter.getDay() == day)
            return;

        mAdapter.setDate(day, month, year, false);
        goTo(month, year);
    }

    /**
     * Set the listener will be called when the selected date is changed.
     * @param listener The {@link DatePicker.OnDateChangedListener} will be called.
     */
    public void setOnDateChangedListener(OnDateChangedListener listener){
        mOnDateChangedListener = listener;
    }

    /**
     * @return The day value of selected date.
     */
    public int getDay(){
        return mAdapter.getDay();
    }

    /**
     * @return The month value of selected date.
     */
    public int getMonth(){
        return mAdapter.getMonth();
    }

    /**
     * @return The year value of selected date.
     */
    public int getYear(){
        return mAdapter.getYear();
    }

    /**
     * Get the formatted string of selected date.
     * @param formatter The DateFormat used to format the date.
     * @return
     */
    public String getFormattedDate(DateFormat formatter){
        mCalendar.set(Calendar.YEAR, mAdapter.getYear());
        mCalendar.set(Calendar.MONTH, mAdapter.getMonth());
        mCalendar.set(Calendar.DAY_OF_MONTH, mAdapter.getDay());
        return formatter.format(mCalendar.getTime());
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

    public Calendar getCalendar(){
        return mCalendar;
    }

    private class ScrollStateRunnable implements Runnable {
        private int mNewState;

        /**
         * Sets up the runnable with a short delay in case the scroll state
         * immediately changes again.
         *
         * @param view The list view that changed state
         * @param scrollState The new state it changed to
         */
        public void doScrollStateChange(AbsListView view, int scrollState) {
            mHandler.removeCallbacks(this);
            mNewState = scrollState;
            mHandler.postDelayed(this, SCROLL_CHANGE_DELAY);
        }

        @Override
        public void run() {
            mCurrentScrollState = mNewState;
            // Fix the position after a scroll or a fling ends
            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mPreviousScrollState = mNewState;

                int i = 0;
                View child = getChildAt(i);
                while(child != null && child.getBottom() <= 0)
                    child = getChildAt(++i);
                if (child == null)
                    return;

                int firstPosition = getFirstVisiblePosition();
                int lastPosition = getLastVisiblePosition();
                boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
                final int top = child.getTop();
                final int bottom = child.getBottom();
                final int midpoint = getHeight() / 2;
                if (scroll && top < LIST_TOP_OFFSET) {
                    if (bottom > midpoint)
                        smoothScrollBy(top, SCROLL_DURATION);
                    else
                        smoothScrollBy(bottom, SCROLL_DURATION);
                }
            }
            else
                mPreviousScrollState = mNewState;
        }
    }

    private class MonthView extends View {

        private long mStartTime;
        private float mAnimProgress;
        private boolean mRunning;

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
            int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
            mFirstDayCol = dayOfWeek < mFirstDayOfWeek ? dayOfWeek + 7 - mFirstDayOfWeek : dayOfWeek - mFirstDayOfWeek;
            mMonthText = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + String.format(YEAR_FORMAT, mYear);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mMonthRealWidth, mMonthRealHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //draw month text
            mPaint.setTextSize(mTextSize);
            mPaint.setTypeface(mTypeface);
            float x = 3.5f * mDayWidth + getPaddingLeft();
            float y = mDayPadding * 2 + mDayBaseHeight + getPaddingTop();
            mPaint.setFakeBoldText(true);
            mPaint.setColor(mTextColor);
            canvas.drawText(mMonthText, x, y, mPaint);

            //draw selection
            float paddingLeft = getPaddingLeft();
            float paddingTop = mDayPadding * 2 + mDayBaseHeight + getPaddingTop();
            if(mSelectedDay > 0){
                int col = (mFirstDayCol + mSelectedDay - 1) % 7;
                int row = (mFirstDayCol + mSelectedDay - 1) / 7 + 1;

                x = (col + 0.5f) * mDayWidth + paddingLeft;
                y = (row + 0.5f) * mDayHeight + paddingTop;
                float radius = mRunning ? mInInterpolator.getInterpolation(mAnimProgress) * mSelectionRadius : mSelectionRadius;
                mPaint.setColor(mSelectionColor);
                canvas.drawCircle(x, y, radius, mPaint);
            }

            if(mRunning && mPreviousSelectedDay > 0){
                int col = (mFirstDayCol + mPreviousSelectedDay - 1) % 7;
                int row = (mFirstDayCol + mPreviousSelectedDay - 1) / 7 + 1;

                x = (col + 0.5f) * mDayWidth + paddingLeft;
                y = (row + 0.5f) * mDayHeight + paddingTop;
                float radius = (1f - mOutInterpolator.getInterpolation(mAnimProgress)) * mSelectionRadius;
                mPaint.setColor(mSelectionColor);
                canvas.drawCircle(x, y, radius, mPaint);
            }

            //draw label
            mPaint.setFakeBoldText(false);
            mPaint.setColor(mTextLabelColor);
            paddingTop += (mDayHeight + mDayBaseHeight) / 2f;
            for(int i = 0; i < 7; i++){
                x = (i + 0.5f) * mDayWidth + paddingLeft;
                y = paddingTop;
                int index = (i + mFirstDayOfWeek - 1) % 7;
                canvas.drawText(mLabels[index], x, y, mPaint);
            }

            //draw date text
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

                x = (col + 0.5f) * mDayWidth + paddingLeft;
                y = row * mDayHeight + paddingTop;

                canvas.drawText(getDayText(day), x, y, mPaint);
                col++;
                if(col == 7) {
                    col = 0;
                    row++;
                }
            }
        }

        private int getTouchedDay(float x, float y){
            float paddingTop = mDayPadding * 2 + mDayBaseHeight + getPaddingTop() + mDayHeight;
            if(x < getPaddingLeft() || x > getWidth() - getPaddingRight() || y < paddingTop || y > getHeight() - getPaddingBottom())
                return -1;

            int col = (int)Math.floor((x - getPaddingLeft()) / mDayWidth);
            int row = (int)Math.floor((y - paddingTop) / mDayHeight);
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
                    return true;
                case MotionEvent.ACTION_UP:
                    if(getTouchedDay(event.getX(), event.getY()) == mTouchedDay && mTouchedDay > 0) {
                        mAdapter.setDate(mTouchedDay, mMonth, mYear, true);
                        mTouchedDay = -1;
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    mTouchedDay = -1;
                    return true;
            }
            return true;
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

    }

    private class MonthAdapter extends BaseAdapter{
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

        public void setDateRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
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

        public void setDate(int day, int month, int year, boolean animation){
            if(mMonth != month || mYear != year) {
                MonthView v = (MonthView)getChildAt(positionOfMonth(mMonth, mYear) - getFirstVisiblePosition());
                if (v != null)
                    v.setSelectedDay(-1, false);

                int oldDay = mDay;
                int oldMonth = mMonth;
                int oldYear = mYear;

                mDay = day;
                mMonth = month;
                mYear = year;

                v = (MonthView)getChildAt(positionOfMonth(mMonth, mYear) - getFirstVisiblePosition());
                if(v != null)
                    v.setSelectedDay(mDay, animation);

                if(mOnDateChangedListener != null)
                    mOnDateChangedListener.onDateChanged(oldDay, oldMonth, oldYear, mDay, mMonth, mYear);
            }
            else if(day != mDay){
                int oldDay = mDay;

                mDay = day;

                MonthView v = (MonthView)getChildAt(positionOfMonth(mMonth, mYear) - getFirstVisiblePosition());
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
        public Object getItem(int position) {
            return position + mMinMonthValue;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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

            return v;
        }
    }
}
