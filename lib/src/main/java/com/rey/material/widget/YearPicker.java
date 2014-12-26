package com.rey.material.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
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
import com.rey.material.drawable.BlankDrawable;
import com.rey.material.drawable.CircleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.TypefaceUtil;

import java.util.Calendar;

/**
 * Created by Rey on 12/26/2014.
 */
public class YearPicker extends ListView{

    private YearAdapter mAdapter;

    private int mTextSize;
    private int mItemHeight;
    private int mSelectionColor;
    private int mAnimDuration;
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;
    private Typeface mTypeface;

    private int mItemRealHeight = -1;
    private int mPadding;

    private Paint mPaint;

    public interface OnYearChangedListener{

        public void onYearChanged(int oldValue, int newValue);

    }

    private OnYearChangedListener mOnYearChangedListener;

    private static final int[][] STATES = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked},
    };

    private int[] mTextColors = new int[2];

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

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YearPicker, defStyleAttr, defStyleRes);
        mTextSize = a.getDimensionPixelSize(R.styleable.YearPicker_dp_yearTextSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_title_material));
        int year = a.getInteger(R.styleable.YearPicker_dp_year, -1);
        int yearMin = a.getInteger(R.styleable.YearPicker_dp_yearMin, -1);
        int yearMax = a.getInteger(R.styleable.YearPicker_dp_yearMax, -1);
        mItemHeight = a.getDimensionPixelSize(R.styleable.YearPicker_dp_yearItemHeight, ThemeUtil.dpToPx(context, 48));
        mTextColors[0] = a.getColor(R.styleable.YearPicker_dp_yearTextColor, 0xFF000000);
        mTextColors[1] = a.getColor(R.styleable.YearPicker_dp_yearTextHighlightColor, 0xFFFFFFFF);
        mSelectionColor = a.getColor(R.styleable.YearPicker_dp_yearSelectionColor, ThemeUtil.colorPrimary(context, 0xFF000000));
        mAnimDuration = a.getInteger(R.styleable.YearPicker_dp_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        int resId = a.getResourceId(R.styleable.YearPicker_dp_inInterpolator, 0);
        if(resId != 0)
            mInInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mInInterpolator = new DecelerateInterpolator();
        resId = a.getResourceId(R.styleable.YearPicker_dp_outInterpolator, 0);
        if(resId != 0)
            mOutInterpolator = AnimationUtils.loadInterpolator(context, resId);
        else
            mOutInterpolator = new DecelerateInterpolator();
        String familyName = a.getString(R.styleable.YearPicker_android_fontFamily);
        int style = a.getInteger(R.styleable.YearPicker_android_textStyle, Typeface.NORMAL);

        mTypeface = TypefaceUtil.load(context, familyName, style);

        a.recycle();

        if(yearMin < 0)
            yearMin = 1990;

        if(yearMax < 0 || yearMax < yearMin)
            yearMax = Integer.MAX_VALUE;

        if(year < 0){
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
        }

        year = Math.max(yearMin, Math.min(yearMax, year));

        mAdapter.setYearRange(yearMin, yearMax);
        mAdapter.setYear(year);
        setSelectionFromTop(mAdapter.positionOfYear(mAdapter.getYear()) - 1, 0);
        mAdapter.notifyDataSetChanged();
    }

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
            heightSize = heightMode == MeasureSpec.AT_MOST ? Math.min(heightSize, mItemRealHeight * 3) : mItemRealHeight * 3;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class YearAdapter extends BaseAdapter implements View.OnClickListener{

        private int mMinYear = 0;
        private int mMaxYear = Integer.MAX_VALUE;
        private int mCurYear;

        public YearAdapter(){}

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
            return mMaxYear - mMinYear;
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

                CircleCheckedTextView child = (CircleCheckedTextView)YearPicker.this.getChildAt(positionOfYear(old) - YearPicker.this.getFirstVisiblePosition());
                if(child != null)
                    child.setChecked(false);

                child = (CircleCheckedTextView)YearPicker.this.getChildAt(positionOfYear(mCurYear) - YearPicker.this.getFirstVisiblePosition());
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
            v.setText(String.valueOf(year));
            v.setCheckedImmediately(year == mCurYear);
            return v;
        }
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
}
