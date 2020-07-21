package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.ArrowDrawable;
import com.rey.material.drawable.DividerDrawable;
import com.rey.material.util.ThemeUtil;

public class Spinner extends FrameLayout implements ThemeManager.OnThemeChangedListener{
		
	private static final int MAX_ITEMS_MEASURED = 15;
	
	private static final int INVALID_POSITION = -1;

    /**
     * Interface definition for a callback to be invoked when a item's view is clicked.
     */
	public interface OnItemClickListener{
        /**
         * Called when a item's view is clicked.
         * @param parent The Spinner view.
         * @param view The item view.
         * @param position The position of item.
         * @param id The id of item.
         * @return false will make the Spinner doesn't select this item.
         */
		boolean onItemClick(Spinner parent, View view, int position, long id);
	}

    /**
     * Interface definition for a callback to be invoked when an item is selected.
     */
	public interface OnItemSelectedListener{
        /**
         * Called when an item is selected.
         * @param parent The Spinner view.
         * @param view The item view.
         * @param position The position of item.
         * @param id The id of item.
         */
		void onItemSelected(Spinner parent, View view, int position, long id);
	}

    private boolean mLabelEnable;
    private TextView mLabelView;

	private SpinnerAdapter mAdapter;
	private OnItemClickListener mOnItemClickListener;
	private OnItemSelectedListener mOnItemSelectedListener;

    private int mMinWidth;
    private int mMinHeight;

	private DropdownPopup mPopup;
	private int mDropDownWidth;
	
	private ArrowDrawable mArrowDrawable;
	private int mArrowSize;
	private int mArrowPadding;
	private boolean mArrowAnimSwitchMode;
	
	private DividerDrawable mDividerDrawable;
	private int mDividerHeight;
	private int mDividerPadding;
	
	private int mGravity;
	private boolean mDisableChildrenWhenDisabled;
	
	private int mSelectedPosition;
	
	private RecycleBin mRecycler = new RecycleBin();
	
	private Rect mTempRect = new Rect();
	
	private DropDownAdapter mTempAdapter;
	
	private SpinnerDataSetObserver mDataSetObserver = new SpinnerDataSetObserver();

    private boolean mIsRtl;
		
	public Spinner(Context context) {
		super(context, null, R.attr.listPopupWindowStyle);
	}
	
