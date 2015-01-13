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
import android.os.Parcel;
import android.os.Parcelable;
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
import com.rey.material.drawable.CircleDrawable;
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
        mTimePickerLayout.setAm(am, false);
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
        private static final String MINUTE_FORMART = "%02d";

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
            mTextTimeSize = a.getDimensionPixelSize(R.styleable.TimePickerDialog_tp_textTimeSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_headline_material));
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

        public void setAm(boolean am, boolean animation){
            if(mIsAm != am){
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
            setAm(v == mAmView, true);
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
            mMinute = String.format(MINUTE_FORMART, newValue);
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

                spec = MeasureSpec.makeMeasureSpec(Math.min(halfWidth, heightSize), MeasureSpec.EXACTLY);
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
                int paddingHorizontal = (childRight / 2 - mTimePicker.getMeasuredWidth()) / 2;
                int paddingVertical = (childBottom - mTimePicker.getMeasuredHeight()) / 2;
                mTimePicker.layout(childRight - paddingHorizontal - mTimePicker.getMeasuredWidth(), childTop + paddingVertical, childRight - paddingHorizontal, childTop + paddingVertical + mTimePicker.getMeasuredHeight());

                childRight = childRight / 2;

                paddingHorizontal = mContentPadding + mActionPadding;
                paddingVertical = mContentPadding - mActionPadding;
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

    public static class Builder extends Dialog.Builder{

        private int mHour;
        private int mMinute;
        private boolean mAm;

        public Builder(){
            super();
            Calendar cal = Calendar.getInstance();
            mHour = cal.get(Calendar.HOUR);
            mMinute = cal.get(Calendar.MINUTE);
            mAm = cal.get(Calendar.AM_PM) == Calendar.AM;
        }

        public Builder(int styleId, int hour, int minute, boolean am){
            super(styleId);
            mHour = hour;
            mMinute = minute;
            mAm = am;
        }

        public Builder hour(int hour){
            mHour = hour;
            return this;
        }

        public Builder minute(int minute){
            mMinute = minute;
            return this;
        }

        public Builder am(boolean am){
            mAm = am;
            return this;
        }

        @Override
        protected Dialog onBuild(Context context, int styleId) {
            TimePickerDialog dialog = new TimePickerDialog(context, styleId);
            dialog.hour(mHour)
                    .minute(mMinute)
                    .am(mAm);
            return dialog;
        }

        private Builder(Parcel in){
            super(in);
        }

        @Override
        protected void onWriteToParcel(Parcel dest, int flags) {
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mAm ? 1 : 0);
        }

        @Override
        protected void onReadFromParcel(Parcel in) {
            mHour = in.readInt();
            mMinute = in.readInt();
            mAm = in.readInt() == 1;
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
