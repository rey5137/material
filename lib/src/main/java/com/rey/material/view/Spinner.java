package com.rey.material.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.support.v7.internal.widget.ViewUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
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
import com.rey.material.drawable.ArrowDrawable;
import com.rey.material.drawable.DividerDrawable;
import com.rey.material.util.ThemeUtil;

public class Spinner extends ViewGroup {
		
	private static final int MAX_ITEMS_MEASURED = 15;
	
	private static final int INVALID_POSITION = -1;
	
	public interface OnItemClickListener{
		boolean onItemClick(Spinner parent, View view, int position, long id);
	}
	
	public interface OnItemSelectedListener{
		void onItemSelected(Spinner parent, View view, int position, long id);
	}
	
	private SpinnerAdapter mAdapter;
	private OnItemClickListener mOnItemClickListener;
	private OnItemSelectedListener mOnItemSelectedListener;
	
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
		
		init(context, null, R.attr.listPopupWindowStyle);
	}
	
	public Spinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, R.attr.listPopupWindowStyle);
	}
	
	public Spinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context, attrs, defStyle);
	}

	public void init(Context context, AttributeSet attrs, int defStyle) {	
		mRippleManager.onCreate(this, context, attrs, defStyle);
				
		TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,  R.styleable.Spinner, defStyle, 0);
		
		mGravity = a.getInt(R.styleable.Spinner_android_gravity, Gravity.CENTER);
		
		mPopup = new DropdownPopup(context, attrs, defStyle);
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
        
        mDividerHeight = a.getDimensionPixelSize(R.styleable.Spinner_spn_dividerHeight, 0);
        mDividerPadding = a.getDimensionPixelSize(R.styleable.Spinner_spn_dividerPadding, 0);
        int dividerAnimDuration = a.getInteger(R.styleable.Spinner_spn_dividerAnimDuration, 0);
        ColorStateList dividerColor = a.getColorStateList(R.styleable.Spinner_spn_dividerColor);
        if(dividerColor == null)
        	dividerColor = ColorStateList.valueOf(ThemeUtil.colorControlNormal(context, 0xFF000000));
        
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
		
		if(isInEditMode()){
			TextView tv = new TextView(context, attrs, defStyle);
			tv.setText("Item 1");
			super.addView(tv);
		}
		
		setClickable(true);
	}
	
	public View getSelectedView() {
		return getChildAt(0);
	}

	public void setSelection(int position) {
		if(mAdapter != null)
			position = Math.min(position, mAdapter.getCount() - 1);
		
		if(mSelectedPosition != position){
			mSelectedPosition = position;
			
			if(mOnItemSelectedListener != null)
				mOnItemSelectedListener.onItemSelected(this, getSelectedView(), position, mAdapter == null ? -1 : mAdapter.getItemViewType(position));
			
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
		
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);
		onDataChanged();

        mRecycler.clear();
        
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
        View child = null;

        if (getChildCount() > 0)
            child = getChildAt(0);
        else if (mAdapter != null && mAdapter.getCount() > 0){
            child = mAdapter.getView(0, null, this);
            mRecycler.put(0, child);
        }

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
    public void addView(@NonNull View child) {
    	//Do nothing
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
    	    	    	
    	if(widthMode != MeasureSpec.UNSPECIFIED)
    		widthSize -= getPaddingLeft() + getPaddingRight() + getArrowDrawableWidth();
    	
    	if(heightMode != MeasureSpec.UNSPECIFIED)
    		heightSize -= getPaddingTop() + getPaddingBottom() + getDividerDrawableHeight();
    	
    	int width = 0;
    	int height = 0;
    	
    	View v = getSelectedView();
    	if(v != null){
    		v.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec(heightSize, heightMode));
    		width = v.getMeasuredWidth();
    		height = v.getMeasuredHeight();
    	}
    	
    	setMeasuredDimension(width + getPaddingLeft() + getPaddingRight() + getArrowDrawableWidth(), height + getPaddingTop() + getPaddingBottom() + getDividerDrawableHeight());    	
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	int childLeft = getPaddingLeft();
		int childRight = r - l - getPaddingRight() - getArrowDrawableWidth();
		int childTop = getPaddingTop();
		int childBottom = b - t - getPaddingBottom();
				
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
		mArrowDrawable.setBounds(w - getArrowDrawableWidth() - getPaddingRight(), getPaddingTop(), w - getPaddingRight(), h - getDividerDrawableHeight() - getPaddingBottom());
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
    
    @Override
    public boolean performClick() {
        boolean handled = super.performClick();
        
        if (!handled) {
            handled = true;
            showPopup();
        }

        return handled;
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
		else if(mSelectedPosition < mAdapter.getCount()){
			int type = mAdapter.getItemViewType(mSelectedPosition);		
			View v = mRecycler.get(type);		
			v = mAdapter.getView(mSelectedPosition, v, this);
			v.setFocusable(false);
			v.setClickable(false);
			
			removeAllViews();
			LayoutParams params = v.getLayoutParams();
			if(params == null)
				params = generateDefaultLayoutParams();
			
			super.addView(v, params);
			
			mRecycler.put(type, v);
		}
		else 
			setSelection(mAdapter.getCount() - 1);
	}
	
	private void onDataInvalidated(){
		if(mAdapter == null)
			return;
		
		int type = mAdapter.getItemViewType(mSelectedPosition);		
		View v = mRecycler.get(type);		
		v = mAdapter.getView(mSelectedPosition, v, this);
		v.setFocusable(false);
		v.setClickable(false);
		
		removeAllViews();
		super.addView(v);
		mRecycler.put(type, v);
	}
	
	private void showPopup(){
		if (!mPopup.isShowing()){
            mPopup.show();
            final ListView lv = mPopup.getListView();
            if(lv != null){
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

        /**
         * Constructor called from {@link AbsSpinnerCompat#onSaveInstanceState()}
         */
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

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }

        View get(int position) {
            // System.out.print("Looking for " + position);
            View result = mScrapHeap.get(position);
            if (result != null)
                mScrapHeap.delete(position);

            return result;
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
//            final int count = scrapHeap.size();
//            for (int i = 0; i < count; i++) {
//                final View view = scrapHeap.valueAt(i);
//            }
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
        
        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

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
