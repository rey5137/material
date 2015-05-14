package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabPageIndicator extends HorizontalScrollView implements ViewPager.OnPageChangeListener, android.view.View.OnClickListener{

	private LinearLayout mTabContainer;	
	private ViewPager mViewPager;
	
	private int mMode;
	private int mTabPadding;
	private int mTabRippleStyle;	
	private int mTextAppearance;
		
	private int mIndicatorOffset;
	private int mIndicatorWidth;
	private int mIndicatorHeight;
	
	private Paint mPaint;
	
	public static final int MODE_SCROLL = 0;
	public static final int MODE_FIXED = 1;
	
	private int mSelectedPosition;
	private boolean mScrolling = false;
	
	private Runnable mTabAnimSelector;
	
	private ViewPager.OnPageChangeListener mListener;
	
	private DataSetObserver mObserver = new DataSetObserver(){
		
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
		
	};
	
	public TabPageIndicator(Context context) {
		super(context);
		
		init(context, null, 0, 0);
	}
	
	public TabPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs, 0, 0);
	}
	
	public TabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public TabPageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		setHorizontalScrollBarEnabled(false);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mTabContainer = new LinearLayout(context);
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabContainer.setGravity(Gravity.CENTER);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
		
		if(isInEditMode())
			addTemporaryTab();
	}

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPageIndicator, defStyleAttr, defStyleRes);

        mTabPadding = a.getDimensionPixelSize(R.styleable.TabPageIndicator_tpi_tabPadding, ThemeUtil.dpToPx(context, 12));
        mTabRippleStyle = a.getResourceId(R.styleable.TabPageIndicator_tpi_tabRipple, 0);
        int indicatorColor = a.getColor(R.styleable.TabPageIndicator_tpi_indicatorColor, ThemeUtil.colorAccent(context, 0xFFFFFFFF));
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.TabPageIndicator_tpi_indicatorHeight, ThemeUtil.dpToPx(context, 2));
        mTextAppearance = a.getResourceId(R.styleable.TabPageIndicator_android_textAppearance, 0);
        mMode = a.getInteger(R.styleable.TabPageIndicator_tpi_mode, MODE_SCROLL);

        a.recycle();

        removeAllViews();
        if(mMode == MODE_SCROLL) {
            addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setFillViewport(false);
        }
        else if(mMode == MODE_FIXED){
            addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setFillViewport(true);
        }

        mPaint.setColor(indicatorColor);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-post the selector we saved
        if (mTabAnimSelector != null)            
            post(mTabAnimSelector);        
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabAnimSelector != null) 
            removeCallbacks(mTabAnimSelector);        
    }
    
    private CheckedTextView getTabView(int position){
    	return (CheckedTextView)mTabContainer.getChildAt(position);
    }
    
    private void animateToTab(final int position) {
    	final CheckedTextView tv = getTabView(position);
    	if(tv == null)
    		return;
    	
        if (mTabAnimSelector != null) 
            removeCallbacks(mTabAnimSelector);
        
        mTabAnimSelector = new Runnable() {
            public void run() {
            	if(!mScrolling)
                	updateIndicator(tv.getLeft(), tv.getWidth());	
            	          
                smoothScrollTo(tv.getLeft() - (getWidth() - tv.getWidth()) / 2 + getPaddingLeft(), 0);
                mTabAnimSelector = null;
            }
        };
        
        post(mTabAnimSelector);
    }

    /**
     * Set a listener will be called when the current page is changed.
     * @param listener The {@link android.support.v4.view.ViewPager.OnPageChangeListener} will be called.
     */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    /**
     * Set the ViewPager associate with this indicator view.
     * @param view The ViewPager view.
     */
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) 
            return;
        
        if (mViewPager != null){
            mViewPager.setOnPageChangeListener(null);
            PagerAdapter adapter = view.getAdapter();
            if(adapter != null)
            	adapter.unregisterDataSetObserver(mObserver);
        }
                
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");        
               
        adapter.registerDataSetObserver(mObserver);
        
        mViewPager = view;        
        view.setOnPageChangeListener(this);
        
        notifyDataSetChanged();
    }

    /**
     * Set the ViewPager associate with this indicator view and the current position;
     * @param view The ViewPager view.
     * @param initialPosition The current position.
     */
    public void setViewPager(ViewPager view, int initialPosition) {
    	setViewPager(view);
        setCurrentItem(initialPosition);
    }
    
    private void updateIndicator(int offset, int width){
		mIndicatorOffset = offset;
		mIndicatorWidth = width;		
		invalidate();
	}
    	
	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		
		int x = mIndicatorOffset + getPaddingLeft();		
		canvas.drawRect(x, getHeight() - mIndicatorHeight, x + mIndicatorWidth, getHeight(), mPaint);
		
		if(isInEditMode())
			canvas.drawRect(getPaddingLeft(), getHeight() - mIndicatorHeight, getPaddingLeft() + mTabContainer.getChildAt(0).getWidth(), getHeight(), mPaint);		
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if(state == ViewPager.SCROLL_STATE_IDLE){
			mScrolling = false;
			TextView tv = getTabView(mSelectedPosition);
			if(tv != null)
				updateIndicator(tv.getLeft(), tv.getMeasuredWidth());			
		}
		else
			mScrolling = true;
		
		if (mListener != null)
			mListener.onPageScrollStateChanged(state);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if (mListener != null)
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);  
		
		CheckedTextView tv_scroll = getTabView(position);
		CheckedTextView tv_next = getTabView(position + 1);
		
		if(tv_scroll != null && tv_next != null){
			int width_scroll = tv_scroll.getWidth();
			int width_next = tv_next.getWidth();
			float distance = (width_scroll + width_next) / 2f;
					
			int width =  (int)(width_scroll + (width_next - width_scroll) * positionOffset + 0.5f);
			int offset = (int)(tv_scroll.getLeft() + width_scroll / 2f + distance * positionOffset - width / 2f + 0.5f);
			updateIndicator(offset, width);
		}		
	}

	@Override
	public void onPageSelected(int position) {		
		setCurrentItem(position);
        if (mListener != null)
            mListener.onPageSelected(position);  
	}
	
	@Override
	public void onClick(android.view.View v) {
		int position = (Integer)v.getTag();
		if(position == mSelectedPosition && mListener != null)
			mListener.onPageSelected(position);  
		
		mViewPager.setCurrentItem(position, true);
	}

    /**
     * Set the current page of this TabPageIndicator.
     * @param position The position of current page.
     */
	public void setCurrentItem(int position) {
		if(mSelectedPosition != position){
			CheckedTextView tv = getTabView(mSelectedPosition);	
			if(tv != null)
				tv.setChecked(false);
		}
		
		mSelectedPosition = position;		
		CheckedTextView tv = getTabView(mSelectedPosition);				
		if(tv != null)
			tv.setChecked(true);	
		
		animateToTab(position);
	}
	
	private void notifyDataSetChanged() {
		mTabContainer.removeAllViews();
				
		PagerAdapter adapter = mViewPager.getAdapter();
		final int count = adapter.getCount();
		
		if (mSelectedPosition > count) 
        	mSelectedPosition = count - 1;
		
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if (title == null) 
                title = "NULL";            
            
            CheckedTextView tv = new CheckedTextView(getContext());
            tv.setCheckMarkDrawable(null);
            tv.setText(title);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(getContext(), mTextAppearance);
            tv.setSingleLine(true);
            tv.setEllipsize(TruncateAt.END);
            tv.setOnClickListener(this);
            tv.setTag(i);
            if(mTabRippleStyle > 0)
                ViewUtil.setBackground(tv, new RippleDrawable.Builder(getContext(), mTabRippleStyle).build());
            	
            if(mMode == MODE_SCROLL){
            	tv.setPadding(mTabPadding, 0, mTabPadding, 0);
            	mTabContainer.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            else if(mMode == MODE_FIXED){
            	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            	params.weight = 1f;
            	mTabContainer.addView(tv, params);            	
            }
            	
        } 	
        
        setCurrentItem(mSelectedPosition);
        requestLayout();
	}
	
	private void notifyDataSetInvalidated() {
		PagerAdapter adapter = mViewPager.getAdapter();
		final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
        	TextView tv = getTabView(i);
        	
        	CharSequence title = adapter.getPageTitle(i);
            if (title == null) 
                title = "NULL";            
            
            tv.setText(title);
        }
        
        requestLayout();
	}
	
	private void addTemporaryTab(){
		for (int i = 0; i < 3; i++) {
            CharSequence title = null;
            if (i == 0) 
                title = "TAB ONE";        
            else if (i == 1) 
                title = "TAB TWO";
            else if (i == 2) 
                title = "TAB THREE";
            
            CheckedTextView tv = new CheckedTextView(getContext());
            tv.setCheckMarkDrawable(null);
            tv.setText(title);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(getContext(), mTextAppearance);
            tv.setSingleLine(true);
            tv.setEllipsize(TruncateAt.END);
            tv.setTag(i);
            tv.setChecked(i == 0);
            if(mMode == MODE_SCROLL){
            	tv.setPadding(mTabPadding, 0, mTabPadding, 0);
            	mTabContainer.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            else if(mMode == MODE_FIXED){
            	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            	params.weight = 1f;
            	mTabContainer.addView(tv, params);            	
            }            	
        } 	
	}
}
