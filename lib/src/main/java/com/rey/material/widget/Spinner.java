package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.support.v7.internal.widget.ViewUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
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
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.rey.material.R;
import com.rey.material.drawable.ArrowDrawable;
import com.rey.material.drawable.DividerDrawable;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Spinner extends FrameLayout {
		
	private static final int MAX_ITEMS_MEASURED = 15;
	
	private static final int INVALID_POSITION = -1;
	
	public interface OnItemClickListener{
		boolean onItemClick(Spinner parent, View view, int position, long id);
	}
	
	public interface OnItemSelectedListener{
		void onItemSelected(Spinner parent, View view, int position, long id);
	}

    private boolean mLabelEnable;
    private LabelView mLabelView;

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
	
	private int mSelectedPosition = INVALID_POSITION;
	
	private RecycleBin mRecycler = new RecycleBin();
	
	private Rect mTempRect = new Rect();
	
	private DropDownAdapter mTempAdapter;
	
	private SpinnerDataSetObserver mDataSetObserver = new SpinnerDataSetObserver();
	
	private TintManager mTintManager;
	
	private RippleManager mRippleManager = new RippleManager();
		
	public Spinner(Context context) {
		super(context);
		
		init(context, null, R.attr.listPopupWindowStyle, 0);
	}
	
	public Spinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, R.attr.listPopupWindowStyle, 0);
	}
	
	public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

	public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setWillNotDraw(false);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
		
		if(isInEditMode()){
			TextView tv = new TextView(context, attrs, defStyleAttr);
			tv.setText("Item 1");
			super.addView(tv);
		}
		
		setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
	}

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        CharSequence memoLabel = mLabelView == null ? null : mLabelView.getText();

        removeAllViews();
        mRippleManager.onCreate(this, context, attrs, defStyleAttr, defStyleRes);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,  R.styleable.Spinner, defStyleAttr, defStyleRes);

        mLabelEnable = a.getBoolean(R.styleable.Spinner_spn_labelEnable, false);
        if(mLabelEnable){
            mLabelView = new LabelView(context);
            mLabelView.setGravity(GravityCompat.START);
            mLabelView.setSingleLine(true);
            int labelPadding = a.getDimensionPixelOffset(R.styleable.Spinner_spn_labelPadding, 0);
            int labelTextSize = a.getDimensionPixelSize(R.styleable.Spinner_spn_labelTextSize, 0);
            ColorStateList labelTextColor = a.getColorStateList(R.styleable.Spinner_spn_labelTextColor);
            int labelTextAppearance = a.getResourceId(R.styleable.Spinner_spn_labelTextAppearance, 0);
            int labelEllipsize = a.getInteger(R.styleable.Spinner_spn_labelEllipsize, 0);
            CharSequence label = ThemeUtil.getString(a, R.styleable.Spinner_spn_label, memoLabel);

            mLabelView.setText(label);
            mLabelView.setPadding(0, 0, 0, labelPadding);
            if(labelTextAppearance > 0)
                mLabelView.setTextAppearance(context, labelTextAppearance);
            if(labelTextSize > 0)
                mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
            if(labelTextColor != null)
                mLabelView.setTextColor(labelTextColor);

            switch (labelEllipsize) {
                case 1:
                    mLabelView.setEllipsize(TextUtils.TruncateAt.START);
                    break;
                case 2:
                    mLabelView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    break;
                case 3:
                    mLabelView.setEllipsize(TextUtils.TruncateAt.END);
                    break;
                case 4:
                    mLabelView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    break;
                default:
                    mLabelView.setEllipsize(TextUtils.TruncateAt.END);
                    break;
            }
            addView(mLabelView, 0, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        mGravity = a.getInt(R.styleable.Spinner_android_gravity, Gravity.CENTER);

        setMinimumWidth(a.getDimensionPixelOffset(R.styleable.Spinner_android_minWidth, 0));
        setMinimumHeight(a.getDimensionPixelOffset(R.styleable.Spinner_android_minHeight, 0));

        mPopup = new DropdownPopup(context, attrs, defStyleAttr, defStyleRes);
        mPopup.setModal(true);
        mDropDownWidth = a.getLayoutDimension(R.styleable.Spinner_android_dropDownWidth, LayoutParams.WRAP_CONTENT);
        mPopup.setBackgroundDrawable(a.getDrawable(R.styleable.Spinner_android_popupBackground));
        mPopup.setPromptText(a.getString(R.styleable.Spinner_prompt));
        mPopup.setItemAnimation(a.getResourceId(R.styleable.Spinner_spn_popupItemAnimation, 0));
        mPopup.setItemAnimationOffset(a.getInteger(R.styleable.Spinner_spn_popupItemAnimOffset, 50));
        mDisableChildrenWhenDisabled = a.getBoolean(R.styleable.Spinner_disableChildrenWhenDisabled, false);

        mArrowAnimSwitchMode = a.getBoolean(R.styleable.Spinner_spn_arrowSwitchMode, false);
        int arrowAnimDuration = a.getInteger(R.styleable.Spinner_spn_arrowAnimDuration, 0);
        mArrowSize = a.getDimensionPixelSize(R.styleable.Spinner_spn_arrowSize, ThemeUtil.dpToPx(getContext(), 4));
        mArrowPadding = a.getDimensionPixelSize(R.styleable.Spinner_spn_arrowPadding, ThemeUtil.dpToPx(getContext(), 4));
        ColorStateList arrowColor = a.getColorStateList(R.styleable.Spinner_spn_arrowColor);
        if(arrowColor == null)
            arrowColor = ColorStateList.valueOf(ThemeUtil.colorControlNormal(context, 0xFF000000));
        int resId = a.getResourceId(R.styleable.Spinner_spn_arrowInterpolator, 0);
        Interpolator arrowInterpolator = resId != 0 ? AnimationUtils.loadInterpolator(context, resId) : null;
        boolean arrowClockwise = a.getBoolean(R.styleable.Spinner_spn_arrowAnimClockwise, true);

        mArrowDrawable = new ArrowDrawable(ArrowDrawable.MODE_DOWN, mArrowSize, arrowColor, arrowAnimDuration, arrowInterpolator, arrowClockwise);
        mArrowDrawable.setCallback(this);

        mDividerHeight = a.getDimensionPixelOffset(R.styleable.Spinner_spn_dividerHeight, 0);
        mDividerPadding = a.getDimensionPixelOffset(R.styleable.Spinner_spn_dividerPadding, 0);
        int dividerAnimDuration = a.getInteger(R.styleable.Spinner_spn_dividerAnimDuration, 0);
        ColorStateList dividerColor = a.getColorStateList(R.styleable.Spinner_spn_dividerColor);
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

        if(mDividerHeight > 0){
            mDividerDrawable = new DividerDrawable(mDividerHeight, dividerColor, dividerAnimDuration);
            mDividerDrawable.setCallback(this);
        }

        mTintManager = a.getTintManager();

        a.recycle();

        if (mTempAdapter != null) {
            mPopup.setAdapter(mTempAdapter);
            mTempAdapter = null;
        }

        if(mAdapter != null)
            setAdapter(mAdapter);
    }

	public View getSelectedView() {
        View v = getChildAt(getChildCount() - 1);
		return v == mLabelView ? null : v;
	}

	public void setSelection(int position) {
		if(mAdapter != null)
			position = Math.min(position, mAdapter.getCount() - 1);
		
		if(mSelectedPosition != position){
			mSelectedPosition = position;
			
			if(mOnItemSelectedListener != null)
				mOnItemSelectedListener.onItemSelected(this, getSelectedView(), position, mAdapter == null ? -1 : mAdapter.getItemId(position));
			
			onDataInvalidated();
		}
	}
	
	public int getSelectedItemPosition(){
		return mSelectedPosition;
	}
		
	public SpinnerAdapter getAdapter() {
		return mAdapter;
	}

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
	
    public void setPopupBackgroundDrawable(Drawable background) {
    	mPopup.setBackgroundDrawable(background);
    }

    public void setPopupBackgroundResource(int resId) {
        setPopupBackgroundDrawable(mTintManager.getDrawable(resId));
    }

    public Drawable getPopupBackground() {
        return mPopup.getBackground();
    }

    public void setDropDownVerticalOffset(int pixels) {
        mPopup.setVerticalOffset(pixels);
    }
    
    public int getDropDownVerticalOffset() {
        return mPopup.getVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int pixels) {
        mPopup.setHorizontalOffset(pixels);
    }
    
    public int getDropDownHorizontalOffset() {
        return mPopup.getHorizontalOffset();
    }
    
    public void setDropDownWidth(int pixels) {
        mDropDownWidth = pixels;
    }

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
            return childBaseline >= 0 ? child.getTop() + childBaseline : -1;
        }
            
        return -1;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mPopup != null && mPopup.isShowing())
            mPopup.dismiss();        
    }

    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        Drawable background = getBackground();
        if(background instanceof RippleDrawable && !(drawable instanceof RippleDrawable))
            ((RippleDrawable) background).setBackgroundDrawable(drawable);
        else
            super.setBackgroundDrawable(drawable);
    }

    @Override
	public void setOnClickListener(OnClickListener l) {
		if(l == mRippleManager)
			super.setOnClickListener(l);
		else{
			mRippleManager.setOnClickListener(l);
			setOnClickListener(mRippleManager);
		}
	}
    
    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }
    
    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mOnItemSelectedListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	return true;
    }
    
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
    	boolean result = super.onTouchEvent(event);
		return  mRippleManager.onTouchEvent(event) || result;
    }

    @Override
	protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || mArrowDrawable == who || mDividerDrawable == who;
    }
    
    private int getArrowDrawableWidth(){
    	return mArrowSize + mArrowPadding * 2;
    }
    
    private int getDividerDrawableHeight(){
    	return mDividerHeight > 0 ? mDividerHeight + mDividerPadding : 0;
    }
    
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
        if(mLabelView != null){
            mLabelView.measure(MeasureSpec.makeMeasureSpec(widthSize - paddingHorizontal, widthMode), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            labelWidth = mLabelView.getMeasuredWidth();
            labelHeight = mLabelView.getMeasuredHeight();
        }

    	int width = 0;
    	int height = 0;
    	
    	View v = getSelectedView();
    	if(v != null){
            int ws;
            int hs;
            ViewGroup.LayoutParams params = v.getLayoutParams();
            switch (params.width){
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    ws = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    break;
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    ws = MeasureSpec.makeMeasureSpec(widthSize - paddingHorizontal, widthMode);
                    break;
                default:
                    ws = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY);
                    break;
            }
            switch (params.height){
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    hs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    break;
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    hs = MeasureSpec.makeMeasureSpec(heightSize - paddingVertical - labelHeight, heightMode);
                    break;
                default:
                    hs = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
                    break;
            }

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

        width -= paddingHorizontal;
        height -= labelHeight + paddingVertical;

        if(v != null && (v.getMeasuredWidth() != width || v.getMeasuredHeight() != height))
            v.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	int childLeft = getPaddingLeft();
		int childRight = r - l - getPaddingRight() - getArrowDrawableWidth();
		int childTop = getPaddingTop();
		int childBottom = b - t - getPaddingBottom();

        if(mLabelView != null){
            mLabelView.layout(childLeft, childTop, childLeft + mLabelView.getMeasuredWidth(), childTop + mLabelView.getMeasuredHeight());
            childTop += mLabelView.getMeasuredHeight();
        }

		View v = getSelectedView();
		if(v != null){			
			int x, y;
			
			int horizontalGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
			
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
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mArrowDrawable.setBounds(w - getArrowDrawableWidth() - getPaddingRight(), getPaddingTop() + (mLabelView == null ? 0 : mLabelView.getMeasuredHeight()), w - getPaddingRight(), h - getDividerDrawableHeight() - getPaddingBottom());
		if(mDividerDrawable != null)
			mDividerDrawable.setBounds(getPaddingLeft(), h - mDividerHeight - getPaddingBottom(), w - getPaddingRight(), h - getPaddingBottom());
	}
    
    @Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		mArrowDrawable.draw(canvas);
		if(mDividerDrawable != null)
			mDividerDrawable.draw(canvas);		
	}
    
    @Override
    protected void drawableStateChanged() {
    	super.drawableStateChanged();
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
            }
            if(mArrowAnimSwitchMode)
            	mArrowDrawable.setMode(ArrowDrawable.MODE_UP, true);
        }
	}
	
	private void onPopupDismissed(){
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
            itemView = adapter.getView(i, itemView, null);
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

    private class LabelView extends android.widget.TextView{

        public LabelView(Context context) {
            super(context);
        }

        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            return Spinner.this.getDrawableState();
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
                hOffset = ViewUtils.isLayoutRtl(Spinner.this) ? mTempRect.right : -mTempRect.left;
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
            
            if (ViewUtils.isLayoutRtl(Spinner.this))
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
