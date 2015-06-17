package com.rey.material.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.BlankDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

import java.util.Calendar;

/**
 * Created by Rey on 12/26/2014.
 */
public class YearPicker extends ListView {

    private YearAdapter mAdapter;

    private int mTextSize = -1;
    private int mItemHeight = -1;
    private int mSelectionColor;
    private int mAnimDuration = -1;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;
    private Typeface mTypeface = Typeface.DEFAULT;

    private int mItemRealHeight = -1;
    private int mPadding;
    private int mPositionShift;
    private int mDistanceShift;

    private Paint mPaint;

    /**
     * Interface definition for a callback to be invoked when the selected year is changed.
     */
    public interface OnYearChangedListener{

        /**
         * Called then the selected year is changed.
         * @param oldValue The old year value.
         * @param newValue The new year value.
         */
        public void onYearChanged(int oldValue, int newValue);

    }

    private OnYearChangedListener mOnYearChangedListener;

    private static final int[][] STATES = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked},
    };

    private int[] mTextColors = new int[]{0xFF000000, 0xFFFFFFFF};

    private static final String YEAR_FORMAT = "%4d";

    public YearPicker(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public YearPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public YearPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public YearPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mAdapter = new YearAdapter();
        setAdapter(mAdapter);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        setSelector(BlankDrawable.getInstance());
        setDividerHeight(0);
        setCacheColorHint(Color.TRANSPARENT);
        setClipToPadding(false);

        mPadding = ThemeUtil.dpToPx(context, 4);

        mSelectionColor = ThemeUtil.colorPrimary(context, 0xFF000000);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super.applyStyle(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YearPicker, defStyleAttr, defStyleRes);

        int year = -1;
        int yearMin = -1;
        int yearMax = -1;
        String familyName = null;
        int style = -1;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.YearPicker_dp_yearTextSize)
                mTextSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_year)
                year = a.getInteger(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_yearMin)
                yearMin = a.getInteger(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_yearMax)
                yearMax = a.getInteger(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_yearItemHeight)
                mItemHeight = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_textColor)
                mTextColors[0] = a.getColor(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_textHighlightColor)
                mTextColors[1] = a.getColor(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_selectionColor)
                mSelectionColor = a.getColor(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_animDuration)
                mAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.YearPicker_dp_inInterpolator)
                mInInterpolator = AnimationUtils.loadInterpolator(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.YearPicker_dp_outInterpolator)
                mOutInterpolator = AnimationUtils.loadInterpolator(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.YearPicker_dp_fontFamily)
                familyName = a.getString(attr);
            else if(attr == R.styleable.YearPicker_dp_textStyle)
                style = a.getInteger(attr, 0);
        }

        a.recycle();

        if(mTextSize < 0)
            mTextSize = context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_title_material);

        if(mItemHeight < 0)
            mItemHeight = ThemeUtil.dpToPx(context, 48);

        if(mAnimDuration < 0)
            mAnimDuration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        if(mInInterpolator == null)
            mInInterpolator = new DecelerateInterpolator();

        if(mOutInterpolator == null)
            mOutInterpolator = new DecelerateInterpolator();

        if(familyName != null || style >= 0)
            mTypeface = TypefaceUtil.load(context, familyName, style);

        if(yearMin >= 0 || yearMax >= 0){
            if(yearMin < 0)
                yearMin = mAdapter.getMinYear();

            if(yearMax < 0)
                yearMax = mAdapter.getMaxYear();

            if(yearMax < yearMin)
                yearMax = Integer.MAX_VALUE;

            setYearRange(yearMin, yearMax);
        }

        if(mAdapter.getYear() < 0 && year < 0){
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
        }

        if(year >= 0){
            year = Math.max(yearMin, Math.min(yearMax, year));
            setYear(year);
        }

        mAdapter.notifyDataSetChanged();
        requestLayout();
    }

    /**
     * Set the range of selectable year value.
     * @param min The minimum selectable year value.
     * @param max The maximum selectable year value.
     */
    public void setYearRange(int min, int max){
        mAdapter.setYearRange(min, max);
    }

    /**
     * Jump to a specific year.
     * @param year
     */
    public void goTo(int year){
        int position = mAdapter.positionOfYear(year) - mPositionShift;
        int offset = mDistanceShift;
        if(position < 0){
            position = 0;
            offset = 0;
        }
        postSetSelectionFromTop(position, offset);
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
     * Set the selected year.
     * @param year The selected year value.
     */
    public void setYear(int year){
        if(mAdapter.getYear() == year)
            return;

        mAdapter.setYear(year);
        goTo(year);
    }

    /**
     * @return The selected year value.
     */
    public int getYear(){
        return mAdapter.getYear();
    }

    /**
     * Set a listener will be called when the selected year value is changed.
     * @param listener The {@link YearPicker.OnYearChangedListener} will be called.
     */
    public void setOnYearChangedListener(OnYearChangedListener listener){
        mOnYearChangedListener = listener;
    }

    private void measureItemHeight(){
        if(mItemRealHeight > 0)
            return;

        mPaint.setTextSize(mTextSize);
        mItemRealHeight = Math.max(Math.round(mPaint.measureText("9999", 0, 4)) + mPadding * 2, mItemHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureItemHeight();

        if(heightMode != MeasureSpec.EXACTLY){
            if(heightMode == MeasureSpec.AT_MOST){
                int num = Math.min(mAdapter.getCount(), heightSize / mItemRealHeight);
                if(num >= 3)
                    heightSize = mItemRealHeight * (num % 2 == 0 ? num - 1 : num);
            }
            else
                heightSize = mItemRealHeight * mAdapter.getCount();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float shift = (h / (float)mItemRealHeight - 1) / 2;
        mPositionShift = (int)Math.floor(shift);
        mPositionShift = shift > mPositionShift ? mPositionShift + 1 : mPositionShift;
        mDistanceShift = (int)((shift - mPositionShift) * mItemRealHeight) - getPaddingTop();
        goTo(mAdapter.getYear());
    }

    private class YearAdapter extends BaseAdapter implements OnClickListener{

        private int mMinYear = 1990;
        private int mMaxYear = Integer.MAX_VALUE - 1;
        private int mCurYear = -1;

        public YearAdapter(){}

        public int getMinYear(){
            return mMinYear;
        }

        public int getMaxYear(){
            return mMaxYear;
        }

        public void setYearRange(int min, int max){
            if(mMinYear != min || mMaxYear != max){
                mMinYear = min;
                mMaxYear = max;
                notifyDataSetChanged();
            }
        }

        public int positionOfYear(int year){
            return year - mMinYear;
        }

        @Override
        public int getCount(){
            return mMaxYear - mMinYear + 1;
        }

        @Override
        public Object getItem(int position){
            return mMinYear + position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setYear(int year){
            if(mCurYear != year){
                int old = mCurYear;
                mCurYear = year;

                CircleCheckedTextView child = (CircleCheckedTextView) YearPicker.this.getChildAt(positionOfYear(old) - YearPicker.this.getFirstVisiblePosition());
                if(child != null)
                    child.setChecked(false);

                child = (CircleCheckedTextView) YearPicker.this.getChildAt(positionOfYear(mCurYear) - YearPicker.this.getFirstVisiblePosition());
                if(child != null)
                    child.setChecked(true);

                if(mOnYearChangedListener != null)
                    mOnYearChangedListener.onYearChanged(old, mCurYear);
            }
        }

        public int getYear(){
            return mCurYear;
        }

        @Override
        public void onClick(View v) {
            setYear((Integer)v.getTag());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CircleCheckedTextView v = (CircleCheckedTextView)convertView;
            if(v == null){
                v = new CircleCheckedTextView(getContext());
                v.setGravity(Gravity.CENTER);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    v.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                v.setMinHeight(mItemRealHeight);
                v.setMaxHeight(mItemRealHeight);
                v.setAnimDuration(mAnimDuration);
                v.setInterpolator(mInInterpolator, mOutInterpolator);
                v.setBackgroundColor(mSelectionColor);
                v.setTypeface(mTypeface);
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                v.setTextColor(new ColorStateList(STATES, mTextColors));
                v.setOnClickListener(this);
            }

            int year = (Integer)getItem(position);
            v.setTag(year);
            v.setText(String.format(YEAR_FORMAT, year));
            v.setCheckedImmediately(year == mCurYear);
            return v;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.yearMin = mAdapter.getMinYear();
        ss.yearMax = mAdapter.getMaxYear();
        ss.year = mAdapter.getYear();

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setYearRange(ss.yearMin, ss.yearMax);
        setYear(ss.year);
    }

    static class SavedState extends BaseSavedState {
        int yearMin;
        int yearMax;
        int year;

        /**
         * Constructor called from {@link com.rey.material.widget.Switch#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            yearMin = in.readInt();
            yearMax = in.readInt();
            year = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(yearMin);
            out.writeValue(yearMax);
            out.writeValue(year);
        }

        @Override
        public String toString() {
            return "YearPicker.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " yearMin=" + yearMin
                    + " yearMax=" + yearMax
                    + " year=" + year + "}";
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
