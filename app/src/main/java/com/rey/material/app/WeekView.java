package com.rey.material.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.CircleCheckedTextView;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Rey on 2/6/2015.
 */
public class WeekView extends FrameLayout{

    private ColorStateList mBackgroundColors;
    private int mCurrentBackgroundColor;
    private int mVerticalPadding;
    private int mHorizontalPadding;

    private int mFirstDayOfWeek;

    private float mOriginalTextSize;

    private Paint mPaint;
    private static final String BASE_TEXT = "WWW";

    private CircleCheckedTextView.OnCheckedChangeListener mCheckListener = new CircleCheckedTextView.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CircleCheckedTextView view, boolean checked) {
            onDaySelectionChanged((Integer)view.getTag(), checked);
        }
    };

    private OnClickListener mClickListener = new OnClickListener(){
        @Override
        public void onClick(View v) {
            CircleCheckedTextView child = (CircleCheckedTextView)v;
            child.setChecked(!child.isChecked());
        }
    };

    public interface OnDaySelectionChangedListener{
        public void onDaySelectionChanged(int dayOfWeek, boolean selected);
    }

    private OnDaySelectionChangedListener mOnDaySelectionChangedListener;

    public WeekView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mVerticalPadding = ThemeUtil.dpToPx(context, 16);
        mHorizontalPadding = ThemeUtil.dpToPx(context, 8);

        if(mBackgroundColors == null){
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{android.R.attr.state_enabled},
            };
            int[] colors = new int[]{
                    ThemeUtil.colorControlNormal(context, 0xFF000000),
                    ThemeUtil.colorControlActivated(context, 0xFF000000),
            };

            mBackgroundColors = new ColorStateList(states, colors);
        }

        Calendar cal = Calendar.getInstance();
        mFirstDayOfWeek = cal.getFirstDayOfWeek();
        int color = mBackgroundColors.getColorForState(getDrawableState(), mBackgroundColors.getDefaultColor());

        for(int i = 0; i < 7; i++){
            int dayOFWeek = mFirstDayOfWeek + i;
            if(dayOFWeek > Calendar.SATURDAY)
                dayOFWeek = Calendar.SUNDAY;
            cal.set(Calendar.DAY_OF_WEEK, dayOFWeek);

            CircleCheckedTextView view = new CircleCheckedTextView(context, attrs, defStyleAttr, defStyleRes);
            view.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()).toUpperCase());
            view.setTag(dayOFWeek);
            view.setOnCheckedChangeListener(mCheckListener);
            view.setOnClickListener(mClickListener);
            view.setCheckedImmediately(false);
            view.setPadding(0, 0, 0, 0);
            view.setBackgroundColor(color);

            if(i == 0) {
                mOriginalTextSize = view.getTextSize();
                mPaint.setTypeface(view.getTypeface());
            }

            addView(view);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for(int i = 0; i < getChildCount(); i++)
            getChildAt(i).setEnabled(enabled);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace);
        updateBackgroundColor(state);
        return state;
    }

    @Override
    public void refreshDrawableState() {
        updateBackgroundColor(getDrawableState());
        super.refreshDrawableState();
    }

    private void updateBackgroundColor(int[] state){
        int color = mBackgroundColors.getColorForState(state, mBackgroundColors.getDefaultColor());
        if(mCurrentBackgroundColor != color){
            mCurrentBackgroundColor = color;
            for(int i = 0; i < getChildCount(); i++){
                CircleCheckedTextView child = (CircleCheckedTextView)getChildAt(i);
                child.setBackgroundColor(color);
            }
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        int width;
        int height;
        int childSize;

        if(isPortrait){
            childSize = (widthSize - getPaddingLeft() - getPaddingRight() - mHorizontalPadding * 3) / 4;
            width = widthSize;
            height = childSize * 2 + mVerticalPadding + getPaddingTop() + getPaddingBottom();
        }
        else{
            childSize = (widthSize - getPaddingLeft() - getPaddingRight() - mHorizontalPadding * 6) / 7;
            width = widthSize;
            height = childSize + getPaddingTop() + getPaddingBottom();
        }

        int spec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);

        for(int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(spec, spec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mPaint.setTextSize(mOriginalTextSize);
        float baseWidth = mPaint.measureText(BASE_TEXT);

        float realWidth = getChildAt(0).getMeasuredWidth() - mHorizontalPadding * 2;

        if(realWidth < baseWidth){
            float textSize = mOriginalTextSize * realWidth / baseWidth;
            for(int i = 0; i < getChildCount(); i++)
                ((CircleCheckedTextView)getChildAt(i)).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();

        for(int i = 0; i < 7; i++){
            View v = getChildAt(i);
            v.layout(childLeft, childTop, childLeft + v.getMeasuredWidth(), childTop + v.getMeasuredHeight());

            if(i == 3 && isPortrait){
                childLeft = getPaddingLeft();
                childTop = getPaddingTop() + v.getMeasuredHeight() + mVerticalPadding;
            }
            else
                childLeft += v.getMeasuredWidth() + mHorizontalPadding;
        }
    }

    public void clearSelection(boolean immediately){
        for(int i = 0; i < getChildCount(); i++)
            if(immediately)
                ((CircleCheckedTextView)getChildAt(i)).setCheckedImmediately(false);
            else
                ((CircleCheckedTextView)getChildAt(i)).setChecked(false);
    }

    public void setSelected(int dayOfWeek, boolean selected, boolean immediately){
        int index = dayOfWeek >= mFirstDayOfWeek ? (dayOfWeek - mFirstDayOfWeek) : (dayOfWeek + 7 - mFirstDayOfWeek);
        CircleCheckedTextView view = (CircleCheckedTextView)getChildAt(index);

        if(immediately)
            view.setCheckedImmediately(selected);
        else
            view.setChecked(selected);
    }

    public void setOnDaySelectionChangedListener(OnDaySelectionChangedListener listener){
        mOnDaySelectionChangedListener = listener;
    }

    private void onDaySelectionChanged(int dayOfWeek, boolean selected){
        if(mOnDaySelectionChangedListener != null)
            mOnDaySelectionChangedListener.onDaySelectionChanged(dayOfWeek, selected);
    }
}