	public Spinner(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.listPopupWindowStyle);
	}
	
	public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

    @Override
	protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mLabelEnable = false;
        mDropDownWidth = LayoutParams.WRAP_CONTENT;
        mArrowAnimSwitchMode = false;
        mGravity = Gravity.CENTER;
        mDisableChildrenWhenDisabled = false;
        mSelectedPosition = INVALID_POSITION;
        mIsRtl = false;

        setWillNotDraw(false);

        mPopup = new DropdownPopup(context, attrs, defStyleAttr, defStyleRes);
        mPopup.setModal(true);

		if(isInEditMode())
            applyStyle(R.style.Material_Widget_Spinner);
		
		setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        super.init(context, attrs, defStyleAttr, defStyleRes);
	}

    private android.widget.TextView getLabelView(){
        if(mLabelView == null){
            mLabelView = new TextView(getContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                mLabelView.setTextDirection(mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR);
            mLabelView.setSingleLine(true);
            mLabelView.setDuplicateParentStateEnabled(true);
        }

        return mLabelView;
    }

    @Override
    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super.applyStyle(context, attrs, defStyleAttr, defStyleRes);

        removeAllViews();

        TypedArray a = context.obtainStyledAttributes(attrs,  R.styleable.Spinner, defStyleAttr, defStyleRes);

        int arrowAnimDuration = -1;
        ColorStateList arrowColor = null;
        Interpolator arrowInterpolator = null;
        boolean arrowClockwise = true;
        int dividerAnimDuration = -1;
        ColorStateList dividerColor = null;
        ColorStateList labelTextColor = null;
        int labelTextSize = -1;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.Spinner_spn_labelEnable)
                mLabelEnable = a.getBoolean(attr, false);
            else if(attr == R.styleable.Spinner_spn_labelPadding)
                getLabelView().setPadding(0, 0, 0, a.getDimensionPixelSize(attr, 0));
            else if (attr == R.styleable.Spinner_spn_labelTextSize)
                labelTextSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.Spinner_spn_labelTextColor)
                labelTextColor = a.getColorStateList(attr);
            else if(attr == R.styleable.Spinner_spn_labelTextAppearance)
                getLabelView().setTextAppearance(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.Spinner_spn_labelEllipsize){
                int labelEllipsize = a.getInteger(attr, 0);
                switch (labelEllipsize) {
                    case 1:
                        getLabelView().setEllipsize(TextUtils.TruncateAt.START);
                        break;
                    case 2:
                        getLabelView().setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        break;
                    case 3:
                        getLabelView().setEllipsize(TextUtils.TruncateAt.END);
                        break;
                    case 4:
                        getLabelView().setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        break;
                    default:
                        getLabelView().setEllipsize(TextUtils.TruncateAt.END);
                        break;
                }
            }
            else if(attr == R.styleable.Spinner_spn_label)
                getLabelView().setText(a.getString(attr));
            else if(attr == R.styleable.Spinner_android_gravity)
                mGravity = a.getInt(attr, 0);
            else if(attr == R.styleable.Spinner_android_minWidth)
                setMinimumWidth(a.getDimensionPixelOffset(attr, 0));
            else if(attr == R.styleable.Spinner_android_minHeight)
                setMinimumHeight(a.getDimensionPixelOffset(attr, 0));
            else if(attr == R.styleable.Spinner_android_dropDownWidth)
                mDropDownWidth = a.getLayoutDimension(attr, LayoutParams.WRAP_CONTENT);
            else if(attr == R.styleable.Spinner_android_popupBackground)
                mPopup.setBackgroundDrawable(a.getDrawable(attr));
            else if(attr == R.styleable.Spinner_android_prompt)
                mPopup.setPromptText(a.getString(attr));
            else if(attr == R.styleable.Spinner_spn_popupItemAnimation)
                mPopup.setItemAnimation(a.getResourceId(attr, 0));
            else if(attr == R.styleable.Spinner_spn_popupItemAnimOffset)
                mPopup.setItemAnimationOffset(a.getInteger(attr, 0));
            else if(attr == R.styleable.Spinner_spn_disableChildrenWhenDisabled)
                mDisableChildrenWhenDisabled = a.getBoolean(attr, false);
            else if(attr == R.styleable.Spinner_spn_arrowSwitchMode)
                mArrowAnimSwitchMode = a.getBoolean(attr, false);
            else if(attr == R.styleable.Spinner_spn_arrowAnimDuration)
                arrowAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.Spinner_spn_arrowSize)
                mArrowSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.Spinner_spn_arrowPadding)
                mArrowPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.Spinner_spn_arrowColor)
                arrowColor = a.getColorStateList(attr);
            else if(attr == R.styleable.Spinner_spn_arrowInterpolator){
                int resId = a.getResourceId(attr, 0);
                arrowInterpolator = AnimationUtils.loadInterpolator(context, resId);
            }
            else if(attr == R.styleable.Spinner_spn_arrowAnimClockwise)
                arrowClockwise = a.getBoolean(attr, true);
            else if(attr == R.styleable.Spinner_spn_dividerHeight)
                mDividerHeight = a.getDimensionPixelOffset(attr, 0);
            else if(attr == R.styleable.Spinner_spn_dividerPadding)
                mDividerPadding = a.getDimensionPixelOffset(attr, 0);
            else if(attr == R.styleable.Spinner_spn_dividerAnimDuration)
                dividerAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.Spinner_spn_dividerColor)
                dividerColor = a.getColorStateList(attr);
        }

        a.recycle();

        if(labelTextColor != null)
            getLabelView().setTextColor(labelTextColor);

        if(labelTextSize >= 0)
            getLabelView().setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);

        if(mLabelEnable)
            addView(getLabelView(), 0, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if(mArrowSize > 0){
            if(mArrowDrawable == null){
                if(arrowColor == null)
                    arrowColor = ColorStateList.valueOf(ThemeUtil.colorControlNormal(context, 0xFF000000));

                if(arrowAnimDuration < 0)
                    arrowAnimDuration = 0;

                mArrowDrawable = new ArrowDrawable(ArrowDrawable.MODE_DOWN, mArrowSize, arrowColor, arrowAnimDuration, arrowInterpolator, arrowClockwise);
                mArrowDrawable.setCallback(this);
            }
            else{
                mArrowDrawable.setArrowSize(mArrowSize);
                mArrowDrawable.setClockwise(arrowClockwise);

                if(arrowColor != null)
                    mArrowDrawable.setColor(arrowColor);

                if(arrowAnimDuration >= 0)
                    mArrowDrawable.setAnimationDuration(arrowAnimDuration);

                if(arrowInterpolator != null)
                    mArrowDrawable.setInterpolator(arrowInterpolator);
            }
        }
        else if(mArrowDrawable != null){
            mArrowDrawable.setCallback(null);
            mArrowDrawable = null;
        }

        if(mDividerHeight > 0){
            if(mDividerDrawable == null){
                if(dividerAnimDuration < 0)
                    dividerAnimDuration = 0;

                if(dividerColor == null){
                    int[][] states = new int[][]{
                            new int[]{-android.R.attr.state_pressed},
                            new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled},
                    };
                    int[] colors = new int[]{
                            ThemeUtil.colorControlNormal(context, 0xFF000000),
                            ThemeUtil.colorControlActivated(context, 0xFF000000),
                    };

                    dividerColor = new ColorStateList(states, colors);
                }

                mDividerDrawable = new DividerDrawable(mDividerHeight, dividerColor, dividerAnimDuration);
                mDividerDrawable.setCallback(this);
            }
            else{
                mDividerDrawable.setDividerHeight(mDividerHeight);

                if(dividerColor != null)
                    mDividerDrawable.setColor(dividerColor);

                if(dividerAnimDuration >= 0)
                    mDividerDrawable.setAnimationDuration(dividerAnimDuration);
            }
        }
        else if(mDividerDrawable != null){
            mDividerDrawable.setCallback(null);
            mDividerDrawable = null;
        }

        if (mTempAdapter != null) {
            mPopup.setAdapter(mTempAdapter);
            mTempAdapter = null;
        }

        if(mAdapter != null)
            setAdapter(mAdapter);

        if(isInEditMode()){
            TextView tv = new TextView(context, attrs, defStyleAttr);
            tv.setText("Item 1");
            super.addView(tv);
        }

        requestLayout();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
        if(mIsRtl != rtl) {
            mIsRtl = rtl;

            if(mLabelView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                mLabelView.setTextDirection(mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR);

            requestLayout();
        }
    }

    /**
     * @return The selected item's view.
     */
    public View getSelectedView() {
        View v = getChildAt(getChildCount() - 1);
		return v == mLabelView ? null : v;
	}

    /**
     * Set the selected position of this Spinner.
     * @param position The selected position.
     */
	public void setSelection(int position) {
		if(mAdapter != null)
			position = Math.max(0, Math.min(position, mAdapter.getCount() - 1));

		if(mSelectedPosition != position){
			mSelectedPosition = position;

			if(mOnItemSelectedListener != null)
				mOnItemSelectedListener.onItemSelected(this, getSelectedView(), position, mAdapter == null ? -1 : mAdapter.getItemId(position));

			onDataInvalidated();
		}
	}

    /**
     * @return The selected posiiton.
     */
	public int getSelectedItemPosition(){
		return mSelectedPosition;
	}

    /**
     * @return The selected item.
     */
    public Object getSelectedItem(){
        return mAdapter == null ? null : mAdapter.getItem(mSelectedPosition);
    }

    /**
     * @return The adapter back this Spinner.
     */
	public SpinnerAdapter getAdapter() {
		return mAdapter;
	}

    /**
     * Set an adapter for this Spinner.
     * @param adapter
     */
	public void setAdapter(SpinnerAdapter adapter) {
		if(mAdapter != null)
			mAdapter.unregisterDataSetObserver(mDataSetObserver);

        mRecycler.clear();

		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);
		onDataChanged();

        if (mPopup != null)
            mPopup.setAdapter(new DropDownAdapter(adapter));
        else
            mTempAdapter = new DropDownAdapter(adapter);
	}

    /**
     * Set the background drawable for the spinner's popup window of choices.
     *
     * @param background Background drawable
     *
     * @attr ref android.R.styleable#Spinner_popupBackground
     */
    public void setPopupBackgroundDrawable(Drawable background) {
    	mPopup.setBackgroundDrawable(background);
    }

    /**
     * Set the background drawable for the spinner's popup window of choices.
     *
     * @param resId Resource ID of a background drawable
     *
     * @attr ref android.R.styleable#Spinner_popupBackground
     */
    public void setPopupBackgroundResource(int resId) {
        setPopupBackgroundDrawable(getContext().getDrawable(resId));
    }

    /**
     * Get the background drawable for the spinner's popup window of choices.
     *
     * @return background Background drawable
     *
     * @attr ref android.R.styleable#Spinner_popupBackground
     */
    public Drawable getPopupBackground() {
        return mPopup.getBackground();
    }

    /**
     * Set a vertical offset in pixels for the spinner's popup window of choices.
     *
     * @param pixels Vertical offset in pixels
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public void setDropDownVerticalOffset(int pixels) {
        mPopup.setVerticalOffset(pixels);
    }

    /**
     * Get the configured vertical offset in pixels for the spinner's popup window of choices.
     *
     * @return Vertical offset in pixels
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public int getDropDownVerticalOffset() {
        return mPopup.getVerticalOffset();
    }

    /**
     * Set a horizontal offset in pixels for the spinner's popup window of choices.
     *
     * @param pixels Horizontal offset in pixels
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public void setDropDownHorizontalOffset(int pixels) {
        mPopup.setHorizontalOffset(pixels);
    }

    /**
     * Get the configured horizontal offset in pixels for the spinner's popup window of choices.
     *
     * @return Horizontal offset in pixels
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public int getDropDownHorizontalOffset() {
        return mPopup.getHorizontalOffset();
    }

    /**
     * Set the width of the spinner's popup window of choices in pixels. This value
     * may also be set to {@link ViewGroup.LayoutParams#MATCH_PARENT}
     * to match the width of the Spinner itself, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to wrap to the measured size
     * of contained dropdown list items.
     *
     * @param pixels Width in pixels, WRAP_CONTENT, or MATCH_PARENT
     *
     * @attr ref android.R.styleable#Spinner_dropDownWidth
     */
    public void setDropDownWidth(int pixels) {
        mDropDownWidth = pixels;
    }

    /**
     * Get the configured width of the spinner's popup window of choices in pixels.
     * The returned value may also be {@link ViewGroup.LayoutParams#MATCH_PARENT}
     * meaning the popup window will match the width of the Spinner itself, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to wrap to the measured size
     * of contained dropdown list items.
     *
     * @return Width in pixels, WRAP_CONTENT, or MATCH_PARENT
     *
     * @attr ref android.R.styleable#Spinner_dropDownWidth
     */
    public int getDropDownWidth() {
        return mDropDownWidth;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mDisableChildrenWhenDisabled) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++)
                getChildAt(i).setEnabled(enabled);
        }
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        mMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        mMinWidth = minWidth;
        super.setMinimumWidth(minWidth);
    }

    /**
     * Describes how the selected item view is positioned.
     *
     * @param gravity See {@link Gravity}
     *
     * @attr ref android.R.styleable#Spinner_gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0)
                gravity |= Gravity.START;
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    public int getBaseline() {
        View child = getSelectedView();

        if (child != null) {
            final int childBaseline = child.getBaseline();
            if(childBaseline < 0)
                return -1;

            int paddingTop = getPaddingTop();
            if(mLabelView != null)
                paddingTop += mLabelView.getMeasuredHeight();

            int remainHeight = getMeasuredHeight() - paddingTop - getPaddingBottom() - getDividerDrawableHeight();

            int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (verticalGravity) {
                case Gravity.TOP:
                    return paddingTop + childBaseline;
                case Gravity.BOTTOM:
                    return paddingTop + remainHeight - child.getMeasuredHeight() + childBaseline;
                default:
                    return (remainHeight - child.getMeasuredHeight()) / 2 + paddingTop + childBaseline;
            }
        }

        return -1;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPopup != null && mPopup.isShowing())
            mPopup.dismiss();
    }

    /**
     * Set a listener that will be called when a item's view is clicked.
     * @param l The {@link OnItemClickListener} will be called.
     */
    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    /**
     * Set a listener that will be called when an item is selected.
     * @param l The {@link OnItemSelectedListener} will be called.
     */
    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mOnItemSelectedListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	return true;
    }

    @Override
	protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mArrowDrawable == who || mDividerDrawable == who;
    }
    
    private int getArrowDrawableWidth(){
    	return mArrowDrawable != null ? mArrowSize + mArrowPadding * 2 : 0;
    }
    
    private int getDividerDrawableHeight(){
    	return mDividerHeight > 0 ? mDividerHeight + mDividerPadding : 0;
    }

    private int getSpec(int availableSize, int size){
        int spec;
        switch (size){
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                if(availableSize > 0)
                    spec = MeasureSpec.makeMeasureSpec(availableSize, MeasureSpec.AT_MOST);
                else
                    spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                break;
            case ViewGroup.LayoutParams.MATCH_PARENT:
                if(availableSize > 0)
                    spec = MeasureSpec.makeMeasureSpec(availableSize, MeasureSpec.EXACTLY);
                else
                    spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                break;
            default:
                spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
                break;
        }

        return spec;
    }

    @SuppressWarnings("Range")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    	int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    	int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    	int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingHorizontal = getPaddingLeft() + getPaddingRight() + getArrowDrawableWidth();
        int paddingVertical = getPaddingTop() + getPaddingBottom() + getDividerDrawableHeight();

        int labelWidth = 0;
        int labelHeight = 0;
        if(mLabelView != null && mLabelView.getLayoutParams() != null){
            int size = widthMode == MeasureSpec.UNSPECIFIED ? 0 : (widthSize - paddingHorizontal);
            int ws = MeasureSpec.makeMeasureSpec(size, widthMode);
            int hs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mLabelView.measure(ws, hs);
            labelWidth = mLabelView.getMeasuredWidth();
            labelHeight = mLabelView.getMeasuredHeight();
        }

    	int width = 0;
    	int height = 0;
    	
    	View v = getSelectedView();
    	if(v != null){
            ViewGroup.LayoutParams params = v.getLayoutParams();
            int ws = getSpec(widthSize - paddingHorizontal, params.width);
            int hs = getSpec(heightSize - paddingVertical - mLabelView.getMeasuredHeight(), params.height);

    		v.measure(ws, hs);
    		width = v.getMeasuredWidth();
    		height = v.getMeasuredHeight();
    	}

        width = Math.max(mMinWidth, Math.max(labelWidth, width) + paddingHorizontal);
        height = Math.max(mMinHeight, height + labelHeight + paddingVertical);

        switch (widthMode){
            case MeasureSpec.AT_MOST:
                width = Math.min(widthSize, width);
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
        }

        switch (heightMode){
            case MeasureSpec.AT_MOST:
                height = Math.min(heightSize, height);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
        }

    	setMeasuredDimension(width, height);

        if(v != null){
            ViewGroup.LayoutParams params = v.getLayoutParams();
            int viewWidth;
            int viewHeight;
            switch (params.width){
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    viewWidth = v.getMeasuredWidth();
                    break;
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    viewWidth = width - paddingHorizontal;
                    break;
                default:
                    viewWidth = params.width;
                    break;
            }
            switch (params.height){
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    viewHeight = v.getMeasuredHeight();
                    break;
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    viewHeight = height - labelHeight - paddingVertical;
                    break;
                default:
                    viewHeight = params.height;
                    break;
            }

            if(v.getMeasuredWidth() != viewWidth || v.getMeasuredHeight() != viewHeight)
                v.measure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        int arrowWidth = getArrowDrawableWidth();

        if(mArrowDrawable != null) {
            int top = getPaddingTop() + (mLabelView == null ? 0 : mLabelView.getMeasuredHeight());
            int bottom = h - getDividerDrawableHeight() - getPaddingBottom();
            if(mIsRtl)
                mArrowDrawable.setBounds(getPaddingLeft(), top, getPaddingLeft() + arrowWidth, bottom);
            else
                mArrowDrawable.setBounds(getWidth() - getPaddingRight() - arrowWidth, top, getWidth() - getPaddingRight(), bottom);
        }

        if(mDividerDrawable != null)
            mDividerDrawable.setBounds(getPaddingLeft(), h - mDividerHeight - getPaddingBottom(), w - getPaddingRight(), h - getPaddingBottom());

    	int childLeft = mIsRtl ? (getPaddingLeft() + arrowWidth) : getPaddingLeft();
		int childRight = mIsRtl ? (w - getPaddingRight()) : (w - getPaddingRight() - arrowWidth);
		int childTop = getPaddingTop();
		int childBottom = h - getPaddingBottom();

        if(mLabelView != null){
            if(mIsRtl)
                mLabelView.layout(childRight - mLabelView.getMeasuredWidth(), childTop, childRight, childTop + mLabelView.getMeasuredHeight());
            else
                mLabelView.layout(childLeft, childTop, childLeft + mLabelView.getMeasuredWidth(), childTop + mLabelView.getMeasuredHeight());
            childTop += mLabelView.getMeasuredHeight();
        }

		View v = getSelectedView();
		if(v != null){			
			int x, y;
			
			int horizontalGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
			if(horizontalGravity == Gravity.START)
                horizontalGravity = mIsRtl ? Gravity.RIGHT : Gravity.LEFT;
            else if(horizontalGravity == Gravity.END)
                horizontalGravity = mIsRtl ? Gravity.LEFT : Gravity.RIGHT;

			switch (horizontalGravity) {
				case Gravity.LEFT:
					x = childLeft;
					break;
				case Gravity.CENTER_HORIZONTAL:
					x = (childRight - childLeft - v.getMeasuredWidth()) / 2 + childLeft;
					break;
				case Gravity.RIGHT:
					x = childRight - v.getMeasuredWidth();
					break;	
				default:
					x = (childRight - childLeft - v.getMeasuredWidth()) / 2 + childLeft;
					break;
			}
			
			int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

			switch (verticalGravity) {
				case Gravity.TOP:
					y = childTop;
					break;
				case Gravity.CENTER_VERTICAL:
					y = (childBottom - childTop - v.getMeasuredHeight()) / 2 + childTop;
					break;
				case Gravity.BOTTOM:
					y = childBottom - v.getMeasuredHeight();
					break;		
				default:
					y = (childBottom - childTop - v.getMeasuredHeight()) / 2 + childTop;
					break;
			}
			
			v.layout(x, y, x + v.getMeasuredWidth(), y + v.getMeasuredHeight());
		}
    }

    @Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
        if(mDividerDrawable != null)
            mDividerDrawable.draw(canvas);
        if(mArrowDrawable != null)
            mArrowDrawable.draw(canvas);
	}
    
    @Override
    protected void drawableStateChanged() {
    	super.drawableStateChanged();
        if(mArrowDrawable != null)
            mArrowDrawable.setState(getDrawableState());
    	if(mDividerDrawable != null)
    		mDividerDrawable.setState(getDrawableState());
    }

	public boolean performItemClick(View view, int position, long id) {
        if (mOnItemClickListener != null) {
//            playSoundEffect(SoundEffectConstants.CLICK);
//            if (view != null) 
//                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            
            if(mOnItemClickListener.onItemClick(this, view, position, id))
            	setSelection(position);
            
            return true;
        }
        else
        	setSelection(position);

        return false;
    }
	
	private void onDataChanged(){
		if(mSelectedPosition == INVALID_POSITION)
			setSelection(0);
		else if(mSelectedPosition < mAdapter.getCount())
            onDataInvalidated();
		else 
			setSelection(mAdapter.getCount() - 1);
	}
	
	private void onDataInvalidated(){
		if(mAdapter == null)
			return;

        if(mLabelView == null)
            removeAllViews();
        else
            for(int i = getChildCount() - 1; i > 0; i--)
                removeViewAt(i);

		int type = mAdapter.getItemViewType(mSelectedPosition);
        View v = mAdapter.getView(mSelectedPosition, mRecycler.get(type), this);
		v.setFocusable(false);
		v.setClickable(false);

        if(v.getParent() != null)
            ((ViewGroup)v.getParent()).removeView(v);

		super.addView(v);

		mRecycler.put(type, v);
	}
	
	private void showPopup(){
		if (!mPopup.isShowing()){
            mPopup.show();
            final ListView lv = mPopup.getListView();
            if(lv != null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            	    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            	lv.setSelection(getSelectedItemPosition());
                if(mArrowDrawable != null && mArrowAnimSwitchMode)
                    lv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            lv.getViewTreeObserver().removeOnPreDrawListener(this);
                            mArrowDrawable.setMode(ArrowDrawable.MODE_UP, true);
                            return true;
                        }
                    });
            }

        }
	}
	
	private void onPopupDismissed(){
        if(mArrowDrawable != null)
		    mArrowDrawable.setMode(ArrowDrawable.MODE_DOWN, true);
	}
		
	private int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
		if (adapter == null)
            return 0;	        

        int width = 0;
        View itemView = null;
        int itemType = 0;
        
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        // Make sure the number of items we'll measure is capped. If it's a huge data set
        // with wildly varying sizes, oh well.
        int start = Math.max(0, getSelectedItemPosition());
        final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
        final int count = end - start;
        start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
        for (int i = start; i < end; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null)
                itemView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, itemView.getMeasuredWidth());
        }

        // Add background padding to measured width
        if (background != null) {
            background.getPadding(mTempRect);
            width += mTempRect.left + mTempRect.right;
        }

        return width;
    }
	
	static class SavedState extends BaseSavedState {
		
        int position;
        boolean showDropdown;

        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        SavedState(Parcel in) {
            super(in);
            position = in.readInt();
            showDropdown = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(position);
            out.writeByte((byte) (showDropdown ? 1 : 0));
        }

        @Override
        public String toString() {
            return "AbsSpinner.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " position=" + position
                    + " showDropdown=" + showDropdown + "}";
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

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.position = getSelectedItemPosition();
        ss.showDropdown = mPopup != null && mPopup.isShowing();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        setSelection(ss.position);
        
        if (ss.showDropdown) {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                final ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        showPopup();                 
                        final ViewTreeObserver vto = getViewTreeObserver();
                        if (vto != null)
                            vto.removeGlobalOnLayoutListener(this);                        
                    }
                };
                vto.addOnGlobalLayoutListener(listener);
            }
        }
    }
	
	private class SpinnerDataSetObserver extends DataSetObserver{

		@Override
		public void onChanged() {
			onDataChanged();
		}

		@Override
		public void onInvalidated() {
			onDataInvalidated();			
		}
		
	}
	
 	private class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<>();

        public void put(int type, View v) {
            mScrapHeap.put(type, v);
        }

        View get(int type) {
            View result = mScrapHeap.get(type);
            if (result != null)
                mScrapHeap.delete(type);

            return result;
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            scrapHeap.clear();
        }
    }
	
	private static class DropDownAdapter implements ListAdapter, SpinnerAdapter, OnClickListener {

        private SpinnerAdapter mAdapter;

        private ListAdapter mListAdapter;

        private AdapterView.OnItemClickListener mOnItemClickListener;
        
        /**
         * <p>Creates a new ListAdapter wrapper for the specified adapter.</p>
         *
         * @param adapter the Adapter to transform into a ListAdapter
         */
        public DropDownAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter)
                this.mListAdapter = (ListAdapter) adapter;            
        }
        
        public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        	mOnItemClickListener = listener;
        }
        
        @Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if(mOnItemClickListener != null)
				mOnItemClickListener.onItemClick(null, v, position, 0);			
		}

        public int getCount() {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        public Object getItem(int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getDropDownView(position, convertView, parent);
            v.setOnClickListener(this);
            v.setTag(position);
            return v;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return (mAdapter == null) ? null : mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call. Otherwise,
         * return true.
         */
        public boolean areAllItemsEnabled() {
            final ListAdapter adapter = mListAdapter;
            return adapter == null || adapter.areAllItemsEnabled();
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call. Otherwise,
         * return true.
         */
        public boolean isEnabled(int position) {
            final ListAdapter adapter = mListAdapter;
            return adapter == null || adapter.isEnabled(position);
        }

        public int getItemViewType(int position) {
        	final ListAdapter adapter = mListAdapter;
            if (adapter != null)
                return adapter.getItemViewType(position);
            else
                return 0;  
        }

        public int getViewTypeCount() {
        	final ListAdapter adapter = mListAdapter;
            if (adapter != null)
                return adapter.getViewTypeCount();
            else
                return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null)
                mAdapter.registerDataSetObserver(observer);            
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null)
                mAdapter.unregisterDataSetObserver(observer);            
		}
    }

	private class DropdownPopup extends ListPopupWindow {
		
        private CharSequence mHintText;

        private DropDownAdapter mAdapter;

        private ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                computeContentWidth();

                // Use super.show here to update; we don't want to move the selected
                // position or adjust other things that would be reset otherwise.
                DropdownPopup.super.show();
            }
        };
        
        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);

            setAnchorView(Spinner.this);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);
            
            setOnDismissListener(new PopupWindow.OnDismissListener() {
            	
                @SuppressWarnings("deprecation")
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
                public void onDismiss() {
                    final ViewTreeObserver vto = getViewTreeObserver();
                    if (vto != null) {
                    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    		vto.removeOnGlobalLayoutListener(layoutListener);
                    	else
                    		vto.removeGlobalOnLayoutListener(layoutListener);
                    }
                    onPopupDismissed();
                }
                
            });
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            mAdapter = (DropDownAdapter)adapter;
            mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {                	
                    Spinner.this.performItemClick(v, position, mAdapter.getItemId(position));                    
                    dismiss();
                }
            });
        }

        public CharSequence getHintText() {
            return mHintText;
        }

        public void setPromptText(CharSequence hintText) {
            mHintText = hintText;
        }

        void computeContentWidth() {
            final Drawable background = getBackground();
            int hOffset = 0;
            if (background != null) {
                background.getPadding(mTempRect);
                hOffset = mIsRtl ? mTempRect.right : -mTempRect.left;
            } else
                mTempRect.left = mTempRect.right = 0;            

            final int spinnerPaddingLeft = Spinner.this.getPaddingLeft();
            final int spinnerPaddingRight = Spinner.this.getPaddingRight();
            final int spinnerWidth = Spinner.this.getWidth();
            
            if (mDropDownWidth == WRAP_CONTENT) {
                int contentWidth = measureContentWidth((SpinnerAdapter) mAdapter, getBackground());
                final int contentWidthLimit = getContext().getResources().getDisplayMetrics().widthPixels - mTempRect.left - mTempRect.right;
                if (contentWidth > contentWidthLimit)
                    contentWidth = contentWidthLimit;
                
                setContentWidth(Math.max(contentWidth, spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight));                
            } else if (mDropDownWidth == MATCH_PARENT)
                setContentWidth(spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight);
            else
                setContentWidth(mDropDownWidth);
            
            if (mIsRtl)
                hOffset += spinnerWidth - spinnerPaddingRight - getWidth();
            else
                hOffset += spinnerPaddingLeft;
            
            setHorizontalOffset(hOffset);
        }

        public void show() {
            final boolean wasShowing = isShowing();

            computeContentWidth();            
            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();            

            if (wasShowing) {
                // Skip setting up the layout/dismiss listener below. If we were previously
                // showing it will still stick around.
                return;
            }

            // Make sure we hide if our anchor goes away.
            // TODO: This might be appropriate to push all the way down to PopupWindow,
            // but it may have other side effects to investigate first. (Text editing handles, etc.)
            final ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null)            	
                vto.addOnGlobalLayoutListener(layoutListener);
        }
    }

}
