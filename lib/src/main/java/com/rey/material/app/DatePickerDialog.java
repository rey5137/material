package com.rey.material.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.DatePicker;
import com.rey.material.widget.YearPicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Rey on 12/30/2014.
 */
public class DatePickerDialog extends Dialog {

    private DatePickerLayout mDatePickerLayout;
    private float mCornerRadius;

    /**
     * Interface definition for a callback to be invoked when the selected date is changed.
     */
    public interface OnDateChangedListener{

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

    public DatePickerDialog(Context context) {
        super(context, R.style.Material_App_Dialog_DatePicker_Light);
    }

    public DatePickerDialog(Context context, int style) {
        super(context, style);
    }

    @Override
    protected void onCreate() {
        mDatePickerLayout = new DatePickerLayout(getContext());
        contentView(mDatePickerLayout);
    }

    @Override
    public Dialog applyStyle(int resId) {
        super.applyStyle(resId);

        if(resId == 0)
            return this;

        mDatePickerLayout.applyStyle(resId);
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

    /**
     * Set the range of selectable dates.
     * @param minDay The day value of minimum date.
     * @param minMonth The month value of minimum date.
     * @param minYear The year value of minimum date.
     * @param maxDay The day value of maximum date.
     * @param maxMonth The month value of maximum date.
     * @param maxYear The year value of maximum date.
     * @return The DatePickerDialog for chaining methods.
     */
    public DatePickerDialog dateRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
        mDatePickerLayout.setDateRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
        return this;
    }

    /**
     * Set the range of selectable dates.
     * @param minTime The minimum date in milis.
     * @param maxTime The maximum date in milis
     * @return The DatePickerDialog for chaining methods.
     */
    public DatePickerDialog dateRange(long minTime, long maxTime){
        mDatePickerLayout.setDateRange(minTime, maxTime);
        return this;
    }

    /**
     * Set the selected date of this DatePickerDialog.
     * @param day The day value of selected date.
     * @param month The month value of selected date.
     * @param year The year value of selected date.
     * @return The DatePickerDialog for chaining methods.
     */
    public DatePickerDialog date(int day, int month, int year){
        mDatePickerLayout.setDate(day, month, year);
        return this;
    }

    /**
     * Set the selected date of this DatePickerDialog.
     * @param time The date in milis.
     * @return The DatePickerDialog for chaining methods.
     */
    public DatePickerDialog date(long time){
        mDatePickerLayout.setDate(time);
        return this;
    }

    /**
     * Set the listener will be called when the selected date is changed.
     * @param listener The {@link DatePickerDialog.OnDateChangedListener} will be called.
     * @return The DatePickerDialog for chaining methods.
     */
    public DatePickerDialog onDateChangedListener(OnDateChangedListener listener){
        mOnDateChangedListener = listener;
        return this;
    }

    /**
     * @return The day value of selected date.
     */
    public int getDay(){
        return mDatePickerLayout.getDay();
    }

    /**
     * @return The month value of selected date.
     */
    public int getMonth(){
        return mDatePickerLayout.getMonth();
    }

    /**
     * @return The year value of selected date.
     */
    public int getYear(){
        return mDatePickerLayout.getYear();
    }

    /**
     * @return The selected date.
     */
    public long getDate(){
        Calendar cal = getCalendar();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.DAY_OF_MONTH, getDay());
        cal.set(Calendar.MONTH, getMonth());
        cal.set(Calendar.YEAR, getYear());
        return cal.getTimeInMillis();
    }

    public Calendar getCalendar(){
        return mDatePickerLayout.getCalendar();
    }

    /**
     * Get the formatted string of selected date.
     * @param formatter The DateFormat used to format the date.
     * @return
     */
    public String getFormattedDate(DateFormat formatter){
        return mDatePickerLayout.getFormattedDate(formatter);
    }

    private class DatePickerLayout extends FrameLayout implements DatePicker.OnDateChangedListener, YearPicker.OnYearChangedListener {

        private YearPicker mYearPicker;
        private DatePicker mDatePicker;

        private int mHeaderPrimaryHeight;
        private int mHeaderPrimaryColor;
        private int mHeaderSecondaryHeight;
        private int mHeaderSecondaryColor;
        private int mHeaderPrimaryTextSize;
        private int mHeaderSecondaryTextSize;
        private int mTextHeaderColor = 0xFF000000;

