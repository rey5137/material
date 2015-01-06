package com.rey.material.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.DatePicker;
import com.rey.material.widget.TimePicker;
import com.rey.material.widget.YearPicker;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Rey on 12/30/2014.
 */
public class DatePickerDialog extends Dialog {

    private DatePickerLayout mDatePickerLayout;
    private float mCornerRadius;

    public DatePickerDialog(Context context) {
        super(context);
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

    public DatePickerDialog dayRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
        mDatePickerLayout.setDayRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
        return this;
    }

    public DatePickerDialog day(int day, int month, int year){
        mDatePickerLayout.setDay(day, month, year);
        return this;
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
        private int mTextHeaderColor;

        private Paint mPaint;
        private int mHeaderPrimaryRealHeight;
        private int mHeaderRealWidth;
        private RectF mRect;
        private Path mHeaderSecondaryBackground;

        private int mPadding;

        private boolean mDateSelectMode = true;

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
        private static final String DAY_FORMART = "%02d";

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

            mYearPicker.setAlpha(mDateSelectMode ? 0f : 1f);
            mDatePicker.setAlpha(mDateSelectMode ? 1f : 0f);

            setWillNotDraw(false);
        }

        public void setDateSelectMode(boolean enable){
            if(mDateSelectMode != enable){
                mDateSelectMode = enable;

                mYearPicker.setAlpha(mDateSelectMode ? 0f : 1f);
                mDatePicker.setAlpha(mDateSelectMode ? 1f : 0f);
                invalidate(0, 0, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight);
            }
        }

        public void applyStyle(int resId){
            mYearPicker.applyStyle(resId);
            mDatePicker.applyStyle(resId);

            Context context = getContext();/**/

            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.DatePickerDialog);
            mHeaderPrimaryHeight = a.getDimensionPixelOffset(R.styleable.DatePickerDialog_dp_headerPrimaryHeight, ThemeUtil.dpToPx(context, 144));
            mHeaderSecondaryHeight = a.getDimensionPixelSize(R.styleable.DatePickerDialog_dp_headerSecondaryHeight, ThemeUtil.dpToPx(context, 32));
            mHeaderPrimaryColor = a.getColor(R.styleable.DatePickerDialog_dp_headerPrimaryColor, mDatePicker.getSelectionColor());
            mHeaderSecondaryColor = a.getColor(R.styleable.DatePickerDialog_dp_headerSecondaryColor, mHeaderPrimaryColor);
            mHeaderPrimaryTextSize = a.getDimensionPixelSize(R.styleable.DatePickerDialog_dp_headerPrimaryTextSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_display_2_material));
            mHeaderSecondaryTextSize = a.getDimensionPixelSize(R.styleable.DatePickerDialog_dp_headerSecondaryTextSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_headline_material));
            mTextHeaderColor = a.getColor(R.styleable.DatePickerDialog_dp_textHeaderColor, 0xFF000000);

            a.recycle();

            mPaint.setTypeface(mDatePicker.getTypeface());
        }

        public void setDayRange(int minDay, int minMonth, int minYear, int maxDay, int maxMonth, int maxYear){
            mDatePicker.setDayRange(minDay, minMonth, minYear, maxDay, maxMonth, maxYear);
            mYearPicker.setYearRange(minYear, maxYear);
        }

        public void setDay(int day, int month, int year){
            mDatePicker.setDay(day, month, year);
        }

        @Override
        public void onYearChanged(int oldYear, int newYear) {
            mDatePicker.setDay(mDatePicker.getDay(), mDatePicker.getMonth(), newYear);
        }

        @Override
        public void onDateChanged(int oldDay, int oldMonth, int oldYear, int newDay, int newMonth, int newYear) {
            mYearPicker.setOnYearChangedListener(null);
            mYearPicker.setYear(newYear);
            mYearPicker.setOnYearChangedListener(this);

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
                mDay = String.format(DAY_FORMART, newDay);
                mYear = String.valueOf(newYear);

                if(oldMonth != newMonth || oldYear != newYear)
                    mDatePicker.goTo(newMonth, newYear);
            }

            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight);
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
                    int hs = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                    mDatePicker.measure(ws, hs);
                    mYearPicker.measure(ws, hs);
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
                    int hs = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                    mDatePicker.measure(ws, hs);
                    mYearPicker.measure(ws, hs);
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
                mHeaderRealWidth = w / 2;
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
            mYearPicker.layout(childLeft, childTop, childRight, childBottom);
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

            mPaint.setColor(mDateSelectMode ? mDatePicker.getTextHighlightColor() : mTextHeaderColor);
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

            mPaint.setColor(mDateSelectMode ? mTextHeaderColor : mDatePicker.getTextHighlightColor());
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
                        return !mDateSelectMode;

                    if(isTouched(mBaseX - mSecondWidth / 2f, mCenterY, mBaseX + mSecondWidth / 2f, mHeaderSecondaryHeight + mHeaderPrimaryRealHeight, event.getX(), event.getY()))
                        return mDateSelectMode;
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
}
