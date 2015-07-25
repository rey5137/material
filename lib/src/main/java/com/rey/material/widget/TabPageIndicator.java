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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabPageIndicator extends HorizontalScrollView implements ViewPager.OnPageChangeListener, android.view.View.OnClickListener, ThemeManager.OnThemeChangedListener{

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

	private TabContainerLayout mTabContainer;
	private ViewPager mViewPager;
	
	private int mMode;
	private int mTabPadding = -1;
	private int mTabRippleStyle = 0;
	private int mTextAppearance = 0;
    private boolean mTabSingleLine = true;
		
	private int mIndicatorOffset;
	private int mIndicatorWidth;
	private int mIndicatorHeight = -1;
	
	private Paint mPaint;
	
	public static final int MODE_SCROLL = 0;
	public static final int MODE_FIXED = 1;
	
	private int mSelectedPosition;
	private boolean mScrolling = false;
    private boolean mIsRtl = false;
	
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
        mPaint.setColor(ThemeUtil.colorAccent(context, 0xFFFFFFFF));

        mTabContainer = new TabContainerLayout(context);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);
		
		if(isInEditMode())
			addTemporaryTab();

        mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
	}

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPageIndicator, defStyleAttr, defStyleRes);

        int textAppearance = 0;
        int mode = -1;
        int rippleStyle = 0;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);
            if(attr == R.styleable.TabPageIndicator_tpi_tabPadding)
                mTabPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_tabRipple)
                rippleStyle = a.getResourceId(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_indicatorColor)
                mPaint.setColor(a.getColor(attr, 0));
            else if(attr == R.styleable.TabPageIndicator_tpi_indicatorHeight)
                mIndicatorHeight = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_tabSingleLine)
                mTabSingleLine = a.getBoolean(attr, true);
            else if(attr == R.styleable.TabPageIndicator_android_textAppearance)
                textAppearance = a.getResourceId(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_mode)
                mode = a.getInteger(attr, 0);
        }

        a.recycle();

        if(mTabPadding < 0)
            mTabPadding = ThemeUtil.dpToPx(context, 12);

        if(mIndicatorHeight < 0)
            mIndicatorHeight = ThemeUtil.dpToPx(context, 2);

        if(mode >= 0){
            if(mMode != mode || getChildCount() == 0){
                mMode = mode;
                removeAllViews();
                if(mMode == MODE_SCROLL) {
                    addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    setFillViewport(false);
                }
                else if(mMode == MODE_FIXED){
                    addView(mTabContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    setFillViewport(true);
                }
            }
        }

        if(textAppearance != 0 && mTextAppearance != textAppearance){
            mTextAppearance = textAppearance;
            for(int i = 0, count = mTabContainer.getChildCount(); i < count; i++){
                CheckedTextView tv = (CheckedTextView)mTabContainer.getChildAt(i);
                tv.setTextAppearance(context, mTextAppearance);
            }
        }

        if(rippleStyle != 0 && rippleStyle != mTabRippleStyle){
            mTabRippleStyle = rippleStyle;
            for(int i = 0, count = mTabContainer.getChildCount(); i < count; i++)
                ViewUtil.setBackground(mTabContainer.getChildAt(i), new RippleDrawable.Builder(getContext(), mTabRippleStyle).build());
        }

        if(mViewPager != null)
            notifyDataSetChanged();
        requestLayout();
    }

    @Override
    public void onThemeChanged(ThemeManager.OnThemeChangedEvent event) {
        int style = ThemeManager.getInstance().getCurrentStyle(mStyleId);
        if(mCurrentStyle != style){
            mCurrentStyle = style;
            applyStyle(mCurrentStyle);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-post the selector we saved
        if (mTabAnimSelector != null)            
            post(mTabAnimSelector);

        if(mStyleId != 0) {
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
            onThemeChanged(null);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabAnimSelector != null) 
            removeCallbacks(mTabAnimSelector);

        if(mStyleId != 0)
            ThemeManager.getInstance().unregisterOnThemeChangedListener(this);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
        if(mIsRtl != rtl) {
            mIsRtl = rtl;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int ws = widthMeasureSpec;
        if(ws != MeasureSpec.UNSPECIFIED)
            ws = MeasureSpec.makeMeasureSpec(widthSize - getPaddingLeft() - getPaddingRight(), widthMode);

        int hs = heightMeasureSpec;
        if(heightMode != MeasureSpec.UNSPECIFIED)
            hs = MeasureSpec.makeMeasureSpec(heightSize - getPaddingTop() - getPaddingBottom(), heightMode);

        mTabContainer.measure(ws, hs);

        int width = 0;
        switch (widthMode){
            case MeasureSpec.UNSPECIFIED:
                width = mTabContainer.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(mTabContainer.getMeasuredWidth() + getPaddingLeft() + getPaddingRight(), widthSize);
                break;
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
        }

        int height = 0;
        switch (heightMode){
            case MeasureSpec.UNSPECIFIED:
                height = mTabContainer.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(mTabContainer.getMeasuredHeight() + getPaddingTop() + getPaddingBottom(), heightSize);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
        }

        if(mTabContainer.getMeasuredWidth() != width - getPaddingLeft() - getPaddingRight() || mTabContainer.getMeasuredHeight() != height - getPaddingTop() - getPaddingBottom())
            mTabContainer.measure(MeasureSpec.makeMeasureSpec(width - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        TextView tv = getTabView(mSelectedPosition);
        if(tv != null)
            updateIndicator(tv.getLeft(), tv.getMeasuredWidth());
    }

    private CheckedTextView getTabView(int position){
    	return (CheckedTextView)mTabContainer.getChildAt(position);
    }
    
    private void animateToTab(final int position) {
    	if(getTabView(position) == null)
    		return;
    	
        if (mTabAnimSelector != null) 
            removeCallbacks(mTabAnimSelector);
        
        mTabAnimSelector = new Runnable() {
            public void run() {
                CheckedTextView tv = getTabView(position);
            	if(!mScrolling) {
                    updateIndicator(tv.getLeft(), tv.getMeasuredWidth());
                }
            	          
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
            mViewPager.removeOnPageChangeListener(this);
            PagerAdapter adapter = view.getAdapter();
            if(adapter != null)
            	adapter.unregisterDataSetObserver(mObserver);
        }
                
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");        
               
        adapter.registerDataSetObserver(mObserver);
        
        mViewPager = view;        
        view.addOnPageChangeListener(this);
        
        notifyDataSetChanged();
        onPageSelected(mViewPager.getCurrentItem());
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
			if(tv != null) {
                updateIndicator(tv.getLeft(), tv.getMeasuredWidth());
            }
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
			int width_scroll = tv_scroll.getMeasuredWidth();
			int width_next = tv_next.getMeasuredWidth();
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
            if(mTabSingleLine)
                tv.setSingleLine(true);
            else {
                tv.setSingleLine(false);
                tv.setMaxLines(2);
            }
            tv.setEllipsize(TruncateAt.END);
            tv.setOnClickListener(this);
            tv.setTag(i);
            if(mTabRippleStyle > 0)
                ViewUtil.setBackground(tv, new RippleDrawable.Builder(getContext(), mTabRippleStyle).build());

            tv.setPadding(mTabPadding, 0, mTabPadding, 0);
            mTabContainer.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

    private class TabContainerLayout extends FrameLayout{

        public TabContainerLayout(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);

            int width = 0;
            int height = 0;

            if(mMode == MODE_SCROLL){
                int ws = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.measure(ws, heightMeasureSpec);
                    width += child.getMeasuredWidth();
                    height = Math.max(height, child.getMeasuredHeight());
                }
                setMeasuredDimension(width, height);
            }
            else{
                if(widthMode != MeasureSpec.EXACTLY){
                    int ws = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    for (int i = 0; i < getChildCount(); i++) {
                        View child = getChildAt(i);
                        child.measure(ws, heightMeasureSpec);
                        width += child.getMeasuredWidth();
                        height = Math.max(height, child.getMeasuredHeight());
                    }

                    if(widthMode == MeasureSpec.UNSPECIFIED || width < widthSize)
                        setMeasuredDimension(widthSize, height);
                    else{
                        int childWidth = widthSize / getChildCount();
                        for (int i = 0, count = getChildCount(); i < count; i++) {
                            View child = getChildAt(i);
                            if(i != count - 1)
                                child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
                            else
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize - childWidth * (count - 1), MeasureSpec.EXACTLY), heightMeasureSpec);
                        }
                        setMeasuredDimension(widthSize, height);
                    }
                }
                else {
                    int childWidth = widthSize / getChildCount();
                    for (int i = 0, count = getChildCount(); i < count; i++) {
                        View child = getChildAt(i);
                        if(i != count - 1)
                            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
                        else
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize - childWidth * (count - 1), MeasureSpec.EXACTLY), heightMeasureSpec);
                        height = Math.max(height, child.getMeasuredHeight());
                    }
                    setMeasuredDimension(widthSize, height);
                }
            }

            int hs = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if(child.getMeasuredHeight() != height)
                    child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY), hs);
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            if(mIsRtl)
                for(int i = 0, count = getChildCount(); i < count; i++){
                    View child = getChildAt(i);
                    child.layout(childRight - child.getMeasuredWidth(), childTop, childRight, childBottom);
                    childRight -= child.getMeasuredWidth();
                }
            else
                for(int i = 0, count = getChildCount(); i < count; i++){
                    View child = getChildAt(i);
                    child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childBottom);
                    childLeft += child.getMeasuredWidth();
                }
        }
    }
}
