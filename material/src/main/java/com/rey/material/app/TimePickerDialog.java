package com.rey.material.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.CircleCheckedTextView;
import com.rey.material.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Rey on 12/24/2014.
 */
public class TimePickerDialog extends Dialog{

    private TimePickerLayout mTimePickerLayout;
    private float mCornerRadius;

    /**
     * Interface definition for a callback to be invoked when the selected time is changed.
     */
    public interface OnTimeChangedListener{

        /**
         * Called when the selected time is changed.
         * @param oldHour The hour value of old time.
         * @param oldMinute The minute value of old time.
         * @param newHour The hour value of new time.
         * @param newMinute The minute value of new time.
         */
        public void onTimeChanged(int oldHour, int oldMinute, int newHour, int newMinute);

    }

    public TimePickerDialog(Context context) {
        super(context, R.style.Material_App_Dialog_TimePicker_Light);
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

    /**
     * Set the selected hour value.
     * @param hour The selected hour value.
     * @return The TimePickerDialog for chaining methods.
     */
    public TimePickerDialog hour(int hour){
        mTimePickerLayout.setHour(hour);
        return this;
    }

    /**
     * Set the selected minute value.
     * @param minute The selected minute value.
     * @return The TimePickerDialog for chaining methods.
     */
    public TimePickerDialog minute(int minute){
        mTimePickerLayout.setMinute(minute);
        return this;
    }

    /**
     * Set a listener will be called when the selected time is changed.
     * @param listener The {@link OnTimeChangedListener} will be called.
     */
    public TimePickerDialog onTimeChangedListener(OnTimeChangedListener listener){
        mTimePickerLayout.setOnTimeChangedListener(listener);
        return this;
    }

    /**
     * @return The selected hour value.
     */
    public int getHour(){
        return mTimePickerLayout.getHour();
    }

    /**
     * @return The selected minute value.
     */
    public int getMinute(){
        return mTimePickerLayout.getMinute();
    }

    /**
     * Get the formatted string of selected time.
     * @param formatter The DateFormat used to format the time.
     * @return
     */
    public String getFormattedTime(DateFormat formatter){
        return mTimePickerLayout.getFormattedTime(formatter);
    }

    private class TimePickerLayout extends android.widget.FrameLayout implements View.OnClickListener, TimePicker.OnTimeChangedListener{

        private int mHeaderHeight;
        private int mTextTimeColor = 0xFF000000;
        private int mTextTimeSize;
        private boolean mIsLeadingZero = false;

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
        private static final String FORMAT = "%02d";
        private static final String FORMAT_NO_LEADING_ZERO = "%d";

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
            mAmView.setGravity(Gravity.CENTER);
            mPmView.setGravity(Gravity.CENTER);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                mAmView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                mPmView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }
            mAmView.setCheckedImmediately(mIsAm);
            mPmView.setCheckedImmediately(!mIsAm);
            mAmView.setOnClickListener(this);
            mPmView.setOnClickListener(this);

            addView(mTimePicker);
            addView(mAmView);
            addView(mPmView);

            setWillNotDraw(false);

            mCheckBoxSize = ThemeUtil.dpToPx(context, 48);
            mHeaderHeight = ThemeUtil.dpToPx(context, 120);
            mTextTimeSize = context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_headline_material);
        }

        public void applyStyle(int resId){
            mTimePicker.applyStyle(resId);

            Context context = getContext();
            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.TimePickerDialog);

            String am = null;
            String pm = null;

            for(int i = 0, count = a.getIndexCount(); i < count; i++){
                int attr = a.getIndex(i);

                if(attr == R.styleable.TimePickerDialog_tp_headerHeight)
                    mHeaderHeight = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.TimePickerDialog_tp_textTimeColor)
                    mTextTimeColor = a.getColor(attr, 0);
                else if(attr == R.styleable.TimePickerDialog_tp_textTimeSize)
                    mTextTimeSize = a.getDimensionPixelSize(attr, 0);
                else if(attr == R.styleable.TimePickerDialog_tp_leadingZero)
                    mIsLeadingZero = a.getBoolean(attr, false);
                else if(attr == R.styleable.TimePickerDialog_tp_am)
                    am = a.getString(attr);
                else if(attr == R.styleable.TimePickerDialog_tp_pm)
                    pm = a.getString(attr);
            }

            a.recycle();

            if(am == null)
                am = DateUtils.getAMPMString(Calendar.AM).toUpperCase();

            if(pm == null)
                pm = DateUtils.getAMPMString(Calendar.PM).toUpperCase();

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

            mHour = String.format(mIsLeadingZero ? FORMAT : FORMAT_NO_LEADING_ZERO, !mTimePicker.is24Hour() && mTimePicker.getHour() == 0 ? 12 : mTimePicker.getHour());
            mMinute = String.format(FORMAT, mTimePicker.getMinute());

            if(!mTimePicker.is24Hour())
                mMidday = mIsAm ? mAmView.getText().toString() : mPmView.getText().toString();

            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);
        }

        public void setHour(int hour){
            if(!mTimePicker.is24Hour()){
                if(hour > 11 && hour < 24)
                    setAm(false, false);
                else
                    setAm(true, false);
            }
            mTimePicker.setHour(hour);
        }

        public int getHour(){
            return mTimePicker.is24Hour() || mIsAm ? mTimePicker.getHour() : mTimePicker.getHour() + 12;
        }

        public void setMinute(int minute){
            mTimePicker.setMinute(minute);
        }

        public int getMinute(){
            return mTimePicker.getMinute();
        }

        private void setAm(boolean am, boolean animation){
            if(mTimePicker.is24Hour())
                return;

            if(mIsAm != am){
                int oldHour = getHour();

                mIsAm = am;
                if(animation) {
                    mAmView.setChecked(mIsAm);
                    mPmView.setChecked(!mIsAm);
                }
                else{
                    mAmView.setCheckedImmediately(mIsAm);
                    mPmView.setCheckedImmediately(!mIsAm);
                }
                mMidday = mIsAm ? mAmView.getText().toString() : mPmView.getText().toString();
                invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

                if(mOnTimeChangedListener != null)
                    mOnTimeChangedListener.onTimeChanged(oldHour, getMinute(), getHour(), getMinute());
            }
        }

        public String getFormattedTime(DateFormat formatter){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, getHour());
            cal.set(Calendar.MINUTE, getMinute());
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return formatter.format(cal.getTime());
        }

        public void setOnTimeChangedListener(OnTimeChangedListener listener){
            mOnTimeChangedListener = listener;
        }

        @Override
        public void onClick(View v) {
            setAm(v == mAmView, true);
        }

        @Override
        public void onModeChanged(int mode){
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);
        }

        @Override
        public void onHourChanged(int oldValue, int newValue) {
            int oldHour = mTimePicker.is24Hour() || mIsAm ? oldValue : oldValue + 12;

            mHour = String.format(mIsLeadingZero ? FORMAT : FORMAT_NO_LEADING_ZERO, !mTimePicker.is24Hour() && newValue == 0 ? 12 : newValue);
            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onTimeChanged(oldHour, getMinute(), getHour(), getMinute());
        }

        @Override
        public void onMinuteChanged(int oldValue, int newValue) {
            mMinute = String.format(FORMAT, newValue);
            mLocationDirty = true;
            invalidate(0, 0, mHeaderRealWidth, mHeaderRealHeight);

            if(mOnTimeChangedListener != null)
                mOnTimeChangedListener.onTimeChanged(getHour(), oldValue, getHour(), newValue);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            int checkboxSize = mTimePicker.is24Hour() ? 0 : mCheckBoxSize;

            if(isPortrait){
                if(heightMode == MeasureSpec.AT_MOST)
                    heightSize = Math.min(heightSize, checkboxSize + widthSize + mHeaderHeight);

                if(checkboxSize > 0) {
                    int spec = MeasureSpec.makeMeasureSpec(mCheckBoxSize, MeasureSpec.EXACTLY);
                    mAmView.setVisibility(View.VISIBLE);
                    mPmView.setVisibility(View.VISIBLE);
                    mAmView.measure(spec, spec);
                    mPmView.measure(spec, spec);
                }
                else{
                    mAmView.setVisibility(View.GONE);
                    mPmView.setVisibility(View.GONE);
                }

                int spec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                mTimePicker.measure(spec, spec);

                setMeasuredDimension(widthSize, heightSize);
            }
            else{
                int halfWidth = widthSize / 2;

                if(heightMode == MeasureSpec.AT_MOST)
                    heightSize = Math.min(heightSize, Math.max(checkboxSize > 0 ? checkboxSize + mHeaderHeight + mContentPadding : mHeaderHeight, halfWidth));

                if(checkboxSize > 0) {
                    int spec = MeasureSpec.makeMeasureSpec(checkboxSize, MeasureSpec.EXACTLY);
                    mAmView.setVisibility(View.VISIBLE);
                    mPmView.setVisibility(View.VISIBLE);
                    mAmView.measure(spec, spec);
                    mPmView.measure(spec, spec);
                }
                else{
                    mAmView.setVisibility(View.GONE);
                    mPmView.setVisibility(View.GONE);
                }

                int spec = MeasureSpec.makeMeasureSpec(Math.min(halfWidth, heightSize), MeasureSpec.EXACTLY);
                mTimePicker.measure(spec, spec);

                setMeasuredDimension(widthSize, heightSize);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            mLocationDirty = true;
            int checkboxSize = mTimePicker.is24Hour() ? 0 : mCheckBoxSize;

            if(isPortrait){
                mHeaderRealWidth = w;
                mHeaderRealHeight = h - checkboxSize - w;
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
                mHeaderRealHeight = checkboxSize > 0 ? h - checkboxSize - mContentPadding : h;
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
            int checkboxSize = mTimePicker.is24Hour() ? 0 : mCheckBoxSize;

            if(isPortrait){
                int paddingHorizontal = mContentPadding + mActionPadding;
                int paddingVertical = mContentPadding - mActionPadding;

                if(checkboxSize > 0) {
                    mAmView.layout(childLeft + paddingHorizontal, childBottom - paddingVertical - checkboxSize, childLeft + paddingHorizontal + checkboxSize, childBottom - paddingVertical);
                    mPmView.layout(childRight - paddingHorizontal - checkboxSize, childBottom - paddingVertical - checkboxSize, childRight - paddingHorizontal, childBottom - paddingVertical);
                }

                childTop += mHeaderRealHeight;
                childBottom -= checkboxSize;
                mTimePicker.layout(childLeft, childTop, childRight, childBottom);
            }
            else{
                int paddingHorizontal = (childRight / 2 - mTimePicker.getMeasuredWidth()) / 2;
                int paddingVertical = (childBottom - mTimePicker.getMeasuredHeight()) / 2;
                mTimePicker.layout(childRight - paddingHorizontal - mTimePicker.getMeasuredWidth(), childTop + paddingVertical, childRight - paddingHorizontal, childTop + paddingVertical + mTimePicker.getMeasuredHeight());

                if(checkboxSize > 0){
                    childRight = childRight / 2;

                    paddingHorizontal = mContentPadding + mActionPadding;
                    paddingVertical = mContentPadding - mActionPadding;
                    mAmView.layout(childLeft + paddingHorizontal, childBottom - paddingVertical - checkboxSize, childLeft + paddingHorizontal + checkboxSize, childBottom - paddingVertical);
                    mPmView.layout(childRight - paddingHorizontal - checkboxSize, childBottom - paddingVertical - checkboxSize, childRight - paddingHorizontal, childBottom - paddingVertical);
                }
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

            if(!mTimePicker.is24Hour()) {
                mPaint.setTextSize(mTimePicker.getTextSize());
                mPaint.setColor(mTextTimeColor);
                canvas.drawText(mMidday, mMiddayX, mBaseY, mPaint);
            }
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

    public static class Builder extends Dialog.Builder implements OnTimeChangedListener {

        protected int mHour;
        protected int mMinute;

        public Builder(){
            super(R.style.Material_App_Dialog_TimePicker_Light);
            Calendar cal = Calendar.getInstance();
            mHour = cal.get(Calendar.HOUR_OF_DAY);
            mMinute = cal.get(Calendar.MINUTE);
        }

        public Builder(int hourOfDay, int minute){
            this(R.style.Material_App_Dialog_TimePicker_Light, hourOfDay, minute);
        }

        public Builder(int styleId, int hourOfDay, int minute){
            super(styleId);
            hour(hourOfDay);
            minute(minute);
        }

        public Builder hour(int hour){
            mHour = Math.min(Math.max(hour, 0), 24);
            return this;
        }

        public Builder minute(int minute){
            mMinute = minute;
            return this;
        }

        public int getHour(){
            return mHour;
        }

        public int getMinute(){
            return mMinute;
        }

        @Override
        public Dialog.Builder contentView(int layoutId) {
            return this;
        }

        @Override
        protected Dialog onBuild(Context context, int styleId) {
            TimePickerDialog dialog = new TimePickerDialog(context, styleId);
            dialog.hour(mHour)
                    .minute(mMinute)
                    .onTimeChangedListener(this);
            return dialog;
        }

        @Override
        public void onTimeChanged(int oldHour, int oldMinute, int newHour, int newMinute) {
            hour(newHour).minute(newMinute);
        }

        protected Builder(Parcel in){
            super(in);
        }

        @Override
        protected void onWriteToParcel(Parcel dest, int flags) {
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        @Override
        protected void onReadFromParcel(Parcel in) {
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

    }
}