        private Paint mPaint;
        private int mHeaderPrimaryRealHeight;
        private int mHeaderRealWidth;
        private RectF mRect;
        private Path mHeaderSecondaryBackground;

        private int mPadding;

        private boolean mDaySelectMode = true;

        private boolean mMonthFirst = true;
        private boolean mLocationDirty = true;

        private String mWeekDay;
        private String mMonth;
        private String mDay;
        private String mYear;

        private float mBaseX;
        private float mWeekDayY;
        private float mMonthY;
        private float mDayY;
        private float mYearY;
        private float mFirstWidth;
        private float mCenterY;
        private float mSecondWidth;

        private static final String BASE_TEXT = "0";
        private static final String DAY_FORMAT = "%2d";
        private static final String YEAR_FORMAT = "%4d";

        public DatePickerLayout(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mRect = new RectF();
            mHeaderSecondaryBackground = new Path();
            mPadding = ThemeUtil.dpToPx(context, 8);

            mYearPicker = new YearPicker(context);
            mDatePicker = new DatePicker(context);
            mYearPicker.setPadding(mPadding, mPadding, mPadding, mPadding);
            mYearPicker.setOnYearChangedListener(this);
            mDatePicker.setContentPadding(mPadding, mPadding, mPadding, mPadding);
            mDatePicker.setOnDateChangedListener(this);

            addView(mDatePicker);
            addView(mYearPicker);

            mYearPicker.setVisibility(mDaySelectMode ? View.GONE : View.VISIBLE);
            mDatePicker.setVisibility(mDaySelectMode ? View.VISIBLE : View.GONE);

            mMonthFirst = isMonthFirst();

            setWillNotDraw(false);

            mHeaderPrimaryHeight = ThemeUtil.dpToPx(context, 144);
            mHeaderSecondaryHeight = ThemeUtil.dpToPx(context, 32);
            mHeaderPrimaryTextSize = context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_display_2_material);
            mHeaderSecondaryTextSize = context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_headline_material);
        }

        private boolean isMonthFirst(){
            SimpleDateFormat format = (SimpleDateFormat)SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL);
            String pattern = format.toLocalizedPattern();

            return pattern.indexOf("M") < pattern.indexOf("d");
        }

        public void setDateSelectMode(boolean enable){
            if(mDaySelectMode != enable){
                mDaySelectMode = enable;

                if(mDaySelectMode) {
                    mDatePicker.goTo(mDatePicker.getMonth(), mDatePicker.getYear());
                    animOut(mYearPicker);
                    animIn(mDatePicker);
                }
                else {
                    mYearPicker.goTo(mYearPicker.getYear());
                    animOut(mDatePicker);
                    animIn(mYearPicker);
                }

                invalidate(0, 0, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight);
            }
        }

        private void animOut(final View v){
            Animation anim = new AlphaAnimation(1f, 0f);
            anim.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            v.startAnimation(anim);
        }

        private void animIn(final View v){
            Animation anim = new AlphaAnimation(0f, 1f);
            anim.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            v.startAnimation(anim);
        }

        public void applyStyle(int resId){
            mYearPicker.applyStyle(resId);
            mDatePicker.applyStyle(resId);

            mHeaderPrimaryColor = mDatePicker.getSelectionColor();
            mHeaderSecondaryColor = mHeaderPrimaryColor;

            Context context = getContext();
            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.DatePickerDialog);

            for(int i = 0, count = a.getIndexCount(); i < count; i++){
                int attr = a.getIndex(i);

                if(attr == R.styleable.DatePickerDialog_dp_headerPrimaryHeight)
                    mHeaderPrimaryHeight = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_headerSecondaryHeight)
                    mHeaderSecondaryHeight = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_headerPrimaryColor)
                    mHeaderPrimaryColor = a.getColor(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_headerSecondaryColor)
                    mHeaderSecondaryColor = a.getColor(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_headerPrimaryTextSize)
                    mHeaderPrimaryTextSize = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_headerSecondaryTextSize)
                    mHeaderSecondaryTextSize = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.DatePickerDialog_dp_textHeaderColor)
                    mTextHeaderColor = a.getColor(attr, 0);
            }

            a.recycle();

            mPaint.setTypeface(mDatePicker.getTypeface());
        }

        public void setDateRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
            mDatePicker.setDateRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
            mYearPicker.setYearRange(minYear, maxYear);
        }

        public void setDateRange(long minTime, long maxTime){
            Calendar cal = mDatePicker.getCalendar();
            cal.setTimeInMillis(minTime);
            int minDay = cal.get(Calendar.DAY_OF_MONTH);
            int minMonth = cal.get(Calendar.MONTH);
            int minYear = cal.get(Calendar.YEAR);
            cal.setTimeInMillis(maxTime);
            int maxDay = cal.get(Calendar.DAY_OF_MONTH);
            int maxMonth = cal.get(Calendar.MONTH);
            int maxYear = cal.get(Calendar.YEAR);

            setDateRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
        }

        public void setDate(int day, int month, int year){
            mDatePicker.setDate(day, month, year);
        }

        public void setDate(long time){
            Calendar cal = mDatePicker.getCalendar();
            cal.setTimeInMillis(time);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            mDatePicker.setDate(day, month, year);
        }

        public int getDay(){
            return mDatePicker.getDay();
        }

        public int getMonth(){
            return mDatePicker.getMonth();
        }

        public int getYear(){
            return mDatePicker.getYear();
        }

        public String getFormattedDate(DateFormat formatter){
            return mDatePicker.getFormattedDate(formatter);
        }

        public Calendar getCalendar(){
            return mDatePicker.getCalendar();
        }

        @Override
        public void onYearChanged(int oldYear, int newYear) {
            if(!mDaySelectMode)
                mDatePicker.setDate(mDatePicker.getDay(), mDatePicker.getMonth(), newYear);
        }

        @Override
        public void onDateChanged(int oldDay, int oldMonth, int oldYear, int newDay, int newMonth, int newYear) {
            if(mDaySelectMode)
                mYearPicker.setYear(newYear);

            if(newDay < 0 || newMonth < 0 || newYear < 0){
                mWeekDay = null;
                mMonth = null;
                mDay = null;
                mYear = null;
            }
            else {
                Calendar cal = mDatePicker.getCalendar();
                cal.set(Calendar.YEAR, newYear);
                cal.set(Calendar.MONTH, newMonth);
                cal.set(Calendar.DAY_OF_MONTH, newDay);

                mWeekDay = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                mMonth = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
                mDay = String.format(DAY_FORMAT, newDay);
                mYear = String.format(YEAR_FORMAT, newYear);

                if(oldMonth != newMonth || oldYear != newYear)
                    mDatePicker.goTo(newMonth, newYear);
            }

            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight);

            if(mOnDateChangedListener != null)
                mOnDateChangedListener.onDateChanged(oldDay, oldMonth, oldYear, newDay, newMonth, newYear);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if(isPortrait){
                if(heightMode == MeasureSpec.AT_MOST){
                    int ws = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                    int hs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mDatePicker.measure(ws, hs);
                    mYearPicker.measure(ws, ws);
                }
                else{
                    int height = Math.max(heightSize - mHeaderSecondaryHeight - mHeaderPrimaryHeight, mDatePicker.getMeasuredHeight());
                    int ws = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                    mDatePicker.measure(ws, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                    mYearPicker.measure(ws, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    if(mYearPicker.getMeasuredHeight() != height)
                        mYearPicker.measure(ws, MeasureSpec.makeMeasureSpec(Math.min(mYearPicker.getMeasuredHeight(), height), MeasureSpec.EXACTLY));
                }

                setMeasuredDimension(widthSize, heightSize);
            }
            else{
                if(heightMode == MeasureSpec.AT_MOST){
                    int ws = MeasureSpec.makeMeasureSpec(widthSize / 2, MeasureSpec.EXACTLY);
                    int hs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mDatePicker.measure(ws, hs);
                    mYearPicker.measure(ws, ws);
                }
                else{
                    int height = Math.max(heightSize, mDatePicker.getMeasuredHeight());
                    int ws = MeasureSpec.makeMeasureSpec(widthSize / 2, MeasureSpec.EXACTLY);
                    mDatePicker.measure(ws, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                    mYearPicker.measure(ws, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    if(mYearPicker.getMeasuredHeight() != height)
                        mYearPicker.measure(ws, MeasureSpec.makeMeasureSpec(Math.min(mYearPicker.getMeasuredHeight(), height), MeasureSpec.EXACTLY));
                }

                setMeasuredDimension(widthSize, heightSize);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                mHeaderRealWidth = w;
                mHeaderPrimaryRealHeight = h - mHeaderSecondaryHeight - mDatePicker.getMeasuredHeight();
                mHeaderSecondaryBackground.reset();
                if(mCornerRadius == 0)
                    mHeaderSecondaryBackground.addRect(0, 0, mHeaderRealWidth, mHeaderSecondaryHeight, Path.Direction.CW);
                else{
                    mHeaderSecondaryBackground.moveTo(0, mHeaderSecondaryHeight);
                    mHeaderSecondaryBackground.lineTo(0, mCornerRadius);
                    mRect.set(0, 0, mCornerRadius * 2, mCornerRadius * 2);
                    mHeaderSecondaryBackground.arcTo(mRect, 180f, 90f, false);
                    mHeaderSecondaryBackground.lineTo(mHeaderRealWidth - mCornerRadius, 0);
                    mRect.set(mHeaderRealWidth - mCornerRadius * 2, 0, mHeaderRealWidth, mCornerRadius * 2);
                    mHeaderSecondaryBackground.arcTo(mRect, 270f, 90f, false);
                    mHeaderSecondaryBackground.lineTo(mHeaderRealWidth, mHeaderSecondaryHeight);
                    mHeaderSecondaryBackground.close();
                }
            }
            else{
                mHeaderRealWidth = w - mDatePicker.getMeasuredWidth();
                mHeaderPrimaryRealHeight = h - mHeaderSecondaryHeight;
                mHeaderSecondaryBackground.reset();
                if(mCornerRadius == 0)
                    mHeaderSecondaryBackground.addRect(0, 0, mHeaderRealWidth, mHeaderSecondaryHeight, Path.Direction.CW);
                else{
                    mHeaderSecondaryBackground.moveTo(0, mHeaderSecondaryHeight);
                    mHeaderSecondaryBackground.lineTo(0, mCornerRadius);
                    mRect.set(0, 0, mCornerRadius * 2, mCornerRadius * 2);
                    mHeaderSecondaryBackground.arcTo(mRect, 180f, 90f, false);
                    mHeaderSecondaryBackground.lineTo(mHeaderRealWidth, 0);
                    mHeaderSecondaryBackground.lineTo(mHeaderRealWidth, mHeaderSecondaryHeight);
                    mHeaderSecondaryBackground.close();
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

            if(isPortrait)
                childTop += mHeaderPrimaryRealHeight + mHeaderSecondaryHeight;
            else
                childLeft += mHeaderRealWidth;

            mDatePicker.layout(childLeft, childTop, childRight, childBottom);

            childTop = (childBottom + childTop - mYearPicker.getMeasuredHeight()) / 2;
            mYearPicker.layout(childLeft, childTop, childRight, childTop + mYearPicker.getMeasuredHeight());
        }

        private void measureHeaderText(){
            if(!mLocationDirty)
                return;

            if(mWeekDay == null){
                mLocationDirty = false;
                return;
            }

            mBaseX = mHeaderRealWidth / 2f;
            Rect bounds = new Rect();

            mPaint.setTextSize(mDatePicker.getTextSize());
            mPaint.getTextBounds(BASE_TEXT, 0, BASE_TEXT.length(), bounds);
            int height = bounds.height();
            mWeekDayY = (mHeaderSecondaryHeight + height) / 2f;

            mPaint.setTextSize(mHeaderPrimaryTextSize);
            mPaint.getTextBounds(BASE_TEXT, 0, BASE_TEXT.length(), bounds);
            int primaryTextHeight = bounds.height();
            if(mMonthFirst)
                mFirstWidth = mPaint.measureText(mDay, 0, mDay.length());
            else
                mFirstWidth = mPaint.measureText(mMonth, 0, mMonth.length());

            mPaint.setTextSize(mHeaderSecondaryTextSize);
            mPaint.getTextBounds(BASE_TEXT, 0, BASE_TEXT.length(), bounds);
            int secondaryTextHeight = bounds.height();
            if(mMonthFirst)
                mFirstWidth = Math.max(mFirstWidth, mPaint.measureText(mMonth, 0, mMonth.length()));
            else
                mFirstWidth = Math.max(mFirstWidth, mPaint.measureText(mDay, 0, mDay.length()));
            mSecondWidth = mPaint.measureText(mYear, 0, mYear.length());

            mCenterY = mHeaderSecondaryHeight + (mHeaderPrimaryRealHeight + primaryTextHeight) / 2f;
            float y = ((mHeaderPrimaryRealHeight - primaryTextHeight) / 2f + secondaryTextHeight) / 2f;
            float aboveY = mHeaderSecondaryHeight + y;
            float belowY = mCenterY + y;

            if(mMonthFirst){
                mDayY = mCenterY;
                mMonthY = aboveY;
            }
            else{
                mMonthY = mCenterY;
                mDayY = aboveY;
            }

            mYearY = belowY;

            mLocationDirty = false;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            mPaint.setColor(mHeaderSecondaryColor);
            canvas.drawPath(mHeaderSecondaryBackground, mPaint);
            mPaint.setColor(mHeaderPrimaryColor);
            canvas.drawRect(0, mHeaderSecondaryHeight, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight, mPaint);

            measureHeaderText();

            if(mWeekDay == null)
                return;

            mPaint.setTextSize(mDatePicker.getTextSize());
            mPaint.setColor(mDatePicker.getTextHighlightColor());

            canvas.drawText(mWeekDay, 0, mWeekDay.length(), mBaseX, mWeekDayY, mPaint);

            mPaint.setColor(mDaySelectMode ? mDatePicker.getTextHighlightColor() : mTextHeaderColor);
            mPaint.setTextSize(mHeaderPrimaryTextSize);
            if(mMonthFirst)
                canvas.drawText(mDay, 0, mDay.length(), mBaseX, mDayY, mPaint);
            else
                canvas.drawText(mMonth, 0, mMonth.length(), mBaseX, mMonthY, mPaint);

            mPaint.setTextSize(mHeaderSecondaryTextSize);
            if(mMonthFirst)
                canvas.drawText(mMonth, 0, mMonth.length(), mBaseX, mMonthY, mPaint);
            else
                canvas.drawText(mDay, 0, mDay.length(), mBaseX, mDayY, mPaint);

            mPaint.setColor(mDaySelectMode ? mTextHeaderColor : mDatePicker.getTextHighlightColor());
            canvas.drawText(mYear, 0, mYear.length(), mBaseX, mYearY, mPaint);
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
                    if(isTouched(mBaseX - mFirstWidth / 2f, mHeaderSecondaryHeight, mBaseX + mFirstWidth / 2f, mCenterY , event.getX(), event.getY()))
                        return !mDaySelectMode;

                    if(isTouched(mBaseX - mSecondWidth / 2f, mCenterY, mBaseX + mSecondWidth / 2f, mHeaderSecondaryHeight + mHeaderPrimaryRealHeight, event.getX(), event.getY()))
                        return mDaySelectMode;
                    break;
                case MotionEvent.ACTION_UP:
                    if(isTouched(mBaseX - mFirstWidth / 2f, mHeaderSecondaryHeight, mBaseX + mFirstWidth / 2f, mCenterY , event.getX(), event.getY())) {
                        setDateSelectMode(true);
                        return true;
                    }

                    if(isTouched(mBaseX - mSecondWidth / 2f, mCenterY, mBaseX + mSecondWidth / 2f, mHeaderSecondaryHeight + mHeaderPrimaryRealHeight, event.getX(), event.getY())) {
                        setDateSelectMode(false);
                        return true;
                    }
                    break;
            }

            return false;
        }

    }

    public static class Builder extends Dialog.Builder implements OnDateChangedListener {

        protected int mMinDay;
        protected int mMinMonth;
        protected int mMinYear;
        protected int mMaxDay;
        protected int mMaxMonth;
        protected int mMaxYear;
        protected int mDay;
        protected int mMonth;
        protected int mYear;

        private Calendar mCalendar;

        public Builder(){
            this(R.style.Material_App_Dialog_DatePicker_Light);
        }

        public Builder(int styleId){
            super(styleId);
            Calendar cal = Calendar.getInstance();
            mDay = cal.get(Calendar.DAY_OF_MONTH);
            mMonth = cal.get(Calendar.MONTH);
            mYear = cal.get(Calendar.YEAR);
            mMinDay = mDay;
            mMinMonth = mMonth;
            mMinYear = mYear - 12;
            mMaxDay = mDay;
            mMaxMonth = mMonth;
            mMaxYear = mYear + 12;
        }

        public Builder(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear, int day, int month, int year){
            this(R.style.Material_App_Dialog_DatePicker_Light, minDay, minMonth, minYear, maxDay, maxMonth, maxYear, day, month, year);
        }

        public Builder(int styleId, int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear, int day, int month, int year){
            super(styleId);
            mMinDay = minDay;
            mMinMonth = minMonth;
            mMinYear = minYear;
            mMaxDay = maxDay;
            mMaxMonth = maxMonth;
            mMaxYear = maxYear;
            mDay = day;
            mMonth = month;
            mYear = year;
        }

        public Builder dateRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
            mMinDay = minDay;
            mMinMonth = minMonth;
            mMinYear = minYear;
            mMaxDay = maxDay;
            mMaxMonth = maxMonth;
            mMaxYear = maxYear;
            return this;
        }

        public Builder dateRange(long minTime, long maxTime){
            if(mCalendar == null)
                mCalendar = Calendar.getInstance();

            mCalendar.setTimeInMillis(minTime);
            int minDay = mCalendar.get(Calendar.DAY_OF_MONTH);
            int minMonth = mCalendar.get(Calendar.MONTH);
            int minYear = mCalendar.get(Calendar.YEAR);
            mCalendar.setTimeInMillis(maxTime);
            int maxDay = mCalendar.get(Calendar.DAY_OF_MONTH);
            int maxMonth = mCalendar.get(Calendar.MONTH);
            int maxYear = mCalendar.get(Calendar.YEAR);

            return dateRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
        }

        public Builder date(int day, int month, int year){
            mDay = day;
            mMonth = month;
            mYear = year;
            return this;
        }

        public Builder date(long time) {
            if (mCalendar == null)
                mCalendar = Calendar.getInstance();

            mCalendar.setTimeInMillis(time);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            int month = mCalendar.get(Calendar.MONTH);
            int year = mCalendar.get(Calendar.YEAR);

            return date(day, month, year);
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

        @Override
        public Dialog.Builder contentView(int layoutId) {
            return this;
        }

        @Override
        protected Dialog onBuild(Context context, int styleId) {
            DatePickerDialog dialog = new DatePickerDialog(context, styleId);

            dialog.dateRange(mMinDay, mMinMonth, mMinYear, mMaxDay, mMaxMonth, mMaxYear)
                    .date(mDay, mMonth, mYear)
                    .onDateChangedListener(this);

            return dialog;
        }

        @Override
        public void onDateChanged(int oldDay, int oldMonth, int oldYear, int newDay, int newMonth, int newYear) {
            date(newDay, newMonth, newYear);
        }

        protected Builder(Parcel in){
            super(in);
        }

        @Override
        protected void onReadFromParcel(Parcel in) {
            mMinDay = in.readInt();
            mMinMonth = in.readInt();
            mMinYear = in.readInt();
            mMaxDay = in.readInt();
            mMaxMonth = in.readInt();
            mMaxYear = in.readInt();
            mDay = in.readInt();
            mMonth = in.readInt();
            mYear = in.readInt();
        }

        @Override
        protected void onWriteToParcel(Parcel dest, int flags) {
            dest.writeInt(mMinDay);
            dest.writeInt(mMinMonth);
            dest.writeInt(mMinYear);
            dest.writeInt(mMaxDay);
            dest.writeInt(mMaxMonth);
            dest.writeInt(mMaxYear);
            dest.writeInt(mDay);
            dest.writeInt(mMonth);
            dest.writeInt(mYear);
        }

        public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };


    }
}
