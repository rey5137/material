package com.rey.material.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.DatePicker;
import com.rey.material.widget.YearPicker;

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

    private class DatePickerLayout extends android.widget.FrameLayout{

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

        public DatePickerLayout(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mRect = new RectF();
            mHeaderSecondaryBackground = new Path();
            mPadding = ThemeUtil.dpToPx(context, 16);

            mYearPicker = new YearPicker(context);
            mDatePicker = new DatePicker(context);
            mYearPicker.setPadding(mPadding, mPadding, mPadding, mPadding);
//            mDatePicker.setPadding(mPadding, mPadding, mPadding, mPadding);

            addView(mYearPicker);
            addView(mDatePicker);

            mYearPicker.setVisibility(mDateSelectMode ? View.GONE : View.VISIBLE);
            mDatePicker.setVisibility(mDateSelectMode ? View.VISIBLE : View.GONE);

            setWillNotDraw(false);
        }

        public void applyStyle(int resId){
            mYearPicker.applyStyle(resId);
            mDatePicker.applyStyle(resId);

            Context context = getContext();

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
            mYearPicker.setYear(year);
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

        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                childTop += mHeaderPrimaryRealHeight + mHeaderSecondaryHeight;
                mDatePicker.layout(childLeft, childTop, childRight, childBottom);
                mYearPicker.layout(childLeft, childTop, childRight, childBottom);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                mPaint.setColor(mHeaderSecondaryColor);
                canvas.drawPath(mHeaderSecondaryBackground, mPaint);
                mPaint.setColor(mHeaderPrimaryColor);
                canvas.drawRect(0, mHeaderSecondaryHeight, mHeaderRealWidth, mHeaderPrimaryRealHeight + mHeaderSecondaryHeight, mPaint);
            }
        }
    }
}
